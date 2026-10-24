CREATE TABLE studio_packages (
 id CHAR(36) PRIMARY KEY,
 name VARCHAR(120) NOT NULL,
 category VARCHAR(80) NOT NULL,
 price DECIMAL(12,2) NOT NULL,
 description VARCHAR(2000),
 features_text VARCHAR(4000),
 active BOOLEAN NOT NULL DEFAULT TRUE,
 sort_order INT NOT NULL DEFAULT 0,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 CONSTRAINT chk_package_price CHECK (price >= 0)
) ENGINE=InnoDB;

CREATE INDEX idx_packages_active_order ON studio_packages(active, sort_order);

CREATE TABLE gallery_items (
 id CHAR(36) PRIMARY KEY,
 title VARCHAR(180) NOT NULL,
 media_type VARCHAR(20) NOT NULL,
 secure_url VARCHAR(1200) NOT NULL,
 category VARCHAR(120),
 published BOOLEAN NOT NULL DEFAULT TRUE,
 sort_order INT NOT NULL DEFAULT 0,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT chk_gallery_media_type CHECK (media_type IN ('IMAGE','VIDEO'))
) ENGINE=InnoDB;

CREATE INDEX idx_gallery_published_order ON gallery_items(published, sort_order);
