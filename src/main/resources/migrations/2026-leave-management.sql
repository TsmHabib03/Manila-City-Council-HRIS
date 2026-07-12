-- Leave Management module: CS Form 6 applications + Employee's Leave Card ledger.
-- Dev environments create these automatically via spring.jpa.hibernate.ddl-auto=update;
-- run this on the live server (hris_01) before deploying the WAR.

CREATE TABLE IF NOT EXISTS leave_application (
    id                        BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    employee_id               BIGINT,
    office_department         VARCHAR(255),
    position                  VARCHAR(255),
    salary                    VARCHAR(255),
    date_of_filing            DATE,
    date_of_receipt           DATE,
    leave_type                VARCHAR(255),
    leave_type_others         VARCHAR(255),
    leave_detail_option       VARCHAR(255),
    leave_detail_text         VARCHAR(255),
    working_days              DOUBLE,
    date_from                 DATE,
    date_to                   DATE,
    inclusive_dates           VARCHAR(255),
    commutation               VARCHAR(255),
    cert_as_of_date           DATE,
    cert_vl_total_earned      DOUBLE,
    cert_vl_less_application  DOUBLE,
    cert_vl_balance           DOUBLE,
    cert_sl_total_earned      DOUBLE,
    cert_sl_less_application  DOUBLE,
    cert_sl_balance           DOUBLE,
    recommendation            VARCHAR(255),
    recommendation_reason     VARCHAR(255),
    status                    VARCHAR(255),
    approved_days_with_pay    DOUBLE,
    approved_days_without_pay DOUBLE,
    approved_others           VARCHAR(255),
    disapproval_reason        VARCHAR(255),
    disapproved_exigency      BIT NOT NULL DEFAULT 0,
    cert_officer_name         VARCHAR(255),
    cert_officer_title        VARCHAR(255),
    recommender_name          VARCHAR(255),
    recommender_title         VARCHAR(255),
    approver_name             VARCHAR(255),
    approver_title            VARCHAR(255),
    remarks                   VARCHAR(255),
    FOREIGN KEY (employee_id) REFERENCES employee(id)
);

CREATE TABLE IF NOT EXISTS leave_card_entry (
    id                   BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    employee_id          BIGINT,
    entry_type           VARCHAR(50),
    entry_date           DATE,
    period               VARCHAR(50),
    particulars          VARCHAR(255),
    vl_earned            DOUBLE,
    vl_deducted          DOUBLE,
    sl_earned            DOUBLE,
    sl_deducted          DOUBLE,
    remarks              VARCHAR(255),
    leave_application_id BIGINT,
    FOREIGN KEY (employee_id) REFERENCES employee(id),
    FOREIGN KEY (leave_application_id) REFERENCES leave_application(id)
);

-- ─── Leave settings: leave types & holidays (System Settings CRUD) ───────────
-- Rows are seeded by the application on first boot (LeaveSettingsSeeder), so
-- these CREATEs are all that is needed on the live server.

CREATE TABLE IF NOT EXISTS leave_type (
    id              BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    leave_type_name VARCHAR(255) NOT NULL,
    legal_basis     VARCHAR(255),
    is_active       BIT NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS holiday (
    id           BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    holiday_name VARCHAR(255) NOT NULL,
    holiday_date DATE NOT NULL,
    holiday_type VARCHAR(100) NOT NULL,
    remarks      VARCHAR(255)
);
