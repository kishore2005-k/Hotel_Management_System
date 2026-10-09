package com.hotel.security;

import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class CsrfTokens {
    private static final SecureRandom RANDOM = new SecureRandom();

    private CsrfTokens() {
    }

    public static String getOrCreate(HttpSession session) {
        String token = (String) session.getAttribute("csrfToken");
        if (token == null) {
            byte[] bytes = new byte[32];
            RANDOM.nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            session.setAttribute("csrfToken", token);
        }
        return token;
    }

    public static boolean isValid(HttpSession session, String suppliedToken) {
        if (session == null || suppliedToken == null) {
            return false;
        }
        String expectedToken = (String) session.getAttribute("csrfToken");
        return expectedToken != null
                && MessageDigest.isEqual(expectedToken.getBytes(StandardCharsets.UTF_8),
                        suppliedToken.getBytes(StandardCharsets.UTF_8));
    }
}
