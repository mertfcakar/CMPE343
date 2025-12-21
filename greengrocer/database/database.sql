-- ============================================
-- Group12 GreenGrocer Database
-- CMPE 343 - Fall 2025-2026 - Project 3
-- ============================================

-- Drop database if exists and create fresh
DROP DATABASE IF EXISTS greengrocer;
CREATE DATABASE greengrocer CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE greengrocer;

-- ============================================
-- Table: users
-- Stores all user information (customers, carriers, owners)
-- ============================================
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('customer', 'carrier', 'owner') NOT NULL,
    address TEXT,
    contact_details VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- Table: products
-- Stores vegetable and fruit information
-- ============================================
CREATE TABLE products (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    type ENUM('vegetable', 'fruit') NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    stock DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    threshold DECIMAL(10, 2) NOT NULL DEFAULT 5.00,
    image LONGBLOB,
    image_type VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_type (type),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- Table: orders
-- Stores order information
-- ============================================
CREATE TABLE orders (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    carrier_id INT,
    order_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delivery_time TIMESTAMP NULL,
    requested_delivery_date TIMESTAMP NOT NULL,
    total_cost DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    vat_amount DECIMAL(10, 2) NOT NULL,
    discount_amount DECIMAL(10, 2) DEFAULT 0.00,
    status ENUM('pending', 'assigned', 'in_delivery', 'completed', 'cancelled') DEFAULT 'pending',
    invoice LONGTEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (carrier_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_user (user_id),
    INDEX idx_carrier (carrier_id),
    INDEX idx_status (status),
    INDEX idx_order_time (order_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- Table: order_items
-- Stores individual items in each order
-- ============================================
CREATE TABLE order_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    quantity DECIMAL(10, 2) NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,
    INDEX idx_order (order_id),
    INDEX idx_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- Table: coupons
-- Stores discount coupons
-- ============================================
CREATE TABLE coupons (
    id INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) UNIQUE NOT NULL,
    discount_percentage DECIMAL(5, 2) NOT NULL,
    min_purchase_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    max_discount_amount DECIMAL(10, 2),
    valid_from TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    valid_until TIMESTAMP NULL,
    is_active BOOLEAN DEFAULT TRUE,
    usage_limit INT DEFAULT NULL,
    times_used INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_code (code),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- Table: user_coupons
-- Links coupons to users
-- ============================================
CREATE TABLE user_coupons (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    coupon_id INT NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    used_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (coupon_id) REFERENCES coupons(id) ON DELETE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_coupon (coupon_id),
    INDEX idx_used (used)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- Table: carrier_ratings
-- Stores customer ratings for carriers
-- ============================================
CREATE TABLE carrier_ratings (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    carrier_id INT NOT NULL,
    customer_id INT NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (carrier_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_carrier (carrier_id),
    INDEX idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- Table: messages
-- Stores customer-owner communication
-- ============================================
CREATE TABLE messages (
    id INT PRIMARY KEY AUTO_INCREMENT,
    sender_id INT NOT NULL,
    receiver_id INT,
    subject VARCHAR(200),
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    reply_to INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reply_to) REFERENCES messages(id) ON DELETE SET NULL,
    INDEX idx_sender (sender_id),
    INDEX idx_receiver (receiver_id),
    INDEX idx_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- Table: loyalty_settings
-- Stores loyalty program configuration
-- ============================================
CREATE TABLE loyalty_settings (
    id INT PRIMARY KEY AUTO_INCREMENT,
    min_orders INT NOT NULL DEFAULT 5,
    discount_percentage DECIMAL(5, 2) NOT NULL DEFAULT 5.00,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- INSERT DEFAULT DATA
-- ============================================

-- Insert default users (password is same as username for testing)
INSERT INTO users (username, password, role, address, contact_details) VALUES
('cust', 'cust', 'customer', '123 Customer Street, Istanbul', 'cust@email.com'),
('carr', 'carr', 'carrier', '456 Carrier Avenue, Istanbul', 'carr@email.com'),
('own', 'own', 'owner', '789 Owner Boulevard, Istanbul', 'own@email.com'),
('customer1', 'customer1', 'customer', 'Kadıköy, Istanbul', 'customer1@email.com'),
('customer2', 'customer2', 'customer', 'Beşiktaş, Istanbul', 'customer2@email.com'),
('customer3', 'customer3', 'customer', 'Üsküdar, Istanbul', 'customer3@email.com'),
('carrier1', 'carrier1', 'carrier', 'Şişli, Istanbul', 'carrier1@email.com'),
('carrier2', 'carrier2', 'carrier', 'Bakırköy, Istanbul', 'carrier2@email.com');

-- Insert vegetables (12 items minimum)
INSERT INTO products (name, type, price, stock, threshold) VALUES
('Tomatoes', 'vegetable', 15.50, 50.00, 5.00),
('Potatoes', 'vegetable', 8.75, 100.00, 10.00),
('Onions', 'vegetable', 12.00, 75.00, 8.00),
('Carrots', 'vegetable', 10.25, 60.00, 5.00),
('Cucumbers', 'vegetable', 18.00, 40.00, 5.00),
('Bell Peppers', 'vegetable', 22.50, 35.00, 5.00),
('Eggplant', 'vegetable', 16.75, 45.00, 5.00),
('Zucchini', 'vegetable', 14.00, 55.00, 5.00),
('Lettuce', 'vegetable', 9.50, 30.00, 3.00),
('Spinach', 'vegetable', 11.25, 25.00, 3.00),
('Broccoli', 'vegetable', 20.00, 20.00, 3.00),
('Cauliflower', 'vegetable', 19.50, 22.00, 3.00),
('Green Beans', 'vegetable', 25.00, 18.00, 3.00),
('Cabbage', 'vegetable', 7.50, 40.00, 5.00),
('Leeks', 'vegetable', 13.75, 28.00, 3.00);

-- Insert fruits (12 items minimum)
INSERT INTO products (name, type, price, stock, threshold) VALUES
('Apples', 'fruit', 18.00, 80.00, 10.00),
('Oranges', 'fruit', 16.50, 70.00, 10.00),
('Bananas', 'fruit', 12.75, 90.00, 15.00),
('Grapes', 'fruit', 28.00, 45.00, 5.00),
('Strawberries', 'fruit', 35.00, 25.00, 3.00),
('Watermelon', 'fruit', 8.50, 30.00, 5.00),
('Melon', 'fruit', 9.75, 35.00, 5.00),
('Peaches', 'fruit', 22.50, 20.00, 3.00),
('Pears', 'fruit', 19.00, 40.00, 5.00),
('Plums', 'fruit', 21.00, 30.00, 5.00),
('Kiwi', 'fruit', 32.00, 15.00, 3.00),
('Mango', 'fruit', 38.50, 12.00, 2.00),
('Cherries', 'fruit', 45.00, 10.00, 2.00),
('Pomegranate', 'fruit', 24.00, 25.00, 3.00),
('Tangerines', 'fruit', 14.50, 50.00, 8.00);

-- Insert sample coupons
INSERT INTO coupons (code, discount_percentage, min_purchase_amount, max_discount_amount, valid_until, is_active) VALUES
('WELCOME10', 10.00, 50.00, 20.00, DATE_ADD(NOW(), INTERVAL 30 DAY), TRUE),
('SUMMER15', 15.00, 100.00, 50.00, DATE_ADD(NOW(), INTERVAL 60 DAY), TRUE),
('BULK20', 20.00, 200.00, 100.00, DATE_ADD(NOW(), INTERVAL 90 DAY), TRUE);

-- Insert loyalty settings
INSERT INTO loyalty_settings (min_orders, discount_percentage, description, is_active) VALUES
(5, 5.00, 'Customer receives 5% discount after 5 completed orders', TRUE);

-- Insert sample completed orders for customer statistics
INSERT INTO orders (user_id, carrier_id, order_time, delivery_time, requested_delivery_date, subtotal, vat_amount, discount_amount, total_cost, status) VALUES
(1, 2, DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 29 DAY), DATE_SUB(NOW(), INTERVAL 29 DAY), 150.00, 27.00, 0.00, 177.00, 'completed'),
(1, 2, DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 24 DAY), DATE_SUB(NOW(), INTERVAL 24 DAY), 200.00, 36.00, 0.00, 236.00, 'completed'),
(1, 7, DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 19 DAY), DATE_SUB(NOW(), INTERVAL 19 DAY), 175.00, 31.50, 0.00, 206.50, 'completed'),
(4, 2, DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY), 125.00, 22.50, 0.00, 147.50, 'completed'),
(4, 7, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY), 180.00, 32.40, 0.00, 212.40, 'completed'),
(5, 2, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY), 95.00, 17.10, 0.00, 112.10, 'completed'),
(6, 7, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 220.00, 39.60, 0.00, 259.60, 'completed');

-- Insert order items for the completed orders
INSERT INTO order_items (order_id, product_id, product_name, quantity, unit_price, total_price) VALUES
-- Order 1
(1, 1, 'Tomatoes', 2.00, 15.50, 31.00),
(1, 16, 'Apples', 3.50, 18.00, 63.00),
(1, 3, 'Onions', 2.00, 12.00, 24.00),
(1, 18, 'Bananas', 2.50, 12.75, 31.88),
-- Order 2
(2, 6, 'Bell Peppers', 3.00, 22.50, 67.50),
(2, 19, 'Grapes', 2.00, 28.00, 56.00),
(2, 8, 'Zucchini', 2.50, 14.00, 35.00),
(2, 21, 'Watermelon', 5.00, 8.50, 42.50),
-- Order 3
(3, 13, 'Green Beans', 2.00, 25.00, 50.00),
(3, 20, 'Strawberries', 1.50, 35.00, 52.50),
(3, 4, 'Carrots', 3.00, 10.25, 30.75),
(3, 23, 'Peaches', 2.00, 22.50, 45.00),
-- Order 4
(4, 2, 'Potatoes', 5.00, 8.75, 43.75),
(4, 17, 'Oranges', 3.00, 16.50, 49.50),
(4, 9, 'Lettuce', 2.00, 9.50, 19.00),
(4, 14, 'Cabbage', 1.50, 7.50, 11.25),
-- Order 5
(5, 7, 'Eggplant', 2.50, 16.75, 41.88),
(5, 25, 'Plums', 2.00, 21.00, 42.00),
(5, 11, 'Broccoli', 2.00, 20.00, 40.00),
(5, 29, 'Pomegranate', 2.00, 24.00, 48.00),
-- Order 6
(6, 5, 'Cucumbers', 2.00, 18.00, 36.00),
(6, 24, 'Pears', 1.50, 19.00, 28.50),
(6, 15, 'Leeks', 1.00, 13.75, 13.75),
(6, 30, 'Tangerines', 1.00, 14.50, 14.50),
-- Order 7
(7, 26, 'Kiwi', 2.00, 32.00, 64.00),
(7, 27, 'Mango', 1.50, 38.50, 57.75),
(7, 12, 'Cauliflower', 2.00, 19.50, 39.00),
(7, 10, 'Spinach', 3.00, 11.25, 33.75),
(7, 28, 'Cherries', 0.50, 45.00, 22.50);

-- Insert carrier ratings
INSERT INTO carrier_ratings (order_id, carrier_id, customer_id, rating, comment) VALUES
(1, 2, 1, 5, 'Excellent service, very fast delivery!'),
(2, 2, 1, 4, 'Good service, products were fresh'),
(3, 7, 1, 5, 'Professional and friendly carrier'),
(4, 2, 4, 4, 'Delivery was on time'),
(5, 7, 4, 5, 'Amazing service!'),
(6, 2, 5, 3, 'Decent service, could be better'),
(7, 7, 6, 5, 'Very careful with the products');

-- Insert sample messages
INSERT INTO messages (sender_id, receiver_id, subject, message, is_read) VALUES
(1, 3, 'Product Quality Issue', 'Some of the tomatoes in my last order were not fresh. Can you please check?', TRUE),
(3, 1, 'RE: Product Quality Issue', 'We apologize for the inconvenience. We will send you a replacement order with fresh tomatoes.', TRUE),
(4, 3, 'Delivery Schedule', 'Can I get my order delivered on weekends?', FALSE),
(5, 3, 'Product Request', 'Do you plan to add organic vegetables to your inventory?', FALSE);

-- Insert pending/available orders for carriers
INSERT INTO orders (user_id, order_time, requested_delivery_date, subtotal, vat_amount, discount_amount, total_cost, status) VALUES
(1, NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), 85.00, 15.30, 0.00, 100.30, 'pending'),
(4, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_ADD(NOW(), INTERVAL 2 DAY), 120.00, 21.60, 10.00, 131.60, 'pending'),
(5, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 1 DAY), 65.00, 11.70, 0.00, 76.70, 'pending');

