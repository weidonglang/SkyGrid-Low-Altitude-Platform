@echo off
setlocal
cd /d "%~dp0.."
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0phase04-smoke-test.ps1"
set EXIT_CODE=%ERRORLEVEL%
echo.
if "%EXIT_CODE%"=="0" (
  echo [Phase04] Completed successfully.
) else (
  echo [Phase04] FAILED. See error above.
)
pause
exit /b %EXIT_CODE%
