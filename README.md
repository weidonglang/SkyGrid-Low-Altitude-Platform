# 低空时空资源协同调度与冲突消解平台

本项目是一个面向无人机巡检任务的低空时空资源管理系统。系统将低空资源抽象为 **Grid 区域网格 + Level 高度层 + TimeSlot 时间片 + Date 日期** 的时空切片模型，并围绕该模型实现低空资源预约、审批、占用、冲突检测、消息最终一致性、可视化展示、限流降级和监控观测。

当前交付版本为 **Phase 08 Delivery Edition**，包含 Phase 01–07 已完成代码，以及 Phase 08 交付文档与辅助脚本。本版本不包含答辩 PPT、论文/毕设正文材料和图形制图文件。

---

## 1. 已完成功能

| 阶段 | 内容 | 状态 |
|---|---|---|
| Phase 01 | Spring Cloud 微服务骨架、Gateway、Nacos、MySQL、RabbitMQ、统一响应、JWT 开发 token | 完成 |
| Phase 02 | 用户、组织、角色、审批员配置、Grid、Level、TimeSlot、RouteTemplate 基础数据 | 完成 |
| Phase 03 | 预约申请、审批通过/驳回、取消释放、审批流转、资源占用生成 | 完成 |
| Phase 04 | 硬冲突、禁飞区、风险网格、相邻网格风险、冲突记录、审批前二次校验 | 完成 |
| Phase 05 | Vue 3 前端控制台、Grid 可视化、预约申请、审批工作台、冲突记录页面 | 完成 |
| Phase 06 | Outbox 本地消息表、RabbitMQ、幂等消费、通知记录、审计日志 | 完成 |
| Phase 07 | Resilience4j 限流/降级/重试、Prometheus、Grafana、JMeter 压测脚本、业务指标 | 完成 |
| Phase 08 | README、部署说明、数据库说明、接口文档、演示脚本、截图清单、测试报告、排错说明 | 完成 |

---

## 2. 模块结构

```text
low-altitude-platform
├── low-altitude-common              公共模块：统一响应、异常、JWT、用户上下文
├── low-altitude-gateway             网关服务：路由、JWT 鉴权、CORS、Nacos 服务发现
├── user-org-service                 用户组织服务：用户、角色、组织、审批配置、开发 token
├── resource-service                 低空资源服务：Grid、Level、TimeSlot、RouteTemplate
├── booking-service                  预约审批服务：申请、审批、占用、冲突检测、Outbox、治理指标
├── conflict-notify-service          通知审计服务：RabbitMQ 消费、通知、审计、幂等记录
├── low-altitude-web                 Vue 3 前端控制台
├── docker                           MySQL / Prometheus / Grafana 等配置
├── docs                             交付文档
├── performance                      JMeter 压测脚本
└── scripts                          启停、验证、演示辅助脚本
```

---

## 3. 技术栈

| 层级 | 技术 |
|---|---|
| 后端基础 | Java 17+、Spring Boot 3.2.x、Spring Cloud 2023.x |
| 微服务 | Spring Cloud Gateway、Nacos、OpenFeign |
| 数据层 | MySQL、MyBatis、HikariCP |
| 缓存/辅助 | Redis |
| 消息 | RabbitMQ、Outbox 本地消息表 |
| 可靠性 | 幂等消费、重试、补偿、审计日志 |
| 服务治理 | Resilience4j RateLimiter / CircuitBreaker / Retry |
| 观测 | Actuator、Micrometer、Prometheus、Grafana |
| 前端 | Vue 3、Vite、Element Plus、Axios、ECharts 预留 |
| 部署 | Docker Compose、Maven、npm |
| 压测 | JMeter |

---

## 4. 本地推荐环境

| 软件 | 推荐版本/说明 |
|---|---|
| Windows | Windows 10/11 |
| JDK | 17 或 21 |
| Maven | 3.9+ |
| Node.js | 18+ 或 20+ |
| npm | 9+ |
| Docker Desktop | 用于 Nacos、RabbitMQ、Redis、Prometheus、Grafana |
| MySQL | 可使用本机 MySQL，默认账号 `root / 123123` |

