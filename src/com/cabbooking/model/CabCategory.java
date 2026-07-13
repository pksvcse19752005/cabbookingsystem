package com.cabbooking.model;

public class CabCategory {
    private int categoryId;
    private String categoryName;
    private double baseFare;
    private double perKmRate;
    private double perMinuteRate;
    private int capacity;

    public CabCategory() {}

    public CabCategory(int categoryId, String categoryName, double baseFare,
                        double perKmRate, double perMinuteRate, int capacity) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.baseFare = baseFare;
        this.perKmRate = perKmRate;
        this.perMinuteRate = perMinuteRate;
        this.capacity = capacity;
    }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public double getBaseFare() { return baseFare; }
    public void setBaseFare(double baseFare) { this.baseFare = baseFare; }

    public double getPerKmRate() { return perKmRate; }
    public void setPerKmRate(double perKmRate) { this.perKmRate = perKmRate; }

    public double getPerMinuteRate() { return perMinuteRate; }
    public void setPerMinuteRate(double perMinuteRate) { this.perMinuteRate = perMinuteRate; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    @Override
    public String toString() { return categoryName; }
}
