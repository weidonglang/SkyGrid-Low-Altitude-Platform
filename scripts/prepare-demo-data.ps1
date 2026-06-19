$ErrorActionPreference = 'Stop'

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot '..')
$initSql = Join-Path $repoRoot 'docker\mysql\init.sql'

if (-not (Test-Path $initSql)) {
  Write-Host "[SkyGrid][FAIL] Demo initialization SQL not found: $initSql" -ForegroundColor Red
  exit 1
}

Write-Host '[SkyGrid] Preparing demo data with idempotent MySQL initialization SQL...'
Write-Host "[SkyGrid] SQL: $initSql"

$mysql = Get-Command mysql -ErrorAction SilentlyContinue
if ($mysql -eq $null) {
  Write-Host '[SkyGrid][FAIL] mysql client is not available on PATH.' -ForegroundColor Red
  Write-Host 'Install MySQL client tools or run the Docker initialization path in docs\database-initialization.md.'
  exit 1
}

$password = ${env:MYSQL_ROOT_PASSWORD}
if ([string]::IsNullOrWhiteSpace($password)) {
  $password = '123123'
}

mysql -h 127.0.0.1 -P 3306 -uroot "-p$password" --default-character-set=utf8mb4 "--execute=source $initSql"
if ($LASTEXITCODE -ne 0) {
  Write-Host '[SkyGrid][FAIL] MySQL demo data initialization failed.' -ForegroundColor Red
  exit $LASTEXITCODE
}

Write-Host '[SkyGrid] Demo data prepared successfully.' -ForegroundColor Green
