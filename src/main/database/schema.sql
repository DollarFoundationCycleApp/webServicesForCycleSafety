-- Cycle Safety database schema
-- Incident information and incident images are stored in two separate tables
-- linked by device_event_id.

CREATE DATABASE IF NOT EXISTS cyclenessafety_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE cyclenessafety_db;

-- Incident Information Table
CREATE TABLE IF NOT EXISTS incidents (
    incident_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_event_id INT UNSIGNED UNIQUE NOT NULL,  -- Links to incident images
    user_id VARCHAR(255) NOT NULL,                  -- ID of the reporting user
    timestamp_ms BIGINT NOT NULL,                   -- Incident timestamp (UTC)
    latitude DECIMAL(10,7),                         -- GPS coordinates from mobile app
    longitude DECIMAL(10,7),
    accuracy_meters DECIMAL(8,2),                   -- Accuracy in meters
    distance_cm INT UNSIGNED,                       -- Distance to object from embedded sensor
    time_offset_ms INT UNSIGNED,                    -- Sensor offset time in ms
    is_anonymized TINYINT(1) DEFAULT 0 NOT NULL,    -- Flag denoting sensitive info stripping
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_device_event_id (device_event_id),
    INDEX idx_user_id (user_id),
    INDEX idx_timestamp_ms (timestamp_ms)
);

-- Incident Image Table (stores binary image data separately)
CREATE TABLE IF NOT EXISTS incident_photos (
    photo_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_event_id INT UNSIGNED NOT NULL,           -- Foreign key linking to incidents table
    image_index INT UNSIGNED NOT NULL,               -- Zero-based index within event
    format VARCHAR(50) NOT NULL DEFAULT 'JPEG',      -- e.g., "JPEG", "PNG"
    image_data MEDIUMBLOB NOT NULL,                  -- Binary image payload
    contains_sensitive_data TINYINT(1) DEFAULT 0 NOT NULL, -- Flagged if pending anonymization
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (device_event_id) REFERENCES incidents(device_event_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_device_event_id_image_index (device_event_id, image_index),
    INDEX idx_format (format),
    INDEX idx_sensitive_data (contains_sensitive_data)
);

-- Offender Vehicle Table (for repeat offender analytics)
CREATE TABLE IF NOT EXISTS offender_vehicles (
    vehicle_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_event_id INT UNSIGNED UNIQUE NOT NULL,    -- Links to incident that reported this vehicle
    license_plate VARCHAR(50),                        -- Raw or normalized license plate string
    state_or_region VARCHAR(100),                     -- Vehicle jurisdiction
    vehicle_make VARCHAR(100),                       -- Vehicle make
    vehicle_model VARCHAR(100),                      -- Vehicle model
    vehicle_color VARCHAR(100),                      -- Vehicle color
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (device_event_id) REFERENCES incidents(device_event_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_license_plate (license_plate),
    INDEX idx_state_or_region (state_or_region)
);

-- User table for user authentication and incident reporting
CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(255) PRIMARY KEY,                -- User ID from mobile app
    username_or_email VARCHAR(255) UNIQUE NOT NULL,  -- Stored email/username
    is_admin TINYINT(1) DEFAULT 0 NOT NULL,          -- Set true for Admin accounts
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Session tokens table for user authentication
CREATE TABLE IF NOT EXISTS sessions (
    session_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,                   -- Links to users table
    auth_token_hash VARCHAR(255) UNIQUE NOT NULL,    -- Secure hashed token
    is_admin TINYINT(1) DEFAULT 0 NOT NULL,          -- Admin flag for session holder
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,                            -- Session expiration time
    FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_auth_token_hash (auth_token_hash),
    INDEX idx_user_id (user_id)
);
