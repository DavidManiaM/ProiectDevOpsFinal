-- Update passwords for existing users with correct BCrypt hashes
UPDATE users SET password = '$2b$10$gWXEtye.oSRu0Lp.7SDsHuqJqTO.c22B4YJXSsTMlrvT4qcDghIOu' WHERE username = 'admin';
UPDATE users SET password = '$2b$10$zvOotG4X.hRaeOv9aTXOA..K3Ofhsn/X5AoEcR0nn2VKrNx8C.pWy' WHERE username = 'demo';

-- Insert test user if not exists
INSERT INTO users (username, email, password, is_active, created_at, updated_at) 
VALUES ('test', 'test@example.com', '$2b$10$VKKJUbE5q3foqu25QGter.ydfiPl.UBxAxViWP.Ji7bz5i4jn0oJK', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO UPDATE SET password = '$2b$10$VKKJUbE5q3foqu25QGter.ydfiPl.UBxAxViWP.Ji7bz5i4jn0oJK';

-- Insert test user role if not exists
INSERT INTO user_roles (user_id, role) 
SELECT id, 'ROLE_USER' FROM users WHERE username = 'test'
ON CONFLICT DO NOTHING;