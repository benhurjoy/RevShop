-- Insert categories
INSERT INTO categories (name) VALUES
                                  ('Smartphones'),
                                  ('Laptops'),
                                  ('Tablets'),
                                  ('Wearables'),
                                  ('Audio'),
                                  ('Cameras'),
                                  ('Gaming'),
                                  ('Accessories'),
                                  ('Smart Home'),
                                  ('Components');

-- Insert sample seller (password: Seller@123)
INSERT INTO users (email, password_hash, role, is_verified) VALUES
    ('seller@revshop.com', 'c2FsdCRleGFtcGxlOjEyMzQ1Njc4OUFCQ0RFRkdISUpLTE1OT1BRUlNUVVZXWFla', 'SELLER', TRUE);

-- Insert sample products
INSERT INTO products (seller_id, category_id, name, description, mrp, discount_price, stock) VALUES
                                                                                                 (1, 1, 'iPhone 15 Pro', 'Latest Apple smartphone with A17 Pro chip', 134999.00, 129999.00, 50),
                                                                                                 (1, 1, 'Samsung Galaxy S24 Ultra', 'Flagship Android phone with S Pen', 129999.00, 119999.00, 30),
                                                                                                 (1, 2, 'MacBook Pro 16"', 'M3 Max chip, 32GB RAM, 1TB SSD', 249999.00, 229999.00, 20),
                                                                                                 (1, 2, 'Dell XPS 15', 'Intel Core i9, 32GB RAM, RTX 4070', 189999.00, 169999.00, 15),
                                                                                                 (1, 3, 'iPad Pro 12.9"', 'M2 chip, Liquid Retina XDR display', 119999.00, 109999.00, 25),
                                                                                                 (1, 4, 'Apple Watch Ultra 2', 'Rugged smartwatch for athletes', 89999.00, 84999.00, 40),
                                                                                                 (1, 5, 'Sony WH-1000XM5', 'Industry-leading noise cancellation', 29999.00, 26999.00, 60),
                                                                                                 (1, 6, 'Sony Alpha 7 IV', 'Full-frame mirrorless camera', 199999.00, 189999.00, 10),
                                                                                                 (1, 7, 'PlayStation 5', 'Gaming console with 4K Blu-ray', 54999.00, 49999.00, 35),
                                                                                                 (1, 8, 'Samsung T7 SSD 2TB', 'Portable SSD with 1050MB/s speed', 17999.00, 15999.00, 100);

-- Insert sample buyer (password: Buyer@123) - This will be registered via application
-- Note: Password will be properly hashed during registration