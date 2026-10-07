-- ============================================================
--  Distributed Hotel Management System using Java RMI and JDBC
--  Database script: creates the database, tables and demo data
--
--  WARNING: this script DROPS and re-creates hotel_management,
--  so running it again resets everything to the demo data.
--
--  Run as root:   mysql -u root -p < database/hotel_management.sql
-- ============================================================

DROP DATABASE IF EXISTS hotel_management;
CREATE DATABASE hotel_management CHARACTER SET utf8mb4;
USE hotel_management;

-- ------------------------------------------------------------
-- users: login accounts for all three roles
-- password stores a SHA-256 hash, never the plain password
-- ------------------------------------------------------------
CREATE TABLE users (
    user_id    INT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   CHAR(64)     NOT NULL,
    role       ENUM('CUSTOMER', 'RECEPTIONIST', 'ADMIN') NOT NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------
-- customers: hotel guests
-- user_id is NULL for walk-in customers (they have no login)
-- ------------------------------------------------------------
CREATE TABLE customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT          NULL UNIQUE,
    full_name   VARCHAR(100) NOT NULL,
    phone       VARCHAR(15)  NOT NULL,
    email       VARCHAR(100),
    address     VARCHAR(255),
    id_proof    VARCHAR(50),
    CONSTRAINT fk_customer_user FOREIGN KEY (user_id) REFERENCES users (user_id),
    INDEX idx_customer_name (full_name),
    INDEX idx_customer_phone (phone)
);

