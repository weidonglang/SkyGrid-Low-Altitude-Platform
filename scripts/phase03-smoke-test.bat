@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -Command "^$ErrorActionPreference='Stop'; ^
  Write-Host '[Phase03] Checking gateway health...'; ^
  Invoke-RestMethod 'http://127.0.0.1:8080/actuator/health' | ConvertTo-Json -Depth 6; ^
  Write-Host '[Phase03] Requesting ADMIN dev token...'; ^
  ^$tokenResp = Invoke-RestMethod -Method Post 'http://127.0.0.1:8080/api/auth/dev-token?username=demo&role=ADMIN'; ^
  ^$token = ^$tokenResp.data.accessToken; ^
  if ([string]::IsNullOrWhiteSpace(^$token)) { throw 'accessToken is empty' }; ^
  ^$headers = @{ Authorization = 'Bearer ' + ^$token }; ^
  ^$date = (Get-Date).AddDays(1).ToString('yyyy-MM-dd'); ^
  ^$bodyObj = @{ taskName='Phase03 demo inspection'; orgId=1; applicantUserId=1; applicantName='demo'; routeTemplateId=1; levelId=1; bookingDate=^$date; timeSlotIds=@(1,2); applyReason='Phase03 smoke test'; description='submit -> approve -> occupancy -> cancel' }; ^
  ^$json = ^$bodyObj | ConvertTo-Json -Depth 8; ^
  Write-Host '[Phase03] Submitting booking...'; ^
  ^$created = Invoke-RestMethod -Method Post -Headers ^$headers -ContentType 'application/json; charset=utf-8' -Body ^$json 'http://127.0.0.1:8080/api/bookings'; ^
  ^$created | ConvertTo-Json -Depth 10; ^
  ^$id = ^$created.data.id; ^
  if (-not ^$id) { throw 'created booking id is empty' }; ^
  Write-Host ('[Phase03] Created booking id=' + ^$id); ^
  Write-Host '[Phase03] Getting booking detail...'; ^
  Invoke-RestMethod -Headers ^$headers ('http://127.0.0.1:8080/api/bookings/' + ^$id) | ConvertTo-Json -Depth 10; ^
  Write-Host '[Phase03] Approving booking...'; ^
  ^$approveJson = @{ operatorUserId=1; operatorName='admin'; comment='approved by phase03 smoke test' } | ConvertTo-Json; ^
  Invoke-RestMethod -Method Post -Headers ^$headers -ContentType 'application/json; charset=utf-8' -Body ^$approveJson ('http://127.0.0.1:8080/api/bookings/' + ^$id + '/approve') | ConvertTo-Json -Depth 10; ^
  Write-Host '[Phase03] Checking occupancies...'; ^
  ^$occ = Invoke-RestMethod -Headers ^$headers ('http://127.0.0.1:8080/api/bookings/' + ^$id + '/occupancies'); ^
  ^$occ | ConvertTo-Json -Depth 10; ^
  if (^$occ.data.Count -lt 1) { throw 'occupancy list is empty after approval' }; ^
  Write-Host '[Phase03] Cancelling booking and releasing occupancies...'; ^
  ^$cancelJson = @{ operatorUserId=1; operatorName='admin'; cancelReason='phase03 smoke test finished' } | ConvertTo-Json; ^
  Invoke-RestMethod -Method Post -Headers ^$headers -ContentType 'application/json; charset=utf-8' -Body ^$cancelJson ('http://127.0.0.1:8080/api/bookings/' + ^$id + '/cancel') | ConvertTo-Json -Depth 10; ^
  Write-Host '[Phase03] OK'"
if %errorlevel% neq 0 (
  echo.
  echo [Phase03] FAILED. See error above.
  pause
  exit /b 1
)
echo.
echo [Phase03] Completed successfully.
endlocal