---

## 5. 快速启动

如果使用你当前本机 MySQL：

```bat
scripts\start-middleware-no-mysql.bat
mysql -uroot -p123123 --default-character-set=utf8mb4 < docker\mysql\local-root-init.sql
```

然后在 IDEA 中按顺序启动：

```text
user-org-service           8101
resource-service           8102
booking-service            8103
conflict-notify-service    8104
low-altitude-gateway       8080
```

启动前端：

```bat
scripts\start-web.bat
```

浏览器打开：

```text
http://127.0.0.1:5173
```

---

## 6. 验证脚本

| 脚本 | 用途 |
|---|---|
| `scripts\phase02-smoke-test.bat` | 用户组织与低空资源基础接口验证 |
| `scripts\phase03-smoke-test.bat` | 预约申请、审批、占用、取消释放验证 |
| `scripts\phase04-smoke-test.bat` | 冲突检测、禁飞区、风险规则验证 |
| `scripts\phase06-smoke-test.bat` | Outbox、RabbitMQ、幂等通知、审计验证 |
| `scripts\phase07-smoke-test.bat` | 限流、降级、重试、Prometheus 指标验证 |
| `scripts\phase08-acceptance-check.bat` | 非破坏性最终交付检查 |

建议最终交付前至少运行：

```bat
scripts\phase08-acceptance-check.bat
```

如果要重新完整验证主链路，可依次运行 Phase 03、Phase 04、Phase 06、Phase 07 脚本。

---

## 7. 监控启动

启动 Prometheus + Grafana：

```bat
scripts\start-monitor.bat
```

访问：

```text
Prometheus: http://127.0.0.1:9090
Grafana:    http://127.0.0.1:3000
账号密码:   admin / lowaltitude123
```

停止监控：

```bat
scripts\stop-monitor.bat
```

---

## 8. 主要入口

| 入口 | 地址 |
|---|---|
| 前端控制台 | http://127.0.0.1:5173 |
| Gateway | http://127.0.0.1:8080 |
| Gateway health | http://127.0.0.1:8080/actuator/health |
| Nacos | http://127.0.0.1:8848/nacos/ |
| RabbitMQ | http://127.0.0.1:15672/ |
| Prometheus | http://127.0.0.1:9090 |
| Grafana | http://127.0.0.1:3000 |

RabbitMQ 账号密码：

```text
lowaltitude / lowaltitude123
```

Grafana 账号密码：

```text
admin / lowaltitude123
```

---

## 9. 交付文档索引

| 文档 | 说明 |
|---|---|
| `docs/quick-start-windows.md` | Windows 本地快速启动 |
| `docs/deployment-guide.md` | 部署与运行说明 |
| `docs/database-initialization.md` | 数据库初始化与默认数据 |
| `docs/frontend-running-guide.md` | 前端运行与页面演示说明 |
| `docs/monitoring-guide.md` | Prometheus / Grafana / JMeter 说明 |
| `docs/api-reference.md` | 核心接口文档 |
| `docs/demo-script.md` | 演示流程脚本 |
| `docs/screenshot-checklist.md` | 截图清单 |
| `docs/testing-report.md` | 阶段测试记录与验收说明 |
| `docs/troubleshooting.md` | 常见问题排查 |
| `docs/release-checklist.md` | 最终交付检查清单 |
| `docs/environment-ports.md` | 端口、账号、环境变量速查 |
| `docs/phase-08-delivery-summary.md` | Phase 08 交付总结 |

---

## 10. 当前版本说明

本版本已经完成项目主要工程功能与交付说明，但不包含以下内容：

```text
架构图 / ER 图 / 流程图等制图文件
答辩 PPT 素材
论文 / 毕设正文写作材料
```

这些内容可在代码稳定后单独制作。
