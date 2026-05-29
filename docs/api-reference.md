# 核心接口文档

## 1. 通用说明

网关地址：

```text
http://127.0.0.1:8080
```

统一响应格式：

```json
{
  "success": true,
  "code": "OK",
  "message": "success",
  "data": {},
  "timestamp": "2026-05-29T00:00:00+08:00"
}
```

除 `/api/auth/dev-token` 和 actuator 相关接口外，其余业务接口建议携带：

```text
Authorization: Bearer <accessToken>
```

获取开发 token：

```bat
curl -X POST "http://127.0.0.1:8080/api/auth/dev-token?username=demo&role=ADMIN"
```

---

## 2. 认证接口

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/auth/dev-token?username=demo&role=ADMIN` | 获取开发调试 token |

---

## 3. 用户组织服务

### 3.1 组织

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/user-org/organizations` | 查询组织列表 |
| GET | `/api/user-org/organizations/{id}` | 查询组织详情 |
| POST | `/api/user-org/organizations` | 创建组织 |
| PUT | `/api/user-org/organizations/{id}` | 更新组织 |
| DELETE | `/api/user-org/organizations/{id}` | 删除组织 |

### 3.2 角色

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/user-org/roles` | 查询角色列表 |
| GET | `/api/user-org/roles/{id}` | 查询角色详情 |
| POST | `/api/user-org/roles` | 创建角色 |
| PUT | `/api/user-org/roles/{id}` | 更新角色 |
| DELETE | `/api/user-org/roles/{id}` | 删除角色 |

### 3.3 用户

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/user-org/users` | 查询用户列表 |
| GET | `/api/user-org/users/{id}` | 查询用户基本信息 |
| GET | `/api/user-org/users/{id}/detail` | 查询用户详情 |
| POST | `/api/user-org/users` | 创建用户 |
| PUT | `/api/user-org/users/{id}` | 更新用户 |
| DELETE | `/api/user-org/users/{id}` | 删除用户 |
| POST | `/api/user-org/users/{userId}/roles/{roleId}` | 分配角色 |
| DELETE | `/api/user-org/users/{userId}/roles/{roleId}` | 移除角色 |

### 3.4 审批配置

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/user-org/approver-configs` | 查询审批配置 |
| GET | `/api/user-org/approver-configs/{id}` | 查询配置详情 |
| POST | `/api/user-org/approver-configs` | 创建审批配置 |
| PUT | `/api/user-org/approver-configs/{id}` | 更新审批配置 |
| DELETE | `/api/user-org/approver-configs/{id}` | 删除审批配置 |

---

## 4. 低空资源服务

### 4.1 Grid 网格

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/resources/grids` | 查询网格列表，支持 `keyword`、`status` |
| GET | `/api/resources/grids/{id}` | 查询网格详情 |
| POST | `/api/resources/grids` | 创建网格 |
| PUT | `/api/resources/grids/{id}` | 更新网格 |
| DELETE | `/api/resources/grids/{id}` | 删除网格 |

### 4.2 高度层

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/resources/levels` | 查询高度层 |
| GET | `/api/resources/levels/{id}` | 查询高度层详情 |
| POST | `/api/resources/levels` | 创建高度层 |
| PUT | `/api/resources/levels/{id}` | 更新高度层 |
| DELETE | `/api/resources/levels/{id}` | 删除高度层 |

### 4.3 时间片

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/resources/time-slots` | 查询时间片 |
| GET | `/api/resources/time-slots/{id}` | 查询时间片详情 |
| POST | `/api/resources/time-slots` | 创建时间片 |
| PUT | `/api/resources/time-slots/{id}` | 更新时间片 |
| DELETE | `/api/resources/time-slots/{id}` | 删除时间片 |

