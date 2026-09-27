# readMeAssets — screenshot manifest

73 JPG captures for the README. All shots taken against a locally running instance
(`http://localhost:8082`, `mvn clean spring-boot:run`) at viewport **1600px wide**, JPEG q90.

| Column | Meaning |
| --- | --- |
| **File** | Filename inside `readMeAssets/` |
| **Screen** | What the capture shows |
| **Route** | Server route that was captured |
| **Role** | Account used to capture it |

Roles: `admin` (HR Administrator) · `hr_user` (HR Officer) · `emp_user` (Rank and File Employee) ·
`supervisor` (Department Supervisor) · `council_sec` (Secretary to the City Council) ·
`vice_mayor` (Vice Mayor / Presiding Officer) · *(none)* = public page.

---

## 1. Entry & dashboards

| File | Screen | Route | Role |
| --- | --- | --- | --- |
| `LOG-01_login_page.jpg` | Login form (username / password / csrf) | `/login` | *(none)* |
| `DSH-01_admin_dashboard.jpg` | Admin dashboard — leave, clearance and appointment widgets | `/dashboard` | admin |
| `DSH-HR_dashboard.jpg` | HR Officer dashboard | `/dashboard` | hr_user |
| `DSH-EMP_employee_dashboard.jpg` | Employee dashboard — My Clearance Application List + profile card | `/dashboard` | emp_user |

## 2. Personal Data Sheet (PDS) — CSC Form 101

Ten tabs of the PDS wizard, plus the printable PDF in §9.

| File | Screen | Route | Role |
| --- | --- | --- | --- |
| `PDS-01_personal_info.jpg` | Personal Information (data entry) | `/employee/1/HRADMIN/{hash}` | admin |
| `PDS-02_family_background.jpg` | Family Background | `/employee/familybg/1/HRADMIN/{hash}` | admin |
| `PDS-03_educational_background.jpg` | Educational Background (+ PDS completion checklist) | `/employee/educationalbg/1/HRADMIN/{hash}` | admin |
| `PDS-04_eligibility.jpg` | Civil Service Eligibility | `/employee/civil-eligibility/1/HRADMIN/{hash}` | admin |
| `PDS-05_work_experience.jpg` | Work Experience | `/employee/work-experience/1/HRADMIN/{hash}` | admin |
| `PDS-06_voluntary_work.jpg` | Voluntary Work | `/employee/voluntary-work/1/HRADMIN/{hash}` | admin |
| `PDS-07_learning_development.jpg` | Learning & Development / Training | `/employee/learning-development/1/HRADMIN/{hash}` | admin |
| `PDS-08_other_info.jpg` | Other Information | `/employee/other-info/1/HRADMIN/{hash}` | admin |
| `PDS-09_references.jpg` | References | `/employee/references/1/HRADMIN/{hash}` | admin |
| `PDS-10_government_id.jpg` | Government Issued ID | `/employee/government-id/1/HRADMIN/{hash}` | admin |

## 3. 201 Files (employee document management)

| File | Screen | Route | Role |
| --- | --- | --- | --- |
| `F201-01_201_files.jpg` | 201 Files — Employee Document Management (312 employees) | `/201files` | admin |
| `F201-02_201_detail.jpg` | 201 File Information — per-employee document checklist | `/201files/1/{hash}` | admin |
| `F201-MY-01_201_files.jpg` | Employee's own 201 Files | `/my201files/3/{hash}` | emp_user |

## 4. Leave Management (CS Form No. 6) — core module

| File | Screen | Route | Role |
| --- | --- | --- | --- |
| `LV-01_leave_employee_list.jpg` | **Leave Management** — employee selector (312 entries) | `/leaves` | admin |
| `LV-02_leave_record.jpg` | **Leave Record** — CS Form No. 6 applications + Employee's Leave Card ledger | `/leaves/1/{hash}` | admin |
| `LV-03_leave_tracker.jpg` | **Leave Tracker** — FullCalendar of employees on approved leave | `/leave-tracker` | admin |
| `LV-04_leave_application_queue.jpg` | **Leave Applications — Decision Flow Queue** + Year-End Mandatory/Forced Leave Processing | `/leave-applications` | admin |
| `LV-05_leave_signatories.jpg` | **Leave Signatories & Endorsement** — 7.A certifying / 7.B recommending / 7.C-7.D approving authority | `/leave-signatories` | admin |
| `LV-MY-01_my_leaves.jpg` | **My Leaves** — employee self-service leave filing | `/my-leaves/3/{hash}` | emp_user |

## 5. Multi-role approval workflow

Server-side redirect `/dashboard` → `/leave-approvals` is **by design** for approver roles.

