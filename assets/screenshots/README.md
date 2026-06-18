# SkyGrid Screenshot Inventory

This directory records v1.0.0 screenshot capture targets. Screenshots should only be added after the local stack is running; this repository does not include synthetic screenshots pretending to be runtime evidence.

| 文件 | 建议截图内容 |
| --- | --- |
| `cockpit-dashboard.png` | Cockpit overview with real dashboard API data |
| `airspace-grid.png` | Grid / Level / TimeSlot occupancy view |
| `booking-approval.png` | Booking approval workbench |
| `conflict-resolution.png` | Conflict records and resolution suggestions |
| `outbox-pending.png` | `outbox_message` 中待投递的 `PENDING` 消息 |
| `outbox-retry-success.png` | 失败重试或手动 requeue 后变为 `SENT` 的消息 |
| `rabbitmq-queue.png` | `low-altitude.booking.notify.queue` 队列积压和恢复 |
| `audit-log.png` | `NOTIFY_SENT` 和 `DUPLICATE_SKIPPED` 审计记录 |
