package com.cabbooking.model;

public class Rating {
    private int ratingId;
    private int bookingId;
    private int customerId;
    private int driverId;
    private int stars; // 1-5
    private String comment; // optional review text

    public Rating() {}

    public Rating(int bookingId, int customerId, int driverId, int stars, String comment) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.driverId = driverId;
        this.stars = stars;
        this.comment = comment;
    }

    public int getRatingId() { return ratingId; }
    public void setRatingId(int ratingId) { this.ratingId = ratingId; }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getDriverId() { return driverId; }
    public void setDriverId(int driverId) { this.driverId = driverId; }

    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
