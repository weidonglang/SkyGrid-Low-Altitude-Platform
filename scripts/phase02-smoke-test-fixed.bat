@echo off
setlocal EnableExtensions EnableDelayedExpansion
set "BASE_URL=http://127.0.0.1:8080"

echo [Phase02] Checking gateway health...
powershell -NoProfile -ExecutionPolicy Bypass -Command "try { Invoke-RestMethod '%BASE_URL%/actuator/health' -ErrorAction Stop | ConvertTo-Json -Depth 5; exit 0 } catch { Write-Host '[ERROR] Gateway is not reachable at %BASE_URL%'; Write-Host $_.Exception.Message; exit 1 }"
if errorlevel 1 exit /b 1

echo [Phase02] Requesting dev token...
set "TOKEN="
for /f "usebackq delims=" %%i in (`powershell -NoProfile -ExecutionPolicy Bypass -Command "try { $r=Invoke-RestMethod -Method Post '%BASE_URL%/api/auth/dev-token?username=demo&role=ADMIN' -ErrorAction Stop; $r.data.accessToken } catch { Write-Host '[ERROR] Failed to request dev token'; Write-Host $_.Exception.Message; exit 1 }"`) do set "TOKEN=%%i"

if not defined TOKEN (
  echo [ERROR] Token is empty. Check /api/auth/dev-token response format and gateway route.
  exit /b 1
)

echo [Phase02] Token acquired.
echo [Phase02] User-org API checks...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-RestMethod -Headers @{Authorization='Bearer !TOKEN!'} '%BASE_URL%/api/user-org/organizations' | ConvertTo-Json -Depth 8"
powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-RestMethod -Headers @{Authorization='Bearer !TOKEN!'} '%BASE_URL%/api/user-org/roles' | ConvertTo-Json -Depth 8"
powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-RestMethod -Headers @{Authorization='Bearer !TOKEN!'} '%BASE_URL%/api/user-org/users' | ConvertTo-Json -Depth 8"
powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-RestMethod -Headers @{Authorization='Bearer !TOKEN!'} '%BASE_URL%/api/user-org/approver-configs' | ConvertTo-Json -Depth 8"

echo [Phase02] Resource API checks...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-RestMethod -Headers @{Authorization='Bearer !TOKEN!'} '%BASE_URL%/api/resources/grids' | ConvertTo-Json -Depth 8"
powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-RestMethod -Headers @{Authorization='Bearer !TOKEN!'} '%BASE_URL%/api/resources/levels' | ConvertTo-Json -Depth 8"
powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-RestMethod -Headers @{Authorization='Bearer !TOKEN!'} '%BASE_URL%/api/resources/time-slots' | ConvertTo-Json -Depth 8"
powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-RestMethod -Headers @{Authorization='Bearer !TOKEN!'} '%BASE_URL%/api/resources/route-templates/1' | ConvertTo-Json -Depth 8"

echo [Phase02] OK
endlocal
