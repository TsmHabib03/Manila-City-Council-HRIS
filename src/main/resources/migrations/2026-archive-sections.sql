-- CR Request ID 023: admin-manageable Archive sub-folders. Sections used to
-- be hardcoded (SALN/RESIGNED/LEAVES); rows are seeded by the application on
-- first boot (ArchiveSectionSeeder), so this CREATE is all that is needed on
-- the live server. Dev picks it up automatically via ddl-auto=update.

CREATE TABLE IF NOT EXISTS archive_section (
    id           BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code         VARCHAR(64) NOT NULL UNIQUE,
    display_name VARCHAR(255) NOT NULL,
    sort_order   INT NOT NULL DEFAULT 0
);
