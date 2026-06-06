-- ============================================================================
-- Migration: PDS UAT Feedback Fixes
-- Date:      2026-06-04
-- Purpose:   Add columns for maiden name, custom school name, and seed
--            missing reference data surfaced during UAT.
--
-- Safe to run live: all column additions are nullable + additive.
-- INSERT IGNORE prevents duplicates on re-run.
-- ============================================================================

DROP PROCEDURE IF EXISTS AddColumnIfNotExists;
DELIMITER //
CREATE PROCEDURE AddColumnIfNotExists(
    IN tbl_name VARCHAR(64),
    IN col_name VARCHAR(64),
    IN col_def VARCHAR(255)
)
BEGIN
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = tbl_name
        AND COLUMN_NAME = col_name
    ) THEN
        SET @sql_text = CONCAT('ALTER TABLE `', tbl_name, '` ADD COLUMN `', col_name, '` ', col_def);
        PREPARE stmt FROM @sql_text;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

CALL AddColumnIfNotExists('family_bg', 'maiden_name', 'VARCHAR(255) NULL');
CALL AddColumnIfNotExists('employee', 'maiden_name', 'VARCHAR(255) NULL');
CALL AddColumnIfNotExists('educational_background', 'school_custom_name', 'VARCHAR(255) NULL');

-- Seed commonly expected academic honours that may be absent from the table.
INSERT IGNORE INTO academic_honors (academic_honors_name) VALUES ('SUMMA CUM LAUDE');
INSERT IGNORE INTO academic_honors (academic_honors_name) VALUES ('MAGNA CUM LAUDE');
INSERT IGNORE INTO academic_honors (academic_honors_name) VALUES ('CUM LAUDE');
INSERT IGNORE INTO academic_honors (academic_honors_name) VALUES ('WITH HONORS');
INSERT IGNORE INTO academic_honors (academic_honors_name) VALUES ('WITH HIGH HONORS');
INSERT IGNORE INTO academic_honors (academic_honors_name) VALUES ('WITH HIGHEST HONORS');

-- Seed "Elementary Graduate" degree level if absent.
INSERT IGNORE INTO degree_level (degree_name, is_active) VALUES ('ELEMENTARY GRADUATE', 1);

-- Persist "Same as Residential" checkbox state for the permanent address section.
CALL AddColumnIfNotExists('employee', 'is_permanent_same_as_residential', 'TINYINT NOT NULL DEFAULT 0');
-- Persist "Same as Residential" checkbox state for the permanent address section.
ALTER TABLE employee DROP COLUMN IF EXISTS is_permanent_same_as_residential;
ALTER TABLE employee ADD COLUMN is_permanent_same_as_residential TINYINT NULL DEFAULT NULL;

DROP PROCEDURE AddColumnIfNotExists;
