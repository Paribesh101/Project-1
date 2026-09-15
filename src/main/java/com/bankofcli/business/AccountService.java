package com.bankofcli.business;

import java.math.BigDecimal;
import java.util.List;

import com.bankofcli.model.Account;
import com.bankofcli.model.Transaction;
import com.bankofcli.repository.AccountRepository;
import com.bankofcli.repository.TransactionRepository;
import com.bankofcli.util.LoggerUtil;

import org.mindrot.jbcrypt.BCrypt;

public class AccountService {

    private AccountRepository accountRepository = new AccountRepository();
    private TransactionRepository transactionRepository = new TransactionRepository();

    public void deposit(int accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Deposit amount must be positive!");
            return;
        }

        Account account = accountRepository.findByAccountId(accountId);

        if (account == null) {
            System.out.println("Account not found!");
            return;
        }

        BigDecimal newBalance = account.getBalance().add(amount);
        accountRepository.updateBalance(accountId, newBalance);
        transactionRepository.recordTransaction(accountId, null, "DEPOSIT", amount);
    }

    public void withdraw(int accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Withdrawal amount must be positive!");
            return;
        }

        Account account = accountRepository.findByAccountId(accountId);

        if (account == null) {
            System.out.println("Account not found!");
            return;
        }

        if (account.getBalance().compareTo(amount) < 0) {
            System.out.println("Insufficient funds!");
            return;
        }

        BigDecimal newBalance = account.getBalance().subtract(amount);
        accountRepository.updateBalance(accountId, newBalance);
        transactionRepository.recordTransaction(accountId, null, "WITHDRAW", amount);
    }

    public boolean transfer(int sourceId, int destId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Transfer amount must be positive!");
            return false;
        }

        Account source = accountRepository.findByAccountId(sourceId);
        Account destination = accountRepository.findByAccountId(destId);

        if (source == null || destination == null) {
            System.out.println("One or both accounts not found!");
            return false;
        }

        if (source.getBalance().compareTo(amount) < 0) {
            System.out.println("Insufficient funds for transfer!");
            return false;
        }

        boolean success = accountRepository.transfer(sourceId, destId, amount);
        if (success) {
            transactionRepository.recordTransaction(sourceId, destId, "TRANSFER", amount);
        }
        return success;
    }

    public int register(String pin, BigDecimal initialBalance) {
        if (pin == null || pin.trim().isEmpty()) {
            System.out.println("PIN cannot be empty!");
            return -1;
        }

        if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            System.out.println("Initial balance cannot be negative!");
            return -1;
        }

        String hashedPin = BCrypt.hashpw(pin, BCrypt.gensalt());
        return accountRepository.createAccount(hashedPin, initialBalance);
    }

    public boolean login(int accountId, String pin) {
    Account account = accountRepository.findByAccountId(accountId);
    if (account == null) {
        LoggerUtil.error("Login attempt for non-existent account: " + accountId);
        return false;
    }
    boolean success = BCrypt.checkpw(pin, account.getPin());
    if (success) {
        LoggerUtil.info("User successfully logged in: account " + accountId);
    } else {
        LoggerUtil.error("Incorrect PIN entered for account: " + accountId);
    }
    return success;
}

    public Account getAccount(int accountId) {
        return accountRepository.findByAccountId(accountId);
    }

    public List<Transaction> getHistory(int accountId) {
        return transactionRepository.getTransactionsByAccountId(accountId);
    }
}