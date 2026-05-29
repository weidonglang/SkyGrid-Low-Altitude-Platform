# Phase 06 开发总结：RabbitMQ、本地消息表与最终一致性

本阶段把预约审批链路扩展为可靠消息链路。审批通过后，`booking-service` 不直接调用通知服务，而是在同一个本地事务中写入 `outbox_message`。随后由定时任务或手动派发接口把消息投递到 RabbitMQ，`conflict-notify-service` 消费消息并写入通知记录、审计日志和幂等记录。

## 新增数据表

- `low_altitude_booking.outbox_message`：本地消息表，记录待投递、已投递、失败重试状态。
- `low_altitude_conflict_notify.notify_record`：通知记录表。
- `low_altitude_conflict_notify.audit_log`：审计日志表。
- `low_altitude_conflict_notify.idempotent_record`：幂等消费表。

## 新增接口

### booking-service

- `GET /api/bookings/outbox`
- `GET /api/bookings/outbox/{id}`
- `POST /api/bookings/outbox/dispatch`
- `POST /api/bookings/outbox/{id}/requeue`
- `POST /api/bookings/outbox/{id}/republish`

### conflict-notify-service

- `GET /api/notifications/records`
- `GET /api/notifications/audits`
- `GET /api/notifications/idempotent`

## 核心演示点

1. 预约审批通过后，业务状态和 outbox 消息在同一个事务中落库。
2. `outbox_message` 被派发到 RabbitMQ 后状态变为 `SENT`。
3. `conflict-notify-service` 消费消息后写入 `notify_record` 和 `audit_log`。
4. 重复投递同一条 outbox 消息时，`idempotent_record` 阻止重复通知，只记录 `DUPLICATE_SKIPPED` 审计。
5. 已审批申请取消后，会生成 `BOOKING_CANCELLED` 事件并完成同样的通知链路。

## 验证脚本

```bat
scripts\phase06-smoke-test.bat
```

成功标志：

```text
[Phase06] OK
[Phase06] Completed successfully.
```
