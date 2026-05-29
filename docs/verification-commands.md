# 常用验证命令

## 1. 获取 token

```bat
curl -X POST "http://127.0.0.1:8080/api/auth/dev-token?username=demo&role=ADMIN"
```

## 2. 基础资源

```bat
curl -H "Authorization: Bearer <token>" http://127.0.0.1:8080/api/resources/grids
curl -H "Authorization: Bearer <token>" http://127.0.0.1:8080/api/resources/levels
curl -H "Authorization: Bearer <token>" http://127.0.0.1:8080/api/resources/time-slots
curl -H "Authorization: Bearer <token>" http://127.0.0.1:8080/api/resources/route-templates/1
```

## 3. 预约与冲突

```bat
curl -H "Authorization: Bearer <token>" http://127.0.0.1:8080/api/bookings
curl -H "Authorization: Bearer <token>" http://127.0.0.1:8080/api/bookings/conflicts
```

## 4. Outbox 与通知

```bat
curl -H "Authorization: Bearer <token>" http://127.0.0.1:8080/api/bookings/outbox/summary
curl -H "Authorization: Bearer <token>" http://127.0.0.1:8080/api/notifications/summary
```

## 5. 治理

```bat
curl -H "Authorization: Bearer <token>" http://127.0.0.1:8080/api/bookings/governance/summary
curl -H "Authorization: Bearer <token>" http://127.0.0.1:8080/api/bookings/governance/circuit?fail=true
curl -H "Authorization: Bearer <token>" "http://127.0.0.1:8080/api/bookings/governance/retry?key=demo&failTimes=2"
```

## 6. 监控

```bat
curl http://127.0.0.1:8103/actuator/prometheus
```

Prometheus 查询建议：

```text
low_altitude_outbox_pending
low_altitude_resource_occupancy_occupied
low_altitude_conflict_active
low_altitude_notify_sent
low_altitude_governance_rate_limited_total
```
