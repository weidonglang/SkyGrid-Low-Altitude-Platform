# Phase 04 下一阶段计划：冲突检测与低空规则

Phase 04 的目标是把 Phase 03 的“预约审批闭环”升级为真正具有领域规则的“低空时空资源冲突检测系统”。

## 1. 核心目标

在审批通过前，对申请将要占用的 Grid + Level + TimeSlot 进行规则检测，输出：

```text
无冲突
风险冲突
硬冲突
禁飞拦截
```

## 2. 必做规则

| 规则 | 说明 | 处理 |
|---|---|---|
| 硬冲突 | 同一日期、同一 Grid、同一 Level、同一 TimeSlot 已被占用 | 拦截审批通过 |
| 禁飞区 | Grid 状态为 NO_FLY | 拦截提交或审批 |
| 风险 Grid | Grid 状态为 RISK | 进入风险复核或提示 |
| 相邻 Grid 风险 | 与已占用 Grid 相邻 | 风险提示 |
| 高度层安全间隔 | 高度层相邻或间隔不足 | 风险提示 |

## 3. 建议新增接口

```text
POST /api/bookings/pre-check
GET  /api/bookings/{id}/conflicts
POST /api/bookings/{id}/risk-approve
```

## 4. 建议新增表

```text
conflict_record
no_fly_rule
risk_rule_config
```

## 5. 答辩价值

Phase 04 完成后，系统才能真正区别于普通预约系统。它会形成清晰主线：

```text
低空资源离散化 → 预约申请 → 规则冲突检测 → 审批决策 → 资源占用
```
