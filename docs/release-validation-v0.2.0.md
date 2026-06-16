# SkyGrid v0.2.0 Validation

## Date

2026-06-16, Asia/Shanghai

## Environment

- OS: Windows 11 amd64
- JDK: Eclipse Temurin 17.0.18
- Maven: 3.9.10
- Node.js: 24.15.0
- npm: 11.12.1
- Docker: 29.3.0 CLI installed; Docker Desktop daemon was not running during validation

## Commands

```bat
cd /d E:\javacode\low-altitude-platform

mvn test

cd low-altitude-web
npm.cmd run build
cd ..

scripts\check-dev-stack.bat
scripts\start-dev-stack.bat
scripts\phase08-acceptance-check.bat
scripts\demo-e2e-check.bat
```

## Results

| Command | Result | Notes |
|---|---|---|
| `mvn test` | Passed | Reactor build completed successfully for common, gateway, user-org-service, resource-service, booking-service, and conflict-notify-service. |
| `low-altitude-web npm.cmd run build` | Passed | Vite build completed. Rollup reported existing large chunk warnings. |
| `scripts\check-dev-stack.bat` | Failed due to environment | MySQL 3306, Gateway 8080, and Nacos 8848 were reachable. Redis 6379, RabbitMQ 5672/15672, user-org-service 8101, resource-service 8102, booking-service 8103, conflict-notify-service 8104, and frontend 5173 were not reachable. |
| `scripts\start-dev-stack.bat` | Failed due to environment | Docker CLI exists, but Docker Desktop daemon was unavailable. The script exits before launching Java services in a half-started state. |
| `scripts\phase08-acceptance-check.bat` | Failed due to environment | Gateway health check passed, then the script stopped with a clear booking-service unavailable message at `127.0.0.1:8103`. |
| `scripts\demo-e2e-check.bat` | Failed due to environment | The script depends on `check-dev-stack.bat`, so it cannot proceed until Redis, RabbitMQ, business services, and frontend are running. |

## Verified v0.2.0 Changes

- Added local stack startup, stop, and health check scripts.
- Reworked Phase08 acceptance to print clear readiness and API failure reasons.
- Guarded `start-dev-stack.ps1` so Docker daemon or compose failures stop the script before backend services are launched.
- Added demo data preparation script using the existing idempotent MySQL initialization SQL.
- Added v0.2.0 integration demo scenario documentation.
- Added E2E demo check script that depends on real service readiness.

## Known Limitations

- Full E2E demo was not executed in this validation run because Redis, RabbitMQ, business services, and frontend were not running.
- Docker-based startup was not executed because Docker Desktop daemon was not available through the local Docker API.
- `prepare-demo-data.bat` requires the MySQL client on PATH and a reachable local MySQL instance.
- v0.2.0 keeps SkyGrid's existing route-template based booking contract. LowAlt maps richer route occupancy metadata into this contract for the first real integration step.
