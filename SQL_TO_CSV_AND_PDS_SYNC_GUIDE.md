# SQL to CSV Conversion & PDS File Sync Guide
**Objective**: Convert employee data (634 records) to CSV and sync PDS files to HRIS with Leave Management module  
**Date**: June 8, 2026

---

## Part 1: SQL to CSV Conversion

### Overview
Your `hris_01 (1).sql` file is a **phpMyAdmin database dump** containing:
- **41 tables** (schema + data)
- **634 employees** and their PDS records
- **~9,818 lines** of SQL

### Tables Containing Employee PDS Data
```
Core Employee Data:
  • employee (main table with 634 records)
  
PDS Section 1 - Personal Information:
  • family_bg (family background)
  • address (residential/permanent addresses)
  
PDS Section 2 - Educational Background:
  • educational_background (school records)
  • degree_level (elementary, secondary, etc.)
  • degree_course (degree/course names)
  • school (school names)
  • academic_honors (honors/scholarships)
  
PDS Section 3 - Civil Service Eligibility:
  • civil_service_eligibility (CSE records)
  • eligibility (eligibility types)
  
PDS Section 4 - Work Experience:
  • work_experience (job positions held)
  • service_record (service records)
  
PDS Section 5 - Voluntary Work:
  • voluntary_work (volunteer positions)
  
PDS Section 6 - Learning & Development:
  • learning_development (training/seminars)
  • learning_type (training types)
  
PDS Section 7 - Other Information:
  • other_info (personal questions)
  • other_info_question (question definitions)
  • emp_references (professional references)
  • government_issued_id (ID documents)
  
Support Tables:
  • division, position_titles, employee_status, etc.
```

---

## Method 1: MySQL Command Line (Recommended for Large Files)

### Step 1: Import SQL into MySQL
```bash
# On your Linux server with MySQL
mysql -u root -p hris_01 < hris_01_\(1\).sql

# Or on Windows:
mysql -u root -p hris_01 < "hris_01 (1).sql"
```

### Step 2: Export to CSV
Use MySQL's native CSV export (fastest method):

#### Export Employee Table (Basic)
```sql
SELECT * FROM employee
INTO OUTFILE '/var/lib/mysql-files/employee_export.csv'
FIELDS TERMINATED BY ',' 
ENCLOSED BY '"'
LINES TERMINATED BY '\n';
```

#### Export with Headers (Better for CSV)
```bash
# Create a bash script to add headers
(echo "id,emp_no,first_name,last_name,middle_name,suffix,date_of_birth,..." && \
  mysql -u root -p hris_01 -e "SELECT * FROM employee;" | tail -n +2) > employee.csv
```

#### Export All PDS Tables to Individual CSVs
```bash
#!/bin/bash
# Save as export_pds_to_csv.sh

MYSQL_USER="root"
MYSQL_PASS="your_password"
MYSQL_DB="hris_01"
OUTPUT_DIR="./pds_csv_export"

mkdir -p $OUTPUT_DIR

# Tables to export
TABLES=(
  "employee"
  "family_bg"
  "educational_background"
  "civil_service_eligibility"
  "work_experience"
  "voluntary_work"
  "learning_development"
  "other_info"
  "emp_references"
  "government_issued_id"
  "service_record"
)

for TABLE in "${TABLES[@]}"; do
  echo "Exporting $TABLE..."
  mysql -u $MYSQL_USER -p$MYSQL_PASS $MYSQL_DB -e "SELECT * FROM $TABLE;" \
    > "$OUTPUT_DIR/${TABLE}.csv"
done

echo "Export complete! Files in: $OUTPUT_DIR"
```

### Step 3: Run the Export Script
```bash
chmod +x export_pds_to_csv.sh
./export_pds_to_csv.sh
```

---

## Method 2: Python Script (Better for Complex Transformations)

### Use MySQLdb or mysql-connector-python

