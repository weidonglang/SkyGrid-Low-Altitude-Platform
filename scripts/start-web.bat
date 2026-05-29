@echo off
setlocal
cd /d %~dp0\..\low-altitude-web
if not exist node_modules (
  echo [Phase05] node_modules not found. Running npm install...
  call npm install
)
echo [Phase05] Starting low-altitude-web at http://127.0.0.1:5173
call npm run dev
pause
