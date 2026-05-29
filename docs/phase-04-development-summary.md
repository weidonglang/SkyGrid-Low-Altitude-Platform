# Phase 04 开发总结：冲突检测与低空规则

## 1. 阶段目标

Phase 04 在 Phase 03 的“预约申请—审批—资源占用—取消释放”闭环基础上，引入低空资源冲突检测规则，使系统从普通预约审批流程升级为具备低空时空资源冲突消解能力的平台。

本阶段不做连续三维仿真，而继续沿用 Grid + Level + TimeSlot 的离散时空切片模型。

## 2. 已实现能力

### 2.1 预检查接口

新增接口：

```http
POST /api/bookings/pre-check
```

输入与预约申请一致，系统会在不落库申请的情况下检查：

- 是否经过禁飞网格；
- 是否经过风险网格；
- 是否与已有 OCCUPIED 资源形成同 Grid + 同 Level + 同 TimeSlot 的硬冲突；
- 是否与已有占用形成相邻 Grid 风险；
- 是否在同 Grid + 同 TimeSlot 下与相邻高度层形成安全间隔风险。

### 2.2 硬冲突检测

规则代码：`SAME_GRID_LEVEL_TIME`

当两个申请同时满足：

- booking_date 相同；
- grid_id 相同；
- level_id 相同；
- time_slot_id 相同；
- 已有占用状态为 OCCUPIED；

则判定为硬冲突，禁止提交或审批。

### 2.3 禁飞区拦截

规则代码：`NO_FLY_GRID`

当航线模板经过的 Grid 状态为 `NO_FLY` 时，系统直接产生阻断冲突，不允许提交或审批。

### 2.4 风险网格提示

规则代码：`RISK_GRID`

当航线模板经过的 Grid 状态为 `RISK` 时，系统不直接阻断申请，而是生成风险提示。审批员可以在人工复核后继续审批。

### 2.5 相邻网格风险

规则代码：`ADJACENT_GRID_OCCUPIED`

如果申请网格与已有占用网格在二维网格上相邻，并且日期、高度层、时间片相同，则生成风险提示。

### 2.6 高度层安全间隔风险

规则代码：`ALTITUDE_SAFETY_INTERVAL`

如果同一 Grid、同一日期、同一时间片下已有其他高度层占用，且高度区间距离小于 20m，则生成风险提示。

### 2.7 冲突记录表

新增表：

```sql
conflict_record
```

用于记录绑定到预约申请的冲突与风险结果。字段包括：

- booking_id / booking_no；
- conflict_type；
- conflict_level；
- rule_code；
- grid_id / grid_code / grid_name；
- level_id；
- time_slot_id；
- booking_date；
- related_booking_id / related_booking_no；
- message；
- status。

### 2.8 审批前二次校验

审批通过前会重新执行冲突检测，防止申请提交后到审批前资源状态发生变化。

如果发现硬冲突或禁飞区，审批失败。

如果只有风险冲突，则允许审批通过，但会记录风险提示。

## 3. 新增接口

```http
POST /api/bookings/pre-check
GET  /api/bookings/conflicts
GET  /api/bookings/{id}/conflicts
```

## 4. 验证脚本

新增：

```bat
scripts\phase04-smoke-test.bat
```

该脚本会验证：

1. Gateway 健康状态；
2. 获取 ADMIN token；
3. 默认路线预检查产生风险提示但允许提交；
4. 提交并审批第一条申请；
5. 审批后生成 6 条资源占用；
6. 第二条相同申请预检查产生硬冲突；
7. 第二条相同申请提交失败；
8. 禁飞区路线预检查失败；
9. 取消第一条申请并释放资源；
10. 查询冲突记录。

## 5. 答辩展示价值

Phase 04 完成后，系统可以演示：

- 正常预约如何生成资源占用；
- 重复预约如何被硬冲突拦截；
- 禁飞区如何直接拦截；
- 风险区域如何进入人工复核；
- 冲突结果如何绑定真实申请与真实资源。

这使项目不再只是普通审批系统，而开始具有低空资源调度平台的领域特征。