| File | Screen | Route | Role |
| --- | --- | --- | --- |
| `LV-06_leave_approvals_sup.jpg` | Leaves Awaiting Your Action (supervisor) | `/leave-approvals` | supervisor |
| `LV-07_leave_record_sup.jpg` | Supervisor's own Leave Record | `/leaves/1067/{hash}` | supervisor |
| `LV-09_leave_approvals_council.jpg` | Leaves Awaiting Your Action (Secretary to the City Council) | `/leave-approvals` | council_sec |
| `LV-10_leave_approvals_vicemayor.jpg` | Leaves Awaiting Your Action (Vice Mayor) | `/leave-approvals` | vice_mayor |
| `DSH-HR_dashboard.jpg` | HR Officer dashboard | `/dashboard` | hr_user |
| `EMP-LIST_as_hr.jpg` | Employee List as seen by HR Officer | `/employee-list` | hr_user |

## 6. Employee self-service

| File | Screen | Route | Role |
| --- | --- | --- | --- |
| `EMP-MY-01_personal_info.jpg` | My Personal Information | `/employee/3/PROFILE/{hash}` | emp_user |
| `EMP-MY-02_family_background.jpg` | My Family Background | `/employee/familybg/3/PROFILE/{hash}` | emp_user |
| `APT-MY-01_my_appointments.jpg` | My Appointments | `/my-appointments/3/{hash}` | emp_user |
| `CLR-01_my_clearance.jpg` | My Clearance | `/myclearance/3/{hash}` | emp_user |
| `SVC-MY-01_my_service_record.jpg` | My Service Record | `/my-service-record/3/{hash}` | emp_user |
| `CPW-MY-01_change_password.jpg` | Change Password (self) | `/change-password/3` | emp_user |

## 7. HR administration screens

| File | Screen | Route | Role |
| --- | --- | --- | --- |
| `EMP-01_employee_list.jpg` | Employee List (admin) | `/employee-list` | admin |
| `USR-01_user_management.jpg` | Employee List with role / status management (585 rows, reduced sidebar) — *reused from `docs/uat/screenshots-admin`; original capture route unverified, shows `/employee-list`* | `/employee-list` *(unverified)* | restricted role |
| `APT-LIST_appointments_list.jpg` | Appointments list | `/appointments` | admin |
| `APT-01_appointments.jpg` | Appointment detail | `/appointments/1/{hash}` | admin |
| `CLR-02_clearance_list.jpg` | Clearance List | `/clearance-list` | admin |
| `SVC-01_service_record.jpg` | Service Record (per employee) | `/employee-service-record/1/{hash}` | admin |
| `TRN-01_trainings.jpg` | Training and Seminar | `/trainings` | admin |
| `ARC-01_archive.jpg` | Archive — SALN section | `/archive/SALN` | admin |
| `CPW-01_change_password.jpg` | Change Password (admin) | `/change-password/1` | admin |

Archive sections available: `SALN`, `RESIGNED`, `LEAVES` (invalid codes silently redirect to `/dashboard`).

## 8. System Settings — reference data

| File | Screen | Route | Role |
| --- | --- | --- | --- |
| `SS-01_academic_honors.jpg` | Academic Honors | `/academic-honors` | admin |
| `SS-02_degree_levels.jpg` | Degree Levels — *reused from `docs/uat/screenshots-admin`* | `/degree-levels` | admin |
| `SS-03_degree_courses.jpg` | Degree Courses — *reused* | `/degree-courses` | admin |
| `SS-04_schools.jpg` | Schools — *reused* | `/schools` | admin |
| `SS-05_scholarships.jpg` | Scholarships — *reused* | `/scholarships` | admin |
| `SS-06_divisions.jpg` | Divisions | `/divisions` | admin |
| `SS-07_districts.jpg` | Districts — *reused* | `/districts` | admin |
| `SS-08_document_types.jpg` | Document Types — *reused* | `/document-types` | admin |
| `SS-09_professions.jpg` | Professions — *reused* | `/professions` | admin |
| `SS-10_employee_status.jpg` | Employee Status — *reused* | `/employee-status` | admin |
| `SS-11_eligibility_types.jpg` | Eligibility Types — *reused* | `/eligibility` | admin |
| `SS-12_learning_type.jpg` | Learning Types — *reused* | `/learning-type` | admin |
| `SS-13_position_titles.jpg` | Position Titles | `/position-titles` | admin |
| `SS-14_service_record_signatory.jpg` | Service Record Signatory — *reused* | `/service-record-signatory` | admin |
| `SS-15_clearance_approvers.jpg` | Clearance Approvers — *reused* | `/clearance-approver-settings` | admin |
| `SS-16_levels.jpg` | Levels — *reused* | `/levels` | admin |
| `SS-17_salary_grades.jpg` | Salary Grades | `/salary-grades` | admin |
| `SS-18_leave_types.jpg` | Leave Types (with legal basis) | `/leave-types` | admin |
| `SS-19_holidays.jpg` | Holidays | `/holidays` | admin |

