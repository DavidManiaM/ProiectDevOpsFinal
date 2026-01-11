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
-- BCrypt hash for 'demo123'
INSERT INTO users (username, email, password, is_active, created_at, updated_at) VALUES
    ('demo', 'demo@example.com', '$2b$10$zvOotG4X.hRaeOv9aTXOA..K3Ofhsn/X5AoEcR0nn2VKrNx8C.pWy', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Assign ROLE_USER to demo user
INSERT INTO user_roles (user_id, role) VALUES
    ((SELECT id FROM users WHERE username = 'demo'), 'ROLE_USER');

-- Insert admin user (password: admin)
-- BCrypt hash for 'admin'
INSERT INTO users (username, email, password, is_active, created_at, updated_at) VALUES
    ('admin', 'admin@example.com', '$2b$10$gWXEtye.oSRu0Lp.7SDsHuqJqTO.c22B4YJXSsTMlrvT4qcDghIOu', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Assign ROLE_ADMIN and ROLE_USER to admin
INSERT INTO user_roles (user_id, role) VALUES
                                           ((SELECT id FROM users WHERE username = 'admin'), 'ROLE_ADMIN'),
                                           ((SELECT id FROM users WHERE username = 'admin'), 'ROLE_USER');

-- Insert test user (password: test123)
-- BCrypt hash for 'test123'
INSERT INTO users (username, email, password, is_active, created_at, updated_at) VALUES
    ('test', 'test@example.com', '$2b$10$VKKJUbE5q3foqu25QGter.ydfiPl.UBxAxViWP.Ji7bz5i4jn0oJK', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Assign ROLE_USER to test user
INSERT INTO user_roles (user_id, role) VALUES
    ((SELECT id FROM users WHERE username = 'test'), 'ROLE_USER');
