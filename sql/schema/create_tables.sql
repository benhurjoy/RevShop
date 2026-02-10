- Create database if not exists
    DROP DATABASE IF EXISTS revshop;
CREATE DATABASE revshop;
USE revshop;

-- Users table
CREATE TABLE users (
                       user_id INT PRIMARY KEY AUTO_INCREMENT,
                       email VARCHAR(100) UNIQUE NOT NULL,
                       password_hash VARCHAR(256) NOT NULL,
                       role ENUM('BUYER', 'SELLER') NOT NULL,
                       is_verified BOOLEAN DEFAULT FALSE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       INDEX idx_email (email),
                       INDEX idx_role (role)
);

-- OTP verification table
CREATE TABLE otp_verification (
                                  user_id INT PRIMARY KEY,
                                  otp VARCHAR(6) NOT NULL,
                                  expires_at TIMESTAMP NOT NULL,
                                  attempts INT DEFAULT 0,
                                  FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                                  INDEX idx_expires (expires_at)
);

-- Categories table
CREATE TABLE categories (
                            category_id INT PRIMARY KEY AUTO_INCREMENT,
                            name VARCHAR(50) NOT NULL UNIQUE,
                            INDEX idx_name (name)
);

-- Products table
CREATE TABLE products (
                          product_id INT PRIMARY KEY AUTO_INCREMENT,
                          seller_id INT NOT NULL,
                          category_id INT NOT NULL,
                          name VARCHAR(200) NOT NULL,
                          description TEXT,
                          mrp DECIMAL(10,2) NOT NULL,
                          discount_price DECIMAL(10,2),
                          stock INT NOT NULL DEFAULT 0,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          FOREIGN KEY (seller_id) REFERENCES users(user_id) ON DELETE CASCADE,
                          FOREIGN KEY (category_id) REFERENCES categories(category_id),
                          INDEX idx_seller (seller_id),
                          INDEX idx_category (category_id),
                          INDEX idx_name (name),
                          INDEX idx_price (discount_price)
);

-- Cart table
CREATE TABLE cart (
                      user_id INT NOT NULL,
                      product_id INT NOT NULL,
                      quantity INT NOT NULL DEFAULT 1,
                      added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      PRIMARY KEY (user_id, product_id),
                      FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                      FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
                      INDEX idx_user (user_id),
                      INDEX idx_product (product_id)
);

-- Orders table
CREATE TABLE orders (
                        order_id INT PRIMARY KEY AUTO_INCREMENT,
                        user_id INT NOT NULL,
                        total_amount DECIMAL(10,2) NOT NULL,
                        status ENUM('PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED') DEFAULT 'PENDING',
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_id) REFERENCES users(user_id),
                        INDEX idx_user (user_id),
                        INDEX idx_status (status),
                        INDEX idx_created (created_at)
);

-- Order items table
CREATE TABLE order_items (
                             order_id INT NOT NULL,
                             product_id INT NOT NULL,
                             quantity INT NOT NULL,
                             price DECIMAL(10,2) NOT NULL,
                             PRIMARY KEY (order_id, product_id),
                             FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
                             FOREIGN KEY (product_id) REFERENCES products(product_id),
                             INDEX idx_order (order_id),
                             INDEX idx_product (product_id)
);

-- Reviews table
CREATE TABLE reviews (
                         user_id INT NOT NULL,
                         product_id INT NOT NULL,
                         rating INT CHECK (rating BETWEEN 1 AND 5),
                         comment TEXT,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         PRIMARY KEY (user_id, product_id),
                         FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                         FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
                         INDEX idx_product (product_id),
                         INDEX idx_rating (rating)
);

-- Favorites table
CREATE TABLE favorites (
                           user_id INT NOT NULL,
                           product_id INT NOT NULL,
                           added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           PRIMARY KEY (user_id, product_id),
                           FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                           FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
                           INDEX idx_user (user_id),
                           INDEX idx_product (product_id)
);

-- Payments table
CREATE TABLE payments (
                          payment_id INT PRIMARY KEY AUTO_INCREMENT,
                          order_id INT UNIQUE NOT NULL,
                          method ENUM('CREDIT_CARD', 'DEBIT_CARD', 'UPI', 'NET_BANKING') NOT NULL,
                          amount DECIMAL(10,2) NOT NULL,
                          status ENUM('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED') DEFAULT 'PENDING',
                          transaction_id VARCHAR(100),
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
                          INDEX idx_status (status),
                          INDEX idx_transaction (transaction_id)
);

-- Notifications table
CREATE TABLE notifications (
                               notification_id INT PRIMARY KEY AUTO_INCREMENT,
                               user_id INT NOT NULL,
                               title VARCHAR(100) NOT NULL,
                               message VARCHAR(500) NOT NULL,
                               type ENUM('ORDER', 'PAYMENT', 'SYSTEM', 'PROMOTION') DEFAULT 'SYSTEM',
                               is_read BOOLEAN DEFAULT FALSE,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                               INDEX idx_user_read (user_id, is_read),
                               INDEX idx_created (created_at)
);
