CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'ADMIN',
    PRIMARY KEY (user_id),
    CONSTRAINT uq_users_username UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS setup_state (
    state_id INT NOT NULL PRIMARY KEY,
    initialized BOOLEAN NOT NULL DEFAULT FALSE
);

INSERT INTO setup_state (state_id, initialized)
SELECT 1, FALSE WHERE NOT EXISTS (SELECT 1 FROM setup_state WHERE state_id = 1);

CREATE TABLE IF NOT EXISTS rooms (
    room_id BIGINT NOT NULL AUTO_INCREMENT,
    room_number VARCHAR(20) NOT NULL,
    room_type VARCHAR(50) NOT NULL,
    nightly_rate DECIMAL(10, 2) NOT NULL,
    available BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (room_id),
    CONSTRAINT uq_rooms_room_number UNIQUE (room_number),
    CONSTRAINT chk_rooms_nightly_rate_positive CHECK (nightly_rate > 0)
);

ALTER TABLE rooms ADD COLUMN IF NOT EXISTS air_conditioned BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS bookings (
    booking_id BIGINT NOT NULL AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    customer_email VARCHAR(254) NOT NULL,
    customer_phone VARCHAR(30) NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    special_requests VARCHAR(1000),
    food_requested BOOLEAN NOT NULL DEFAULT FALSE,
    food_items VARCHAR(1000) NOT NULL DEFAULT '',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (booking_id),
    CONSTRAINT fk_bookings_room FOREIGN KEY (room_id) REFERENCES rooms (room_id),
    CONSTRAINT chk_bookings_dates CHECK (check_out_date > check_in_date),
    CONSTRAINT chk_bookings_status CHECK (status IN ('PENDING', 'CONFIRMED', 'REJECTED', 'CANCELLED', 'CHECKED_OUT'))
);

ALTER TABLE bookings ADD COLUMN IF NOT EXISTS food_requested BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS food_items VARCHAR(1000) NOT NULL DEFAULT '';
ALTER TABLE bookings DROP CONSTRAINT IF EXISTS chk_bookings_status;
ALTER TABLE bookings ADD CONSTRAINT chk_bookings_status
    CHECK (status IN ('PENDING', 'CONFIRMED', 'REJECTED', 'CANCELLED', 'CHECKED_OUT'));
