# 消息最终一致性设计

本文档说明 SkyGrid 在预约审批、资源占用、通知审计之间如何通过 Outbox 本地消息表和 RabbitMQ 实现最终一致性。

## 设计目标

审批通过后，系统必须同时完成两类结果：

- 业务结果：预约状态变为已审批，写入 `resource_occupancy`。
- 通知结果：通知申请人和审批相关方，并写入 `notify_record`、`audit_log`。

如果直接在审批事务中同步调用通知服务，会让审批链路依赖远程服务可用性。SkyGrid 采用 Outbox 模式，把业务数据和待投递消息写在同一个本地事务里，再由后台任务异步投递到 RabbitMQ。

## 核心链路

```text
booking-service
  审批通过
    ↓
  更新 booking_record
    ↓
  写 resource_occupancy
    ↓
  写 outbox_message(PENDING)
    ↓
  OutboxService scheduledDispatch / manual dispatch
    ↓
  RabbitMQ exchange: low-altitude.booking.exchange
    ↓
  queue: low-altitude.booking.notify.queue
    ↓
conflict-notify-service
  BookingEventListener
    ↓
  NotificationService.consumeBookingEvent
    ↓
  INSERT IGNORE idempotent_record
    ↓
  写 notify_record
    ↓
  写 audit_log
```

## 关键表

| 表 | 所属服务 | 作用 |
| --- | --- | --- |
| `booking_record` | `booking-service` | 预约主记录，保存申请和审批状态 |
| `resource_occupancy` | `booking-service` | 审批通过后的低空资源占用记录 |
| `outbox_message` | `booking-service` | 本地消息表，保存待投递事件 |
| `idempotent_record` | `conflict-notify-service` | 消费幂等记录，防止重复消息重复落库 |
| `notify_record` | `conflict-notify-service` | 通知结果记录 |
| `audit_log` | `conflict-notify-service` | 通知和重复消费审计 |

## Outbox 消息

`OutboxService` 会在审批通过或取消释放时创建事件：

| 事件 | routing key | 触发动作 |
| --- | --- | --- |
| `BOOKING_APPROVED` | `booking.approved` | 预约审批通过 |
| `BOOKING_CANCELLED` | `booking.cancelled` | 预约取消或资源释放 |

消息写入时的关键字段：

| 字段 | 含义 |
| --- | --- |
| `message_key` | 业务幂等键，格式为 `eventType:bookingNo` |
| `event_type` | 事件类型 |
| `aggregate_type` | 聚合类型，当前为 `BOOKING` |
| `aggregate_id` | 预约记录 ID |
| `routing_key` | RabbitMQ 路由键 |
| `payload` | JSON 事件体 |
| `status` | `PENDING`、`SENT` 或 `FAILED` |
| `retry_count` | 已重试次数 |
| `max_retry_count` | 最大重试次数，当前默认 8 |
| `next_retry_at` | 下次可投递时间 |

## 投递策略

`OutboxService` 通过两个入口投递消息：

- 定时任务：`scheduledDispatch()`，默认按 `low-altitude.outbox.fixed-delay-ms` 周期执行。
- 手动接口：`POST /api/bookings/outbox/dispatch?limit=20`。

投递规则：

1. 查询 `status IN ('PENDING', 'FAILED')` 的消息。
2. 只选择 `retry_count < max_retry_count` 且 `next_retry_at <= CURRENT_TIMESTAMP` 的消息。
3. 发送到 `low-altitude.booking.exchange`。
4. 成功后标记为 `SENT` 并写入 `sent_at`。
5. 失败后标记为 `FAILED`，`retry_count + 1`，并设置下一次 `next_retry_at`。

当前重试延迟计算：

```text
nextRetryDelaySeconds = min(300, 5 * (retryCount + 1))
```

## 消费幂等

`conflict-notify-service` 在消费 RabbitMQ 消息时不直接写通知记录，而是先写入 `idempotent_record`：

```sql
INSERT IGNORE INTO idempotent_record(message_key, event_type, consumer_name, status, processed_at)
VALUES(..., 'conflict-notify-service', 'PROCESSED', CURRENT_TIMESTAMP)
```

如果插入成功，说明这是第一次消费，继续写 `notify_record` 和 `audit_log`。

如果插入失败，说明消息已经被处理过，服务不会重复写通知记录，只写一条 `DUPLICATE_SKIPPED` 审计。

## 一致性保证

| 风险点 | 处理方式 |
| --- | --- |
| 审批事务成功但通知服务不可用 | `outbox_message` 已落库，后续定时任务继续投递 |
| RabbitMQ 暂时不可用 | 消息进入 `FAILED`，按 `next_retry_at` 重试 |
| RabbitMQ 重复投递 | `idempotent_record` 保证消费幂等 |
| 手动重复 republish | 重复消息只产生 `DUPLICATE_SKIPPED` 审计 |
| 消息达到最大重试次数 | 保留 `FAILED` 和 `last_error`，支持人工排查后 requeue |

## 演示接口

| 接口 | 作用 |
| --- | --- |
| `GET /api/bookings/outbox` | 查询 Outbox 消息 |
| `GET /api/bookings/outbox/summary` | 查询 Outbox 状态汇总 |
| `POST /api/bookings/outbox/dispatch` | 手动触发一批消息投递 |
| `POST /api/bookings/outbox/{id}/requeue` | 将失败消息重新置为待投递 |
| `POST /api/bookings/outbox/{id}/republish` | 为幂等演示重复发布一条消息 |

通知侧可通过通知服务的通知、审计和幂等查询接口确认最终结果。

