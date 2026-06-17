# SkyGrid Failure Recovery Real Demo

## Scope

This v0.5.0 demo uses the existing Outbox table, RabbitMQ publisher, notification consumer, idempotent consume record, notify record, and audit log.

## Scenario 1: Notify Service Unavailable

1. Start SkyGrid infrastructure and core services.
2. Stop `conflict-notify-service`.
3. Submit and approve a booking.
4. Confirm resource occupancy is created.
5. Confirm `outbox_message` contains a pending or failed message.
6. Restart `conflict-notify-service`.
7. Run:

```bat
scripts\demo-outbox-recovery.bat
```

8. Confirm notification and audit records are visible:

```text
GET /api/notifications/records
GET /api/notifications/audits
```

## Scenario 2: Duplicate Consume

After at least one outbox message exists:

```bat
scripts\demo-idempotent-consume.bat
```

The script republishes the same outbox message twice and then queries:

```text
GET /api/notifications/idempotent
```

The consumer should identify the existing idempotent key and avoid duplicate notify records.

## Scenario 3: RabbitMQ Temporary Failure

1. Stop RabbitMQ.
2. Approve a booking.
3. Confirm outbox dispatch moves the message to failed/retry state.
4. Restart RabbitMQ.
5. Run `scripts\demo-outbox-recovery.bat`.
6. Confirm the message is sent and notification records are created.

## Dashboard Evidence

The cockpit dashboard can read message consistency status through:

```text
GET /api/dashboard/message-health
GET /api/dashboard/audit-summary
GET /api/dashboard/overview
```
