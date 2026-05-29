# Phase 02 下一步开发计划草案

下一次开发建议主题：用户组织与低空资源基础数据。

## 1. 用户组织服务

目标表：

- `user_account`
- `role`
- `user_role`
- `organization`
- `approver_config`

目标接口：

- 用户注册 / 登录。
- 用户分页查询。
- 角色分配。
- 组织管理。
- 审批员配置。

## 2. 资源服务

目标表：

- `grid`
- `level`
- `time_slot`
- `route_template`
- `route_template_grid`

目标接口：

- Grid CRUD。
- Level CRUD。
- TimeSlot CRUD。
- RouteTemplate CRUD。
- 查询某个 Grid + Level 的时段资源状态。

## 3. 前端最低目标

- 登录页。
- 首页布局。
- 用户/角色管理页面。
- Grid/Level/TimeSlot 管理页面。

## 4. 保持暂不做

- 完整审批流。
- 复杂冲突规则。
- RabbitMQ 最终一致性。
- Prometheus/Grafana。
