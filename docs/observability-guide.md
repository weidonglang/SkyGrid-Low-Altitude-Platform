# SkyGrid Observability Guide

## Endpoints

Each Spring service exposes Actuator endpoints:

```text
/actuator/health
/actuator/metrics
/actuator/prometheus
```

Prometheus configuration:

```text
docker/prometheus/prometheus.yml
```

Grafana provisioning:

```text
docker/grafana/provisioning/
docker/grafana/dashboards/low-altitude-overview.json
```

## Key Signals

| Signal | Source |
|---|---|
| Gateway availability | `low-altitude-gateway /actuator/health` |
| Booking and occupancy counts | `GET /api/dashboard/overview` |
| Outbox pending / failed / retrying | `GET /api/dashboard/message-health` |
| Notification and idempotency state | `conflict-notify-service` metrics and notification APIs |
| Resilience demo counters | `low_altitude_governance_*` Micrometer counters |

## Local Monitoring Startup

```bat
docker compose -f docker-compose.monitoring.yml up -d
```

Open:

```text
Prometheus: http://127.0.0.1:9090
Grafana:    http://127.0.0.1:3000
```

## Validation Notes

The repository records observability configuration and smoke scripts. Real metric values depend on a running local SkyGrid stack and are not hard-coded into the docs.
