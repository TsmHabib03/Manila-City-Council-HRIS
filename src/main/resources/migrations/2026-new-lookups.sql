-- ============================================================================
-- Migration: New System Settings lookups
-- Date:      2026-07-03
-- Purpose:   Change Request Phase 1 — Civil Status, Department, Office, and
--            Appointment Status become full CRUD System Settings lookups
--            instead of hardcoded/free-text fields.
--
-- Safe to run live: new tables only, existing columns left untouched
-- (Employee.civil_status, position_titles.department_code, appointment/
-- work_experience office fields, and appointment/work_experience status
-- fields all remain plain String columns — no FK conversion, fully
-- backward compatible with existing data).
-- INSERT IGNORE prevents duplicates on re-run.
-- ============================================================================

CREATE TABLE IF NOT EXISTS civil_status (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    civil_status_name VARCHAR(255) NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1
);
INSERT IGNORE INTO civil_status (civil_status_name, is_active) VALUES
    ('SINGLE', 1), ('MARRIED', 1), ('WIDOWED', 1), ('SEPARATED', 1), ('OTHERS', 1);

CREATE TABLE IF NOT EXISTS department (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    department_name VARCHAR(255) NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1
);
-- Seed from any distinct existing PositionTitle.department_code values (safe no-op if none exist).
INSERT IGNORE INTO department (department_name, is_active)
    SELECT DISTINCT UPPER(TRIM(department_code)), 1
    FROM position_titles
    WHERE department_code IS NOT NULL AND TRIM(department_code) <> '';

CREATE TABLE IF NOT EXISTS office (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    office_name VARCHAR(255) NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1
);
-- Seed from distinct existing free-text office values on Appointment and WorkExperience.
INSERT IGNORE INTO office (office_name, is_active)
    SELECT DISTINCT UPPER(TRIM(office_assignment)), 1 FROM appointment
    WHERE office_assignment IS NOT NULL AND TRIM(office_assignment) <> '';
INSERT IGNORE INTO office (office_name, is_active)
    SELECT DISTINCT UPPER(TRIM(office_name)), 1 FROM work_experience
    WHERE office_name IS NOT NULL AND TRIM(office_name) <> '';

CREATE TABLE IF NOT EXISTS appointment_status (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    appointment_status_name VARCHAR(255) NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1
);
INSERT IGNORE INTO appointment_status (appointment_status_name, is_active) VALUES
    ('CASUAL', 1), ('CONTRACTUAL', 1), ('PERMANENT', 1), ('TEMPORARY', 1),
    ('NO-EMP-STATUS', 1), ('ELECTIVE', 1), ('JOB ORDER', 1),
    ('CONTRACT OF SERVICE', 1), ('CONSULTANT', 1), ('COTERMINOUS', 1),
    ('PART-TIME', 1);
