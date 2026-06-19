@echo off
setlocal
cd /d %~dp0\..
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0demo-outbox-recovery.ps1"
exit /b %ERRORLEVEL%
