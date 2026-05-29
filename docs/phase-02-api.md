# Phase 02 API 速查

所有接口都建议通过 Gateway `http://127.0.0.1:8080` 访问。除 `/api/auth/**` 外，接口需要请求头：

```http
Authorization: Bearer <token>
```

先获取开发 token：

```bat
curl -X POST "http://127.0.0.1:8080/api/auth/dev-token?username=demo&role=ADMIN"
```

## 1. 用户组织服务

### 组织

```http
GET    /api/user-org/organizations?keyword=默认
GET    /api/user-org/organizations/{id}
POST   /api/user-org/organizations
PUT    /api/user-org/organizations/{id}
DELETE /api/user-org/organizations/{id}
```

创建组织示例：

```json
{
  "orgCode": "ORG_TEST",
  "orgName": "测试巡检组织",
  "parentId": null,
  "contactName": "张三",
  "contactPhone": "13800000000",
  "enabled": true,
  "description": "用于 Phase 02 联调"
}
```

### 角色

```http
GET    /api/user-org/roles
GET    /api/user-org/roles/{id}
POST   /api/user-org/roles
PUT    /api/user-org/roles/{id}
DELETE /api/user-org/roles/{id}
```

### 用户

```http
GET    /api/user-org/users?keyword=admin
GET    /api/user-org/users/{id}
GET    /api/user-org/users/{id}/detail
POST   /api/user-org/users
PUT    /api/user-org/users/{id}
DELETE /api/user-org/users/{id}
POST   /api/user-org/users/{userId}/roles/{roleId}
DELETE /api/user-org/users/{userId}/roles/{roleId}
```

创建用户示例：

```json
{
  "username": "pilot01",
  "passwordHash": "DEV_NOT_ENCRYPTED",
  "realName": "巡检员01",
  "phone": "13900000001",
  "email": "pilot01@example.com",
  "orgId": 1,
  "enabled": true
}
```

### 审批员配置

```http
GET    /api/user-org/approver-configs?orgId=1
GET    /api/user-org/approver-configs/{id}
POST   /api/user-org/approver-configs
PUT    /api/user-org/approver-configs/{id}
DELETE /api/user-org/approver-configs/{id}
```

创建审批配置示例：

```json
{
  "orgId": 1,
  "approverUserId": 1,
  "levelOrder": 1,
  "enabled": true,
  "description": "默认一审审批员"
}
```

## 2. 低空资源服务

### Grid 网格

```http
GET    /api/resources/grids?keyword=G-01&status=ACTIVE
GET    /api/resources/grids/{id}
POST   /api/resources/grids
PUT    /api/resources/grids/{id}
DELETE /api/resources/grids/{id}
```

创建 Grid 示例：

```json
{
  "gridCode": "G-03-01",
  "gridName": "A区-3行1列",
  "rowIndex": 3,
  "colIndex": 1,
  "centerLon": 113.9360000,
  "centerLat": 22.5350000,
  "status": "ACTIVE",
  "description": "测试新增网格"
}
```

允许的 Grid 状态：

- `ACTIVE`
- `RISK`
- `NO_FLY`
- `DISABLED`

### 高度层

```http
GET    /api/resources/levels?enabled=true
GET    /api/resources/levels/{id}
POST   /api/resources/levels
PUT    /api/resources/levels/{id}
DELETE /api/resources/levels/{id}
```

创建高度层示例：

```json
{
  "levelCode": "L4",
  "levelName": "低空四层 200-300m",
  "minAltitudeM": 200,
  "maxAltitudeM": 300,
  "sortOrder": 4,
  "enabled": true,
  "description": "测试高度层"
}
```

### 时间片

```http
GET    /api/resources/time-slots?enabled=true
GET    /api/resources/time-slots/{id}
POST   /api/resources/time-slots
PUT    /api/resources/time-slots/{id}
DELETE /api/resources/time-slots/{id}
```

创建时间片示例：

```json
{
  "slotCode": "EV-01",
  "slotName": "傍晚 18:00-20:00",
  "startTime": "18:00:00",
  "endTime": "20:00:00",
  "sortOrder": 5,
  "enabled": true,
  "description": "测试时间片"
}
```

### 航线模板

```http
GET    /api/resources/route-templates?enabled=true
GET    /api/resources/route-templates/{id}
POST   /api/resources/route-templates
PUT    /api/resources/route-templates/{id}
DELETE /api/resources/route-templates/{id}
POST   /api/resources/route-templates/{routeTemplateId}/grids
PUT    /api/resources/route-templates/{routeTemplateId}/grids/{routeGridId}
DELETE /api/resources/route-templates/{routeTemplateId}/grids/{routeGridId}
```

创建航线模板示例：

```json
{
  "routeCode": "RT-TEST-01",
  "routeName": "测试巡检航线",
  "description": "用于验证多网格航线模板",
  "enabled": true,
  "createdBy": "demo"
}
```

向航线模板追加网格示例：

```json
{
  "gridId": 1,
  "sequenceNo": 1,
  "plannedDurationMinutes": 5
}
```
