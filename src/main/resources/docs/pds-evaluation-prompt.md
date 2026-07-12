# PDS Evaluation & Remediation Prompt

**Master Claude Prompt for System Evaluation Against CSC Form 212 Revised 2025 Specification**

---

## Introduction

This document is a comprehensive prompt for Claude (or a development team) to systematically evaluate and remediate the HR Information System's Personal Data Sheet (PDS) implementation against the official CS Form No. 212 (Revised 2025) specification.

The prompt is designed to:
1. **Ensure compliance** with the 2025 CSC form standard
2. **Identify gaps and bugs** in the current Spring Boot + Jasper system
3. **Provide prioritized fix list** (P0 = critical, P1 = high, P2 = medium, P3 = nice-to-have)
4. **Validate against real-world test data** (Gloria Soriao Tirado's completed PDS)

---

## Context Documents

Before running this evaluation, **read these three documents in order:**

1. **`csc-form-212-2025-reference.md`** — Authoritative specification of every field, section, and constraint on the CSC Form 212 Revised 2025 (4 pages). This is the ground truth for compliance.

2. **`pds-sample-tirado.md`** — Real completed PDS for Gloria Soriao Tirado (attorney, retired from SSS, 21+ years of work experience, 3 graduate degrees). This sample is invaluable for testing edge cases (multiple education rows, "PRESENT" dates, fractional units, part-time employment, "DECEASED" parent, etc.).

3. **`pds-evaluation-prompt.md`** — This document (you are reading it now).

---

## System Overview: Spring Boot + Jasper PDS

**Project**: `c:\Users\Habib\IdeaProjects\hrisp-web01`

**Tech Stack**:
- Spring Boot 2.7+
- MySQL 5.7+
- Jasper Reports 6.x
- Thymeleaf HTML templates
- Bootstrap 4 / jQuery

**Key Components**:

| Component | Location | Purpose |
|-----------|----------|---------|
| **Employee Entity** | `src/main/java/com/ian/web/employee/Employee.java` | Core employee data; extends Person (firstName, lastName, etc.). Stores all PDS fields. |
| **Related Entities** | `src/main/java/com/ian/web/employee/.../*.java` | FamilyBg, EducationalBackground, CivilServiceEligibility, WorkExperience, VoluntaryWork, LearningAndDevelopment, OtherInfo, OtherInfoQuestion, EmpReferences, GovermentIssuedId |
| **ReportsController** | `src/main/java/com/ian/web/reports/ReportsController.java` | `GET /viewPds/{employeeId}` endpoint; generates 4-page PDS PDF via Jasper. Contains `populateMapReport2025_P1/P2/P3/P4()` methods that build parameter maps. |
| **Jasper Templates** | `src/main/resources/jasper/reports/PDS2025_*.jrxml` & `.jasper` | Four compiled Jasper reports (P1–P4). XML defines parameters, layout, datasources. |
| **HTML Forms** | `src/main/resources/templates/employee/pds/*.html` | Thymeleaf views for data entry (personnal-info.html, family-background.html, etc.). |
| **SQL Migrations** | `src/main/resources/migrations/2025-*.sql` | Database schema updates (e.g., added `umid_no`, `philsys_no`, `maiden_name` columns). |

**Report Generation Flow**:
```
GET /viewPds/{employeeId}
  ↓
Fetch Employee + all related data (FamilyBg, EducationalBackground, etc.)
  ↓
Load 4x Jasper .jasper templates from classpath
  ↓
Call populateMapReport2025_P1/P2/P3/P4() → build parameter maps
  ↓
JasperFillManager.fillReport() for each page (JREmptyDataSource)
  ↓
Merge pages 2–4 into page 1
  ↓
Export to PDF → send to browser
```

---

## Evaluation Tasks (Ordered by Priority)

### CRITICAL PRIORITY (P0)

#### P0-1: Verify `I.PI_MaidenName` Parameter Declaration in PDS2025_P1.jrxml

**What to Check**:
- Open: `src/main/resources/jasper/reports/PDS2025_P1.jrxml`
- Search for: `<parameter name="I.PI_MaidenName">`
- Expected: A parameter declaration with type `String`

**Context**:
- The ReportsController method `populateMapReport2025_P1()` calls:
  ```java
  map.put("I.PI_MaidenName", emp.getMaidenName());
  ```
- If the jrxml does not have a matching `<parameter>` declaration, Jasper silently drops the value and the maiden name never renders on the PDF.
- This affects married female employees (maiden name should appear on Page 1, Personal Information section).

**Expected Fix** (if missing):
- Add to the P1.jrxml `<parameters>` section:
  ```xml
  <parameter name="I.PI_MaidenName" class="java.lang.String"/>
  ```
- Recompile the .jasper file

**Validation**:
- Generate PDS for a married female employee in the system
- Verify maiden name appears on the PDF page 1 (expected location: near civil status field or as a separate row)

**Sample Test Case**: Use Tirado's PDS if re-entered into the system; expected maiden name: N/A (or blank if not collected).

---

#### P0-2: Audit Annotation Overlays on PDS2025_P1.jrxml/jasper

**What to Check**:
- The ANNEX H-1 reference PDF shows **red annotation marks** from a designer/reviewer.
- Annotations include: "FIX THIS FORMAT", "SSS NO. TO UMID ID NO.", "ADD THIS FIELD", "REMOVE ALL RED MARKS THAT IS NEEDED", etc.
- These are **editorial notes** and **must not** be baked into the final Jasper template.

**Context**:
- Jasper reports can embed graphics (PNG backgrounds) and shapes. If the red annotation overlays were added to the .jrxml and compiled into the .jasper, they will appear on every PDF output.
- This would look unprofessional and confuse employees filling out the form.

**Expected Check**:
1. Generate a test PDS PDF for any employee
2. Open the PDF and visually inspect page 1 (PDS2025_P1)
3. Verify: No red text/boxes/overlays visible
4. If red annotations ARE visible:
   - Open `PDS2025_P1.jrxml` in a text editor
   - Search for red color codes: `#ff0000`, `#FF0000`, or RGB(255,0,0)
   - Identify and remove any graphic elements with red fill/stroke that correspond to the annotations
   - Re-compile to .jasper

**Validation**:
- PDF page 1 should be clean, professional, matching the blank form in `csc-form-212-2025-reference.md`
- No red overlays, no annotation text

---

### HIGH PRIORITY (P1)

#### P1-1: Verify `I.PI_GSIS_ID_NO` Parameter in PDS2025_P1.jrxml

**What to Check**:
- Open: `src/main/resources/jasper/reports/PDS2025_P1.jrxml`
- Search for: `<parameter name="I.PI_GSIS_ID_NO">`
- Also search ReportsController for: `map.put("I.PI_GSIS_ID_NO", ...)`

**Context**:
- The Employee entity stores `gsisIdNo` (GSIS ID Number).
- The 2025 CSC form spec removes GSIS BP No. and GSIS Policy No. but **retains** GSIS ID No.
- The controller should populate the Jasper map with this value.

**Expected Fix** (if missing parameter in jrxml):
- Add to P1.jrxml `<parameters>` section:
  ```xml
  <parameter name="I.PI_GSIS_ID_NO" class="java.lang.String"/>
  ```
- Ensure ReportsController calls: `map.put("I.PI_GSIS_ID_NO", getDisplayValue(emp.getGsisIdNo()));`

**Validation**:
- Generate PDS for an employee with a GSIS ID
- Verify GSIS ID appears on PDF page 1 (Item 10 area or nearby)

---

#### P1-2: Audit `populateMapReport2025_P4()` for Missing `positionTitle` in References

**What to Check**:
- Open: `src/main/java/com/ian/web/reports/ReportsController.java`
- Find method: `populateMapReport2025_P4()`
- Search for references mapping: Look for lines like `map.put("VIII.OI_41_References_Name1", ref.getReferenceName());`

**Context**:
- The EmpReferences entity has these fields:
  - `referenceName`
  - `positionTitle` ← **stored but not used in report**
  - `companyAddress`
  - `companyContactNo`
- The CSC Form 212 (2025) includes a **Position / Title** column in the References section (Item 41).
- The current implementation likely only maps `referenceName`, `companyAddress`, and `companyContactNo`.

**Expected Fix**:
- Verify PDS2025_P4.jrxml has parameter(s): `VIII.OI_41_References_Position1`, `VIII.OI_41_References_Position2`, `VIII.OI_41_References_Position3` (or similar naming)
- If missing parameters, add them to P4.jrxml
- In ReportsController, add lines like:
  ```java
  map.put("VIII.OI_41_References_Position1", ref1.getPositionTitle() != null ? ref1.getPositionTitle() : "");
  map.put("VIII.OI_41_References_Position2", ref2.getPositionTitle() != null ? ref2.getPositionTitle() : "");
  map.put("VIII.OI_41_References_Position3", ref3.getPositionTitle() != null ? ref3.getPositionTitle() : "");
  ```

**Validation**:
- Generate PDS for an employee with reference positions filled (e.g., "Senior Attorney", "HR Manager")
- Verify these positions appear on PDF page 4, References section (Item 41)
- Cross-check against `pds-sample-tirado.md` where references show no position field, so "N/A" or blank is acceptable

---

#### P1-3: Fix Government Issued ID (Item 42) Date + Place Concatenation

**What to Check**:
- Open: `src/main/java/com/ian/web/reports/ReportsController.java`
- Find: `populateMapReport2025_P4()` method
- Search for: `VIII.OI_42_Date_Place_Of_Issurance` (note: typo "Issurance" vs. "Issuance")

**Context**:
- The GovermentIssuedId entity has:
  - `idNo` (e.g., "N04 00-460783")
  - `issuanceDate` (e.g., LocalDate 2024-06-14)
  - `placeOfIssuance` (e.g., "DLRC SM MANILA FO")
- The CSC form spec requires **combined** display: "DATE / PLACE" (e.g., "JUN 14, 2024 / DLRC SM MANILA FO")
- The current implementation likely only maps `placeOfIssuance`, missing the date.

**Expected Fix**:
- In ReportsController, modify the population of `VIII.OI_42_Date_Place_Of_Issurance`:
  ```java
  GovermentIssuedId govId = govIdList.get(0); // First ID only
  String datePlaceStr = "";
  if (govId.getIssuanceDate() != null) {
    datePlaceStr = formatDateDdMmYyyy(govId.getIssuanceDate()) + " / ";
  }
  if (govId.getPlaceOfIssuance() != null) {
    datePlaceStr += govId.getPlaceOfIssuance();
  }
  map.put("VIII.OI_42_Date_Place_Of_Issurance", datePlaceStr);
  ```
- Alternatively, if the Jasper template has separate parameters for date and place, populate both:
  ```java
  map.put("VIII.OI_42_Issuance_Date", formatDateDdMmYyyy(govId.getIssuanceDate()));
  map.put("VIII.OI_42_Place_Of_Issuance", govId.getPlaceOfIssuance());
  ```

**Validation**:
- Generate PDS for an employee with a gov't ID (driver's license, passport, etc.)
- Verify on PDF page 4, Item 42: date and place both appear (either concatenated or side-by-side)
- Expected format: "JUN 14, 2024 / DLRC SM MANILA FO" or similar

