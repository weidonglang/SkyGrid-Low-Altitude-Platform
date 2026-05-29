# 发布检查清单

发布到 GitHub 或交付演示前，建议按本清单完成检查。

## 1. 代码与仓库

- [ ] 根目录结构完整，核心模块均存在。
- [ ] `README.md`、`LICENSE`、`CONTRIBUTING.md`、`SECURITY.md`、`CHANGELOG.md` 存在且可读。
- [ ] `.gitignore` 已排除 `target/`、`node_modules/`、`dist/`、日志和本地环境文件。
- [ ] 未提交 `.env`、真实密钥、生产密码或个人凭据。
- [ ] Maven 能正常 reload。
- [ ] 前端依赖可通过 `npm ci` 或 `npm install` 安装。

## 2. 后端构建

- [ ] JDK 版本为 17 或更高。
- [ ] 执行以下命令通过：

```powershell
mvn clean package
```

如仅做快速检查，可执行：

```powershell
mvn clean package -DskipTests
```

## 3. 前端构建

- [ ] Node.js 版本为 18 或更高。
- [ ] 执行以下命令通过：

```powershell
cd low-altitude-web
npm ci
npm run build
```

## 4. 基础设施

- [ ] Nacos 可访问：`http://127.0.0.1:8848/nacos/`
- [ ] RabbitMQ 可访问：`http://127.0.0.1:15672/`
- [ ] Redis 可连接。
- [ ] MySQL 已完成初始化。
- [ ] Prometheus 可访问：`http://127.0.0.1:9090`
- [ ] Grafana 可访问：`http://127.0.0.1:3000`

## 5. 服务启动

- [ ] `user-org-service` 启动在 `8101`。
- [ ] `resource-service` 启动在 `8102`。
- [ ] `booking-service` 启动在 `8103`。
- [ ] `conflict-notify-service` 启动在 `8104`。
- [ ] `low-altitude-gateway` 启动在 `8080`。
- [ ] Gateway 健康检查为 `UP`：`http://127.0.0.1:8080/actuator/health`
- [ ] Nacos 服务列表能看到所有后端服务。

## 6. 数据库与业务链路

- [ ] 默认用户、组织、角色和审批人配置存在。
- [ ] 默认 Grid、Level、TimeSlot、RouteTemplate 存在。
- [ ] 可获取 ADMIN token。
- [ ] 可创建预约申请。
- [ ] 审批通过后能生成资源占用。
- [ ] 审批驳回和取消释放流程正常。
- [ ] 冲突检测、禁飞区和风险网格校验正常。
- [ ] Outbox 消息能投递到 RabbitMQ。
- [ ] 通知记录和审计日志能生成。
- [ ] 重复消息不会重复生成通知。

## 7. 前端验证

- [ ] `http://127.0.0.1:5173` 可打开。
- [ ] 控制台能获取 token。
- [ ] 资源网格页面能加载数据。
- [ ] 预约申请页面能提交请求。
- [ ] 审批工作台能查看并处理预约。
- [ ] 冲突记录页面能加载数据。
- [ ] 航路态势展示页面无明显布局错位。

## 8. 脚本验证

建议按需运行：

```powershell
scripts\phase03-smoke-test.bat
scripts\phase04-smoke-test.bat
scripts\phase06-smoke-test.bat
scripts\phase07-smoke-test.bat
scripts\phase08-acceptance-check.bat
```

最终验收脚本成功时应输出：

```text
[Phase08] OK
```

## 9. GitHub 发布

- [ ] 创建版本标签，例如 `v0.1.0`。
- [ ] Release Notes 从 `CHANGELOG.md` 提取。
- [ ] 上传必要截图或演示说明。
- [ ] 确认 GitHub Actions CI 通过。
- [ ] 确认 Issue 模板和 PR 模板展示正常。
