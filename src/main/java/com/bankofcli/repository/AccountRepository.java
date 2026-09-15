package com.bankofcli.repository;

import java.sql.Statement;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.bankofcli.model.Account;

public class AccountRepository {          // ← THIS LINE must be here

    public Account findByAccountId(int accountId) {
        String sql = "SELECT * FROM accounts WHERE account_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, accountId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("account_id");
                String pin = rs.getString("pin");
                BigDecimal balance = rs.getBigDecimal("balance");
                return new Account(id, pin, balance);
            }

        } catch (SQLException e) {
            System.out.println("Error finding account: " + e.getMessage());
        }

        return null;
    }

    public int createAccount(String pin, BigDecimal balance) {
        String sql = "INSERT INTO accounts (pin, balance) VALUES (?, ?)";
    
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
    
            stmt.setString(1, pin);
            stmt.setBigDecimal(2, balance);
            stmt.executeUpdate();
    
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
    
        } catch (SQLException e) {
            System.out.println("Error creating account: " + e.getMessage());
        }
    
        return -1;
    }

    public void updateBalance(int accountId, BigDecimal newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE account_id = ?";
    
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
    
            stmt.setBigDecimal(1, newBalance);
            stmt.setInt(2, accountId);
            stmt.executeUpdate();
    
        } catch (SQLException e) {
            System.out.println("Error updating balance: " + e.getMessage());
        }
    }

    public boolean transfer(int sourceId, int destId, BigDecimal amount) {
        String withdrawSql = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?";
        String depositSql = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
    
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
    
            try (PreparedStatement withdrawStmt = conn.prepareStatement(withdrawSql)) {
                withdrawStmt.setBigDecimal(1, amount);
                withdrawStmt.setInt(2, sourceId);
                int rowsUpdated = withdrawStmt.executeUpdate();
    
                if (rowsUpdated == 0) {
                    conn.rollback();
                    System.out.println("Source account not found.");
                    return false;
                }
            }
    
            try (PreparedStatement depositStmt = conn.prepareStatement(depositSql)) {
                depositStmt.setBigDecimal(1, amount);
                depositStmt.setInt(2, destId);
                int rowsUpdated = depositStmt.executeUpdate();
    
                if (rowsUpdated == 0) {
                    conn.rollback();
                    System.out.println("Destination account not found.");
                    return false;
                }
            }
    
            conn.commit();
            return true;
    
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.out.println("Error rolling back: " + rollbackEx.getMessage());
                }
            }
            System.out.println("Transfer failed: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    System.out.println("Error closing connection: " + closeEx.getMessage());
                }
            }
        }
    }

}
        