---

#### P1-4: Audit Multiple Education Rows (COLLEGE & GRADUATE STUDIES)

**What to Check**:
- Open: `src/main/java/com/ian/web/reports/ReportsController.java`
- Find: `populateMapReport2025_P1()` method
- Search for: How education entries are retrieved (likely: `List<EducationalBackground> eduList = educationalBackgroundRepository.findByEmployee(emp)`)
- Look for: How degrees are filtered by level (likely using `DegreeLevel` enum or similar)

**Current Likely Issue**:
- The code probably does something like:
  ```java
  List<EducationalBackground> collegeList = eduList.stream()
    .filter(e -> e.getDegreeLevel().getLevelCode().equals("COLLEGE"))
    .collect(Collectors.toList());
  if (!collegeList.isEmpty()) {
    EducationalBackground college = collegeList.get(0); // ← ONLY FIRST!
    map.put("III.EB_College_School", college.getSchool().getSchoolName());
    // ... rest of fields
  }
  ```
- This approach **only renders the first COLLEGE entry**, silently dropping additional entries.

**Test Case from `pds-sample-tirado.md`**:
- Tirado has **two COLLEGE entries**:
  1. Polytechnic University of the Philippines (BS Business Management, 1985–1989)
  2. Adamson University (Juris Doctor, 1989–1994)
