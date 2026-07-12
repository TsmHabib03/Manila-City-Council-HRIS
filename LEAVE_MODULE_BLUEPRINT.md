# Leave Management Module — Blueprint (Analysis Only, No Code Changes)

## Context

The user asked for a local analysis of the three documents in `Leave_Docs/` (per `LEAVE_MANAGEMENT_PROMPT.md`) to produce a leave-module blueprint for this HRIS before any implementation. This document is that blueprint — the deliverable of this step. Nothing is uploaded to the server and no code is changed.

Sources analyzed:
1. `PROCEDURES-MANUAL-FAD-LEAVE-PROCESSING.pdf` (4 pp.) — workflow. Note: this is a Mines & Geosciences Bureau XIII manual (Doc PM-FAD-02, eff. 2017), used here as a reference template; roles must be mapped to Manila City Council equivalents.
2. `CS Form No. 6, Revised 2020 (Application for Leave) (Fillable) (2).pdf` (1 p.) — Manila City Council's customized CS Form 6 (printable export target).
3. `LEAVE_CARD_SAMPLE FOR EMPLOYEES.pdf` (5 pp.) — real employee leave card ledger (2017–2026 history).

---

## 1. What the leave module must do

**Workflow (from the procedures manual, steps 01–06):**
1. **Receive** a leave application (CS Form 6) from the requestor (office policy: 2 copies).
2. **Validate** — form filled up properly, signed by requestor, with initial of the Division Chief in the approval part. If incomplete → return to requestor; if complete → proceed.
3. **Compute** remaining leave credits (HR Staff).
4. **Record** the leave to the employee's Leave Card.
5. **Endorse for action** — three-stage chain:
   - HR Staff: *certifies leave balances* (Form 6 §7.A)
   - Chief Administrative Officer / immediate supervisor: *recommends approval or disapproval* (§7.B)
   - Approving authority: *approves or disapproves* (§7.C/7.D)
   - Special rule: **if disapproved due to exigency of the service, no deduction from leave credits** — process ends.
6. **File** — one copy of the approved application attached to the DTR (for COA), one copy for HR filing.

**Manila City Council signatory mapping (printed on their CS Form 6):**
- Certification of leave credits: *Gloria S. Tirado — Acting Chief Administrative Officer, concurrent HRM Section*
- Recommendation: immediate supervisor / *Atty. Hans Roger S. Luna — City Gov't Dept. Head III (Secretary to the City Council)*
- Approval: *Angela Lei "Chi" L. Atienza — Vice Mayor and Presiding Officer*

**Module capabilities implied:**
- File/encode a leave application for an employee (HR-encoded; the paper flow starts on paper, so the system records applications rather than employee self-service — self-service is not described in the docs).
- Status lifecycle: at minimum `Filed → Certified → Recommended → Approved / Disapproved` (plus `Returned` for incomplete, and "disapproved – exigency" with no credit deduction).
- Auto-compute current VL/SL balances at certification time ("Total Earned / Less this application / Balance").
- Post approved leave to the employee's leave card ledger (deduct only from the matching ledger; several leave types deduct nothing — see §4).
- Export the filled CS Form 6 as PDF.
- Render/print the Employee's Leave Card (ledger view + PDF).
- Support monthly accrual of 1.25 VL + 1.25 SL, and manual **Restoration** entries (credits added back, e.g. cancelled leave).

## 2. What data must be stored

**Leave application** (one row per CS Form 6):
- employee reference (existing `Employee`), office/department, position, salary (snapshot at filing)
- date of filing; stamp/date of receipt
- leave type (see full list in §3) + "Others" free text
- leave details (conditional on type): location within PH / abroad + place; in-hospital / outpatient + illness; women's-special-leave illness; study-leave purpose (Master's completion / BAR-Board review / other purpose)
- number of working days applied for; inclusive dates (start–end; possibly non-contiguous date list — the leave card shows entries like `2/15,19/19`)
- commutation: requested / not requested
- certification block: as-of date, VL and SL {total earned, less this application, balance}
- recommendation: for approval / for disapproval + reason
- action: approved for N days with pay / N days without pay / others; or disapproved + reason
- status + flag for "disapproved due to exigency" (no deduction)
- signatory names/positions used on the printout (configurable, since the form has named officials)

**Leave card ledger** (one row per period or transaction, per employee):
- employee reference; first day of service (header)
- period (e.g. month `12/20`) or blank for transaction rows
- particulars (e.g. `(1-0-0) V`, `(2-0-0) SPL`, `30 days Maternity Leave with Pay`, `Restoration`)
- VL: earned, absence/undertime-with-pay (days deducted), balance
- SL: earned, absence/undertime-with-pay, balance
- remarks: actual dates of the leave (e.g. `12/26/19, 1/2/20`)
- link to the leave application row when the entry came from an approved application
- footer signatories: Prepared By, Certified Correct

**Balances** are derivable as running totals but the ledger stores the balance per row (matching the card).

## 3. What the printable CS Form 6 PDF must contain

