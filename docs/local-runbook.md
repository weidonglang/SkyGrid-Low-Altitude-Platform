# SkyGrid Local Runbook

This runbook describes the local development stack used for the v0.2.0 integration demo with LowAlt-RouteLab.

## Environment

- Windows 10/11
- JDK 17
- Maven 3.8+
- Node.js 18+
- Docker Desktop
- PowerShell 5+

The scripts assume the repository is located at `E:\javacode\low-altitude-platform`.

## Ports

| Component | Port | Check |
|---|---:|---|
| Gateway | 8080 | `http://127.0.0.1:8080/actuator/health` |
| user-org-service | 8101 | `http://127.0.0.1:8101/actuator/health` |
| resource-service | 8102 | `http://127.0.0.1:8102/actuator/health` |
| booking-service | 8103 | `http://127.0.0.1:8103/actuator/health` |
| conflict-notify-service | 8104 | `http://127.0.0.1:8104/actuator/health` |
| low-altitude-web | 5173 | `http://127.0.0.1:5173/` |
| Nacos | 8848 | `http://127.0.0.1:8848/nacos/` |
| MySQL | 3306 | TCP |
| Redis | 6379 | TCP |
| RabbitMQ AMQP | 5672 | TCP |
| RabbitMQ Management | 15672 | `http://127.0.0.1:15672/` |

## One-command Startup

```bat
scripts\start-dev-stack.bat
```

The script starts Docker infrastructure first and then launches the Spring services and frontend in hidden windows. Service logs are written to:

```text
logs\dev-stack
```

After startup, wait 60-90 seconds and run:

```bat
scripts\check-dev-stack.bat
```

## Manual Startup

Start middleware:

```bat
scripts\start-infra.bat
```

Start services from separate terminals:

```bat
mvn -pl user-org-service spring-boot:run
mvn -pl resource-service spring-boot:run
mvn -pl booking-service spring-boot:run
mvn -pl conflict-notify-service spring-boot:run
mvn -pl low-altitude-gateway spring-boot:run
```

Start frontend:

```bat
cd low-altitude-web
npm run dev -- --host 127.0.0.1
```

## Readiness Check

```bat
scripts\check-dev-stack.bat
```

The check covers Gateway, Nacos, RabbitMQ, Redis, MySQL, user-org-service, resource-service, booking-service, conflict-notify-service, and the frontend.

## Phase08 Acceptance

```bat
scripts\phase08-acceptance-check.bat
```

The script verifies:

1. Gateway health.
2. Dev token acquisition.
3. Grid / Level / TimeSlot query.
4. Conflict pre-check.
5. Booking submit.
6. Booking approval.
7. Resource occupancy query.
8. Outbox message query and dispatch.
9. Notification record query.
10. Audit log query.

If a service is unavailable, the script prints the failed component and the URL that could not be reached.

## Stop

```bat
scripts\stop-dev-stack.bat
```

The script stops processes listening on the SkyGrid development ports and then shuts down the Docker infrastructure.

## Common Failures

| Symptom | Likely Cause | Action |
|---|---|---|
| `start-dev-stack.bat` reports Docker daemon unavailable | Docker Desktop is not running or the Linux engine pipe is unavailable | Start Docker Desktop, wait until it reports ready, then rerun `scripts\start-dev-stack.bat` |
| Gateway health is unavailable | Gateway process not started or Nacos unavailable | Run `scripts\check-dev-stack.bat`; inspect `logs\dev-stack\low-altitude-gateway.err.log` |
| Gateway returns 503 | A downstream service is not registered in Nacos | Check user-org/resource/booking/notify service health endpoints |
| Booking submit fails with seed data errors | MySQL schema or demo data is missing | Re-run Docker initialization or prepare demo data before acceptance |
| Outbox exists but notification is empty | RabbitMQ or conflict-notify-service is unavailable | Check RabbitMQ management console and notify service health |
| Frontend unavailable | `npm run dev` did not start or node modules are missing | Run `npm install` inside `low-altitude-web`, then restart frontend |
