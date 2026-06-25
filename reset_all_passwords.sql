-- Reset ALL employee passwords to "password"
-- BCrypt hash (cost 11) of the string: password
-- Run on both dev and prod DBs.
-- Does NOT touch id=1 (system admin account).

SET NAMES utf8mb4;

UPDATE employee
SET password = '$2a$11$PBmRAX6wog1h4ZHS9IUz7OyGs04/s3172xTRTUO8SafooYalmNnLW'
WHERE id != 1;

SELECT ROW_COUNT() AS employees_updated;
