@echo off
setlocal
cd /d %~dp0\..
where powershell >nul 2>nul
if errorlevel 1 (
  echo [SkyGrid] PowerShell not found.
  exit /b 1
)
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0prepare-demo-data.ps1"
exit /b %ERRORLEVEL%
