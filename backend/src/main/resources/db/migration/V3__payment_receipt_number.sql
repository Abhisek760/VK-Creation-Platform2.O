ALTER TABLE payments
    ADD COLUMN receipt_number VARCHAR(40) NULL;

CREATE UNIQUE INDEX uk_payments_receipt_number
    ON payments (receipt_number);