### 4.4 航线模板

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/resources/route-templates` | 查询航线模板 |
| GET | `/api/resources/route-templates/{id}` | 查询航线模板详情 |
| POST | `/api/resources/route-templates` | 创建航线模板 |
| PUT | `/api/resources/route-templates/{id}` | 更新航线模板 |
| DELETE | `/api/resources/route-templates/{id}` | 删除航线模板 |
| POST | `/api/resources/route-templates/{routeTemplateId}/grids` | 添加航线网格点 |
| PUT | `/api/resources/route-templates/{routeTemplateId}/grids/{routeGridId}` | 更新航线网格点 |
| DELETE | `/api/resources/route-templates/{routeTemplateId}/grids/{routeGridId}` | 删除航线网格点 |

---

## 5. 预约审批服务

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/bookings` | 查询预约列表 |
| POST | `/api/bookings/pre-check` | 提交前冲突预检查 |
| GET | `/api/bookings/conflicts` | 查询全局冲突记录 |
| GET | `/api/bookings/{id}` | 查询预约详情 |
| POST | `/api/bookings` | 提交预约申请 |
| POST | `/api/bookings/{id}/approve` | 审批通过 |
| POST | `/api/bookings/{id}/reject` | 审批驳回 |
| POST | `/api/bookings/{id}/cancel` | 取消申请并释放资源 |
| GET | `/api/bookings/{id}/flows` | 查询审批流转 |
| GET | `/api/bookings/{id}/occupancies` | 查询资源占用 |
| GET | `/api/bookings/{id}/conflicts` | 查询该申请冲突记录 |

### 5.1 提交预约示例

```json
{
  "taskName": "园区光伏板巡检",
  "orgId": 1,
  "applicantUserId": 1,
  "applicantName": "demo",
  "routeTemplateId": 1,
  "levelId": 1,
  "bookingDate": "2026-06-01",
  "timeSlotIds": [1, 2],
  "applyReason": "例行巡检",
  "description": "demo booking"
}
```

### 5.2 审批通过示例

```json
{
  "operatorUserId": 1,
  "operatorName": "admin",
  "comment": "审批通过"
}
```

### 5.3 取消申请示例

```json
{
  "operatorUserId": 1,
  "operatorName": "admin",
  "cancelReason": "测试完成"
}
```

---

## 6. Outbox 消息接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/bookings/outbox` | 查询 outbox 消息 |
| GET | `/api/bookings/outbox/summary` | 查询 outbox 统计 |
| GET | `/api/bookings/outbox/{id}` | 查询 outbox 详情 |
| POST | `/api/bookings/outbox/dispatch` | 派发待发送消息 |
| POST | `/api/bookings/outbox/{id}/requeue` | 失败消息重新入队 |
| POST | `/api/bookings/outbox/{id}/republish` | 重复投递，用于幂等演示 |

---

## 7. 通知审计服务

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/notifications/summary` | 通知审计统计 |
| GET | `/api/notifications/records` | 查询通知记录 |
| GET | `/api/notifications/audits` | 查询审计日志 |
| GET | `/api/notifications/idempotent` | 查询幂等消费记录 |

---

## 8. 治理与监控接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/bookings/governance/summary` | 业务治理汇总 |
| GET | `/api/bookings/governance/rate-limited` | 限流演示 |
| GET | `/api/bookings/governance/circuit?fail=true` | 降级演示 |
| GET | `/api/bookings/governance/retry?key=demo&failTimes=2` | 重试演示 |
| GET | `/actuator/health` | 健康检查 |
| GET | `/actuator/prometheus` | Prometheus 指标 |

---

## 9. 常用 curl 示例

```bat
curl -X POST "http://127.0.0.1:8080/api/auth/dev-token?username=demo&role=ADMIN"

curl -H "Authorization: Bearer <token>" http://127.0.0.1:8080/api/resources/grids

curl -H "Authorization: Bearer <token>" http://127.0.0.1:8080/api/bookings/governance/summary

curl -H "Authorization: Bearer <token>" http://127.0.0.1:8080/api/notifications/summary
```
