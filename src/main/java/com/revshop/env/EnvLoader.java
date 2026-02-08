package com.revshop.env;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class EnvLoader {
    private static final Map<String, String> envVars = new HashMap<>();

    static {
//        System.out.println("=== Loading Environment Variables ===");
        loadEnvFile();
//        printLoadedVariables();
        validateRequiredVariables();
//        System.out.println("=== Environment Loaded Successfully ===");
    }

    private static void loadEnvFile() {
        // Try multiple locations in order
        String[] possiblePaths = {
                ".env",                          // Project root
                "src/main/resources/.env",       // Resources folder
                "../.env",                       // Parent directory
                System.getProperty("user.dir") + "/.env"  // Absolute path
        };

        // Also try loading from classpath
        loadFromClasspath();

        // Try file system paths
        for (String path : possiblePaths) {
            if (tryLoadFromFile(path)) {
                return; // Stop after first successful load
            }
        }

        // Set defaults if not found in .env
        setDefaultValues();
    }

    private static boolean tryLoadFromFile(String filePath) {
        try {
            System.out.println("Trying to load .env from: " + filePath);
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            loadFromReader(reader);
            reader.close();
            System.out.println("✓ Successfully loaded from: " + filePath);
            return true;
        } catch (Exception e) {
            System.out.println("✗ Failed to load from " + filePath + ": " + e.getMessage());
            return false;
        }
    }

    private static void loadFromClasspath() {
        try {
            InputStream inputStream = EnvLoader.class.getClassLoader()
                    .getResourceAsStream(".env");

            if (inputStream != null) {
                System.out.println("Trying to load .env from classpath");
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(inputStream)
                );
                loadFromReader(reader);
                reader.close();
                System.out.println("✓ Successfully loaded from classpath");
            }
        } catch (Exception e) {
            System.out.println("✗ Failed to load from classpath: " + e.getMessage());
        }
    }

    private static void loadFromReader(BufferedReader reader) throws Exception {
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            String[] parts = line.split("=", 2);
            if (parts.length == 2) {
                String key = parts[0].trim();
                String value = parts[1].trim();

                // Remove quotes if present
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }

                envVars.put(key, value);
            }
        }
    }

    private static void setDefaultValues() {
        System.out.println("Setting default values...");
        envVars.putIfAbsent("LOW_STOCK_THRESHOLD", "10");
        envVars.putIfAbsent("MAX_LOGIN_ATTEMPTS", "3");

        // For development/testing - REMOVE THESE IN PRODUCTION
        if (!envVars.containsKey("DB_URL")) {
            System.out.println("⚠ WARNING: Using development defaults!");
            envVars.put("DB_URL", "jdbc:mysql://localhost:3306/revshop");
            envVars.put("DB_USERNAME", "root");
            envVars.put("DB_PASSWORD", "");
            envVars.put("SMTP_HOST", "smtp.gmail.com");
            envVars.put("SMTP_PORT", "587");
            envVars.put("SMTP_EMAIL", "test@example.com");
            envVars.put("SMTP_PASSWORD", "test123");
        }
    }

    private static void printLoadedVariables() {
        System.out.println("\nLoaded Environment Variables:");
        System.out.println("=============================");
        for (Map.Entry<String, String> entry : envVars.entrySet()) {
            if (entry.getKey().toLowerCase().contains("password")) {
                System.out.printf("%-20s: %s%n", entry.getKey(), "***HIDDEN***");
            } else {
                System.out.printf("%-20s: %s%n", entry.getKey(), entry.getValue());
            }
        }
        System.out.println("=============================\n");
    }

    private static void validateRequiredVariables() {
        String[] required = {
                "DB_URL", "DB_USERNAME", "DB_PASSWORD",
                "SMTP_HOST", "SMTP_PORT", "SMTP_EMAIL", "SMTP_PASSWORD"
        };

        StringBuilder missing = new StringBuilder();
        for (String var : required) {
            if (!envVars.containsKey(var) || envVars.get(var).isEmpty()) {
                missing.append(var).append(", ");
            }
        }

        if (missing.length() > 0) {
            String errorMsg = "Missing required environment variables: " +
                    missing.substring(0, missing.length() - 2);
            System.err.println("\n❌ ERROR: " + errorMsg);
            System.err.println("Current working directory: " + System.getProperty("user.dir"));
            throw new RuntimeException(errorMsg + "\nPlease check your .env file");
        }
    }

    public static String get(String key) {
        String value = envVars.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Environment variable not found: " + key);
        }
        return value;
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    // Helper method for debugging

    }
