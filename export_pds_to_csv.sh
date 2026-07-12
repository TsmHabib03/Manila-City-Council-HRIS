#!/bin/bash
#
# PDS Export to CSV Script
# Usage: ./export_pds_to_csv.sh
#
# This script exports all PDS-related tables from MySQL to individual CSV files
# Configure DB credentials below before running
#

set -e  # Exit on error

# ===== CONFIGURATION =====
MYSQL_USER="${DB_USER:-root}"
MYSQL_PASS="${DB_PASSWORD:-}"
MYSQL_HOST="${DB_HOST:-localhost}"
MYSQL_DB="${DB_NAME:-hris_01}"
OUTPUT_DIR="./pds_csv_export"
LOG_FILE="export_pds.log"

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# ===== FUNCTIONS =====

log_info() {
    echo -e "${BLUE}ℹ️  $1${NC}" | tee -a "$LOG_FILE"
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}" | tee -a "$LOG_FILE"
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}" | tee -a "$LOG_FILE"
}

log_error() {
    echo -e "${RED}❌ $1${NC}" | tee -a "$LOG_FILE"
}

# Check MySQL connectivity
check_mysql_connection() {
    log_info "Checking MySQL connection..."
    if mysql -h "$MYSQL_HOST" -u "$MYSQL_USER" -p"$MYSQL_PASS" -e "SELECT 1;" 2>/dev/null; then
        log_success "MySQL connection successful"
        return 0
    else
        log_error "Cannot connect to MySQL"
        echo "  Host: $MYSQL_HOST"
        echo "  User: $MYSQL_USER"
        echo "  Database: $MYSQL_DB"
        return 1
    fi
}

# Create output directory
setup_output_dir() {
    log_info "Setting up output directory: $OUTPUT_DIR"
    mkdir -p "$OUTPUT_DIR"
    log_success "Output directory ready"
}

# Export single table with headers
export_table_with_headers() {
    local table=$1
    local output_file="$OUTPUT_DIR/${table}.csv"

    log_info "Exporting table: $table"

    # Get column names
    local columns=$(mysql -h "$MYSQL_HOST" -u "$MYSQL_USER" -p"$MYSQL_PASS" "$MYSQL_DB" \
        -sN -e "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='$table' ORDER BY ORDINAL_POSITION;" \
        2>/dev/null | paste -sd ',' -)

    # Export data without headers
    mysql -h "$MYSQL_HOST" -u "$MYSQL_USER" -p"$MYSQL_PASS" "$MYSQL_DB" \
        -sN -e "SELECT * FROM $table;" 2>/dev/null > "$output_file.tmp"

    # Create final CSV with headers
    {
        echo "$columns"
        cat "$output_file.tmp"
    } > "$output_file"

    rm -f "$output_file.tmp"

    # Count rows
    local row_count=$(wc -l < "$output_file")
    log_success "Exported $table ($((row_count - 1)) rows)"
}

# Export all PDS tables
export_all_pds_tables() {
    local tables=(
        "employee"
        "family_bg"
        "educational_background"
        "civil_service_eligibility"
        "work_experience"
        "voluntary_work"
        "learning_development"
        "other_info"
        "other_info_question"
        "emp_references"
        "government_issued_id"
        "service_record"
        "school"
        "degree_level"
        "degree_course"
        "academic_honors"
        "learning_type"
        "position_titles"
        "division"
        "employee_status"
    )

    log_info "Starting export of ${#tables[@]} tables..."

    local successful=0
    local failed=0

    for table in "${tables[@]}"; do
        if export_table_with_headers "$table"; then
            ((successful++))
        else
            log_warning "Failed to export $table"
            ((failed++))
        fi
    done

    log_info "Export complete: $successful successful, $failed failed"
    return $failed
}

