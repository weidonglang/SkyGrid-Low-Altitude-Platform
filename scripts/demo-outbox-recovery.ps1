$ErrorActionPreference = 'Stop'

$Gateway = ${env:SKYGRID_GATEWAY_URL}
if ([string]::IsNullOrWhiteSpace($Gateway)) {
  $Gateway = 'http://127.0.0.1:8080'
}

function Invoke-Json($method, $url, $headers=$null, $body=$null) {
  if ($body -ne $null) {
    return Invoke-RestMethod -Method $method -Uri $url -Headers $headers -ContentType 'application/json' -Body ($body | ConvertTo-Json -Depth 20) -TimeoutSec 15
  }
  return Invoke-RestMethod -Method $method -Uri $url -Headers $headers -TimeoutSec 15
}

function Fail($message) {
  Write-Host "[OutboxDemo][FAIL] $message" -ForegroundColor Red
  exit 1
}

try {
  $tokenResp = Invoke-Json POST "$Gateway/api/auth/dev-token?username=demo&role=ADMIN"
} catch {
  Fail "Gateway is unavailable. Start SkyGrid first. Detail: $($_.Exception.Message)"
}

if (-not $tokenResp.success -or -not $tokenResp.data.accessToken) {
  Fail 'Cannot acquire dev token.'
}

$headers = @{ Authorization = "Bearer $($tokenResp.data.accessToken)" }

$summaryBefore = Invoke-Json GET "$Gateway/api/bookings/outbox/summary" $headers
Write-Host '[OutboxDemo] Outbox before dispatch:'
$summaryBefore | ConvertTo-Json -Depth 10

$dispatch = Invoke-Json POST "$Gateway/api/bookings/outbox/dispatch?limit=20" $headers
Write-Host '[OutboxDemo] Dispatch result:'
$dispatch | ConvertTo-Json -Depth 10

$summaryAfter = Invoke-Json GET "$Gateway/api/bookings/outbox/summary" $headers
Write-Host '[OutboxDemo] Outbox after dispatch:'
$summaryAfter | ConvertTo-Json -Depth 10

$notify = Invoke-Json GET "$Gateway/api/notifications/summary" $headers
Write-Host '[OutboxDemo] Notification summary:'
$notify | ConvertTo-Json -Depth 10

Write-Host 'Outbox recovery demo completed.' -ForegroundColor Green
