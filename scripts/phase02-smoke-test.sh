#!/usr/bin/env bash
set -euo pipefail

BASE_URL=${BASE_URL:-http://127.0.0.1:8080}

echo "[Phase02] Requesting dev token..."
TOKEN=$(curl -s -X POST "$BASE_URL/api/auth/dev-token?username=demo&role=ADMIN" | python -c "import sys,json; print(json.load(sys.stdin)['data']['token'])")
AUTH="Authorization: Bearer $TOKEN"

echo "[Phase02] User-org API checks..."
curl -s -H "$AUTH" "$BASE_URL/api/user-org/organizations" | python -m json.tool
curl -s -H "$AUTH" "$BASE_URL/api/user-org/roles" | python -m json.tool
curl -s -H "$AUTH" "$BASE_URL/api/user-org/users" | python -m json.tool
curl -s -H "$AUTH" "$BASE_URL/api/user-org/approver-configs" | python -m json.tool

echo "[Phase02] Resource API checks..."
curl -s -H "$AUTH" "$BASE_URL/api/resources/grids" | python -m json.tool
curl -s -H "$AUTH" "$BASE_URL/api/resources/levels" | python -m json.tool
curl -s -H "$AUTH" "$BASE_URL/api/resources/time-slots" | python -m json.tool
curl -s -H "$AUTH" "$BASE_URL/api/resources/route-templates/1" | python -m json.tool

echo "[Phase02] OK"
