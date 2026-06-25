-- Migration: Add free-text academic honors column to educational_background
-- Required for prod (ddl-auto=none). Run once on the production database.
ALTER TABLE educational_background ADD COLUMN academic_honors_text VARCHAR(255) NULL;
