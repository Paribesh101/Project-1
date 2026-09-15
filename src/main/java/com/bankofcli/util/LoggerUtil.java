package com.bankofcli.util;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerUtil {

    private static final Logger logger = Logger.getLogger("BankOfCLI");
    private static boolean initialized = false;

    private static void init() {
        if (initialized) {
            return;
        }
        try {
            FileHandler fileHandler = new FileHandler("bank.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false);
            initialized = true;
        } catch (IOException e) {
            System.out.println("Could not initialize log file.");
        }
    }

    public static void info(String message) {
        init();
        logger.log(Level.INFO, message);
    }

    public static void error(String message) {
        init();
        logger.log(Level.SEVERE, message);
    }
}