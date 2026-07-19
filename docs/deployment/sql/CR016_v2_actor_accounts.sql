-- ============================================================================
-- CR Request ID 016 (revision 2): leave workflow actor accounts
-- ============================================================================
-- Creates the dedicated login accounts for the leave decision flow:
--   supervisor  -> ROLE_SUPERVISOR  (Supervisor endorsement stage)
--   council_sec -> ROLE_COUNCIL     (Secretary to the City Council — Council Review stage)
--   vice_mayor  -> ROLE_VICEMAYOR   (Vice-Mayor — Final Approval stage)
--
-- NO DDL — the roles/statuses are plain varchar values; the CR016 v1 tables
-- (leave_workflow_action, notification, leave_application_supporting_doc_urls)
-- must already exist (run CR016_leave_workflow_migration.sql first).
--
-- Safe to re-run: INSERT ... WHERE NOT EXISTS keyed on the unique username.
--
-- TEMPORARY PASSWORDS (BCrypt cost=11, change via the app after handover):
--   supervisor  / Supervisor@2026
--   council_sec / Council@2026
--   vice_mayor  / ViceMayor@2026
-- >>> HAND THESE OVER SECURELY AND HAVE EACH OFFICIAL CHANGE THEIR PASSWORD
-- >>> ON FIRST LOGIN (Employee List -> credentials, or /change-password).
--
-- Notes:
--  * status = 'N/A' (not ACTIVE) keeps these service accounts out of the
--    year-end mandatory leave deduction and other active-employee sweeps.
--  * If an official already exists as a real employee row and should approve
--    from their own record instead, skip the seed for that actor and run:
--        UPDATE employee SET user_type = 'ROLE_VICEMAYOR' WHERE username = '<their username>';
--    (Any number of ROLE_SUPERVISOR accounts may exist; the queue is global.)
-- ============================================================================

INSERT INTO employee (username, password, emp_hash_code, first_name, last_name, status, user_type)
SELECT 'supervisor',
       '$2a$11$33TsPVTYL3lvJon46zvC/OYS8Ze3GojkLjGrlvsyVcOhOXylstlda',
       UUID(), 'Department', 'Supervisor', 'N/A', 'ROLE_SUPERVISOR'
WHERE NOT EXISTS (SELECT 1 FROM employee WHERE username = 'supervisor');

INSERT INTO employee (username, password, emp_hash_code, first_name, last_name, status, user_type)
SELECT 'council_sec',
       '$2a$11$wf1d32eiyhptwEIFVwHR8.PPie1SQg2PAUzMTHfgUsDtRk6C3LH2G',
       UUID(), 'Hans Roger', 'Luna', 'N/A', 'ROLE_COUNCIL'
WHERE NOT EXISTS (SELECT 1 FROM employee WHERE username = 'council_sec');

INSERT INTO employee (username, password, emp_hash_code, first_name, last_name, status, user_type)
SELECT 'vice_mayor',
       '$2a$11$lv72MUGmkzCLtn6avB/Iqe6Q5E5RZt34jo4AlaVdyeFJVia3tdtrW',
       UUID(), 'Angela Lei', 'Atienza', 'N/A', 'ROLE_VICEMAYOR'
WHERE NOT EXISTS (SELECT 1 FROM employee WHERE username = 'vice_mayor');

-- Verification:
--   SELECT id, username, user_type, status FROM employee
--   WHERE user_type IN ('ROLE_SUPERVISOR','ROLE_COUNCIL','ROLE_VICEMAYOR');
