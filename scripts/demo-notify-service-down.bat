@echo off
echo [SkyGrid] Notify-service-down demo:
echo 1. Stop conflict-notify-service with scripts\stop-dev-stack.bat or stop only port 8104 manually.
echo 2. Submit and approve a booking.
echo 3. Check /api/bookings/outbox/summary for PENDING or FAILED messages.
echo 4. Restart conflict-notify-service.
echo 5. Run scripts\demo-outbox-recovery.bat to dispatch and verify notification records.
exit /b 0
