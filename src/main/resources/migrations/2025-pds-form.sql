-- ============================================================================
-- Migration: CS Form No. 212 (Revised 2025) — PDS report rebuild
-- Date:      2026-05-26
-- Purpose:   Add new fields required by the 2025 PDS form to existing tables.
--
-- Safe to run live: all columns are nullable + additive.
--
-- Required only in environments where spring.jpa.hibernate.ddl-auto is NOT
-- set to "update" (e.g. production, where it is "none"). Dev profile auto-
-- migrates on boot.
-- ============================================================================

-- New CSC-mandated identifiers (items 10 and 13 of the 2025 PDS).
ALTER TABLE employee
  ADD COLUMN umid_no    VARCHAR(255) NULL,
  ADD COLUMN philsys_no VARCHAR(255) NULL;

-- (No changes required to other_info_question. The existing column `question_six`
--  already represents PDS item 38B — "Have you resigned from the government
--  service during the three (3)-month period before the last election to
--  promote/actively campaign for a national or local candidate?" — both in the
--  edit form (other-info-question.html line ~259) and in the controller
--  mapping (ReportsController.populateMapReport4 line ~990, key VIII.OI_38_B_*).)