-- Insert items for pending orders
INSERT INTO order_items (order_id, product_id, product_name, quantity, unit_price, total_price) VALUES
-- Pending Order 1
(8, 1, 'Tomatoes', 1.50, 15.50, 23.25),
(8, 16, 'Apples', 2.00, 18.00, 36.00),
(8, 3, 'Onions', 2.00, 12.00, 24.00),
-- Pending Order 2
(9, 19, 'Grapes', 2.00, 28.00, 56.00),
(9, 7, 'Eggplant', 1.50, 16.75, 25.13),
(9, 24, 'Pears', 2.00, 19.00, 38.00),
-- Pending Order 3
(10, 18, 'Bananas', 2.00, 12.75, 25.50),
(10, 4, 'Carrots', 1.50, 10.25, 15.38),
(10, 9, 'Lettuce', 2.00, 9.50, 19.00);

-- ============================================
-- VIEWS FOR REPORTING
-- ============================================

-- View: Product inventory status
CREATE VIEW v_inventory_status AS
SELECT 
    p.id,
    p.name,
    p.type,
    p.stock,
    p.threshold,
    p.price,
    CASE 
        WHEN p.stock <= p.threshold THEN p.price * 2
        ELSE p.price
    END AS current_price,
    CASE 
        WHEN p.stock <= p.threshold THEN 'Low Stock - Price Doubled'
        WHEN p.stock <= p.threshold * 2 THEN 'Low Stock Warning'
        ELSE 'In Stock'
    END AS stock_status
