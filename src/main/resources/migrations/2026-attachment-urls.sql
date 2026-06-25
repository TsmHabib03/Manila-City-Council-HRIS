-- ============================================================================
-- Migration: Document Attachment URLs for Service Records, Clearances, Appointments
-- Date:      2026-06-12
-- Purpose:   Create @ElementCollection join tables for attachment URLs on
--            ServiceRecord, Clearance, and Appointment entities.
--            prod uses ddl-auto=none so Hibernate will NOT create these automatically.
--
-- Safe to run live: CREATE TABLE IF NOT EXISTS is idempotent.
-- ============================================================================

CREATE TABLE IF NOT EXISTS service_record_attachment_urls (
    service_record_id BIGINT NOT NULL,
    attachment_urls   VARCHAR(500),
    FOREIGN KEY (service_record_id) REFERENCES service_record(id)
);

CREATE TABLE IF NOT EXISTS clearance_attachment_urls (
    clearance_id    BIGINT NOT NULL,
    attachment_urls VARCHAR(500),
    FOREIGN KEY (clearance_id) REFERENCES clearance(id)
);

CREATE TABLE IF NOT EXISTS appointment_attachment_urls (
    appointment_id  BIGINT NOT NULL,
    attachment_urls VARCHAR(500),
    FOREIGN KEY (appointment_id) REFERENCES appointment(id)
);
