-- P2-2: Add hours_display column to store text-based L&D hour descriptions
-- (e.g. "8-week online programme") alongside the existing numeric noHours column.
ALTER TABLE learning_development
    ADD COLUMN hours_display VARCHAR(100) NULL AFTER no_hours;
