# SkyGrid 截图清单

本目录用于保存 v0.1.0 封版演示截图。当前先保留采集清单，待本地服务、RabbitMQ 和数据库完整启动后补真实图片。

| 文件 | 建议截图内容 |
| --- | --- |
| `outbox-pending.png` | `outbox_message` 中待投递的 `PENDING` 消息 |
| `outbox-retry-success.png` | 失败重试或手动 requeue 后变为 `SENT` 的消息 |
| `rabbitmq-queue.png` | `low-altitude.booking.notify.queue` 队列积压和恢复 |
| `audit-log.png` | `NOTIFY_SENT` 和 `DUPLICATE_SKIPPED` 审计记录 |

