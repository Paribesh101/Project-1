Bank of CLI

A terminal banking application. Register, log in with a PIN, check your balance, deposit, withdraw, transfer money, and view transaction history.

Java, PostgreSQL, JDBC, Maven, JUnit 5. Database runs in Docker.

Layers

BankMenu prints the menu and reads input. No SQL.

AccountService holds the rules — checks the amount is positive, the account exists, and there's enough money before anything happens. Calls both repositories, since a deposit updates a balance and saves a record.

AccountRepository and TransactionRepository run the SQL and turn rows into objects.

Account and Transaction hold data as it moves between layers.

Each layer only calls the one below it.

Database

accounts — account_id, pin, balance
transactions — transaction_id, account_id, related_account_id, transaction_type, amount, timestamp

One account has many transactions. The pin column is VARCHAR(255) because it holds a BCrypt hash, not the PIN. related_account_id is only used for transfers. Money uses DECIMAL and BigDecimal, since doubles round incorrectly.

Running it

Needs Java 17+, Maven, and Docker Desktop running.

bash
docker compose up -d
mvn clean compile
mvn exec:java -Dexec.mainClass="com.bankofcli.Main"

Accounts 1, 2, and 3 are seeded with PIN 9999.

Stop with docker compose down -v. If PostgreSQL is already on port 5432 locally, stop it first.

Tests: mvn test — 27 of them, one positive and one negative per method.

Logging

Logs go to bank.log, generated at runtime and not committed. Log in with account 1 / PIN 9999, then again with a wrong PIN, then cat bank.log for an INFO and a SEVERE entry. Database errors go to the log instead of the screen.

Transfers

A transfer changes two accounts — if only one changes, money is created or destroyed. So AccountRepository.transfer turns off auto-commit, runs both updates, and commits only if both worked. Otherwise it rolls back.

A bug I found

I tested transferring to an account that doesn't exist, expecting it to fail. It returned true.

An UPDATE matching zero rows isn't an error in SQL — it succeeds without doing anything. The money left the first account, the second update did nothing, nothing threw, and it committed. The money was gone.

Fixed by checking the row count executeUpdate() returns and rolling back on zero.

Manual testing never caught it, because AccountService validates both accounts before calling the repository. Only a unit test hitting the repository directly could reach it.

PINs

Hashed with BCrypt before storage, so the real PIN is never in the database. gensalt() makes a new salt each time, so identical PINs produce different hashes. Login re-hashes what you typed using the stored salt and compares — nothing is decrypted.

Things I'd fix
Tests run against the real database and mutate it each run
The balance update and transaction record use separate connections, so a crash between them loses the record
Transfers only appear in the sender's history
No concurrency handling — atomicity but not isolation