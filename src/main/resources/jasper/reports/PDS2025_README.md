# PDS 2025 (CS Form No. 212 Revised 2025) — finishing the rebuild

This folder holds the new four-page PDS report for the 2025 CSC form. The Java
side (entity columns, edit form, controller mapping, endpoint switch, build
plugin) is done. Two manual steps remain — both require Jaspersoft Studio
Community 6.20.x (must match the runtime version 6.20.0 in `pom.xml`).

## What's already wired

| Concern | Where |
| --- | --- |
| New entity fields | `Employee.umidNo`, `Employee.philsysNo` |
| DB migration (prod) | `src/main/resources/migrations/2025-pds-form.sql` |
| Edit-form inputs | `templates/employee/pds/personnal-info.html` (UMID, PhilSys, "Sex at Birth", "Solo Parent") + `other-info-question.html` (item 40 RA wording) |
| Controller endpoint | `GET /viewPds/{employeeId}` → loads `PDS2025_P1..P4.jasper` and calls `populateMapReport2025_P1..P4` |
| Date format | New helper `formatDateDdMmYyyy` renders `dd/MM/yyyy` for all 2025 date fields |
| Exam-date assembly | New helper `formatExamDateDdMmYyyy(CivilServiceEligibility)` builds dd/MM/yyyy from `examDay`/`examMonth`/`examYear` |
| Build-time compile | `pom.xml` adds the `com.alexnederlof:jasperreports-plugin` so `.jrxml` → `.jasper` happens on every `mvn package` |
| .jrxml scaffolds | `PDS2025_P1.jrxml`..`P4.jrxml` (this folder) — declare all required parameters; visual layout left blank for Studio |

The legacy `PDS1.jasper`..`PDS4.jasper` + `PDS1.png`..`PDS4.png` are untouched
and remain as a one-line-revert rollback target (point `viewPdsNew` back at
the old paths + map methods).

## Manual step 1 — Background PNGs (already generated)

The 4 background PNGs have already been rendered from the authoritative source
`ANNEX H-1 - CS Form No. 212 Revised 2025 - Personal Data Sheet.pdf` and saved
to `src/main/resources/static/images/PDS2025_P1..P4.png` at 150 DPI.

- Page size: **Letter (8.5 × 11 in)** — confirmed from the PDF metadata
- PNG dimensions: 1275 × 1650 px each
- `.jrxml` page dimensions are set to `pageWidth="612" pageHeight="792"` to match

To regenerate (if the PDF is ever updated), run from the repo root:

```bash
python -m pip install --user pymupdf  # one-time
python -c "import fitz, os; doc=fitz.open('ANNEX H-1 - CS Form No. 212 Revised 2025 - Personal Data Sheet.pdf'); mat=fitz.Matrix(150/72,150/72); [doc.load_page(i).get_pixmap(matrix=mat, alpha=False).save(f'src/main/resources/static/images/PDS2025_P{i+1}.png') for i in range(doc.page_count)]"
```

The controller already references these exact paths
(`ReportsController.populateMapReport2025_P1..P4`).

## Manual step 2 — Place text fields in Jaspersoft Studio

For each of `PDS2025_P1.jrxml` ... `PDS2025_P4.jrxml`:

1. Open the file in Jaspersoft Studio Community 6.20.x.
2. The `<background>` band already references `$P{FormBg}` (P2: `$P{FormBg2}`,
   etc.). The controller passes in the absolute path to the matching PNG, so
   Studio's preview should immediately show the blank form behind the layout.
3. Drag a Text Field over each form cell. Set its expression to the matching
   parameter (already declared at the top of the file). Example for the
   Surname field on page 1:

   ```xml
   <textField>
       <reportElement x="100" y="120" width="170" height="14"/>
       <textElement><font fontName="Arial" size="10"/></textElement>
       <textFieldExpression><![CDATA[$P{I.PI_Surname}]]></textFieldExpression>
   </textField>
   ```

4. For Yes/No tick-boxes (items 34-40), use a small text field whose expression
   is `$P{VIII.OI_34_A_Yes}` — the controller emits either `"X"` or `""`.
5. The .jrxml scaffolds declare only row-1 parameters for repeating sections
   (work experience 1-28, eligibility 1-7, L&D 1-21, voluntary 1-7). Add row-N
   `<parameter>` declarations as you place row-N fields — the controller
   already emits them.
6. Studio compiles `.jasper` on save. Alternatively, run `mvn process-resources`
   to compile all .jrxml in this folder.
7. Commit both `.jrxml` (source of truth) and the regenerated `.jasper`.

## Verification

1. **Run the DB migration** (prod only — dev auto-updates):

   ```sql
   -- see src/main/resources/migrations/2025-pds-form.sql
   ALTER TABLE employee
     ADD COLUMN umid_no VARCHAR(255) NULL,
     ADD COLUMN philsys_no VARCHAR(255) NULL;
   ```

2. **Smoke test in dev:**

   ```bash
   mvn clean spring-boot:run
   ```

   Then in a browser:
   - Edit an employee. Fill in UMID, PhilSys, set Civil Status = "SOLO PARENT".
   - In Other Info Questions, answer 38B (questionSix) Yes.
   - Visit `http://localhost:8080/hrisp/viewPds/{employeeId}` and download the
     PDF.

3. **Visual diff:** open the generated PDF alongside the 2025 xlsx in two
   panes. Each field on the PDF should land on the corresponding cell of the
   form.

4. **Edge cases worth re-testing:**
   - Empty employee (all nulls) — no exceptions, blank PDF.
   - 12-children family — children rows fill correctly.
   - 28+ work-experience records — overflow handled.
   - Date format — every visible date is `dd/MM/yyyy`.

## Rollback

The legacy assets in this folder (`PDS1.jasper`..`PDS4.jasper` and
`static/images/PDS1.png`..`PDS4.png`) are untouched. To roll back, edit
`ReportsController.viewPdsNew` (around line 139) so it loads `PDS1.jasper`..
`PDS4.jasper` and calls `populateMapReport1..4` instead of the `_2025_P*`
variants. The new entity columns and edit-form inputs are nullable + additive
and can stay in place after a rollback.
