# 监控、治理与压测说明

## 1. 监控目标

Phase 07 已加入：

```text
Actuator
Micrometer Prometheus Registry
Prometheus
Grafana
Resilience4j RateLimiter / CircuitBreaker / Retry
JMeter 压测脚本
```

监控目标包括：

```text
服务健康状态
HTTP 请求量和延迟
JVM 内存和线程
Outbox pending / failed
资源占用数量
冲突记录数量
通知发送数量
审计日志数量
幂等消费数量
限流、降级、重试指标
```

## 2. 启动监控栈

```bat
scripts\start-monitor.bat
```

访问：

```text
Prometheus: http://127.0.0.1:9090
Grafana:    http://127.0.0.1:3000
```

Grafana：

```text
admin / lowaltitude123
```

停止：

```bat
scripts\stop-monitor.bat
```

## 3. Prometheus 抓取目标

Prometheus 配置文件：

```text
docker/prometheus/prometheus.yml
```

默认通过 `host.docker.internal` 抓取本机 IDEA 启动的服务：

```text
low-altitude-gateway:     8080/actuator/prometheus
user-org-service:         8101/actuator/prometheus
resource-service:         8102/actuator/prometheus
booking-service:          8103/actuator/prometheus
conflict-notify-service:  8104/actuator/prometheus
```

## 4. Grafana 面板

Grafana 已预置：

```text
docker/grafana/dashboards/low-altitude-overview.json
```

如果面板未自动出现，检查容器是否正常：

```bat
docker ps
```

## 5. 治理演示接口

| 接口 | 说明 |
|---|---|
| `GET /api/bookings/governance/summary` | 系统治理状态汇总 |
| `GET /api/bookings/governance/rate-limited` | 限流演示 |
| `GET /api/bookings/governance/circuit?fail=true` | 熔断/降级演示 |
| `GET /api/bookings/governance/retry?key=demo&failTimes=2` | 重试演示 |

## 6. 验证治理能力

```bat
scripts\phase07-smoke-test.bat
```

该脚本会验证：

```text
Prometheus actuator 可访问
summary 接口可访问
Outbox / Notification summary 可访问
RateLimiter 能拒绝突发请求
CircuitBreaker 能返回降级响应
Retry 能在瞬时失败后成功
业务指标能在 actuator/prometheus 中出现
```

## 7. JMeter 压测

JMeter 脚本：

```text
performance/jmeter/phase07-rate-limiter.jmx
```

使用 GUI 打开或命令行运行：

```bat
jmeter -n -t performance\jmeter\phase07-rate-limiter.jmx -l performance\jmeter\phase07-result.jtl
```

压测目标：

```text
http://127.0.0.1:8103/api/bookings/governance/rate-limited
```

预期现象：一部分请求成功，一部分请求返回限流响应。

## 8. 监控演示建议

答辩或验收时可按以下顺序展示：

1. 打开 Grafana 面板。
2. 运行 `phase07-smoke-test.bat`。
3. 观察限流、HTTP 请求、业务指标变化。
4. 打开 Prometheus 查询：

```text
low_altitude_outbox_pending
low_altitude_resource_occupancy_occupied
low_altitude_notify_sent
low_altitude_governance_rate_limited_total
```
