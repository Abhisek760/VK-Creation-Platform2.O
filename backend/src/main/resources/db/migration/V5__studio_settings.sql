CREATE TABLE studio_settings (
 id CHAR(36) PRIMARY KEY,
 studio_name VARCHAR(160) NOT NULL,
 contact_email VARCHAR(254),
 contact_phone VARCHAR(32),
 upi_id VARCHAR(160),
 upi_qr_url VARCHAR(1200),
 bank_name VARCHAR(160),
 bank_account_name VARCHAR(160),
 bank_account_number VARCHAR(80),
 bank_ifsc VARCHAR(20),
 payment_instructions VARCHAR(2000),
 legal_terms VARCHAR(8000),
 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;
