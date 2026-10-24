ALTER TABLE bookings
    ADD COLUMN current_due_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00;

UPDATE bookings
SET current_due_amount = advance_amount
WHERE current_due_amount = 0.00 AND advance_amount > 0.00;

ALTER TABLE bookings
    ADD CONSTRAINT chk_booking_current_due CHECK (current_due_amount >= 0 AND current_due_amount <= total_amount);
