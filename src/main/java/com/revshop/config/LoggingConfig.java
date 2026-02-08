package com.revshop.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class LoggingConfig {
    public static void configure() {
        try {
            // Create logs directory if it doesn't exist
            File logsDir = new File("logs");
            if (!logsDir.exists()) {
                logsDir.mkdir();
            }

            // Load log4j2 configuration
            File configFile = new File("src/main/resources/log4j2.xml");
            if (configFile.exists()) {
                try (InputStream inputStream = new FileInputStream(configFile)) {
                    ConfigurationSource source = new ConfigurationSource(inputStream, configFile);
                    LoggerContext context = (LoggerContext) LogManager.getContext(false);
                    context.setConfigLocation(source.getURI());
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to configure logging: " + e.getMessage());
        }
    }
}