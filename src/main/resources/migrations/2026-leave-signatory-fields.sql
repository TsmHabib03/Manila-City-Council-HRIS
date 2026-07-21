-- CR Request ID 020: "Verified by" signatory, printed on both CS Form 6
-- (replacing the blank "IMMEDIATE SUPERVISOR" line) and the Leave
-- Verification Receipt (replacing the whoever's-logged-in auto-fill).
-- Dev picks these up automatically via spring.jpa.hibernate.ddl-auto=update;
-- run this on the live server (hris_01) before deploying the WAR.

ALTER TABLE leave_application ADD COLUMN verifier_name VARCHAR(255);
ALTER TABLE leave_application ADD COLUMN verifier_title VARCHAR(255);

ALTER TABLE leave_signatory ADD COLUMN verifier_name VARCHAR(255);
ALTER TABLE leave_signatory ADD COLUMN verifier_title VARCHAR(255);
