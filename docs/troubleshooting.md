# 常见问题排查

## 1. MySQL 中文导入失败

现象：

```text
ERROR 1366 Incorrect string value
```

原因：Windows 下 mysql 客户端未按 utf8mb4 读取 SQL 文件。

解决：

```bat
mysql -uroot -p123123 --default-character-set=utf8mb4 < docker\mysql\local-root-init.sql
```

## 2. Gateway 返回 503

可能原因：

```text
业务服务未启动
服务未注册到 Nacos
Gateway 缺少 loadbalancer 依赖
Nacos 未完全启动
```

检查：

```bat
curl http://127.0.0.1:8080/actuator/health
```

进入 Nacos 控制台检查服务实例。

## 3. 报 parameter name information not available

现象：

```text
Name for argument of type ... not specified
```

原因：Controller 中 `@RequestParam` 或 `@PathVariable` 未显式指定 name，或代码未重新 clean 编译。

解决：

```java
@RequestParam(name = "keyword", required = false)
@PathVariable("id")
```

重新编译：

```bat
mvn clean package -DskipTests
```

## 4. Redis 6379 端口被占用

检查：

```bat
netstat -ano | findstr :6379
```

如果是 Docker/WSL 占用，可暂时忽略；如需 Docker Redis 独立端口，把 compose 改为：

```yaml
ports:
  - "6380:6379"
```

并设置：

```text
REDIS_PORT=6380
```

## 5. RabbitMQ 无法连接

检查 RabbitMQ 管理台：

```text
http://127.0.0.1:15672/
lowaltitude / lowaltitude123
```

检查端口：

```bat
netstat -ano | findstr :5672
```

## 6. Nacos 可访问但服务注册失败

检查：

```text
NACOS_ADDR=127.0.0.1:8848
NACOS_REGISTER_IP=127.0.0.1
```

确认 Nacos 8848 和 9848 端口可用。

## 7. 前端无法调用接口

检查 Gateway：

```bat
curl http://127.0.0.1:8080/actuator/health
```

检查 `low-altitude-web/vite.config.js` 中代理是否指向：

```text
http://127.0.0.1:8080
```

## 8. Phase07 脚本提示 Prometheus 指标不存在

先确认 booking-service 已启动 Phase 07 版本，并访问：

```bat
curl http://127.0.0.1:8103/actuator/prometheus
```

如果 actuator 有内容但没有 HTTP 指标，可先调用业务接口，再刷新指标。

## 9. Grafana 没有 Dashboard

检查容器：

```bat
docker ps
```

重启监控：

```bat
scripts\stop-monitor.bat
scripts\start-monitor.bat
```

## 10. PowerShell 输出中文乱码

这是终端编码问题。可尝试：

```bat
chcp 65001
```

或使用 Windows Terminal。演示时优先展示前端页面。
