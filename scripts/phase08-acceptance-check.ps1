$ErrorActionPreference = 'Stop'
$Gateway = 'http://127.0.0.1:8080'
$BookingService = 'http://127.0.0.1:8103'

function Show-Json($title, $obj) {
  Write-Host "`n[Phase08] $title" -ForegroundColor Cyan
  $obj | ConvertTo-Json -Depth 10
}

function Invoke-Json($method, $url, $headers=$null, $body=$null) {
  if ($body -ne $null) {
    return Invoke-RestMethod -Method $method -Uri $url -Headers $headers -ContentType 'application/json' -Body ($body | ConvertTo-Json -Depth 20)
  }
  return Invoke-RestMethod -Method $method -Uri $url -Headers $headers
}

Write-Host '[Phase08] Checking gateway health...'
$health = Invoke-Json GET "$Gateway/actuator/health"
Show-Json 'Gateway health' $health
if ($health.status -ne 'UP') { throw 'Gateway is not UP.' }

Write-Host '[Phase08] Requesting ADMIN token...'
$tokenResp = Invoke-Json POST "$Gateway/api/auth/dev-token?username=demo&role=ADMIN"
if (-not $tokenResp.success -or -not $tokenResp.data.accessToken) { throw 'Cannot get ADMIN token.' }
$token = $tokenResp.data.accessToken
$headers = @{ Authorization = "Bearer $token" }
Write-Host '[Phase08] Token acquired.'

$orgs = Invoke-Json GET "$Gateway/api/user-org/organizations" $headers
if (-not $orgs.success -or @($orgs.data).Count -lt 1) { throw 'Organization API check failed.' }
Show-Json 'Organization sample' $orgs

$grids = Invoke-Json GET "$Gateway/api/resources/grids" $headers
if (-not $grids.success -or @($grids.data).Count -lt 4) { throw 'Grid API check failed.' }
Show-Json 'Grid sample' $grids

$routes = Invoke-Json GET "$Gateway/api/resources/route-templates/1" $headers
if (-not $routes.success) { throw 'Route template API check failed.' }
Show-Json 'Route template #1' $routes

$summary = Invoke-Json GET "$Gateway/api/bookings/governance/summary" $headers
if (-not $summary.success) { throw 'Governance summary failed.' }
Show-Json 'Governance summary' $summary

$outbox = Invoke-Json GET "$Gateway/api/bookings/outbox/summary" $headers
if (-not $outbox.success) { throw 'Outbox summary failed.' }
Show-Json 'Outbox summary' $outbox

$notify = Invoke-Json GET "$Gateway/api/notifications/summary" $headers
if (-not $notify.success) { throw 'Notification summary failed.' }
Show-Json 'Notification summary' $notify

$prom = Invoke-WebRequest -Uri "$BookingService/actuator/prometheus" -UseBasicParsing
if ($prom.StatusCode -ne 200) { throw 'booking-service prometheus endpoint failed.' }
if ($prom.Content -notmatch 'jvm_memory|process_uptime|application_started') { throw 'Prometheus output does not contain stable JVM/process/application metrics.' }
Write-Host '[Phase08] booking-service prometheus endpoint is available.'

Write-Host '[Phase08] OK' -ForegroundColor Green
