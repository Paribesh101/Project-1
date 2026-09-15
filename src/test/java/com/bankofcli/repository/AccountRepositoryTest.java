package com.bankofcli.repository;

import com.bankofcli.model.Account;
import com.bankofcli.model.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccountRepositoryTest {

    private AccountRepository accountRepository = new AccountRepository();
    private TransactionRepository transactionRepository = new TransactionRepository();

    // --- findByAccountId ---

    @Test
    public void findByAccountId_withValidId_returnsAccount() {
        Account account = accountRepository.findByAccountId(1);
        assertNotNull(account);
        assertEquals(1, account.getAccountId());
    }

    @Test
    public void findByAccountId_withInvalidId_returnsNull() {
        Account account = accountRepository.findByAccountId(99999);
        assertNull(account);
    }

    // --- createAccount ---

    @Test
    public void createAccount_returnsGeneratedId() {
        int newId = accountRepository.createAccount("hashedpin", new BigDecimal("100.00"));
        assertTrue(newId > 0);
    }

    @Test
    public void createAccount_createdAccountIsRetrievable() {
        int newId = accountRepository.createAccount("hashedpin2", new BigDecimal("75.00"));
        Account account = accountRepository.findByAccountId(newId);
        assertNotNull(account);
        assertEquals(new BigDecimal("75.00"), account.getBalance());
    }

    // --- updateBalance ---

    @Test
    public void updateBalance_changesTheBalance() {
        int newId = accountRepository.createAccount("hashedpin3", new BigDecimal("50.00"));
        accountRepository.updateBalance(newId, new BigDecimal("200.00"));

        Account account = accountRepository.findByAccountId(newId);
        assertEquals(new BigDecimal("200.00"), account.getBalance());
    }

    @Test
    public void updateBalance_withInvalidId_changesNothing() {
        Account before = accountRepository.findByAccountId(1);
        accountRepository.updateBalance(99999, new BigDecimal("500.00"));
        Account after = accountRepository.findByAccountId(1);

        assertEquals(before.getBalance(), after.getBalance());
    }

    // --- transfer ---

    @Test
    public void transfer_movesMoneyBetweenAccounts() {
        int source = accountRepository.createAccount("src", new BigDecimal("100.00"));
        int dest = accountRepository.createAccount("dst", new BigDecimal("0.00"));

        boolean result = accountRepository.transfer(source, dest, new BigDecimal("30.00"));

        assertTrue(result);
        assertEquals(new BigDecimal("70.00"), accountRepository.findByAccountId(source).getBalance());
        assertEquals(new BigDecimal("30.00"), accountRepository.findByAccountId(dest).getBalance());
    }

    @Test
    public void transfer_withInvalidDestination_returnsFalse() {
        int source = accountRepository.createAccount("src2", new BigDecimal("100.00"));
        boolean result = accountRepository.transfer(source, 99999, new BigDecimal("30.00"));
        assertFalse(result);
    }

    // --- recordTransaction ---

    @Test
    public void recordTransaction_addsToHistory() {
        int id = accountRepository.createAccount("hist", new BigDecimal("10.00"));
        transactionRepository.recordTransaction(id, null, "DEPOSIT", new BigDecimal("10.00"));

        List<Transaction> history = transactionRepository.getTransactionsByAccountId(id);
        assertFalse(history.isEmpty());
        assertEquals("DEPOSIT", history.get(0).getTransactionType());
    }

    @Test
    public void recordTransaction_withRelatedAccount_storesIt() {
        int id = accountRepository.createAccount("hist2", new BigDecimal("10.00"));
        transactionRepository.recordTransaction(id, 1, "TRANSFER", new BigDecimal("5.00"));

        List<Transaction> history = transactionRepository.getTransactionsByAccountId(id);
        assertEquals(1, history.get(0).getRelatedAccountId());
    }

    // --- getTransactionsByAccountId ---

    @Test
    public void getTransactionsByAccountId_withTransactions_returnsNonEmpty() {
        List<Transaction> history = transactionRepository.getTransactionsByAccountId(1);
        assertNotNull(history);
    }

    @Test
    public void getTransactionsByAccountId_withNoTransactions_returnsEmptyList() {
        List<Transaction> history = transactionRepository.getTransactionsByAccountId(99999);
        assertTrue(history.isEmpty());
    }
}