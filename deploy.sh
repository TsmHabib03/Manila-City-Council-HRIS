#!/usr/bin/env bash
#
# deploy.sh — build the HRIS WAR and deploy it to the production server.
#
# This app does NOT run under a system Tomcat (no /var/lib/tomcat/webapps).
# It runs as an embedded-Tomcat fat WAR launched by ~/hrisp-web01/restart.sh,
# which executes  java -jar /home/habib/hrisp-web01/target/hrisp.war.
# Therefore the WAR MUST be uploaded to that target/ path, not a webapps dir.
#
# The maven jasperreports-plugin recompiles every .jrxml -> .jasper during the
# build, so editing the PDS templates and running this script is all that's needed.
#
# Usage:  bash deploy.sh
set -euo pipefail

SERVER="habib@35.185.180.249"
KEY="$HOME/.ssh/id_rsa"
REMOTE_WAR="/home/habib/hrisp-web01/target/hrisp.war"
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
