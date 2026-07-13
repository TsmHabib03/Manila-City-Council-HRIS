# Claude Code Prompt — Fix the Appointments Module (HRIS_WEB01)

## Context

This is a Spring Boot + Thymeleaf HRIS web application. The **Appointments** module is broken and currently non-functional. Your job is to fully fix it end-to-end.

---

## What is Already There (Do NOT Delete)

| File | Purpose |
|------|---------|
| `src/main/java/com/ian/web/employee/appointment/Appointment.java` | JPA entity — correct fields already defined |
| `src/main/java/com/ian/web/employee/appointment/AppointmentRepository.java` | JPA repo — already has `findByEmployeeId(long)` |
| `src/main/java/com/ian/web/employee/appointment/AppointmentController.java` | GET-only controller — needs POST endpoints added |
| `src/main/resources/templates/employee/appointments/employee-list-appointments.html` | Employee picker list — minor bug (see below) |
| `src/main/resources/templates/employee/appointments/employee-appointment-record.html` | **Completely wrong** — copy-paste of Service Record template, must be rebuilt |

---

## Bug List (Fix All of These)

### BUG 1 — `employee-appointment-record.html` is the wrong template
The file was copy-pasted from the Service Record module and never updated. It references service record fields everywhere. It must be rewritten to match the `Appointment` entity fields:

```
plantillaNo, signingDate, pageNo, positionTitle (FK→PositionTitle),
status, salary (Double), vice, statusOfSepeparation, statusOfAppointment,
salaryGrade (int), stepInc (int), entranceDate, eligibility,
highestEducAttainment, officeAssignment, remarks, district, experience, training
```

The modal title currently says "Service Record Information" — change to "Appointment Record".
The form action currently submits to `/addServiceRecord` — change to `/addAppointment`.

### BUG 2 — Model attribute name mismatch
In `AppointmentController.viewEmployeeAppointments()`, the model puts `appointmentRecordList` but the template iterates `${serviceRecordList}` — which is null/empty, so the table always shows nothing.

Fix: The template must iterate `${appointmentRecordList}`, not `${serviceRecordList}`.

### BUG 3 — No POST endpoint to save or update appointments
`AppointmentController` only has a `@GetMapping`. There is no save or update endpoint.

Add these two endpoints:
- `@PostMapping("/addAppointment")` — insert new Appointment, redirect back with SUCCESS message
- `@PostMapping("/updateAppointment")` — update existing Appointment by ID, redirect back with SUCCESS message

Both should redirect to `/appointments/{employee.id}/{employee.empHashCode}?msg=SUCCESS` (or use `RedirectAttributes`).

### BUG 4 — No delete endpoint
Add `@GetMapping("/deleteAppointment/{id}")` that deletes by ID then redirects back.

### BUG 5 — `showMode` check is always wrong
The controller checks `request.getServletPath().startsWith("/profile")` but the URL is `/appointments/{id}/{hash}`, so `showMode` is always `"HRADMIN"`. This check should be either removed or corrected. For now, remove the `showMode` logic if it is not used in the template yet.

### BUG 6 — DataTable selector wrong in `employee-list-appointments.html`
The script at the bottom calls `$('#table-list').DataTable({})` but the table element has class `datatable-highlight` and **no id**. Either add `id="table-list"` to the `<table>` tag, or change the selector to `$('.datatable-highlight').DataTable({})`.

---

## Reference — How Other Working Modules Are Structured

Look at the **Service Record** module as your reference for patterns:
- `src/main/java/com/ian/web/employee/servicerecord/ServiceRecord.java`
- `src/main/java/com/ian/web/employee/servicerecord/ServiceRecordController.java`
- `src/main/resources/templates/employee/service-record/employee-service-record.html`

The appointments module should follow the exact same GET/POST/redirect pattern, the same modal structure, and the same table column layout — just using Appointment fields instead.

---

## Appointment Form Fields to Show in the Modal

Build the Add/Edit modal with these fields (grouped sensibly in rows):

| Field | Type | Required |
|-------|------|----------|
| Plantilla No | text | yes |
| Position Title | dropdown (`${positionTitleList}`) | yes |
| Status of Appointment | text | yes |
| Status of Separation | text | no |
| Salary | number (decimal) | yes |
| Salary Grade | number (int) | yes |
| Step Increment | number (int) | yes |
| Signing Date | date | yes |
| Entrance Date | date | yes |
| Eligibility | text | no |
| Highest Educational Attainment | text | no |
| Office Assignment | text | no |
| Vice | text | no |
| District | text | no |
| Experience | text | no |
| Training | text | no |
| Remarks | textarea | no |
| Page No | number (int) | no |

The hidden fields must include `id`, `employee.id`, and `employee.empHashCode` (needed for redirect).

---

## Table Columns to Show in the List

Show these columns in the appointment records table:
- Actions (View/Edit button)
- Plantilla No
- Position Title
- Status of Appointment
- Salary
- Salary Grade
- Signing Date
- Entrance Date

---

## CSRF

This is a Spring Security app. All POST forms need the CSRF token. Follow the pattern in other Thymeleaf templates — `th:action="@{/addAppointment}"` on the form tag automatically includes it.

---

## What to Deliver

1. **Modified** `AppointmentController.java` — add POST save, POST update, GET delete endpoints
2. **Rebuilt** `employee-appointment-record.html` — correct modal + table using Appointment fields
3. **Fixed** `employee-list-appointments.html` — DataTable selector bug fixed
4. Do **not** change `Appointment.java` or `AppointmentRepository.java` unless you find a genuine schema issue.
5. Test by running the app and navigating to `/hrisp/appointments` → pick an employee → add a record → verify it appears in the table.

---

## Notes

- The app runs at `http://localhost:8080/hrisp`
- Spring Boot with JPA/Hibernate — entity fields map to DB columns automatically
- Thymeleaf templates use Bootstrap 4 modal pattern (same as other modules)
- Keep the blue colour scheme consistent with the rest of the app (`btn-primary`, `bg-primary`)
