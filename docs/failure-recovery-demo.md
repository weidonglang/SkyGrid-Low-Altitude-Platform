# 故障恢复演示

本文档用于演示 SkyGrid 的最终一致性、失败重试、补偿恢复和幂等消费能力。

## 演示目标

证明审批主链路不依赖通知服务实时可用：

```text
审批通过成功
  ↓
resource_occupancy 已写入
  ↓
outbox_message 保留待投递消息
  ↓
通知服务或 RabbitMQ 异常后可恢复
  ↓
notify_record / audit_log 最终写入
```

## 前置条件

需要启动：

- MySQL
- Redis
- RabbitMQ
- Nacos
- `user-org-service`
- `resource-service`
- `booking-service`
- `low-altitude-gateway`

为了演示消息积压，可以先不启动或临时停止：

- `conflict-notify-service`

## 步骤一：提交并审批预约

1. 打开前端控制台。
2. 创建或选择一条不会产生硬冲突的预约申请。
3. 在审批工作台审批通过。
4. 查询资源占用，确认 `resource_occupancy` 已写入。

也可以通过现有冒烟脚本创建演示数据：

```bat
scripts\phase06-smoke-test.bat
```

## 步骤二：确认 Outbox 消息生成

通过 Gateway 查询：

```bat
curl http://127.0.0.1:8080/api/bookings/outbox/summary
curl "http://127.0.0.1:8080/api/bookings/outbox?eventType=BOOKING_APPROVED&limit=10"
```

期望看到：

- `eventType = BOOKING_APPROVED`
- `routingKey = booking.approved`
- `status = PENDING` 或 `SENT`
- `messageKey = BOOKING_APPROVED:{bookingNo}`

如果定时任务已经投递成功，状态可能已经变为 `SENT`。为了稳定演示待投递状态，可以在 `booking-service` 配置中临时设置：

```yaml
low-altitude:
  outbox:
    publisher-enabled: false
```

然后审批一条新预约，确认消息停留在 `PENDING`。

## 步骤三：演示通知服务不可用

停止 `conflict-notify-service`，保持 RabbitMQ 和 `booking-service` 运行。

然后手动触发投递：

```bat
curl -X POST "http://127.0.0.1:8080/api/bookings/outbox/dispatch?limit=20"
```

期望现象：

- Outbox 消息可以被投递到 RabbitMQ。
- RabbitMQ 的 `low-altitude.booking.notify.queue` 出现 ready 消息。
- 因为通知服务未运行，`notify_record` 和 `audit_log` 暂时不会新增。

RabbitMQ 控制台：

```text
http://127.0.0.1:15672/
```

## 步骤四：恢复通知服务

启动 `conflict-notify-service`。

观察：

- RabbitMQ 队列积压减少。
- `notify_record` 新增 `SENT` 记录。
- `audit_log` 新增 `NOTIFY_SENT` 记录。
- `idempotent_record` 新增 `PROCESSED` 记录。

通知侧查询可使用通知服务提供的通知、审计和幂等查询接口，或直接查询数据库。

## 步骤五：演示投递失败与 requeue

如果需要演示 `FAILED` 状态，可以临时停止 RabbitMQ 或修改 RabbitMQ 连接配置，再手动触发 dispatch。

期望 Outbox 变化：

| 字段 | 期望 |
| --- | --- |
| `status` | `FAILED` |
| `retry_count` | 增加 1 |
| `next_retry_at` | 被推迟 |
| `last_error` | 记录失败原因 |

恢复 RabbitMQ 后，可以等待定时任务自动重试，也可以人工 requeue：

```bat
curl -X POST http://127.0.0.1:8080/api/bookings/outbox/{id}/requeue
curl -X POST "http://127.0.0.1:8080/api/bookings/outbox/dispatch?limit=20"
```

最终期望：

- `outbox_message.status = SENT`
- `notify_record.status = SENT`
- `audit_log.action = NOTIFY_SENT`

## 步骤六：演示重复投递幂等

对已经成功发送的 Outbox 消息执行重复发布：

```bat
curl -X POST http://127.0.0.1:8080/api/bookings/outbox/{id}/republish
```

期望：

- 不重复写入业务通知。
- `idempotent_record` 中同一 `message_key` 仍只有一条 `PROCESSED`。
- `audit_log` 新增 `DUPLICATE_SKIPPED`。

## 截图清单

建议为答辩或 README 补充以下截图：

| 文件 | 内容 |
| --- | --- |
| `assets/screenshots/outbox-pending.png` | Outbox 中 `PENDING` 消息 |
| `assets/screenshots/outbox-retry-success.png` | 失败重试后消息变为 `SENT` |
| `assets/screenshots/rabbitmq-queue.png` | RabbitMQ 队列积压与恢复 |
| `assets/screenshots/audit-log.png` | `NOTIFY_SENT` 与 `DUPLICATE_SKIPPED` 审计 |

如果当前运行环境无法完整启动基础设施，可以先保留截图清单，等本地 RabbitMQ/MySQL 环境稳定后补真实图片。

