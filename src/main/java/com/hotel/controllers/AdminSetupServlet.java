package com.hotel.controllers;

import com.hotel.dao.UserDAO;
import com.hotel.security.CsrfTokens;
import com.hotel.security.PasswordHasher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/setup")
public class AdminSetupServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger(AdminSetupServlet.class);
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (redirectIfSetupComplete(request, response)) {
            return;
        }
        CsrfTokens.getOrCreate(request.getSession(true));
        response.setHeader("Cache-Control", "no-store");
        request.getRequestDispatcher("/views/setup.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        if (!CsrfTokens.isValid(session, request.getParameter("csrfToken"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid security token.");
            return;
        }
        if (redirectIfSetupComplete(request, response)) {
            return;
        }

        String username = request.getParameter("username");
        String passwordValue = request.getParameter("password");
        String confirmationValue = request.getParameter("confirmPassword");
        if (username == null || !username.matches("[A-Za-z0-9._-]{3,100}")) {
            showError(request, response, "Use 3-100 letters, numbers, dots, underscores, or hyphens for the username.");
            return;
        }
        if (passwordValue == null || passwordValue.length() < 12 || passwordValue.length() > 128) {
            showError(request, response, "Choose a password between 12 and 128 characters.");
            return;
        }
        if (!passwordValue.equals(confirmationValue)) {
            showError(request, response, "The passwords do not match.");
            return;
        }

        char[] password = passwordValue.toCharArray();
        try {
            String passwordHash = PasswordHasher.hash(password);
            if (userDAO.createFirstAdmin(username, passwordHash)) {
                response.sendRedirect(request.getContextPath() + "/auth?setup=complete");
            } else {
                response.sendRedirect(request.getContextPath() + "/auth");
            }
        } catch (IOException | java.sql.SQLException | GeneralSecurityException exception) {
            LOGGER.error("Unable to create the initial administrator account.", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            showError(request, response,
                    "Administrator setup failed. Check the application log and try again.");
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private boolean redirectIfSetupComplete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            if (userDAO.hasUsers()) {
                response.sendRedirect(request.getContextPath() + "/auth");
                return true;
            }
            return false;
        } catch (IOException | java.sql.SQLException exception) {
            LOGGER.error("Unable to check administrator setup state.", exception);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Administrator setup is temporarily unavailable.");
            return true;
        }
    }

    private void showError(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("setupError", message);
        response.setHeader("Cache-Control", "no-store");
        request.getRequestDispatcher("/views/setup.jsp").forward(request, response);
    }
}
