# Phase 01 接口说明

## 1. 获取开发 Token

```http
POST /api/auth/dev-token?username=demo&role=APPLICANT
```

角色可选：

- `APPLICANT`
- `APPROVER`
- `ADMIN`

响应中 `data.accessToken` 用于后续接口。

## 2. 用户组织服务占位接口

```http
GET /api/users/bootstrap
Authorization: Bearer <token>
```

用于验证网关鉴权、下游请求头透传、用户组织服务路由。

## 3. 资源服务占位接口

```http
GET /api/resources/bootstrap
Authorization: Bearer <token>
```

用于验证资源服务路由，并返回 Grid / Level / TimeSlot / RouteTemplate 领域模型说明。

## 4. 申请审批服务占位接口

```http
GET /api/bookings/bootstrap
Authorization: Bearer <token>
```

用于验证申请审批服务路由，并返回后续状态机与事务边界说明。

## 5. 冲突检测与通知服务占位接口

```http
GET /api/conflicts/bootstrap
Authorization: Bearer <token>
```

用于验证冲突检测服务路由，并返回后续冲突规则与消息链路说明。

## 6. Actuator

每个服务都开放：

```http
GET /actuator/health
GET /actuator/metrics
```

网关额外开放：

```http
GET /actuator/gateway/routes
```
