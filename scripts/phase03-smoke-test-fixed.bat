@echo off
setlocal
set SCRIPT_DIR=%~dp0
powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT_DIR%phase03-smoke-test-fixed.ps1"
set EXIT_CODE=%ERRORLEVEL%
echo.
if not "%EXIT_CODE%"=="0" (
  echo [Phase03] FAILED. Please scroll up to read the error details.
) else (
  echo [Phase03] Completed successfully.
)
echo.
pause
exit /b %EXIT_CODE%
