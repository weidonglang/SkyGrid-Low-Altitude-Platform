# SkyGrid v1.0.0 Release Checklist

## Code

- [x] Booking, approval, conflict, occupancy, Outbox, notification, audit, dashboard, and governance code is present.
- [x] Docker Compose files exist for local infrastructure and monitoring.
- [x] Startup, health-check, acceptance, recovery, and smoke scripts are documented.
- [x] Public configuration examples avoid real production secrets.

## Validation

- [x] `mvn test`
- [x] `cd low-altitude-web && npm run build`
- [ ] `scripts\check-dev-stack.bat` - blocked until local Redis/RabbitMQ/Nacos/Gateway/services are running.
- [ ] `scripts\phase08-acceptance-check.bat` - blocked until Gateway is running.
- [ ] `scripts\demo-e2e-check.bat` - blocked until the local stack is ready.
- [ ] `scripts\demo-outbox-recovery.bat` - blocked until Gateway and notification chain are running.
- [ ] `performance\skygrid-smoke.bat` - blocked until Gateway is running.

## Documentation

- [x] Architecture and project-boundary docs exist.
- [x] Local runbook, Docker deployment, troubleshooting, consistency, message-flow, and recovery docs exist.
- [x] Resilience, observability, performance, demo scenario, release checklist, and validation docs are included.
- [x] Screenshot and diagram capture inventory is explicit.

## Release Rule

Do not tag `v1.0.0` until validation has been run in the target local environment and the result is recorded in `docs/release-validation-v1.0.0.md`.
