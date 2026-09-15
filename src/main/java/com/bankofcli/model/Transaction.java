package com.bankofcli.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {

    // FIELDS (delcare all six):
    private int transactionId;
    private int accountId;
    private int relatedAccountId;
    private String transactionType;
    private BigDecimal amount;
    private LocalDateTime timestamp;

    // CONSTRUCTOR: take all six params, assign each with this.
    public Transaction(int transactionId, int accountID, int relatedAccountId, String transactionType, BigDecimal amount, LocalDateTime timestamp){
        this.transactionId = transactionId;
        this.accountId = accountID;
        this.relatedAccountId = relatedAccountId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.timestamp = timestamp;
    }
    public int getTransactionId(){
        return transactionId;
    }
    public void setTransactionId(int transactionId){
        this.transactionId = transactionId;
    }
    public int getAccountId(){
        return accountId;
    }
    public void setAccountId(int accountId){
        this.accountId = accountId;
    }
    public int getRelatedAccountId(){
        return relatedAccountId;
    }

    public void setRelatedAccountId(int relatedAccountId){
        this.relatedAccountId = relatedAccountId;
    }
    public String getTransactionType(){
        return transactionType;
    }
    public void setTransactionType(String transactionType){
        this.transactionType = transactionType;
    }
    public BigDecimal getAmount(){
        return amount;
    }
    public void setAmount(BigDecimal amount){
        this.amount = amount;
    }
    public LocalDateTime getTimestamp(){
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp){
        this.timestamp = timestamp;
    }
}












