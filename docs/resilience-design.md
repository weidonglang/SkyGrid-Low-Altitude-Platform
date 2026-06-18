# SkyGrid Resilience Design

## Scope

SkyGrid v1.0.0 uses lightweight resilience policies for demo-critical APIs instead of claiming a production-grade traffic platform.

Protected areas:

- Booking pre-check and approval traffic.
- Dashboard and governance probes.
- Outbox dispatch and recovery demos.
- Notification-service degradation demos.

## Implemented Policies

| Policy | Instance | Purpose |
|---|---|---|
| RateLimiter | `hotGridPreCheck` | Reject bursty hot-grid demo traffic quickly. |
| CircuitBreaker | `notifyProbe` | Return a controlled degraded response when the downstream notification probe fails. |
| Retry | `remoteProbe` | Demonstrate transient-failure recovery and retry metrics. |

The demo endpoints live under:

```text
GET /api/bookings/governance/summary
GET /api/bookings/governance/rate-limited
GET /api/bookings/governance/circuit
GET /api/bookings/governance/retry
```

## Metrics

The governance demo exposes Micrometer counters:

```text
low_altitude_governance_rate_limited_total
low_altitude_governance_circuit_fallback_total
low_altitude_governance_retry_success_total
```

These are available through:

```text
GET /actuator/prometheus
```

## Demo Commands

```bat
curl http://127.0.0.1:8080/api/bookings/governance/summary
curl http://127.0.0.1:8080/api/bookings/governance/rate-limited
curl "http://127.0.0.1:8080/api/bookings/governance/circuit?fail=true"
curl "http://127.0.0.1:8080/api/bookings/governance/retry?key=v1&failTimes=2"
```

## Limitations

- The policies are intentionally small and demo-focused.
- Dashboard and booking endpoints remain backed by local MySQL/RabbitMQ/Nacos state.
- Full service-mesh traffic shaping is out of scope for this repository.
