@echo off
cd /d %~dp0\..
docker compose -f docker-compose.monitor.yml down
echo Monitor stack stopped.
pause
