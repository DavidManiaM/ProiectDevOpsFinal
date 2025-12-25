-- Users table
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       is_active BOOLEAN DEFAULT TRUE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User roles table
CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            role VARCHAR(50) NOT NULL,
                            PRIMARY KEY (user_id, role)
);

-- Symbols table
CREATE TABLE symbols (
                         id BIGSERIAL PRIMARY KEY,
                         ticker VARCHAR(20) NOT NULL UNIQUE,
                         name VARCHAR(100) NOT NULL,
                         type VARCHAR(20) NOT NULL,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Prices table
CREATE TABLE prices (
                        id BIGSERIAL PRIMARY KEY,
                        symbol_id BIGINT NOT NULL REFERENCES symbols(id) ON DELETE CASCADE,
                        price DECIMAL(20, 8) NOT NULL,
                        volume DECIMAL(20, 8),
                        moving_average_5 DECIMAL(20, 8),
                        moving_average_20 DECIMAL(20, 8),
                        percent_change DECIMAL(10, 4),
                        timestamp TIMESTAMP NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for faster price lookups
CREATE INDEX idx_prices_symbol_timestamp ON prices(symbol_id, timestamp DESC);
CREATE INDEX idx_prices_timestamp ON prices(timestamp DESC);

-- Alerts table
CREATE TABLE alerts (
                        id BIGSERIAL PRIMARY KEY,
                        symbol_id BIGINT NOT NULL REFERENCES symbols(id) ON DELETE CASCADE,
                        alert_type VARCHAR(50) NOT NULL,
                        message VARCHAR(500) NOT NULL,
                        trigger_value DECIMAL(20, 8),
                        threshold_value DECIMAL(10, 4),
                        is_read BOOLEAN DEFAULT FALSE,
                        timestamp TIMESTAMP NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for alert lookups
CREATE INDEX idx_alerts_symbol ON alerts(symbol_id);
CREATE INDEX idx_alerts_timestamp ON alerts(timestamp DESC);
CREATE INDEX idx_alerts_unread ON alerts(is_read) WHERE is_read = FALSE;

