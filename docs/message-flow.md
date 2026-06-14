# Outbox 消息流

本文档用更短的链路说明 SkyGrid 从审批通过到通知审计的消息流。

## 消息流总览

```text
用户审批通过预约
  ↓
booking-service
  ↓
booking_record.status = APPROVED
  ↓
resource_occupancy 写入多条占用
  ↓
outbox_message 写入 BOOKING_APPROVED / PENDING
  ↓
OutboxService.dispatchReadyMessages
  ↓
RabbitMQ exchange: low-altitude.booking.exchange
  ↓
routing key: booking.approved
  ↓
queue: low-altitude.booking.notify.queue
  ↓
conflict-notify-service
  ↓
idempotent_record INSERT IGNORE
  ↓
notify_record.status = SENT
  ↓
audit_log.action = NOTIFY_SENT
```

## 事件类型

| 事件 | routing key | 消费结果 |
| --- | --- | --- |
| `BOOKING_APPROVED` | `booking.approved` | 生成审批通过通知和审计 |
| `BOOKING_CANCELLED` | `booking.cancelled` | 生成取消释放通知和审计 |

## RabbitMQ 命名

| 类型 | 名称 |
| --- | --- |
| Exchange | `low-altitude.booking.exchange` |
| Queue | `low-altitude.booking.notify.queue` |
| Approved Routing Key | `booking.approved` |
| Cancelled Routing Key | `booking.cancelled` |

## 消息状态流转

```text
PENDING
  ├─ publish success → SENT
  └─ publish failed  → FAILED

FAILED
  ├─ next_retry_at reached → dispatch again
  ├─ manual requeue        → PENDING
  └─ retry_count exhausted → keep FAILED for manual handling
```

## 消费幂等流转

```text
message arrives
  ↓
INSERT IGNORE idempotent_record(message_key, event_type, consumer_name)
  ├─ inserted = 1 → write notify_record + audit_log(NOTIFY_SENT)
  └─ inserted = 0 → skip notify + audit_log(DUPLICATE_SKIPPED)
```

## 观察点

演示或验收时至少观察以下结果：

| 观察点 | 期望 |
| --- | --- |
| `resource_occupancy` | 审批通过后有对应占用记录 |
| `outbox_message` | 生成 `BOOKING_APPROVED` 消息 |
| `outbox_message.status` | 投递前为 `PENDING`，成功后为 `SENT` |
| RabbitMQ queue | 通知服务暂停时可看到消息积压 |
| `notify_record` | 通知服务恢复后有 `SENT` 记录 |
| `audit_log` | 有 `NOTIFY_SENT`，重复发布时有 `DUPLICATE_SKIPPED` |
| `idempotent_record` | 同一 `message_key` 只处理一次 |

