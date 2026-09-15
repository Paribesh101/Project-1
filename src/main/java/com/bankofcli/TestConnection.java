package com.bankofcli;

import com.bankofcli.business.AccountService;
import java.math.BigDecimal;

public class TestConnection {
    public static void main(String[] args) {
        AccountService service = new AccountService();
        
        System.out.println("Correct PIN:  " + service.login(3, "9999"));
        System.out.println("Wrong PIN:    " + service.login(3, "0000"));
        System.out.println("No such acct: " + service.login(999, "9999"));
    }
}