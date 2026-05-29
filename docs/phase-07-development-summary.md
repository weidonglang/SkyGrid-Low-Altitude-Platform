# Phase 07 · 限流、降级、监控与压测

## 1. 本阶段目标

Phase 07 的目标不是新增业务主流程，而是补齐微服务治理与可观测性能力，使系统可以在答辩中展示：

- 服务健康检查与 Prometheus 指标暴露；
- Grafana 可视化监控面板；
- Resilience4j RateLimiter 限流；
- Resilience4j CircuitBreaker 熔断降级；
- Resilience4j Retry 瞬时失败重试；
- Outbox、资源占用、通知、审计和幂等记录的业务指标；
- JMeter 压测脚本。

## 2. 后端新增能力

### 2.1 Prometheus 指标暴露

所有后端服务均增加 `micrometer-registry-prometheus`，并在 Actuator 中开放：

```text
/actuator/prometheus
```

暴露服务：

```text
low-altitude-gateway:        8080
user-org-service:            8101
resource-service:            8102
booking-service:             8103
conflict-notify-service:     8104
```

### 2.2 booking-service 治理演示接口

新增：

```text
GET /api/bookings/governance/summary
GET /api/bookings/governance/rate-limited
GET /api/bookings/governance/circuit?fail=true|false
GET /api/bookings/governance/retry?key=xxx&failTimes=2
```

说明：

- `/rate-limited` 用 `RateLimiter` 演示热点接口限流；
- `/circuit` 用 `CircuitBreaker` 演示下游异常时返回降级结果；
- `/retry` 用 `Retry` 演示瞬时失败后自动重试成功；
- `/summary` 聚合预约、占用、冲突和 outbox 状态。

### 2.3 业务指标

booking-service 新增 Prometheus 指标：

```text
low_altitude_outbox_pending
low_altitude_outbox_failed
low_altitude_resource_occupancy_occupied
low_altitude_conflict_active
low_altitude_governance_rate_limited_total
low_altitude_governance_circuit_fallback_total
low_altitude_governance_retry_success_total
```

conflict-notify-service 新增 Prometheus 指标：

```text
low_altitude_notify_sent
low_altitude_audit_total
low_altitude_idempotent_processed
```

## 3. 监控部署

新增：

```text
docker-compose.monitor.yml
docker/prometheus/prometheus.yml
docker/grafana/provisioning
docker/grafana/dashboards/low-altitude-overview.json
```

启动监控：

```bat
scripts\start-monitor.bat
```

或：

```bash
bash scripts/start-monitor.sh
```

访问：

```text
Prometheus: http://127.0.0.1:9090
Grafana:    http://127.0.0.1:3000
账号密码:   admin / lowaltitude123
```

Prometheus 通过 `host.docker.internal` 抓取宿主机上由 IDEA 启动的 5 个 Spring 服务。

## 4. 压测脚本

新增 JMeter 脚本：

```text
performance/jmeter/phase07-rate-limiter.jmx
```

它直接访问：

```text
http://127.0.0.1:8103/api/bookings/governance/rate-limited
```

用于制造短时突发请求，观察 RateLimiter 触发与 Prometheus / Grafana 指标变化。

## 5. 验证脚本

新增：

```text
scripts/phase07-smoke-test.bat
scripts/phase07-smoke-test.ps1
```

验证内容：

1. Gateway 健康检查；
2. 获取 ADMIN token；
3. booking-service `/actuator/prometheus` 可访问；
4. governance summary 可访问；
5. outbox summary 可访问；
6. notification summary 可访问；
7. RateLimiter 至少拒绝一次突发请求；
8. CircuitBreaker fallback 正常；
9. Retry 在瞬时失败后成功；
10. 自定义业务指标出现在 Prometheus actuator 输出中。

运行：

```bat
scripts\phase07-smoke-test.bat
```

成功标志：

```text
[Phase07] OK
```

## 6. 答辩演示建议

推荐演示顺序：

1. 打开 Nacos，展示服务注册；
2. 打开 Gateway `/actuator/health`，展示服务发现；
3. 打开 Prometheus targets，展示 5 个服务指标抓取；
4. 打开 Grafana dashboard，展示 HTTP 请求、延迟、outbox、占用、通知、幂等指标；
5. 运行 `phase07-smoke-test.bat`，展示限流、熔断、重试；
6. 运行 JMeter，对 `/governance/rate-limited` 制造并发，观察 Grafana 指标变化。
