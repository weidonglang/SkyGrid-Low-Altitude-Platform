#!/usr/bin/env bash
set -euo pipefail

echo '[Phase03] Checking gateway health...'
curl -s http://127.0.0.1:8080/actuator/health | python3 -m json.tool || true

echo '[Phase03] Requesting ADMIN dev token...'
TOKEN=$(curl -s -X POST 'http://127.0.0.1:8080/api/auth/dev-token?username=demo&role=ADMIN' | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['accessToken'])")
DATE=$(python3 - <<'PY'
from datetime import date, timedelta
print((date.today()+timedelta(days=1)).isoformat())
PY
)

echo '[Phase03] Submitting booking...'
CREATE=$(curl -s -X POST 'http://127.0.0.1:8080/api/bookings' \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json; charset=utf-8' \
  -d "{\"taskName\":\"Phase03 demo inspection\",\"orgId\":1,\"applicantUserId\":1,\"applicantName\":\"demo\",\"routeTemplateId\":1,\"levelId\":1,\"bookingDate\":\"$DATE\",\"timeSlotIds\":[1,2],\"applyReason\":\"Phase03 smoke test\",\"description\":\"submit approve occupancy cancel\"}")
echo "$CREATE" | python3 -m json.tool
ID=$(echo "$CREATE" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['id'])")

echo '[Phase03] Approving booking...'
curl -s -X POST "http://127.0.0.1:8080/api/bookings/$ID/approve" \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json; charset=utf-8' \
  -d '{"operatorUserId":1,"operatorName":"admin","comment":"approved by phase03 smoke test"}' | python3 -m json.tool

echo '[Phase03] Checking occupancies...'
curl -s -H "Authorization: Bearer $TOKEN" "http://127.0.0.1:8080/api/bookings/$ID/occupancies" | python3 -m json.tool

echo '[Phase03] Cancelling booking...'
curl -s -X POST "http://127.0.0.1:8080/api/bookings/$ID/cancel" \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json; charset=utf-8' \
  -d '{"operatorUserId":1,"operatorName":"admin","cancelReason":"phase03 smoke test finished"}' | python3 -m json.tool

echo '[Phase03] OK'
