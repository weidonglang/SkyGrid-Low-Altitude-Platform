@echo off
setlocal
cd /d %~dp0\..
docker info >nul 2>nul
if errorlevel 1 (
  echo [SkyGrid][WARN] Docker daemon is not available. No Docker services were stopped.
  exit /b 0
)
docker compose -f docker-compose.dev.yml down
exit /b %ERRORLEVEL%
