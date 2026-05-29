-- Phase 03 schema: user organization, low-altitude resource and booking workflow data.
-- This script is idempotent and can be re-run safely.

CREATE DATABASE IF NOT EXISTS low_altitude_user_org DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS low_altitude_resource DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS low_altitude_booking DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS low_altitude_conflict_notify DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE low_altitude_user_org;

CREATE TABLE IF NOT EXISTS organization (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    org_code VARCHAR(64) NOT NULL,
    org_name VARCHAR(128) NOT NULL,
    parent_id BIGINT NULL,
    contact_name VARCHAR(64) NULL,
    contact_phone VARCHAR(32) NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    description VARCHAR(512) NULL,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_organization_code (org_code),
    KEY idx_organization_parent (parent_id),
    KEY idx_organization_enabled_deleted (enabled, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(64) NOT NULL,
    role_name VARCHAR(128) NOT NULL,
    description VARCHAR(512) NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_code (role_code),
    KEY idx_role_enabled_deleted (enabled, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    real_name VARCHAR(64) NOT NULL,
    phone VARCHAR(32) NULL,
    email VARCHAR(128) NULL,
    org_id BIGINT NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_username (username),
    KEY idx_user_org (org_id),
    KEY idx_user_enabled_deleted (enabled, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_user_role_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS approver_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    org_id BIGINT NOT NULL,
    approver_user_id BIGINT NOT NULL,
    level_order INT NOT NULL DEFAULT 1,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    description VARCHAR(512) NULL,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_approver_org (org_id),
    KEY idx_approver_user (approver_user_id),
    KEY idx_approver_enabled_deleted (enabled, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO sys_role(role_code, role_name, description, enabled)
VALUES
    ('APPLICANT', '普通申请人', '可提交低空巡检预约申请', 1),
    ('APPROVER', '审批员', '可处理预约审批与风险复核', 1),
    ('ADMIN', '管理员', '可维护用户、组织和基础资源', 1)
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name), description = VALUES(description), enabled = VALUES(enabled);

INSERT INTO organization(org_code, org_name, parent_id, contact_name, contact_phone, enabled, description)
VALUES ('ORG_DEFAULT', '默认低空运维组织', NULL, '系统管理员', '00000000000', 1, 'Phase 02 默认初始化组织')
ON DUPLICATE KEY UPDATE org_name = VALUES(org_name), enabled = VALUES(enabled), description = VALUES(description);

INSERT INTO user_account(username, password_hash, real_name, phone, email, org_id, enabled)
SELECT 'admin', 'DEV_NOT_ENCRYPTED_CHANGE_IN_PHASE_AUTH', '默认管理员', '00000000000', 'admin@example.com', o.id, 1
FROM organization o WHERE o.org_code = 'ORG_DEFAULT'
ON DUPLICATE KEY UPDATE real_name = VALUES(real_name), org_id = VALUES(org_id), enabled = VALUES(enabled);

INSERT IGNORE INTO user_role(user_id, role_id)
SELECT u.id, r.id FROM user_account u JOIN sys_role r ON r.role_code = 'ADMIN' WHERE u.username = 'admin';
INSERT IGNORE INTO user_role(user_id, role_id)
SELECT u.id, r.id FROM user_account u JOIN sys_role r ON r.role_code = 'APPROVER' WHERE u.username = 'admin';

INSERT INTO approver_config(org_id, approver_user_id, level_order, enabled, description)
SELECT o.id, u.id, 1, 1, '默认审批链路：默认组织的一审审批员'
FROM organization o JOIN user_account u ON u.username = 'admin'
WHERE o.org_code = 'ORG_DEFAULT'
  AND NOT EXISTS (SELECT 1 FROM approver_config ac WHERE ac.org_id = o.id AND ac.approver_user_id = u.id AND ac.deleted = 0);

USE low_altitude_resource;

CREATE TABLE IF NOT EXISTS grid (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    grid_code VARCHAR(64) NOT NULL,
    grid_name VARCHAR(128) NOT NULL,
    row_index INT NOT NULL,
    col_index INT NOT NULL,
    center_lon DECIMAL(11, 7) NULL,
    center_lat DECIMAL(10, 7) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    description VARCHAR(512) NULL,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_grid_code (grid_code),
    UNIQUE KEY uk_grid_position (row_index, col_index, deleted),
    KEY idx_grid_status_deleted (status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS altitude_level (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    level_code VARCHAR(64) NOT NULL,
    level_name VARCHAR(128) NOT NULL,
    min_altitude_m INT NOT NULL,
    max_altitude_m INT NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    description VARCHAR(512) NULL,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_altitude_level_code (level_code),
    KEY idx_altitude_enabled_deleted (enabled, deleted),
    KEY idx_altitude_range (min_altitude_m, max_altitude_m)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS time_slot (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    slot_code VARCHAR(64) NOT NULL,
    slot_name VARCHAR(128) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    description VARCHAR(512) NULL,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_time_slot_code (slot_code),
    KEY idx_time_slot_enabled_deleted (enabled, deleted),
    KEY idx_time_slot_range (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS route_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    route_code VARCHAR(64) NOT NULL,
    route_name VARCHAR(128) NOT NULL,
    description VARCHAR(512) NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_by VARCHAR(64) NULL,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_route_code (route_code),
    KEY idx_route_enabled_deleted (enabled, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS route_template_grid (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    route_template_id BIGINT NOT NULL,
    grid_id BIGINT NOT NULL,
    sequence_no INT NOT NULL,
    planned_duration_minutes INT NOT NULL DEFAULT 5,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_route_sequence (route_template_id, sequence_no),
    KEY idx_route_grid_grid (grid_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO grid(grid_code, grid_name, row_index, col_index, center_lon, center_lat, status, description)
VALUES
    ('G-01-01', 'A区-1行1列', 1, 1, 113.9340000, 22.5330000, 'ACTIVE', '默认示例网格'),
    ('G-01-02', 'A区-1行2列', 1, 2, 113.9350000, 22.5330000, 'ACTIVE', '默认示例网格'),
    ('G-02-01', 'A区-2行1列', 2, 1, 113.9340000, 22.5340000, 'RISK', '相邻风险示例网格'),
    ('G-02-02', 'A区-2行2列', 2, 2, 113.9350000, 22.5340000, 'NO_FLY', '禁飞区示例网格')
ON DUPLICATE KEY UPDATE grid_name = VALUES(grid_name), status = VALUES(status), description = VALUES(description);

INSERT INTO altitude_level(level_code, level_name, min_altitude_m, max_altitude_m, sort_order, enabled, description)
VALUES
    ('L1', '低空一层 0-60m', 0, 60, 1, 1, '适合低速巡检任务'),
    ('L2', '低空二层 60-120m', 60, 120, 2, 1, '适合常规巡检任务'),
    ('L3', '低空三层 120-200m', 120, 200, 3, 1, '适合较高航线任务')
ON DUPLICATE KEY UPDATE level_name = VALUES(level_name), min_altitude_m = VALUES(min_altitude_m), max_altitude_m = VALUES(max_altitude_m), enabled = VALUES(enabled);

INSERT INTO time_slot(slot_code, slot_name, start_time, end_time, sort_order, enabled, description)
VALUES
    ('AM-01', '上午 08:00-10:00', '08:00:00', '10:00:00', 1, 1, '默认时间片'),
    ('AM-02', '上午 10:00-12:00', '10:00:00', '12:00:00', 2, 1, '默认时间片'),
    ('PM-01', '下午 14:00-16:00', '14:00:00', '16:00:00', 3, 1, '默认时间片'),
    ('PM-02', '下午 16:00-18:00', '16:00:00', '18:00:00', 4, 1, '默认时间片')
ON DUPLICATE KEY UPDATE slot_name = VALUES(slot_name), start_time = VALUES(start_time), end_time = VALUES(end_time), enabled = VALUES(enabled);

INSERT INTO route_template(route_code, route_name, description, enabled, created_by)
VALUES ('RT-DEMO-01', '默认巡检航线模板', '经过 G-01-01、G-01-02、G-02-01 的示例巡检路线', 1, 'system')
ON DUPLICATE KEY UPDATE route_name = VALUES(route_name), description = VALUES(description), enabled = VALUES(enabled);

INSERT IGNORE INTO route_template_grid(route_template_id, grid_id, sequence_no, planned_duration_minutes)
SELECT rt.id, g.id, 1, 5 FROM route_template rt JOIN grid g ON g.grid_code = 'G-01-01' WHERE rt.route_code = 'RT-DEMO-01';
INSERT IGNORE INTO route_template_grid(route_template_id, grid_id, sequence_no, planned_duration_minutes)
SELECT rt.id, g.id, 2, 5 FROM route_template rt JOIN grid g ON g.grid_code = 'G-01-02' WHERE rt.route_code = 'RT-DEMO-01';
INSERT IGNORE INTO route_template_grid(route_template_id, grid_id, sequence_no, planned_duration_minutes)
SELECT rt.id, g.id, 3, 5 FROM route_template rt JOIN grid g ON g.grid_code = 'G-02-01' WHERE rt.route_code = 'RT-DEMO-01';


INSERT INTO route_template(route_code, route_name, description, enabled, created_by)
VALUES ('RT-NOFLY-01', '禁飞区测试航线模板', '经过 G-02-02 的禁飞区测试路线，用于 Phase 04 规则演示', 1, 'system')
ON DUPLICATE KEY UPDATE route_name = VALUES(route_name), description = VALUES(description), enabled = VALUES(enabled);

INSERT IGNORE INTO route_template_grid(route_template_id, grid_id, sequence_no, planned_duration_minutes)
SELECT rt.id, g.id, 1, 5 FROM route_template rt JOIN grid g ON g.grid_code = 'G-02-02' WHERE rt.route_code = 'RT-NOFLY-01';
USE low_altitude_booking;

CREATE TABLE IF NOT EXISTS booking_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_no VARCHAR(64) NOT NULL,
    task_name VARCHAR(128) NOT NULL,
    org_id BIGINT NOT NULL,
    applicant_user_id BIGINT NOT NULL,
    applicant_name VARCHAR(64) NOT NULL,
    route_template_id BIGINT NOT NULL,
    route_template_name VARCHAR(128) NULL,
    level_id BIGINT NOT NULL,
    booking_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    apply_reason VARCHAR(512) NULL,
    approval_comment VARCHAR(512) NULL,
    reject_reason VARCHAR(512) NULL,
    cancel_reason VARCHAR(512) NULL,
    description VARCHAR(512) NULL,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_booking_no (booking_no),
    KEY idx_booking_status_deleted (status, deleted),
    KEY idx_booking_applicant (applicant_user_id, deleted),
    KEY idx_booking_date (booking_date),
    KEY idx_booking_route_level (route_template_id, level_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS booking_time_slot (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    time_slot_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_booking_time_slot (booking_id, time_slot_id),
    KEY idx_booking_time_slot_slot (time_slot_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS booking_flow_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    action VARCHAR(32) NOT NULL,
    from_status VARCHAR(32) NULL,
    to_status VARCHAR(32) NOT NULL,
    operator_user_id BIGINT NULL,
    operator_name VARCHAR(64) NULL,
    comment VARCHAR(512) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_booking_flow_booking (booking_id),
    KEY idx_booking_flow_action (action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS resource_occupancy (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    booking_no VARCHAR(64) NOT NULL,
    route_template_id BIGINT NOT NULL,
    grid_id BIGINT NOT NULL,
    grid_code VARCHAR(64) NOT NULL,
    grid_name VARCHAR(128) NULL,
    level_id BIGINT NOT NULL,
    time_slot_id BIGINT NOT NULL,
    booking_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'OCCUPIED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    released_at DATETIME NULL,
    KEY idx_occupancy_booking (booking_id),
    KEY idx_occupancy_resource (booking_date, grid_id, level_id, time_slot_id, status),
    KEY idx_occupancy_grid_date (grid_id, booking_date),
    KEY idx_occupancy_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS conflict_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NULL,
    booking_no VARCHAR(64) NULL,
    conflict_type VARCHAR(32) NOT NULL,
    conflict_level VARCHAR(32) NOT NULL,
    rule_code VARCHAR(64) NOT NULL,
    grid_id BIGINT NULL,
    grid_code VARCHAR(64) NULL,
    grid_name VARCHAR(128) NULL,
    level_id BIGINT NULL,
    time_slot_id BIGINT NULL,
    booking_date DATE NULL,
    related_booking_id BIGINT NULL,
    related_booking_no VARCHAR(64) NULL,
    message VARCHAR(512) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at DATETIME NULL,
    KEY idx_conflict_booking (booking_id),
    KEY idx_conflict_type_status (conflict_type, status),
    KEY idx_conflict_resource (booking_date, grid_id, level_id, time_slot_id),
    KEY idx_conflict_related_booking (related_booking_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Phase 06: RabbitMQ + outbox eventual consistency tables.
USE low_altitude_booking;

CREATE TABLE IF NOT EXISTS outbox_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    message_key VARCHAR(128) NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    aggregate_type VARCHAR(64) NOT NULL,
    aggregate_id BIGINT NOT NULL,
    routing_key VARCHAR(128) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    max_retry_count INT NOT NULL DEFAULT 8,
    next_retry_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at DATETIME NULL,
    last_error VARCHAR(512) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_outbox_message_key (message_key),
    KEY idx_outbox_status_retry (status, next_retry_at, retry_count),
    KEY idx_outbox_aggregate (aggregate_type, aggregate_id),
    KEY idx_outbox_event_type (event_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

USE low_altitude_conflict_notify;

CREATE TABLE IF NOT EXISTS notify_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    message_key VARCHAR(128) NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    booking_id BIGINT NULL,
    booking_no VARCHAR(64) NULL,
    recipient_type VARCHAR(64) NOT NULL,
    recipient VARCHAR(255) NULL,
    channel VARCHAR(32) NOT NULL DEFAULT 'IN_APP',
    subject VARCHAR(255) NOT NULL,
    content VARCHAR(1024) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'SENT',
    sent_at DATETIME NULL,
    error_message VARCHAR(512) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_notify_message_key (message_key),
    KEY idx_notify_booking_no (booking_no),
    KEY idx_notify_event_status (event_type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    message_key VARCHAR(128) NULL,
    event_type VARCHAR(64) NULL,
    booking_id BIGINT NULL,
    booking_no VARCHAR(64) NULL,
    action VARCHAR(64) NOT NULL,
    detail VARCHAR(1024) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_audit_message_key (message_key),
    KEY idx_audit_booking_no (booking_no),
    KEY idx_audit_action (action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS idempotent_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    message_key VARCHAR(128) NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    consumer_name VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PROCESSED',
    processed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_idempotent_message_consumer (message_key, consumer_name),
    KEY idx_idempotent_event_type (event_type),
    KEY idx_idempotent_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Phase 10 demo data: richer 5x5 airspace, multiple route templates and sample occupancy records.
USE low_altitude_resource;

INSERT INTO grid(grid_code, grid_name, row_index, col_index, center_lon, center_lat, status, description)
VALUES
('G-01-01','北区-1行1列',1,1,113.9340000,22.5330000,'ACTIVE','北侧起飞与巡检入口'),
('G-01-02','北区-1行2列',1,2,113.9350000,22.5330000,'ACTIVE','北侧主巡检走廊'),
('G-01-03','北区-1行3列',1,3,113.9360000,22.5330000,'ACTIVE','光伏阵列北段'),
('G-01-04','北区-1行4列',1,4,113.9370000,22.5330000,'ACTIVE','仓库北侧通道'),
('G-01-05','北区-1行5列',1,5,113.9380000,22.5330000,'RISK','靠近建筑物的风险边界'),
('G-02-01','中北区-2行1列',2,1,113.9340000,22.5340000,'RISK','相邻风险示例网格'),
('G-02-02','中北区-2行2列',2,2,113.9350000,22.5340000,'NO_FLY','禁飞区示例网格'),
('G-02-03','中北区-2行3列',2,3,113.9360000,22.5340000,'ACTIVE','光伏阵列中心通道'),
('G-02-04','中北区-2行4列',2,4,113.9370000,22.5340000,'ACTIVE','仓库巡检走廊'),
('G-02-05','中北区-2行5列',2,5,113.9380000,22.5340000,'ACTIVE','园区东侧边界'),
('G-03-01','中心区-3行1列',3,1,113.9340000,22.5350000,'ACTIVE','西侧设备区'),
('G-03-02','中心区-3行2列',3,2,113.9350000,22.5350000,'ACTIVE','主干道低空走廊'),
('G-03-03','中心区-3行3列',3,3,113.9360000,22.5350000,'ACTIVE','调度中心上空'),
('G-03-04','中心区-3行4列',3,4,113.9370000,22.5350000,'RISK','人流密集风险区'),
('G-03-05','中心区-3行5列',3,5,113.9380000,22.5350000,'ACTIVE','东侧巡检点'),
('G-04-01','中南区-4行1列',4,1,113.9340000,22.5360000,'ACTIVE','西南角巡检点'),
('G-04-02','中南区-4行2列',4,2,113.9350000,22.5360000,'ACTIVE','库房西侧走廊'),
('G-04-03','中南区-4行3列',4,3,113.9360000,22.5360000,'ACTIVE','能源站巡检点'),
('G-04-04','中南区-4行4列',4,4,113.9370000,22.5360000,'NO_FLY','临时施工禁飞区'),
('G-04-05','中南区-4行5列',4,5,113.9380000,22.5360000,'ACTIVE','东南设备区'),
('G-05-01','南区-5行1列',5,1,113.9340000,22.5370000,'ACTIVE','南侧起降点'),
('G-05-02','南区-5行2列',5,2,113.9350000,22.5370000,'RISK','靠近高压设备风险区'),
('G-05-03','南区-5行3列',5,3,113.9360000,22.5370000,'ACTIVE','南侧主通道'),
('G-05-04','南区-5行4列',5,4,113.9370000,22.5370000,'ACTIVE','河道巡检点'),
('G-05-05','南区-5行5列',5,5,113.9380000,22.5370000,'ACTIVE','东南返航点')
ON DUPLICATE KEY UPDATE grid_name=VALUES(grid_name), center_lon=VALUES(center_lon), center_lat=VALUES(center_lat), status=VALUES(status), description=VALUES(description);

INSERT INTO time_slot(slot_code, slot_name, start_time, end_time, sort_order, enabled, description)
VALUES
('EV-01','傍晚 18:00-20:00','18:00:00','20:00:00',5,1,'扩展演示时间片'),
('NT-01','夜间 20:00-22:00','20:00:00','22:00:00',6,1,'夜间巡检演示时间片')
ON DUPLICATE KEY UPDATE slot_name=VALUES(slot_name), start_time=VALUES(start_time), end_time=VALUES(end_time), sort_order=VALUES(sort_order), enabled=VALUES(enabled), description=VALUES(description);

INSERT INTO route_template(route_code, route_name, description, enabled, created_by)
VALUES
('RT-SOLAR-01','光伏阵列巡检航线','从北侧入口穿越光伏阵列中心，经过风险网格前完成折返，用于展示风险但可提交场景',1,'seed'),
('RT-WAREHOUSE-02','仓储屋顶巡检航线','从南侧起降点斜穿库房与调度中心，适合多航线对比展示',1,'seed'),
('RT-RIVER-03','河道边界巡检航线','沿南侧河道和边界巡检，重点展示长航线走廊',1,'seed'),
('RT-RISK-BYPASS-04','风险绕行航线','绕开禁飞网格但经过风险边界，用于展示人工复核建议',1,'seed'),
('RT-PERIMETER-05','园区周界巡逻航线','覆盖园区外围多网格，适合展示多节点航线与时间片占用',1,'seed'),
('RT-NOFLY-02','施工禁飞测试航线','经过 G-04-04 施工禁飞区，用于展示 NO_FLY_GRID 阻断',1,'seed')
ON DUPLICATE KEY UPDATE route_name=VALUES(route_name), description=VALUES(description), enabled=VALUES(enabled);

-- Helper pattern: insert route grid points if not present.
INSERT IGNORE INTO route_template_grid(route_template_id, grid_id, sequence_no, planned_duration_minutes)
SELECT rt.id, g.id, 1, 4 FROM route_template rt JOIN grid g ON g.grid_code='G-01-01' WHERE rt.route_code='RT-SOLAR-01';
INSERT IGNORE INTO route_template_grid(route_template_id, grid_id, sequence_no, planned_duration_minutes)
SELECT rt.id, g.id, 2, 4 FROM route_template rt JOIN grid g ON g.grid_code='G-01-02' WHERE rt.route_code='RT-SOLAR-01';
INSERT IGNORE INTO route_template_grid(route_template_id, grid_id, sequence_no, planned_duration_minutes)
SELECT rt.id, g.id, 3, 5 FROM route_template rt JOIN grid g ON g.grid_code='G-01-03' WHERE rt.route_code='RT-SOLAR-01';
INSERT IGNORE INTO route_template_grid(route_template_id, grid_id, sequence_no, planned_duration_minutes)
SELECT rt.id, g.id, 4, 5 FROM route_template rt JOIN grid g ON g.grid_code='G-02-03' WHERE rt.route_code='RT-SOLAR-01';
INSERT IGNORE INTO route_template_grid(route_template_id, grid_id, sequence_no, planned_duration_minutes)
SELECT rt.id, g.id, 5, 5 FROM route_template rt JOIN grid g ON g.grid_code='G-03-03' WHERE rt.route_code='RT-SOLAR-01';

INSERT IGNORE INTO route_template_grid(route_template_id, grid_id, sequence_no, planned_duration_minutes)
SELECT rt.id, g.id, 1, 5 FROM route_template rt JOIN grid g ON g.grid_code='G-05-01' WHERE rt.route_code='RT-WAREHOUSE-02';
INSERT IGNORE INTO route_template_grid(route_template_id, grid_id, sequence_no, planned_duration_minutes)
SELECT rt.id, g.id, 2, 5 FROM route_template rt JOIN grid g ON g.grid_code='G-04-02' WHERE rt.route_code='RT-WAREHOUSE-02';
INSERT IGNORE INTO route_template_grid(route_template_id, grid_id, sequence_no, planned_duration_minutes)
SELECT rt.id, g.id, 3, 5 FROM route_template rt JOIN grid g ON g.grid_code='G-03-03' WHERE rt.route_code='RT-WAREHOUSE-02';
INSERT IGNORE INTO route_template_grid(route_template_id, grid_id, sequence_no, planned_duration_minutes)
SELECT rt.id, g.id, 4, 5 FROM route_template rt JOIN grid g ON g.grid_code='G-02-04' WHERE rt.route_code='RT-WAREHOUSE-02';
INSERT IGNORE INTO route_template_grid(route_template_id, grid_id, sequence_no, planned_duration_minutes)
SELECT rt.id, g.id, 5, 5 FROM route_template rt JOIN grid g ON g.grid_code='G-01-05' WHERE rt.route_code='RT-WAREHOUSE-02';

INSERT IGNORE INTO route_template_grid(route_template_id, grid_id, sequence_no, planned_duration_minutes)
SELECT rt.id, g.id, 1, 6 FROM route_template rt JOIN grid g ON g.grid_code='G-03-01' WHERE rt.route_code='RT-RIVER-03';
INSERT IGNORE INTO route_template_grid(route_template_id, grid_id, sequence_no, planned_duration_minutes)
SELECT rt.id, g.id, 2, 6 FROM route_template rt JOIN grid g ON g.grid_code='G-04-02' WHERE rt.route_code='RT-RIVER-03';
INSERT IGNORE INTO route_template_grid(route_template_id, grid_id, sequence_no, planned_duration_minutes)
SELECT rt.id, g.id, 3, 6 FROM route_template rt JOIN grid g ON g.grid_code='G-05-03' WHERE rt.route_code='RT-RIVER-03';
INSERT IGNORE INTO route_template_grid(route_template_id, grid_id, sequence_no, planned_duration_minutes)
SELECT rt.id, g.id, 4, 6 FROM route_template rt JOIN grid g ON g.grid_code='G-05-04' WHERE rt.route_code='RT-RIVER-03';
INSERT IGNORE INTO route_template_grid(route_template_id, grid_id, sequence_no, planned_duration_minutes)
SELECT rt.id, g.id, 5, 6 FROM route_template rt JOIN grid g ON g.grid_code='G-05-05' WHERE rt.route_code='RT-RIVER-03';

INSERT IGNORE INTO route_template_grid(route_template_id, grid_id, sequence_no, planned_duration_minutes)
SELECT rt.id, g.id, 1, 5 FROM route_template rt JOIN grid g ON g.grid_code='G-02-01' WHERE rt.route_code='RT-RISK-BYPASS-04';
INSERT IGNORE INTO route_template_grid(route_template_id, grid_id, sequence_no, planned_duration_minutes)
SELECT rt.id, g.id, 2, 5 FROM route_template rt JOIN grid g ON g.grid_code='G-03-02' WHERE rt.route_code='RT-RISK-BYPASS-04';
INSERT IGNORE INTO route_template_grid(route_template_id, grid_id, sequence_no, planned_duration_minutes)
SELECT rt.id, g.id, 3, 5 FROM route_template rt JOIN grid g ON g.grid_code='G-03-03' WHERE rt.route_code='RT-RISK-BYPASS-04';
INSERT IGNORE INTO route_template_grid(route_template_id, grid_id, sequence_no, planned_duration_minutes)
SELECT rt.id, g.id, 4, 5 FROM route_template rt JOIN grid g ON g.grid_code='G-03-04' WHERE rt.route_code='RT-RISK-BYPASS-04';
INSERT IGNORE INTO route_template_grid(route_template_id, grid_id, sequence_no, planned_duration_minutes)
SELECT rt.id, g.id, 5, 5 FROM route_template rt JOIN grid g ON g.grid_code='G-03-05' WHERE rt.route_code='RT-RISK-BYPASS-04';

INSERT IGNORE INTO route_template_grid(route_template_id, grid_id, sequence_no, planned_duration_minutes)
SELECT rt.id, g.id, 1, 4 FROM route_template rt JOIN grid g ON g.grid_code='G-01-01' WHERE rt.route_code='RT-PERIMETER-05';
INSERT IGNORE INTO route_template_grid(route_template_id, grid_id, sequence_no, planned_duration_minutes)
SELECT rt.id, g.id, 2, 4 FROM route_template rt JOIN grid g ON g.grid_code='G-01-05' WHERE rt.route_code='RT-PERIMETER-05';
INSERT IGNORE INTO route_template_grid(route_template_id, grid_id, sequence_no, planned_duration_minutes)
SELECT rt.id, g.id, 3, 4 FROM route_template rt JOIN grid g ON g.grid_code='G-03-05' WHERE rt.route_code='RT-PERIMETER-05';
INSERT IGNORE INTO route_template_grid(route_template_id, grid_id, sequence_no, planned_duration_minutes)
SELECT rt.id, g.id, 4, 4 FROM route_template rt JOIN grid g ON g.grid_code='G-05-05' WHERE rt.route_code='RT-PERIMETER-05';
INSERT IGNORE INTO route_template_grid(route_template_id, grid_id, sequence_no, planned_duration_minutes)
SELECT rt.id, g.id, 5, 4 FROM route_template rt JOIN grid g ON g.grid_code='G-05-01' WHERE rt.route_code='RT-PERIMETER-05';

INSERT IGNORE INTO route_template_grid(route_template_id, grid_id, sequence_no, planned_duration_minutes)
SELECT rt.id, g.id, 1, 5 FROM route_template rt JOIN grid g ON g.grid_code='G-04-04' WHERE rt.route_code='RT-NOFLY-02';

USE low_altitude_booking;

-- Several visual demo bookings used by the front-end situation page. They are idempotent by booking_no.
INSERT INTO booking_record(booking_no, task_name, org_id, applicant_user_id, applicant_name, route_template_id, route_template_name, level_id, booking_date, status, apply_reason, approval_comment, description)
SELECT 'DEMO-BK-SOLAR-L2-AM', '光伏阵列例行巡检', 1, 1, 'demo', rt.id, rt.route_name, 2, '2026-07-10', 'APPROVED', '可视化演示默认数据', 'seed approved', '用于多航线态势展示的已审批航线'
FROM low_altitude_resource.route_template rt WHERE rt.route_code='RT-SOLAR-01'
ON DUPLICATE KEY UPDATE status='APPROVED', route_template_id=VALUES(route_template_id), route_template_name=VALUES(route_template_name), level_id=VALUES(level_id), booking_date=VALUES(booking_date), description=VALUES(description);

INSERT INTO booking_record(booking_no, task_name, org_id, applicant_user_id, applicant_name, route_template_id, route_template_name, level_id, booking_date, status, apply_reason, approval_comment, description)
SELECT 'DEMO-BK-WAREHOUSE-L1-PM', '仓储屋顶巡检', 1, 1, 'demo', rt.id, rt.route_name, 1, '2026-07-10', 'APPROVED', '可视化演示默认数据', 'seed approved', '用于多航线对比的已审批航线'
FROM low_altitude_resource.route_template rt WHERE rt.route_code='RT-WAREHOUSE-02'
ON DUPLICATE KEY UPDATE status='APPROVED', route_template_id=VALUES(route_template_id), route_template_name=VALUES(route_template_name), level_id=VALUES(level_id), booking_date=VALUES(booking_date), description=VALUES(description);

INSERT INTO booking_record(booking_no, task_name, org_id, applicant_user_id, applicant_name, route_template_id, route_template_name, level_id, booking_date, status, apply_reason, approval_comment, description)
SELECT 'DEMO-BK-RIVER-L3-EV', '河道边界巡检', 1, 1, 'demo', rt.id, rt.route_name, 3, '2026-07-10', 'PENDING', '可视化演示默认数据', NULL, '用于审批工作台展示的待审批航线'
FROM low_altitude_resource.route_template rt WHERE rt.route_code='RT-RIVER-03'
ON DUPLICATE KEY UPDATE status='PENDING', route_template_id=VALUES(route_template_id), route_template_name=VALUES(route_template_name), level_id=VALUES(level_id), booking_date=VALUES(booking_date), description=VALUES(description);

INSERT IGNORE INTO booking_time_slot(booking_id, time_slot_id)
SELECT b.id, ts.id FROM booking_record b JOIN low_altitude_resource.time_slot ts ON ts.slot_code IN ('AM-01','AM-02') WHERE b.booking_no='DEMO-BK-SOLAR-L2-AM';
INSERT IGNORE INTO booking_time_slot(booking_id, time_slot_id)
SELECT b.id, ts.id FROM booking_record b JOIN low_altitude_resource.time_slot ts ON ts.slot_code IN ('PM-01') WHERE b.booking_no='DEMO-BK-WAREHOUSE-L1-PM';
INSERT IGNORE INTO booking_time_slot(booking_id, time_slot_id)
SELECT b.id, ts.id FROM booking_record b JOIN low_altitude_resource.time_slot ts ON ts.slot_code IN ('EV-01') WHERE b.booking_no='DEMO-BK-RIVER-L3-EV';

INSERT INTO booking_flow_record(booking_id, action, from_status, to_status, operator_user_id, operator_name, comment)
SELECT b.id, 'SUBMIT', NULL, b.status, 1, 'seed', 'visual demo booking seeded'
FROM booking_record b
WHERE b.booking_no IN ('DEMO-BK-SOLAR-L2-AM','DEMO-BK-WAREHOUSE-L1-PM','DEMO-BK-RIVER-L3-EV')
  AND NOT EXISTS (SELECT 1 FROM booking_flow_record f WHERE f.booking_id=b.id AND f.action='SUBMIT');

-- Occupancies for approved visual demo bookings.
INSERT INTO resource_occupancy(booking_id, booking_no, route_template_id, grid_id, grid_code, grid_name, level_id, time_slot_id, booking_date, status)
SELECT b.id, b.booking_no, b.route_template_id, g.id, g.grid_code, g.grid_name, b.level_id, ts.id, b.booking_date, 'OCCUPIED'
FROM booking_record b
JOIN low_altitude_resource.route_template rt ON rt.id=b.route_template_id
JOIN low_altitude_resource.route_template_grid rg ON rg.route_template_id=rt.id
JOIN low_altitude_resource.grid g ON g.id=rg.grid_id
JOIN booking_time_slot bts ON bts.booking_id=b.id
JOIN low_altitude_resource.time_slot ts ON ts.id=bts.time_slot_id
WHERE b.booking_no IN ('DEMO-BK-SOLAR-L2-AM','DEMO-BK-WAREHOUSE-L1-PM')
  AND NOT EXISTS (
    SELECT 1 FROM resource_occupancy ro
    WHERE ro.booking_no=b.booking_no AND ro.grid_id=g.id AND ro.level_id=b.level_id AND ro.time_slot_id=ts.id AND ro.booking_date=b.booking_date
  );