```python
#!/usr/bin/env python3
import mysql.connector
import csv
from mysql.connector import Error

def export_table_to_csv(db_config, table_name, output_file):
    """Export single MySQL table to CSV"""
    try:
        connection = mysql.connector.connect(**db_config)
        cursor = connection.cursor()
        
        # Get column names
        cursor.execute(f"DESCRIBE {table_name}")
        columns = [row[0] for row in cursor.fetchall()]
        
        # Fetch all data
        cursor.execute(f"SELECT * FROM {table_name}")
        rows = cursor.fetchall()
        
        # Write to CSV
        with open(output_file, 'w', newline='', encoding='utf-8') as f:
            writer = csv.writer(f)
            writer.writerow(columns)  # Header
            writer.writerows(rows)    # Data
        
        print(f"✅ Exported {table_name} to {output_file}")
        
    except Error as e:
        print(f"❌ Error: {e}")
    finally:
        cursor.close()
        connection.close()

# Configuration
db_config = {
    'host': 'localhost',
    'user': 'root',
    'password': 'your_password',
    'database': 'hris_01'
}

# Export all PDS tables
pds_tables = [
    'employee',
    'family_bg',
    'educational_background',
    'civil_service_eligibility',
    'work_experience',
    'voluntary_work',
    'learning_development',
    'other_info',
    'emp_references',
    'government_issued_id'
]

for table in pds_tables:
    export_table_to_csv(db_config, table, f"./pds_export/{table}.csv")

print("✅ All PDS tables exported to CSV!")
```

### Install Required Package
```bash
pip install mysql-connector-python
python3 export_pds.py
```

---

## Method 3: Using phpmyadmin Export Function (Easiest GUI)

If your data is already in phpMyAdmin:
1. Login to phpMyAdmin
2. Select database `hris_01`
3. Select table (e.g., `employee`)
4. Click **Export**
5. Choose format: **CSV**
6. Click **Go**
7. Repeat for each PDS table

---

## Part 2: Identifying All PDS Files to Sync

### PDS-Related Files in Current Project

#### Java/Backend Files
```
src/main/java/com/ian/web/
├── employee/
│   ├── Employee.java
│   ├── EmployeeRepository.java
│   ├── EmployeeController.java
│   ├── familybg/
│   │   ├── FamilyBg.java
│   │   └── FamilyBgRepository.java
│   ├── educationalbg/
│   │   ├── EducationalBackground.java
│   │   └── EducationalBackgroundRepository.java
│   ├── eligibility/
│   │   ├── CivilServiceEligibility.java
│   │   └── CivilServiceEligibilityRepository.java
│   ├── workexperience/
│   │   ├── WorkExperience.java
│   │   └── WorkExperienceRepository.java
│   ├── voluntary_workexperience/
│   │   ├── VoluntaryWork.java
│   │   └── VoluntaryWorkRepository.java
│   ├── learning/
│   │   ├── LearningAndDevelopment.java
│   │   └── LearningAndDevelopmentRepository.java
│   ├── otherinfo/
│   │   ├── OtherInfo.java
│   │   └── OtherInfoRepository.java
│   ├── otherinfoquestion/
│   │   ├── OtherInfoQuestion.java
│   │   └── OtherInfoQuestionRepository.java
│   ├── references/
│   │   ├── EmpReferences.java
│   │   └── EmpReferencesRepository.java
│   └── govermentid/
│       ├── GovermentIssuedId.java
│       └── GovermentIssuedIdRepository.java
└── reports/
    └── ReportsController.java (handles PDS report generation)
```

#### Frontend/Template Files
```
src/main/resources/templates/employee/pds/
├── personnal-info.html         (Section 1 - Personal)
├── family-background.html      (Section 2 - Family)
├── educational-background.html (Section 3 - Education)
├── eligibility.html            (Section 3 - CSE)
├── work-experience.html        (Section 4 - Work)
├── voluntary-work.html         (Section 5 - Voluntary)
├── learning-development.html   (Section 6 - L&D)
├── other-info.html            (Section 7 - Other Info Part 1)
├── other-info-question.html   (Section 7 - Other Info Part 2)
├── references.html            (Section 7 - References)
└── gov-issued-id.html         (Section 7 - Government ID)
```

#### Jasper Report Files
```
src/main/resources/jasper/reports/
├── PDS2025_P1.jrxml           (Page 1 template)
├── PDS2025_P1.jasper          (Compiled)
├── PDS2025_P2.jrxml           (Page 2 template)
├── PDS2025_P2.jasper          (Compiled)
├── PDS2025_P3.jrxml           (Page 3 template)
├── PDS2025_P3.jasper          (Compiled)
├── PDS2025_P4.jrxml           (Page 4 template)
└── PDS2025_P4.jasper          (Compiled)
```

