@echo off
cd /d %~dp0\..
docker compose -f docker-compose.middleware.yml up -d
echo.
echo Middleware started WITHOUT MySQL. Use this when your local MySQL root / 123123 is already running on 3306.
echo Nacos:    http://127.0.0.1:8848/nacos/
echo RabbitMQ: http://127.0.0.1:15672/  user=lowaltitude password=lowaltitude123
