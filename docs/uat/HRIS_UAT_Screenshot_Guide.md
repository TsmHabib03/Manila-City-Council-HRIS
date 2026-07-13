# HRIS Employee UAT - Screenshot Update Guide

## Overview
You need to replace the Admin UI screenshots in sections 7.2-7.7 of the DOCX file with Employee Dashboard screenshots. Below are the steps and URLs to capture the required screenshots.

## Screenshots Required

### Login Credentials
- **Username:** sabao02111965
- **Password:** 123
- **URL:** http://localhost:8080/hrisp/login

### Sections to Capture

#### 7.2 Change Password
- **URL:** http://localhost:8080/hrisp/change-password/4
- **Expected:** Form with fields for Old Password, New Password, Confirm New Password

#### 7.3 My Personal Data Sheet (PDS)
- **URL:** http://localhost:8080/hrisp/employee/4/PROFILE/56ec21099f5f4879b190
- **Expected:** Personal Information form with fields for employee details
- **Subsections:** The page shows a dropdown with:
  - Personal Information
  - Family Background
  - Educational Background
  - Eligibility
  - Work Experience
  - Voluntary Work
  - Learning & Development
  - Other Information
  - References
  - Government ID

#### 7.4 My Appointments
- **URL:** http://localhost:8080/hrisp/my-appointments/4/56ec21099f5f4879b190
- **Expected:** "My Appointment History" table with columns for Plantilla No, Position Title, Status of Appointment, Salary, Salary Grade, Signing Date, Entrance Date

#### 7.5 My Clearance
- **URL:** http://localhost:8080/hrisp/myclearance/4/56ec21099f5f4879b190
- **Expected:** "My Clearance List" table with columns for Filling Date, Purpose, Effectivity Date, Status

#### 7.6 My 201 Files
- **URL:** http://localhost:8080/hrisp/my201files/4/56ec21099f5f4879b190
- **Expected:** "201 File Information" with search functionality and columns for Document Type and Remarks

#### 7.7 My Service Record
- **URL:** http://localhost:8080/hrisp/my-service-record/4/56ec21099f5f4879b190
- **Expected:** "My Service Record List" table with columns for Date From, Date To, Present, Designation

## Step-by-Step Instructions

### Step 1: Log In
1. Open http://localhost:8080/hrisp/login in your browser
2. Enter username: sabao02111965
3. Enter password: 123
4. Click "Sign in"

### Step 2: Capture Screenshots
For each URL above:
1. Navigate to the URL
2. Wait for the page to load completely
3. Use your browser's screenshot tool:
   - **Chrome/Edge:** Press `Ctrl+Shift+S` to take a screenshot
   - Or right-click > "Take screenshot"
4. Save the screenshot with the corresponding section name

### Step 3: Update the Word Document
1. Open "HRIS_WEB01_Employee_UAT-updated.docx"
2. Navigate to section 7.2 (Change Password)
3. Replace the old admin UI screenshot with the new Employee dashboard screenshot
4. Repeat for sections 7.3 through 7.7
5. Save the document

## Alternative: Automated Screenshot Capture

If your system has Python and Selenium installed, you can use the following script to automate the capture:

```bash
pip install selenium pillow
python3 capture_employee_uat_screenshots.py
```

This will save all screenshots to: `C:\Users\Habib\IdeaProjects\hrisp-web01\uat_screenshots_employee\`

## Troubleshooting

- **Login fails:** Ensure the HRIS system is running on localhost:8080
- **Page doesn't load:** Wait a few seconds for JavaScript to render
- **Subsections not visible:** Scroll down on section 7.3 to see all PDS subsections

## Notes

- The Employee Dashboard is now fully functional for all sections
- Screenshots should show the entire page content including the sidebar menu
- Make sure to log in with the provided credentials before capturing screenshots
- The dashboard differs from the Admin UI in layout and available options

## Support

If you encounter any issues:
1. Check that localhost:8080 is accessible
2. Verify the login credentials are correct
3. Clear browser cache if pages don't load properly
4. Try a different browser if issues persist