- The form should show **both** on page 1
- Currently, only the first (or last, depending on sort order) is rendered

**Expected Fix**:
- Modify P1.jrxml to have **multiple rows for COLLEGE** (e.g., `III.EB_College1_School`, `III.EB_College2_School`, etc.)
- In ReportsController, iterate through all college entries:
  ```java
  List<EducationalBackground> collegeList = eduList.stream()
    .filter(e -> e.getDegreeLevel().getLevelCode().equals("COLLEGE"))
    .collect(Collectors.toList());
  for (int i = 0; i < collegeList.size(); i++) {
    EducationalBackground college = collegeList.get(i);
    map.put("III.EB_College" + (i+1) + "_School", getDisplayValue(college.getSchool().getSchoolName()));
    map.put("III.EB_College" + (i+1) + "_Degree", getDisplayValue(college.getDegreeCourse().getCourseName()));
    // ... rest of fields
  }
  ```
- Similarly for GRADUATE STUDIES (Tirado has **3 graduate entries**)

**Validation**:
1. Create/import an employee with 2+ COLLEGE entries (e.g., two law degrees or MBA + another degree)
2. Generate PDS PDF
3. Verify **both entries appear** on page 1, Educational Background section, COLLEGE rows
4. Repeat for GRADUATE STUDIES (create employee with 3+ graduate degrees)

