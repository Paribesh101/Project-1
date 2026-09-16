# Bank of CLI

This is a banking application that runs in the terminal. A user can register an account, log in with a PIN, check their balance, deposit money, withdraw money, transfer money to another account, and look at their transaction history. It's written in Java and uses PostgreSQL for the database, with JDBC to connect to it. Maven handles the build and JUnit 5 is used for the tests. The database runs in Docker so it can be set up with a single command.

The application is split into three layers, and each layer only calls the one below it.

BankMenu is the API layer. It's the part the user actually sees. It prints the menu, reads whatever they type in, converts it to the right type, and prints the result back out. It doesn't contain any SQL and it doesn't decide whether an operation is allowed.

AccountService is the business layer and it's where all the banking rules are. Before anything happens it checks that the amount is positive, that the account actually exists, and that there's enough money in it. If any of those checks fail it prints a message and stops right there. Only when everything passes does it call the repository. It also calls both repositories when an operation works, because something like a deposit means changing the balance and also saving a record of what happened.

AccountRepository and TransactionRepository are the repository layer and they're the only classes that run SQL. They open a connection, run the query, and turn the rows that come back into Java objects. They don't make any decisions, they just do what they're told.

Account and Transaction are model classes that hold data. They aren't really a layer, they're just what the data looks like while it's being passed between the layers.

## Database

There are two tables. The accounts table has account_id, pin, and balance. The transactions table has transaction_id, account_id, related_account_id, transaction_type, amount, and timestamp. One account can have many transactions, so account_id in the transactions table is a foreign key pointing back to accounts.

The pin column is VARCHAR(255) because it doesn't hold the actual PIN, it holds a BCrypt hash, and those come out to 60 characters. The related_account_id column is only used for transfers, since a deposit or withdrawal only involves one account, so it's null most of the time.

All the money columns use DECIMAL in the database and BigDecimal in Java. Doubles round incorrectly and that isn't acceptable when you're dealing with money.

## How to run it

You need Java 17 or higher, Maven, and Docker Desktop installed. Make sure Docker Desktop is actually running before you start.

Clone the repo and cd into it, then start the database:


docker compose up -d


That spins up a PostgreSQL container and runs init.sql automatically, which creates both tables. You don't have to set up a database yourself or change any connection settings.

Give it a few seconds, then check the tables are there:


docker compose exec db psql -U bankuser -d bankofcli -c "\dt"


Then build and run the application:


mvn clean compile
mvn exec:java -Dexec.mainClass="com.bankofcli.Main"


The database starts empty, so register an account first. It'll give you an account ID, and you use that with your PIN to log in.

When you're done, stop the container with `docker compose down`. If you want to wipe the data and start fresh, use `docker compose down -v`.

One thing to watch out for: if you already have PostgreSQL running locally on port 5432, the container won't be able to start. Stop your local instance first.

To run the tests, use `mvn test`. There are 27 of them, two for every method in the service and repository layers. One test checks that the method works when it should and the other checks that it fails properly when it should.

## Transfers

Transfers were the most complicated part. A transfer changes two accounts, and if only one of them ends up changing then money either gets created or destroyed. So both updates have to happen together or neither of them can.

That's why AccountRepository.transfer manages its own connection instead of using try-with-resources like the other methods do. It turns off auto-commit, runs both updates, and only commits if both of them worked. If anything goes wrong it rolls back and neither account changes.

## A bug I found

I wrote a test that transferred money to an account that doesn't exist. I expected it to fail, but it came back true.

The reason is that an UPDATE statement that doesn't match any rows isn't actually an error in SQL. It just succeeds without doing anything. So the money came out of the first account, the second update did nothing at all, no exception was ever thrown, and the whole thing committed. The money just disappeared.

I fixed it by checking the number that executeUpdate() returns, which is how many rows it changed, and rolling back if it comes back as zero.

The interesting part is that I had run the application manually plenty of times and never hit this. AccountService checks that both accounts exist before it calls the repository, so going through the menu you never reach the repository's missing check. Only a unit test could find it, because the test calls the repository directly and skips the service.

## PINs

PINs get hashed with BCrypt before they go into the database, so the real PIN is never stored anywhere. gensalt() generates a new random salt every time, which means two people who pick the same PIN still end up with completely different hashes. When someone logs in, nothing gets decrypted. It takes the PIN they typed, hashes it using the salt that's stored inside the existing hash, and checks whether the two match.

## Logging

Successful logins and failed login attempts get written to bank.log. Database errors go into the log file instead of being printed on screen, so the user gets a normal message instead of a stack trace.

## Things I'd fix

The tests run against the real database, so every time I run them the balances change and new accounts get created. They still pass because I wrote them to check the difference rather than an exact number, but I'd use a separate test database if I did it again.

The balance update and the transaction record go through two different connections, so if the program crashed in between them you'd have a balance that changed with no record of it.

Transfers only show up in the sender's transaction history. getTransactionsByAccountId only filters on account_id, and a transfer stores the other account in related_account_id, so the person receiving the money doesn't see it in their history.

There's nothing handling two operations happening at the same time. The transaction gives atomicity but not isolation, so two transfers on the same account at once could interfere with each other.