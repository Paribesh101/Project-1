package com.bankofcli.business;

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

public class AccountServiceTest {

    private AccountService accountService = new AccountService();

    // --- login ---

    @Test
    public void login_withCorrectPin_returnsTrue() {
        boolean result = accountService.login(3, "9999");
        assertTrue(result);
    }

    @Test
    public void login_withWrongPin_returnsFalse() {
        boolean result = accountService.login(3, "0000");
        assertFalse(result);
    }

    // --- getAccount ---

    @Test
    public void getAccount_withValidId_returnsAccount() {
        Account account = accountService.getAccount(3);
        assertNotNull(account);
    }

    @Test
    public void getAccount_withInvalidId_returnsNull() {
        Account account = accountService.getAccount(99999);
        assertNull(account);
    }

    // --- deposit ---

    @Test
    public void deposit_increasesBalance() {
        BigDecimal before = accountService.getAccount(3).getBalance();
        accountService.deposit(3, new BigDecimal("10.00"));
        BigDecimal after = accountService.getAccount(3).getBalance();

        assertEquals(before.add(new BigDecimal("10.00")), after);
    }

    @Test
    public void deposit_withInvalidAccount_doesNotChangeAnything() {
        BigDecimal before = accountService.getAccount(3).getBalance();
        accountService.deposit(99999, new BigDecimal("10.00"));
        BigDecimal after = accountService.getAccount(3).getBalance();

        assertEquals(before, after);
    }

    // --- withdraw ---

    @Test
    public void withdraw_decreasesBalance() {
        BigDecimal before = accountService.getAccount(3).getBalance();
        accountService.withdraw(3, new BigDecimal("5.00"));
        BigDecimal after = accountService.getAccount(3).getBalance();

        assertEquals(before.subtract(new BigDecimal("5.00")), after);
    }

    @Test
    public void withdraw_withInsufficientFunds_doesNotChangeBalance() {
        BigDecimal before = accountService.getAccount(3).getBalance();
        accountService.withdraw(3, new BigDecimal("999999.00"));
        BigDecimal after = accountService.getAccount(3).getBalance();

        assertEquals(before, after);
    }

    // --- transfer ---

    @Test
    public void transfer_withValidAccountsAndFunds_returnsTrue() {
        boolean result = accountService.transfer(3, 1, new BigDecimal("5.00"));
        assertTrue(result);
    }

    @Test
    public void transfer_withInsufficientFunds_returnsFalse() {
        boolean result = accountService.transfer(3, 1, new BigDecimal("999999.00"));
        assertFalse(result);
    }

    // --- register ---

    @Test
    public void register_withValidInput_returnsNewAccountId() {
        int newId = accountService.register("4321", new BigDecimal("50.00"));
        assertTrue(newId > 0);
    }

    @Test
    public void register_withNegativeBalance_returnsNegativeOne() {
        int result = accountService.register("1111", new BigDecimal("-10.00"));
        assertEquals(-1, result);
    }

    @Test
    public void register_storesHashedPin() {
        int newId = accountService.register("7777", new BigDecimal("25.00"));
        Account account = accountService.getAccount(newId);
        assertTrue(account.getPin().startsWith("$2a$"));
    }

    // --- getHistory ---

    @Test
    public void getHistory_withTransactions_returnsNonEmptyList() {
        List<Transaction> history = accountService.getHistory(3);
        assertFalse(history.isEmpty());
    }

    @Test
    public void getHistory_withNoTransactions_returnsEmptyList() {
        List<Transaction> history = accountService.getHistory(99999);
        assertTrue(history.isEmpty());
    }
}