---

### MEDIUM PRIORITY (P2)

#### P2-1: Verify Work Experience Part-Time Section

**What to Check**:
- Open: `src/main/resources/jasper/reports/PDS2025_P2.jrxml`
- Search for: "PART-TIME EMPLOYMENT" text or section separator
- Also check: `src/main/java/com/ian/web/employee/workexperience/WorkExperience.java` for a `partTime` boolean flag

**Context**:
- The CSC Form 212 includes a **separate "PART-TIME EMPLOYMENT" subsection** at the bottom of the Work Experience section.
- Gloria Tirado's sample PDS shows 2 part-time entries:
  - Associate Professor V (University of Makati, 601/unit)
  - Associate Professorial Lecturer I (Universidad de Manila, 501/unit)
- The current system may not distinguish between full-time and part-time entries, rendering them all in a single work experience table.

**Expected Fix**:
- If `WorkExperience` lacks a `partTime` flag:
  - Add `boolean partTime` field to the entity
  - Create migration: `ALTER TABLE work_experience ADD COLUMN is_part_time TINYINT DEFAULT 0;`
  - Update the UI form to have a "Part-time Employment" checkbox
- If P2.jrxml lacks part-time section:
  - Add Jasper parameters and a separate table/section for part-time entries
  - Populate from ReportsController: filter work experience by `isPartTime` true vs. false

