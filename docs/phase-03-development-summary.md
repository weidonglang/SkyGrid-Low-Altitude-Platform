# Phase 03 开发总结：预约申请与审批业务闭环

## 1. 阶段目标

Phase 03 的目标是把系统从“基础数据管理平台”推进到“低空资源预约审批平台”。本阶段不做复杂冲突检测，先完成可运行、可验证的主流程：

1. 用户提交低空巡检预约申请；
2. 审批员审批通过或驳回；
3. 审批通过后根据航线模板生成资源占用记录；
4. 已通过的申请取消后释放占用；
5. 全流程保留状态流转记录。

## 2. 新增数据库表

数据库：`low_altitude_booking`

| 表名 | 作用 |
|---|---|
| `booking_record` | 预约申请主表，记录任务、申请人、航线、高度层、日期和状态 |
| `booking_time_slot` | 预约申请与时间片的多对多关系 |
| `booking_flow_record` | 申请提交、审批、驳回、取消等状态流转记录 |
| `resource_occupancy` | 审批通过后生成的 Grid + Level + TimeSlot 资源占用记录 |

## 3. 新增核心接口

| 方法 | 路径 | 说明 |
|---|---|---|
| `POST` | `/api/bookings` | 提交预约申请 |
| `GET` | `/api/bookings` | 查询预约列表 |
| `GET` | `/api/bookings/{id}` | 查询预约详情 |
| `POST` | `/api/bookings/{id}/approve` | 审批通过 |
| `POST` | `/api/bookings/{id}/reject` | 驳回申请 |
| `POST` | `/api/bookings/{id}/cancel` | 取消申请并释放占用 |
| `GET` | `/api/bookings/{id}/flows` | 查询审批流转记录 |
| `GET` | `/api/bookings/{id}/occupancies` | 查询资源占用记录 |

## 4. 当前状态机

Phase 03 暂时只实现最小稳定状态机：

```text
PENDING  → APPROVED
PENDING  → REJECTED
PENDING  → CANCELLED
APPROVED → CANCELLED
```

其中：

- `PENDING`：申请已提交，等待审批；
- `APPROVED`：审批通过，并已生成资源占用；
- `REJECTED`：审批驳回，不生成资源占用；
- `CANCELLED`：申请取消；若原状态为 `APPROVED`，则释放对应资源占用。

## 5. 审批通过时的资源占用生成规则

审批通过时，系统会调用 `resource-service` 查询航线模板详情。

若航线模板包含 3 个 Grid，用户选择 2 个 TimeSlot，则系统生成：

```text
3 × 2 = 6 条 resource_occupancy
```

每条占用记录包含：

```text
booking_id
booking_no
route_template_id
grid_id
grid_code
grid_name
level_id
time_slot_id
booking_date
status = OCCUPIED
```

## 6. 本阶段边界

Phase 03 暂不拦截资源冲突。也就是说，即使同一 Grid + Level + TimeSlot 已被占用，当前版本仍允许审批通过。

这个限制是有意保留的，因为 Phase 04 将专门实现：

- 硬冲突检测；
- 风险冲突检测；
- 禁飞区 / 禁飞时段规则；
- 相邻 Grid 风险；
- 高度层安全间隔规则。

## 7. 验证方式

执行：

```bat
mysql -uroot -p123123 --default-character-set=utf8mb4 < docker\mysql\local-root-init.sql
```

然后启动 5 个服务：

```text
user-org-service
resource-service
booking-service
conflict-notify-service
low-altitude-gateway
```

最后运行：

```bat
scripts\phase03-smoke-test.bat
```

脚本会自动完成：

```text
获取 token
提交预约申请
查询预约详情
审批通过
查询资源占用
取消申请
释放占用
```
