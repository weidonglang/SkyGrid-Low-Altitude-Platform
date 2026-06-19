# SkyGrid Troubleshooting

## Gateway Is Unavailable

Check:

```bat
curl http://127.0.0.1:8080/actuator/health
```

If the request fails, start the local stack:

```bat
scripts\start-dev-stack.bat
scripts\check-dev-stack.bat
```

Then inspect:

```text
logs\dev-stack\low-altitude-gateway.out.log
logs\dev-stack\low-altitude-gateway.err.log
```

## Docker Daemon Is Unavailable

If startup stops with:

```text
[SkyGrid][FAIL] Docker daemon is not available.
```

Docker CLI is installed but Docker Desktop is not ready. Start Docker Desktop, wait until the Linux engine is running, then retry:

```bat
scripts\start-dev-stack.bat
```

The startup script stops at this point so the Java services are not launched against missing Redis, RabbitMQ, or Nacos infrastructure.

## Gateway Returns 503

This usually means the Gateway is running but at least one downstream service is not registered in Nacos.

Check these endpoints:

```text
http://127.0.0.1:8101/actuator/health
http://127.0.0.1:8102/actuator/health
http://127.0.0.1:8103/actuator/health
http://127.0.0.1:8104/actuator/health
```

Also open Nacos:

```text
http://127.0.0.1:8848/nacos/
```

Confirm that `user-org-service`, `resource-service`, `booking-service`, `conflict-notify-service`, and `low-altitude-gateway` are registered.

## MySQL Connection Fails

Default local settings:

```text
host: 127.0.0.1
port: 3306
root password: 123123
application user: lowaltitude / lowaltitude123
```

Check the port:

```bat
netstat -ano | findstr :3306
```

If Docker MySQL is not running:

```bat
docker compose -f docker-compose.infra.yml up -d mysql
```

## Redis or RabbitMQ Is Unavailable

Check ports:

```bat
netstat -ano | findstr :6379
netstat -ano | findstr :5672
```

Open RabbitMQ management:

```text
http://127.0.0.1:15672/
```

Default demo account:

```text
lowaltitude / lowaltitude123
```

## Dev Token API Fails

Check that user-org-service is running and Gateway can route to it:

```bat
curl -X POST "http://127.0.0.1:8080/api/auth/dev-token?username=demo&role=ADMIN"
```

If Gateway returns 503, fix service registration first.

## Phase08 Booking Submit Fails

The acceptance script requires seeded resource data:

- At least one organization.
- Grid records.
- Altitude level records.
- TimeSlot records.
- Route template `1`.

Run the local database initialization described in `docs\database-initialization.md`, then retry:

```bat
scripts\phase08-acceptance-check.bat
```

## Outbox Exists but Notify Record Is Missing

Check:

```text
http://127.0.0.1:8104/actuator/health
http://127.0.0.1:15672/
```

Then dispatch messages manually:

```bat
curl -X POST "http://127.0.0.1:8080/api/bookings/outbox/dispatch?limit=20" -H "Authorization: Bearer <token>"
```

If RabbitMQ or conflict-notify-service is down, outbox messages remain pending or retrying until the service recovers.

## Frontend Cannot Reach APIs

Check Gateway first:

```bat
curl http://127.0.0.1:8080/actuator/health
```

Then confirm the frontend proxy target in `low-altitude-web/vite.config.js` points to:

```text
http://127.0.0.1:8080
```

## PowerShell Encoding

Use Windows Terminal or run:

```bat
chcp 65001
```

The v0.2.0 scripts use ASCII output to avoid console encoding issues.
