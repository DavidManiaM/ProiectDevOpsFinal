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
    ('demo', 'demo@example.com', '$2a$10$rJHQPE3.K6Iu9TK4OuoJ5.eYbWGH7PmE1.XD3S7SoMVs8f/DVLKaK', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Assign ROLE_USER to demo user
INSERT INTO user_roles (user_id, role) VALUES
    ((SELECT id FROM users WHERE username = 'demo'), 'ROLE_USER');

-- Insert admin user (password: admin123)
INSERT INTO users (username, email, password, is_active, created_at, updated_at) VALUES
    ('admin', 'admin@example.com', '$2a$10$DowR5a3Q5r3q3PN3zN7gHe0FvGM8hC0l3q5e9Y5Qe1v3R6d8p0nKa', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Assign ROLE_ADMIN and ROLE_USER to admin
INSERT INTO user_roles (user_id, role) VALUES
                                           ((SELECT id FROM users WHERE username = 'admin'), 'ROLE_ADMIN'),
                                           ((SELECT id FROM users WHERE username = 'admin'), 'ROLE_USER');
