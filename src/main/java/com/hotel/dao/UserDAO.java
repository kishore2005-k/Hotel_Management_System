package com.hotel.dao;

import com.hotel.config.DatabaseConfig;
import com.hotel.models.User;
import com.hotel.security.PasswordHasher;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    public User authenticate(String username, char[] password)
            throws IOException, SQLException, GeneralSecurityException {
        String sql = "SELECT user_id, username, password_hash, role FROM users WHERE username = ?";
        try (Connection connection = DatabaseConfig.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next() || !PasswordHasher.verify(password, results.getString("password_hash"))) {
                    return null;
                }
                User user = new User();
                user.setId(results.getLong("user_id"));
                user.setUsername(results.getString("username"));
                user.setRole(results.getString("role"));
                return user;
            }
        }
    }

    public boolean hasUsers() throws IOException, SQLException {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection connection = DatabaseConfig.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            results.next();
            return results.getLong(1) > 0;
        }
    }

    public boolean createFirstAdmin(String username, String passwordHash)
            throws IOException, SQLException {
        try (Connection connection = DatabaseConfig.openConnection()) {
            connection.setAutoCommit(false);
            try {
                String lockSql = "UPDATE setup_state SET initialized = TRUE "
                        + "WHERE state_id = 1 AND initialized = FALSE";
                try (PreparedStatement lock = connection.prepareStatement(lockSql)) {
                    if (lock.executeUpdate() != 1) {
                        connection.rollback();
                        return false;
                    }
                }

                try (PreparedStatement count = connection.prepareStatement("SELECT COUNT(*) FROM users");
                     ResultSet results = count.executeQuery()) {
                    results.next();
                    if (results.getLong(1) != 0) {
                        connection.rollback();
                        return false;
                    }
                }

                String insertSql = "INSERT INTO users (username, password_hash, role) "
                        + "VALUES (?, ?, 'ADMIN')";
                try (PreparedStatement insert = connection.prepareStatement(insertSql)) {
                    insert.setString(1, username);
                    insert.setString(2, passwordHash);
                    insert.executeUpdate();
                }
                connection.commit();
                return true;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public void create(String username, String passwordHash) throws IOException, SQLException {
        String sql = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, 'ADMIN')";
        try (Connection connection = DatabaseConfig.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, passwordHash);
            statement.executeUpdate();
            try (PreparedStatement updateSetup = connection.prepareStatement(
                    "UPDATE setup_state SET initialized = TRUE WHERE state_id = 1")) {
                updateSetup.executeUpdate();
            }
        }
    }
}
