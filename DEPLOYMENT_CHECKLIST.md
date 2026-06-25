# Linux Deployment Checklist - HRIS PDS System

**Project**: HRIS-01  
**Build Date**: June 8, 2026  
**WAR File**: hrisp.war (92 MB)  
**Status**: ✅ Ready to Deploy

---

## Pre-Deployment Phase

### 1. Code Commit & Push
- [ ] Run `git status` to verify all changes
- [ ] Review all modified files (18 files total)
- [ ] Commit with meaningful message: `git commit -m "Add PDS enhancements: GSIS ID, passport photo, L&D hours display, work experience part-time flag"`
- [ ] Push to remote repository

### 2. Database Preparation
- [ ] **BACKUP CURRENT DATABASE**:
  ```bash
  mysqldump -u root -p hris_01 > hris_01_backup_$(date +%Y%m%d).sql
  ```
- [ ] Verify backup file size is reasonable
- [ ] Store backup in safe location

### 3. Run Database Migrations
Order matters — execute in this sequence:

#### Step 1: Learning & Development Hours Display
```bash
mysql -u [DB_USER] -p[DB_PASSWORD] hris_01 < src/main/resources/migrations/2025-ld-hours-display.sql
```
- [ ] Verify command executed without errors
- [ ] Check column was added:
  ```sql
  DESCRIBE learning_development;
  ```

#### Step 2: Work Experience Part-Time Flag
```bash
mysql -u [DB_USER] -p[DB_PASSWORD] hris_01 < src/main/resources/migrations/2025-we-part-time.sql
```
- [ ] Verify command executed without errors
- [ ] Check column was added:
  ```sql
  DESCRIBE work_experience;
  ```

### 4. Linux Server Preparation

#### File Permissions & Directories
```bash
# Create required directories
mkdir -p /hrisp/uploads
mkdir -p /hrisp/tmp

# Set ownership (assuming Tomcat user)
chown -R tomcat:tomcat /hrisp/
chown -R tomcat:tomcat /var/lib/tomcat/webapps/

# Set permissions
chmod 755 /hrisp/uploads
chmod 755 /hrisp/tmp
chmod 755 /var/lib/tomcat/webapps/
```
- [ ] Directories created
- [ ] Ownership set correctly
- [ ] Permissions verified (`ls -l /hrisp/`)

#### Java & Tomcat Verification
```bash
# Verify Java 11
java -version

# Verify Tomcat is running
ps aux | grep tomcat
```
- [ ] Java 11 installed and in PATH
- [ ] Tomcat version compatible (8.5+ recommended)
- [ ] Tomcat is stopped before deployment

#### Database Connection Verification
```bash
# Test MySQL connection
mysql -h [DB_HOST] -u [DB_USER] -p[DB_PASSWORD] -e "SELECT 1;"
```
- [ ] Database is accessible
- [ ] Credentials are correct
- [ ] Firewall allows connection

---

## Deployment Phase

### 5. Deploy WAR File

#### Option A: Manual Tomcat Deployment
```bash
# Stop Tomcat
sudo systemctl stop tomcat

# Backup old WAR if exists
mv /var/lib/tomcat/webapps/hrisp.war /var/lib/tomcat/webapps/hrisp.war.backup

# Copy new WAR
cp target/hrisp.war /var/lib/tomcat/webapps/

# Fix ownership
chown tomcat:tomcat /var/lib/tomcat/webapps/hrisp.war

# Start Tomcat
sudo systemctl start tomcat
```
- [ ] Tomcat stopped
- [ ] Old WAR backed up
- [ ] New WAR copied
- [ ] Ownership corrected
- [ ] Tomcat started

#### Option B: Using Jenkins/CI-CD
```bash
# Push to remote, Jenkins automatically deploys
git push origin fix/clearance-appointments-20260524
```
- [ ] Jenkins pipeline triggered
- [ ] Build successful
- [ ] Auto-deployment completed

### 6. Monitor Deployment

#### Check Application Logs
```bash
# Follow Tomcat logs
tail -f /var/lib/tomcat/logs/catalina.out

# Wait for startup message:
# "INFO [main] org.apache.catalina.startup.Catalina.start Server startup in X ms"
```
- [ ] No startup errors in logs
- [ ] Application initialized successfully
- [ ] Database connection established

#### Application Health Check
```bash
# Test HTTP access
curl -I http://[SERVER]:8082/hrisp/

# Expected response: HTTP/1.1 200 OK or 302 (redirect to login)
```
- [ ] HTTP status 200 or 302
- [ ] No connection refused errors
- [ ] Response time reasonable (<2 seconds)

---

## Post-Deployment Testing Phase

### 7. Functional Testing

#### PDS Form Navigation
- [ ] Login with test employee account
- [ ] Navigate to PDS section
- [ ] All pages load without errors (8 pages total):
  - [ ] Personal Information
  - [ ] Family Background
  - [ ] Educational Background
  - [ ] Civil Service Eligibility
  - [ ] Work Experience
  - [ ] Voluntary Work
  - [ ] Learning & Development
  - [ ] Other Information

#### New Features Testing

##### Passport Photo Upload
- [ ] Navigate to Personal Information
- [ ] Find "Passport-Sized Photo" section
- [ ] Click "Choose File" and select JPG/PNG image
- [ ] Verify preview updates
- [ ] Click "Save Photo"
- [ ] Verify success message appears
- [ ] Refresh page and verify photo persists
- [ ] Test with file > 5MB (should reject)
- [ ] Test with non-image file (should reject)

