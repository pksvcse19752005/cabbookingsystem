package com.cabbooking.model;

import java.time.LocalDateTime;

public class Ride {
    private int rideId;
    private int bookingId;
    private int driverId;
    private int vehicleId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double actualDistanceKm;
    private String status; // ASSIGNED, ACCEPTED, REJECTED, ONGOING, COMPLETED, CANCELLED

    public Ride() {}

    public int getRideId() { return rideId; }
    public void setRideId(int rideId) { this.rideId = rideId; }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getDriverId() { return driverId; }
    public void setDriverId(int driverId) { this.driverId = driverId; }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public double getActualDistanceKm() { return actualDistanceKm; }
    public void setActualDistanceKm(double actualDistanceKm) { this.actualDistanceKm = actualDistanceKm; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
