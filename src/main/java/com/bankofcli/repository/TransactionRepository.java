package com.bankofcli.repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;    // ← THIS is the one you're missing
import java.util.ArrayList;                  // needed for the ResultSet
import java.util.List;                 // needed for ArrayList

import com.bankofcli.model.Transaction;                      // needed for List


public class TransactionRepository{

    public void recordTransaction(int accountId, Integer relatedAccountId, String transactionType, BigDecimal amount){
        String sql = "INSERT INTO transactions (account_id, related_account_id, transaction_type, amount) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){
            
            // TODO: set placeholder 1 = accountId (setInt)
            stmt.setInt(1, accountId);
            // TODO: set placeholder 2 = relatedAccountId (see note below!)
            if (relatedAccountId != null){
                stmt.setInt(2, relatedAccountId);
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER);
            }
            // TODO: set placeholder 3 = transactionType (setString)
            stmt.setString(3, transactionType);
            // TODO: set placeholder 4 = amount (setBigDecimal)
            stmt.setBigDecimal(4, amount);
            // TODO: executeUpdate()
            stmt.executeUpdate();
            
            
        } catch (SQLException e){
            System.out.println("Error recording transaction: " + e.getMessage());
        }

    }

    public List<Transaction> getTransactionsByAccountId(int accountId){
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY timestamp DESC";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){

                stmt.setInt(1, accountId);
                ResultSet rs = stmt.executeQuery();

                while(rs.next()){
                    Transaction t = new Transaction(
                        rs.getInt("transaction_id"),
                        rs.getInt("account_id"),
                        rs.getInt("related_account_id"),
                        rs.getString("transaction_type"),
                        rs.getBigDecimal("amount"),
                        rs.getTimestamp("timestamp").toLocalDateTime()
                    );
                    transactions.add(t);

                }
        } catch (SQLException e){
            System.out.println("Error fetching transactions: " + e.getMessage());
        }
        return transactions;

    }


}

