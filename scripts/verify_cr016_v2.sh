#!/usr/bin/env bash
# CR 016 v2 end-to-end verification: real actor accounts (Supervisor, Council
# Secretary, Vice-Mayor), per-stage authorization, notifications, docs gate,
# appeal/cancel, year-end coterminous exclusion.
#
# Run against a dev-profile app on a SCRATCH database (data.sql seeds the six
# dev logins). Usage:  BASE=http://localhost:8082 bash scripts/verify_cr016_v2.sh
set -u
BASE="${BASE:-http://localhost:8082}"
JARS="$(mktemp -d)"
PASS=0; FAIL=0

say()  { printf '%s\n' "$*"; }
ok()   { PASS=$((PASS+1)); say "ok  $PASS - $*"; }
bad()  { FAIL=$((FAIL+1)); say "FAIL    - $*"; }
check() { # check <desc> <expected> <actual>
  if [ "$2" = "$3" ]; then ok "$1"; else bad "$1 (expected [$2] got [$3])"; fi
}
contains() { # contains <desc> <needle> <haystack-file>
  if grep -q "$2" "$3"; then ok "$1"; else bad "$1 (missing [$2])"; fi
}

login() { # login <user> <pass>
  local jar="$JARS/$1.jar"
  local csrf
  csrf=$(curl -s -c "$jar" "$BASE/login" | grep -o 'name="_csrf" value="[^"]*"' | head -1 | sed 's/.*value="//;s/"//')
  curl -s -b "$jar" -c "$jar" -d "username=$1&password=$2&_csrf=$csrf" "$BASE/login" -o /dev/null -w '%{http_code}' >/dev/null
  # seed session actorObj (mirrors real login landing)
  curl -s -b "$jar" -c "$jar" -L "$BASE/dashboard" -o /dev/null
}

csrf_of() { # csrf_of <user> — fresh token from any page
  curl -s -b "$JARS/$1.jar" "$BASE/my-notifications" >/dev/null
  curl -s -b "$JARS/$1.jar" "$BASE/login" | grep -o 'name="_csrf" value="[^"]*"' | head -1 | sed 's/.*value="//;s/"//'
}

post() { # post <user> <path> <data...> -> http code, body to $JARS/last.html (follows redirect)
  local user="$1" path="$2"; shift 2
  local jar="$JARS/$user.jar" csrf
  csrf=$(csrf_of "$user")
  curl -s -b "$jar" -c "$jar" -L -o "$JARS/last.html" -w '%{http_code}' \
    --data-urlencode "_csrf=$csrf" "$@" "$BASE$path"
}

get() { # get <user> <path> -> body to $JARS/last.html, prints http code
  curl -s -b "$JARS/$1.jar" -o "$JARS/last.html" -w '%{http_code}' "$BASE$2"
}

status_of() { # status_of <appId> (as hr_user via panel JSON)
  get hr_user "/leave-workflow/$1/panel" >/dev/null
  grep -o '"status":"[^"]*"' "$JARS/last.html" | head -1 | sed 's/.*:"//;s/"//'
}

file_leave() { # file_leave <type> <days> <from> <to>  (as emp_user) -> app id via hr panel list
  post emp_user "/addLeaveApplication" \
    --data-urlencode "id=0" \
    --data-urlencode "leaveType=$1" \
    --data-urlencode "workingDays=$2" \
    --data-urlencode "dateFrom=$3" \
    --data-urlencode "dateTo=$4" \
    --data-urlencode "dateOfFiling=2026-07-19" \
    --data-urlencode "officeDepartment=TEST" \
    --data-urlencode "position=TESTER" >/dev/null
  get hr_user "/leave-pending-list" >/dev/null
  grep -o '"id":[0-9]*' "$JARS/last.html" | sed 's/"id"://' | sort -n | tail -1
}

wf() { # wf <user> <appId> <action> [signatoryName] [remarks]
  post "$1" "/leave-workflow/$2/action" \
    --data-urlencode "action=$3" \
    --data-urlencode "signatoryName=${4:-}" \
    --data-urlencode "signatoryTitle=T" \
    --data-urlencode "remarks=${5:-}" >/dev/null
}

unread() { # unread <user>
  get "$1" "/my-notifications" >/dev/null
  grep -o '"unread":[0-9]*' "$JARS/last.html" | head -1 | sed 's/.*://'
}

say "== CR016 v2 E2E against $BASE =="

# ── logins ────────────────────────────────────────────────────────────────────
for u in admin:Admin@2026 hr_user:HrOfficer@2026 emp_user:Employee@2026 \
         supervisor:Supervisor@2026 council_sec:Council@2026 vice_mayor:ViceMayor@2026; do
  login "${u%%:*}" "${u#*:}"
  code=$(get "${u%%:*}" "/my-notifications")
  check "login ${u%%:*}" "200" "$code"
done

