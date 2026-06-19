# SkyGrid v0.5.0 Validation

## Date

2026-06-17, Asia/Shanghai

## Environment

- OS: Windows 11 amd64
- JDK: Eclipse Temurin 17.0.18
- Maven: 3.9.10

## Commands

```bat
cd /d E:\javacode\low-altitude-platform

mvn test
scripts\demo-outbox-recovery.bat
scripts\demo-idempotent-consume.bat
scripts\demo-notify-service-down.bat
```

## Results

| Command | Result | Notes |
|---|---|---|
| `mvn test` | Passed | Full Maven reactor completed. `booking-service` ran 5 unit tests covering dashboard aggregation and conflict suggestions. |
| `scripts\demo-outbox-recovery.bat` | Failed due to environment | Gateway was unavailable, and the script stopped with a clear preflight failure. |
| `scripts\demo-idempotent-consume.bat` | Failed due to environment | Gateway was unavailable, and the script stopped before publishing duplicate messages. |
| `scripts\demo-notify-service-down.bat` | Passed | Printed the manual service-down scenario steps and expected evidence checks. |

## Verified Changes

- Added guided recovery demos for Outbox retry and idempotent notification consumption.
- Added a manual notify-service-down scenario script for controlled environment validation.
- Added dashboard message-health retrying count support for failed outbox messages.
- Documented the recovery scenarios and the dashboard endpoints that expose evidence.

## Known Limitations

- Live RabbitMQ and notification-chain recovery was not executed because the local Gateway and full service stack were not running.
- The demo scripts are acceptance helpers; they require a running SkyGrid stack with Gateway access.
