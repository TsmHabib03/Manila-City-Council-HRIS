-- ============================================================================
-- CR Request ID 016 — Multi-stage Leave Decision Flow
-- Production migration for hris_01 (spring.jpa.hibernate.ddl-auto=none in prod;
-- dev environments create these automatically via ddl-auto=update).
--
-- Additive only: three new tables, no ALTERs to existing tables.
-- Safe to re-run (IF NOT EXISTS).
-- Run as:  mysql -u root -p hris_01 < CR016_leave_workflow_migration.sql
-- ============================================================================

-- Audit trail: one row per workflow step (screen/endorse/review/approve/...)
CREATE TABLE IF NOT EXISTS leave_workflow_action (
    id                   BIGINT       NOT NULL AUTO_INCREMENT,
    leave_application_id BIGINT       DEFAULT NULL,
    from_status          VARCHAR(255) DEFAULT NULL,
    to_status            VARCHAR(255) DEFAULT NULL,
    action               VARCHAR(255) DEFAULT NULL,
    actor_username       VARCHAR(255) DEFAULT NULL,
    actor_name           VARCHAR(255) DEFAULT NULL,
    signatory_name       VARCHAR(255) DEFAULT NULL,
    signatory_title      VARCHAR(255) DEFAULT NULL,
    remarks              VARCHAR(255) DEFAULT NULL,
    created_at           DATETIME     DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_lwa_application (leave_application_id),
    CONSTRAINT fk_lwa_application FOREIGN KEY (leave_application_id)
        REFERENCES leave_application (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- In-app notifications (navbar bell)
CREATE TABLE IF NOT EXISTS notification (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    employee_id BIGINT       DEFAULT NULL,
    message     VARCHAR(255) DEFAULT NULL,
    link        VARCHAR(255) DEFAULT NULL,
    read_flag   BIT(1)       NOT NULL DEFAULT b'0',
    created_at  DATETIME     DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_notification_employee (employee_id),
    CONSTRAINT fk_notification_employee FOREIGN KEY (employee_id)
        REFERENCES employee (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Supporting documents for leaves > 5 working days (@ElementCollection)
CREATE TABLE IF NOT EXISTS leave_application_supporting_doc_urls (
    leave_application_id BIGINT       NOT NULL,
    supporting_doc_urls  VARCHAR(255) DEFAULT NULL,
    KEY idx_lasdu_application (leave_application_id),
    CONSTRAINT fk_lasdu_application FOREIGN KEY (leave_application_id)
        REFERENCES leave_application (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
