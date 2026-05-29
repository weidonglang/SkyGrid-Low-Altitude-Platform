@echo off
setlocal
cd /d "%~dp0.."
powershell -ExecutionPolicy Bypass -File "%~dp0phase07-smoke-test-fixed.ps1"
set EXIT_CODE=%ERRORLEVEL%
echo.
if not "%EXIT_CODE%"=="0" (
  echo [Phase07] FAILED with exit code %EXIT_CODE%.
) else (
  echo [Phase07] Script finished successfully.
)
pause
exit /b %EXIT_CODE%
