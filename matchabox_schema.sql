-- ═══════════════════════════════════════════════════════════
--   Matcha Box Cafe System — MySQL Database Schema
--   Run this file in MySQL Workbench
--   Database: matchabox
-- ═══════════════════════════════════════════════════════════

-- Step 1: Create and select the database
CREATE DATABASE IF NOT EXISTS matchabox
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE matchabox;


-- ─────────────────────────────────────────────────────────────
-- TABLE 1: customers
-- Stores customer accounts and loyalty points
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS customers (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL,
    email           VARCHAR(150)    UNIQUE NOT NULL,
    phone           VARCHAR(20),
    password_hash   VARCHAR(255),
    loyalty_points  INT             DEFAULT 0,
    total_orders   INT             DEFAULT 0,
    total_spent     DECIMAL(10,2)   DEFAULT 0.00,
    is_active       BOOLEAN         DEFAULT TRUE,
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


-- ─────────────────────────────────────────────────────────────
-- TABLE 2: staff
-- Stores staff accounts, roles, and PIN for clock-in/out
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS staff (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL,
    email           VARCHAR(150)    UNIQUE NOT NULL,
    phone           VARCHAR(20),
    password_hash   VARCHAR(255)    NOT NULL,
    pin             VARCHAR(10)     NOT NULL,               -- 4-digit PIN for clock-in
    role            ENUM('BARISTA', 'CASHIER', 'KITCHEN', 'SUPERVISOR', 'MANAGER') DEFAULT 'BARISTA',
    is_active       BOOLEAN         DEFAULT TRUE,
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


-- ─────────────────────────────────────────────────────────────
-- TABLE 3: menu_categories
-- Groups menu items into categories
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS menu_categories (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(50)     NOT NULL,               -- e.g. Matcha, Coffee, Pastry
    slug            VARCHAR(50)     UNIQUE NOT NULL,        -- e.g. matcha, coffee
    display_order   INT             DEFAULT 0,
    is_active       BOOLEAN         DEFAULT TRUE
);


-- ─────────────────────────────────────────────────────────────
-- TABLE 4: menu_items
-- All items on the cafe menu
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS menu_items (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    category_id     BIGINT          NOT NULL,
    name            VARCHAR(100)    NOT NULL,
    description     VARCHAR(255),
    base_price      DECIMAL(8,2)    NOT NULL,
    image_url       VARCHAR(255),
    is_available    BOOLEAN         DEFAULT TRUE,
    is_popular      BOOLEAN         DEFAULT FALSE,
    is_featured     BOOLEAN         DEFAULT FALSE,
    calories        INT,
    display_order   INT             DEFAULT 0,
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (category_id) REFERENCES menu_categories(id)
);


-- ─────────────────────────────────────────────────────────────
-- TABLE 5: menu_item_options
-- Customisation options per item (size, milk, temperature)
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS menu_item_options (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    menu_item_id    BIGINT          NOT NULL,
    option_group    VARCHAR(50)     NOT NULL,               -- e.g. 'size', 'milk', 'temperature'
    option_name     VARCHAR(50)     NOT NULL,               -- e.g. 'Large', 'Oat milk', 'Iced'
    price_add       DECIMAL(6,2)    DEFAULT 0.00,           -- extra charge for this option
    is_default      BOOLEAN         DEFAULT FALSE,
    display_order   INT             DEFAULT 0,

    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE
);


-- ─────────────────────────────────────────────────────────────
-- TABLE 6: menu_addons
-- Add-ons available across menu items (e.g. cold foam, extra shot)
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS menu_addons (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL,               -- e.g. 'Cold foam topping'
    price           DECIMAL(6,2)    NOT NULL,
    is_available    BOOLEAN         DEFAULT TRUE
);


-- ─────────────────────────────────────────────────────────────
-- TABLE 7: orders
-- Each customer order (dine-in or takeaway)
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS orders (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    order_number    VARCHAR(20)     UNIQUE NOT NULL,        -- e.g. #0051
    customer_id     BIGINT,                                 -- NULL if walk-in customer
    staff_id        BIGINT,                                 -- staff who processed the order
    order_type      ENUM('DINE_IN', 'TAKEAWAY')    NOT NULL,
    table_number    VARCHAR(10),                            -- e.g. T-04 (for dine-in)
    status          ENUM('NEW', 'PREPARING', 'READY', 'COMPLETED', 'CANCELLED') DEFAULT 'NEW',
    subtotal        DECIMAL(10,2)   NOT NULL,
    service_charge  DECIMAL(10,2)   DEFAULT 0.00,
    discount        DECIMAL(10,2)   DEFAULT 0.00,
    loyalty_used   DECIMAL(10,2)   DEFAULT 0.00,
    total           DECIMAL(10,2)   NOT NULL,
    notes           VARCHAR(255),
    promo_code     VARCHAR(50),
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (staff_id)    REFERENCES staff(id)
);


-- ─────────────────────────────────────────────────────────────
-- TABLE 8: order_items
-- Individual items within each order
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS order_items (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT          NOT NULL,
    menu_item_id    BIGINT          NOT NULL,
    quantity        INT             NOT NULL DEFAULT 1,
    unit_price      DECIMAL(8,2)    NOT NULL,               -- price at time of order
    subtotal        DECIMAL(10,2)   NOT NULL,
    size            VARCHAR(20),
    milk_type       VARCHAR(30),
    temperature     VARCHAR(20),
    sweetness_level INT DEFAULT 50,
    special_request VARCHAR(300),
    customisations  JSON,                                   -- stores size, milk, temp, sweetness
    notes           VARCHAR(255),
    is_done         BOOLEAN         DEFAULT FALSE,          -- barista ticked this item

    FOREIGN KEY (order_id)      REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_item_id)  REFERENCES menu_items(id)
);


-- ─────────────────────────────────────────────────────────────
-- TABLE 9: order_item_addons
-- Add-ons selected for each order item
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS order_item_addons (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    order_item_id   BIGINT          NOT NULL,
    addon_id        BIGINT          NOT NULL,
    price           DECIMAL(6,2)    NOT NULL,

    FOREIGN KEY (order_item_id) REFERENCES order_items(id) ON DELETE CASCADE,
    FOREIGN KEY (addon_id)      REFERENCES menu_addons(id)
);


-- ─────────────────────────────────────────────────────────────
-- TABLE 10: payments
-- Payment record for each order
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS payments (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT          UNIQUE NOT NULL,
    method          ENUM('FPX', 'EWALLET', 'CARD', 'CASH') NOT NULL,
    provider        VARCHAR(50),                            -- e.g. 'Maybank', 'TnG', 'Visa'
    amount          DECIMAL(10,2)   NOT NULL,
    status          ENUM('PENDING', 'PAID', 'FAILED', 'REFUNDED') DEFAULT 'PENDING',
    transaction_ref VARCHAR(100),                           -- payment gateway reference
    paid_at         DATETIME,
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (order_id) REFERENCES orders(id)
);


-- ─────────────────────────────────────────────────────────────
-- TABLE 11: promo_codes
-- Discount codes for promotions
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS promo_codes (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    code            VARCHAR(30)     UNIQUE NOT NULL,        -- e.g. 'MATCHA10'
    discount_type   ENUM('percent', 'fixed') NOT NULL,
    discount_value  DECIMAL(8,2)    NOT NULL,               -- 10 = 10% or RM10
    min_order       DECIMAL(8,2)    DEFAULT 0.00,
    max_uses        INT             DEFAULT NULL,           -- NULL = unlimited
    used_count      INT             DEFAULT 0,
    is_active       BOOLEAN         DEFAULT TRUE,
    expires_at      DATETIME,
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP
);


-- ─────────────────────────────────────────────────────────────
-- TABLE 12: loyalty_transactions
-- Records every time points are earned or redeemed
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS loyalty_transactions (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    customer_id     BIGINT          NOT NULL,
    order_id        BIGINT,
    type            ENUM('earned', 'redeemed') NOT NULL,
    points          INT             NOT NULL,
    description     VARCHAR(255),
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (order_id)    REFERENCES orders(id)
);


-- ─────────────────────────────────────────────────────────────
-- TABLE 13: clock_records
-- Staff clock-in and clock-out attendance records
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS clock_records (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    staff_id        BIGINT          NOT NULL,
    clock_in        DATETIME        NOT NULL,
    clock_out       DATETIME,                               -- NULL if still clocked in
    break_start     DATETIME,
    break_end       DATETIME,
    hours_worked    DECIMAL(5,2),                           -- calculated on clock-out
    shift_date      DATE            NOT NULL,
    notes           VARCHAR(255),
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (staff_id) REFERENCES staff(id)
);


-- ─────────────────────────────────────────────────────────────
-- TABLE 14: inventory
-- Track stock levels for ingredients and supplies
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS inventory (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL,               -- e.g. 'Matcha powder'
    unit            VARCHAR(20)     NOT NULL,               -- e.g. 'kg', 'litre', 'pack'
    quantity        DECIMAL(10,2)   NOT NULL DEFAULT 0,
    low_stock_alert DECIMAL(10,2)   DEFAULT 0,              -- alert when below this amount
    cost_per_unit   DECIMAL(8,2),
    supplier        VARCHAR(100),
    updated_at      DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


-- ═══════════════════════════════════════════════════════════
--   SEED DATA — Insert sample records to get started
-- ═══════════════════════════════════════════════════════════

-- Categories
INSERT INTO menu_categories (name, slug, display_order) VALUES
('Matcha',  'matcha',  1),
('Coffee',  'coffee',  2),
('Pastry',  'pastry',  3),
('Food',    'food',    4);

-- Menu Items
INSERT INTO menu_items (category_id, name, description, base_price, is_available, is_popular, is_featured) VALUES
(1, 'Matcha Latte',       'Ceremonial grade matcha, oat milk',            14.90, TRUE, TRUE,  FALSE),
(1, 'Iced Matcha',        'Premium matcha, whole milk',                   13.90, TRUE, FALSE, FALSE),
(1, 'Matcha Cocoa',       'Matcha and dark chocolate blend',               15.90, TRUE, FALSE, FALSE),
(1, 'Iced Hojicha Latte', 'Roasted green tea, oat milk, brown sugar foam',15.90, TRUE, FALSE, TRUE),
(2, 'Flat White',         'Double espresso, silky microfoam',              12.90, TRUE, TRUE,  FALSE),
(2, 'Cold Brew',          '12-hour brew, served over ice',                 13.50, TRUE, FALSE, FALSE),
(3, 'Matcha Croissant',   'Butter pastry with matcha cream filling',        8.90, TRUE, FALSE, FALSE),
(4, 'Avocado Toast',      'Sourdough, smashed avocado, poached egg',       18.90, TRUE, FALSE, FALSE);

-- Menu Item Options (for Matcha Latte)
INSERT INTO menu_item_options (menu_item_id, option_group, option_name, price_add, is_default, display_order) VALUES
(1, 'size',        'Small (240ml)',   0.00, FALSE, 1),
(1, 'size',        'Regular (360ml)', 0.00, TRUE,  2),
(1, 'size',        'Large (480ml)',   2.00, FALSE, 3),
(1, 'milk',        'Oat milk',        0.00, TRUE,  1),
(1, 'milk',        'Whole milk',      0.00, FALSE, 2),
(1, 'milk',        'Soy milk',        0.00, FALSE, 3),
(1, 'milk',        'Almond milk',     1.00, FALSE, 4),
(1, 'milk',        'Coconut milk',    2.00, FALSE, 5),
(1, 'temperature', 'Hot',             0.00, TRUE,  1),
(1, 'temperature', 'Iced',            0.00, FALSE, 2),
(1, 'temperature', 'Blended',         0.00, FALSE, 3);

-- Add-ons
INSERT INTO menu_addons (name, price, is_available) VALUES
('Extra matcha shot',  1.50, TRUE),
('Brown sugar syrup',  1.50, TRUE),
('Cold foam topping',  2.00, TRUE),
('Vanilla bean',       1.00, TRUE),
('Oat milk upgrade',   1.00, TRUE);

-- Staff
INSERT INTO staff (name, email, phone, password_hash, pin, role) VALUES
('Nur Fatin',    'nurfatin@matchabox.my',   '012-3456789', '$2a$10$examplehash1', '1234', 'SUPERVISOR'),
('Ahmad Zaki',   'zaki@matchabox.my',       '011-2345678', '$2a$10$examplehash2', '2345', 'BARISTA'),
('Siti Liyana',  'liyana@matchabox.my',     '013-4567890', '$2a$10$examplehash3', '3456', 'BARISTA'),
('Rajan Harish', 'rajan@matchabox.my',      '019-5678901', '$2a$10$examplehash4', '4567', 'CASHIER'),
('Daniel Wong',  'daniel@matchabox.my',     '017-6789012', '$2a$10$examplehash5', '5678', 'KITCHEN');

-- Promo Codes
INSERT INTO promo_codes (code, discount_type, discount_value, min_order, is_active) VALUES
('MATCHA10', 'percent', 10.00, 20.00, TRUE),
('WELCOME',  'percent', 15.00, 10.00, TRUE),
('MB5',      'fixed',    5.00,  0.00, TRUE);

-- Sample Customer
INSERT INTO customers (name, email, phone, loyalty_points) VALUES
('Aisha Rahman', 'aisha@email.com', '016-7890123', 6);

-- Inventory
INSERT INTO inventory (name, unit, quantity, low_stock_alert, cost_per_unit, supplier) VALUES
('Matcha powder (ceremonial)', 'kg',    2.50, 0.50, 180.00, 'Ippodo Tea Co'),
('Oat milk',                   'litre', 15.0, 3.00,   8.50, 'Oatly MY'),
('Whole milk',                 'litre', 10.0, 2.00,   4.20, 'Farm Fresh'),
('Espresso beans',             'kg',    3.00, 0.50,  95.00, 'local roaster'),
('Butter croissant dough',     'pack',  24.0, 6.00,  12.00, 'local supplier'),
('Brown sugar syrup',          'litre',  2.0, 0.50,  15.00, 'local supplier'),
('Sourdough bread',            'loaf',   5.0, 1.00,  12.50, 'local bakery'),
('Avocado',                    'kg',    3.00, 1.00,  18.00, 'local market');
