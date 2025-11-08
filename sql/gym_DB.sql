-- SQL Script for creating the Gym Database Schema
DROP SCHEMA IF EXISTS gym_db;
CREATE DATABASE IF NOT EXISTS gym_db;

USE gym_db;

-- Set foreign key checks to 0 to avoid errors during table creation
SET FOREIGN_KEY_CHECKS = 0;

-- 1. GymPersonnel Table
-- This table stores information about trainers and other staff.
CREATE TABLE GymPersonnel (
    personnelID INT AUTO_INCREMENT PRIMARY KEY,
    firstName VARCHAR(100) NOT NULL,
    lastName VARCHAR(100) NOT NULL,
    personnelType VARCHAR(50),  -- e.g., 'Trainer', 'Front Desk'
    schedule TEXT,
    instructorRecord TEXT,
    speciality VARCHAR(255)
);

-- 2. Product Table
-- This table stores information about products sold at the gym.
CREATE TABLE Product (
    productID INT AUTO_INCREMENT PRIMARY KEY,
    productName VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    price DECIMAL(10, 2) NOT NULL,
    stockQty INT DEFAULT 0
);

-- 3. Class Table
-- This table stores details about gym classes.
CREATE TABLE Class (
    classID INT AUTO_INCREMENT PRIMARY KEY,
    className VARCHAR(100) NOT NULL,
    classType VARCHAR(100),
    scheduleDate DATE,
    startTime TIME,
    endTime TIME,
    personnelID INT, -- The instructor for the class
    FOREIGN KEY (personnelID) REFERENCES GymPersonnel(personnelID) ON DELETE SET NULL
);

-- 4. Locker Table (Modified for new Member-Locker relationship)
-- This table manages locker details.
CREATE TABLE Locker (
    lockerID INT AUTO_INCREMENT PRIMARY KEY,
    status VARCHAR(50) DEFAULT 'available',
    rentalStartDate DATE,
    rentalEndDate DATE
);


-- 5. Member Table (Updated with trainerID and lockerID)
-- This table stores information about the gym members.
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
    trainerID INT, -- Added based on new image
    lockerID INT UNIQUE, -- Added based on new image, and set as UNIQUE
    FOREIGN KEY (classID) REFERENCES Class(classID) ON DELETE SET NULL,
    FOREIGN KEY (trainerID) REFERENCES GymPersonnel(personnelID) ON DELETE SET NULL,
    FOREIGN KEY (lockerID) REFERENCES Locker(lockerID) ON DELETE SET NULL
);

-- 6. Payment Table
-- This table records payments made by members.
CREATE TABLE Payment (
    paymentID INT AUTO_INCREMENT PRIMARY KEY,
    payment_num VARCHAR(100), -- e.g., Receipt number
    payment_date DATETIME NOT NULL,
    transaction_type VARCHAR(100), -- e.g., 'Membership', 'Product'
    amount DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(100) NOT NULL,
    memberID INT NOT NULL,
    FOREIGN KEY (memberID) REFERENCES Member(memberID)
);

-- 7. MemberFeedback Table
-- This table stores feedback from members about gym personnel.
CREATE TABLE MemberFeedback (
    feedbackID INT AUTO_INCREMENT PRIMARY KEY,
    comments TEXT,
    personnelID INT NOT NULL,
    memberID INT NOT NULL,
    FOREIGN KEY (personnelID) REFERENCES GymPersonnel(personnelID),
    FOREIGN KEY (memberID) REFERENCES Member(memberID)
);

-- 8. Purchase Table (Bridge Table)
-- This associative table handles the many-to-many relationship
-- between Members and Products (Member 'Purchases' Product).
CREATE TABLE Purchase (
    purchaseID INT AUTO_INCREMENT PRIMARY KEY,
    purchase_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    quantity INT NOT NULL,
    memberID INT NOT NULL,
    productID INT NOT NULL,
    FOREIGN KEY (memberID) REFERENCES Member(memberID),
    FOREIGN KEY (productID) REFERENCES Product(productID)
);

-- 9. Attendance Table (Bridge Table)
-- This associative table handles the many-to-many relationship
-- between Members and Classes (Member 'Attends' Class).
CREATE TABLE Attendance (
    attendanceID INT AUTO_INCREMENT PRIMARY KEY,
    attendance_datetime DATETIME DEFAULT CURRENT_TIMESTAMP,
    memberID INT NOT NULL,
    classID INT NOT NULL,
    FOREIGN KEY (memberID) REFERENCES Member(memberID),
    FOREIGN KEY (classID) REFERENCES Class(classID)
); 

-- 10. Equipment table
-- Self-contained inventory list for Gym Equipment
CREATE TABLE Equipment (
	equipmentID INT AUTO_INCREMENT PRIMARY KEY,
    equipment_name VARCHAR(100) NOT NULL,
    equipment_description VARCHAR(255) DEFAULT NULL,
    quantity INT NOT NULL CHECK (quantity >= 0),
    unit_price DECIMAL(10, 2) NOT NULL,
    vendor VARCHAR(100) NOT NULL,
    contact_no VARCHAR(20) NOT NULL,
    purchase_date DATETIME NOT NULL
);

-- Reset foreign key checks to 1
SET FOREIGN_KEY_CHECKS = 1;
