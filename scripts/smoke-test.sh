#!/usr/bin/env bash
set -euo pipefail
GATEWAY="${GATEWAY:-http://localhost:8080}"

echo "Requesting dev token..."
TOKEN_RESPONSE="$(curl -s -X POST "${GATEWAY}/api/auth/dev-token?username=demo&role=APPLICANT")"
echo "${TOKEN_RESPONSE}"

TOKEN="$(TOKEN_RESPONSE="${TOKEN_RESPONSE}" python3 - <<'PYSMOKE'
import json, os
print(json.loads(os.environ["TOKEN_RESPONSE"])["data"]["accessToken"])
PYSMOKE
)"

echo "Calling protected phase-01 endpoints..."
curl -s -H "Authorization: Bearer ${TOKEN}" "${GATEWAY}/api/users/bootstrap" | python3 -m json.tool
curl -s -H "Authorization: Bearer ${TOKEN}" "${GATEWAY}/api/resources/bootstrap" | python3 -m json.tool
curl -s -H "Authorization: Bearer ${TOKEN}" "${GATEWAY}/api/bookings/bootstrap" | python3 -m json.tool
curl -s -H "Authorization: Bearer ${TOKEN}" "${GATEWAY}/api/conflicts/bootstrap" | python3 -m json.tool
