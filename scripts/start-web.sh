#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../low-altitude-web"
if [ ! -d node_modules ]; then
  echo "[Phase05] node_modules not found. Running npm install..."
  npm install
fi
echo "[Phase05] Starting low-altitude-web at http://127.0.0.1:5173"
npm run dev
