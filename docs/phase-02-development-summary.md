# Phase 02 开发说明：用户组织与低空资源基础数据

本阶段目标是把 Phase 01 的“可启动微服务骨架”推进到“基础业务数据可落库、可查询、可维护”的状态。完成后，用户组织域与低空资源域不再只是 bootstrap 占位接口，而是具备真实 MySQL 表、MyBatis Mapper、Service 校验逻辑与 REST CRUD 接口。

## 1. 本阶段边界

已完成：

- 用户组织服务：组织、角色、用户、用户角色、审批员配置。
- 资源服务：Grid 区域网格、AltitudeLevel 高度层、TimeSlot 时间片、RouteTemplate 航线模板。
- MySQL 表结构与默认数据初始化。
- Gateway 路由补充 `/api/user-org/**`。
- API 文档与 smoke test 脚本。

暂不完成：

- 真正密码登录与 Spring Security 用户认证。
- 预约申请、审批流转、冲突检测落库。
- Redis 防重与资源缓存。
- 前端页面。

这些会进入 Phase 03 和 Phase 04。

## 2. 数据库初始化

如果你使用本机 MySQL `root / 123123`，在项目根目录执行：

```bat
mysql -uroot -p123123 < docker\mysql\local-root-init.sql
```

该脚本会创建或补全：

- `low_altitude_user_org`
- `low_altitude_resource`
- `low_altitude_booking`
- `low_altitude_conflict_notify`

并创建 Phase 02 所需表和默认数据。

## 3. 默认数据

用户组织库默认包含：

- 组织：`ORG_DEFAULT`
- 角色：`APPLICANT`、`APPROVER`、`ADMIN`
- 用户：`admin`
- 默认审批链路：默认组织的一审审批员为 `admin`

资源库默认包含：

- 示例 Grid：`G-01-01`、`G-01-02`、`G-02-01`、`G-02-02`
- 示例高度层：`L1`、`L2`、`L3`
- 示例时间片：`AM-01`、`AM-02`、`PM-01`、`PM-02`
- 示例航线模板：`RT-DEMO-01`

## 4. 启动顺序

继续沿用 Phase 01 的顺序：

1. Nacos / RabbitMQ / Redis / MySQL
2. user-org-service
3. resource-service
4. booking-service
5. conflict-notify-service
6. low-altitude-gateway

如果你本机 6379 被 Docker Desktop/WSL 占用，而服务暂时不报 Redis 连接错误，可以先不处理。Phase 02 主要验证 MySQL CRUD。

## 5. 关键验证点

服务启动后，Nacos 中应能看到：

- `user-org-service`
- `resource-service`
- `booking-service`
- `conflict-notify-service`
- `low-altitude-gateway`

然后通过 Gateway 访问：

- `GET /api/user-org/organizations`
- `GET /api/user-org/users`
- `GET /api/resources/grids`
- `GET /api/resources/levels`
- `GET /api/resources/time-slots`
- `GET /api/resources/route-templates/1`

## 6. 架构意义

本阶段建立了后续预约审批和冲突检测的基础数据底座：

- `Grid` 决定空域区域维度。
- `AltitudeLevel` 决定高度层维度。
- `TimeSlot` 决定离散时间维度。
- `RouteTemplate` 把无人机巡检任务从“单点预约”升级为“多网格路径占用”。
- `ApproverConfig` 为后续审批流转提供组织域依据。

Phase 03 将在这些基础上实现预约申请与审批主流程。
