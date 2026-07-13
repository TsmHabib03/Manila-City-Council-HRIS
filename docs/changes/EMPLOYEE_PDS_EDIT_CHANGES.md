# Employee PDS Self-Edit Feature — Change Summary

**Date:** 2026-05-25  
**Branch:** fix/clearance-appointments-20260524  
**Purpose:** Allow employees to edit their own Personal Data Sheet. Previously only HR/Admin could edit PDS; employees could only view and export their own.

---

## What Changed

### Problem
The PDS edit restriction was purely JavaScript-based. When an employee clicked "My Personal Data Sheet", the URL used `showMode=PROFILE`. A JavaScript block in every PDS template detected this and:
- Disabled all input fields and select dropdowns
- Hid the Save button (`$('#submitBtn').hide()`)
- Hid the Add Record buttons on sub-pages

The backend had **no access control** on the save endpoints — any authenticated user could POST to them.

### Solution
1. **Templates**: Removed the input-disabling and button-hiding JS from the PROFILE mode block. The nav highlighting (showing "My Account" as active) is preserved.
2. **Backend**: Added ownership checks on all POST save endpoints — employees can only save their own records; Admin can save any record.
3. **Security**: The ownership check also prevents privilege escalation (employees cannot change their own `userType` through form manipulation).

---

## Files Changed

### 1. `src/main/resources/templates/employee/pds/personnal-info.html`

**What changed:** Removed input-disabling and button-hiding from the PROFILE JS block.

**Before:**
```javascript
if(showMode === 'PROFILE'){
    $(".nav-item-submenu").removeClass("nav-item-expanded nav-item-open");
    $("#myAccountNav").addClass("nav-item-expanded nav-item-open");
    $("#myPersonalInfoNav").addClass("active");

    // (various commented-out lines)

    $('input').prop('readonly', true);
    $('select').prop('disabled', true);
    $('input, select').removeClass("required-field-hrisp");

    $('#returnBtn').hide();
    $('#submitBtn').hide();
    $('#updatePhotoBtn').hide();
    $('#cameraArea').hide();

    $("#showMode").val("PROFILE");
} else {
```

**After:**
```javascript
if(showMode === 'PROFILE'){
    $(".nav-item-submenu").removeClass("nav-item-expanded nav-item-open");
    $("#myAccountNav").addClass("nav-item-expanded nav-item-open");
    $("#myPersonalInfoNav").addClass("active");

    $('#returnBtn').hide();

    $("#showMode").val("PROFILE");
} else {
```

---

### 2–11. All 10 PDS Sub-Page Templates

Same pattern applied to each file. In each template, the PROFILE block previously disabled all inputs, hid the Add/Submit buttons. After the change it only sets the nav highlight.

