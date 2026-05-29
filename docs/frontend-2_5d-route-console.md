# 前端 2.5D 航线态势控制台升级说明

本次前端重构的目标是将系统从普通后台管理界面升级为“低空航线态势可视化控制台”。展示重点从表格 CRUD 转向低空资源、航线、时间片、冲突结果与消息链路的可视化呈现。

## 1. 核心设计目标

新版前端围绕以下要素组织界面：

- Grid：低空区域网格。
- Level：高度层。
- TimeSlot：时间片。
- RouteTemplate：航线模板，由有序 Grid 构成。
- ResourceOccupancy：审批通过后生成的资源占用。
- ConflictRecord：硬冲突、风险冲突、禁飞区等检测结果。
- Outbox / Notify / Audit / Idempotent：最终一致性链路。

## 2. 2.5D 空域图设计

核心组件为：

```text
src/components/AirspaceRouteMap.vue
```

它基于 SVG 绘制菱形 Grid 地块，并使用 CSS 动画显示航线流光、冲突脉冲、风险点、禁飞点和占用点。

状态映射如下：

| 数据状态 | 可视化效果 |
|---|---|
| ACTIVE | 青蓝色可用地块 |
| RISK | 黄色风险地块和感叹号 |
| NO_FLY | 红色禁飞地块和禁止图标 |
| OCCUPIED | 紫色占用点和高亮地块 |
| SAME_GRID_LEVEL_TIME | 红色 HARD 冲突标记 |
| RISK_GRID | 黄色风险标记 |
| ADJACENT_GRID_OCCUPIED | 黄色风险提示 |

## 3. 页面结构

新版页面包括：

1. 态势总览：系统健康、KPI、低空态势概览。
2. 低空航线态势：航线模板库 + 2.5D 地图 + 航线详情。
3. 预约 / Pre-check：表单 + 候选航线地图 + 冲突结果面板。
4. 审批工作台：预约卡片 + 占用预览 + 流程记录。
5. 冲突记录：冲突卡片 + 地图高亮 + 详情。
6. 消息一致性：Outbox → RabbitMQ → Notify → Audit → Idempotent 链路。
7. 治理监控：限流、降级、重试演示入口。

## 4. 为什么不用 Three.js

当前项目的核心建模是离散化的 Grid-Level-TimeSlot，不是真实三维仿真。因此采用 SVG 更合适：

- 易于点击、悬浮、选择和事件绑定。
- 易于根据后端数据直接映射状态。
- 易于绘制航线 path、节点、冲突 marker。
- 运行稳定，不依赖 WebGL。
- 答辩现场风险更低。

## 5. 验收方式

进入 `low-altitude-web` 后执行：

```bat
npm install
npm run build
npm run dev
```

打开：

```text
http://127.0.0.1:5173
```

完成以下检查：

- 能获取 ADMIN Token。
- 能刷新 Grid、Level、TimeSlot、RouteTemplate。
- 2.5D 地图能显示航线模板路径。
- Pre-check 结果能在地图上显示 HARD/RISK/NO_FLY 标记。
- 审批通过后占用记录能映射到地图。
- 消息一致性页面能显示 Outbox、Notify、Audit 数据。
- 治理监控页面能触发 RateLimiter、CircuitBreaker、Retry。
