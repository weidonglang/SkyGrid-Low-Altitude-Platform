# 演示流程脚本

本文档用于人工演示，不依赖图形文件或 PPT。

## 1. 演示前准备

确认服务启动：

```bat
curl http://127.0.0.1:8080/actuator/health
```

确认前端：

```text
http://127.0.0.1:5173
```

确认 Nacos：

```text
http://127.0.0.1:8848/nacos/
```

确认 RabbitMQ：

```text
http://127.0.0.1:15672/
```

确认 Grafana：

```text
http://127.0.0.1:3000/
```

## 2. 演示一：基础运行状态

1. 打开前端运行状态页。
2. 展示 Gateway health 为 UP。
3. 获取 ADMIN Token。
4. 在 Nacos 控制台展示 5 个服务注册成功。

讲解要点：系统采用 Spring Cloud Gateway + Nacos，服务通过注册中心发现，前端统一访问网关。

## 3. 演示二：低空资源模型

1. 进入“资源网格”页面。
2. 展示 `G-01-01`、`G-01-02`、`G-02-01`、`G-02-02`。
3. 说明：
   - `G-02-01` 是风险网格；
   - `G-02-02` 是禁飞网格；
   - 航线模板由多个 Grid 组成；
   - 资源占用的核心粒度是 `Date + Grid + Level + TimeSlot`。

## 4. 演示三：预约提交与审批占用

1. 进入“预约申请 / Pre-check”。
2. 选择默认航线 `RT-DEMO-01`。
3. 选择 L1、日期、时间片。
4. 执行 Pre-check。
5. 提交预约申请。
6. 进入审批工作台。
7. 审批通过。
8. 展示资源占用记录。

讲解要点：审批通过后，系统根据航线经过的 Grid 和申请的 TimeSlot 自动生成多条 `resource_occupancy`。

## 5. 演示四：冲突检测

1. 在已有占用未释放时，再次提交相同日期、航线、高度层、时间片的申请。
2. 执行 Pre-check。
3. 展示 `SAME_GRID_LEVEL_TIME` 硬冲突。
4. 尝试提交，系统返回冲突阻断。

讲解要点：硬冲突会阻断提交或审批，避免同一低空时空切片被重复占用。

## 6. 演示五：禁飞区与风险网格

1. 选择禁飞航线 `RT-NOFLY-01`。
2. 执行 Pre-check。
3. 展示 `NO_FLY_GRID` 阻断。
4. 切换到默认航线，展示经过 `RISK` 网格时产生风险提示但允许提交。

讲解要点：系统区分硬阻断与风险提示，支持低空巡检任务中的分级处理。

## 7. 演示六：RabbitMQ 与最终一致性

可以用脚本演示：

```bat
scripts\phase06-smoke-test.bat
```

讲解要点：

```text
审批通过
→ 写业务数据和 outbox_message
→ 派发 RabbitMQ
→ 通知服务消费
→ 写 notify_record
→ 写 audit_log
→ 幂等记录防止重复消费
```

重点展示：

- outbox 消息状态；
- notify_record；
- audit_log；
- duplicate skipped 幂等记录。

## 8. 演示七：限流、降级、重试与监控

运行：

```bat
scripts\phase07-smoke-test.bat
```

展示：

- RateLimiter 限流；
- CircuitBreaker 降级；
- Retry 第三次成功；
- Prometheus 指标；
- Grafana 面板。

## 9. 演示八：取消释放

1. 在审批工作台选择已审批申请。
2. 点击取消。
3. 查看资源占用状态从 `OCCUPIED` 变为 `RELEASED`。

讲解要点：资源释放后，对应时空切片可以被后续任务重新申请。

## 10. 演示兜底方案

如果前端临时不可用，使用脚本演示：

```bat
scripts\phase03-smoke-test.bat
scripts\phase04-smoke-test.bat
scripts\phase06-smoke-test.bat
scripts\phase07-smoke-test.bat
```

如果 Grafana 未启动，使用 Actuator 和脚本输出证明监控指标已暴露：

```bat
curl http://127.0.0.1:8103/actuator/prometheus
```