Files changed (same before/after pattern as above, adjusted for each page's nav ID):

| File | Nav ID activated |
|------|-----------------|
| `src/main/resources/templates/employee/pds/educational-background.html` | `#myPersonalInfoNav` |
| `src/main/resources/templates/employee/pds/family-background.html` | `#myPersonalInfoNav` |
| `src/main/resources/templates/employee/pds/eligibility.html` | `#myPersonalInfoNav` |
| `src/main/resources/templates/employee/pds/gov-issued-id.html` | `#myPersonalInfoNav` |
| `src/main/resources/templates/employee/pds/learning-development.html` | `#myPersonalInfoNav` |
| `src/main/resources/templates/employee/pds/other-info.html` | `#myPersonalInfoNav` |
| `src/main/resources/templates/employee/pds/other-info-question.html` | `#myPersonalInfoNav` |
| `src/main/resources/templates/employee/pds/references.html` | `#myPersonalInfoNav` |
| `src/main/resources/templates/employee/pds/voluntary-work.html` | `#myPersonalInfoNav` |
| `src/main/resources/templates/employee/pds/work-experience.html` | `#myPersonalInfoNav` |

**Before (all sub-pages):**
```javascript
if(showMode === 'PROFILE'){
    $(".nav-item-submenu").removeClass("nav-item-expanded nav-item-open");
    $("#myAccountNav").addClass("nav-item-expanded nav-item-open");
    $("#myPersonalInfoNav").addClass("active");

    $('#addButton').hide();
    $('#submitBtn').hide();
    $('#updatePhotoBtn').hide();
    $('#cameraArea').hide();

    $('textarea').prop('readonly', true);
    $('input').prop('readonly', true);
    $('select').prop('disabled', true);
    $('input, select').removeClass("required-field-hrisp");
    // other-info-question.html also had:
    // $('input[type="radio"]').prop('disabled', true);

    $("#showMode").val("PROFILE");
} else {
```

**After (all sub-pages):**
```javascript
if(showMode === 'PROFILE'){
    $(".nav-item-submenu").removeClass("nav-item-expanded nav-item-open");
    $("#myAccountNav").addClass("nav-item-expanded nav-item-open");
    $("#myPersonalInfoNav").addClass("active");

    $("#showMode").val("PROFILE");
} else {
```

---

### 12. `src/main/java/com/ian/web/employee/EmployeeController.java`

**What changed:** Added ownership check on `POST /addEmployee /editEmployee /saveProfile` — inserted after the edit-mode block (after setting `employeeOldRecord`).

**Code added (inserted before `if (errors.hasErrors())`)**:
```java
// Access control: only Admin can add new employees or edit others; employees may only edit themselves
Employee actorObj = (Employee) request.getSession().getAttribute("actorObj");
boolean isAdmin = actorObj != null && "ROLE_ADMIN".equals(actorObj.getUserType());
boolean isOwnRecord = actorObj != null && employee.getId() > 0 && actorObj.getId() == employee.getId();

if (!isAdmin && !isOwnRecord) {
    redirect.addFlashAttribute("msg", new UXMessage("ERROR", "Access denied."));
    return "redirect:/dashboard";
}

// Prevent privilege escalation: preserve original userType for non-admins
if (!isAdmin && isOwnRecord && employeeOldRecord != null) {
    employee.setUserType(employeeOldRecord.getUserType());
}
```

**Imports needed:** None — `Employee`, `UXMessage`, `HttpServletRequest` are already imported.

---

### 13–22. 10 PDS Sub-Controllers

Ownership check added to every sub-controller's POST save method, inserted **before** `String showMode = entity.getShowMode();`.

**Pattern added to each controller:**
```java
// Ownership check
Employee actorObj = (Employee) request.getSession().getAttribute("actorObj");
boolean isAdmin = actorObj != null && "ROLE_ADMIN".equals(actorObj.getUserType());
boolean isOwnRecord = actorObj != null && actorObj.getId() == <entity>.getEmployee().getId();
if (!isAdmin && !isOwnRecord) {
    redirect.addFlashAttribute("msg", new UXMessage("ERROR", "Access denied."));
    return "redirect:/dashboard";
}
```

| File | Entity variable | Method |
|------|----------------|--------|
| `src/main/java/com/ian/web/employee/educationalbg/EducationalBackgroundController.java` | `educBackground` | `saveEducationalBg` |
| `src/main/java/com/ian/web/employee/familybg/FamilyBgController.java` | `familyBg` | `saveFamilyBg` |
| `src/main/java/com/ian/web/employee/eligibility/CivilServiceEligibilityController.java` | `civilServiceEligibility` | `saveFamilyBg` |
| `src/main/java/com/ian/web/employee/govermentid/GovermentIssuedIdController.java` | `govermentIssuedId` | `saveFamilyBg` |
| `src/main/java/com/ian/web/employee/learning/LearningAndDevelopmentController.java` | `learningAndDevelopment` | `saveFamilyBg` |
| `src/main/java/com/ian/web/employee/otherinfo/OtherInfoController.java` | `otherInfo` | `getRecord` |
| `src/main/java/com/ian/web/employee/otherinfoquestion/OtherInfoQuestionController.java` | `otherInfoQuestion` | `saveFamilyBg` |
| `src/main/java/com/ian/web/employee/references/EmpReferencesController.java` | `references` | `getRecord` |
| `src/main/java/com/ian/web/employee/voluntary_workexperience/VoluntaryWorkController.java` | `voluntaryWork` | `saveFamilyBg` |
| `src/main/java/com/ian/web/employee/workexperience/WorkExperienceController.java` | `workExperience` | `saveFamilyBg` |

**Imports needed:** None — `Employee`, `UXMessage`, `HttpServletRequest` already imported in all 10 controllers.

---

## No New Files Created

All changes are modifications to existing files. No new Java classes, templates, or configuration files were added.

---

## How to Apply to Linux Server

### Step 1: Pull or copy the changed files

If using git:
```bash
cd ~/hrisp-web01
git pull origin fix/clearance-appointments-20260524
```

Or copy files manually. The changed files are:
- `src/main/resources/templates/employee/pds/personnal-info.html`
- `src/main/resources/templates/employee/pds/educational-background.html`
- `src/main/resources/templates/employee/pds/family-background.html`
- `src/main/resources/templates/employee/pds/eligibility.html`
- `src/main/resources/templates/employee/pds/gov-issued-id.html`
- `src/main/resources/templates/employee/pds/learning-development.html`
- `src/main/resources/templates/employee/pds/other-info.html`
- `src/main/resources/templates/employee/pds/other-info-question.html`
- `src/main/resources/templates/employee/pds/references.html`
- `src/main/resources/templates/employee/pds/voluntary-work.html`
- `src/main/resources/templates/employee/pds/work-experience.html`
- `src/main/java/com/ian/web/employee/EmployeeController.java`
- `src/main/java/com/ian/web/employee/educationalbg/EducationalBackgroundController.java`
- `src/main/java/com/ian/web/employee/familybg/FamilyBgController.java`
- `src/main/java/com/ian/web/employee/eligibility/CivilServiceEligibilityController.java`
- `src/main/java/com/ian/web/employee/govermentid/GovermentIssuedIdController.java`
- `src/main/java/com/ian/web/employee/learning/LearningAndDevelopmentController.java`
- `src/main/java/com/ian/web/employee/otherinfo/OtherInfoController.java`
- `src/main/java/com/ian/web/employee/otherinfoquestion/OtherInfoQuestionController.java`
- `src/main/java/com/ian/web/employee/references/EmpReferencesController.java`
- `src/main/java/com/ian/web/employee/voluntary_workexperience/VoluntaryWorkController.java`
- `src/main/java/com/ian/web/employee/workexperience/WorkExperienceController.java`

### Step 2: Rebuild and deploy
```bash
cd ~/hrisp-web01
mvn clean install -DskipTests -Dspring.profiles.active=prod
sudo systemctl restart hrisp
```

### Step 3: Verify
```bash
sudo systemctl status hrisp
journalctl -u hrisp -f
```

---

## Verification Steps

1. Log in as an **employee** (non-admin) account
2. Click **My Personal Data Sheet** from the sidebar nav
3. Confirm: all form fields are editable, **Save Info** button is visible
4. Edit a field (e.g., address) and click **Save Info**
5. Confirm: page redirects back with "Employee Record Successfully Updated" message
6. Click **Educational Background** in the PDS sidebar
7. Confirm: **Add Record** button is visible, records can be added/edited
8. Repeat spot-check for Family Background, Work Experience
9. Test PDF export: click **Export PDF** — confirm it generates the PDS PDF
10. Log in as **Admin** — confirm admin workflow is unchanged (can still view and edit any employee's PDS)

---

## Access Control Summary (After This Change)

| User Role | View Own PDS | Edit Own PDS | Export Own PDS | Edit Other Employee PDS |
|-----------|-------------|-------------|---------------|------------------------|
| **Employee** | ✅ YES | ✅ YES (NEW) | ✅ YES | ❌ NO (denied at backend) |
| **Admin** | ✅ YES | ✅ YES | ✅ YES | ✅ YES |

---

*Generated: 2026-05-25 — For deployment to https://manilacitycouncil.com*
