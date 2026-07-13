package com.cabbooking.service;

import com.cabbooking.dao.*;
import com.cabbooking.model.Driver;
import com.cabbooking.model.Ride;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class DriverService {

    private final DriverDAO driverDAO = new DriverDAO();
    private final RideDAO rideDAO = new RideDAO();

    public static class ServiceException extends Exception {
        public ServiceException(String message) { super(message); }
    }

    // ---- Admin actions ----

    public List<Driver> pendingApprovals() throws SQLException {
        return driverDAO.findPending();
    }

    public boolean approveDriver(int driverId) throws SQLException {
        return driverDAO.setApprovalStatus(driverId, "APPROVED");
    }

    public boolean rejectDriver(int driverId) throws SQLException {
        return driverDAO.setApprovalStatus(driverId, "REJECTED");
    }

    // ---- Driver self-service ----

    public boolean goOnline(int driverId) throws SQLException {
        return driverDAO.setOnlineStatus(driverId, true);
    }

    public boolean goOffline(int driverId) throws SQLException {
        return driverDAO.setOnlineStatus(driverId, false);
    }

    public List<Ride> todaysRides(int driverId) throws SQLException {
        return rideDAO.findTodayByDriver(driverId);
    }

    public void respondToRide(int rideId, boolean accept) throws SQLException {
        rideDAO.respondToRide(rideId, accept);
    }

    public void startTrip(int rideId) throws SQLException {
        rideDAO.startTrip(rideId);
    }

    public void endTrip(int rideId, double actualDistanceKm, int driverId, int bookingId, double fare)
            throws SQLException {
        rideDAO.endTrip(rideId, actualDistanceKm);
        recordEarning(driverId, bookingId, fare);
    }

    private void recordEarning(int driverId, int bookingId, double amount) throws SQLException {
        String sql = "INSERT INTO Earnings (driver_id, booking_id, amount) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, driverId);
            ps.setInt(2, bookingId);
            ps.setDouble(3, amount);
            ps.executeUpdate();
        }
    }

    public double totalEarnings(int driverId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount),0) AS total FROM Earnings WHERE driver_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, driverId);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("total");
            }
        }
        return 0.0;
    }
}
