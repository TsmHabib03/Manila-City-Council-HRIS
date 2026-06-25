-- P2-1: Add is_part_time flag to work_experience to distinguish
-- part-time employment entries from regular (full-time) ones.
ALTER TABLE work_experience
    ADD COLUMN is_part_time TINYINT(1) NOT NULL DEFAULT 0 AFTER up_to_present;
