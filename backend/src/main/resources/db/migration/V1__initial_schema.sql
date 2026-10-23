CREATE TABLE users (
    id CHAR(36) PRIMARY KEY,
    full_name VARCHAR(160) NOT NULL,
    email VARCHAR(254) NOT NULL UNIQUE,
    phone VARCHAR(32),
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(24) NOT NULL DEFAULT 'CLIENT',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'CLIENT'))
) ENGINE=InnoDB;

CREATE TABLE bookings (
    id CHAR(36) PRIMARY KEY,
    booking_code VARCHAR(32) NOT NULL UNIQUE,
    client_id CHAR(36) NOT NULL,
    client_name VARCHAR(160) NOT NULL,
    client_email VARCHAR(254) NOT NULL,
    client_phone VARCHAR(32) NOT NULL,
    event_type VARCHAR(80) NOT NULL,
    event_date DATE NOT NULL,
    event_location VARCHAR(500),
    package_name VARCHAR(160),
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    advance_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_bookings_client FOREIGN KEY (client_id) REFERENCES users(id),
    CONSTRAINT chk_booking_total CHECK (total_amount >= 0),
    CONSTRAINT chk_booking_advance CHECK (advance_amount >= 0 AND advance_amount <= total_amount),
    CONSTRAINT chk_booking_status CHECK (status IN ('PENDING','APPROVED','AWAITING_PAYMENT','CONFIRMED','COMPLETED','CANCELLED','REJECTED'))
) ENGINE=InnoDB;

CREATE INDEX idx_bookings_event_date ON bookings(event_date);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_client_id ON bookings(client_id);

CREATE TABLE payments (
    id CHAR(36) PRIMARY KEY,
    booking_id CHAR(36) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    method VARCHAR(32) NOT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'SUBMITTED',
    provider_reference VARCHAR(160),
    proof_url VARCHAR(1000),
    verified_by CHAR(36),
    verified_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payments_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT fk_payments_verifier FOREIGN KEY (verified_by) REFERENCES users(id),
    CONSTRAINT chk_payment_amount CHECK (amount > 0),
    CONSTRAINT chk_payment_status CHECK (status IN ('SUBMITTED','VERIFIED','REJECTED','REFUNDED'))
) ENGINE=InnoDB;

CREATE INDEX idx_payments_booking_id ON payments(booking_id);
CREATE INDEX idx_payments_status ON payments(status);