# ── landing pages / queue access ─────────────────────────────────────────────
check "supervisor reaches /leave-approvals"  "200" "$(get supervisor /leave-approvals)"
check "council reaches /leave-approvals"     "200" "$(get council_sec /leave-approvals)"
check "vice-mayor reaches /leave-approvals"  "200" "$(get vice_mayor /leave-approvals)"
check "employee blocked from /leave-approvals" "403" "$(get emp_user /leave-approvals)"
check "supervisor blocked from /leaves roster" "403" "$(get supervisor /leaves)"
check "supervisor blocked from /leave-applications" "403" "$(get supervisor /leave-applications)"
check "supervisor blocked from /leave-signatories"  "403" "$(get supervisor /leave-signatories)"
check "supervisor blocked from year-end run" "403" "$(post supervisor /leave-year-end/run --data-urlencode year=2026)"

# ── short path (3 days): file -> screen -> supervisor endorse -> HR approve ──
ID1=$(file_leave "Vacation Leave" 3 2026-08-03 2026-08-05)
say "-- short-path application id=$ID1"
check "new application is FILED" "FILED" "$(status_of "$ID1")"

SUP_BEFORE=$(unread supervisor)
wf hr_user "$ID1" FORWARD_TO_SUPERVISOR
check "HR screening -> FOR_ENDORSEMENT" "FOR_ENDORSEMENT" "$(status_of "$ID1")"
SUP_AFTER=$(unread supervisor)
[ "$SUP_AFTER" -gt "$SUP_BEFORE" ] && ok "supervisor notified on forward" || bad "supervisor not notified ($SUP_BEFORE -> $SUP_AFTER)"

get supervisor "/leave-approvals" >/dev/null
contains "supervisor queue lists the leave" "FOR ENDORSEMENT" "$JARS/last.html"

get supervisor "/leave-workflow/$ID1/panel" >/dev/null
contains "supervisor panel offers ENDORSE" '"action":"ENDORSE"' "$JARS/last.html"
if grep -q '"action":"APPROVE"' "$JARS/last.html"; then bad "supervisor panel must not offer APPROVE"; else ok "supervisor panel has no APPROVE"; fi

# role fence: council cannot act at FOR_ENDORSEMENT
wf council_sec "$ID1" ENDORSE "WRONG ACTOR"
check "council blocked at FOR_ENDORSEMENT" "FOR_ENDORSEMENT" "$(status_of "$ID1")"
# role fence: employee cannot call workflow actions at all
check "employee POST /leave-workflow denied" "403" "$(post emp_user "/leave-workflow/$ID1/action" --data-urlencode action=ENDORSE)"

wf supervisor "$ID1" ENDORSE "SUPERVISOR, DEPARTMENT"
check "supervisor endorse -> ENDORSED" "ENDORSED" "$(status_of "$ID1")"
# supervisor may not act after their stage
wf supervisor "$ID1" APPROVE "SUPERVISOR, DEPARTMENT"
check "supervisor blocked after endorsement" "ENDORSED" "$(status_of "$ID1")"

wf hr_user "$ID1" APPROVE
check "HR approves short leave" "APPROVED" "$(status_of "$ID1")"

# ── extended path (7 days): docs gate + admin review + VM final approval ─────
ID2=$(file_leave "Vacation Leave" 7 2026-09-01 2026-09-09)
say "-- extended-path application id=$ID2"
wf hr_user "$ID2" FORWARD_TO_SUPERVISOR
wf supervisor "$ID2" ENDORSE "SUPERVISOR, DEPARTMENT"
check "7-day leave ENDORSED" "ENDORSED" "$(status_of "$ID2")"

wf hr_user "$ID2" SEND_TO_ADMIN_REVIEW
check "docs gate blocks forward without docs" "ENDORSED" "$(status_of "$ID2")"

# attach a doc as HR (multipart)
CSRF=$(csrf_of hr_user)
echo "medical record" > "$JARS/doc.txt"
curl -s -b "$JARS/hr_user.jar" -c "$JARS/hr_user.jar" -L -o /dev/null \
  -F "_csrf=$CSRF" -F "supportingDocs=@$JARS/doc.txt" "$BASE/leave-workflow/$ID2/upload-docs"
get hr_user "/leave-workflow/$ID2/panel" >/dev/null
contains "document attached" '"hasDocs":true' "$JARS/last.html"

VM_BEFORE=$(unread vice_mayor)
wf hr_user "$ID2" SEND_TO_ADMIN_REVIEW
check "with docs -> FOR_ADMIN_REVIEW" "FOR_ADMIN_REVIEW" "$(status_of "$ID2")"
wf hr_user "$ID2" ADMIN_ENDORSE "GLORIA S. TIRADO"
check "7-day: admin review -> FOR_FINAL_APPROVAL (no council)" "FOR_FINAL_APPROVAL" "$(status_of "$ID2")"
VM_AFTER=$(unread vice_mayor)
[ "$VM_AFTER" -gt "$VM_BEFORE" ] && ok "vice-mayor notified at final approval" || bad "vice-mayor not notified ($VM_BEFORE -> $VM_AFTER)"

