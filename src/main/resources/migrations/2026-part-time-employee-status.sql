-- ============================================================================
-- Migration: Add "Part-time" Employment Status
-- Date:      2026-07-03
-- Purpose:   Change Request Phase 1, item #8 — Employee Status list is fully
--            DB-driven, so adding Part-time is a seed-only change.
--
-- Safe to run live: INSERT IGNORE prevents duplicates on re-run.
-- ============================================================================

INSERT IGNORE INTO employee_status (employee_status_name, is_active) VALUES ('PART-TIME', 1);
