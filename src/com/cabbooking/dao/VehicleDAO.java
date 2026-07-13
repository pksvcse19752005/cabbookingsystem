package com.cabbooking.dao;

import com.cabbooking.model.Vehicle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VehicleDAO {

    public int addVehicle(Vehicle v) throws SQLException {
        String sql = "INSERT INTO Vehicle (driver_id, category_id, registration_number, " +
                "model_name, color, city) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, v.getDriverId());
            ps.setInt(2, v.getCategoryId());
            ps.setString(3, v.getRegistrationNumber());
            ps.setString(4, v.getModelName());
            ps.setString(5, v.getColor());
            ps.setString(6, v.getCity());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public Optional<Vehicle> findByDriverId(int driverId) throws SQLException {
        String sql = "SELECT * FROM Vehicle WHERE driver_id = ? LIMIT 1";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, driverId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public List<Vehicle> findByCategory(int categoryId) throws SQLException {
        String sql = "SELECT * FROM Vehicle WHERE category_id = ?";
        List<Vehicle> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    private Vehicle map(ResultSet rs) throws SQLException {
        Vehicle v = new Vehicle();
        v.setVehicleId(rs.getInt("vehicle_id"));
        v.setDriverId(rs.getInt("driver_id"));
        v.setCategoryId(rs.getInt("category_id"));
        v.setRegistrationNumber(rs.getString("registration_number"));
        v.setModelName(rs.getString("model_name"));
        v.setColor(rs.getString("color"));
        v.setCity(rs.getString("city"));
        return v;
    }
}