## 9. JasperReports — generated PDFs

Captured by `scripts/screenshot_jasper.cjs` (authenticated `fetch` → PDF → PyMuPDF render of page 1).

| File | Report | Route | Role |
| --- | --- | --- | --- |
| `JAS-01_pds_form.jpg` | PDS (CSC Form 101), 4 pages | `/viewPds/1` | admin |
| `JAS-03_cs_form_6_leave.jpg` | **CS Form No. 6 — Application for Leave** | `/leaveForm6Pdf/1` | admin |
| `JAS-04_leave_card.jpg` | Leave Card | `/leaveCardPdf/1/{hash}` | admin |
| `JAS-05_clearance_form.jpg` | Clearance Form (CS Form 41-ish) — id is a **clearance application id** (valid: `4`) | `/viewClearanceForm/4` | admin |
| `Leave-Verification-Receipt-1_page-0001.jpg` | Leave Verification Receipt — *pre-existing asset* | — | — |
| `Leave-Card-1_page-0001.jpg` | Leave Card — *pre-existing asset* | — | — |
| `CS-Form-6-1 (1)_page-0001.jpg` | CS Form 6 — *pre-existing asset* | — | — |

## 10. Pre-existing / legacy assets

| File | Notes |
| --- | --- |
| `PDS-1_pages-to-jpg-0001.jpg`, `PDS-1_pages-to-jpg-0002.jpg`, `PDS-1_pages-to-jpg-0003.jpg`, `PDS-1_pages-to-jpg-0004.jpg` | 4 pages of a PDS export, already in the repo before this capture run. |

---

## Known issues & deliberate exclusions

| Item | Status |
| --- | --- |
| `JAS-02_service_record_form` | **Blank** — `hris_01.service_record` has **0 rows**, so the Jasper PDF renders empty. File deleted; not a capture bug. |
| `JAS-06_employee_list_report` | **Dead endpoint** — `ReportsController.java:1428-1431` `viewEmployeeListReport` has an empty method body (HTTP 500). |
| `/leave-pending-list` | Returns JSON (`LeaveController.java:139`), not a page — no screenshot. |
| `LV-06` / `LV-07` | Approval queue and supervisor leave record are **empty** — real data state (only 1 leave application exists, still `FILED`). |
| `LV-02`, `LV-04`, `JAS-03` | **Resolved** — the seeded test string `hentaivirus` in `hris_01.leave_application.leave_detail_text` (row `id=1`) was replaced with `Home confinement`, and all three shots were re-captured. Whole-database scan of every `varchar`/`text`/`longtext` column in `hris_01` returns 0 matches. |
| `readMeAssets/` | **Tracked** so the `README.md` screenshots render on GitHub (73 JPGs, ~12 MB). Regenerate with the commands below if captures are re-run. |
| `LV-08` | Never defined in the harness (id gap). |
| `/users` | `SysUserController.java:17` returns `users/users_list`, but **`templates/users/users_list.html` does not exist** → HTTP 500. No dedicated user-management screen to capture. |
| PII | Screenshots contain real employee names, positions, ID photos, birth dates and mobile numbers. Present on the public repo unless blurred or restricted. |

## Recommended README shortlist

Highest-value shots for a recruiter-facing README:

1. `LOG-01_login_page.jpg` — hero / getting started
2. `DSH-01_admin_dashboard.jpg` — overview
3. `LV-01_leave_employee_list.jpg` — Leave Management hub
4. `LV-04_leave_application_queue.jpg` — decision flow + year-end processing
5. `LV-05_leave_signatories.jpg` — CSC signatory chain
6. `F201-02_201_detail.jpg` — 201 Files
7. `PDS-01_personal_info.jpg` + `PDS-03_educational_background.jpg` — PDS wizard
8. `JAS-03_cs_form_6_leave.jpg` — JasperReports output
9. `LV-06_leave_approvals_sup.jpg` + `LV-10_leave_approvals_vicemayor.jpg` — multi-role approval
10. `SS-18_leave_types.jpg` — configuration/reference data

## Re-running captures

```bash
node scripts/screenshot.cjs --list        # batches: pilot, admin, employee, workflow, jasper
node scripts/screenshot.cjs admin
node scripts/screenshot_jasper.cjs        # PDF reports → readMeAssets/JAS-*.jpg
python scripts/render_pdf.py in.pdf out.jpg 144
```

Credentials are read from `%TEMP%\hrisp_creds.json` (outside the repo, never committed).
