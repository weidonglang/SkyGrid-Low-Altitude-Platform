@echo off
setlocal EnableExtensions
set "BASE_URL=http://127.0.0.1:8080"

echo [Phase02] Checking gateway health...
powershell -NoProfile -ExecutionPolicy Bypass -Command "try { $h=Invoke-RestMethod '%BASE_URL%/actuator/health' -ErrorAction Stop; $h | ConvertTo-Json -Depth 6; exit 0 } catch { Write-Host '[ERROR] Gateway is not reachable at %BASE_URL%'; Write-Host $_.Exception.Message; exit 1 }"
if errorlevel 1 exit /b 1

echo [Phase02] Requesting dev token...
set "TOKEN_FILE=%TEMP%\low_altitude_phase02_token.txt"
del "%TOKEN_FILE%" >nul 2>nul
powershell -NoProfile -ExecutionPolicy Bypass -Command "try { $r=Invoke-RestMethod -Method Post '%BASE_URL%/api/auth/dev-token?username=demo^&role=ADMIN' -ErrorAction Stop; if (-not $r.data.accessToken) { Write-Host '[ERROR] accessToken missing in response'; $r | ConvertTo-Json -Depth 8; exit 2 }; [System.IO.File]::WriteAllText('%TOKEN_FILE%', $r.data.accessToken.Trim(), [System.Text.Encoding]::ASCII); exit 0 } catch { Write-Host '[ERROR] Failed to request dev token'; Write-Host $_.Exception.Message; exit 1 }"
if errorlevel 1 exit /b 1
set /p TOKEN=<"%TOKEN_FILE%"
if "%TOKEN%"=="" (
  echo [ERROR] Token is empty. Check /api/auth/dev-token response and gateway route.
  exit /b 1
)

echo [Phase02] Token acquired.
echo [Phase02] User-org API checks...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$headers=@{Authorization='Bearer %TOKEN%'}; Invoke-RestMethod -Headers $headers '%BASE_URL%/api/user-org/organizations' -ErrorAction Stop | ConvertTo-Json -Depth 8"
if errorlevel 1 exit /b 1
powershell -NoProfile -ExecutionPolicy Bypass -Command "$headers=@{Authorization='Bearer %TOKEN%'}; Invoke-RestMethod -Headers $headers '%BASE_URL%/api/user-org/roles' -ErrorAction Stop | ConvertTo-Json -Depth 8"
if errorlevel 1 exit /b 1
powershell -NoProfile -ExecutionPolicy Bypass -Command "$headers=@{Authorization='Bearer %TOKEN%'}; Invoke-RestMethod -Headers $headers '%BASE_URL%/api/user-org/users' -ErrorAction Stop | ConvertTo-Json -Depth 8"
if errorlevel 1 exit /b 1
powershell -NoProfile -ExecutionPolicy Bypass -Command "$headers=@{Authorization='Bearer %TOKEN%'}; Invoke-RestMethod -Headers $headers '%BASE_URL%/api/user-org/approver-configs' -ErrorAction Stop | ConvertTo-Json -Depth 8"
if errorlevel 1 exit /b 1

echo [Phase02] Resource API checks...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$headers=@{Authorization='Bearer %TOKEN%'}; Invoke-RestMethod -Headers $headers '%BASE_URL%/api/resources/grids' -ErrorAction Stop | ConvertTo-Json -Depth 8"
if errorlevel 1 exit /b 1
powershell -NoProfile -ExecutionPolicy Bypass -Command "$headers=@{Authorization='Bearer %TOKEN%'}; Invoke-RestMethod -Headers $headers '%BASE_URL%/api/resources/levels' -ErrorAction Stop | ConvertTo-Json -Depth 8"
if errorlevel 1 exit /b 1
powershell -NoProfile -ExecutionPolicy Bypass -Command "$headers=@{Authorization='Bearer %TOKEN%'}; Invoke-RestMethod -Headers $headers '%BASE_URL%/api/resources/time-slots' -ErrorAction Stop | ConvertTo-Json -Depth 8"
if errorlevel 1 exit /b 1
powershell -NoProfile -ExecutionPolicy Bypass -Command "$headers=@{Authorization='Bearer %TOKEN%'}; Invoke-RestMethod -Headers $headers '%BASE_URL%/api/resources/route-templates/1' -ErrorAction Stop | ConvertTo-Json -Depth 8"
if errorlevel 1 exit /b 1

echo [Phase02] OK
endlocal
