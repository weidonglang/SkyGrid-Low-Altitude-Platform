@echo off
echo [SkyGrid] This command removes the Docker MySQL/Redis/RabbitMQ volumes for a clean demo environment.
echo [SkyGrid] Run manually only when you intentionally want to reset local demo data:
echo docker compose -f docker-compose.dev.yml down -v
exit /b 0
