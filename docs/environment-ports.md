# 环境变量、端口与账号速查

## 1. 服务端口

| 服务 | 端口 | 地址 |
|---|---:|---|
| Gateway | 8080 | http://127.0.0.1:8080 |
| user-org-service | 8101 | http://127.0.0.1:8101 |
| resource-service | 8102 | http://127.0.0.1:8102 |
| booking-service | 8103 | http://127.0.0.1:8103 |
| conflict-notify-service | 8104 | http://127.0.0.1:8104 |
| 前端 Vite | 5173 | http://127.0.0.1:5173 |
| Nacos | 8848 | http://127.0.0.1:8848/nacos/ |
| RabbitMQ 管理台 | 15672 | http://127.0.0.1:15672/ |
| RabbitMQ AMQP | 5672 | 127.0.0.1:5672 |
| MySQL | 3306 | 127.0.0.1:3306 |
| Redis | 6379 | 127.0.0.1:6379 |
| Prometheus | 9090 | http://127.0.0.1:9090 |
| Grafana | 3000 | http://127.0.0.1:3000 |

## 2. 账号密码

| 组件 | 账号 | 密码 |
|---|---|---|
| MySQL 本机 | root | 123123 |
| RabbitMQ | lowaltitude | lowaltitude123 |
| Grafana | admin | lowaltitude123 |
| Nacos | nacos | nacos |

## 3. 后端环境变量

```text
NACOS_ADDR=127.0.0.1:8848;NACOS_REGISTER_IP=127.0.0.1;MYSQL_USER=root;MYSQL_PASSWORD=123123;REDIS_HOST=127.0.0.1;REDIS_PORT=6379;RABBITMQ_HOST=127.0.0.1;RABBITMQ_USER=lowaltitude;RABBITMQ_PASSWORD=lowaltitude123
```

## 4. 常用 health 地址

```text
http://127.0.0.1:8080/actuator/health
http://127.0.0.1:8101/actuator/health
http://127.0.0.1:8102/actuator/health
http://127.0.0.1:8103/actuator/health
http://127.0.0.1:8104/actuator/health
```

## 5. Prometheus 指标地址

```text
http://127.0.0.1:8080/actuator/prometheus
http://127.0.0.1:8101/actuator/prometheus
http://127.0.0.1:8102/actuator/prometheus
http://127.0.0.1:8103/actuator/prometheus
http://127.0.0.1:8104/actuator/prometheus
```
