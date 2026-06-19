# SkyGrid Rate Limit Demo

## Goal

Show that the booking service can reject bursty requests in a controlled way and record a metric.

## Endpoint

```text
GET /api/bookings/governance/rate-limited
```

The endpoint is protected by the `hotGridPreCheck` Resilience4j RateLimiter.

## Run

With Gateway and booking-service running:

```bat
for /l %i in (1,1,8) do curl http://127.0.0.1:8080/api/bookings/governance/rate-limited
```

Expected behavior:

- Some requests return success when capacity is available.
- Excess requests return `RATE_LIMITED`.
- `low_altitude_governance_rate_limited_total` increases in Prometheus metrics.

## Evidence

Capture evidence through:

```text
GET /api/bookings/governance/summary
GET /actuator/prometheus
```

This demo is designed for local engineering proof, not production SLA measurement.