wf vice_mayor "$ID2" FINAL_APPROVE "ANGELA LEI ATIENZA"
check "vice-mayor approves on own account" "APPROVED" "$(status_of "$ID2")"

# ── long path (20 days): council review by council account ───────────────────
ID3=$(file_leave "Vacation Leave" 20 2026-10-01 2026-10-28)
say "-- long-path application id=$ID3"
wf hr_user "$ID3" FORWARD_TO_SUPERVISOR
wf supervisor "$ID3" ENDORSE "SUPERVISOR, DEPARTMENT"
CSRF=$(csrf_of hr_user)
curl -s -b "$JARS/hr_user.jar" -c "$JARS/hr_user.jar" -L -o /dev/null \
  -F "_csrf=$CSRF" -F "supportingDocs=@$JARS/doc.txt" "$BASE/leave-workflow/$ID3/upload-docs"
wf hr_user "$ID3" SEND_TO_ADMIN_REVIEW
CO_BEFORE=$(unread council_sec)
wf hr_user "$ID3" ADMIN_ENDORSE "GLORIA S. TIRADO"
check "20-day: admin review -> FOR_COUNCIL_REVIEW" "FOR_COUNCIL_REVIEW" "$(status_of "$ID3")"
CO_AFTER=$(unread council_sec)
[ "$CO_AFTER" -gt "$CO_BEFORE" ] && ok "council secretary notified" || bad "council secretary not notified ($CO_BEFORE -> $CO_AFTER)"

# vice-mayor cannot act at council stage
wf vice_mayor "$ID3" COUNCIL_ENDORSE "WRONG"
check "vice-mayor blocked at council stage" "FOR_COUNCIL_REVIEW" "$(status_of "$ID3")"

wf council_sec "$ID3" COUNCIL_ENDORSE "ATTY. HANS ROGER S. LUNA"
check "council endorses on own account" "FOR_FINAL_APPROVAL" "$(status_of "$ID3")"
wf vice_mayor "$ID3" FINAL_APPROVE "ANGELA LEI ATIENZA"
check "20-day leave approved by VM" "APPROVED" "$(status_of "$ID3")"

# ── denial + appeal + cancel ─────────────────────────────────────────────────
ID4=$(file_leave "Sick Leave" 4 2026-11-03 2026-11-06)
say "-- denial/appeal application id=$ID4"
EMP_BEFORE=$(unread emp_user)
wf hr_user "$ID4" FORWARD_TO_SUPERVISOR
wf supervisor "$ID4" DISAPPROVE "" "No staffing coverage"
check "supervisor denies with reason" "DISAPPROVED" "$(status_of "$ID4")"
EMP_AFTER=$(unread emp_user)
[ "$EMP_AFTER" -gt "$EMP_BEFORE" ] && ok "employee notified of decisions" || bad "employee not notified ($EMP_BEFORE -> $EMP_AFTER)"

# disapprove without remarks must be rejected
ID5=$(file_leave "Sick Leave" 2 2026-11-10 2026-11-11)
wf hr_user "$ID5" FORWARD_TO_SUPERVISOR
wf supervisor "$ID5" DISAPPROVE ""
check "disapprove without remarks rejected" "FOR_ENDORSEMENT" "$(status_of "$ID5")"

# employee appeals ID4
post emp_user "/appealMyLeave/$ID4" --data-urlencode "remarks=Please reconsider" >/dev/null
check "employee appeal -> APPEALED" "APPEALED" "$(status_of "$ID4")"
# employee cancels ID5 while pending at supervisor stage
post emp_user "/cancelMyLeave/$ID5" >/dev/null
check "employee cancels FOR_ENDORSEMENT leave" "CANCELLED" "$(status_of "$ID5")"

# ── year-end: coterminous exclusion + idempotency ────────────────────────────
post admin "/leave-year-end/run" --data-urlencode "year=2026" >/dev/null
contains "year-end summary reports coterminous exclusion" "coterminous (excluded)" "$JARS/last.html"
post admin "/leave-year-end/run" --data-urlencode "year=2026" >/dev/null
contains "second year-end run is idempotent" "already processed" "$JARS/last.html"

# ── Form 6 PDF for reviewer accounts ─────────────────────────────────────────
check "vice-mayor can open Form 6 PDF" "200" "$(get vice_mayor "/leaveForm6Pdf/$ID2")"
check "supervisor can open Form 6 PDF"  "200" "$(get supervisor "/leaveForm6Pdf/$ID1")"
check "receipt PDF stays HR-only" "403" "$(get supervisor "/leaveVerificationReceiptPdf/$ID2")"

say ""
say "== RESULT: $PASS passed, $FAIL failed =="
rm -rf "$JARS"
[ "$FAIL" -eq 0 ]
