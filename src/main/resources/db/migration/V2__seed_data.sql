-- Insert demo symbols (stocks and crypto)
INSERT INTO symbols (ticker, name, type, created_at, updated_at) VALUES
                                                                     ('AAPL', 'Apple Inc.', 'STOCK', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                     ('GOOGL', 'Alphabet Inc.', 'STOCK', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                     ('MSFT', 'Microsoft Corporation', 'STOCK', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                     ('AMZN', 'Amazon.com Inc.', 'STOCK', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                     ('TSLA', 'Tesla Inc.', 'STOCK', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                     ('BTC', 'Bitcoin', 'CRYPTO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                     ('ETH', 'Ethereum', 'CRYPTO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                     ('SOL', 'Solana', 'CRYPTO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert demo user (password: demo123)
-- Password hash for 'demo123' using BCrypt
INSERT INTO users (username, email, password, is_active, created_at, updated_at) VALUES
    ('demo', 'demo@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqVNo9VrIPbQdBHhD5P.gWvT1.Ld6', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Assign ROLE_USER to demo user
INSERT INTO user_roles (user_id, role) VALUES
    ((SELECT id FROM users WHERE username = 'demo'), 'ROLE_USER');

-- Insert admin user (password: admin123)
INSERT INTO users (username, email, password, is_active, created_at, updated_at) VALUES
    ('admin', 'admin@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqaFS', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Assign ROLE_ADMIN and ROLE_USER to admin
INSERT INTO user_roles (user_id, role) VALUES
                                           ((SELECT id FROM users WHERE username = 'admin'), 'ROLE_ADMIN'),
                                           ((SELECT id FROM users WHERE username = 'admin'), 'ROLE_USER');
