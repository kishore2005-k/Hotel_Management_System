package com.hotel.controllers;

import com.hotel.dao.BookingDAO;
import com.hotel.models.Booking;
import com.hotel.services.BookingApprovalEmailService;
import com.hotel.services.BookingConfirmationSmsService;
import javax.mail.MessagingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/app/bookings")
public class BookingServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger(BookingServlet.class);
    private final BookingDAO bookingDAO = new BookingDAO();
    private final BookingApprovalEmailService approvalEmailService = new BookingApprovalEmailService();
    private final BookingConfirmationSmsService smsService = new BookingConfirmationSmsService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("bookings", bookingDAO.findAll());
        } catch (IOException | SQLException exception) {
            LOGGER.error("Unable to load booking requests.", exception);
            request.setAttribute("databaseError", "Booking requests could not be loaded.");
            request.setAttribute("bookings", List.of());
        }
        request.getRequestDispatcher("/views/booking.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        String idValue = request.getParameter("bookingId");
        String status;
        if ("approve".equals(action)) {
            status = "CONFIRMED";
        } else if ("reject".equals(action)) {
            status = "REJECTED";
        } else if ("checkout".equals(action)) {
            status = "CHECKED_OUT";
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid booking action.");
            return;
        }

        try {
            long bookingId = Long.parseLong(idValue);
            if (!bookingDAO.updateStatus(bookingId, status)) {
                response.sendRedirect(request.getContextPath() + "/app/bookings?updated=unavailable");
                return;
            }
            if ("approve".equals(action)) {
                String emailStatus = sendApprovalEmail(bookingId);
                String smsStatus = sendConfirmationSms(bookingId);
                response.sendRedirect(request.getContextPath()
                        + "/app/bookings?updated=approve&email=" + emailStatus + "&sms=" + smsStatus);
                return;
            }
            if ("checkout".equals(action)) {
                response.sendRedirect(request.getContextPath() + "/app/bookings?updated=checkout");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/app/bookings?updated=" + action);
        } catch (NumberFormatException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid booking ID.");
        } catch (IOException | SQLException exception) {
            LOGGER.error("Unable to update booking request.", exception);
            request.setAttribute("databaseError",
                    "Booking request could not be updated. It may conflict with another confirmed stay.");
            doGet(request, response);
        }
    }

    private String sendApprovalEmail(long bookingId) {
        if (!approvalEmailService.isConfigured()) {
            LOGGER.warn("Booking {} was approved, but SMTP email settings are not configured.", bookingId);
            return "not-configured";
        }
        try {
            Booking booking = bookingDAO.findById(bookingId);
            if (booking == null) {
                LOGGER.error("Booking {} was approved, but its confirmation details could not be found.", bookingId);
                return "failed";
            }
            approvalEmailService.sendApproval(booking);
            return "sent";
        } catch (IOException | SQLException | MessagingException | IllegalStateException exception) {
            LOGGER.error("Booking {} was approved, but its confirmation email could not be sent.",
                    bookingId, exception);
            return "failed";
        }
    }

    private String sendConfirmationSms(long bookingId) {
        if (!smsService.isConfigured()) {
            LOGGER.warn("Booking {} was approved, but SMS settings are not configured.", bookingId);
            return "not-configured";
        }
        try {
            Booking booking = bookingDAO.findById(bookingId);
            if (booking == null) {
                LOGGER.error("Booking {} was approved, but its confirmation details could not be found.", bookingId);
                return "failed";
            }
            if (booking.getCustomerPhone() == null || booking.getCustomerPhone().isBlank()) {
                LOGGER.warn("Booking {} was approved, but customer phone number is missing.", bookingId);
                return "no-phone";
            }
            smsService.sendConfirmation(booking);
            return "sent";
        } catch (IOException | InterruptedException | SQLException | IllegalStateException exception) {
            LOGGER.error("Booking {} was approved, but its confirmation SMS could not be sent.",
                    bookingId, exception);
            return "failed";
        }
    }
}
