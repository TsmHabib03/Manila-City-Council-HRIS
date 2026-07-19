# SSH Upload Guide - Deploy to Linux Server

**Server Details:**
- Host: `34.142.191.39`
- Username: `habib`
- SSH Key: `C:\Users\Habib\.ssh/id_rsa`

---

## Step 1: Open PowerShell

1. Press `Windows + R`
2. Type `powershell` and press Enter
3. Or open Windows Terminal and select PowerShell

---

## Step 2: Navigate to Your Project Directory

```powershell
cd C:\Users\Habib\IdeaProjects\hrisp-web01
```

---

## Step 3: Upload Files Using SCP

### Option A: Upload Single File
To upload the cleanup script:
```powershell
scp -i "C:\Users\Habib\.ssh/id_rsa" "C:\Users\Habib\IdeaProjects\hrisp-web01\cleanup_mysql.sql" habib@34.142.191.39:/home/habib/
```

### Option B: Upload Entire Project Folder
```powershell
scp -i "C:\Users\Habib\.ssh/id_rsa" -r "C:\Users\Habib\IdeaProjects\hrisp-web01" habib@34.142.191.39:/home/habib/
```

### Option C: Upload CSV Backup
```powershell
scp -i "C:\Users\Habib\.ssh/id_rsa" "C:\Users\Habib\IdeaProjects\hrisp-web01\hris_01.csv" habib@34.142.191.39:/home/habib/backups/
```

---

## Step 4: Verify Upload on Server

Connect to your server:
```powershell
ssh -i "C:\Users\Habib\.ssh/id_rsa" habib@34.142.191.39
```

Then list the files:
```bash
ls -la ~/
```

---

## Step 5: Run SQL Cleanup (If MySQL is on Server)

Once connected via SSH:

### Option 1: Direct MySQL Import
```bash
mysql -u root -p hris_01 < ~/cleanup_mysql.sql
```

### Option 2: Login to MySQL and Run
```bash
mysql -u root -p
```

Then in MySQL:
```sql
USE hris_01;
source ~/cleanup_mysql.sql;
```

---

## Common Errors & Solutions

### "Permission denied (publickey)"
- Ensure SSH key has correct permissions: `icacls "C:\Users\Habib\.ssh" /grant:r "%username%:F"`
- Verify key path is correct

### "Connection refused"
- Check if server is running: `ping 34.142.191.39`
- Verify SSH port (usually 22) is open

### "File not found"
- Double-check file paths use forward slashes `/` or escaped backslashes `\\`

---

## Quick Copy-Paste Commands

**Upload cleanup script:**
```powershell
scp -i "C:\Users\Habib\.ssh/id_rsa" "C:\Users\Habib\IdeaProjects\hrisp-web01\cleanup_mysql.sql" habib@34.142.191.39:/home/habib/
```

**Connect to server:**
```powershell
ssh -i "C:\Users\Habib\.ssh/id_rsa" habib@34.142.191.39
```

**Check uploaded files:**
```bash
ls -lh ~/cleanup_mysql.sql
```

---

## Security Reminders

⚠️ **IMPORTANT:**
- Never share your SSH private key (`id_rsa`) with anyone
- Keep your server credentials confidential
- Use strong passwords for database access
- Consider restricting SSH to specific IPs for extra security

---

## Need Help?

If you encounter issues:
1. Check your internet connection
2. Verify SSH key permissions on Windows
3. Ensure the server is accessible
4. Check firewall rules on both ends

