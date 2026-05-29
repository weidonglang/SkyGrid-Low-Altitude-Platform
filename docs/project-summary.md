# 项目总结

## 项目名称

Low Altitude Platform，低空时空资源协同调度与冲突消解平台。

## 项目定位

本项目面向无人机巡检、低空航线预约、空域资源审批和运行态势展示等场景，提供一套从资源建模、任务预约、审批流转、冲突检测到通知审计和监控观测的端到端工程实现。

它适合作为：

- 低空经济、无人机调度、空域资源管理相关课程设计或毕业设计项目。
- Spring Cloud 微服务综合实践项目。
- Vue 3 可视化控制台示例项目。
- 消息最终一致性、服务治理、限流降级、可观测性实践样例。

## 核心模型

系统将低空资源抽象为四个维度：

```text
Grid 区域网格 + Level 高度层 + TimeSlot 时间片 + Date 日期
```

通过这个时空切片模型，系统可以判断某个无人机任务在指定区域、高度、时间和日期下是否可预约、是否冲突、是否存在禁飞或风险规则。

## 核心能力

- 用户组织与权限：用户、组织、角色、审批人配置、JWT 鉴权。
- 低空资源管理：网格、高度层、时间片、航线模板。
- 预约审批：任务申请、审批通过、审批驳回、取消释放。
- 冲突检测：同一时空资源硬冲突、禁飞区、风险网格和相邻网格风险。
- 消息通知：Outbox 本地消息表、RabbitMQ 投递、幂等消费、通知记录。
- 审计追踪：关键业务操作留痕。
- 服务治理：限流、降级、重试、健康检查。
- 可观测性：Prometheus 指标采集、Grafana 看板、JMeter 压测脚本。
- 前端控制台：资源网格、预约申请、审批工作台、冲突记录和航路态势展示。

## 技术架构

后端采用 Java 17、Spring Boot 3.2、Spring Cloud 2023 和 Spring Cloud Alibaba。服务通过 Nacos 注册发现，由 Spring Cloud Gateway 统一入口路由。数据存储使用 MySQL，缓存和辅助能力使用 Redis，异步通知链路使用 RabbitMQ。治理和观测侧使用 Resilience4j、Actuator、Micrometer、Prometheus 和 Grafana。

前端采用 Vue 3、Vite、Element Plus、Axios 和 ECharts，提供面向业务演示和操作验证的控制台页面。

## 模块划分

| 模块 | 职责 |
| --- | --- |
| `low-altitude-common` | 公共响应、异常、JWT、安全上下文 |
| `low-altitude-gateway` | 网关路由、鉴权、CORS、服务入口 |
| `user-org-service` | 用户、组织、角色、审批配置、认证 |
| `resource-service` | Grid、Level、TimeSlot、RouteTemplate |
| `booking-service` | 预约审批、占用生成、冲突检测、Outbox |
| `conflict-notify-service` | 通知消费、幂等处理、审计记录 |
| `low-altitude-web` | Vue 3 前端控制台 |

## 发布状态

当前仓库已补齐 GitHub 发布所需的基础材料：

- `README.md`
- `LICENSE`
- `CONTRIBUTING.md`
- `SECURITY.md`
- `CODE_OF_CONDUCT.md`
- `CHANGELOG.md`
- `.github` Issue/PR 模板
- `.github/workflows/ci.yml`
- `.github/dependabot.yml`
- `docs/release-checklist.md`

发布前建议执行：

```powershell
mvn clean package
cd low-altitude-web
npm ci
npm run build
```
