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
| `mvn test` | 通过 | 临时设置 `JAVA_HOME=C:\Program Files\Java\jdk-17.0.18+8` 后执行，全模块 `BUILD SUCCESS` |
| `cd low-altitude-web; npm run build` | 通过 | Vite build 成功，输出 `dist/index.html`、CSS 和 JS；存在大 chunk 警告 |
| `scripts\phase08-acceptance-check.bat` | 未通过 | Gateway 未启动，脚本在 health check 阶段无法连接 `http://127.0.0.1:8080` |

## 一致性链路验收点

| 验收点 | 状态 | 说明 |
| --- | --- | --- |
| 审批通过后写入 `resource_occupancy` | 未执行 | 需要启动 Gateway、MySQL 和后端服务 |
| 审批通过后写入 `outbox_message` | 未执行 | 需要启动 booking-service |
| Outbox 投递到 RabbitMQ | 未执行 | 需要启动 RabbitMQ |
| 通知服务写入 `notify_record` | 未执行 | 需要启动 conflict-notify-service |
| 通知服务写入 `audit_log` | 未执行 | 需要启动 conflict-notify-service |
| 重复投递触发 `DUPLICATE_SKIPPED` | 未执行 | 通过 `/republish` 演示，需完整运行环境 |

## 已知限制

- `mvn test` 初次执行失败是因为系统 `JAVA_HOME` 指向 `D:\DevEnvManager\current\jdk`，该路径不可用；临时设置为本机 JDK 17 后通过。
- `npm run build` 初次执行失败是因为 Vite/Rollup 在 Windows 路径下把 `index.html` 作为绝对 fileName；已通过 `low-altitude-web/vite.config.js` 的 `build.rollupOptions.input` 修复。
- `phase08-acceptance-check.bat` 需要完整运行中的 Gateway 和后端服务，本次未启动服务，因此验收脚本未通过。
- 当前截图目录只包含采集清单，真实截图需在完整本地环境启动后补充。
