-- Create the database if it doesn't exist
CREATE DATABASE IF NOT EXISTS price_comparison_db;
USE price_comparison_db;

-- Create the table to store product prices and details
CREATE TABLE IF NOT EXISTS products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    url VARCHAR(500) NOT NULL,
    fetched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Sample insert query
INSERT INTO products (product_name, price, url) 
VALUES ('Example Product', 99.99, 'http://example.com/product1'); -- Removed 'site'

-- Sample select query
SELECT * FROM products WHERE product_name = 'Example Product';
