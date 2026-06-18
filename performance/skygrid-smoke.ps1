$ErrorActionPreference = 'Stop'

$Gateway = ${env:SKYGRID_GATEWAY_URL}
if ([string]::IsNullOrWhiteSpace($Gateway)) {
  $Gateway = 'http://127.0.0.1:8080'
}

$iterations = 20
$paths = @(
  '/api/dashboard/overview',
  '/api/dashboard/message-health',
  '/api/dashboard/service-health'
)

try {
  $tokenResp = Invoke-RestMethod -Method POST -Uri "$Gateway/api/auth/dev-token?username=demo&role=ADMIN" -TimeoutSec 10
} catch {
  Write-Host "[Perf][FAIL] Gateway is unavailable: $($_.Exception.Message)" -ForegroundColor Red
  exit 1
}

$headers = @{ Authorization = "Bearer $($tokenResp.data.accessToken)" }
$rows = @()

foreach ($path in $paths) {
  for ($i = 1; $i -le $iterations; $i++) {
    $watch = [System.Diagnostics.Stopwatch]::StartNew()
    try {
      $response = Invoke-WebRequest -Uri "$Gateway$path" -Headers $headers -UseBasicParsing -TimeoutSec 10
      $watch.Stop()
      $rows += [pscustomobject]@{ path = $path; status = $response.StatusCode; elapsedMs = $watch.ElapsedMilliseconds }
    } catch {
      $watch.Stop()
      $rows += [pscustomobject]@{ path = $path; status = 'ERROR'; elapsedMs = $watch.ElapsedMilliseconds }
    }
  }
}

$resultDir = Join-Path (Resolve-Path (Join-Path $PSScriptRoot '..')) 'performance\results'
New-Item -ItemType Directory -Path $resultDir -Force | Out-Null
$csv = Join-Path $resultDir 'skygrid-smoke.csv'
$rows | Export-Csv -Path $csv -NoTypeInformation -Encoding UTF8

Write-Host "[Perf] Smoke result written to $csv"
$rows | Group-Object path | ForEach-Object {
  $items = $_.Group
  $avg = [math]::Round(($items | Measure-Object elapsedMs -Average).Average, 2)
  Write-Host "[Perf] $($_.Name) avg=${avg}ms count=$($items.Count)"
}