-- ------------------------------------------------------------
-- rooms
-- ------------------------------------------------------------
CREATE TABLE rooms (
    room_id     INT AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(10)   NOT NULL UNIQUE,
    room_type   ENUM('SINGLE', 'DOUBLE', 'DELUXE', 'SUITE') NOT NULL,
    price       DECIMAL(10, 2) NOT NULL,
    capacity    INT           NOT NULL,
    status      ENUM('AVAILABLE', 'BOOKED', 'OCCUPIED', 'MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT chk_room_price CHECK (price > 0),
    CONSTRAINT chk_room_capacity CHECK (capacity > 0)
);

-- ------------------------------------------------------------
-- bookings
-- ------------------------------------------------------------
CREATE TABLE bookings (
    booking_id       INT AUTO_INCREMENT PRIMARY KEY,
    customer_id      INT  NOT NULL,
    room_id          INT  NOT NULL,
    check_in         DATE NOT NULL,
    check_out        DATE NOT NULL,
    number_of_guests INT  NOT NULL,
    booking_status   ENUM('CONFIRMED', 'CANCELLED', 'CHECKED_IN', 'CHECKED_OUT') NOT NULL DEFAULT 'CONFIRMED',
    booking_date     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_customer FOREIGN KEY (customer_id) REFERENCES customers (customer_id),
    CONSTRAINT fk_booking_room FOREIGN KEY (room_id) REFERENCES rooms (room_id),
    CONSTRAINT chk_booking_dates CHECK (check_out > check_in),
    CONSTRAINT chk_booking_guests CHECK (number_of_guests > 0),
    INDEX idx_booking_room_dates (room_id, check_in, check_out),
    INDEX idx_booking_status (booking_status)
);

-- ------------------------------------------------------------
-- services: extra services a guest can use during the stay
-- ------------------------------------------------------------
CREATE TABLE services (
    service_id   INT AUTO_INCREMENT PRIMARY KEY,
    service_name VARCHAR(50)    NOT NULL UNIQUE,
    price        DECIMAL(10, 2) NOT NULL
);

-- ------------------------------------------------------------
-- service_usage: which booking used which service, how many times
-- ------------------------------------------------------------
CREATE TABLE service_usage (
    usage_id   INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    service_id INT NOT NULL,
    quantity   INT NOT NULL DEFAULT 1,
    usage_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usage_booking FOREIGN KEY (booking_id) REFERENCES bookings (booking_id),
    CONSTRAINT fk_usage_service FOREIGN KEY (service_id) REFERENCES services (service_id),
    CONSTRAINT chk_usage_quantity CHECK (quantity > 0)
);

-- ------------------------------------------------------------
-- payments: one final payment per booking (made at check-out)
-- ------------------------------------------------------------
CREATE TABLE payments (
    payment_id      INT AUTO_INCREMENT PRIMARY KEY,
    booking_id      INT            NOT NULL UNIQUE,
    room_charges    DECIMAL(10, 2) NOT NULL,
    service_charges DECIMAL(10, 2) NOT NULL,
    gst_amount      DECIMAL(10, 2) NOT NULL,
    total_amount    DECIMAL(10, 2) NOT NULL,
    payment_method  ENUM('CASH', 'CARD', 'UPI') NOT NULL,
    payment_date    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_booking FOREIGN KEY (booking_id) REFERENCES bookings (booking_id)
);

-- ============================================================
--  DEMO DATA
-- ============================================================

-- Demo logins (plain passwords shown here only for the demo):
--   admin     / admin123       (ADMIN)
--   reception / reception123   (RECEPTIONIST)
--   rahul     / rahul123       (CUSTOMER)
--   priya     / priya123       (CUSTOMER)
INSERT INTO users (username, password, role) VALUES
    ('admin',     SHA2('admin123', 256),     'ADMIN'),
    ('reception', SHA2('reception123', 256), 'RECEPTIONIST'),
    ('rahul',     SHA2('rahul123', 256),     'CUSTOMER'),
    ('priya',     SHA2('priya123', 256),     'CUSTOMER');

-- Customers 1 and 2 have logins, customers 3 to 5 are walk-ins
INSERT INTO customers (user_id, full_name, phone, email, address, id_proof) VALUES
    (3,    'Rahul Sharma',   '9876543210', 'rahul.sharma@example.com', '12 MG Road, Bengaluru',      'AADHAAR-4521'),
    (4,    'Priya Nair',     '9123456780', 'priya.nair@example.com',   '45 Marine Drive, Kochi',     'PASSPORT-K7781'),
    (NULL, 'Amit Verma',     '9988776655', 'amit.verma@example.com',   '8 Park Street, Kolkata',     'DL-WB2019'),
    (NULL, 'Sneha Kulkarni', '9001122334', NULL,                       '21 FC Road, Pune',           'AADHAAR-9034'),
    (NULL, 'Arjun Mehta',    '9811223344', 'arjun.mehta@example.com',  '5 Connaught Place, Delhi',   'PAN-AKPM22');

INSERT INTO rooms (room_number, room_type, price, capacity, status) VALUES
    ('101', 'SINGLE', 1500.00, 1, 'AVAILABLE'),
    ('102', 'SINGLE', 1500.00, 1, 'AVAILABLE'),
    ('103', 'SINGLE', 1600.00, 1, 'AVAILABLE'),
    ('104', 'SINGLE', 1600.00, 1, 'MAINTENANCE'),
    ('201', 'DOUBLE', 2500.00, 2, 'AVAILABLE'),
    ('202', 'DOUBLE', 2500.00, 2, 'BOOKED'),
    ('203', 'DOUBLE', 2800.00, 2, 'AVAILABLE'),
    ('204', 'DOUBLE', 2800.00, 2, 'AVAILABLE'),
    ('301', 'DELUXE', 4000.00, 3, 'OCCUPIED'),
    ('302', 'DELUXE', 4200.00, 3, 'AVAILABLE'),
    ('401', 'SUITE',  7000.00, 4, 'BOOKED'),
    ('402', 'SUITE',  7500.00, 4, 'AVAILABLE');

INSERT INTO services (service_name, price) VALUES
    ('Room Service',   400.00),
    ('Laundry',        300.00),
    ('Breakfast',      250.00),
    ('Airport Pickup', 1200.00);

-- Dates are relative to today so the demo data always makes sense
INSERT INTO bookings (customer_id, room_id, check_in, check_out, number_of_guests, booking_status, booking_date) VALUES
    -- 1: finished stay (Rahul, room 201)
    (1, 5,  DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_SUB(CURDATE(), INTERVAL 7 DAY),  2, 'CHECKED_OUT', DATE_SUB(NOW(), INTERVAL 15 DAY)),
    -- 2: finished stay (Priya, room 101)
    (2, 1,  DATE_SUB(CURDATE(), INTERVAL 20 DAY), DATE_SUB(CURDATE(), INTERVAL 18 DAY), 1, 'CHECKED_OUT', DATE_SUB(NOW(), INTERVAL 25 DAY)),
    -- 3: guest currently staying (Amit, room 301)
    (3, 9,  DATE_SUB(CURDATE(), INTERVAL 1 DAY),  DATE_ADD(CURDATE(), INTERVAL 2 DAY),  3, 'CHECKED_IN',  DATE_SUB(NOW(), INTERVAL 4 DAY)),
    -- 4: arriving today, ready for check-in (Sneha, room 202)
    (4, 6,  CURDATE(),                            DATE_ADD(CURDATE(), INTERVAL 2 DAY),  2, 'CONFIRMED',   DATE_SUB(NOW(), INTERVAL 2 DAY)),
    -- 5: future booking (Priya, room 401)
    (2, 11, DATE_ADD(CURDATE(), INTERVAL 3 DAY),  DATE_ADD(CURDATE(), INTERVAL 6 DAY),  4, 'CONFIRMED',   DATE_SUB(NOW(), INTERVAL 1 DAY)),
    -- 6: cancelled booking (Rahul, room 102)
    (1, 2,  DATE_ADD(CURDATE(), INTERVAL 5 DAY),  DATE_ADD(CURDATE(), INTERVAL 7 DAY),  1, 'CANCELLED',   DATE_SUB(NOW(), INTERVAL 3 DAY));

INSERT INTO service_usage (booking_id, service_id, quantity, usage_date) VALUES
    (1, 3, 3, DATE_SUB(NOW(), INTERVAL 9 DAY)),   -- Breakfast x3
    (1, 2, 1, DATE_SUB(NOW(), INTERVAL 8 DAY)),   -- Laundry x1
    (3, 1, 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),   -- Room Service x1
    (3, 3, 3, NOW());                             -- Breakfast x3

-- GST is 12% of (room charges + service charges)
INSERT INTO payments (booking_id, room_charges, service_charges, gst_amount, total_amount, payment_method, payment_date) VALUES
    (1, 7500.00, 1050.00, 1026.00, 9576.00, 'CARD', DATE_SUB(NOW(), INTERVAL 7 DAY)),
    (2, 3000.00,    0.00,  360.00, 3360.00, 'UPI',  DATE_SUB(NOW(), INTERVAL 18 DAY));

-- ============================================================
--  Application account used by the RMI server (see database/db.properties)
-- ============================================================
CREATE USER IF NOT EXISTS 'hotel_user'@'localhost' IDENTIFIED BY 'hotel123';
GRANT ALL PRIVILEGES ON hotel_management.* TO 'hotel_user'@'localhost';
FLUSH PRIVILEGES;
