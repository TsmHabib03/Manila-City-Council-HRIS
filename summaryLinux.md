# HRISP Manila City Council - Deployment Summary

**Project:** Human Resource Information System (HRIS) for Manila City Council  
**Deployment Date:** May 24-25, 2026  
**Current Status:** ✅ LIVE & WORKING  
**Access URL:** https://manilacitycouncil.com

---

## 📋 Project Overview

HRISP is a Spring Boot web application for managing employee records, including:
- Employee Personal Data Sheets (PDS)
- Clearances
- Appointments
- Change password functionality
- Reports generation (PDF with images)

**Current Capability:** Only HR can edit PDS. Employees can view/export only.  
**Planned Feature:** Allow employees to edit their own PDS

---

## 🖥️ Server Environment

| Property | Value |
|----------|-------|
| **Cloud Provider** | Google Cloud Platform (GCP) |
| **Instance** | instance-20260426-082130 |
| **OS** | Debian (Linux) |
| **Private IP** | 10.148.0.3 |
| **Public IP** | 34.143.251.125 |
| **Domain** | manilacitycouncil.com (Cloudflare proxied) |
| **User** | habib |
| **Working Directory** | `/home/habib/hrisp-web01` |

---

## 📦 Application Build Configuration

### Maven Build Command
```bash
mvn clean install -DskipTests -Dspring.profiles.active=prod
```

### Build Results
- **Output:** `target/hrisp.jar` (85MB)
- **Packaging:** JAR (changed from WAR in `pom.xml`)
- **Tomcat Port:** 8080
- **Context Path:** `/` (ROOT - no more `/hrisp`)

---

## 🗄️ Database Setup

| Property | Value |
|----------|-------|
| **Engine** | MariaDB 10.11.14 |
| **Database** | hrisp (utf8mb4) |
| **Username** | cdsiadmin |
| **Password** | Manila@2026 |
| **Employees in DB** | 636 |
| **Tables** | 40+ (employee, sys_user, clearance, appointment, etc.) |
| **Schema File** | ~/hris_01.sql |

### Connection String
```
jdbc:mysql://localhost:3306/hrisp?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Manila
```

---

## 🔧 Spring Boot Configuration

**File:** `src/main/resources/application-prod.properties`

### Key Settings
```properties
# Server
spring.profiles.active=prod
server.port=8080
server.servlet.context-path=/          # ROOT - no /hrisp path

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/hrisp?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Manila
spring.datasource.username=${DB_USERNAME:cdsiadmin}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=none

# File Upload
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB
file.upload.dir=/hrisp/uploads

# Migration
migration.data.dir=/opt/hrisp/migration-data
```

### Environment Variables (set in systemd service)
```bash
DB_USERNAME=cdsiadmin
DB_PASSWORD=Manila@2026
```

---

## 🐍 Systemd Service Configuration

**File:** `/etc/systemd/system/hrisp.service`

```ini
[Unit]
Description=HRISP Application
After=network.target mariadb.service

[Service]
Type=simple
User=habib
WorkingDirectory=/home/habib/hrisp-web01
Environment="DB_USERNAME=cdsiadmin"
Environment="DB_PASSWORD=Manila@2026"
ExecStart=/usr/bin/java -Dspring.profiles.active=prod -jar /home/habib/hrisp-web01/target/hrisp.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

### Service Commands
```bash
# Check status
sudo systemctl status hrisp

# Start/Stop/Restart
sudo systemctl start hrisp
sudo systemctl stop hrisp
sudo systemctl restart hrisp

# View logs
journalctl -u hrisp -f          # Live logs
journalctl -u hrisp -n 50       # Last 50 lines

# Enable on boot
sudo systemctl enable hrisp
```

---

## 🧵 Nginx Reverse Proxy Configuration

**File:** `/etc/nginx/sites-available/default`

```nginx
# HTTP → HTTPS redirect
server {
    listen 80;
    listen [::]:80;
    server_name manilacitycouncil.com www.manilacitycouncil.com;
    return 301 https://$server_name$request_uri;
}

