# 最终交付检查清单

## 1. 代码检查

- [ ] 项目根目录结构完整。
- [ ] Maven 能正常 reload。
- [ ] `mvn clean package -DskipTests` 能通过。
- [ ] 前端 `npm install`、`npm run dev` 能启动。
- [ ] `.env.example` 存在。
- [ ] 未提交 `target`、`node_modules`、日志文件等临时文件。

## 2. 数据库检查

- [ ] `local-root-init.sql` 可执行。
- [ ] 四个数据库均存在。
- [ ] 默认用户、角色、组织存在。
- [ ] 默认 Grid、Level、TimeSlot、RouteTemplate 存在。
- [ ] Outbox、通知、审计、幂等表存在。

## 3. 后端检查

- [ ] Nacos 可访问。
- [ ] RabbitMQ 可访问。
- [ ] 5 个服务均启动。
- [ ] Gateway health 为 UP。
- [ ] Nacos 服务列表中能看到 5 个服务。
- [ ] 获取 ADMIN token 正常。

## 4. 前端检查

- [ ] `http://127.0.0.1:5173` 可打开。
- [ ] 能获取 token。
- [ ] 资源网格页面能加载 Grid。
- [ ] Pre-check 能返回风险/冲突结果。
- [ ] 审批工作台能查看预约。
- [ ] 冲突记录页面能加载数据。

## 5. 消息链路检查

- [ ] 审批通过生成 outbox_message。
- [ ] dispatch 后 outbox 状态变为 SENT。
- [ ] notify_record 生成通知。
- [ ] audit_log 生成审计。
- [ ] 重复投递不会重复生成通知。

## 6. 治理监控检查

- [ ] `/actuator/prometheus` 可访问。
- [ ] RateLimiter 能触发限流。
- [ ] CircuitBreaker 能返回降级响应。
- [ ] Retry 能在失败后成功。
- [ ] Prometheus 可访问。
- [ ] Grafana 可访问。
- [ ] JMeter 脚本存在。

## 7. 文档检查

- [ ] README 完整。
- [ ] 快速启动说明完整。
- [ ] 数据库初始化说明完整。
- [ ] 前端说明完整。
- [ ] 监控说明完整。
- [ ] API 文档完整。
- [ ] 演示脚本完整。
- [ ] 截图清单完整。
- [ ] 测试报告完整。
- [ ] 排错文档完整。

## 8. 最终命令

```bat
scripts\phase08-acceptance-check.bat
```

成功输出：

```text
[Phase08] OK
```
