package com.hotel.tools;

import com.hotel.dao.UserDAO;
import com.hotel.security.PasswordHasher;

import java.io.Console;
import java.security.GeneralSecurityException;
import java.util.Arrays;

public final class CreateAdmin {
    private CreateAdmin() {
    }

    public static void main(String[] args) throws Exception {
        Console console = System.console();
        if (console == null) {
            throw new IllegalStateException("Run this command from an interactive terminal.");
        }

        String username = console.readLine("Admin username: ");
        if (username == null || !username.matches("[A-Za-z0-9._-]{3,100}")) {
            throw new IllegalArgumentException("Username must be 3-100 letters, numbers, dots, underscores, or hyphens.");
        }

        char[] password = console.readPassword("Admin password (12-128 characters): ");
        char[] confirmation = console.readPassword("Confirm password: ");
        try {
            if (password.length < 12 || password.length > 128) {
                throw new IllegalArgumentException("Password must be 12-128 characters.");
            }
            int difference = password.length ^ confirmation.length;
            int maxLength = Math.max(password.length, confirmation.length);
            for (int i = 0; i < maxLength; i++) {
                char first = i < password.length ? password[i] : 0;
                char second = i < confirmation.length ? confirmation[i] : 0;
                difference |= first ^ second;
            }
            if (difference != 0) {
                throw new IllegalArgumentException("Passwords do not match.");
            }

            String hash = PasswordHasher.hash(password);
            new UserDAO().create(username, hash);
            console.printf("Administrator '%s' created successfully.%n", username);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Unable to hash the administrator password.", exception);
        } finally {
            Arrays.fill(password, '\0');
            Arrays.fill(confirmation, '\0');
        }
    }
}