#### CSS/JavaScript
```
src/main/resources/static/
├── css/
│   └── pds-specific.css        (if exists)
└── js/
    └── pds-form-handler.js     (if exists)
```

#### Database/Migration Files
```
src/main/resources/
├── migrations/
│   ├── 2025-ld-hours-display.sql
│   └── 2025-we-part-time.sql
└── data.sql                    (seed data)
```

---

## Part 3: PDS File Sync Strategy

### Option A: Complete PDS Module Transfer (Recommended)

#### Step 1: Create Transfer Package
```bash
# Create directory structure
mkdir -p pds_transfer/{java,html,jasper,css,js,migrations}

# Copy Java files
cp -r src/main/java/com/ian/web/employee/familybg pds_transfer/java/
cp -r src/main/java/com/ian/web/employee/educationalbg pds_transfer/java/
cp -r src/main/java/com/ian/web/employee/eligibility pds_transfer/java/
cp -r src/main/java/com/ian/web/employee/workexperience pds_transfer/java/
cp -r src/main/java/com/ian/web/employee/voluntary_workexperience pds_transfer/java/
cp -r src/main/java/com/ian/web/employee/learning pds_transfer/java/
cp -r src/main/java/com/ian/web/employee/otherinfo pds_transfer/java/
cp -r src/main/java/com/ian/web/employee/otherinfoquestion pds_transfer/java/
cp -r src/main/java/com/ian/web/employee/references pds_transfer/java/
cp -r src/main/java/com/ian/web/employee/govermentid pds_transfer/java/

# Copy HTML templates
cp -r src/main/resources/templates/employee/pds/* pds_transfer/html/

# Copy Jasper reports
cp src/main/resources/jasper/reports/PDS2025_* pds_transfer/jasper/

# Copy migrations
cp src/main/resources/migrations/2025-*.sql pds_transfer/migrations/

# Copy Employee.java (main entity)
cp src/main/java/com/ian/web/employee/Employee.java pds_transfer/java/

# Copy ReportsController.java (report generation)
cp src/main/java/com/ian/web/reports/ReportsController.java pds_transfer/java/
```

#### Step 2: Check Compatibility in Target Project
```bash
# In the other HRIS project with leave management
cd /path/to/hris-with-leave-management

# Check if it has same structure
ls -la src/main/java/com/ian/web/employee/
ls -la src/main/resources/jasper/reports/

# Check existing PDS implementation (outdated)
find . -name "*pds*" -type f
```

#### Step 3: Merge/Replace Files
```bash
# Backup old PDS files first!
mkdir -p backup_old_pds
cp -r src/main/java/com/ian/web/employee/*/old_pds_backup

# Copy new PDS Java classes
cp -r /path/to/pds_transfer/java/* src/main/java/com/ian/web/employee/

# Copy new PDS HTML templates
cp -r /path/to/pds_transfer/html/* src/main/resources/templates/employee/pds/

# Copy new Jasper reports (overwrite old ones)
cp /path/to/pds_transfer/jasper/PDS2025_* src/main/resources/jasper/reports/

# Run migrations
mysql -u root -p hris_db < /path/to/pds_transfer/migrations/2025-*.sql
```

---

### Option B: Selective File Transfer

If the other project has different package names or structure:

#### 1. Identify Differences
```bash
# Compare Employee entity
diff hrisp-web01/src/main/java/com/ian/web/employee/Employee.java \
     hris-with-leave/src/main/java/com/ian/web/employee/Employee.java

# Compare controller structure
ls -la hrisp-web01/src/main/java/com/ian/web/employee/
ls -la hris-with-leave/src/main/java/com/ian/web/employee/
```

#### 2. Adapt Class Names (if needed)
```bash
# If package names differ, use sed to replace
sed -i 's/com\.ian\.web/org\.mycompany\.hris/g' transferred_file.java
sed -i 's/com\.ian\.web/org\.mycompany\.hris/g' transferred_template.html
```

#### 3. Test Integration
```bash
# In target project
mvn clean compile  # Check for compilation errors
mvn test          # Run tests
```

---

### Option C: Using Git to Track Both Projects

```bash
# Create shared PDS branch in both projects
cd hrisp-web01
git checkout -b pds/2025-enhancements

cd ../hris-with-leave
git pull ../hrisp-web01 pds/2025-enhancements
# Or manually cherry-pick commits
git cherry-pick [commit-hash]
```

