# SkyGrid Full Demo Deployment

## Startup

```bat
cd /d E:\javacode\low-altitude-platform
scripts\docker-start-dev.bat
scripts\start-dev-stack.bat
scripts\check-dev-stack.bat
scripts\prepare-demo-data.bat
```

## Demo

```bat
scripts\phase08-acceptance-check.bat
scripts\demo-e2e-check.bat
scripts\demo-outbox-recovery.bat
scripts\demo-idempotent-consume.bat
```

## LowAlt Integration

Start LowAlt-RouteLab separately and run:

```bat
cd /d E:\javacode\LowAlt-RouteLab
scripts\run-real-skygrid-demo.bat
```

## Shutdown

```bat
scripts\stop-dev-stack.bat
scripts\docker-stop-dev.bat
```
