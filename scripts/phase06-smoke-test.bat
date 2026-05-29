@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0phase06-smoke-test.ps1"
set ERR=%ERRORLEVEL%
if "%ERR%"=="0" (
  echo.
  echo [Phase06] Script finished with success.
) else (
  echo.
  echo [Phase06] Script failed with exit code %ERR%.
)
pause
exit /b %ERR%
