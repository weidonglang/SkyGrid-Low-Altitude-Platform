$ErrorActionPreference = 'Stop'

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot '..')
$logDir = Join-Path $repoRoot 'logs\dev-stack'
New-Item -ItemType Directory -Path $logDir -Force | Out-Null

function Start-MavenService($name, $module, $port) {
  $outLog = Join-Path $logDir "$name.out.log"
  $errLog = Join-Path $logDir "$name.err.log"
  $args = @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-Command', "cd '$repoRoot'; mvn -pl $module spring-boot:run")
  Write-Host "[SkyGrid] Starting $name on port $port..."
  Start-Process -FilePath 'powershell.exe' -ArgumentList $args -WindowStyle Hidden -RedirectStandardOutput $outLog -RedirectStandardError $errLog | Out-Null
}

Write-Host '[SkyGrid] Starting infrastructure with docker compose...'
$previousErrorActionPreference = $ErrorActionPreference
$ErrorActionPreference = 'Continue'
docker info *> $null
$dockerInfoExitCode = $LASTEXITCODE
$ErrorActionPreference = $previousErrorActionPreference
if ($dockerInfoExitCode -ne 0) {
  Write-Host '[SkyGrid][FAIL] Docker daemon is not available. Start Docker Desktop, then rerun scripts\start-dev-stack.bat.' -ForegroundColor Red
  exit 1
}

$ErrorActionPreference = 'Continue'
docker compose -f (Join-Path $repoRoot 'docker-compose.infra.yml') up -d
$composeExitCode = $LASTEXITCODE
$ErrorActionPreference = $previousErrorActionPreference
if ($composeExitCode -ne 0) {
  Write-Host '[SkyGrid][FAIL] docker compose failed to start infrastructure. Check Docker Desktop and port conflicts, then rerun.' -ForegroundColor Red
  exit $composeExitCode
}

Write-Host '[SkyGrid] Waiting 20 seconds for infrastructure to become reachable...'
Start-Sleep -Seconds 20

Start-MavenService 'user-org-service' 'user-org-service' 8101
Start-MavenService 'resource-service' 'resource-service' 8102
Start-MavenService 'booking-service' 'booking-service' 8103
Start-MavenService 'conflict-notify-service' 'conflict-notify-service' 8104
Start-MavenService 'low-altitude-gateway' 'low-altitude-gateway' 8080

$webDir = Join-Path $repoRoot 'low-altitude-web'
if (Test-Path $webDir) {
  $webOut = Join-Path $logDir 'low-altitude-web.out.log'
  $webErr = Join-Path $logDir 'low-altitude-web.err.log'
  Write-Host '[SkyGrid] Starting low-altitude-web on port 5173...'
  Start-Process -FilePath 'npm.cmd' -ArgumentList @('run', 'dev', '--', '--host', '127.0.0.1') -WorkingDirectory $webDir -WindowStyle Hidden -RedirectStandardOutput $webOut -RedirectStandardError $webErr | Out-Null
}

Write-Host ''
Write-Host '[SkyGrid] Start commands have been issued.'
Write-Host "Logs: $logDir"
Write-Host 'Run scripts\check-dev-stack.bat after 60-90 seconds to verify service readiness.'
