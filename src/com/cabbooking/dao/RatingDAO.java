package com.cabbooking.dao;

import com.cabbooking.model.Rating;

import java.sql.*;

public class RatingDAO {

    /** Inserts Rating row and, if a comment is present, the linked Review row. */
    public int addRating(Rating r) throws SQLException {
        String sql = "INSERT INTO Rating (booking_id, customer_id, driver_id, stars) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getBookingId());
            ps.setInt(2, r.getCustomerId());
            ps.setInt(3, r.getDriverId());
            ps.setInt(4, r.getStars());
            ps.executeUpdate();
            int ratingId = -1;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) ratingId = rs.getInt(1);
            }
            if (ratingId != -1 && r.getComment() != null && !r.getComment().isBlank()) {
                try (PreparedStatement ps2 = con.prepareStatement(
                        "INSERT INTO Review (rating_id, comment) VALUES (?, ?)")) {
                    ps2.setInt(1, ratingId);
                    ps2.setString(2, r.getComment());
                    ps2.executeUpdate();
                }
            }
            return ratingId;
        }
    }

    public double averageForDriver(int driverId) throws SQLException {
        String sql = "SELECT COALESCE(AVG(stars),0) AS avg_stars FROM Rating WHERE driver_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, driverId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("avg_stars");
            }
        }
        return 0.0;
    }
}
