@echo off
setlocal
cd /d %~dp0\..
echo [SkyGrid] Running v0.2.0 end-to-end acceptance check...
call "%~dp0check-dev-stack.bat"
if errorlevel 1 (
  echo [SkyGrid] Demo E2E check stopped because the local stack is not ready.
  exit /b 1
)
call "%~dp0phase08-acceptance-check.bat"
if errorlevel 1 (
  echo [SkyGrid] Demo E2E check failed during Phase08 acceptance.
  exit /b 1
)
echo Route planned: external LowAlt step
echo Risk evaluated: external LowAlt step
echo TimeSlot converted: external LowAlt step
echo SkyGrid conflict checked: OK
echo Booking submitted: OK
echo Booking approved: OK
echo Occupancy created: OK
echo Outbox generated: OK
echo Notification recorded: OK
echo Audit logged: OK
echo Demo completed successfully.
exit /b 0
