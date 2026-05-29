# low-altitude-web

低空航线态势可视化控制台。新版前端将原来的表格后台升级为以 **2.5D SVG 空域图** 为中心的演示控制台，面向毕设/答辩展示：航线模板、Grid、Level、TimeSlot、资源占用、冲突检测、Outbox 消息一致性与治理监控可以在一个界面中联动观察。

## 技术栈

- Vue 3
- Vite
- Element Plus
- Axios
- SVG + CSS 动画实现 2.5D 空域航线图

## 启动方式

先启动后端 5 个服务：

1. user-org-service
2. resource-service
3. booking-service
4. conflict-notify-service
5. low-altitude-gateway

然后启动前端：

```bat
cd low-altitude-web
npm install
npm run dev
```

浏览器打开：

```text
http://127.0.0.1:5173
```

也可以在项目根目录运行：

```bat
scripts\start-web.bat
```

## 新版页面结构

| 页面 | 用途 |
|---|---|
| 态势总览 | 展示系统健康、核心指标、低空航线态势概览 |
| 低空航线态势 | 2.5D 可视化查看航线模板、风险区、禁飞区、占用区 |
| 预约 / Pre-check | 在地图上预览候选航线，并将冲突检测结果映射到 Grid 节点 |
| 审批工作台 | 审批通过、驳回、取消申请，并查看占用生成/释放 |
| 冲突记录 | 将 HARD/RISK/NO_FLY 等冲突记录映射回 2.5D 空域图 |
| 消息一致性 | 展示 Outbox → RabbitMQ → Notify → Audit → Idempotent 链路 |
| 治理监控 | 触发 RateLimiter、CircuitBreaker、Retry，并打开 Prometheus/Grafana |

## 推荐演示流程

1. 点击“获取 ADMIN Token”。
2. 点击“刷新全量态势数据”。
3. 进入“低空航线态势”，选择 `RT-DEMO-01` 查看航线 `G-01-01 → G-01-02 → G-02-01`。
4. 进入“预约 / Pre-check”，选择日期、高度层和时间片，点击“冲突预检查”。
5. 如果返回风险但未阻断，可以提交申请。
6. 进入“审批工作台”，审批通过，观察地图上资源占用生成。
7. 再次提交同日期、同航线、同高度层、同时间片的申请，观察 HARD 冲突映射到图上。
8. 进入“消息一致性”，查看 outbox、notify、audit 记录。
9. 进入“治理监控”，触发限流、降级和重试。

## 设计说明

新版前端没有引入 Three.js，而是使用 Vue + SVG 实现 2.5D 空域地图。这样更稳定，便于绑定点击事件、航线连线、冲突标记和状态高亮，也更适合本项目当前 Grid/Level/TimeSlot 的离散化建模。

## Phase 10 增强能力

本版本新增航线编辑器、多航线对比、增强版消息一致性链路和治理监控操作面板。为了获得最佳展示效果，请先重新执行数据库初始化脚本，加载 5×5 Grid、多条航线模板和示例占用数据。

```bat
mysql -uroot -p123123 --default-character-set=utf8mb4 < docker\mysql\local-root-init.sql
```

新增页面：

- 航线编辑器：点击 2.5D Grid 生成草稿航线并保存为模板。
- 多航线对比：同时叠加多条 route_template，观察路径重合、风险点和禁飞点。
- 消息一致性：支持查看 Outbox、Notify、Audit，并触发重复投递/重新入队。
- 治理监控：网页触发 RateLimiter、CircuitBreaker、Retry 演示。
