# Low Altitude Platform

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17%2B-blue.svg)](pom.xml)
[![Vue](https://img.shields.io/badge/Vue-3-42b883.svg)](low-altitude-web/package.json)

低空时空资源协同调度与冲突消解平台。项目面向无人机巡检、低空航线预约、空域资源审批和运行监测等场景，将低空资源抽象为 `Grid 区域网格 + Level 高度层 + TimeSlot 时间片 + Date 日期` 的时空切片模型，并围绕该模型提供预约、审批、占用、冲突检测、通知审计、限流降级和可视化控制台能力。

当前版本为 `0.1.0-SNAPSHOT`，后端采用 Spring Cloud 微服务架构，前端采用 Vue 3 + Vite。

## 与 LowAlt-RouteLab 的关系

SkyGrid 是低空资源调度与冲突治理平台，负责空域资源建模、预约审批、占用记录、冲突检测、消息通知和治理监控。LowAlt-RouteLab 是低空航线规划与风险评估仿真系统，负责根据起点、终点、高度层、禁飞区、风险区和障碍物生成航线，并将航线转换为 SkyGrid 可识别的 `Grid + Level + TimeSlot` 占用序列。

两个项目组合后形成完整闭环：

```text
LowAlt-RouteLab
航线规划 / 风险评估 / 能耗估计 / TimeSlot 转换
        ↓
SkyGrid
空域预约 / 审批流转 / 冲突检测 / 占用记录 / 消息通知 / 监控治理
```

主链路：

```text
航线规划 → 风险评估 → TimeSlot 占用转换 → SkyGrid 冲突检测 → 预约审批 → 通知审计 → 监控治理
```

## 项目总结

本项目是一套完整的低空资源预约与冲突治理演示平台，覆盖从资源建模、申请审批、冲突预检、资源占用到消息通知、审计留痕和监控观测的主链路。它既可以作为低空经济、无人机巡检、空域预约调度等课题的工程原型，也可以作为 Spring Cloud 微服务、消息最终一致性、服务治理和前端可视化的综合实践项目。

平台的核心价值是把低空空域拆分为可计算、可审批、可占用的时空切片，并通过审批流和冲突检测避免同一时间、同一空间或高风险区域内的任务冲突。

## 功能概览

- 低空资源建模：网格、航线模板、高度层、时间片、日期维度组合管理。
- 用户组织体系：用户、组织、角色、审批人配置和 JWT 登录鉴权。
- 预约审批流程：预约申请、审批通过、驳回、取消释放、资源占用生成。
- 冲突与风险检测：硬冲突、禁飞区、风险网格、相邻网格风险和审批前校验。
- 消息最终一致性：Outbox 本地消息表、RabbitMQ 投递、幂等消费、通知记录和审计日志。
- 服务治理与观测：Gateway 路由、Nacos 注册发现、Resilience4j 限流/降级/重试、Actuator、Prometheus、Grafana。
- 前端控制台：资源网格、航线态势、预约申请、审批工作台、冲突记录和 2.5D 航路展示。

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 后端 | Java 17、Spring Boot 3.2、Spring Cloud 2023、Spring Cloud Alibaba |
| 网关与注册 | Spring Cloud Gateway、Nacos |
| 数据与消息 | MySQL、MyBatis、Redis、RabbitMQ、Outbox |
| 治理与监控 | Resilience4j、Actuator、Micrometer、Prometheus、Grafana |
| 前端 | Vue 3、Vite、Element Plus、Axios、ECharts |
| 工程化 | Maven、npm、Docker Compose、JMeter |

## 项目结构

```text
low-altitude-platform/
├── low-altitude-common/           # 公共响应、异常、JWT、安全上下文
├── low-altitude-gateway/          # 网关、路由、鉴权、CORS
├── user-org-service/              # 用户、组织、角色、审批配置、认证
├── resource-service/              # Grid、Level、TimeSlot、RouteTemplate
├── booking-service/               # 预约、审批、占用、冲突检测、Outbox
├── conflict-notify-service/       # 通知消费、审计、幂等记录
├── low-altitude-web/              # Vue 3 前端控制台
├── docker/                        # MySQL、Prometheus、Grafana 配置
├── docs/                          # API、部署、测试、排错和交付文档
├── performance/                   # JMeter 压测脚本
├── scripts/                       # 启停、验收、冒烟测试脚本
└── docker-compose.*.yml           # 本地基础设施编排
```

## 环境要求

- JDK 17+
- Maven 3.9+
- Node.js 18+ 与 npm 9+
- Docker Desktop
- MySQL 8.x，可使用 Docker Compose 内置实例或本机实例

默认本地账号仅用于开发演示，请在生产环境中通过环境变量替换：

```text
MySQL:    root / 123123
RabbitMQ: lowaltitude / lowaltitude123
Grafana:  admin / lowaltitude123
```

## 快速开始

1. 复制环境变量示例：

```powershell
Copy-Item .env.example .env
```

2. 启动基础设施：

```powershell
docker compose -f docker-compose.infra.yml up -d
```

如果使用本机 MySQL，只启动 Nacos、Redis、RabbitMQ：

```powershell
scripts\start-middleware-no-mysql.bat
mysql -uroot -p123123 --default-character-set=utf8mb4 < docker\mysql\local-root-init.sql
```

3. 构建后端：

```powershell
mvn clean package -DskipTests
```

4. 在 IDE 中依次启动后端服务：

| 服务 | 端口 |
| --- | --- |
| user-org-service | 8101 |
| resource-service | 8102 |
| booking-service | 8103 |
| conflict-notify-service | 8104 |
| low-altitude-gateway | 8080 |

5. 启动前端：

```powershell
cd low-altitude-web
npm install
npm run dev
```

访问 `http://127.0.0.1:5173`。

## 常用入口

| 入口 | 地址 |
| --- | --- |
| 前端控制台 | http://127.0.0.1:5173 |
| Gateway | http://127.0.0.1:8080 |
| Gateway Health | http://127.0.0.1:8080/actuator/health |
| Nacos | http://127.0.0.1:8848/nacos/ |
| RabbitMQ | http://127.0.0.1:15672/ |
| Prometheus | http://127.0.0.1:9090 |
| Grafana | http://127.0.0.1:3000 |

## 验证与测试

```powershell
mvn test
cd low-altitude-web
npm run build
```

本项目还提供阶段性冒烟测试和最终验收脚本：

```powershell
scripts\phase03-smoke-test.bat
scripts\phase04-smoke-test.bat
scripts\phase06-smoke-test.bat
scripts\phase07-smoke-test.bat
scripts\phase08-acceptance-check.bat
```

## 文档索引

| 文档 | 说明 |
| --- | --- |
| [docs/quick-start-windows.md](docs/quick-start-windows.md) | Windows 本地快速启动 |
| [docs/project-summary.md](docs/project-summary.md) | 项目总结与发布简介 |
| [docs/deployment-guide.md](docs/deployment-guide.md) | 部署与运行说明 |
| [docs/database-initialization.md](docs/database-initialization.md) | 数据库初始化与默认数据 |
| [docs/api-reference.md](docs/api-reference.md) | 核心 API 参考 |
| [docs/frontend-running-guide.md](docs/frontend-running-guide.md) | 前端运行与演示说明 |
| [docs/monitoring-guide.md](docs/monitoring-guide.md) | Prometheus、Grafana、JMeter |
| [docs/troubleshooting.md](docs/troubleshooting.md) | 常见问题排查 |
| [docs/release-checklist.md](docs/release-checklist.md) | 发布前检查清单 |
| [docs/project-boundary.md](docs/project-boundary.md) | 与 LowAlt-RouteLab 的项目边界 |
| [docs/demo-script.md](docs/demo-script.md) | 演示流程脚本 |
| [docs/consistency-design.md](docs/consistency-design.md) | Outbox 与 RabbitMQ 最终一致性设计 |
| [docs/failure-recovery-demo.md](docs/failure-recovery-demo.md) | 故障恢复与补偿演示 |
| [docs/message-flow.md](docs/message-flow.md) | 审批到通知审计的消息流 |
| [docs/release-checklist-v0.1.0.md](docs/release-checklist-v0.1.0.md) | v0.1.0 发布检查清单 |
| [docs/release-validation.md](docs/release-validation.md) | v0.1.0 验证记录 |
| [docs/frontend-low-altitude-console-plan.md](docs/frontend-low-altitude-console-plan.md) | 低空运行态势控制台升级规划 |

## 发布说明

推荐发布前执行：

```powershell
mvn clean package
cd low-altitude-web
npm ci
npm run build
cd ..
scripts\phase08-acceptance-check.bat
```

发布版本建议使用语义化版本，例如 `v0.1.0`、`v0.2.0`。

## 贡献

欢迎通过 Issue 和 Pull Request 参与改进。提交前请阅读 [CONTRIBUTING.md](CONTRIBUTING.md)，并确保不要提交 `.env`、日志、构建产物或真实密钥。

## 许可证

本项目基于 [MIT License](LICENSE) 开源。