---

## Part 4: Step-by-Step Execution Plan

### Week 1: Preparation
- [ ] Export SQL data to CSV (use Method 1 or 2)
- [ ] Verify 634 employees exported successfully
- [ ] Create backup of target project
- [ ] List all differences between projects

### Week 2: PDS File Transfer
- [ ] Copy Java classes from current project
- [ ] Copy HTML templates from current project
- [ ] Copy Jasper reports from current project
- [ ] Update package names if necessary

### Week 3: Integration & Testing
- [ ] Compile target project with new PDS files
- [ ] Fix any import/package issues
- [ ] Run database migrations
- [ ] Load employee data from CSV
- [ ] Test PDS form submission
- [ ] Generate sample PDS reports
- [ ] Verify leave management features still work

### Week 4: Deployment
- [ ] Deploy updated application
- [ ] Monitor logs for errors
- [ ] Run smoke tests
- [ ] Get user sign-off

---

## Checklist: PDS Files to Transfer

### Java Classes (26 files)
- [ ] Employee.java
- [ ] EmployeeRepository.java
- [ ] FamilyBg.java + Repository
- [ ] EducationalBackground.java + Repository
- [ ] CivilServiceEligibility.java + Repository
- [ ] WorkExperience.java + Repository
- [ ] VoluntaryWork.java + Repository
- [ ] LearningAndDevelopment.java + Repository
- [ ] OtherInfo.java + Repository
- [ ] OtherInfoQuestion.java + Repository
- [ ] EmpReferences.java + Repository
- [ ] GovermentIssuedId.java + Repository
- [ ] ReportsController.java

### HTML Templates (11 files)
- [ ] personnal-info.html
- [ ] family-background.html
- [ ] educational-background.html
- [ ] eligibility.html
- [ ] work-experience.html
- [ ] voluntary-work.html
- [ ] learning-development.html
- [ ] other-info.html
- [ ] other-info-question.html
- [ ] references.html
- [ ] gov-issued-id.html

### Jasper Reports (8 files)
- [ ] PDS2025_P1.jrxml
- [ ] PDS2025_P1.jasper
- [ ] PDS2025_P2.jrxml
- [ ] PDS2025_P2.jasper
- [ ] PDS2025_P3.jrxml
- [ ] PDS2025_P3.jasper
- [ ] PDS2025_P4.jrxml
- [ ] PDS2025_P4.jasper

### Database Migrations (2 files)
- [ ] 2025-ld-hours-display.sql
- [ ] 2025-we-part-time.sql

---

## Troubleshooting

### Issue: CSV encoding problems
```bash
# Convert to UTF-8 if needed
iconv -f cp1252 -t UTF-8 input.csv -o output.csv
```

### Issue: Import errors in target project
```bash
# Check for missing dependencies
mvn dependency:tree | grep -i "employee\|pds"

# Rebuild with new classes
mvn clean install -DskipTests
```

### Issue: Report generation fails
```bash
# Regenerate compiled reports from JRXML
mvn jasperreports:jasper

# Verify Jasper plugin is in pom.xml
grep -A5 jasperreports pom.xml
```

### Issue: Database migration fails
```bash
# Check existing schema
SHOW COLUMNS FROM learning_development;
SHOW COLUMNS FROM work_experience;

# If columns already exist, skip migration
# If columns missing, run migration again
```

---

## Summary

| Task | Time | Difficulty |
|------|------|-----------|
| Export SQL to CSV | 30 min | Easy |
| Identify PDS files | 30 min | Easy |
| Transfer PDS Java classes | 1 hour | Medium |
| Transfer PDS templates | 30 min | Easy |
| Transfer Jasper reports | 30 min | Easy |
| Update package names (if needed) | 1-2 hours | Medium |
| Test integration | 2-3 hours | Medium |
| **Total** | **6-8 hours** | **Medium** |

---

## Resources Provided

1. **SQL to CSV Methods**: 3 different approaches
2. **Bash Scripts**: For automated export
3. **Python Script**: For complex transformations
4. **File Transfer Guide**: Step-by-step instructions
5. **Compatibility Checklist**: All 37 PDS files

**Next Steps**: Choose a method and let me know if you need help with the actual conversion or transfer!
