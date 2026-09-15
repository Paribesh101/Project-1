package com.bankofcli.model;

import java.math.BigDecimal;

public class Account {
    
    private int accountID;
    private String pin;
    private BigDecimal balance;


    // CONSTRUCTOR: takes all three params, assigns them with this.
    public Account(int accountID, String pin, BigDecimal balance){
        this.accountID = accountID;
        this.pin = pin;
        this.balance = balance;
    }

    // GETTERS & SETTERS: one pair for each field
    public int getAccountId(){
        return accountID;
    }

    public void setAccountId(int accountID){
        this.accountID = accountID;
    }

    public String getPin(){
        return pin;
    }

    public void setPin(String pin){
        this.pin = pin;
    }

    public BigDecimal getBalance(){
        return balance;
    }

    public void setBalance(BigDecimal balance){
        this.balance = balance;
    }

}