FROM products p
WHERE p.is_active = TRUE
ORDER BY p.type, p.name;

-- View: Carrier performance
CREATE VIEW v_carrier_performance AS
SELECT 
    u.id,
    u.username,
    COUNT(DISTINCT o.id) AS total_deliveries,
    COALESCE(AVG(cr.rating), 0) AS avg_rating,
    COUNT(DISTINCT cr.id) AS total_ratings
FROM users u
LEFT JOIN orders o ON u.id = o.carrier_id AND o.status = 'completed'
LEFT JOIN carrier_ratings cr ON u.id = cr.carrier_id
WHERE u.role = 'carrier'
GROUP BY u.id, u.username
ORDER BY avg_rating DESC, total_deliveries DESC;

-- View: Customer order statistics
CREATE VIEW v_customer_statistics AS
SELECT 
    u.id,
    u.username,
    COUNT(DISTINCT o.id) AS total_orders,
    SUM(CASE WHEN o.status = 'completed' THEN 1 ELSE 0 END) AS completed_orders,
    COALESCE(SUM(CASE WHEN o.status = 'completed' THEN o.total_cost ELSE 0 END), 0) AS total_spent,
    COALESCE(AVG(CASE WHEN o.status = 'completed' THEN o.total_cost END), 0) AS avg_order_value
