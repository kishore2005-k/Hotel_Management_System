package com.hotel.controllers;

import com.hotel.config.DatabaseConfig;
import com.hotel.dao.BookingDAO;
import com.hotel.dao.RoomDAO;
import com.hotel.models.Booking;
import com.hotel.models.Room;
import com.hotel.security.CsrfTokens;
import com.hotel.services.IndianMenu;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/booking")
public class CustomerBookingServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger(CustomerBookingServlet.class);
    private final BookingDAO bookingDAO = new BookingDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private static final Set<String> FOOD_MENU_ITEMS = IndianMenu.getItemNames();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        CsrfTokens.getOrCreate(request.getSession(true));
        populateRoomCatalog(request);
        request.setAttribute("hotelPhone", DatabaseConfig.getHotelContactPhone());
        request.setAttribute("foodMenuCategories", IndianMenu.getCategories());
        request.setAttribute("requested", "1".equals(request.getParameter("requested")));
        populateAvailability(request, response, null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!CsrfTokens.isValid(request.getSession(false), request.getParameter("csrfToken"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid security token.");
            return;
        }

        Booking booking = new Booking();
        booking.setCustomerName(trim(request.getParameter("customerName")));
        booking.setCustomerEmail(trim(request.getParameter("customerEmail")));
        booking.setCustomerPhone(trim(request.getParameter("customerPhone")));
        booking.setSpecialRequests(trim(request.getParameter("specialRequests")));
        String foodRequested = request.getParameter("foodRequested");
        String[] foodItems = request.getParameterValues("foodItems");
        booking.setFoodRequested("yes".equals(foodRequested));
        booking.setFoodItems(foodItems == null ? List.of() : Arrays.asList(foodItems));

        try {
            booking.setRoomId(Long.parseLong(request.getParameter("roomId")));
            booking.setCheckInDate(LocalDate.parse(request.getParameter("checkIn")));
            booking.setCheckOutDate(LocalDate.parse(request.getParameter("checkOut")));
        } catch (NumberFormatException | DateTimeParseException | NullPointerException exception) {
            showError(request, response, booking, "Choose a valid room and check-in/check-out dates.");
            return;
        }

        if (booking.getCheckInDate().isBefore(LocalDate.now())
                || !booking.getCheckOutDate().isAfter(booking.getCheckInDate())) {
            showError(request, response, booking,
                    "Check-in must be today or later, and check-out must be after check-in.");
            return;
        }
        if (booking.getCustomerName() == null || booking.getCustomerName().isBlank()
                || booking.getCustomerName().length() > 100) {
            showError(request, response, booking, "Enter your name (up to 100 characters).");
            return;
        }
        if (booking.getCustomerEmail() == null || booking.getCustomerEmail().length() > 254
                || !booking.getCustomerEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            showError(request, response, booking, "Enter a valid email address.");
            return;
        }
        if (booking.getCustomerPhone() == null
                || !booking.getCustomerPhone().matches("[+()0-9 .-]{7,30}")) {
            showError(request, response, booking, "Enter a valid phone number.");
            return;
        }
        if (booking.getSpecialRequests() != null && booking.getSpecialRequests().length() > 1000) {
            showError(request, response, booking, "Special requests must be 1000 characters or fewer.");
            return;
        }
        if (!"yes".equals(foodRequested) && !"no".equals(foodRequested)) {
            showError(request, response, booking, "Choose whether you would like to request food.");
            return;
        }
        Set<String> selectedFoodItems = new LinkedHashSet<>();
        if (foodItems != null) {
            selectedFoodItems.addAll(Arrays.asList(foodItems));
            if (selectedFoodItems.size() != foodItems.length
                    || !FOOD_MENU_ITEMS.containsAll(selectedFoodItems)) {
                showError(request, response, booking, "Choose food items from the menu.");
                return;
            }
        }
        if (booking.isFoodRequested() && selectedFoodItems.isEmpty()) {
            showError(request, response, booking, "Select at least one item from the Indian menu.");
            return;
        }
        if (!booking.isFoodRequested() && !selectedFoodItems.isEmpty()) {
            showError(request, response, booking, "Choose Yes before selecting food items.");
            return;
        }
        booking.setFoodItems(List.copyOf(selectedFoodItems));

        try {
            if (!bookingDAO.createRequest(booking)) {
                showError(request, response, booking,
                        "That room is no longer available for those dates. Search again and choose another room.");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/booking?requested=1");
        } catch (IOException | SQLException exception) {
            LOGGER.error("Unable to create customer reservation request.", exception);
            showError(request, response, booking,
                    "Your request could not be submitted. Please try again or contact the hotel.");
        }
    }

    private void populateAvailability(HttpServletRequest request, HttpServletResponse response,
                                     String error) throws ServletException, IOException {
        String checkInValue = request.getParameter("checkIn");
        String checkOutValue = request.getParameter("checkOut");
        request.setAttribute("checkInValue", checkInValue == null ? "" : checkInValue);
        request.setAttribute("checkOutValue", checkOutValue == null ? "" : checkOutValue);
        request.setAttribute("minimumDate", LocalDate.now().toString());
        request.setAttribute("formError", error);
        request.setAttribute("foodMenuCategories", IndianMenu.getCategories());
        String acType = request.getParameter("acType");
        if (acType == null || acType.isBlank()) {
            acType = "any";
        }
        request.setAttribute("acType", acType);
        if (!"any".equals(acType) && !"ac".equals(acType) && !"non-ac".equals(acType)) {
            request.setAttribute("searched", false);
            request.setAttribute("formError", "Choose Any, AC, or Non-AC for your room preference.");
            request.setAttribute("rooms", List.of());
            request.getRequestDispatcher("/views/customer-booking.jsp").forward(request, response);
            return;
        }
        if (checkInValue == null && checkOutValue == null) {
            request.setAttribute("rooms", List.of());
            request.setAttribute("searched", false);
            request.getRequestDispatcher("/views/customer-booking.jsp").forward(request, response);
            return;
        }

        try {
            LocalDate checkIn = LocalDate.parse(checkInValue);
            LocalDate checkOut = LocalDate.parse(checkOutValue);
            if (checkIn.isBefore(LocalDate.now()) || !checkOut.isAfter(checkIn)) {
                throw new DateTimeParseException("Invalid stay dates", checkInValue, 0);
            }
            Booking dates = new Booking();
            dates.setCheckInDate(checkIn);
            dates.setCheckOutDate(checkOut);
            request.setAttribute("rooms", bookingDAO.findAvailableRooms(dates, acType));
            request.setAttribute("searched", true);
        } catch (DateTimeParseException | NullPointerException exception) {
            request.setAttribute("searched", false);
            request.setAttribute("formError",
                    "Choose a check-in date today or later and a check-out date after check-in.");
            request.setAttribute("rooms", List.of());
        } catch (SQLException exception) {
            LOGGER.error("Unable to search room availability.", exception);
            request.setAttribute("searched", false);
            request.setAttribute("databaseError",
                    "Room availability is temporarily unavailable. Please contact the hotel.");
            request.setAttribute("rooms", List.of());
        }
        request.getRequestDispatcher("/views/customer-booking.jsp").forward(request, response);
    }

    private void showError(HttpServletRequest request, HttpServletResponse response,
                           Booking booking, String message) throws ServletException, IOException {
        request.setAttribute("bookingForm", booking);
        request.setAttribute("formError", message);
        request.setAttribute("checkInValue",
                booking.getCheckInDate() == null ? request.getParameter("checkIn") : booking.getCheckInDate().toString());
        request.setAttribute("checkOutValue",
                booking.getCheckOutDate() == null ? request.getParameter("checkOut") : booking.getCheckOutDate().toString());
        request.setAttribute("minimumDate", LocalDate.now().toString());
        request.setAttribute("hotelPhone", safeHotelPhone());
        populateRoomCatalog(request);
        request.setAttribute("foodMenuCategories", IndianMenu.getCategories());
        request.setAttribute("selectedFoodItems", booking.getFoodItems());
        request.setAttribute("foodRequested", booking.isFoodRequested()
                || "yes".equals(request.getParameter("foodRequested")));
        String requestedAcType = request.getParameter("acType");
        if (requestedAcType == null || requestedAcType.isBlank()) {
            requestedAcType = "any";
        }
        if (!"any".equals(requestedAcType) && !"ac".equals(requestedAcType)
                && !"non-ac".equals(requestedAcType)) {
            requestedAcType = "any";
            message = "Choose Any, AC, or Non-AC for your room preference.";
        }
        request.setAttribute("acType", requestedAcType);
        request.setAttribute("formError", message);
        try {
            if (booking.getCheckInDate() != null && booking.getCheckOutDate() != null
                    && booking.getCheckOutDate().isAfter(booking.getCheckInDate())
                    && !booking.getCheckInDate().isBefore(LocalDate.now())) {
                String acType = request.getParameter("acType");
                request.setAttribute("rooms", bookingDAO.findAvailableRooms(
                        booking, acType == null || acType.isBlank() ? "any" : acType));
                request.setAttribute("searched", true);
            } else {
                request.setAttribute("rooms", List.of());
                request.setAttribute("searched", false);
            }
        } catch (IOException | SQLException exception) {
            LOGGER.error("Unable to reload room availability after booking validation.", exception);
            request.setAttribute("rooms", List.of());
            request.setAttribute("searched", false);
        }
        request.getRequestDispatcher("/views/customer-booking.jsp").forward(request, response);
    }

    private String safeHotelPhone() {
        try {
            return DatabaseConfig.getHotelContactPhone();
        } catch (IOException exception) {
            LOGGER.error("Unable to load hotel contact phone.", exception);
            return "";
        }
    }

    private void populateRoomCatalog(HttpServletRequest request) {
        try {
            List<Room> catalog = roomDAO.findAll();
            Map<String, BigDecimal> startingRates = new HashMap<>();
            BigDecimal maximumRate = BigDecimal.ZERO;
            for (Room room : catalog) {
                if (!room.isAvailable()) {
                    continue;
                }
                String category = roomCategory(room.getRoomType());
                if (category == null) {
                    continue;
                }
                startingRates.merge(category, room.getNightlyRate(),
                        (current, candidate) -> current.min(candidate));
                maximumRate = maximumRate.max(room.getNightlyRate());
            }
            request.setAttribute("startingRates", startingRates);
            request.setAttribute("maximumRate", maximumRate);
        } catch (IOException | SQLException exception) {
            LOGGER.error("Unable to load room catalog for the customer booking page.", exception);
            request.setAttribute("databaseError",
                    "Room prices are temporarily unavailable. Please contact the hotel.");
            request.setAttribute("startingRates", Map.of());
            request.setAttribute("maximumRate", BigDecimal.ZERO);
        }
    }

    private static String roomCategory(String roomType) {
        String normalizedType = roomType.toLowerCase(Locale.ROOT);
        if (normalizedType.contains("suite")) {
            return "suite";
        }
        if (normalizedType.contains("double")) {
            return "double";
        }
        if (normalizedType.contains("single")) {
            return "single";
        }
        return null;
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }
}
