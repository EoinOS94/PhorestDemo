package com.phorestdemo.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = ConfigReader.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input != null) {
                properties.load(input);
            } else {
                System.err.println("config.properties not found!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isHeadless() {
        // Force headless in CI (GitHub Actions sets CI=true)
        if (System.getenv("CI") != null) {
            return true;
        }
        return Boolean.parseBoolean(
                // Set this to true for headful mode
                properties.getProperty("driverHeadless", "true"));
    }

    public static String getBaseUrl() {
        return properties.getProperty("baseUrl", "https://gift-cards.phorest.com/salons/demo#");
    }

    public static String getBrowserType() {
        return properties.getProperty("browserType", "chromium");
    }

    public static int getDefaultTimeout() {
        return Integer.parseInt(properties.getProperty("defaultTimeout", "10000"));
    }
}
