-- ============================================================================
-- Migration: PDS Blood Type listbox -> free-text box (CR-005)
-- Date:      2026-06-26
-- Purpose:   The Blood Type field changed from a coded dropdown (O1/O2/A1...)
--            to a free-text box. Convert legacy stored codes to their readable
--            equivalents so existing records display sensibly in the new textbox.
--
-- Safe to run live: idempotent, only rewrites the 8 known legacy codes.
-- The PDS PDF already renders both forms correctly (formatBloodType default
-- passes through), so this is purely for the data-entry textbox UX.
-- ============================================================================

UPDATE employee SET blood_type = 'O+'  WHERE blood_type = 'O1';
UPDATE employee SET blood_type = 'O-'  WHERE blood_type = 'O2';
UPDATE employee SET blood_type = 'A+'  WHERE blood_type = 'A1';
UPDATE employee SET blood_type = 'A-'  WHERE blood_type = 'A2';
UPDATE employee SET blood_type = 'B+'  WHERE blood_type = 'B1';
UPDATE employee SET blood_type = 'B-'  WHERE blood_type = 'B2';
UPDATE employee SET blood_type = 'AB+' WHERE blood_type = 'AB1';
UPDATE employee SET blood_type = 'AB-' WHERE blood_type = 'AB2';
