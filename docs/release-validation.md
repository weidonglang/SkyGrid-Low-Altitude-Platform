# SkyGrid v0.1.0 验证记录

验证日期：2026-06-14

本文档记录 v0.1.0 封版前的测试、构建和验收命令执行结果。未执行或失败的项目必须说明原因，不伪造通过结果。

## 环境

| 项目 | 要求 |
| --- | --- |
| JDK | 17+ |
| Maven | 3.9+ |
| Node.js | 18+ |
| npm | 9+ |
| Docker Desktop | 用于 MySQL、Nacos、Redis、RabbitMQ、Prometheus、Grafana |

## 验证命令

| 命令 | 状态 | 结果 |
| --- | --- | --- |
| `mvn test` | 待执行 | 待更新 |
| `cd low-altitude-web; npm run build` | 待执行 | 待更新 |
| `scripts\phase08-acceptance-check.bat` | 待执行 | 待更新 |

## 一致性链路验收点

| 验收点 | 状态 | 说明 |
| --- | --- | --- |
| 审批通过后写入 `resource_occupancy` | 待验证 | 需要本地 MySQL 和后端服务 |
| 审批通过后写入 `outbox_message` | 待验证 | 需要 booking-service |
| Outbox 投递到 RabbitMQ | 待验证 | 需要 RabbitMQ |
| 通知服务写入 `notify_record` | 待验证 | 需要 conflict-notify-service |
| 通知服务写入 `audit_log` | 待验证 | 需要 conflict-notify-service |
| 重复投递触发 `DUPLICATE_SKIPPED` | 待验证 | 通过 `/republish` 演示 |

## 已知限制

- 本文档会在实际执行测试命令后更新。
- 当前截图目录只包含采集清单，真实截图需在完整本地环境启动后补充。

