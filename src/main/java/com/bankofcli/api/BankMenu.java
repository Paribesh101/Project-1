package com.bankofcli.api;

import java.math.BigDecimal;

import com.bankofcli.business.AccountService;
import com.bankofcli.model.Account;
import com.bankofcli.model.Transaction;

import java.util.List;
import java.util.Scanner;

public class BankMenu {

    private AccountService accountService = new AccountService();
    private Scanner scanner = new Scanner(System.in);
    private Integer loggedInAccountId = null;

    public void start() {
        boolean running = true;
        while (running) {
            if (loggedInAccountId == null) {
                System.out.println("\n=== Bank of CLI ===");
                System.out.println("1. Register");
                System.out.println("2. Login");
                System.out.println("3. Exit");
                System.out.print("Choose an option: ");

                String choice = scanner.nextLine();

                switch (choice) {
                    case "1":
                        System.out.print("Choose a PIN: ");
                        String newPin = scanner.nextLine();
                        System.out.print("Initial deposit: ");
                         try {
                            BigDecimal initial = new BigDecimal(scanner.nextLine());
                            int newId = accountService.register(newPin, initial);
                            if (newId != -1) {
                                System.out.println("Account created. Your account ID is: " + newId);
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid amount.");
                        }
                        break;
                    case "2":
                        System.out.print("Account ID: ");
                        try {
                            int id = Integer.parseInt(scanner.nextLine());
                            System.out.print("PIN: ");
                            String pin = scanner.nextLine();

                            if (accountService.login(id, pin)) {
                                loggedInAccountId = id;
                                System.out.println("Login successful.");
                            } else {
                                System.out.println("Invalid account ID or PIN.");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Account ID must be a number.");
                        }
                        break;
                    case "3":
                        running = false;
                        System.out.println("Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid option.");
                }

            } else {
                System.out.println("\n=== Account Menu ===");
                System.out.println("1. Check Balance");
                System.out.println("2. Deposit");
                System.out.println("3. Withdraw");
                System.out.println("4. Transfer");
                System.out.println("5. Transaction History");
                System.out.println("6. Logout");
                System.out.print("Choose an option: ");

                String choice = scanner.nextLine();

                switch (choice) {
                    case "1":
                        Account account = accountService.getAccount(loggedInAccountId);
                        if (account == null) {
                            System.out.println("Account not found.");
                        } else {
                            System.out.println("Your balance: $" + account.getBalance());
                        }
                        break;
                    case "2":
                        System.out.print("Enter amount to deposit: ");
                        try {
                            BigDecimal amount = new BigDecimal(scanner.nextLine());
                            accountService.deposit(loggedInAccountId, amount);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid amount. Please enter a number.");
                        }
                        break;
                    case "3":
                        System.out.print("Enter amount to withdraw: ");
                        try {
                            BigDecimal amount = new BigDecimal(scanner.nextLine());
                            accountService.withdraw(loggedInAccountId, amount);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid amount. Please enter a number.");
                        }
                        break;
                    case "4":
                        System.out.print("Destination account ID: ");
                        try {
                            int destId = Integer.parseInt(scanner.nextLine());
                            System.out.print("Amount to transfer: ");
                            BigDecimal transferAmount = new BigDecimal(scanner.nextLine());

                            boolean success = accountService.transfer(loggedInAccountId, destId, transferAmount);
                            if (success) {
                                System.out.println("Transfer complete.");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input. Please enter a number.");
                        }
                        break;
                    case "5":
                        List<Transaction> history = accountService.getHistory(loggedInAccountId);
                        if (history.isEmpty()) {
                            System.out.println("No transactions yet.");
                        } else {
                            System.out.println("\n--- Transaction History ---");
                            for (Transaction t : history) {
                                System.out.println(t.getTransactionType() + " | $" + t.getAmount() + " | " + t.getTimestamp());
                            }
                        }
                        break;
                    case "6":
                        loggedInAccountId = null;
                        System.out.println("Logged out.");
                        break;
                    default:
                        System.out.println("Invalid option.");
                }
            }
        }
    }
}