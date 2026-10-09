package com.hotel.controllers;

import com.hotel.dao.UserDAO;
import com.hotel.models.User;
import com.hotel.security.CsrfTokens;
import com.hotel.security.PasswordHasher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/auth")
public class AuthServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger(AuthServlet.class);
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getSession(false) != null
                && request.getSession(false).getAttribute("user") != null) {
            response.sendRedirect(request.getContextPath() + "/app/rooms");
            return;
        }
        HttpSession session = request.getSession(true);
        CsrfTokens.getOrCreate(session);
        response.setHeader("Cache-Control", "no-store");
        request.getRequestDispatcher("/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String username = request.getParameter("username");
        String passwordValue = request.getParameter("password");
        HttpSession existingSession = request.getSession(false);
        if (!CsrfTokens.isValid(existingSession, request.getParameter("csrfToken"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid security token.");
            return;
        }
        if (username == null || !username.trim().matches("[A-Za-z0-9._-]{3,100}")
                || passwordValue == null || passwordValue.length() > 128 || passwordValue.isEmpty()) {
            request.setAttribute("loginError", "Enter your username and password.");
            request.getRequestDispatcher("/views/login.jsp").forward(request, response);
            return;
        }

        char[] password = passwordValue.toCharArray();
        try {
            User user = userDAO.authenticate(username.trim(), password);
            if (user == null) {
                request.setAttribute("loginError", "Username or password is incorrect.");
                request.getRequestDispatcher("/views/login.jsp").forward(request, response);
                return;
            }

            HttpSession session = request.getSession(true);
            request.changeSessionId();
            session.setAttribute("user", user);
            Object requestedPage = session.getAttribute("loginReturnTo");
            session.removeAttribute("loginReturnTo");
            String destination = requestedPage instanceof String path
                    && path.startsWith("/app/") && !path.contains("..") && !path.contains("\\")
                    ? path : "/app/rooms";
            response.sendRedirect(request.getContextPath() + destination);
        } catch (IOException | java.sql.SQLException | GeneralSecurityException exception) {
            LOGGER.error("Unable to authenticate user.", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            request.setAttribute("loginError",
                    "Sign-in is temporarily unavailable. Check the database configuration and try again.");
            request.getRequestDispatcher("/views/login.jsp").forward(request, response);
        } finally {
            java.util.Arrays.fill(password, '\0');
        }
    }
}
