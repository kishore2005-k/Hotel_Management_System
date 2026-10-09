package com.hotel.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public final class DatabaseConfig {
    private static final String PROPERTIES_FILE = "/db.properties";
    private static final String SCHEMA_FILE = "/db/schema.sql";
    private static boolean schemaInitialized;

    private DatabaseConfig() {
    }

    public static String getHotelContactPhone() throws IOException {
        Properties properties = loadProperties();
        String phone = properties.getProperty("hotel.contact.phone", "").trim();
        return phone.isEmpty() || "Not configured".equalsIgnoreCase(phone)
                ? "" : phone;
    }

    public static Connection openConnection() throws IOException, SQLException {
        Properties properties = loadProperties();

        String driver = requiredProperty(properties, "db.driver");
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException exception) {
            throw new IOException("Database driver could not be loaded: " + driver, exception);
        }
        String url = requiredProperty(properties, "db.url");
        String username = properties.getProperty("db.username", "").trim();
        String password = properties.getProperty("db.password", "");
        Connection connection = DriverManager.getConnection(url, username, password);
        try {
            initializeSchema(connection);
            return connection;
        } catch (IOException | SQLException exception) {
            connection.close();
            throw exception;
        }
    }

    private static Properties loadProperties() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = DatabaseConfig.class.getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                throw new IOException("Database configuration not found: " + PROPERTIES_FILE);
            }
            properties.load(input);
        }
        return properties;
    }

    private static synchronized void initializeSchema(Connection connection)
            throws IOException, SQLException {
        if (schemaInitialized) {
            return;
        }

        String schema;
        try (InputStream input = DatabaseConfig.class.getResourceAsStream(SCHEMA_FILE)) {
            if (input == null) {
                throw new IOException("Database schema not found: " + SCHEMA_FILE);
            }
            schema = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }

        try (Statement statement = connection.createStatement()) {
            for (String sql : schema.split(";")) {
                if (!sql.isBlank()) {
                    statement.execute(sql.trim());
                }
            }
        }
        schemaInitialized = true;
    }

    private static String requiredProperty(Properties properties, String name) throws IOException {
        String value = properties.getProperty(name);
        if (value == null || value.trim().isEmpty() || value.startsWith("CHANGE_ME")) {
            throw new IOException("Set a valid value for " + name + " in " + PROPERTIES_FILE);
        }
        return value.trim();
    }
}
