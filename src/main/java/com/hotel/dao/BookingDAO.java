package com.hotel.dao;

import com.hotel.config.DatabaseConfig;
import com.hotel.models.Booking;
import com.hotel.models.Room;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class BookingDAO {
    public boolean createRequest(Booking booking) throws IOException, SQLException {
        String lockRoom = "SELECT available FROM rooms WHERE room_id = ? FOR UPDATE";
        String conflict = "SELECT COUNT(*) FROM bookings WHERE room_id = ? "
                + "AND status IN ('PENDING', 'CONFIRMED') "
                + "AND check_in_date < ? AND check_out_date > ?";
        String insert = "INSERT INTO bookings (room_id, customer_name, customer_email, customer_phone, "
                + "check_in_date, check_out_date, special_requests, food_requested, food_items, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'PENDING')";

        try (Connection connection = DatabaseConfig.openConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(lockRoom)) {
                    statement.setLong(1, booking.getRoomId());
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next() || !result.getBoolean("available")) {
                            connection.rollback();
                            return false;
                        }
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(conflict)) {
                    statement.setLong(1, booking.getRoomId());
                    statement.setDate(2, Date.valueOf(booking.getCheckOutDate()));
                    statement.setDate(3, Date.valueOf(booking.getCheckInDate()));
                    try (ResultSet result = statement.executeQuery()) {
                        result.next();
                        if (result.getLong(1) > 0) {
                            connection.rollback();
                            return false;
                        }
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(insert)) {
                    statement.setLong(1, booking.getRoomId());
                    statement.setString(2, booking.getCustomerName());
                    statement.setString(3, booking.getCustomerEmail());
                    statement.setString(4, booking.getCustomerPhone());
                    statement.setDate(5, Date.valueOf(booking.getCheckInDate()));
                    statement.setDate(6, Date.valueOf(booking.getCheckOutDate()));
                    statement.setString(7, booking.getSpecialRequests());
                    statement.setBoolean(8, booking.isFoodRequested());
                    statement.setString(9, String.join("|", booking.getFoodItems()));
                    statement.executeUpdate();
                }
                connection.commit();
                return true;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public List<Room> findAvailableRooms(Booking dates, String acType) throws IOException, SQLException {
        if (!"any".equals(acType) && !"ac".equals(acType) && !"non-ac".equals(acType)) {
            throw new IllegalArgumentException("Unsupported air-conditioning filter.");
        }
        String sql = "SELECT r.room_id, r.room_number, r.room_type, r.nightly_rate, r.available, "
                + "r.air_conditioned "
                + "FROM rooms r WHERE r.available = TRUE AND NOT EXISTS ("
                + "SELECT 1 FROM bookings b WHERE b.room_id = r.room_id "
                + "AND b.status IN ('PENDING', 'CONFIRMED') "
                + "AND b.check_in_date < ? AND b.check_out_date > ?)"
                + ("any".equals(acType) ? "" : " AND r.air_conditioned = ?")
                + " ORDER BY r.room_number";
        List<Room> rooms = new ArrayList<>();
        try (Connection connection = DatabaseConfig.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(dates.getCheckOutDate()));
            statement.setDate(2, Date.valueOf(dates.getCheckInDate()));
            if (!"any".equals(acType)) {
                statement.setBoolean(3, "ac".equals(acType));
            }
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    Room room = new Room();
                    room.setId(results.getLong("room_id"));
                    room.setRoomNumber(results.getString("room_number"));
                    room.setRoomType(results.getString("room_type"));
                    room.setNightlyRate(results.getBigDecimal("nightly_rate"));
                    room.setAvailable(results.getBoolean("available"));
                    room.setAirConditioned(results.getBoolean("air_conditioned"));
                    rooms.add(room);
                }
            }
        }
        return rooms;
    }

    public List<Booking> findAll() throws IOException, SQLException {
        String sql = "SELECT b.booking_id, b.room_id, r.room_number, r.room_type, r.nightly_rate, "
                + "b.customer_name, b.customer_email, b.customer_phone, b.check_in_date, "
                + "b.check_out_date, b.special_requests, b.food_requested, b.food_items, b.status "
                + "FROM bookings b JOIN rooms r ON r.room_id = b.room_id "
                + "ORDER BY CASE b.status WHEN 'PENDING' THEN 0 ELSE 1 END, b.created_at DESC";
        List<Booking> bookings = new ArrayList<>();
        try (Connection connection = DatabaseConfig.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                Booking booking = new Booking();
                booking.setId(results.getLong("booking_id"));
                booking.setRoomId(results.getLong("room_id"));
                booking.setRoomNumber(results.getString("room_number"));
                booking.setRoomType(results.getString("room_type"));
                booking.setNightlyRate(results.getBigDecimal("nightly_rate"));
                booking.setCustomerName(results.getString("customer_name"));
                booking.setCustomerEmail(results.getString("customer_email"));
                booking.setCustomerPhone(results.getString("customer_phone"));
                booking.setCheckInDate(results.getDate("check_in_date").toLocalDate());
                booking.setCheckOutDate(results.getDate("check_out_date").toLocalDate());
                booking.setSpecialRequests(results.getString("special_requests"));
                booking.setFoodRequested(results.getBoolean("food_requested"));
                String foodItems = results.getString("food_items");
                booking.setFoodItems(foodItems.isEmpty() ? List.of() : Arrays.asList(foodItems.split("\\|", -1)));
                booking.setStatus(results.getString("status"));
                bookings.add(booking);
            }
        }
        return bookings;
    }

    public Booking findById(long bookingId) throws IOException, SQLException {
        String sql = "SELECT b.booking_id, b.room_id, r.room_number, r.room_type, r.nightly_rate, "
                + "b.customer_name, b.customer_email, b.customer_phone, b.check_in_date, "
                + "b.check_out_date, b.special_requests, b.food_requested, b.food_items, b.status "
                + "FROM bookings b JOIN rooms r ON r.room_id = b.room_id WHERE b.booking_id = ?";
        try (Connection connection = DatabaseConfig.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, bookingId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }
                Booking booking = new Booking();
                booking.setId(result.getLong("booking_id"));
                booking.setRoomId(result.getLong("room_id"));
                booking.setRoomNumber(result.getString("room_number"));
                booking.setRoomType(result.getString("room_type"));
                booking.setNightlyRate(result.getBigDecimal("nightly_rate"));
                booking.setCustomerName(result.getString("customer_name"));
                booking.setCustomerEmail(result.getString("customer_email"));
                booking.setCustomerPhone(result.getString("customer_phone"));
                booking.setCheckInDate(result.getDate("check_in_date").toLocalDate());
                booking.setCheckOutDate(result.getDate("check_out_date").toLocalDate());
                booking.setSpecialRequests(result.getString("special_requests"));
                booking.setFoodRequested(result.getBoolean("food_requested"));
                String foodItems = result.getString("food_items");
                booking.setFoodItems(foodItems.isEmpty() ? List.of() : Arrays.asList(foodItems.split("\\|", -1)));
                booking.setStatus(result.getString("status"));
                return booking;
            }
        }
    }

    public boolean updateStatus(long bookingId, String status) throws IOException, SQLException {
        if (!"CONFIRMED".equals(status) && !"REJECTED".equals(status)
                && !"CHECKED_OUT".equals(status)) {
            throw new IllegalArgumentException("Unsupported booking status.");
        }
        String findRoom = "SELECT room_id FROM bookings WHERE booking_id = ?";
        String lockRoom = "SELECT room_id FROM rooms WHERE room_id = ? FOR UPDATE";
        String lockBooking = "SELECT room_id, check_in_date, check_out_date, status "
                + "FROM bookings WHERE booking_id = ? FOR UPDATE";
        try (Connection connection = DatabaseConfig.openConnection()) {
            connection.setAutoCommit(false);
            try {
                long roomId;
                try (PreparedStatement find = connection.prepareStatement(findRoom)) {
                    find.setLong(1, bookingId);
                    try (ResultSet result = find.executeQuery()) {
                        if (!result.next()) {
                            connection.rollback();
                            return false;
                        }
                        roomId = result.getLong("room_id");
                    }
                }
                try (PreparedStatement lock = connection.prepareStatement(lockRoom)) {
                    lock.setLong(1, roomId);
                    try (ResultSet result = lock.executeQuery()) {
                        if (!result.next()) {
                            connection.rollback();
                            return false;
                        }
                    }
                }

                Date checkIn;
                Date checkOut;
                String currentStatus;
                try (PreparedStatement lock = connection.prepareStatement(lockBooking)) {
                    lock.setLong(1, bookingId);
                    try (ResultSet result = lock.executeQuery()) {
                        if (!result.next()) {
                            connection.rollback();
                            return false;
                        }
                        checkIn = result.getDate("check_in_date");
                        checkOut = result.getDate("check_out_date");
                        currentStatus = result.getString("status");
                    }
                }

                String expectedStatus;
                if ("CHECKED_OUT".equals(status)) {
                    if (!"CONFIRMED".equals(currentStatus) || checkIn.toLocalDate().isAfter(LocalDate.now())) {
                        connection.rollback();
                        return false;
                    }
                    expectedStatus = "CONFIRMED";
                } else {
                    if (!"PENDING".equals(currentStatus)) {
                        connection.rollback();
                        return false;
                    }
                    expectedStatus = "PENDING";
                }

                if ("CONFIRMED".equals(status)) {
                    String conflict = "SELECT COUNT(*) FROM bookings WHERE room_id = ? "
                            + "AND booking_id <> ? AND status = 'CONFIRMED' "
                            + "AND check_in_date < ? AND check_out_date > ?";
                    try (PreparedStatement statement = connection.prepareStatement(conflict)) {
                        statement.setLong(1, roomId);
                        statement.setLong(2, bookingId);
                        statement.setDate(3, checkOut);
                        statement.setDate(4, checkIn);
                        try (ResultSet result = statement.executeQuery()) {
                            result.next();
                            if (result.getLong(1) > 0) {
                                connection.rollback();
                                return false;
                            }
                        }
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE bookings SET status = ? WHERE booking_id = ? AND status = ?")) {
                    statement.setString(1, status);
                    statement.setLong(2, bookingId);
                    statement.setString(3, expectedStatus);
                    if (statement.executeUpdate() != 1) {
                        connection.rollback();
                        return false;
                    }
                }
                connection.commit();
                return true;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }
}
