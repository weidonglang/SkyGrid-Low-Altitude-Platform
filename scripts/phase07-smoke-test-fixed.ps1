$ErrorActionPreference = 'Stop'
$Base = 'http://127.0.0.1:8080'
$Booking = 'http://127.0.0.1:8103'
$Notify = 'http://127.0.0.1:8104'

function Show($title, $obj) {
  Write-Host "`n$title" -ForegroundColor Cyan
  $obj | ConvertTo-Json -Depth 20 | Write-Host
}

Write-Host '[Phase07] Checking gateway health...'
$health = Invoke-RestMethod "$Base/actuator/health"
Show '[Phase07] Gateway health' $health

Write-Host '[Phase07] Requesting ADMIN dev token...'
$tokenResp = Invoke-RestMethod -Method Post "$Base/api/auth/dev-token?username=demo&role=ADMIN"
if (-not $tokenResp.success -or -not $tokenResp.data.accessToken) { throw 'Failed to acquire token.' }
$token = $tokenResp.data.accessToken
$headers = @{ Authorization = "Bearer $token" }
Write-Host '[Phase07] Token acquired.'

Write-Host '[Phase07] Checking booking-service Prometheus endpoint...'
# The first scrape may happen before booking-service has recorded any HTTP-server timer.
# Therefore, use stable JVM/process metrics to verify the Prometheus endpoint itself.
$prom = Invoke-WebRequest "$Booking/actuator/prometheus" -UseBasicParsing
if (($prom.Content -notmatch 'jvm_memory_used_bytes') -and ($prom.Content -notmatch 'process_uptime_seconds') -and ($prom.Content -notmatch 'application_started_time_seconds')) {
  throw 'Prometheus endpoint is reachable but does not look like Prometheus text output.'
}
Write-Host '[Phase07] Prometheus actuator endpoint is available.'

$summary = Invoke-RestMethod -Headers $headers "$Base/api/bookings/governance/summary"
Show '[Phase07] Governance summary' $summary

$outboxSummary = Invoke-RestMethod -Headers $headers "$Base/api/bookings/outbox/summary"
Show '[Phase07] Outbox summary' $outboxSummary

$notifySummary = Invoke-RestMethod -Headers $headers "$Base/api/notifications/summary"
Show '[Phase07] Notification summary' $notifySummary

Write-Host '[Phase07] Triggering RateLimiter. At least one request should be RATE_LIMITED.'
$rateLimited = 0
for ($i = 1; $i -le 12; $i++) {
  $resp = Invoke-RestMethod -Headers $headers "$Base/api/bookings/governance/rate-limited?label=phase07-$i"
  if (-not $resp.success -or $resp.code -eq 'RATE_LIMITED') { $rateLimited++ }
  Write-Host "  request=$i success=$($resp.success) code=$($resp.code)"
}
if ($rateLimited -lt 1) { throw 'RateLimiter did not reject any request. Check resilience4j.ratelimiter config.' }
Write-Host "[Phase07] RateLimiter rejected $rateLimited request(s) as expected."

Write-Host '[Phase07] Triggering CircuitBreaker fallback with fail=true.'
$circuit = Invoke-RestMethod -Headers $headers "$Base/api/bookings/governance/circuit?fail=true"
Show '[Phase07] CircuitBreaker response' $circuit
if ($circuit.message -notmatch 'degraded') { throw 'CircuitBreaker fallback was not triggered.' }

Write-Host '[Phase07] Triggering Retry demo. It should succeed after transient failures.'
$key = [guid]::NewGuid().ToString('N')
$retry = Invoke-RestMethod -Headers $headers "$Base/api/bookings/governance/retry?key=$key&failTimes=2"
Show '[Phase07] Retry response' $retry
if ($retry.data.result -ne 'RETRY_SUCCESS') { throw 'Retry demo did not succeed after retries.' }

Start-Sleep -Milliseconds 500
$prom2 = Invoke-WebRequest "$Booking/actuator/prometheus" -UseBasicParsing
if ($prom2.Content -notmatch 'low_altitude_governance_rate_limited_total') { throw 'Governance custom metrics were not found in Prometheus endpoint.' }
if ($prom2.Content -notmatch 'low_altitude_outbox_pending') { throw 'Outbox custom metrics were not found in Prometheus endpoint.' }
Write-Host '[Phase07] Custom metrics are exposed from booking-service.'

try {
  $ready = Invoke-WebRequest 'http://127.0.0.1:9090/-/ready' -UseBasicParsing -TimeoutSec 2
  Write-Host '[Phase07] Prometheus container appears to be running on :9090.'
} catch {
  Write-Host '[Phase07] Prometheus container is not running. This is OK for code smoke test; start it with scripts\start-monitor.bat when you want Grafana.' -ForegroundColor Yellow
}

Write-Host "`n[Phase07] OK"