**Validation**:
- Create an employee with both full-time and part-time work entries
- Generate PDS; verify part-time entries appear in a separate "PART-TIME EMPLOYMENT" section (if applicable per the form)
- Compare against `pds-sample-tirado.md` which shows part-time entries in a separate section

---

#### P2-2: Audit L&D Non-Numeric Hour Counts

**What to Check**:
- Open: `src/main/java/com/ian/web/employee/learninganddevelopment/LearningAndDevelopment.java`
- Check: `noHours` field type (likely `Integer`)
- Also check: Any `hoursDisplay` or `hoursText` field (likely missing)

**Context**:
- The LearningAndDevelopment entity stores `noHours` as `Integer` (numeric only).
- Real-world L&D entries sometimes have text-based hour counts:
  - "8-week online programme"
  - "6-month online program"
  - "120 hours"
  - Single-day seminars: "4 hours"
- The current form input is likely a single numeric field, which **cannot represent text-based durations**.

**Test Case from `pds-sample-tirado.md`**:
- Tirado has L&D entries with:
  - "32 hours" (numeric, OK)
  - "8-week online programme" (text, problematic)
  - "6-month online program" (text, problematic)

**Expected Fix**:
- Add to LearningAndDevelopment entity:
  ```java
  @Column(name = "hours_display")
  private String hoursDisplay; // Override display; e.g., "8-week online programme"
  ```
- Update L&D form to have:
  - A **numeric field** for "Number of Hours"
  - A **text field** (optional) for "Hours Description Override" (if non-standard format)
- In ReportsController, prioritize hoursDisplay:
  ```java
  String hours = lad.getHoursDisplay() != null ? lad.getHoursDisplay() : String.valueOf(lad.getNoHours());
  map.put("VII.LAD_Number_Of_HoursN", hours);
  ```

**Validation**:
1. Create/edit an L&D entry with text-based hours (e.g., "8-week online programme")
2. Generate PDS
3. Verify the text appears on page 3, L&D section (Item 30, "Number of Hours" column)
4. Also test with numeric hours to ensure backward compatibility

---

#### P2-3: Verify `I.PI_Sex_At_Birth_M` and `I.PI_Sex_At_Birth_F` Parameters

**What to Check**:
- Open: `src/main/resources/jasper/reports/PDS2025_P1.jrxml`
- Search for: `I.PI_Sex_At_Birth_M` and `I.PI_Sex_At_Birth_F` parameters
- Also check ReportsController for: how gender/sex is populated

**Context**:
- The CSC Form 212 (2025) uses **checkboxes** for Sex at Birth (Male ☐ / Female ☐).
- The Jasper template likely has two boolean parameters: one for Male (show ☐ or ☒), one for Female.
- The Employee entity stores `gender` as a String (likely "M", "F", "MALE", "FEMALE", etc.).

**Expected Check**:
- Verify the jrxml correctly maps:
  ```xml
  <parameter name="I.PI_Sex_At_Birth_M" class="java.lang.Boolean"/>
  <parameter name="I.PI_Sex_At_Birth_F" class="java.lang.Boolean"/>
  ```
- Verify ReportsController populates:
  ```java
  String gender = emp.getGender();
  map.put("I.PI_Sex_At_Birth_M", "M".equalsIgnoreCase(gender) || "MALE".equalsIgnoreCase(gender));
  map.put("I.PI_Sex_At_Birth_F", "F".equalsIgnoreCase(gender) || "FEMALE".equalsIgnoreCase(gender));
  ```

**Validation**:
- Generate PDS for a male employee; verify Male checkbox is marked ☒, Female is empty ☐
- Generate PDS for a female employee (e.g., Tirado); verify Female checkbox is marked ☒, Male is empty ☐

---

### INFORMATIONAL PRIORITY (P3)

#### P3-1: Verify Civil Status Checkbox Handling

