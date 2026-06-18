# SkyGrid v1.0.0 Demo Scenario

## Story

An operator plans a low-altitude inspection route in LowAlt-RouteLab and submits the resulting TimeSlot occupancy to SkyGrid. SkyGrid checks the airspace, creates a booking, approves it, writes occupancy records, publishes an Outbox message, and records notification/audit evidence.

## Main Flow

```text
LowAlt route plan
  -> TimeSlot occupancy
  -> SkyGrid conflict check
  -> Booking submit
  -> Approval
  -> resource_occupancy
  -> outbox_message
  -> RabbitMQ
  -> notify_record / audit_log
  -> cockpit dashboard
```

## Demo Commands

```bat
scripts\docker-start-dev.bat
scripts\start-dev-stack.bat
scripts\check-dev-stack.bat
scripts\prepare-demo-data.bat
scripts\phase08-acceptance-check.bat
scripts\demo-e2e-check.bat
scripts\demo-outbox-recovery.bat
```

## Dashboard Evidence

```text
GET /api/dashboard/overview
GET /api/dashboard/occupancy
GET /api/dashboard/conflicts
GET /api/dashboard/message-health
GET /api/dashboard/audit-summary
GET /api/dashboard/service-health
```

## Failure Recovery Demo

Use:

```bat
scripts\demo-notify-service-down.bat
scripts\demo-idempotent-consume.bat
scripts\demo-outbox-recovery.bat
```

## Known Limitations

- The Java services are still started locally because this repository does not include service Dockerfiles.
- Screenshots are listed as capture targets; no synthetic screenshots are committed as real evidence.
