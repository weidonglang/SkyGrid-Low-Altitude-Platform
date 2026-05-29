# 部署与运行说明

## 1. 部署模式

当前项目推荐三种运行模式。

### 模式 A：本地开发模式

适合日常开发与调试：

```text
中间件：Docker Desktop
MySQL：本机 MySQL root / 123123
后端服务：IDEA 逐个启动
前端：Vite dev server
监控：按需启动 Prometheus / Grafana
```

这是当前已经验证最多的模式。

### 模式 B：后端 jar 运行模式

适合不使用 IDEA 的演示机：

```bat
mvn clean package -DskipTests
java -jar user-org-service\target\user-org-service-*.jar
java -jar resource-service\target\resource-service-*.jar
java -jar booking-service\target\booking-service-*.jar
java -jar conflict-notify-service\target\conflict-notify-service-*.jar
java -jar low-altitude-gateway\target\low-altitude-gateway-*.jar
```

每个命令窗口需要配置同样的环境变量。

### 模式 C：前端打包部署模式

用于把前端构建为静态资源：

```bat
cd low-altitude-web
npm install
npm run build
```

构建产物位于：

```text
low-altitude-web/dist
```

当前推荐答辩演示使用 Vite dev server，操作更简单。

## 2. 后端启动顺序

必须先启动中间件，再启动业务服务。

```text
1. Nacos / RabbitMQ / Redis / MySQL
2. user-org-service
3. resource-service
4. booking-service
5. conflict-notify-service
6. low-altitude-gateway
```

Gateway 最后启动，便于它通过 Nacos 发现已注册服务。

## 3. 中间件启动

本机 MySQL 模式：

```bat
scripts\start-middleware-no-mysql.bat
```

Docker 全量模式：

```bat
scripts\start-infra.bat
```

停止：

```bat
scripts\stop-infra.bat
```

## 4. 数据库初始化

```bat
mysql -uroot -p123123 --default-character-set=utf8mb4 < docker\mysql\local-root-init.sql
```

如果要重置数据库，可手动删除四个库后重新导入：

```bat
mysql -uroot -p123123 -e "DROP DATABASE IF EXISTS low_altitude_user_org; DROP DATABASE IF EXISTS low_altitude_resource; DROP DATABASE IF EXISTS low_altitude_booking; DROP DATABASE IF EXISTS low_altitude_conflict_notify;"
mysql -uroot -p123123 --default-character-set=utf8mb4 < docker\mysql\local-root-init.sql
```

## 5. 健康检查

```bat
curl http://127.0.0.1:8080/actuator/health
curl http://127.0.0.1:8101/actuator/health
curl http://127.0.0.1:8102/actuator/health
curl http://127.0.0.1:8103/actuator/health
curl http://127.0.0.1:8104/actuator/health
```

## 6. 交付验收

推荐最终验收顺序：

```bat
scripts\phase08-acceptance-check.bat
scripts\phase03-smoke-test.bat
scripts\phase04-smoke-test.bat
scripts\phase06-smoke-test.bat
scripts\phase07-smoke-test.bat
```

说明：Phase 03/04/06 脚本会写入测试数据；Phase 08 脚本主要做非破坏性检查。
