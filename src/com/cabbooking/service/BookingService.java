package com.cabbooking.service;

import com.cabbooking.dao.*;
import com.cabbooking.model.*;

import java.sql.SQLException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Orchestrates the full booking workflow:
 * estimate -> create booking -> auto-assign driver -> confirm -> ride -> payment -> rating.
 */
public class BookingService {

    private final BookingDAO bookingDAO = new BookingDAO();
    private final RideDAO rideDAO = new RideDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final RatingDAO ratingDAO = new RatingDAO();
    private final DriverDAO driverDAO = new DriverDAO();
    private final VehicleDAO vehicleDAO = new VehicleDAO();
    private final FareService fareService = new FareService();

    public static class ServiceException extends Exception {
        public ServiceException(String message) { super(message); }
    }

    /** Haversine distance in km between two lat/lng points. */
    public double calculateDistanceKm(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public double estimateFare(CabCategory category, double distanceKm) {
        return fareService.estimateFare(category, distanceKm, LocalTime.now());
    }

    /** Step 1-3: create a booking record in PENDING state. */
    public int createBooking(Booking booking) throws SQLException {
        booking.setStatus("PENDING");
        return bookingDAO.createBooking(booking);
    }

    /**
     * Step 4-5: auto-assign the best-rated available driver in the customer's city
     * for the requested category. Returns the assigned driver, or empty if none free.
     */
    public Optional<Driver> autoAssignDriver(int bookingId, int categoryId, String city) throws SQLException {
        List<Driver> candidates = driverDAO.findAvailableDrivers(categoryId, city);
        if (candidates.isEmpty()) return Optional.empty();

        Driver chosen = candidates.get(0); // highest rated (query already sorts DESC)
        Vehicle vehicle = vehicleDAO.findByDriverId(chosen.getDriverId())
                .orElseThrow(() -> new SQLException("Driver has no registered vehicle."));

        rideDAO.assignRide(bookingId, chosen.getDriverId(), vehicle.getVehicleId());
        bookingDAO.updateStatus(bookingId, "ASSIGNED");
        return Optional.of(chosen);
    }

    /** Step 6: customer confirms after seeing assigned driver. */
    public void confirmBooking(int bookingId) throws SQLException {
        bookingDAO.updateStatus(bookingId, "CONFIRMED");
    }

    /** Cancellation with time-based policy (free within FREE_CANCEL_MINUTES of creation). */
    public double cancelBooking(int bookingId) throws SQLException, ServiceException {
        Booking b = bookingDAO.findById(bookingId)
                .orElseThrow(() -> new ServiceException("Booking not found."));
        if ("COMPLETED".equals(b.getStatus()) || "CANCELLED".equals(b.getStatus())) {
            throw new ServiceException("Booking already " + b.getStatus().toLowerCase() + ".");
        }
        long minutesSinceCreated = ChronoUnit.MINUTES.between(b.getCreatedAt(), java.time.LocalDateTime.now());
        double fee = minutesSinceCreated <= 5 ? 0.0 : fareService.applyCancellationFee(50.0);
        bookingDAO.updateStatus(bookingId, "CANCELLED");
        return fee;
    }

    // ---- Ride lifecycle (delegates to RideDAO via DriverService in UI layer) ----

    public Optional<Ride> getRide(int bookingId) throws SQLException {
        return rideDAO.findByBookingId(bookingId);
    }

    // ---- Payment ----

    public int recordPayment(int bookingId, double amount, String method) throws SQLException {
        Payment p = new Payment(bookingId, amount, method);
        int paymentId = paymentDAO.createPayment(p);
        paymentDAO.markSuccess(paymentId); // simulated instant success for Cash/Card/UPI
        bookingDAO.updateStatus(bookingId, "COMPLETED");
        return paymentId;
    }

    // ---- Rating ----

    public void rateDriver(int bookingId, int customerId, int driverId, int stars, String comment)
            throws SQLException {
        Rating r = new Rating(bookingId, customerId, driverId, stars, comment);
        ratingDAO.addRating(r);
        driverDAO.recalculateAverageRating(driverId);
    }

    // ---- History ----

    public List<Booking> customerHistory(int customerId, int page, int pageSize) throws SQLException {
        return bookingDAO.findByCustomer(customerId, pageSize, page * pageSize);
    }
}