**What to Check**:
- Verify all civil status values are correctly mapped to checkboxes on page 1
- Check ReportsController for: `I.PI_Civil_Status_Single`, `I.PI_Civil_Status_Married`, `I.PI_Civil_Status_Widowed`, `I.PI_Civil_Status_Separated`, `I.PI_Civil_Status_Others`, `I.PI_Civil_Status_Others_Text`

**Validation**:
- Generate PDS for employees with each civil status; verify correct checkbox is marked

---

#### P3-2: Audit `email2` and `mobileNo2` Fields

**What to Check**:
- The Employee entity has: `email1`, `email2`, `mobileNo1`, `mobileNo2`
- The CSC Form 212 (2025) shows only one email and one mobile number field (Items 21 and 20)
- Verify: Does the Jasper report map only `email1` and `mobileNo1`, or does it attempt to show both?

**Current Likely Behavior**:
- The form UI collects both `email2` and `mobileNo2`
- The Jasper report only outputs `email1` and `mobileNo1` (missing the secondary fields)
- This means secondary contact info is collected but never displayed on the PDF

**Decision**:
- This is a **non-issue** for CSC compliance (the form only requires one email/mobile)
- But if the system wants to capture secondary info, it should be documented or omitted from the UI form

---

#### P3-3: Verify Long Field Text Wrapping in Jasper

**What to Check**:
- Jasper text fields that might exceed box width:
  - Position titles (e.g., "Acting Chief Administrative Officer, Concurrent HRM Section" — 58 chars)
  - Agency names (e.g., "Social Security System" — fits)
  - Organization names (e.g., "University of Heidelberg Institute of Public Health" — 52 chars)
  - L&D program titles (often 50+ chars)

**Validation**:
- Generate PDS for Tirado (known long position titles)
- Visually inspect: Do long titles wrap correctly within their Jasper text boxes, or do they overflow?
- If overflow: Adjust Jasper text field properties (font size, wrapping mode, box height)

---

## Evaluation Workflow

**Step 1: Document Reading (30 min)**
1. Read `csc-form-212-2025-reference.md` — become familiar with every form field and requirement
2. Read `pds-sample-tirado.md` — understand real-world data patterns and edge cases
3. Return to this prompt for context

**Step 2: File Inspection (1–2 hours)**
- Inspect the files listed in Section III (System Overview) to verify current state:
  - ReportsController.java (methods: populateMapReport2025_P1/P2/P3/P4)
  - PDS2025_P1.jrxml, P2, P3, P4 (check parameter declarations)
  - Employee.java and related entity files
  - Migration files (verify schema changes)

