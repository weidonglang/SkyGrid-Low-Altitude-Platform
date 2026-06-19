$ErrorActionPreference = 'Continue'

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot '..')

$ports = @(8080, 8101, 8102, 8103, 8104, 5173)
foreach ($port in $ports) {
  $connections = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
  foreach ($conn in $connections) {
    $processId = $conn.OwningProcess
    $process = Get-Process -Id $processId -ErrorAction SilentlyContinue
    if ($process) {
      Write-Host "[SkyGrid] Stopping process $($process.ProcessName) on port $port (PID $processId)"
      Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
    }
  }
}

Write-Host '[SkyGrid] Stopping infrastructure containers...'
docker compose -f (Join-Path $repoRoot 'docker-compose.infra.yml') down

Write-Host '[SkyGrid] Local development stack stopped.'
