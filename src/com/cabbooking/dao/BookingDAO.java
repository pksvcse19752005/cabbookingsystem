package com.cabbooking.dao;

import com.cabbooking.model.Booking;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookingDAO {

    public int createBooking(Booking b) throws SQLException {
        String sql = "INSERT INTO Booking (customer_id, category_id, pickup_address, drop_address, " +
                "pickup_lat, pickup_lng, drop_lat, drop_lng, distance_km, estimated_fare, " +
                "scheduled_time, promo_id, status) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, b.getCustomerId());
            ps.setInt(2, b.getCategoryId());
            ps.setString(3, b.getPickupAddress());
            ps.setString(4, b.getDropAddress());
            ps.setDouble(5, b.getPickupLat());
            ps.setDouble(6, b.getPickupLng());
            ps.setDouble(7, b.getDropLat());
            ps.setDouble(8, b.getDropLng());
            ps.setDouble(9, b.getDistanceKm());
            ps.setDouble(10, b.getEstimatedFare());
            if (b.getScheduledTime() != null) {
                ps.setTimestamp(11, Timestamp.valueOf(b.getScheduledTime()));
            } else {
                ps.setNull(11, Types.TIMESTAMP);
            }
            if (b.getPromoId() != null) ps.setInt(12, b.getPromoId());
            else ps.setNull(12, Types.INTEGER);
            ps.setString(13, b.getStatus());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    logHistory(con, id, b.getStatus());
                    return id;
                }
            }
        }
        return -1;
    }

    public Optional<Booking> findById(int bookingId) throws SQLException {
        String sql = "SELECT * FROM Booking WHERE booking_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public List<Booking> findByCustomer(int customerId, int limit, int offset) throws SQLException {
        String sql = "SELECT * FROM Booking WHERE customer_id = ? ORDER BY booking_id DESC LIMIT ? OFFSET ?";
        List<Booking> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public List<Booking> findByStatus(String status) throws SQLException {
        String sql = "SELECT * FROM Booking WHERE status = ? ORDER BY booking_id DESC";
        List<Booking> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public boolean updateStatus(int bookingId, String status) throws SQLException {
        String sql = "UPDATE Booking SET status=? WHERE booking_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, bookingId);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) logHistory(con, bookingId, status);
            return ok;
        }
    }

    private void logHistory(Connection con, int bookingId, String status) throws SQLException {
        String sql = "INSERT INTO BookingHistory (booking_id, status) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setString(2, status);
            ps.executeUpdate();
        }
    }

    private Booking map(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setBookingId(rs.getInt("booking_id"));
        b.setCustomerId(rs.getInt("customer_id"));
        b.setCategoryId(rs.getInt("category_id"));
        b.setPickupAddress(rs.getString("pickup_address"));
        b.setDropAddress(rs.getString("drop_address"));
        b.setPickupLat(rs.getDouble("pickup_lat"));
        b.setPickupLng(rs.getDouble("pickup_lng"));
        b.setDropLat(rs.getDouble("drop_lat"));
        b.setDropLng(rs.getDouble("drop_lng"));
        b.setDistanceKm(rs.getDouble("distance_km"));
        b.setEstimatedFare(rs.getDouble("estimated_fare"));
        Timestamp sched = rs.getTimestamp("scheduled_time");
        if (sched != null) b.setScheduledTime(sched.toLocalDateTime());
        int promo = rs.getInt("promo_id");
        b.setPromoId(rs.wasNull() ? null : promo);
        b.setStatus(rs.getString("status"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) b.setCreatedAt(created.toLocalDateTime());
        return b;
    }
}
