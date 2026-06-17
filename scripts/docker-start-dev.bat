@echo off
setlocal
cd /d %~dp0\..
docker info >nul 2>nul
if errorlevel 1 (
  echo [SkyGrid][FAIL] Docker daemon is not available. Start Docker Desktop, then rerun this script.
  exit /b 1
)
docker compose -f docker-compose.dev.yml up -d
exit /b %ERRORLEVEL%
