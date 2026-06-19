# SkyGrid v0.2.0 Integration Demo Scenario

## Purpose

This scenario verifies the first real integration step between LowAlt-RouteLab and SkyGrid.

LowAlt-RouteLab generates the route, evaluates risk and energy, converts the route into TimeSlot occupancy, and calls SkyGrid Gateway. SkyGrid performs conflict pre-check, creates a booking, approves it, writes resource occupancy, generates an outbox message, dispatches notification, and exposes notify/audit records.

## Demo Task

- Task: East District UAV Inspection
- Start grid: `G-02-03`
- End grid: `G-16-14`
- Level: `L120`
- Time window: `10:00 - 10:30`
- Algorithm: `A_STAR`
- Risk area: around `G-08-09`
- No-fly area: `G-10-10` to `G-12-12`

## SkyGrid Startup

```bat
cd /d E:\javacode\low-altitude-platform
scripts\start-dev-stack.bat
scripts\check-dev-stack.bat
```

## Demo Data

```bat
scripts\prepare-demo-data.bat
```

The script reuses the existing idempotent MySQL initialization SQL in `docker\mysql\init.sql`. It does not delete runtime data.

## SkyGrid Acceptance

```bat
scripts\phase08-acceptance-check.bat
scripts\demo-e2e-check.bat
```

The acceptance script verifies:

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

## LowAlt Real Demo

Run from the LowAlt-RouteLab repository:

```bat
scripts\run-real-skygrid-demo.bat
```

The LowAlt route-adapter-service must be running with `skygrid.mode=real` and a valid dev token.

## Success Criteria

```text
Route planned: OK
Risk evaluated: OK
TimeSlot converted: OK
SkyGrid conflict checked: OK
Booking submitted: OK
Booking approved: OK
Occupancy created: OK
Outbox generated: OK
Notification recorded: OK
Audit logged: OK
Demo completed successfully.
```

## Known v0.2.0 Boundary

SkyGrid currently accepts booking requests through its existing route-template based booking API. LowAlt preserves richer route metadata in the request description and uses SkyGrid's configured demo `routeTemplateId`, `levelId`, and `timeSlotIds` for the actual booking contract.
