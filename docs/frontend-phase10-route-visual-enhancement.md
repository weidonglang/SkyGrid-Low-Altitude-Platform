# Phase 10 前端增强：航线编辑、多航线对比与更多演示数据

本阶段在 2.5D 航线态势控制台基础上，继续增强以下内容：

1. 航线编辑器：在 2.5D 空域网格中点击 Grid 生成草稿航线，可保存为新的 route_template。
2. 多航线对比：支持同时叠加多条航线，显示不同航线的路径、风险点、禁飞点和 Grid 数量。
3. 消息一致性链路图：增强 Outbox、RabbitMQ、Notify、Audit、Idempotent 链路的可视化操作入口，支持重复投递与重新入队。
4. 治理监控操作面板：保留并强化 RateLimiter、CircuitBreaker、Retry 的网页演示入口。
5. 更多演示数据：数据库初始化脚本扩展到 5×5 Grid、更多风险/禁飞区域、更多时间片、多条航线模板和示例预约占用数据。

## 使用方法

执行数据库初始化脚本以加载更多演示数据：

```bat
mysql -uroot -p123123 --default-character-set=utf8mb4 < docker\mysql\local-root-init.sql
```

启动后端 5 个服务后，启动前端：

```bat
cd low-altitude-web
npm install
npm run dev
```

浏览器打开：

```text
http://127.0.0.1:5173
```

## 建议演示流程

1. 点击“获取 ADMIN Token”。
2. 点击“刷新全量态势数据”。
3. 进入“低空航线态势”，观察 5×5 Grid 和多条航线模板。
4. 进入“航线编辑器”，按顺序点击多个 Grid，保存新航线模板。
5. 进入“多航线对比”，勾选 3–5 条航线，观察路径重合、风险网格、禁飞网格。
6. 进入“预约 / Pre-check”，选择航线、日期、高度层和时间片，执行冲突预检查。
7. 进入“消息一致性”，查看 Outbox → RabbitMQ → Notify → Audit → Idempotent 链路，并可执行重复投递演示。
8. 进入“治理监控”，触发限流、降级和重试，查看接口返回。

## 新增示例数据

资源侧新增：

- 5×5 低空 Grid。
- 多个 ACTIVE / RISK / NO_FLY 区域。
- EV-01、NT-01 两个扩展时间片。
- RT-SOLAR-01、RT-WAREHOUSE-02、RT-RIVER-03、RT-RISK-BYPASS-04、RT-PERIMETER-05、RT-NOFLY-02 等航线模板。

预约侧新增：

- 光伏阵列例行巡检：已审批，占用 L2 / AM-01 / AM-02。
- 仓储屋顶巡检：已审批，占用 L1 / PM-01。
- 河道边界巡检：待审批，用于审批工作台展示。