**Step 3: Test PDF Generation (1 hour)**
- Run the application locally
- Create or import a test employee (ideally matching Tirado's profile)
- Navigate to: `GET /viewPds/{employeeId}`
- Save the generated PDF
- Open in Adobe Reader or similar; visually compare against `csc-form-212-2025-reference.md` and `pds-sample-tirado.md`

**Step 4: Gap & Bug Identification (2–4 hours)**
- Cross-reference visual inspection against the Priority list above (P0, P1, P2, P3)
- Document findings (what's missing, what's broken, what's partially implemented)

**Step 5: Remediation Plan (1–2 hours)**
- Prioritize bugs by priority level (P0 first, then P1, etc.)
- For each bug, determine:
  - Root cause (missing parameter, wrong mapping, missing field, etc.)
  - Required code changes (file, method, lines)
  - Required Jasper template changes (if any)
  - Required database migration (if any)
  - Verification steps

**Step 6: Implementation** (varies)
- Execute fixes in priority order
- Re-test PDF generation after each fix
- Verify no regressions in other PDS sections

---

## Acceptance Criteria

A PDS is **compliant with CSC Form 212 (2025)** when:

### Page 1 ✓
- [ ] All Personal Information fields (Items 1–21) present and correctly formatted
- [ ] UMID ID (Item 10) and PhilSys PSN (Item 13) both visible
- [ ] Date format is dd/mm/yyyy throughout
- [ ] Sex at Birth uses checkboxes (not dropdown)
- [ ] Maiden name visible for married females
- [ ] All Family Background fields (Items 22–25) present; deceased parent handled
- [ ] All Educational Background entries present (multiple COLLEGE and GRADUATE STUDIES rows supported)
- [ ] No GSIS BP/Policy No. fields shown (2025 removal)
- [ ] Professional appearance (no red annotation overlays)

### Page 2 ✓
- [ ] Civil Service Eligibility entries render correctly (max 7 rows)
- [ ] Work Experience entries render correctly (max 28 rows + part-time subsection)
- [ ] "PRESENT" shown for current employment end dates
- [ ] Monthly salary, grade, appointment status all present
- [ ] Dates in dd/mm/yyyy format

### Page 3 ✓
- [ ] Voluntary Work entries render (max 7 rows)
- [ ] L&D entries render (max 21 rows); supports both numeric and text-based hours
- [ ] Other Information (Special Skills, Distinctions, Memberships) render
- [ ] Long field text wraps correctly without overflow

### Page 4 ✓
- [ ] Questions 34–40 all present with Yes/No checkboxes
- [ ] If-Yes details fields render correctly
- [ ] References (max 3) render with position title column
- [ ] Profile photo visible (4.5 cm x 3.5 cm)
- [ ] Government-issued ID with combined date/place field
- [ ] Signature, date, thumbmark sections present
- [ ] Oath section complete

### Cross-Cutting ✓
- [ ] All dates consistent (dd/mm/yyyy)
- [ ] No data loss (all form-captured data appears on report)
- [ ] "N/A" shown for empty optional fields (not blank)
- [ ] No errors/warnings in Jasper compilation
- [ ] PDF loads without corruption in standard PDF readers
- [ ] Tested against sample data (Tirado PDS)

---

## Known Issues & Gaps (Pre-Evaluation)

Based on codebase analysis, these issues are **expected to be found** and need remediation:

| Priority | Issue | Component | Impact |
|----------|-------|-----------|--------|
| P0 | `I.PI_MaidenName` parameter missing from P1.jrxml | PDS2025_P1.jrxml | Maiden names do not appear on PDF |
| P0 | Annotation overlays may be baked into P1 template | PDS2025_P1.jrxml/.jasper | Unprofessional appearance with red marks |
| P1 | `positionTitle` not mapped for references (Item 41) | ReportsController + P4.jrxml | References incomplete on page 4 |
| P1 | Government ID date not concatenated with place | ReportsController | Date and place separated or date missing on Item 42 |
| P1 | Multiple COLLEGE/GRADUATE entries not supported | ReportsController + P1.jrxml | Only first entry per level renders; others dropped |
| P2 | Part-time employment may not have separate section | WorkExperience entity + P2.jrxml | Part-time entries mixed with full-time |
| P2 | L&D hours field is numeric only; text-based durations unsupported | LearningAndDevelopment entity | "8-week online programme" cannot be stored |
| P3 | `email2`/`mobileNo2` collected but never shown on report | Employee entity + P1.jrxml | Non-issue for CSC (form only requires one of each) |

---

## Next Steps

After evaluation is complete:

1. **Generate Prioritized Bug List** — Enumerate all found issues with severity
2. **Create Implementation Tasks** — One task per bug, with AC (acceptance criteria)
3. **Execute Fixes** — Implement in priority order (P0 → P1 → P2 → P3)
4. **Regression Testing** — Ensure no side effects after each fix
5. **Final Validation** — Re-test Tirado's PDS against `pds-sample-tirado.md`; verify 100% match

---

## Contact & Questions

If clarifications are needed during evaluation:
- Refer to `csc-form-212-2025-reference.md` for form specification questions
- Refer to `pds-sample-tirado.md` for edge-case/data pattern questions
- Refer to the Jasper Reports 6.x documentation for template/parameter questions
- Refer to Spring Boot + Thymeleaf docs for controller/form questions
