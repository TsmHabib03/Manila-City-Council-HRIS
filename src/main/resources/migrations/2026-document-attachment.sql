CREATE TABLE IF NOT EXISTS document_attachment (
    id                 BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    original_file_name VARCHAR(500),
    file_url           VARCHAR(500),
    file_type          VARCHAR(20),
    file_size_bytes    BIGINT,
    uploaded_at        DATETIME,
    uploaded_by        VARCHAR(255),
    module             VARCHAR(50),
    record_id          BIGINT,
    employee_id        BIGINT,
    FOREIGN KEY (employee_id) REFERENCES employee(id)
);

-- Drop old @ElementCollection join tables (run AFTER confirming no real data to keep)
DROP TABLE IF EXISTS service_record_attachment_urls;
DROP TABLE IF EXISTS clearance_attachment_urls;
DROP TABLE IF EXISTS appointment_attachment_urls;
