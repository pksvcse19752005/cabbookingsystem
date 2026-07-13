package com.cabbooking.model;

public class Vehicle {
    private int vehicleId;
    private int driverId;
    private int categoryId;
    private String registrationNumber;
    private String modelName;
    private String color;
    private String city;

    public Vehicle() {}

    public Vehicle(int vehicleId, int driverId, int categoryId, String registrationNumber,
                    String modelName, String color, String city) {
        this.vehicleId = vehicleId;
        this.driverId = driverId;
        this.categoryId = categoryId;
        this.registrationNumber = registrationNumber;
        this.modelName = modelName;
        this.color = color;
        this.city = city;
    }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public int getDriverId() { return driverId; }
    public void setDriverId(int driverId) { this.driverId = driverId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
}
