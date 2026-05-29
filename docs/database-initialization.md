# 数据库初始化说明

## 1. 数据库列表

项目使用四个业务库：

| 数据库 | 服务 | 说明 |
|---|---|---|
| `low_altitude_user_org` | user-org-service | 用户、角色、组织、审批员配置 |
| `low_altitude_resource` | resource-service | Grid、Level、TimeSlot、航线模板 |
| `low_altitude_booking` | booking-service | 预约、审批流、占用、冲突、Outbox |
| `low_altitude_conflict_notify` | conflict-notify-service | 通知记录、审计日志、幂等消费记录 |

## 2. 初始化命令

```bat
mysql -uroot -p123123 --default-character-set=utf8mb4 < docker\mysql\local-root-init.sql
```

必须使用 `--default-character-set=utf8mb4`，否则 Windows CMD/PowerShell 导入中文数据时可能出现 `Incorrect string value`。

## 3. 核心表

### 3.1 用户组织库

| 表 | 说明 |
|---|---|
| `organization` | 组织信息 |
| `sys_role` | 角色信息 |
| `user_account` | 用户账户 |
| `user_role` | 用户角色关联 |
| `approver_config` | 审批员配置 |

### 3.2 低空资源库

| 表 | 说明 |
|---|---|
| `grid` | 区域网格 |
| `altitude_level` | 高度层 |
| `time_slot` | 时间片 |
| `route_template` | 航线模板 |
| `route_template_grid` | 航线经过网格序列 |

### 3.3 预约审批库

| 表 | 说明 |
|---|---|
| `booking_record` | 预约申请主表 |
| `booking_time_slot` | 预约申请时间片关联 |
| `booking_flow_record` | 审批流转记录 |
| `resource_occupancy` | 资源占用记录 |
| `conflict_record` | 冲突记录 |
| `outbox_message` | 本地消息表 |

### 3.4 通知审计库

| 表 | 说明 |
|---|---|
| `notify_record` | 通知记录 |
| `audit_log` | 审计日志 |
| `idempotent_record` | 幂等消费记录 |

## 4. 默认数据

初始化后包含：

```text
默认组织：ORG_DEFAULT
默认用户：admin
默认角色：APPLICANT / APPROVER / ADMIN
默认审批员：admin
默认 Grid：G-01-01、G-01-02、G-02-01、G-02-02
默认高度层：L1、L2、L3
默认时间片：AM-01、AM-02、PM-01、PM-02
默认航线：RT-DEMO-01、RT-NOFLY-01
```

其中：

```text
G-02-01 = RISK 风险网格
G-02-02 = NO_FLY 禁飞网格
RT-DEMO-01 = 经过 G-01-01 / G-01-02 / G-02-01
RT-NOFLY-01 = 经过 G-02-02
```

## 5. 常用检查命令

```bat
mysql -uroot -p123123 -e "SHOW DATABASES LIKE 'low_altitude_%';"
mysql -uroot -p123123 -e "USE low_altitude_user_org; SELECT * FROM sys_role;"
mysql -uroot -p123123 -e "USE low_altitude_resource; SELECT * FROM grid;"
mysql -uroot -p123123 -e "USE low_altitude_resource; SELECT * FROM route_template;"
mysql -uroot -p123123 -e "USE low_altitude_booking; SHOW TABLES;"
mysql -uroot -p123123 -e "USE low_altitude_conflict_notify; SHOW TABLES;"
```

## 6. 数据重置

如需清空重新开始：

```bat
mysql -uroot -p123123 -e "DROP DATABASE IF EXISTS low_altitude_user_org; DROP DATABASE IF EXISTS low_altitude_resource; DROP DATABASE IF EXISTS low_altitude_booking; DROP DATABASE IF EXISTS low_altitude_conflict_notify;"
mysql -uroot -p123123 --default-character-set=utf8mb4 < docker\mysql\local-root-init.sql
```
