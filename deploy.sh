#!/usr/bin/env bash
#
# deploy.sh — build the HRIS WAR and deploy it to a production server.
#
# The target host is NOT hardcoded. Supply it via environment variables so
# no server address lives in this repository:
#
#   HRISP_DEPLOY_SERVER   user@host          (required)
#   HRISP_DEPLOY_KEY      ssh private key    (optional, defaults to ~/.ssh/id_rsa)
#   HRISP_DEPLOY_WARPATH  remote war path    (optional, see below)
#
# This app does NOT run under a system Tomcat (no /var/lib/tomcat/webapps).
# It runs as an embedded-Tomcat fat WAR launched by <warpath>/../restart.sh,
# which executes  java -jar <warpath>/hrisp.war.
# Therefore the WAR MUST be uploaded to that target/ path, not a webapps dir.
#
# The maven jasperreports-plugin recompiles every .jrxml -> .jasper during the
# build, so editing the PDS templates and running this script is all that's needed.
#
# Usage:  HRISP_DEPLOY_SERVER=user@host bash deploy.sh
set -euo pipefail

: "${HRISP_DEPLOY_SERVER:?Set HRISP_DEPLOY_SERVER to user@host (previous host has been decommissioned)}"
SERVER="$HRISP_DEPLOY_SERVER"
KEY="${HRISP_DEPLOY_KEY:-$HOME/.ssh/id_rsa}"
REMOTE_WAR="${HRISP_DEPLOY_WARPATH:-$HOME/hrisp-web01/target/hrisp.war}"
LOCAL_WAR="target/hrisp.war"

echo "==> [1/4] Building (mvn clean package -DskipTests) ..."
mvn clean package -DskipTests

if [ ! -f "$LOCAL_WAR" ]; then
  echo "ERROR: $LOCAL_WAR not found after build." >&2
  exit 1
fi

echo "==> [2/4] Uploading WAR to $SERVER:$REMOTE_WAR ..."
scp -i "$KEY" "$LOCAL_WAR" "$SERVER:$REMOTE_WAR"

echo "==> [3/4] Verifying md5 (local vs remote) ..."
LOCAL_MD5=$(md5sum "$LOCAL_WAR" | awk '{print $1}')
REMOTE_MD5=$(ssh -i "$KEY" "$SERVER" "md5sum $REMOTE_WAR" | awk '{print $1}')
echo "    local : $LOCAL_MD5"
echo "    remote: $REMOTE_MD5"
if [ "$LOCAL_MD5" != "$REMOTE_MD5" ]; then
  echo "ERROR: md5 mismatch — upload did not land correctly. Aborting restart." >&2
  exit 1
fi

echo "==> [4/4] Restarting app (bash ~/hrisp-web01/restart.sh) ..."
ssh -i "$KEY" "$SERVER" "bash ~/hrisp-web01/restart.sh"

echo "==> Done. Tail the log to confirm a clean start:"
echo "    ssh -i $KEY $SERVER 'tail -f ~/hrisp-web01/app.log'"
