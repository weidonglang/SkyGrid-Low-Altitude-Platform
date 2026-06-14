# SkyGrid 与 LowAlt-RouteLab 项目边界

本文档说明 SkyGrid 和 LowAlt-RouteLab 在低空项目群中的职责边界、数据交接点和演示闭环。

## 项目群定位

两个项目共同表达一个低空工程闭环：

```text
LowAlt-RouteLab
航线规划 / 风险评估 / 能耗估计 / TimeSlot 转换
        ↓
SkyGrid
空域预约 / 审批流转 / 冲突检测 / 占用记录 / 消息通知 / 监控治理
```

SkyGrid 不是路径规划系统，LowAlt-RouteLab 也不是审批治理平台。它们分别覆盖低空任务从“生成航线”到“占用空域”的两个阶段。

## SkyGrid 负责什么

SkyGrid 负责低空空域资源治理，核心对象是可审批、可占用、可审计的时空资源。

- 空域资源建模：`Grid + Level + TimeSlot + Date`。
- 预约申请：接收任务方提交的空域占用申请。
- 冲突检测：识别硬冲突、禁飞区冲突、风险网格和相邻网格风险。
- 审批流转：支持审批通过、驳回、取消释放。
- 占用记录：审批通过后写入 `resource_occupancy`。
- 消息一致性：通过 `outbox_message`、RabbitMQ、幂等消费和补偿任务保证最终一致性。
- 通知审计：生成 `notify_record`、`audit_log` 和幂等消费记录。
- 服务治理：展示限流、降级、重试、监控指标和队列状态。

## LowAlt-RouteLab 负责什么

LowAlt-RouteLab 负责低空航线算法仿真，核心对象是可解释、可评估、可提交的航线。

- 低空地图建模：20x20 demo 城市网格、禁飞区、障碍物、风险区。
- 路径规划：Dijkstra、A*、Theta*。
- 转弯代价：使用 C8 离散航向表达转弯成本。
- 对称增强：使用 D4 旋转/反射生成增强任务。
- 风险评估：输出风险分数、风险等级和风险因素。
- 能耗估计：结合距离、转弯和风险因素估算能耗。
- 占用转换：将路径转换成 `Grid + Level + TimeSlot` 占用序列。
- SkyGrid 联动：通过 mock 客户端模拟冲突检查和预约提交。

## 数据交接点

LowAlt-RouteLab 向 SkyGrid 交付的关键数据不是普通路径坐标，而是 SkyGrid 可识别的占用单元。

| 字段 | 含义 | 示例 |
| --- | --- | --- |
| `gridCode` | 低空网格编号 | `G-08-12` |
| `levelCode` | 高度层 | `L120` |
| `startTime` | 占用开始时间 | `2026-06-08T10:15:00` |
| `endTime` | 占用结束时间 | `2026-06-08T10:20:00` |
| `slotCode` | 离散时间片 | `T-003` |
| `riskScore` | 路径风险评分 | `42.5` |
| `energyCost` | 估算能耗 | `18.7` |

SkyGrid 接收这些占用单元后，按照 `Date + Grid + Level + TimeSlot` 检查是否与已有预约冲突。

## 演示闭环

完整演示应按以下顺序展开：

1. 在 LowAlt-RouteLab 中创建无人机任务。
2. 选择起点、终点、高度层、算法和是否规避风险。
3. 生成航线，查看路径长度、风险评分、转弯次数和能耗估计。
4. 将路径转换为 TimeSlot 占用序列。
5. 将占用序列提交到 SkyGrid 进行冲突检查。
6. 在 SkyGrid 中提交预约申请并进入审批流。
7. 审批通过后写入资源占用。
8. 通过 Outbox 和 RabbitMQ 派发通知。
9. 通知服务消费消息，写入通知记录和审计日志。
10. 在监控面板查看队列、重试、限流和服务健康。

## 边界说明

- SkyGrid 不评价路径算法优劣，只判断提交的占用序列是否可审批、可占用。
- LowAlt-RouteLab 不承担真实审批流，只输出可被 SkyGrid 消费的航线与占用结果。
- 当前 LowAlt-RouteLab 使用 `MockSkyGridClient` 模拟真实 SkyGrid，便于无外部依赖演示。
- 接入真实 SkyGrid 时，mock 客户端应替换为 Gateway API 调用。
