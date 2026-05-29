# Phase 03 下一阶段计划：预约申请与审批主流程

Phase 03 的目标是让系统从“基础数据可维护”进入“业务闭环可运行”。

## 必做内容

1. 预约申请表：`booking_record`
2. 审批流转表：`booking_flow_record`
3. 资源占用表：`resource_occupancy`
4. 申请状态机：`DRAFT / PENDING / APPROVED / REJECTED / CANCELLED`
5. 提交申请接口：选择 Grid / Level / TimeSlot / RouteTemplate
6. 审批通过接口：写入占用记录
7. 取消申请接口：释放占用记录
8. 与 resource-service 的 Feign 调用：校验 Grid、Level、TimeSlot、RouteTemplate 是否存在

## 暂缓内容

- 完整冲突检测规则：Phase 04 做
- RabbitMQ Outbox：Phase 06 做
- 前端可视化：Phase 05 做

## Phase 03 验收标准

- 用户能提交预约申请。
- 审批员能通过或驳回。
- 审批通过后能生成 `resource_occupancy`。
- 取消后能释放占用。
- 每次状态变化都能写入 `booking_flow_record`。