FROM users u
LEFT JOIN orders o ON u.id = o.user_id
WHERE u.role = 'customer'
GROUP BY u.id, u.username
ORDER BY total_spent DESC;

-- View: Daily sales report
CREATE VIEW v_daily_sales AS
SELECT 
    DATE(o.order_time) AS order_date,
    COUNT(DISTINCT o.id) AS total_orders,
    SUM(o.subtotal) AS total_sales,
    SUM(o.vat_amount) AS total_vat,
    SUM(o.discount_amount) AS total_discounts,
    SUM(o.total_cost) AS total_revenue
FROM orders o
WHERE o.status IN ('completed', 'in_delivery')
GROUP BY DATE(o.order_time)
ORDER BY order_date DESC;

-- View: Popular products
CREATE VIEW v_popular_products AS
SELECT 
    p.id,
    p.name,
    p.type,
    COUNT(oi.id) AS times_ordered,
    SUM(oi.quantity) AS total_quantity_sold,
    SUM(oi.total_price) AS total_revenue
FROM products p
INNER JOIN order_items oi ON p.id = oi.product_id
INNER JOIN orders o ON oi.order_id = o.id
WHERE o.status = 'completed'
GROUP BY p.id, p.name, p.type
ORDER BY total_quantity_sold DESC;

