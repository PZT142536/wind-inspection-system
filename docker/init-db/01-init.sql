-- 创建数据库扩展
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "postgis";

-- 创建枚举类型
CREATE TYPE user_source AS ENUM ('LDAP', 'LOCAL');
CREATE TYPE task_type AS ENUM ('ARRIVAL', 'INSTALL', 'ACCEPTANCE');
CREATE TYPE task_status AS ENUM ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED', 'CANCELLED');
CREATE TYPE media_type AS ENUM ('VIDEO', 'PHOTO');
CREATE TYPE finding_status AS ENUM ('PENDING', 'CONFIRMED', 'REJECTED');
CREATE TYPE alert_type AS ENUM ('SAFETY_HELMET', 'SLING_POSITION', 'SYSTEM');

-- ============================================================
-- 用户与权限表
-- ============================================================

-- 用户表
CREATE TABLE sys_user (
    id BIGSERIAL PRIMARY KEY,
    emp_no VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(64) NOT NULL,
    dept VARCHAR(128),
    source user_source NOT NULL DEFAULT 'LOCAL',
    password_hash VARCHAR(128),
    salt VARCHAR(64),
    status SMALLINT NOT NULL DEFAULT 1,  -- 1:启用 0:禁用
    locked_until TIMESTAMP,
    last_login_time TIMESTAMP,
    last_login_ip VARCHAR(64),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_emp_no ON sys_user(emp_no);
CREATE INDEX idx_user_name ON sys_user(name);

-- 角色表
CREATE TABLE sys_role (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(64) NOT NULL,
    description VARCHAR(256),
    created_by BIGINT REFERENCES sys_user(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 权限表
CREATE TABLE sys_permission (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(128) NOT NULL UNIQUE,
    name VARCHAR(64) NOT NULL,
    type VARCHAR(16) NOT NULL,  -- MENU, BUTTON, DATA
    parent_id BIGINT REFERENCES sys_permission(id),
    path VARCHAR(256),
    icon VARCHAR(64),
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 用户角色关联表
CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL REFERENCES sys_user(id),
    role_id BIGINT NOT NULL REFERENCES sys_role(id),
    PRIMARY KEY (user_id, role_id)
);

-- 角色权限关联表
CREATE TABLE sys_role_permission (
    role_id BIGINT NOT NULL REFERENCES sys_role(id),
    permission_id BIGINT NOT NULL REFERENCES sys_permission(id),
    PRIMARY KEY (role_id, permission_id)
);

-- ============================================================
-- 日志表
-- ============================================================

-- 登录日志表
CREATE TABLE sys_login_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES sys_user(id),
    emp_no VARCHAR(32) NOT NULL,
    name VARCHAR(64),
    ip VARCHAR(64),
    source VARCHAR(16),  -- WEB, APP
    user_agent VARCHAR(512),
    login_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status SMALLINT NOT NULL,  -- 1:成功 0:失败
    fail_reason VARCHAR(256)
);

CREATE INDEX idx_login_log_user_id ON sys_login_log(user_id);
CREATE INDEX idx_login_log_time ON sys_login_log(login_time);

-- 操作日志表
CREATE TABLE sys_operation_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES sys_user(id),
    emp_no VARCHAR(32),
    name VARCHAR(64),
    module VARCHAR(64) NOT NULL,
    action VARCHAR(128) NOT NULL,
    detail TEXT,
    ip VARCHAR(64),
    time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    result VARCHAR(16) NOT NULL,  -- SUCCESS, FAIL
    error_msg TEXT
);

CREATE INDEX idx_operation_log_user_id ON sys_operation_log(user_id);
CREATE INDEX idx_operation_log_time ON sys_operation_log(time);
CREATE INDEX idx_operation_log_module ON sys_operation_log(module);

-- ============================================================
-- 业务核心表
-- ============================================================

-- 项目表
CREATE TABLE biz_project (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    code VARCHAR(64) UNIQUE,
    owner VARCHAR(128),  -- 业主
    location VARCHAR(256),  -- 项目位置
    lat DOUBLE PRECISION,
    lng DOUBLE PRECISION,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE, COMPLETED, ARCHIVED
    manager_id BIGINT REFERENCES sys_user(id),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_project_name ON biz_project(name);
CREATE INDEX idx_project_status ON biz_project(status);

-- 机位表
CREATE TABLE biz_turbine (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES biz_project(id),
    code VARCHAR(64) NOT NULL,  -- 机位编号
    model VARCHAR(128),  -- 机型
    lat DOUBLE PRECISION NOT NULL,
    lng DOUBLE PRECISION NOT NULL,
    altitude DOUBLE PRECISION,  -- 海拔高度
    hub_height DOUBLE PRECISION,  -- 轮毂高度
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',  -- PENDING, INSTALLED, COMMISSIONED
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(project_id, code)
);

CREATE INDEX idx_turbine_project_id ON biz_turbine(project_id);
CREATE INDEX idx_turbine_status ON biz_turbine(status);

-- 巡检任务表
CREATE TABLE biz_inspection_task (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES biz_project(id),
    turbine_id BIGINT REFERENCES biz_turbine(id),
    type task_type NOT NULL,
    component VARCHAR(64),  -- 部套: BLADE, HUB, NACELLE, TOWER, BOX_TRANSFORMER
    status task_status NOT NULL DEFAULT 'PENDING',
    inspector_id BIGINT REFERENCES sys_user(id),
    planned_at TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    progress SMALLINT DEFAULT 0,  -- 进度百分比
    remark TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_task_project_id ON biz_inspection_task(project_id);
CREATE INDEX idx_task_turbine_id ON biz_inspection_task(turbine_id);
CREATE INDEX idx_task_status ON biz_inspection_task(status);
CREATE INDEX idx_task_type ON biz_inspection_task(type);
CREATE INDEX idx_task_inspector_id ON biz_inspection_task(inspector_id);

-- 飞行航线表
CREATE TABLE biz_flight_route (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES biz_inspection_task(id),
    name VARCHAR(128) NOT NULL,
    surface VARCHAR(64),  -- 检查面: 前缘、后缘、主梁面
    altitude DOUBLE PRECISION,  -- 飞行高度
    speed DOUBLE PRECISION,  -- 飞行速度
    config_json JSONB,  -- 航线配置
    waypoints_json JSONB NOT NULL,  -- 航点数据
    dji_mission_json JSONB,  -- DJI任务格式
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_route_task_id ON biz_flight_route(task_id);

-- 上传任务表
CREATE TABLE biz_upload_task (
    id BIGSERIAL PRIMARY KEY,
    file_md5 VARCHAR(64) NOT NULL,
    file_name VARCHAR(256) NOT NULL,
    file_size BIGINT NOT NULL,
    total_chunks INT NOT NULL,
    uploaded_chunks INT NOT NULL DEFAULT 0,
    task_id BIGINT,
    file_type VARCHAR(32),
    status VARCHAR(16) NOT NULL DEFAULT 'UPLOADING',  -- UPLOADING, MERGING, COMPLETED, FAILED
    file_path VARCHAR(512),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_upload_task_md5 ON biz_upload_task(file_md5);
CREATE INDEX idx_upload_task_status ON biz_upload_task(status);

-- 上传分片表
CREATE TABLE biz_upload_chunk (
    id BIGSERIAL PRIMARY KEY,
    file_md5 VARCHAR(64) NOT NULL,
    chunk_number INT NOT NULL,
    chunk_size BIGINT,
    chunk_path VARCHAR(512),
    status VARCHAR(16) NOT NULL DEFAULT 'UPLOADED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_chunk_md5 ON biz_upload_chunk(file_md5);
CREATE UNIQUE INDEX idx_chunk_md5_number ON biz_upload_chunk(file_md5, chunk_number);

-- 媒体文件表
CREATE TABLE biz_media_file (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES biz_inspection_task(id),
    type media_type NOT NULL,
    name VARCHAR(256) NOT NULL,
    original_name VARCHAR(256),
    path VARCHAR(512) NOT NULL,  -- MinIO路径
    bucket VARCHAR(128),
    size_bytes BIGINT,
    surface VARCHAR(64),  -- 检查面
    key_point VARCHAR(128),  -- 关键点
    duration_sec INT,  -- 视频时长(秒)
    checksum VARCHAR(128),  -- MD5或SHA-256
    thumbnail_path VARCHAR(512),  -- 缩略图路径
    uploaded_by BIGINT REFERENCES sys_user(id),
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_media_task_id ON biz_media_file(task_id);
CREATE INDEX idx_media_type ON biz_media_file(type);

-- AI发现表
CREATE TABLE biz_ai_finding (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES biz_inspection_task(id),
    media_id BIGINT REFERENCES biz_media_file(id),
    type VARCHAR(64) NOT NULL,  -- SAFETY_HELMET, SLING_POSITION
    severity VARCHAR(16) NOT NULL,  -- HIGH, MEDIUM, LOW
    confidence DOUBLE PRECISION,  -- 置信度
    frame_time_sec DOUBLE PRECISION,  -- 帧时间点
    frame_image_path VARCHAR(512),  -- 帧图片路径
    bbox_json JSONB,  -- 检测框坐标
    description TEXT,
    status finding_status NOT NULL DEFAULT 'PENDING',
    reviewer_id BIGINT REFERENCES sys_user(id),
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_finding_task_id ON biz_ai_finding(task_id);
CREATE INDEX idx_finding_media_id ON biz_ai_finding(media_id);
CREATE INDEX idx_finding_status ON biz_ai_finding(status);
CREATE INDEX idx_finding_type ON biz_ai_finding(type);

-- 巡检报告表
CREATE TABLE biz_inspection_report (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES biz_inspection_task(id),
    title VARCHAR(256) NOT NULL,
    content_json JSONB,  -- 报告内容
    pdf_path VARCHAR(512),  -- PDF文件路径
    total_findings INT DEFAULT 0,
    high_severity_count INT DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',  -- DRAFT, FINAL
    generated_by BIGINT REFERENCES sys_user(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_report_task_id ON biz_inspection_report(task_id);

-- 预警消息表
CREATE TABLE biz_alert (
    id BIGSERIAL PRIMARY KEY,
    finding_id BIGINT REFERENCES biz_ai_finding(id),
    task_id BIGINT REFERENCES biz_inspection_task(id),
    target_user_id BIGINT REFERENCES sys_user(id),
    type alert_type NOT NULL,
    title VARCHAR(256) NOT NULL,
    message TEXT,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP,
    is_read BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_alert_target_user ON biz_alert(target_user_id);
CREATE INDEX idx_alert_is_read ON biz_alert(is_read);
CREATE INDEX idx_alert_sent_at ON biz_alert(sent_at);

-- ============================================================
-- 初始数据
-- ============================================================

-- 插入默认管理员用户 (密码: admin123, SHA-256加盐)
INSERT INTO sys_user (emp_no, name, dept, source, password_hash, salt, status) VALUES
('ADMIN001', '系统管理员', '信息技术部', 'LOCAL', '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8', 'salt_admin', 1);

-- 插入默认角色
INSERT INTO sys_role (code, name, description, created_by) VALUES
('ADMIN', '系统管理员', '拥有所有权限', 1),
('INSPECTOR', '巡检员', '执行巡检任务', 1),
('MANAGER', '项目经理', '管理项目和查看报告', 1),
('VIEWER', '查看者', '只读权限', 1);

-- 插入默认权限
INSERT INTO sys_permission (code, name, type, parent_id, path, sort_order) VALUES
-- 系统管理
('system', '系统管理', 'MENU', NULL, '/system', 1),
('system:user', '用户管理', 'MENU', 1, '/system/user', 1),
('system:user:add', '新增用户', 'BUTTON', 2, NULL, 1),
('system:user:edit', '编辑用户', 'BUTTON', 2, NULL, 2),
('system:user:delete', '删除用户', 'BUTTON', 2, NULL, 3),
('system:role', '角色管理', 'MENU', 1, '/system/role', 2),
('system:role:add', '新增角色', 'BUTTON', 6, NULL, 1),
('system:role:edit', '编辑角色', 'BUTTON', 6, NULL, 2),
('system:role:delete', '删除角色', 'BUTTON', 6, NULL, 3),
('system:log', '日志管理', 'MENU', 1, '/system/log', 3),
-- 项目管理
('project', '项目管理', 'MENU', NULL, '/project', 2),
('project:list', '项目列表', 'MENU', 11, '/project/list', 1),
('project:add', '新增项目', 'BUTTON', 12, NULL, 1),
('project:edit', '编辑项目', 'BUTTON', 12, NULL, 2),
('project:detail', '项目详情', 'BUTTON', 12, NULL, 3),
-- 巡检任务
('inspection', '巡检任务', 'MENU', NULL, '/inspection', 3),
('inspection:task', '任务管理', 'MENU', 16, '/inspection/task', 1),
('inspection:task:create', '创建任务', 'BUTTON', 17, NULL, 1),
('inspection:task:execute', '执行任务', 'BUTTON', 17, NULL, 2),
('inspection:route', '航线管理', 'MENU', 16, '/inspection/route', 2),
-- 媒体管理
('media', '媒体管理', 'MENU', NULL, '/media', 4),
('media:list', '媒体列表', 'MENU', 21, '/media/list', 1),
('media:upload', '上传媒体', 'BUTTON', 22, NULL, 1),
-- AI分析
('ai', 'AI分析', 'MENU', NULL, '/ai', 5),
('ai:finding', '缺陷发现', 'MENU', 24, '/ai/finding', 1),
('ai:alert', '预警管理', 'MENU', 24, '/ai/alert', 2),
-- 报告管理
('report', '报告管理', 'MENU', NULL, '/report', 6),
('report:list', '报告列表', 'MENU', 27, '/report/list', 1),
('report:generate', '生成报告', 'BUTTON', 28, NULL, 1),
('report:export', '导出报告', 'BUTTON', 28, NULL, 2);

-- 管理员角色拥有所有权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission;

-- 管理员用户关联管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);
