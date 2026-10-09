package com.hotel.controllers;

import com.hotel.dao.RoomDAO;
import com.hotel.models.Room;
import com.hotel.security.CsrfTokens;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/app/rooms")
public class RoomServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger(RoomServlet.class);
    private final RoomDAO roomDAO = new RoomDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String editId = request.getParameter("edit");
            if (editId != null && !editId.isBlank()) {
                long id = Long.parseLong(editId);
                Room room = roomDAO.findById(id);
                if (room == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Room not found.");
                    return;
                }
                request.setAttribute("roomToEdit", room);
            }
            request.setAttribute("rooms", roomDAO.findAll());
            request.getRequestDispatcher("/views/rooms.jsp").forward(request, response);
        } catch (NumberFormatException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid room ID.");
        } catch (IOException | SQLException exception) {
            LOGGER.error("Unable to load room inventory.", exception);
            request.setAttribute("databaseError",
                    "Room inventory could not be loaded. Check the database setup and try again.");
            request.setAttribute("rooms", List.of());
            request.getRequestDispatcher("/views/rooms.jsp").forward(request, response);
        }
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
        String action = request.getParameter("action");
        String idValue = request.getParameter("id");
        String roomNumber = trim(request.getParameter("roomNumber"));
        String roomType = trim(request.getParameter("roomType"));
        String rateValue = trim(request.getParameter("nightlyRate"));
        String availability = request.getParameter("available");
        String airConditioned = request.getParameter("airConditioned");

        Room room = new Room();
        room.setRoomNumber(roomNumber);
        room.setRoomType(roomType);
        room.setAvailable("true".equals(availability));
        room.setAirConditioned("true".equals(airConditioned));

        if (!"create".equals(action) && !"update".equals(action)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid room action.");
            return;
        }

        if (roomNumber == null || roomNumber.isEmpty() || roomNumber.length() > 20
                || roomType == null || roomType.isEmpty() || roomType.length() > 50
                || !("true".equals(availability) || "false".equals(availability))
                || !("true".equals(airConditioned) || "false".equals(airConditioned))) {
            showFormError(request, response, room, idValue,
                    "Enter a room number, room type, availability, and AC setting.");
            return;
        }

        if (rateValue == null || rateValue.isEmpty()) {
            showFormError(request, response, room, idValue, "Enter a valid nightly rate.");
            return;
        }
        try {
            BigDecimal rate = new BigDecimal(rateValue);
            if (rate.signum() <= 0 || rate.scale() > 2 || rate.precision() > 10) {
                showFormError(request, response, room, idValue,
                        "Nightly rate must be greater than zero and have at most two decimal places.");
                return;
            }
            room.setNightlyRate(rate);
        } catch (NumberFormatException exception) {
            showFormError(request, response, room, idValue, "Enter a valid nightly rate.");
            return;
        }

        try {
            if ("create".equals(action)) {
                roomDAO.create(room);
            } else {
                long id = Long.parseLong(idValue);
                room.setId(id);
                if (!roomDAO.update(room)) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Room not found.");
                    return;
                }
            }
            response.sendRedirect(request.getContextPath() + "/app/rooms?saved=" + action);
        } catch (NumberFormatException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid room ID.");
        } catch (IOException | SQLException exception) {
            LOGGER.error("Unable to save room {}.", roomNumber, exception);
            showFormError(request, response, room, idValue,
                    "Room could not be saved. Check the database connection and ensure the room number is unique.");
        }
    }

    private void showFormError(HttpServletRequest request, HttpServletResponse response,
                               Room room, String idValue, String message)
            throws ServletException, IOException {
        request.setAttribute("formError", message);
        request.setAttribute("formRoom", room);
        request.setAttribute("formNightlyRate", request.getParameter("nightlyRate"));
        request.setAttribute("rooms", loadRooms(request));
        if (idValue != null && !idValue.isBlank()) {
            try {
                room.setId(Long.parseLong(idValue));
            } catch (NumberFormatException exception) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid room ID.");
                return;
            }
        }
        request.getRequestDispatcher("/views/rooms.jsp").forward(request, response);
    }

    private List<Room> loadRooms(HttpServletRequest request) {
        try {
            return roomDAO.findAll();
        } catch (IOException | SQLException exception) {
            LOGGER.error("Unable to reload room inventory after a form error.", exception);
            request.setAttribute("databaseError",
                    "Room inventory could not be loaded. Check the database setup and try again.");
            return List.of();
        }
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }
}
