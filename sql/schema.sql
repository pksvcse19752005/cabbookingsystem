-- ============================================================
DROP DATABASE IF EXISTS cab_booking_system;
CREATE DATABASE cab_booking_system;
USE cab_booking_system;
 
-- ---------------- ADMIN ----------------
CREATE TABLE Admin (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
 
-- ---------------- CUSTOMER ----------------
CREATE TABLE Customer (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(15) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    security_question VARCHAR(255),
    security_answer_hash VARCHAR(255),
    wallet_balance DECIMAL(10,2) DEFAULT 0.00,
    reward_points INT DEFAULT 0,
    status ENUM('ACTIVE','BLOCKED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
 
-- ---------------- DRIVER ----------------
CREATE TABLE Driver (
    driver_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(15) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    license_number VARCHAR(50) NOT NULL UNIQUE,
    license_doc_path VARCHAR(255),
    approval_status ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
    rating_avg DECIMAL(3,2) DEFAULT 0.00,
    city VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
 
-- ---------------- CAB CATEGORY ----------------
CREATE TABLE CabCategory (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(30) NOT NULL UNIQUE, -- Mini, Sedan, SUV, Luxury
    base_fare DECIMAL(8,2) NOT NULL,
    per_km_rate DECIMAL(8,2) NOT NULL,
    per_minute_rate DECIMAL(8,2) NOT NULL,
    capacity INT NOT NULL
);
 
-- ---------------- VEHICLE ----------------
CREATE TABLE Vehicle (
    vehicle_id INT AUTO_INCREMENT PRIMARY KEY,
    driver_id INT NOT NULL,
    category_id INT NOT NULL,
    registration_number VARCHAR(20) NOT NULL UNIQUE,
    model_name VARCHAR(50),
    color VARCHAR(30),
    city VARCHAR(50),
    FOREIGN KEY (driver_id) REFERENCES Driver(driver_id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES CabCategory(category_id)
);
 
-- ---------------- DRIVER AVAILABILITY ----------------
CREATE TABLE DriverAvailability (
    availability_id INT AUTO_INCREMENT PRIMARY KEY,
    driver_id INT NOT NULL UNIQUE,
    is_online BOOLEAN DEFAULT FALSE,
    last_lat DECIMAL(10,6),
    last_lng DECIMAL(10,6),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (driver_id) REFERENCES Driver(driver_id) ON DELETE CASCADE
);
 
-- ---------------- FARE (pricing rules) ----------------
CREATE TABLE Fare (
    fare_id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT NOT NULL,
    peak_multiplier DECIMAL(4,2) DEFAULT 1.00,
    night_charge_flat DECIMAL(8,2) DEFAULT 0.00,
    cancellation_fee DECIMAL(8,2) DEFAULT 0.00,
    FOREIGN KEY (category_id) REFERENCES CabCategory(category_id)
);
 
-- ---------------- PROMO CODE ----------------
CREATE TABLE PromoCode (
    promo_id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    discount_percent DECIMAL(5,2),
    max_discount DECIMAL(8,2),
    valid_from DATE,
    valid_to DATE,
    active BOOLEAN DEFAULT TRUE
);
 
-- ---------------- SAVED LOCATION ----------------
CREATE TABLE SavedLocation (
    location_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    label VARCHAR(30), -- Home, Office, etc.
    address VARCHAR(255) NOT NULL,
    lat DECIMAL(10,6),
    lng DECIMAL(10,6),
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id) ON DELETE CASCADE
);
 
-- ---------------- BOOKING ----------------
CREATE TABLE Booking (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    category_id INT NOT NULL,
    pickup_address VARCHAR(255) NOT NULL,
    drop_address VARCHAR(255) NOT NULL,
    pickup_lat DECIMAL(10,6), pickup_lng DECIMAL(10,6),
    drop_lat DECIMAL(10,6), drop_lng DECIMAL(10,6),
    distance_km DECIMAL(8,2),
    estimated_fare DECIMAL(10,2),
    scheduled_time DATETIME NULL, -- NULL = instant booking
    promo_id INT NULL,
    status ENUM('PENDING','ASSIGNED','CONFIRMED','CANCELLED','COMPLETED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id),
    FOREIGN KEY (category_id) REFERENCES CabCategory(category_id),
    FOREIGN KEY (promo_id) REFERENCES PromoCode(promo_id)
);
 
-- ---------------- RIDE ----------------
CREATE TABLE Ride (
    ride_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL UNIQUE,
    driver_id INT NOT NULL,
    vehicle_id INT NOT NULL,
    start_time DATETIME NULL,
    end_time DATETIME NULL,
    actual_distance_km DECIMAL(8,2),
    status ENUM('ASSIGNED','ACCEPTED','REJECTED','ONGOING','COMPLETED','CANCELLED') DEFAULT 'ASSIGNED',
    FOREIGN KEY (booking_id) REFERENCES Booking(booking_id) ON DELETE CASCADE,
    FOREIGN KEY (driver_id) REFERENCES Driver(driver_id),
    FOREIGN KEY (vehicle_id) REFERENCES Vehicle(vehicle_id)
);
 
-- ---------------- PAYMENT ----------------
CREATE TABLE Payment (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL UNIQUE,
    amount DECIMAL(10,2) NOT NULL,
    method ENUM('CASH','CARD','UPI','WALLET') NOT NULL,
    status ENUM('PENDING','SUCCESS','FAILED') DEFAULT 'PENDING',
    paid_at TIMESTAMP NULL,
    FOREIGN KEY (booking_id) REFERENCES Booking(booking_id) ON DELETE CASCADE
);
 
-- ---------------- RATING ----------------
CREATE TABLE Rating (
    rating_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL UNIQUE,
    customer_id INT NOT NULL,
    driver_id INT NOT NULL,
    stars TINYINT NOT NULL CHECK (stars BETWEEN 1 AND 5),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES Booking(booking_id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id),
    FOREIGN KEY (driver_id) REFERENCES Driver(driver_id)
);
 
-- ---------------- REVIEW ----------------
CREATE TABLE Review (
    review_id INT AUTO_INCREMENT PRIMARY KEY,
    rating_id INT NOT NULL UNIQUE,
    comment VARCHAR(500),
    FOREIGN KEY (rating_id) REFERENCES Rating(rating_id) ON DELETE CASCADE
);
 
-- ---------------- WALLET (transaction log) ----------------
CREATE TABLE Wallet (
    txn_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    txn_type ENUM('CREDIT','DEBIT') NOT NULL,
    reference VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id) ON DELETE CASCADE
);
 
-- ---------------- REWARD POINTS (transaction log) ----------------
CREATE TABLE RewardPoints (
    reward_txn_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    points INT NOT NULL,
    reason VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id) ON DELETE CASCADE
);
 
-- ---------------- NOTIFICATION ----------------
CREATE TABLE Notification (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    user_type ENUM('CUSTOMER','DRIVER','ADMIN') NOT NULL,
    user_id INT NOT NULL,
    message VARCHAR(255) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
 
-- ---------------- EARNINGS (driver) ----------------
CREATE TABLE Earnings (
    earning_id INT AUTO_INCREMENT PRIMARY KEY,
    driver_id INT NOT NULL,
    booking_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (driver_id) REFERENCES Driver(driver_id) ON DELETE CASCADE,
    FOREIGN KEY (booking_id) REFERENCES Booking(booking_id)
);
 
-- ---------------- BOOKING HISTORY (denormalized audit trail) ----------------
CREATE TABLE BookingHistory (
    history_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    status ENUM('PENDING','ASSIGNED','CONFIRMED','CANCELLED','COMPLETED') NOT NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES Booking(booking_id) ON DELETE CASCADE
);
 
-- ---------------- SYSTEM SETTINGS ----------------
CREATE TABLE SystemSettings (
    setting_key VARCHAR(50) PRIMARY KEY,
    setting_value VARCHAR(255) NOT NULL
);
 
-- ---------------- Seed data ----------------
INSERT INTO CabCategory (category_name, base_fare, per_km_rate, per_minute_rate, capacity) VALUES
('Mini', 30.00, 8.00, 1.00, 4),
('Sedan', 50.00, 11.00, 1.50, 4),
('SUV', 80.00, 14.00, 2.00, 6),
('Luxury', 150.00, 22.00, 3.00, 4);
 
INSERT INTO Fare (category_id, peak_multiplier, night_charge_flat, cancellation_fee)
SELECT category_id, 1.25, 40.00, 50.00 FROM CabCategory;
 
INSERT INTO SystemSettings (setting_key, setting_value) VALUES
('PEAK_HOURS', '08:00-10:00,18:00-21:00'),
('NIGHT_HOURS', '23:00-05:00'),
('FREE_CANCEL_MINUTES', '5');
 
INSERT INTO Admin (username, password_hash, email) VALUES
('admin', 'CHANGE_ME_HASH', 'admin@cabsystem.com');