Header: "Republic of the Philippines / City Council of Manila / Manila City Hall", "Civil Service Form No. 6, Revised 2020", stamp-of-receipt box.

- 1. Office/Department  2. Name (Last, First, Middle)  3. Date of filing  4. Position  5. Salary
- **6. Details of Application**
  - 6.A Type of leave (checkboxes, with legal citations): Vacation; Mandatory/Forced; Sick; Maternity; Paternity; Special Privilege; Solo Parent; Study; 10-Day VAWC; Rehabilitation Privilege; Special Leave Benefits for Women; Special Emergency (Calamity); Adoption; Others: Monetization of Leave Credits, Terminal Leave
  - 6.B Details of leave (conditional sub-fields): Vacation/SPL → within Philippines ___ / abroad ___; Sick → in hospital (illness) / outpatient (illness); Women's special leave → specify illness; Study → completion of Master's degree / BAR-Board exam review / other purpose
  - 6.C Number of working days applied for + inclusive dates
  - 6.D Commutation: requested / not requested + signature of applicant
- **7. Details of Action on Application**
  - 7.A Certification of leave credits: "As of ___", table (Vacation | Sick) × (Total Earned | Less this application | Balance), signed *Gloria S. Tirado, Acting Chief Administrative Officer, concurrent HRM Section*
  - 7.B Recommendation: for approval / for disapproval due to ___, immediate supervisor line; *Atty. Hans Roger S. Luna, City Gov't Dept. Head III (Secretary to the City Council)*
  - 7.C Approved for: ___ days with pay / ___ days without pay / ___ others
  - 7.D Disapproved due to: ___
  - Final signature: *Angela Lei "Chi" L. Atienza — Vice Mayor and Presiding Officer*

## 4. What the leave card PDF/view must contain

Header: "Republic of the Philippines / CITY COUNCIL / Manila / Employee's Leave Card"; NAME (surname-first, e.g. `GARING, AILEEN A.`); FIRST DAY OF SERVICE (repeated on every page).

Columns: **PERIOD | PARTICULARS | VACATION LEAVE (Earned, Absence-Undertime w/pay, Balance) | SICK LEAVE (Earned, Absence-Undertime w/pay, Balance) | REMARKS** (dates of leave).

Observed ledger behavior (from the sample):
- Monthly accrual row: period = month (e.g. `3/22`), VL earned 1.25 and SL earned 1.25, balances increment. First partial month is prorated (e.g. `11/26-30/17` → 0.208).
- Leave-usage row: particulars in `(d-h-m) TYPE` notation (e.g. `(1-0-0) V`, `(2-0-0) S`); deducted days go in the *Absence/Undertime w/pay* column of the matching ledger and reduce that balance only; remarks hold the actual dates.
- **Non-deducting entries** appear in particulars/remarks but change no balance: SPL, Calamity/Special Emergency, Maternity ("30 days Maternity Leave with Pay"), Home Quarantine, Wellness, SEL, VAWC-type special leaves. Only V deducts VL and S deducts SL.
- **Restoration** rows add credits back (e.g. `Restoration 1` → +1 VL, remark "Restored Leave: 12/29/25").
- Footer: PREPARED BY (name + Administrative Officer IV) and CERTIFIED CORRECT (name + Chief Administrative Officer).

## 5. Out of scope for now

- Attendance/DTR processing and payroll — the manual mentions DTR (CSC Form 48) only as a filing/routing attachment for COA; leave processing does not depend on it beyond attaching a copy.
- Employee self-service filing and digital signatures (paper-driven flow; documents describe HR encoding).
- Monetization and terminal-leave *computation* (they appear only as leave-type checkboxes on Form 6; no computation rules are given in these documents).
- Undertime computation from DTR (leave card has the column, but no rules are documented; sample shows only whole/fraction day deductions from filed leaves).
- Server deployment/upload — explicitly excluded by the prompt.

**Unclear / needs confirmation later:** exact meaning of `(d-h-m)` notation (assumed days-hours-minutes); proration formula for partial first month (0.208); whether forced/mandatory leave deducts from VL (CSC rules say yes, but the sample has no explicit "F" entry); how Manila City Council maps the manual's "Division Chief initial" step.

---

## How this maps to the codebase (for the *next* planning step, not this one)

- New package following existing submodule pattern: `src/main/java/com/ian/web/employee/leave/` (entity + repository + controller, like `employee/appointment/`).
- Two entities: `LeaveApplication` and `LeaveCardEntry` (ledger), linked to `Employee`.
- PDF export via the existing JasperReports setup (`src/main/resources/jasper/reports/`, patterns in `reports/ReportsController.java`) — one template for CS Form 6, one for the Leave Card.
- Thymeleaf views under `templates/employee/` following existing employee sub-tab conventions.
- Signatory names should be configurable (system settings pattern in `systemsettings/`) rather than hardcoded, since officials change.

## Verification (for this analysis step)

No code to run. Verification = user reviews this blueprint against the source PDFs. Next step after approval: detailed implementation plan (DB schema/migration, controllers, Jasper templates).
