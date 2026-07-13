package com.cabbooking.model;

public class Driver {
    private int driverId;
    private String fullName;
    private String email;
    private String phone;
    private String passwordHash;
    private String licenseNumber;
    private String licenseDocPath;
    private String approvalStatus; // PENDING, APPROVED, REJECTED
    private double ratingAvg;
    private String city;

    public Driver() {}

    public Driver(int driverId, String fullName, String email, String phone,
                  String licenseNumber, String approvalStatus, double ratingAvg, String city) {
        this.driverId = driverId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.licenseNumber = licenseNumber;
        this.approvalStatus = approvalStatus;
        this.ratingAvg = ratingAvg;
        this.city = city;
    }

    public int getDriverId() { return driverId; }
    public void setDriverId(int driverId) { this.driverId = driverId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public String getLicenseDocPath() { return licenseDocPath; }
    public void setLicenseDocPath(String licenseDocPath) { this.licenseDocPath = licenseDocPath; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public double getRatingAvg() { return ratingAvg; }
    public void setRatingAvg(double ratingAvg) { this.ratingAvg = ratingAvg; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    @Override
    public String toString() {
        return fullName + " [" + approvalStatus + "] rating=" + ratingAvg;
    }
}
