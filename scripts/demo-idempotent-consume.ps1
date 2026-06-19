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
  Write-Host "[IdempotentDemo][FAIL] $message" -ForegroundColor Red
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
$outbox = Invoke-Json GET "$Gateway/api/bookings/outbox?limit=1" $headers
if (-not $outbox.success -or @($outbox.data).Count -lt 1) {
  Fail 'No outbox message is available. Approve a booking first.'
}

$messageId = $outbox.data[0].id
Write-Host "[IdempotentDemo] Republishing outbox message $messageId twice..."
Invoke-Json POST "$Gateway/api/bookings/outbox/$messageId/republish" $headers | Out-Null
Invoke-Json POST "$Gateway/api/bookings/outbox/$messageId/republish" $headers | Out-Null

Start-Sleep -Seconds 3

$records = Invoke-Json GET "$Gateway/api/notifications/idempotent?limit=20" $headers
Write-Host '[IdempotentDemo] Idempotent records:'
$records | ConvertTo-Json -Depth 10

Write-Host 'Idempotent consume demo completed.' -ForegroundColor Green
