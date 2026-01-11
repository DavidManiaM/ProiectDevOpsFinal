import bcrypt

# Generate BCrypt hashes for the three passwords
passwords = {
    'admin': 'admin',
    'demo': 'demo123',
    'test': 'test123'
}

for username, password in passwords.items():
    # Generate hash with salt rounds = 10 (BCrypt default)
    hashed = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt(rounds=10))
    print(f"{username}: {hashed.decode('utf-8')}")
