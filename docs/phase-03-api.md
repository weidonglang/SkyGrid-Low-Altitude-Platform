# Phase 03 API 说明

## 1. 提交预约申请

`POST /api/bookings`

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
  "applyReason": "例行低空巡检",
  "description": "Phase 03 demo"
}
```

返回的 `data.id` 是后续审批、查询和取消使用的预约 ID。

## 2. 查询预约列表

`GET /api/bookings`

可选查询参数：

```text
status=PENDING / APPROVED / REJECTED / CANCELLED
applicantUserId=1
bookingDate=2026-06-01
keyword=BK2026
```

## 3. 查询预约详情

`GET /api/bookings/{id}`

详情包含：

```text
booking_record 主信息
timeSlotIds
flows
occupancies
```

## 4. 审批通过

`POST /api/bookings/{id}/approve`

```json
{
  "operatorUserId": 1,
  "operatorName": "admin",
  "comment": "符合当前巡检安排，审批通过"
}
```

审批通过后会生成 `resource_occupancy`。

## 5. 驳回申请

`POST /api/bookings/{id}/reject`

```json
{
  "operatorUserId": 1,
  "operatorName": "admin",
  "rejectReason": "该时段不适合执行巡检"
}
```

## 6. 取消申请

`POST /api/bookings/{id}/cancel`

```json
{
  "operatorUserId": 1,
  "operatorName": "admin",
  "cancelReason": "任务计划调整"
}
```

若申请已经审批通过，取消时会将占用状态从 `OCCUPIED` 更新为 `RELEASED`。

## 7. 查询流转记录

`GET /api/bookings/{id}/flows`

## 8. 查询资源占用

`GET /api/bookings/{id}/occupancies`
