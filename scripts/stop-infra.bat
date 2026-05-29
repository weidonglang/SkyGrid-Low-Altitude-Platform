@echo off
cd /d %~dp0\..
docker compose -f docker-compose.infra.yml down
docker compose -f docker-compose.middleware.yml down
