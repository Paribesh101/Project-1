# Bank of CLI

A banking app that runs in the terminal. You can make an account, log in with a PIN, check your balance, deposit, withdraw, send money to another account, and see your past transactions.

Made with Java, PostgreSQL, JDBC, Maven, and JUnit 5.

## How it's set up

There are three layers:

```
BankMenu              the menu the user sees
    |
AccountService        the banking rules
    |
AccountRepository     the SQL
TransactionRepository
    |
PostgreSQL
```

Each layer only talks to the one under it. BankMenu doesn't write any SQL, and the repositories don't decide if something is allowed.

BankMenu prints the menu and reads what the user types.

AccountService has the rules. Before it does anything it checks that the amount is positive, the account exists, and there's enough money. It also calls both repositories, because a deposit means changing the balance and also saving a record of it.

AccountRepository and TransactionRepository run the SQL and turn rows from the database into Java objects.

Account and Transaction are just classes that hold data. They get passed between the layers.

## Database

```sql
CREATE TABLE accounts (
    account_id SERIAL PRIMARY KEY,
    pin VARCHAR(255) NOT NULL,        -- this is the hashed pin, not the real one
    balance DECIMAL(10, 2) NOT NULL DEFAULT 0.00
);

CREATE TABLE transactions (
    transaction_id SERIAL PRIMARY KEY,
    account_id INTEGER,
    related_account_id INTEGER,        -- the other account if it's a transfer
    transaction_type VARCHAR(20),      -- DEPOSIT, WITHDRAW, TRANSFER
    amount DECIMAL(10, 2),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(account_id)
);
```

I used DECIMAL for money in the database and BigDecimal in Java. Doubles round wrong and you can't do that with money.

## How to run it

You need Java 17+, Maven, and PostgreSQL.

Make the database:

```bash
psql postgres -c "CREATE DATABASE bankofcli;"
```

Connect to it and run the two CREATE TABLE statements above:

```bash
psql bankofcli
```

Put your PostgreSQL username and password in DatabaseConnection.java:

```java
private static final String URL = "jdbc:postgresql://localhost:5432/bankofcli";
private static final String USER = "your_username";
private static final String PASSWORD = "your_password";
```

Then run it:

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="com.bankofcli.Main"
```

## Tests

```bash
mvn test
```

There are 27 tests. Two for each method in the service and repository layers, one that should pass and one that should fail.

## Things worth explaining

### Transfers

A transfer changes two accounts. If only one of them changes then money gets made or lost, so both have to happen or neither does.

That's why AccountRepository.transfer handles the connection itself instead of using try-with-resources. It turns off auto-commit, runs both updates, and only commits if they both worked:

```java
conn.setAutoCommit(false);
// take money from the first account, check rows affected
// add money to the second account, check rows affected
conn.commit();
```

If something goes wrong it rolls back and nothing changes.

### A bug I found

I wrote a test that transferred money to an account that doesn't exist. I thought it would fail but it returned true.

The reason is that an UPDATE that doesn't match any rows isn't an error in SQL. It just says it worked. So the money came out of the first account, the second update did nothing, no exception happened, and it committed. The money was just gone.

I fixed it by checking the number that executeUpdate() gives back, which is how many rows it changed, and rolling back if it's zero.

I never hit this running the app myself because AccountService checks both accounts exist before it calls the repository. Only the test found it, because the test calls the repository directly.

### PINs

PINs get hashed with BCrypt before saving, so the real PIN is never in the database:

```java
String hashedPin = BCrypt.hashpw(pin, BCrypt.gensalt());
```

gensalt() makes a random salt every time, so two people with the same PIN get different hashes. Logging in doesn't decrypt anything. It hashes what you typed using the salt that's already inside the stored hash and checks if they match.

### Transaction history

Every deposit, withdrawal, and transfer that works saves a row in the transactions table with the type, amount, and time.

### Logging

Logins and failed logins get written to bank.log. Database errors go in the log file instead of showing up on screen so the user doesn't see a stack trace.

## Things that could be better

- The tests use the real database, so the balance changes and new accounts get added every time I run them. They still pass because I wrote them to check the difference instead of an exact number, but it would be better to use a separate test database.
- The balance update and the transaction record use two different connections, so if the program crashed in between you'd have a balance change with no record of it.
- Transfers only show up in the sender's history. getTransactionsByAccountId only looks at account_id, and a transfer puts the other account in related_account_id, so the person getting the money doesn't see it.
- Nothing handles two things happening at once. Two transfers on the same account at the same time could mess each other up.

## Files

```
src/
  main/java/com/bankofcli/
    Main.java
    api/BankMenu.java
    business/AccountService.java
    model/Account.java
    model/Transaction.java
    repository/AccountRepository.java
    repository/TransactionRepository.java
    repository/DatabaseConnection.java
    util/LoggerUtil.java
  test/java/com/bankofcli/
    business/AccountServiceTest.java
    repository/AccountRepositoryTest.java
pom.xml
```