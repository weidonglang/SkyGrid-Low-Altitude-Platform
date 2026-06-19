# SkyGrid Performance Report

## Scope

SkyGrid v1.0.0 provides a lightweight smoke benchmark for dashboard and message-health endpoints.

## Run

```bat
cd /d E:\javacode\low-altitude-platform
performance\skygrid-smoke.bat
```

## Output

```text
performance/results/skygrid-smoke.csv
```

## Metrics

- Endpoint
- HTTP status
- Elapsed milliseconds
- Average latency per endpoint

## Validation Note

No production-scale throughput number is claimed in this repository. Results must be generated on the local machine used for the release validation.
