$ErrorActionPreference = 'Stop'
$base = 'http://127.0.0.1:8080'

function Show-Json($obj) {
  $obj | ConvertTo-Json -Depth 20
}

function Invoke-Api($method, $url, $headers = $null, $body = $null) {
  if ($null -eq $body) {
    return Invoke-RestMethod -Method $method -Uri $url -Headers $headers
  }
  return Invoke-RestMethod -Method $method -Uri $url -Headers $headers -ContentType 'application/json; charset=utf-8' -Body (($body | ConvertTo-Json -Depth 20))
}

Write-Host '[Phase04] Checking gateway health...'
$health = Invoke-Api GET "$base/actuator/health"
Show-Json $health

Write-Host '[Phase04] Requesting ADMIN dev token...'
$tokenResp = Invoke-Api POST "$base/api/auth/dev-token?username=demo&role=ADMIN"
Show-Json $tokenResp
$token = $tokenResp.data.accessToken
if ([string]::IsNullOrWhiteSpace($token)) { throw 'Token is empty.' }
$headers = @{ Authorization = "Bearer $token" }
Write-Host '[Phase04] Token acquired.'

$bookingDate = (Get-Date).AddDays(2).ToString('yyyy-MM-dd')

$baseRequest = @{
  taskName = 'Phase04 conflict base inspection'
  orgId = 1
  applicantUserId = 1
  applicantName = 'demo'
  routeTemplateId = 1
  levelId = 1
  bookingDate = $bookingDate
  timeSlotIds = @(1, 2)
  applyReason = 'Phase04 conflict smoke test base booking'
  description = 'first booking should pass with risk warnings and then occupy resources'
}

Write-Host '[Phase04] Pre-check base booking: default route contains one RISK grid, so riskCount should be greater than 0 but canSubmit should be true.'
$pre = Invoke-Api POST "$base/api/bookings/pre-check" $headers $baseRequest
Show-Json $pre
if (-not $pre.data.canSubmit) { throw 'Base pre-check should allow submit.' }
if ($pre.data.riskCount -lt 1) { throw 'Base pre-check should produce at least one RISK warning because RT-DEMO-01 contains G-02-01.' }

Write-Host '[Phase04] Submit base booking...'
$created = Invoke-Api POST "$base/api/bookings" $headers $baseRequest
Show-Json $created
$bookingId = $created.data.id
if ($null -eq $bookingId) { throw 'Created booking id is empty.' }

Write-Host '[Phase04] Check persisted risk conflicts for base booking...'
$baseConflicts = Invoke-Api GET "$base/api/bookings/$bookingId/conflicts" $headers
Show-Json $baseConflicts
if ($baseConflicts.data.Count -lt 1) { throw 'Risk conflict records should be persisted for base booking.' }

Write-Host '[Phase04] Approve base booking and generate occupancies...'
$approveBody = @{ operatorUserId = 1; operatorName = 'admin'; comment = 'approved by phase04 smoke test' }
$approved = Invoke-Api POST "$base/api/bookings/$bookingId/approve" $headers $approveBody
Show-Json $approved
if ($approved.data.status -ne 'APPROVED') { throw 'Base booking was not approved.' }
if ($approved.data.occupancies.Count -ne 6) { throw "Expected 6 occupancies, got $($approved.data.occupancies.Count)." }

Write-Host '[Phase04] Duplicate pre-check: same route, level, date and time slots should produce HARD blocking conflicts.'
$dupReq = @{
  taskName = 'Phase04 duplicate inspection'
  orgId = 1
  applicantUserId = 1
  applicantName = 'demo'
  routeTemplateId = 1
  levelId = 1
  bookingDate = $bookingDate
  timeSlotIds = @(1, 2)
  applyReason = 'duplicate conflict test'
  description = 'should be blocked by existing occupancies'
}
$dupPre = Invoke-Api POST "$base/api/bookings/pre-check" $headers $dupReq
Show-Json $dupPre
if ($dupPre.data.canSubmit) { throw 'Duplicate pre-check should not allow submit.' }
if ($dupPre.data.blockingCount -lt 1) { throw 'Duplicate pre-check should produce blocking conflicts.' }

Write-Host '[Phase04] Duplicate submit should fail with conflict.'
$duplicateBlocked = $false
try {
  $dupSubmit = Invoke-Api POST "$base/api/bookings" $headers $dupReq
  Show-Json $dupSubmit
} catch {
  $duplicateBlocked = $true
  Write-Host '[Phase04] Duplicate submit blocked as expected.'
  Write-Host $_.Exception.Message
}
if (-not $duplicateBlocked) { throw 'Duplicate submit unexpectedly succeeded.' }

Write-Host '[Phase04] No-fly route pre-check: RT-NOFLY-01 should be blocked by NO_FLY grid G-02-02.'
$routes = Invoke-Api GET "$base/api/resources/route-templates" $headers
$noFlyRoute = $routes.data | Where-Object { $_.routeCode -eq 'RT-NOFLY-01' } | Select-Object -First 1
if ($null -eq $noFlyRoute) { throw 'RT-NOFLY-01 is missing. Please rerun docker\mysql\local-root-init.sql with utf8mb4.' }
$noFlyReq = @{
  taskName = 'Phase04 no fly inspection'
  orgId = 1
  applicantUserId = 1
  applicantName = 'demo'
  routeTemplateId = $noFlyRoute.id
  levelId = 1
  bookingDate = $bookingDate
  timeSlotIds = @(1)
  applyReason = 'no fly conflict test'
  description = 'should be blocked by no-fly grid'
}
$noFlyPre = Invoke-Api POST "$base/api/bookings/pre-check" $headers $noFlyReq
Show-Json $noFlyPre
if ($noFlyPre.data.canSubmit) { throw 'No-fly route should not allow submit.' }

Write-Host '[Phase04] Cancel base booking and release occupancies...'
$cancelBody = @{ operatorUserId = 1; operatorName = 'admin'; cancelReason = 'phase04 smoke test finished' }
$cancelled = Invoke-Api POST "$base/api/bookings/$bookingId/cancel" $headers $cancelBody
Show-Json $cancelled
if ($cancelled.data.status -ne 'CANCELLED') { throw 'Base booking was not cancelled.' }

Write-Host '[Phase04] Check global conflict records endpoint...'
$conflictList = Invoke-Api GET "$base/api/bookings/conflicts?bookingId=$bookingId" $headers
Show-Json $conflictList

Write-Host '[Phase04] OK'