-- ============================================
-- STORED PROCEDURES
-- ============================================

DELIMITER //

-- Procedure: Check if customer qualifies for loyalty discount
CREATE PROCEDURE sp_check_loyalty_discount(
    IN p_user_id INT,
    OUT p_qualifies BOOLEAN,
    OUT p_discount_percentage DECIMAL(5,2)
)
BEGIN
    DECLARE completed_orders INT;
    DECLARE min_orders_required INT;
    DECLARE discount_pct DECIMAL(5,2);
    
    -- Get loyalty settings
    SELECT min_orders, discount_percentage 
    INTO min_orders_required, discount_pct
    FROM loyalty_settings 
    WHERE is_active = TRUE 
    LIMIT 1;
    
    -- Count completed orders for user
    SELECT COUNT(*) INTO completed_orders
    FROM orders
    WHERE user_id = p_user_id AND status = 'completed';
    
    -- Check if qualifies
    IF completed_orders >= min_orders_required THEN
        SET p_qualifies = TRUE;
        SET p_discount_percentage = discount_pct;
    ELSE
        SET p_qualifies = FALSE;
        SET p_discount_percentage = 0;
    END IF;
END //

-- Procedure: Get available carriers
CREATE PROCEDURE sp_get_available_carriers()
BEGIN
    SELECT 
        u.id,
        u.username,
        u.contact_details,
        COUNT(o.id) AS current_deliveries,
        COALESCE(AVG(cr.rating), 0) AS avg_rating
    FROM users u
    LEFT JOIN orders o ON u.id = o.carrier_id AND o.status IN ('assigned', 'in_delivery')
    LEFT JOIN carrier_ratings cr ON u.id = cr.carrier_id
    WHERE u.role = 'carrier'
    GROUP BY u.id, u.username, u.contact_details
    ORDER BY current_deliveries ASC, avg_rating DESC;
END //

DELIMITER ;

-- ============================================
-- INDEXES FOR PERFORMANCE
-- ============================================

-- Additional composite indexes for common queries
CREATE INDEX idx_orders_user_status ON orders(user_id, status);
CREATE INDEX idx_orders_carrier_status ON orders(carrier_id, status);
CREATE INDEX idx_products_type_active ON products(type, is_active);
CREATE INDEX idx_order_items_order_product ON order_items(order_id, product_id);

-- ============================================
-- GRANT PRIVILEGES TO myuser
-- ============================================

-- Create user if not exists
CREATE USER IF NOT EXISTS 'myuser'@'localhost' IDENTIFIED BY '1234';

-- Grant all privileges on greengrocer database
GRANT ALL PRIVILEGES ON greengrocer.* TO 'myuser'@'localhost';
FLUSH PRIVILEGES;

-- ============================================
-- DATABASE SETUP COMPLETE
-- ============================================

SELECT 'Database setup completed successfully!' AS Status;
SELECT COUNT(*) AS 'Total Products' FROM products;
SELECT COUNT(*) AS 'Total Users' FROM users;
SELECT COUNT(*) AS 'Total Orders' FROM orders;
SELECT 'Database is ready for use!' AS Message;