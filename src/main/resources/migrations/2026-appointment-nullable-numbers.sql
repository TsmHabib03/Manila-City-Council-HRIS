-- ============================================================================
-- Migration: Make Appointment.salary_grade / step_inc nullable
-- Date:      2026-07-03
-- Purpose:   Change Request Phase 1, item #6/#7 — Appointment's Salary Grade
--            and Step Increment were primitive ints, so a blank form submit
--            threw an uncaught Spring data-binding exception (the raw
--            "200/500 ..." style error page). Making them nullable lets a
--            blank submission bind to NULL and display as "N/A" instead.
--
-- Safe to run live: widening a NOT NULL int column to NULL is non-destructive.
-- ============================================================================

ALTER TABLE appointment MODIFY salary_grade INT NULL;
ALTER TABLE appointment MODIFY step_inc INT NULL;
