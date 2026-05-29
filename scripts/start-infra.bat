@echo off
cd /d %~dp0\..
docker compose -f docker-compose.infra.yml up -d
echo.
echo Infrastructure started: Nacos 8848/9848, MySQL 3306, Redis 6379, RabbitMQ 5672/15672.
echo Nacos:    http://127.0.0.1:8848/nacos/
echo RabbitMQ: http://127.0.0.1:15672/  user=lowaltitude password=lowaltitude123
