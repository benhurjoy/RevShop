package com.revshop.app;

import com.revshop.config.DatabaseConfig;
import com.revshop.config.LoggingConfig;
import com.revshop.ui.AuthMenu;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            // Initialize logging
            LoggingConfig.configure();
            logger.info("=== RevShop E-Commerce System Starting ===");

            // Test database connection
            if (DatabaseConfig.testConnection()) {
                logger.info("Database connection successful");

                // Start authentication menu
                AuthMenu authMenu = new AuthMenu();
                authMenu.show();
            } else {
                logger.error("Failed to connect to database. Please check configuration.");
                System.out.println("ERROR: Database connection failed. Check .env file and database.");
            }

        } catch (Exception e) {
            logger.error("Application startup failed: ", e);
            System.out.println("Critical error occurred: " + e.getMessage());
        } finally {
            logger.info("=== RevShop Application Shutdown ===");
            DatabaseConfig.closeConnection();
        }
    }
}