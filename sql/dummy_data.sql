-- SQL Script for creating the Gym Database Schema
DROP SCHEMA IF EXISTS gym_db;

CREATE DATABASE IF NOT EXISTS gym_db;

USE gym_db;

-- Disable FK checks for clean creation
SET FOREIGN_KEY_CHECKS = 0;

-- 1. GymPersonnel Table
CREATE TABLE GymPersonnel (
    personnelID INT AUTO_INCREMENT PRIMARY KEY,
    firstName VARCHAR(100) NOT NULL,
    lastName VARCHAR(100) NOT NULL,
    personnelType VARCHAR(50),
    schedule TEXT,
    instructorRecord TEXT,
    speciality VARCHAR(255)
);

-- 2. Product Table
CREATE TABLE Product (
    productID INT AUTO_INCREMENT PRIMARY KEY,
    productName VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    price DECIMAL(10, 2) NOT NULL,
    stockQty INT DEFAULT 0
);

-- 3. Class Table
CREATE TABLE Class (
    classID INT AUTO_INCREMENT PRIMARY KEY,
    className VARCHAR(100) NOT NULL,
    classType VARCHAR(100),
    scheduleDate DATE,
    startTime TIME,
    endTime TIME,
    personnelID INT,
    FOREIGN KEY (personnelID) REFERENCES GymPersonnel(personnelID) ON DELETE SET NULL
);

-- 4. Locker Table
CREATE TABLE Locker (
    lockerID INT AUTO_INCREMENT PRIMARY KEY,
    status VARCHAR(50) DEFAULT 'available',
    rentalStartDate DATE,
    rentalEndDate DATE
);

-- 5. Member Table
CREATE TABLE Member (
    memberID INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    contact_no VARCHAR(20),
    membership_type VARCHAR(50),
    start_date DATE,
    end_date DATE,
    health_goal TEXT,
    initial_weight DECIMAL(5, 2),
    goal_weight DECIMAL(5, 2),
    start_bmi DECIMAL(4, 2),
    updated_bmi DECIMAL(4, 2),
    classID INT,
    trainerID INT,
    lockerID INT UNIQUE,
    FOREIGN KEY (classID) REFERENCES Class(classID) ON DELETE SET NULL,
    FOREIGN KEY (trainerID) REFERENCES GymPersonnel(personnelID) ON DELETE SET NULL,
    FOREIGN KEY (lockerID) REFERENCES Locker(lockerID) ON DELETE SET NULL
);

-- 6. Payment Table
CREATE TABLE Payment (
    paymentID INT AUTO_INCREMENT PRIMARY KEY,
    payment_num VARCHAR(100),
    payment_date DATETIME NOT NULL,
    transaction_type VARCHAR(100),
    amount DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(100) NOT NULL,
    memberID INT NOT NULL,
    FOREIGN KEY (memberID) REFERENCES Member(memberID)
);

-- 7. MemberFeedback Table
CREATE TABLE MemberFeedback (
    feedbackID INT AUTO_INCREMENT PRIMARY KEY,
    comments TEXT,
    personnelID INT NOT NULL,
    memberID INT NOT NULL,
    FOREIGN KEY (personnelID) REFERENCES GymPersonnel(personnelID),
    FOREIGN KEY (memberID) REFERENCES Member(memberID)
);

-- 8. Purchase Table
CREATE TABLE Purchase (
    purchaseID INT AUTO_INCREMENT PRIMARY KEY,
    purchase_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    quantity INT NOT NULL,
    memberID INT NOT NULL,
    productID INT NOT NULL,
    FOREIGN KEY (memberID) REFERENCES Member(memberID),
    FOREIGN KEY (productID) REFERENCES Product(productID)
);

-- 9. Attendance Table
CREATE TABLE Attendance (
    attendanceID INT AUTO_INCREMENT PRIMARY KEY,
    attendance_datetime DATETIME DEFAULT CURRENT_TIMESTAMP,
    memberID INT NOT NULL,
    classID INT NOT NULL,
    FOREIGN KEY (memberID) REFERENCES Member(memberID),
    FOREIGN KEY (classID) REFERENCES Class(classID)
);

-- 10. Equipment Table
CREATE TABLE Equipment (
    equipmentID INT AUTO_INCREMENT PRIMARY KEY,
    equipment_name VARCHAR(100) NOT NULL,
    equipment_description VARCHAR(255) DEFAULT NULL,
    quantity INT NOT NULL CHECK (quantity >= 0),
    unit_price DECIMAL(10, 2) NOT NULL,
    vendor VARCHAR(100) NOT NULL,
    contact_no VARCHAR(20) NOT NULL,
    purchase_date DATETIME NOT NULL,
    INDEX idx_equipment_name (equipment_name)
);

-- Re-enable FK checks
SET FOREIGN_KEY_CHECKS = 1;

