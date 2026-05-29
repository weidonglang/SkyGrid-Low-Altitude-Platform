# 截图清单

本文档用于整理最终提交材料所需截图。这里不提供制图文件，仅列出建议截图内容。

## 1. 运行环境截图

- [ ] Docker Desktop 容器列表：Nacos、RabbitMQ、Redis、Prometheus、Grafana。
- [ ] Nacos 服务列表：5 个服务均在线。
- [ ] RabbitMQ 管理台：exchange / queue / connection / consumer。
- [ ] Gateway `/actuator/health` 返回 UP。

## 2. 后端接口截图

- [ ] 获取 ADMIN token 成功。
- [ ] `/api/resources/grids` 返回 Grid 数据。
- [ ] `/api/resources/route-templates/1` 返回航线模板详情。
- [ ] `/api/bookings/pre-check` 返回风险或冲突结果。
- [ ] `/api/bookings/governance/summary` 返回治理汇总。
- [ ] `/api/notifications/summary` 返回通知审计统计。

## 3. 数据库截图

- [ ] `low_altitude_user_org` 表列表。
- [ ] `low_altitude_resource.grid` 默认数据。
- [ ] `low_altitude_booking.booking_record` 预约记录。
- [ ] `low_altitude_booking.resource_occupancy` 占用记录。
- [ ] `low_altitude_booking.conflict_record` 冲突记录。
- [ ] `low_altitude_booking.outbox_message` 消息记录。
- [ ] `low_altitude_conflict_notify.notify_record` 通知记录。
- [ ] `low_altitude_conflict_notify.audit_log` 审计日志。
- [ ] `low_altitude_conflict_notify.idempotent_record` 幂等记录。

## 4. 前端截图

- [ ] 运行状态页面。
- [ ] 资源网格页面。
- [ ] 预约申请 / Pre-check 页面。
- [ ] Pre-check 风险提示结果。
- [ ] HARD 冲突阻断结果。
- [ ] NO_FLY 禁飞区阻断结果。
- [ ] 审批工作台。
- [ ] 占用记录展示。
- [ ] 冲突记录页面。

## 5. 消息一致性截图

- [ ] Phase 06 脚本通过截图。
- [ ] outbox_message 从 PENDING 到 SENT。
- [ ] notify_record 生成通知。
- [ ] audit_log 生成 NOTIFY_SENT。
- [ ] 重复投递后出现 DUPLICATE_SKIPPED。

## 6. 治理监控截图

- [ ] Phase 07 脚本通过截图。
- [ ] RateLimiter 返回 RATE_LIMITED。
- [ ] CircuitBreaker 返回 DEGRADED。
- [ ] Retry 返回 RETRY_SUCCESS。
- [ ] Prometheus Targets 页面。
- [ ] Grafana Overview 面板。
- [ ] JMeter 压测结果。

## 7. 推荐命名

建议截图按顺序命名：

```text
01-nacos-services.png
02-gateway-health.png
03-grid-page.png
04-precheck-risk.png
05-hard-conflict.png
06-no-fly-block.png
07-booking-approve.png
08-resource-occupancy.png
09-outbox-message.png
10-notify-audit-idempotent.png
11-rate-limiter.png
12-grafana-overview.png
```