# HTTPS Server (proxies to Spring Boot)
server {
    listen 443 ssl http2;
    listen [::]:443 ssl http2;
    server_name manilacitycouncil.com www.manilacitycouncil.com;

    ssl_certificate /etc/letsencrypt/live/manilacitycouncil.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/manilacitycouncil.com/privkey.pem;

    # Proxy all requests to Spring Boot on port 8080
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### Nginx Commands
```bash
# Test configuration
sudo nginx -t

# Restart nginx
sudo systemctl restart nginx

# Check status
sudo systemctl status nginx
```

---

## 🔒 SSL/TLS Certificates

| Property | Value |
|----------|-------|
| **Provider** | Let's Encrypt |
| **Certificate Path** | `/etc/letsencrypt/live/manilacitycouncil.com/` |
| **Domains** | manilacitycouncil.com, www.manilacitycouncil.com |
| **Expiration** | 2026-08-22 |
| **Auto-renewal** | Configured by certbot |

---

## ☁️ Cloudflare Configuration

| Setting | Value |
|---------|-------|
| **DNS A Record** | 34.143.251.125 (Proxied - orange cloud) |
| **SSL/TLS Mode** | Full |
| **Status** | Working ✅ |

**Note:** Cloudflare initially showed challenge page due to WAF settings. Resolved by disabling browser integrity checks for API requests.

---

## 🐛 Code Fixes Applied

### 1. ReportsController.java - FileNotFoundException Fix

**Problem:** JAR-packaged resources cannot be accessed via `ResourceUtils.getFile()`

**File:** `src/main/java/com/ian/web/reports/ReportsController.java`

**Fixed Methods:**
- `populateMapReport1()` (line 262-265) - PDS1.png
- `populateMapReport2()` (line 694) - PDS2.png
- `populateMapReport3()` (line 756) - PDS3.png
- `populateMapReport4()` (line 828) - PDS4.png

**Original Code:**
```java
File file = ResourceUtils.getFile("classpath:static/images/PDSN.png");
String bgImg = file.getAbsolutePath();
```

**Fixed Code:**
```java
ClassPathResource resource = new ClassPathResource("static/images/PDSN.png");
String bgImg = resource.getURL().toString();
```

**Imports Added:**
```java
import org.springframework.core.io.ClassPathResource;
import java.io.IOException;
```

**Exception Handling:** Changed `throws FileNotFoundException` → `throws IOException` in method signatures

---

### 2. ChangePasswordController.java - Template Path Fix

**Problem:** Leading slash in template return path caused Thymeleaf routing failure

**File:** `src/main/java/com/ian/web/changepassword/ChangePasswordController.java`

**Change:**
```java
// BEFORE (broken)
return "/changepassword/change-password";

// AFTER (working)
return "changepassword/change-password";
```

**Line:** 35

---

### 3. DataMigrationService.java - BOM Character Fix

**Problem:** UTF-8 BOM character at file start caused compilation error

**Fix Command:**
```bash
sed -i '1s/^\xEF\xBB\xBF//' DataMigrationService.java
```

---

### 4. ClearanceController.java - Primitive Long Null Check

**Problem:** Primitive `long` type cannot be compared to `null`

**File:** `src/main/java/com/ian/web/clearance/ClearanceController.java`

**Change (Line 83):**
```java
// BEFORE (compilation error)
if (getId() != null) { ... }

// AFTER (correct)
if (getId() != 0) { ... }
```

---

## 📝 Current PDS Feature Access Control

### Role-Based Access (Current)

| User Role | View PDS | Edit PDS | Export PDS |
|-----------|----------|----------|-----------|
| **HR** | Own + All | Own + All | Own + All |
| **Employee** | Own only | ❌ NO | Own only |
| **Admin** | All | All | All |

---

## 🔄 How to Modify: Allow Employees to Edit Their Own PDS

### Step 1: Find PDS Edit Controller

**File:** `src/main/java/com/ian/web/pds/PdsController.java` (or similar)

Look for methods like:
- `editPds()`
- `savePds()`
- `updatePds()`

### Step 2: Add Role-Based Logic

**Pattern to implement:**
```java
@PostMapping("/pds/{pdsId}/save")
public String savePds(@PathVariable Long pdsId, @ModelAttribute Pds pds) {
    
    // Get current logged-in user
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    SysUser currentUser = (SysUser) auth.getPrincipal();
    
    // Check: Is user HR OR editing their own PDS?
    boolean isHR = currentUser.hasRole("ROLE_HR");
    boolean isOwnPDS = currentUser.getEmployeeId().equals(pds.getEmployeeId());
    
    if (!isHR && !isOwnPDS) {
        return "error/access-denied";  // Deny access
    }
    
    // Save the PDS
    pdsService.save(pds);
    return "redirect:/pds/" + pdsId;
}
```

### Step 3: Update PDS Form Template

**File:** `src/main/resources/templates/pds/pds-form.html` (or similar)

**Show edit button only if allowed:**
```html
<!-- Thymeleaf conditional -->
<div th:if="${isHR || isOwnPDS}">
    <button type="submit" class="btn btn-primary">Save PDS</button>
</div>

<!-- Read-only view only -->
<div th:unless="${isHR || isOwnPDS}">
    <p class="alert alert-info">You can only view your PDS. Contact HR to request changes.</p>
</div>
```

### Step 4: Pass Data to Template

**In Controller:**
```java
model.addAttribute("isHR", currentUser.hasRole("ROLE_HR"));
model.addAttribute("isOwnPDS", currentUser.getEmployeeId().equals(pds.getEmployeeId()));
```

### Step 5: Rebuild & Deploy

```bash
cd ~/hrisp-web01
mvn clean install -DskipTests -Dspring.profiles.active=prod
sudo systemctl restart hrisp
```

---

## ✅ Verification Checklist

### Local Testing (on server)
```bash
# Test app directly
curl http://localhost:8080/ 
# Expected: HTTP 302 redirect to /login

# Test through nginx
curl https://localhost/
# Expected: HTTP 302 redirect to HTTPS

# Test on public IP
curl -k https://34.143.251.125/
# Expected: HTTP 302
```

### Public Access
```bash
# Test domain
curl https://manilacitycouncil.com/
# Expected: HTTP 200 with login page

# Or access in browser
https://manilacitycouncil.com
```

---

## 📂 Important File Locations

| Purpose | Path |
|---------|------|
| **Project Root** | `/home/habib/hrisp-web01` |
| **JAR File** | `/home/habib/hrisp-web01/target/hrisp.jar` |
| **Source Code** | `/home/habib/hrisp-web01/src/main/java` |
| **Spring Config** | `/home/habib/hrisp-web01/src/main/resources/application-prod.properties` |
| **Templates** | `/home/habib/hrisp-web01/src/main/resources/templates` |
| **Nginx Config** | `/etc/nginx/sites-available/default` |
| **Systemd Service** | `/etc/systemd/system/hrisp.service` |
| **SSL Certificates** | `/etc/letsencrypt/live/manilacitycouncil.com/` |
| **App Logs** | `journalctl -u hrisp -f` |
| **Nginx Logs** | `/var/log/nginx/error.log`, `/var/log/nginx/access.log` |

---

## 🚀 Quick Start Commands

```bash
# SSH into server
ssh habib@34.143.251.125

# Check app status
sudo systemctl status hrisp

# View live logs
journalctl -u hrisp -f

# Restart application
sudo systemctl restart hrisp

# Rebuild (after code changes)
cd ~/hrisp-web01
mvn clean install -DskipTests -Dspring.profiles.active=prod
sudo systemctl restart hrisp

# Check nginx
sudo systemctl status nginx
sudo nginx -t

# Test app locally
curl http://localhost:8080/

# Test public
https://manilacitycouncil.com
```

---

## 📞 Troubleshooting

### App Not Responding
```bash
# Check if running
sudo systemctl status hrisp

# Check logs for errors
journalctl -u hrisp -n 100 | grep -i error

# Restart
sudo systemctl restart hrisp
```

### 403 Forbidden Error
- Check Cloudflare WAF settings
- Disable browser integrity check if needed
- Verify SSL/TLS mode is "Full"

### Database Connection Error
```bash
# Check MariaDB
sudo systemctl status mariadb

# Test connection
mysql -u cdsiadmin -p -h localhost
# Password: Manila@2026
```

### Nginx Not Proxying
```bash
# Test nginx config
sudo nginx -t

# Check if proxying to right port
curl http://localhost/

# Check nginx logs
sudo tail -50 /var/log/nginx/error.log
```

---

## 📌 Next Steps & Future Modifications

1. **✅ COMPLETED:** Deploy app at root URL
2. **📝 TODO:** Implement employee PDS editing (see "How to Modify" section above)
3. **📋 TODO:** Add audit logging for PDS changes
4. **📊 TODO:** Implement PDS change approval workflow
5. **🔐 TODO:** Add additional security logging

---

## 🎯 Key Takeaways

- **App is JAR-based** (not WAR), runs with Spring Boot embedded Tomcat
- **Context path is ROOT** (no /hrisp prefix) after latest changes
- **Nginx proxies** all requests to localhost:8080
- **SSL/TLS handled by** Let's Encrypt + nginx
- **Cloudflare proxies** the domain to your GCP instance
- **Database is MariaDB** with 636 employees
- **Service auto-restarts** on failure and server reboot

---

**Last Updated:** May 25, 2026  
**Deployment Status:** ✅ LIVE & OPERATIONAL  
**Live URL:** https://manilacitycouncil.com