##### Work Experience - Part-Time Flag
- [ ] Go to Work Experience section
- [ ] Add new work experience entry
- [ ] Check "Part-Time" checkbox
- [ ] Submit form
- [ ] Edit entry again
- [ ] Verify "Part-Time" checkbox is still checked
- [ ] Update and verify database shows `is_part_time = 1`

##### Learning & Development - Hours Display
- [ ] Go to Learning & Development section
- [ ] Add new training entry
- [ ] Fill in numeric "Number of Hours" (e.g., 40)
- [ ] Fill in "Hours (text)" field (e.g., "8-week online programme")
- [ ] Save entry
- [ ] View entry - should show text value, not numeric
- [ ] Test empty text field - should fallback to numeric value
- [ ] Verify PDS report shows the text value

##### Government ID - Date Formatting
- [ ] Go to Other Information → Government ID
- [ ] Enter "Date of issuance" using date picker or dd/mm/yyyy format
- [ ] Verify date displays in correct format
- [ ] Save and re-open
- [ ] Verify date persists
- [ ] Generate PDS report and verify date format (MMM dd, YYYY)

##### GSIS ID Field
- [ ] Check Personal Information section
- [ ] Verify GSIS ID field exists
- [ ] Enter GSIS number
- [ ] Save
- [ ] Generate PDS P1 report
- [ ] Verify GSIS ID displays on report

### 8. Data Integrity Verification

#### Database Record Checks
```sql
-- Check new columns exist
SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME='learning_development' AND COLUMN_NAME='hours_display';

SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME='work_experience' AND COLUMN_NAME='is_part_time';

-- Check sample data
SELECT id, no_hours, hours_display FROM learning_development LIMIT 5;
SELECT id, position_title, is_part_time FROM work_experience LIMIT 5;
```
- [ ] hours_display column exists and is VARCHAR(100)
- [ ] is_part_time column exists and is TINYINT(1)
- [ ] Data types are correct

#### Sample Employee Report Generation
```bash
# Generate PDS for a test employee
# (Use application UI: Employee → Generate Report)
```
- [ ] PDS P1 generates without errors
- [ ] PDS P2-4 generates without errors
- [ ] PDF downloads successfully
- [ ] All data displays correctly
- [ ] New fields show proper values

### 9. Performance Verification

#### Application Response Time
- [ ] Login response: < 2 seconds
- [ ] PDS page load: < 3 seconds
- [ ] Report generation: < 5 seconds
- [ ] File upload: < 10 seconds (5MB file)

#### Database Performance
- [ ] No timeout errors in logs
- [ ] No connection pool exhaustion
- [ ] Queries completing normally

#### Memory & CPU
```bash
# Monitor system resources
top -b -n 1 | grep tomcat
free -h
df -h
```
- [ ] Memory usage reasonable (< 50% available)
- [ ] CPU usage normal (< 30% idle)
- [ ] Disk space adequate (> 10% free)

### 10. Security Verification

#### File Upload Security
- [ ] Only authorized users can upload photos
- [ ] Uploaded files are stored securely (outside web root if possible)
- [ ] File type validation working (JPG/PNG only)
- [ ] File size validation working (5MB max)

#### CSRF Token Protection
- [ ] Form submissions include CSRF tokens
- [ ] Tokens are unique per session
- [ ] Invalid tokens are rejected

#### Database Security
- [ ] Credentials not exposed in logs
- [ ] Connections use SSL/TLS (recommended)
- [ ] Database firewall rules restrictive

#### Audit Trail
- [ ] All changes logged with timestamp
- [ ] User ID recorded for changes
- [ ] Photo uploads recorded in logs

---

## Rollback Plan (If Issues Occur)

### Immediate Rollback
```bash
# Stop application
sudo systemctl stop tomcat

# Restore old WAR
rm /var/lib/tomcat/webapps/hrisp.war
mv /var/lib/tomcat/webapps/hrisp.war.backup /var/lib/tomcat/webapps/hrisp.war

# Restore database (only if schema changes caused issues)
# mysql -u root -p hris_01 < hris_01_backup_YYYYMMDD.sql

# Start application
sudo systemctl start tomcat
```
- [ ] Old WAR restored
- [ ] Tomcat restarted
- [ ] Application accessible again

### Investigation Steps
1. Check Tomcat logs for error messages
2. Verify database migrations were correct
3. Ensure file permissions are correct
4. Test database connection
5. Clear browser cache and try again

---

## Post-Deployment Monitoring (Next 48 Hours)

- [ ] Monitor logs daily for errors
- [ ] Check user feedback for issues
- [ ] Verify no data loss occurred
- [ ] Monitor database growth (new columns)
- [ ] Check server resources remain stable
- [ ] Confirm nightly backups execute

---

## Sign-Off

| Role | Name | Date | Signature |
|------|------|------|-----------|
| **Developer** | __________ | ___/___/___ | __________ |
| **QA Tester** | __________ | ___/___/___ | __________ |
| **DevOps/SysAdmin** | __________ | ___/___/___ | __________ |
| **Project Manager** | __________ | ___/___/___ | __________ |

---

## Support Contact

**For Issues**: [Your Contact Info]  
**Escalation**: [Management Contact]  
**Hours**: [Available Hours]

---

**Document Created**: June 8, 2026  
**Last Updated**: June 8, 2026  
**Version**: 1.0
