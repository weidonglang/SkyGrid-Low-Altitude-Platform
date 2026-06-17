# SkyGrid Docker Deployment

## Scope

SkyGrid v0.3.0 introduces Docker Compose entry points for reproducible middleware and monitoring:

- Nacos
- MySQL
- Redis
- RabbitMQ
- Prometheus
- Grafana

The Java microservices and frontend are still started by local scripts because the repository does not currently contain Dockerfiles for each Spring service.

## Development Middleware

```bat
scripts\docker-start-dev.bat
scripts\check-dev-stack.bat
```

Stop middleware:

```bat
scripts\docker-stop-dev.bat
```

## Monitoring

```bat
docker compose -f docker-compose.monitoring.yml up -d
```

## Full Demo Support Services

```bat
docker compose -f docker-compose.full.yml up -d
```

Then start SkyGrid services:

```bat
scripts\start-dev-stack.bat
```

## Data Reset

`scripts\docker-reset-data.bat` prints the destructive reset command instead of running it automatically. This avoids accidental deletion of local demo data.
