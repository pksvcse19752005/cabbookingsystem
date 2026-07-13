package com.cabbooking.dao;

import com.cabbooking.model.Ride;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RideDAO {

    public int assignRide(int bookingId, int driverId, int vehicleId) throws SQLException {
        String sql = "INSERT INTO Ride (booking_id, driver_id, vehicle_id, status) VALUES (?, ?, ?, 'ASSIGNED')";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, driverId);
            ps.setInt(3, vehicleId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public Optional<Ride> findByBookingId(int bookingId) throws SQLException {
        String sql = "SELECT * FROM Ride WHERE booking_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public List<Ride> findTodayByDriver(int driverId) throws SQLException {
        String sql = "SELECT * FROM Ride WHERE driver_id = ? AND DATE(COALESCE(start_time, NOW())) = CURDATE()";
        List<Ride> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, driverId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public boolean respondToRide(int rideId, boolean accept) throws SQLException {
        String sql = "UPDATE Ride SET status=? WHERE ride_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, accept ? "ACCEPTED" : "REJECTED");
            ps.setInt(2, rideId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean startTrip(int rideId) throws SQLException {
        String sql = "UPDATE Ride SET status='ONGOING', start_time=NOW() WHERE ride_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, rideId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean endTrip(int rideId, double actualDistanceKm) throws SQLException {
        String sql = "UPDATE Ride SET status='COMPLETED', end_time=NOW(), actual_distance_km=? WHERE ride_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, actualDistanceKm);
            ps.setInt(2, rideId);
            return ps.executeUpdate() > 0;
        }
    }

    private Ride map(ResultSet rs) throws SQLException {
        Ride r = new Ride();
        r.setRideId(rs.getInt("ride_id"));
        r.setBookingId(rs.getInt("booking_id"));
        r.setDriverId(rs.getInt("driver_id"));
        r.setVehicleId(rs.getInt("vehicle_id"));
        Timestamp st = rs.getTimestamp("start_time");
        if (st != null) r.setStartTime(st.toLocalDateTime());
        Timestamp et = rs.getTimestamp("end_time");
        if (et != null) r.setEndTime(et.toLocalDateTime());
        r.setActualDistanceKm(rs.getDouble("actual_distance_km"));
        r.setStatus(rs.getString("status"));
        return r;
    }
}
