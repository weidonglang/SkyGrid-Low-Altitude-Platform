@echo off
setlocal
cd /d %~dp0\..
where powershell >nul 2>nul
if errorlevel 1 (
  echo [Phase08] PowerShell not found.
  pause
  exit /b 1
)
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0phase08-acceptance-check.ps1"
set EXITCODE=%ERRORLEVEL%
if %EXITCODE%==0 (
  echo.
  echo [Phase08] Completed successfully.
) else (
  echo.
  echo [Phase08] FAILED with exit code %EXITCODE%.
)
pause
exit /b %EXITCODE%
