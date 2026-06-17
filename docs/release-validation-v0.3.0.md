# SkyGrid v0.3.0 Validation

## Date

2026-06-17, Asia/Shanghai

## Environment

- OS: Windows 11 amd64
- JDK: Eclipse Temurin 17.0.18
- Maven: 3.9.10
- Node.js: 24.15.0
- npm: 11.12.1
- Docker CLI: 29.3.0

## Commands

```bat
cd /d E:\javacode\low-altitude-platform

mvn test
scripts\docker-start-dev.bat
scripts\check-dev-stack.bat
```

## Results

| Command | Result | Notes |
|---|---|---|
| `mvn test` | Passed | Full Maven reactor completed. `booking-service` now runs 5 unit tests for dashboard and conflict suggestions. |
| `scripts\docker-start-dev.bat` | Failed due to environment | Docker CLI exists, but Docker Desktop daemon was unavailable. The script now prints a clear failure and exits. |
| `scripts\check-dev-stack.bat` | Failed due to environment | MySQL 3306 was reachable. Redis, RabbitMQ, Gateway, Nacos, user-org-service, resource-service, booking-service, conflict-notify-service, and frontend were unavailable. |

## Verified Changes

- Added Docker Compose wrapper scripts for development middleware.
- Added Docker deployment and port reference documentation.
- Added Docker daemon guards so startup and shutdown scripts do not fail with opaque Docker API messages.

## Known Limitations

- Full one-command startup was not executed because Docker Desktop daemon was not running.
- Java services still run through local Maven commands because service Dockerfiles are not present in this repository.
