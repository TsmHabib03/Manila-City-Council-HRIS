# HRISP — Manila City Council HRIS

Human Resource Information System for the Manila City Council. Manages employee
201 files: Personal Data Sheets (CS Form 212), service records, appointments,
clearances, leave management (CS Form 6 + leave cards), and PDF report
generation via JasperReports.

Live at **https://manilacitycouncil.com** (nginx reverse proxy → Spring Boot on
port 8082, MySQL database `hris_01` with ~429 employees).

## Tech stack

| Component | Version / notes |
|---|---|
| Java | **11** (`java.version` in `pom.xml`) |
| Spring Boot | 2.2.4.RELEASE (Web, Data JPA, Data REST, Security, Thymeleaf) |
| Packaging | WAR with embedded Tomcat (`target/hrisp.war`, run with `java -jar`) |
| Database | MySQL (`hris_01` in production, `hrisp` datasource name in prod config) |
| Reporting | JasperReports (`.jrxml` templates under `src/main/resources/jasper/reports`, compiled to `.jasper` during the Maven build) |
| UI | Thymeleaf templates + Limitless/Bootstrap theme (`src/main/resources/templates`, `static/`) |

## Prerequisites

- JDK 11
- Maven 3.x — **there is no Maven wrapper (`mvnw`) in this repo**, so Maven must
  be installed locally
- MySQL 5.7/8.x running on `localhost:3306`

## Configuration & profiles

`src/main/resources/application.properties` selects the profile:
`SPRING_PROFILES_ACTIVE` env var, default `dev`.

- **dev** — `application-dev.properties`. Local DB overrides (passwords) belong
  in `config/application-dev.properties`, which is git-ignored and loaded
  automatically by Spring Boot from the working directory.
- **prod** — `application-prod.properties`. Reads `DB_USERNAME` (default
  `cdsiadmin`) and `DB_PASSWORD` from the environment. Uploads go to
  `/hrisp/uploads`.
- `EMPLOYEE_DEFAULT_PASSWORD` env var sets the default password assigned to
  employee accounts (falls back to `ChangeMe123`).

## Build, run, deploy

```bash
# Run locally (dev profile)
mvn spring-boot:run

# Build the WAR (also recompiles all .jrxml -> .jasper)
mvn clean package -DskipTests   # -> target/hrisp.war (~92 MB)

# Deploy to production
bash deploy.sh
```

**Deployment quirk:** the app does **not** run under a system Tomcat. The
server launches the fat WAR from `/home/habib/hrisp-web01/target/hrisp.war` via
`~/hrisp-web01/restart.sh`, so the WAR must be uploaded to exactly that path —
`deploy.sh` does this correctly (build → scp → md5 verify → restart).
Treat `deploy.sh` as the authoritative source for the server address/SSH key;
older docs under `docs/deployment/` mention superseded IPs. Nginx + Let's
Encrypt config reference: `docs/deployment/nginx-hrisp.conf`.

The **leave management module** (leave applications, leave cards, holidays,
leave types, CS Form 6 export) lives on the `feature/leave-management` branch;
its design docs are in `docs/leave/`. Run
`src/main/resources/migrations/2026-leave-management.sql` when deploying it.

## Repository layout

| Path | Contents |
|---|---|
| `src/` | Application source (Java under `com.ian.web`, Thymeleaf templates, Jasper templates, SQL migrations) |
| `deploy.sh` | Build + deploy script (primary ops entry point) |
| `docs/changes/` | Change logs and assessment reports for past feature work |
| `docs/deployment/` | Deployment checklist, SSH upload guide, deployment summary, nginx config, data-migration guide |
| `docs/leave/` | Leave module blueprint + the source leave-processing documents |
| `docs/reference/` | Blank CSC PDS forms and Excel templates |
| `docs/uat/` | Final UAT documents and admin/employee UAT screenshots |
| `data/` | ⚠️ Sensitive: DB dumps, seed SQL, credential lists, employee masterlists (see below) |
| `scripts/` | Data import/export utilities (`export_pds_to_csv.sh`, `import_employees.py`, `gen_councilors.py`) |
| `archive/` | Retained legacy material: one-off PDS/JRXML calibration scripts, UAT screenshot-capture tooling, superseded UAT drafts, an already-merged patch. Kept for reference; nothing here is needed to build or run the app |
| `config/` | Git-ignored local Spring Boot overrides (local DB password) |

## ⚠️ Security notes for the new owner

- `data/` contains **real employee PII and login credentials**: full database
  dumps (`data/db-dumps/`), a credentials list showing every account uses the
  default password `password` (`data/credentials/`), and employee masterlist
  PDFs. These are tracked in git and exist in the history on the remote.
  Recommended follow-ups:
  1. Force all users to change passwords in production (see
     `data/sql/reset_all_passwords.sql` for how they were set).
  2. If the repo will be shared more widely, scrub `data/` and the old
     `.claude/worktrees/` blobs from git history (BFG or `git filter-repo`)
     and force-push, then rotate anything exposed.
- Database passwords are supplied via environment variables / git-ignored
  `config/` files — keep it that way.

## Cleanup summary (July 2026 handoff prep)

- Untracked a stale 93 MB copy of the whole project that had been committed
  under `.claude/worktrees/` since the first commit (recoverable from history).
- Consolidated ~30 loose root files into `docs/`, `data/`, `scripts/`,
  `archive/`; dissolved the old `ZBackup/` and `Leave_Docs/` folders.
- Deleted only regenerable artifacts: build/test logs, generated debug
  PDFs/PNGs, `pom.xml.bak`, Spring Initializr boilerplate `HELP.md`.
- `archive/` was retained (not deleted) because its calibration and UAT-capture
  scripts may be useful if the PDS Jasper templates or UAT documents ever need
  to be regenerated.
