package com.hotel.dao;

import com.hotel.config.DatabaseConfig;
import com.hotel.models.Room;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {
    public List<Room> findAll() throws IOException, SQLException {
        String sql = "SELECT room_id, room_number, room_type, nightly_rate, available, air_conditioned "
                + "FROM rooms ORDER BY room_number";
        List<Room> rooms = new ArrayList<>();
        try (Connection connection = DatabaseConfig.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                rooms.add(mapRoom(results));
            }
        }
        return rooms;
    }

    public Room findById(long id) throws IOException, SQLException {
        String sql = "SELECT room_id, room_number, room_type, nightly_rate, available, air_conditioned "
                + "FROM rooms WHERE room_id = ?";
        try (Connection connection = DatabaseConfig.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? mapRoom(results) : null;
            }
        }
    }

    public void create(Room room) throws IOException, SQLException {
        String sql = "INSERT INTO rooms (room_number, room_type, nightly_rate, available, air_conditioned) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConfig.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setRoomValues(statement, room);
            statement.executeUpdate();
        }
    }

    public boolean update(Room room) throws IOException, SQLException {
        String sql = "UPDATE rooms SET room_number = ?, room_type = ?, nightly_rate = ?, "
                + "available = ?, air_conditioned = ? WHERE room_id = ?";
        try (Connection connection = DatabaseConfig.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setRoomValues(statement, room);
            statement.setLong(6, room.getId());
            return statement.executeUpdate() == 1;
        }
    }

    private static Room mapRoom(ResultSet results) throws SQLException {
        Room room = new Room();
        room.setId(results.getLong("room_id"));
        room.setRoomNumber(results.getString("room_number"));
        room.setRoomType(results.getString("room_type"));
        room.setNightlyRate(results.getBigDecimal("nightly_rate"));
        room.setAvailable(results.getBoolean("available"));
        room.setAirConditioned(results.getBoolean("air_conditioned"));
        return room;
    }

    private static void setRoomValues(PreparedStatement statement, Room room) throws SQLException {
        statement.setString(1, room.getRoomNumber());
        statement.setString(2, room.getRoomType());
        statement.setBigDecimal(3, room.getNightlyRate());
        statement.setBoolean(4, room.isAvailable());
        statement.setBoolean(5, room.isAirConditioned());
    }
}
