CREATE TABLE bookings (
    id UUID PRIMARY KEY,
    booking_code VARCHAR(32) NOT NULL UNIQUE,
    client_name VARCHAR(160) NOT NULL,
    client_email VARCHAR(254) NOT NULL,
    client_phone VARCHAR(32) NOT NULL,
    event_type VARCHAR(80) NOT NULL,
    event_date DATE NOT NULL,
    event_location TEXT,
    package_name VARCHAR(160),
    total_amount NUMERIC(12,2) NOT NULL CHECK (total_amount >= 0),
    advance_amount NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (advance_amount >= 0),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'AWAITING_PAYMENT', 'CONFIRMED', 'COMPLETED', 'CANCELLED')),
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT advance_not_over_total CHECK (advance_amount <= total_amount)
);

CREATE INDEX idx_bookings_event_date ON bookings(event_date);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_client_email ON bookings(client_email);

CREATE TABLE payments (
    id UUID PRIMARY KEY,
    booking_id UUID NOT NULL REFERENCES bookings(id),
    amount NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    method VARCHAR(32) NOT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'SUBMITTED', 'VERIFIED', 'REJECTED', 'REFUNDED')),
    provider_reference VARCHAR(160),
    proof_url TEXT,
    verified_by VARCHAR(160),
    verified_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payments_booking_id ON payments(booking_id);
CREATE INDEX idx_payments_status ON payments(status);