# Phase 01 架构说明

## 1. 阶段目标

第一次开发的目标是完成“可继续开发”的微服务工程骨架。此阶段不追求完整业务功能，而是把服务边界、公共规范、网关入口、中间件和运行方式先定下来。

## 2. 服务边界

| 服务 | 端口 | 职责 |
|---|---:|---|
| low-altitude-gateway | 8080 | 统一入口、路由、JWT 鉴权过滤 |
| user-org-service | 8101 | 用户、角色、组织、审批员配置；Phase 01 只提供开发 token 与 bootstrap 接口 |
| resource-service | 8102 | Grid、Level、TimeSlot、RouteTemplate；Phase 01 只提供领域模型说明接口 |
| booking-service | 8103 | 预约申请、审批流转、占用写入；Phase 01 只提供状态与事务边界说明接口 |
| conflict-notify-service | 8104 | 冲突检测、通知、审计、补偿；Phase 01 只提供规则链与消息链路说明接口 |

## 3. 第一阶段已固定的工程约定

- 包名前缀：`com.lowaltitude`。
- 网关统一入口：`http://localhost:8080`。
- 服务注册发现：Nacos。
- 服务间调用：OpenFeign。
- 鉴权方式：Gateway 校验 Bearer Token，并向下游透传 `X-User-*` 请求头。
- 业务响应体：`ApiResponse<T>`。
- 业务异常：`BusinessException` + `ErrorCode`。
- 数据库：按服务拆分数据库，便于后续微服务边界清晰。

## 4. 低空领域主模型

项目后续围绕如下模型展开：

```text
Grid 区域网格
Level 高度层
TimeSlot 离散时间片
RouteTemplate 航线模板
```

核心资源占用单元：

```text
GridId + LevelId + TimeSlotId
```

后续冲突检测、预约审批、资源释放、可视化展示都围绕这个单元展开。

## 5. 后续演进顺序

```text
Phase 01 工程骨架
→ Phase 02 用户组织与低空资源基础数据
→ Phase 03 预约申请与审批业务闭环
→ Phase 04 冲突检测规则链
→ Phase 05 前端可视化
→ Phase 06 RabbitMQ + Outbox 最终一致性
→ Phase 07 治理、监控、压测
→ Phase 08 部署、论文材料、答辩演示
```
