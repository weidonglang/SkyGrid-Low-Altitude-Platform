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