-- ============================
-- Sample Data (ordered for FK)
-- ============================

-- GymPersonnel
INSERT INTO GymPersonnel (personnelID, firstName, lastName, personnelType, schedule, instructorRecord, speciality) VALUES
(1, 'John', 'Doe', 'Trainer', 'Mon/Wed/Fri 06:00-08:00', 'Certified PT Level 2', 'Strength'),
(2, 'Jane', 'Smith', 'Front Desk', 'Tue/Thu 09:00-17:00', NULL, NULL),
(3, 'Mike', 'Lee', 'Trainer', 'Sat/Sun 08:00-10:00', 'Crossfit L1', 'Cardio');

-- Product
INSERT INTO Product (productID, productName, category, price, stockQty) VALUES
(1, 'Protein Bar', 'Nutrition', 2.50, 50),
(2, 'Yoga Mat', 'Equipment', 25.00, 20),
(3, 'Shaker Bottle', 'Accessory', 8.99, 100);

-- Locker
INSERT INTO Locker (lockerID, status, rentalStartDate, rentalEndDate) VALUES
(1, 'available', NULL, NULL),
(2, 'rented', '2025-01-01', '2025-12-31'),
(3, 'available', NULL, NULL);

-- Class
INSERT INTO Class (classID, className, classType, scheduleDate, startTime, endTime, personnelID) VALUES
(1, 'Morning Yoga', 'Yoga', '2025-11-10', '07:00:00', '08:00:00', 1),
(2, 'Spin Blast', 'Spin', '2025-11-11', '18:00:00', '19:00:00', 3),
(3, 'Strength Circuit', 'Strength', '2025-11-12', '17:00:00', '18:00:00', 1);

-- Member
INSERT INTO Member (memberID, first_name, last_name, email, contact_no, membership_type, start_date, end_date, health_goal, initial_weight, goal_weight, start_bmi, updated_bmi, classID, trainerID, lockerID) VALUES
(1, 'Alice', 'Johnson', 'alice.j@example.com', '09171234567', 'Monthly', '2025-01-01', '2025-12-31', 'Lose weight', 80.50, 70.00, 28.50, 27.00, 1, 1, 2),
(2, 'Bob', 'Reyes', 'bob.reyes@example.com', '09179876543', 'Monthly', '2025-02-01', '2025-12-31', 'Improve stamina', 75.00, 72.00, 26.80, 26.50, 2, 3, 1),
(3, 'Carol', 'Tan', 'carol.tan@example.com', '09170001111', 'Annual', '2024-06-15', '2025-06-14', 'Gain muscle', 62.00, 68.00, 22.10, 23.00, 3, 1, 3);

-- Payment
INSERT INTO Payment (paymentID, payment_num, payment_date, transaction_type, amount, payment_method, memberID) VALUES
(1, 'RCPT-0001', '2025-01-01 10:00:00', 'Membership', 50.00, 'Credit Card', 1),
(2, 'RCPT-0002', '2025-02-01 11:30:00', 'Membership', 50.00, 'Cash', 2),
(3, 'RCPT-0003', '2024-06-15 09:15:00', 'Membership', 550.00, 'Credit Card', 3);

-- MemberFeedback
INSERT INTO MemberFeedback (feedbackID, comments, personnelID, memberID) VALUES
(1, 'John pushed me but in a good way. Great trainer!', 1, 1),
(2, 'Front desk was super helpful booking classes.', 2, 2);

-- Purchase
INSERT INTO Purchase (purchaseID, purchase_date, quantity, memberID, productID) VALUES
(1, '2025-01-05 14:20:00', 2, 1, 1),
(2, '2025-03-03 16:00:00', 1, 2, 2),
(3, '2025-01-07 12:00:00', 1, 3, 3);

-- Attendance
INSERT INTO Attendance (attendanceID, attendance_datetime, memberID, classID) VALUES
(1, '2025-11-10 07:05:00', 1, 1),
(2, '2025-11-11 18:02:00', 2, 2),
(3, '2025-11-12 17:10:00', 3, 3),
(4, '2025-11-10 07:06:00', 2, 1);

-- Equipment
INSERT INTO Equipment (equipmentID, equipment_name, equipment_description, quantity, unit_price, vendor, contact_no, purchase_date) VALUES
(1, 'Treadmill', 'Electric treadmill with speed and incline controls', 5, 1500.00, 'FitEquip Co.', '09175551234', '2025-01-10 09:00:00'),
(2, 'Dumbbell Set', 'Full rack from 5kg to 30kg', 10, 800.00, 'IronWorks Ltd.', '09172223333', '2025-02-15 10:30:00'),
(3, 'Stationary Bike', 'Adjustable resistance spin bikes', 7, 1200.00, 'CardioWorld', '09179998888', '2025-03-20 14:00:00');
