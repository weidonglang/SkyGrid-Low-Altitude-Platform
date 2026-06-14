$ErrorActionPreference = 'Stop'

$checks = @(
  @{ Name = 'Gateway'; Url = 'http://127.0.0.1:8080/actuator/health'; Required = $true },
  @{ Name = 'Nacos'; Url = 'http://127.0.0.1:8848/nacos/'; Required = $true },
  @{ Name = 'RabbitMQ Management'; Url = 'http://127.0.0.1:15672/'; Required = $true },
  @{ Name = 'user-org-service'; Url = 'http://127.0.0.1:8101/actuator/health'; Required = $true },
  @{ Name = 'resource-service'; Url = 'http://127.0.0.1:8102/actuator/health'; Required = $true },
  @{ Name = 'booking-service'; Url = 'http://127.0.0.1:8103/actuator/health'; Required = $true },
  @{ Name = 'conflict-notify-service'; Url = 'http://127.0.0.1:8104/actuator/health'; Required = $true },
  @{ Name = 'low-altitude-web'; Url = 'http://127.0.0.1:5173/'; Required = $false }
)

function Test-TcpPort($name, $hostName, $port) {
  $result = Test-NetConnection -ComputerName $hostName -Port $port -InformationLevel Quiet -WarningAction SilentlyContinue
  if ($result) {
    Write-Host "[OK] $name port ${hostName}:$port is reachable" -ForegroundColor Green
    return $true
  }
  Write-Host "[FAIL] $name port ${hostName}:$port is not reachable" -ForegroundColor Red
  return $false
}

function Test-HttpEndpoint($name, $url, $required) {
  try {
    $response = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 5
    if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 500) {
      Write-Host "[OK] $name $url -> HTTP $($response.StatusCode)" -ForegroundColor Green
      return $true
    }
    Write-Host "[FAIL] $name $url -> HTTP $($response.StatusCode)" -ForegroundColor Red
    return -not $required
  } catch {
    $message = $_.Exception.Message
    if ($required) {
      Write-Host "[FAIL] $name $url is unavailable: $message" -ForegroundColor Red
      return $false
    }
    Write-Host "[WARN] $name $url is unavailable: $message" -ForegroundColor Yellow
    return $true
  }
}

Write-Host '[SkyGrid] Checking local development stack...'
$ok = $true

$ok = (Test-TcpPort 'MySQL' '127.0.0.1' 3306) -and $ok
$ok = (Test-TcpPort 'Redis' '127.0.0.1' 6379) -and $ok
$ok = (Test-TcpPort 'RabbitMQ AMQP' '127.0.0.1' 5672) -and $ok

foreach ($check in $checks) {
  $result = Test-HttpEndpoint $check.Name $check.Url $check.Required
  $ok = $result -and $ok
}

if (-not $ok) {
  Write-Host ''
  Write-Host '[SkyGrid] Development stack is not ready.' -ForegroundColor Red
  Write-Host 'Run scripts\start-dev-stack.bat, then retry scripts\check-dev-stack.bat.'
  Write-Host 'If a single service remains down, see docs\local-runbook.md and docs\troubleshooting.md.'
  exit 1
}

Write-Host ''
Write-Host '[SkyGrid] Development stack is ready.' -ForegroundColor Green