# Generate summary report
generate_summary() {
    local summary_file="$OUTPUT_DIR/EXPORT_SUMMARY.txt"

    log_info "Generating summary report..."

    {
        echo "╔════════════════════════════════════════════════════════════╗"
        echo "║       PDS DATA EXPORT SUMMARY - $(date +%Y-%m-%d\ %H:%M:%S)         ║"
        echo "╚════════════════════════════════════════════════════════════╝"
        echo ""
        echo "Database: $MYSQL_DB"
        echo "Export Directory: $(pwd)/$OUTPUT_DIR"
        echo "Export Time: $(date)"
        echo ""
        echo "FILES EXPORTED:"
        echo "─────────────────────────────────────────────────────────────"

        for file in "$OUTPUT_DIR"/*.csv; do
            if [ -f "$file" ]; then
                local filename=$(basename "$file")
                local line_count=$(wc -l < "$file")
                local row_count=$((line_count - 1))
                printf "%-40s %8d rows\n" "$filename" "$row_count"
            fi
        done

        echo ""
        echo "TOTAL FILES: $(ls -1 "$OUTPUT_DIR"/*.csv 2>/dev/null | wc -l)"
        echo ""
        echo "INSTRUCTIONS FOR IMPORTING:"
        echo "─────────────────────────────────────────────────────────────"
        echo "1. Transfer CSV files to target server"
        echo "2. Import into target HRIS database:"
        echo "   - Update column names if they differ in target schema"
        echo "   - Use LOAD DATA INFILE for bulk import (faster)"
        echo "3. Verify data integrity with row counts"
        echo ""
        echo "SAMPLE IMPORT COMMAND:"
        echo "─────────────────────────────────────────────────────────────"
        echo "mysql> LOAD DATA LOCAL INFILE 'employee.csv'"
        echo "       INTO TABLE employee"
        echo "       FIELDS TERMINATED BY ','"
        echo "       ENCLOSED BY '\"'"
        echo "       LINES TERMINATED BY '\n'"
        echo "       IGNORE 1 ROWS;"
        echo ""

    } | tee "$summary_file"

    log_success "Summary report saved to: $summary_file"
}

# Display usage
show_usage() {
    cat << EOF
Usage: $0 [OPTIONS]

Options:
  -h, --host HOST       MySQL host (default: localhost)
  -u, --user USER       MySQL user (default: root)
  -p, --password PASS   MySQL password (default: interactive prompt)
  -d, --database DB     Database name (default: hris_01)
  -o, --output DIR      Output directory (default: ./pds_csv_export)
  --help                Show this help message

Examples:
  $0
  $0 -u root -p mypassword -d hris_01
  $0 --host 192.168.1.10 --user root --password secret

EOF
}

# ===== MAIN EXECUTION =====

main() {
    # Parse command line arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--host)
                MYSQL_HOST="$2"
                shift 2
                ;;
            -u|--user)
                MYSQL_USER="$2"
                shift 2
                ;;
            -p|--password)
                MYSQL_PASS="$2"
                shift 2
                ;;
            -d|--database)
                MYSQL_DB="$2"
                shift 2
                ;;
            -o|--output)
                OUTPUT_DIR="$2"
                shift 2
                ;;
            --help)
                show_usage
                exit 0
                ;;
            *)
                echo "Unknown option: $1"
                show_usage
                exit 1
                ;;
        esac
    done

    # Start export process
    echo ""
    log_info "╔════════════════════════════════════════════════════════════╗"
    log_info "║          PDS DATA EXPORT TO CSV - Starting...              ║"
    log_info "╚════════════════════════════════════════════════════════════╝"
    echo ""

    # Show configuration
    log_info "Configuration:"
    echo "  Host: $MYSQL_HOST"
    echo "  User: $MYSQL_USER"
    echo "  Database: $MYSQL_DB"
    echo "  Output: $OUTPUT_DIR"
    echo ""

    # Check connection
    if ! check_mysql_connection; then
        log_error "Cannot proceed without MySQL connection"
        exit 1
    fi
    echo ""

    # Setup and export
    setup_output_dir
    echo ""

    if export_all_pds_tables; then
        echo ""
        generate_summary
        echo ""
        log_success "Export completed successfully!"
        echo ""
        echo "Next steps:"
        echo "  1. Review files in: $OUTPUT_DIR"
        echo "  2. Transfer to target server"
        echo "  3. Import using SQL LOAD DATA commands"
        echo ""
        exit 0
    else
        log_error "Export completed with errors"
        exit 1
    fi
}

# Run main function
main "$@"
