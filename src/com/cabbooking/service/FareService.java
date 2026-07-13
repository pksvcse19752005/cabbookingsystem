package com.cabbooking.service;

import com.cabbooking.model.CabCategory;

import java.time.LocalTime;

/**
 * Computes ride fare: base + distance + time, with peak-hour and
 * night-charge multipliers applied on top.
 */
public class FareService {

    private static final LocalTime PEAK1_START = LocalTime.of(8, 0);
    private static final LocalTime PEAK1_END = LocalTime.of(10, 0);
    private static final LocalTime PEAK2_START = LocalTime.of(18, 0);
    private static final LocalTime PEAK2_END = LocalTime.of(21, 0);
    private static final LocalTime NIGHT_START = LocalTime.of(23, 0);
    private static final LocalTime NIGHT_END = LocalTime.of(5, 0);

    private static final double PEAK_MULTIPLIER = 1.25;
    private static final double NIGHT_FLAT_CHARGE = 40.0;
    private static final double AVG_SPEED_KMPH = 30.0; // used to estimate minutes from km

    /**
     * @param category    selected cab category with its rate config
     * @param distanceKm  trip distance
     * @param rideTime    time of day the ride occurs (use LocalTime.now() for instant bookings)
     */
    public double estimateFare(CabCategory category, double distanceKm, LocalTime rideTime) {
        double estimatedMinutes = (distanceKm / AVG_SPEED_KMPH) * 60.0;

        double fare = category.getBaseFare()
                + (distanceKm * category.getPerKmRate())
                + (estimatedMinutes * category.getPerMinuteRate());

        if (isPeakHour(rideTime)) {
            fare *= PEAK_MULTIPLIER;
        }
        if (isNightHour(rideTime)) {
            fare += NIGHT_FLAT_CHARGE;
        }
        return round2(fare);
    }

    public boolean isPeakHour(LocalTime t) {
        return isBetween(t, PEAK1_START, PEAK1_END) || isBetween(t, PEAK2_START, PEAK2_END);
    }

    public boolean isNightHour(LocalTime t) {
        // wraps past midnight: 23:00 -> 05:00
        return t.isAfter(NIGHT_START) || t.isBefore(NIGHT_END) || t.equals(NIGHT_START);
    }

    private boolean isBetween(LocalTime t, LocalTime start, LocalTime end) {
        return !t.isBefore(start) && !t.isAfter(end);
    }

    /** Applies a percentage discount capped at maxDiscount (used for promo codes). */
    public double applyPromo(double fare, double discountPercent, double maxDiscount) {
        double discount = Math.min(fare * (discountPercent / 100.0), maxDiscount);
        return round2(fare - discount);
    }

    public double applyCancellationFee(double flatFee) {
        return round2(flatFee);
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
