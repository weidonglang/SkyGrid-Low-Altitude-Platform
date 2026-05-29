# Windows 本地快速启动说明

本说明适用于 Windows 10/11 + IDEA + 本机 MySQL `root / 123123` 的开发运行方式。

## 1. 准备软件

确认本机已安装：

```text
JDK 17 或 21
Maven 3.9+
Node.js 18+
Docker Desktop
MySQL
IntelliJ IDEA
```

检查命令：

```bat
java -version
mvn -version
node -v
npm -v
docker version
mysql --version
```

## 2. 启动中间件

如果使用本机 MySQL，不要启动 Docker MySQL，执行：

```bat
scripts\start-middleware-no-mysql.bat
```

该脚本启动：

```text
Nacos
Redis
RabbitMQ
```

如果 Redis 6379 被占用，但服务不报 Redis 连接错误，可以暂时不处理；如需独立 Docker Redis，可把 docker-compose 中 Redis 外部端口改为 6380。

## 3. 初始化数据库

必须带 `--default-character-set=utf8mb4`，否则 Windows 下中文默认数据可能导入失败。

```bat
mysql -uroot -p123123 --default-character-set=utf8mb4 < docker\mysql\local-root-init.sql
```

检查数据库：

```bat
mysql -uroot -p123123 -e "SHOW DATABASES LIKE 'low_altitude_%';"
mysql -uroot -p123123 -e "USE low_altitude_resource; SHOW TABLES;"
mysql -uroot -p123123 -e "USE low_altitude_booking; SHOW TABLES;"
mysql -uroot -p123123 -e "USE low_altitude_conflict_notify; SHOW TABLES;"
```

## 4. IDEA 启动后端

打开项目根目录，刷新 Maven。建议每个服务 Run Configuration 都设置环境变量：

```text
NACOS_ADDR=127.0.0.1:8848;NACOS_REGISTER_IP=127.0.0.1;MYSQL_USER=root;MYSQL_PASSWORD=123123;REDIS_HOST=127.0.0.1;REDIS_PORT=6379;RABBITMQ_HOST=127.0.0.1;RABBITMQ_USER=lowaltitude;RABBITMQ_PASSWORD=lowaltitude123
```

按顺序启动：

```text
user-org-service
resource-service
booking-service
conflict-notify-service
low-altitude-gateway
```

检查 Gateway：

```bat
curl http://127.0.0.1:8080/actuator/health
```

## 5. 启动前端

```bat
scripts\start-web.bat
```

或手动：

```bat
cd low-altitude-web
npm install
npm run dev
```

打开：

```text
http://127.0.0.1:5173
```

## 6. 启动监控

```bat
scripts\start-monitor.bat
```

打开：

```text
Prometheus: http://127.0.0.1:9090
Grafana:    http://127.0.0.1:3000
```

Grafana：`admin / lowaltitude123`。

## 7. 最终检查

```bat
scripts\phase08-acceptance-check.bat
```

如果输出 `[Phase08] OK`，说明基础交付状态正常。
