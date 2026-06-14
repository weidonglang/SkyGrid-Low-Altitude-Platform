# SkyGrid v0.1.0 发布检查清单

版本标题：

```text
SkyGrid v0.1.0 - Low-altitude Resource Scheduling and Conflict Governance Platform
```

## 发布定位

v0.1.0 是 SkyGrid 的低空资源调度与冲突治理封版版本，重点证明平台具备以下工程能力：

- 低空时空切片模型：`Grid + Level + TimeSlot + Date`。
- 预约申请、审批通过、驳回、取消释放。
- 审批通过后生成 `resource_occupancy`。
- 硬冲突、禁飞区、风险网格和相邻风险检测。
- Outbox 本地消息表、RabbitMQ 投递、幂等消费和补偿恢复。
- 通知记录、审计日志和重复消费审计。
- Gateway、Nacos、Redis、RabbitMQ、Prometheus、Grafana 等服务治理能力。
- 与 LowAlt-RouteLab 组成低空项目群闭环。

## 必备文档

| 文档 | 状态 |
| --- | --- |
| `README.md` | 已补项目群关系 |
| `docs/project-boundary.md` | 已补 |
| `docs/demo-script.md` | 已存在 |
| `docs/consistency-design.md` | 已补 |
| `docs/failure-recovery-demo.md` | 已补 |
| `docs/message-flow.md` | 已补 |
| `docs/release-validation.md` | 待执行验证后更新 |

## 必备截图

| 截图 | 状态 |
| --- | --- |
| `assets/diagrams/outbox-message-flow.png` | 待补真实图 |
| `assets/screenshots/outbox-pending.png` | 待补真实截图 |
| `assets/screenshots/outbox-retry-success.png` | 待补真实截图 |
| `assets/screenshots/rabbitmq-queue.png` | 待补真实截图 |
| `assets/screenshots/audit-log.png` | 待补真实截图 |

如果本地基础设施暂时无法完整启动，发布说明中应明确截图暂未内置，并保留截图采集清单。

## 发布前验证命令

```powershell
mvn test
cd low-altitude-web
npm run build
cd ..
scripts\phase08-acceptance-check.bat
```

## GitHub Release 内容建议

```markdown
## Highlights

- Low-altitude airspace model based on Grid + Level + TimeSlot + Date.
- Booking approval workflow with resource occupancy generation.
- Conflict detection for same grid/level/time, no-fly zones and risk grids.
- Outbox + RabbitMQ eventual consistency with idempotent notification consumption.
- Audit log, notify record and failure recovery demo documents.
- Project-group narrative with LowAlt-RouteLab route planning simulator.

## Validation

See docs/release-validation.md.

## Known Limits

- Demo data and local credentials are for development only.
- Screenshots should be regenerated from a running local environment.
```

## 发布前确认

- [ ] README 已说明与 LowAlt-RouteLab 的关系。
- [ ] 一致性链路文档已完成。
- [ ] 故障恢复演示文档已完成。
- [ ] 测试和构建结果已写入 `docs/release-validation.md`。
- [ ] 没有提交 `.env`、日志、构建产物或真实密钥。
- [ ] GitHub Release 标题和说明已准备。

