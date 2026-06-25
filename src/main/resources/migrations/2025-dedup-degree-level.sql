-- Migration: Remove duplicate degree levels, keeping lowest ID per name
-- This fixes the Edu BG dropdown showing duplicate "Elementary", "Secondary", etc.
DELETE FROM degree_level
WHERE id NOT IN (
    SELECT min_id FROM (SELECT MIN(id) AS min_id FROM degree_level GROUP BY degree_name) AS t
);
