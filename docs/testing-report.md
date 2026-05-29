# 测试与验收记录

## 1. 测试范围

本测试记录覆盖：

```text
微服务启动与注册
用户组织基础接口
低空资源基础接口
预约申请与审批闭环
冲突检测与低空规则
RabbitMQ / Outbox / 幂等通知 / 审计
限流 / 降级 / 重试 / Prometheus 指标
前端页面可用性
```

## 2. 测试环境

| 项 | 内容 |
|---|---|
| OS | Windows 10/11 |
| JDK | 17 或 21 |
| MySQL | 本机 MySQL，账号 root / 123123 |
| 中间件 | Docker Desktop 启动 Nacos、RabbitMQ、Redis |
| 后端 | IDEA 启动 5 个 Spring Boot 服务 |
| 前端 | Vite dev server |
| 监控 | Prometheus + Grafana |

## 3. 阶段测试结果

| 阶段 | 脚本 | 预期结果 | 状态 |
|---|---|---|---|
| Phase 02 | `scripts\phase02-smoke-test.bat` | 用户组织和资源接口返回成功 | 通过 |
| Phase 03 | `scripts\phase03-smoke-test.bat` | 申请、审批、占用、取消释放完成 | 通过 |
| Phase 04 | `scripts\phase04-smoke-test.bat` | RISK 提示、HARD 阻断、NO_FLY 阻断完成 | 通过 |
| Phase 06 | `scripts\phase06-smoke-test.bat` | Outbox、MQ、通知、审计、幂等完成 | 通过 |
| Phase 07 | `scripts\phase07-smoke-test.bat` | 限流、降级、重试、指标暴露完成 | 通过 |
| Phase 08 | `scripts\phase08-acceptance-check.bat` | 非破坏性交付检查完成 | 待本机执行 |

## 4. 关键验收点

### 4.1 微服务注册发现

验收命令：

```bat
curl http://127.0.0.1:8080/actuator/health
```

预期：

```text
status = UP
discoveryClient services 包含 5 个服务
```

### 4.2 预约审批闭环

验证内容：

```text
提交申请 -> PENDING
审批通过 -> APPROVED
生成 resource_occupancy -> OCCUPIED
取消申请 -> CANCELLED
占用释放 -> RELEASED
```

### 4.3 冲突检测

验证内容：

```text
默认航线经过 RISK 网格：允许提交，记录风险
重复申请相同资源：返回 409 冲突
禁飞航线：NO_FLY_GRID 阻断
```

### 4.4 消息最终一致性

验证内容：

```text
审批通过后生成 outbox_message
手动 dispatch 后投递 RabbitMQ
通知服务消费后生成 notify_record
audit_log 记录 NOTIFY_SENT
重复投递后记录 DUPLICATE_SKIPPED
```

### 4.5 服务治理

验证内容：

```text
RateLimiter：突发请求被限流
CircuitBreaker：失败请求返回降级响应
Retry：模拟前两次失败，第三次成功
Prometheus：业务指标暴露
```

## 5. 已知非功能问题

| 问题 | 影响 | 处理建议 |
|---|---|---|
| PowerShell/CMD 中文乱码 | 影响终端观感，不影响数据库和接口逻辑 | 前端演示中文；脚本输出尽量英文 |
| Redis 6379 可能被 Docker/WSL 占用 | 当前主流程不受影响 | 后续可改 Docker Redis 外部端口为 6380 |
| 相邻 Grid 风险可能出现多条提示 | 展示较多，不影响硬冲突判断 | 后续做去重优化 |

## 6. 最终测试建议

交付前执行：

```bat
scripts\phase08-acceptance-check.bat
```

需要完整复现主链路时执行：

```bat
scripts\phase03-smoke-test.bat
scripts\phase04-smoke-test.bat
scripts\phase06-smoke-test.bat
scripts\phase07-smoke-test.bat
```
