# PDS Self-Edit Bug Fix — "Mandatory Fields in RED" Error

**Date:** 2026-05-25  
**Status:** Fixed  
**Affects:** Employee self-edit of Personal Data Sheet (personnal-info.html)

---

## Background

A feature was added to allow employees to edit their own PDS (Personal Data Sheet). Previously only HR/Admin could edit PDS records. The change removed JavaScript restrictions in `PROFILE` mode that had been disabling all form inputs and hiding the Save button.

See `EMPLOYEE_PDS_EDIT_CHANGES.md` for the original feature change details.

---

## The Bug

After the feature was deployed, employees could open the PDS form and see editable fields, but clicking **Save Info** produced:

> **Error! Mandatory fields marked in RED are mandatory.**

...even when all visible personal fields were filled in.

---

## Root Cause

The form has a JS submit validator in `personnal-info.html`:

```javascript
$('#employeeForm').submit(function(event) {
    var emptyFields = [];
    $('.required-field-hrisp').each(function() {
        if ($(this).val().trim() === '') {
            emptyFields.push($(this));
        }
    });
    if (emptyFields.length > 0) {
        event.preventDefault();
        swal({ title: "Error!", text: "Mandatory fields marked in RED are mandatory.", ... });
    }
});
```

The following fields have the `required-field-hrisp` class and are **HR-managed** — employees don't fill them in:

| Field ID | Description |
|----------|-------------|
| `empNo` | Employee Number (assigned by HR) |
| `assumptiondate` | Date Assumed Position (assigned by HR) |
| `employeeStatus` | Employment Status (set by HR) |
| `positionTitle` | Position Title (set by HR) |
| `division` | Division/Department (set by HR) |

The original PROFILE-mode JS block used to strip `required-field-hrisp` from **all** fields with:
```javascript
$('input, select').removeClass("required-field-hrisp");
```
When the feature was added, this blanket removal was also removed — so validation now ran on the HR-only fields that employees never fill in, blocking form submission.

---

## The Fix

### Fix 1 — `src/main/resources/templates/employee/pds/personnal-info.html`

In the `if(showMode === 'PROFILE')` JS block, added targeted locking of HR-only fields:

```javascript
if(showMode === 'PROFILE'){
    // ... nav highlighting lines ...
    $('#returnBtn').hide();
    $("#showMode").val("PROFILE");

    // HR-only fields: lock and remove required validation so employees can submit
    $("#empNo, #assumptiondate, #plantillaNo").prop("readonly", true).removeClass("required-field-hrisp");
    $("#employeeStatus, #positionTitle, #division").prop("disabled", true).removeClass("required-field-hrisp");
} else {
```

- Text inputs (`empNo`, `assumptiondate`, `plantillaNo`) → `readonly` (value still submits)
- Select dropdowns (`employeeStatus`, `positionTitle`, `division`) → `disabled` (value does NOT submit — handled by Fix 2)

### Fix 2 — `src/main/java/com/ian/web/employee/EmployeeController.java`

In the non-admin ownership block, added restoration of HR-managed fields from the old DB record (so disabled selects don't null out DB values):

**Before:**
```java
if (!isAdmin && isOwnRecord && employeeOldRecord != null) {
    employee.setUserType(employeeOldRecord.getUserType());
}
```

**After:**
```java
if (!isAdmin && isOwnRecord && employeeOldRecord != null) {
    employee.setUserType(employeeOldRecord.getUserType());
    employee.setEmpNo(employeeOldRecord.getEmpNo());
    employee.setAssumptiondate(employeeOldRecord.getAssumptiondate());
    employee.setPlantillaNo(employeeOldRecord.getPlantillaNo());
    employee.setPositionTitle(employeeOldRecord.getPositionTitle());
    employee.setDivision(employeeOldRecord.getDivision());
    employee.setEmployeeStatus(employeeOldRecord.getEmployeeStatus());
}
```

This ensures that even if the disabled selects aren't submitted with the form, their values are restored from the database before save — preventing Hibernate from writing NULL to those columns.

---

## Field Access After Fix

| Field | Employee in PROFILE mode |
|-------|--------------------------|
| firstName, lastName, middleName | ✅ Editable, required |
| birthdate, birthPlace | ✅ Editable, required |
| gender, civilStatus, citizenship | ✅ Editable, required |
| Address fields, contact info, IDs | ✅ Editable |
| empNo, assumptiondate, plantillaNo | 🔒 Readonly (greyed out) |
| employeeStatus, positionTitle, division | 🔒 Disabled dropdown (greyed out) |

---

## Files Changed in This Fix

| File | Change |
|------|--------|
| `src/main/resources/templates/employee/pds/personnal-info.html` | Added HR-field readonly/disabled + required-class removal in PROFILE JS block |
| `src/main/java/com/ian/web/employee/EmployeeController.java` | Added HR-field value restoration from old record for non-admin saves |

---

## How to Deploy to Linux Server

```bash
cd ~/hrisp-web01
git pull origin fix/clearance-appointments-20260524
mvn clean install -DskipTests -Dspring.profiles.active=prod
sudo systemctl restart hrisp
```

Or apply changes manually via WinSCP and rebuild.

---

## Verification

1. Log in as an **employee**
2. Go to My Personal Data Sheet
3. Check: `empNo` and `assumptiondate` appear greyed out (readonly)
4. Check: `employeeStatus`, `positionTitle`, `division` dropdowns appear greyed out (disabled)
5. Edit a personal field (e.g. mobile number or residential address)
6. Click **Save Info** → should redirect back with success, NO error popup
7. Log back in as Admin, view the same employee — confirm their position/division/status are unchanged
