@echo off
echo [SkyGrid] reset-demo-data uses the idempotent initialization path for v0.2.0.
echo [SkyGrid] It does not delete runtime data. For a clean database, recreate the Docker MySQL volume intentionally.
call "%~dp0prepare-demo-data.bat"
exit /b %ERRORLEVEL%
