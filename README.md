# 风机施工安装监测与质量巡检系统

## 项目简介

本系统为上海电气风电集团开发，用于风力发电机施工安装期间的无人机巡检和质量监测。系统支持自动化航线规划、4K视频采集与上传、AI实时检测预警、巡检报告生成等核心功能。

### 核心能力

- **系统管理**：LDAP+RBAC权限管理、操作审计、项目/机位管理
- **航线引擎**：到货检查、施工监督、整体验收三类自动化航线自动生成
- **AI检测**：安全帽佩戴检测（≥95%准确率）、吊带/夹具位置检测（≥95%准确率），3秒内预警
- **大文件处理**：5MB分片上传、断点续传、MinIO对象存储
- **离线工作**：Android+鸿蒙端APP，支持离线任务执行和数据同步

---

## 系统架构

```
                        ┌─────────────────┐
                        │   Nginx 反向代理  │
                        └────────┬────────┘
                                 │
                        ┌────────▼────────┐
                        │  Spring Cloud    │
                        │  Gateway :8080   │
                        │  (鉴权/路由/CORS) │
                        └────────┬────────┘
                                 │
          ┌──────────────────────┼──────────────────────┐
          │                      │                      │
 ┌────────▼────────┐  ┌─────────▼─────────┐  ┌────────▼────────┐
 │ admin-service   │  │ inspection-service │  │ media-service   │
 │ :8081           │  │ :8082             │  │ :8083           │
 │                 │  │                   │  │                 │
 │ · 用户/角色管理  │  │ · 项目/机位管理    │  │ · 分片上传      │
 │ · LDAP同步      │  │ · 任务管理        │  │ · MinIO存储     │
 │ · 日志审计      │  │ · 航线引擎        │  │ · 文件预览/下载  │
 │ · 权限校验      │  │ · AI调度          │  │                 │
 │                 │  │ · 预警管理        │  │                 │
 └────────┬────────┘  └────────┬──────────┘  └────────┬────────┘
          │                    │                      │
          └────────────────────┼──────────────────────┘
                               │
                    ┌──────────▼──────────┐
                    │   PostgreSQL 16     │
                    │   + Redis 7         │
                    │   + MinIO           │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │  AI 推理服务 :8090   │
                    │  (FastAPI + YOLOv8)  │
                    │  · 安全帽检测        │
                    │  · 吊带位置检测      │
                    └─────────────────────┘
```

---

## 技术栈

| 层级 | 技术选型 | 说明 |
|------|----------|------|
| **前端** | Vue 3 + TypeScript + Element Plus + ECharts | 企业级后台管理 |
| **后端** | Java 17 + Spring Boot 3.3 + Spring Cloud 2023 + Nacos 2.3 | 微服务架构 |
| **ORM** | MyBatis-Plus 3.5.6 | 自动填充、分页、逻辑删除 |
| **认证** | Sa-Token + LDAP + SHA-256加盐 | 轻量级RBAC |
| **数据库** | PostgreSQL 16 + PostGIS + Redis 7 | 地理空间+缓存 |
| **对象存储** | MinIO | S3兼容，适合内网大文件 |
| **AI推理** | Python 3.11 + YOLOv8 + ONNX Runtime + FastAPI | 实时检测 |
| **移动端** | Kotlin (Android) + ArkTS (鸿蒙) + DJI Mobile SDK V5 | 原生开发 |
| **容器化** | Docker Compose | 一键部署开发环境 |

---

## 项目结构

```
wind-inspection-system/
├── services/                          # 后端微服务
│   ├── common/                        # 公共模块（R、ResultCode、异常处理）
│   ├── gateway/                       # API网关（路由、鉴权、CORS）
│   ├── admin-service/                 # 系统管理服务
│   │   └── src/main/java/.../admin/
│   │       ├── controller/            # Auth、User、Role、Permission、Log
│   │       ├── service/               # 业务逻辑
│   │       ├── mapper/                # 数据访问
│   │       ├── entity/                # 实体类
│   │       ├── dto/                   # 数据传输对象
│   │       └── config/                # Sa-Token、MyBatis-Plus、Redis配置
│   ├── inspection-service/            # 巡检业务服务
│   │   └── src/main/java/.../inspection/
│   │       ├── controller/            # Project、Turbine、Task、Route、AiFinding、Alert
│   │       ├── service/               # 业务逻辑 + AI调度
│   │       ├── mapper/                # 数据访问
│   │       ├── entity/                # 实体类
│   │       ├── dto/                   # 数据传输对象
│   │       ├── engine/                # 航线生成引擎（FlightPlanEngine）
│   │       └── config/                # 配置
│   ├── media-service/                 # 媒体文件服务
│   │   └── src/main/java/.../media/
│   │       ├── controller/            # UploadController
│   │       ├── service/               # UploadService、MinioService、FileNamingService
│   │       ├── mapper/                # 数据访问
│   │       ├── entity/                # MediaFile、UploadTask、UploadChunk
│   │       ├── dto/                   # 上传相关DTO
│   │       └── config/                # MinIO、MyBatis-Plus配置
│   └── ai/                            # AI推理服务（Python）
│       ├── app/
│       │   ├── main.py                # FastAPI入口
│       │   ├── detectors/             # 检测器（helmet、sling）
│       │   ├── models/                # Pydantic模型
│       │   └── utils/                 # 视频抽帧工具
│       ├── configs/                   # 配置
│       ├── weights/                   # ONNX模型权重
│       ├── requirements.txt           # Python依赖
│       └── Dockerfile                 # 容器化
├── apps/                              # 前端应用
│   └── web/                           # Web管理端（Vue 3）
│       └── src/
│           ├── api/                   # API调用（auth、user、role、project、task、media、ai、alert）
│           ├── views/                 # 页面
│           │   ├── login/             # 登录页
│           │   ├── dashboard/         # 首页仪表盘
│           │   ├── system/            # 系统管理（用户、角色、日志）
│           │   ├── project/           # 项目管理
│           │   ├── inspection/        # 巡检任务、航线管理
│           │   ├── media/             # 媒体文件管理
│           │   ├── ai/                # AI缺陷发现、预警管理
│           │   └── report/            # 巡检报告
│           ├── router/                # 路由配置
│           ├── store/                 # Pinia状态管理
│           ├── layout/                # 布局组件
│           ├── utils/                 # 工具（axios封装）
│           └── styles/                # 全局样式
├── docker/                            # Docker配置
│   ├── docker-compose.yml             # 开发环境（PostgreSQL、Redis、MinIO、Nacos、AI服务）
│   └── init-db/
│       └── 01-init.sql                # 数据库初始化脚本
├── scripts/                           # 启动脚本
│   ├── start-dev.bat                  # Windows启动
│   └── start-dev.sh                   # Linux/Mac启动
├── pom.xml                            # Maven父POM
└── README.md                          # 本文件
```

---

## 数据库设计

### 核心表

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `sys_user` | 用户表 | emp_no, name, dept, source(LDAP/LOCAL), password_hash, salt |
| `sys_role` | 角色表 | code, name |
| `sys_permission` | 权限表 | code, type(MENU/BUTTON/DATA), path |
| `sys_user_role` | 用户角色关联 | user_id, role_id |
| `sys_role_permission` | 角色权限关联 | role_id, permission_id |
| `sys_login_log` | 登录日志 | user_id, ip, source(WEB/APP), status |
| `sys_operation_log` | 操作日志 | user_id, module, action, detail |
| `biz_project` | 项目表 | name, owner, location, status |
| `biz_turbine` | 机位表 | project_id, code, model, lat, lng, altitude |
| `biz_inspection_task` | 巡检任务 | project_id, turbine_id, type(ARRIVAL/INSTALL/ACCEPTANCE), status |
| `biz_flight_route` | 航线表 | task_id, name, waypoints_json, dji_mission_json |
| `biz_upload_task` | 上传任务 | file_md5, file_name, file_size, total_chunks, status |
| `biz_upload_chunk` | 上传分片 | file_md5, chunk_number, chunk_path |
| `biz_media_file` | 媒体文件 | task_id, type(VIDEO/PHOTO), path, bucket, checksum |
| `biz_ai_finding` | AI发现 | task_id, type, severity, confidence, status(PENDING/CONFIRMED/REJECTED) |
| `biz_alert` | 预警消息 | finding_id, task_id, target_user_id, message, read_at |

### 枚举类型

```sql
-- 任务类型
CREATE TYPE task_type AS ENUM ('ARRIVAL', 'INSTALL', 'ACCEPTANCE');

-- 任务状态
CREATE TYPE task_status AS ENUM ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED');

-- 媒体类型
CREATE TYPE media_type AS ENUM ('VIDEO', 'PHOTO');

-- 发现状态
CREATE TYPE finding_status AS ENUM ('PENDING', 'CONFIRMED', 'REJECTED');
```

---

## API接口

### 网关路由

| 路径前缀 | 目标服务 | 说明 |
|----------|----------|------|
| `/admin/**` | admin-service:8081 | 系统管理 |
| `/inspection/**` | inspection-service:8082 | 巡检业务 |
| `/media/**` | media-service:8083 | 媒体文件 |

### 认证接口（/admin/auth）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/captcha` | 获取验证码 |
| POST | `/login` | 用户登录 |
| POST | `/logout` | 用户登出 |
| GET | `/info` | 获取当前用户信息 |

### 用户管理（/admin/users）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/` | 分页查询用户 |
| GET | `/{id}` | 获取用户详情 |
| POST | `/` | 创建用户 |
| PUT | `/{id}` | 更新用户 |
| DELETE | `/{id}` | 删除用户 |
| PUT | `/{id}/roles` | 分配角色 |
| PUT | `/{id}/lock` | 锁定账户 |
| PUT | `/{id}/unlock` | 解锁账户 |
| PUT | `/{id}/reset-password` | 重置密码 |

### 项目管理（/inspection/projects）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/` | 分页查询项目 |
| GET | `/all` | 获取所有项目 |
| GET | `/{id}` | 获取项目详情 |
| POST | `/` | 创建项目 |
| PUT | `/{id}` | 更新项目 |
| DELETE | `/{id}` | 删除项目 |

### 任务管理（/inspection/tasks）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/` | 分页查询任务 |
| GET | `/{id}` | 获取任务详情 |
| POST | `/` | 创建任务 |
| PUT | `/{id}` | 更新任务 |
| DELETE | `/{id}` | 删除任务 |
| PUT | `/{id}/start` | 开始任务 |
| PUT | `/{id}/complete` | 完成任务 |
| PUT | `/{id}/progress` | 更新进度 |

### 航线管理（/inspection/routes）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/task/{taskId}` | 获取任务航线 |
| GET | `/{id}` | 获取航线详情 |
| POST | `/generate/blade-arrival` | 生成叶片到货检查航线 |
| POST | `/generate/orbital-arrival` | 生成360°环绕到货检查航线 |
| POST | `/generate/blade-install` | 生成叶片安装监督航线 |
| POST | `/generate/acceptance` | 生成整体验收航线 |
| DELETE | `/{id}` | 删除航线 |

### 媒体文件（/media）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/upload/init` | 初始化上传（秒传检测+断点续传） |
| POST | `/upload/chunk` | 上传分片 |
| POST | `/upload/merge` | 合并分片 |
| GET | `/upload/chunks/{fileMd5}` | 获取已上传分片列表 |
| GET | `/files` | 分页查询媒体文件 |
| GET | `/files/{id}` | 获取文件详情 |
| DELETE | `/files/{id}` | 删除文件 |
| GET | `/files/{id}/download` | 获取下载URL |
| GET | `/files/{id}/preview` | 获取预览URL |

### AI检测（/inspection/ai/findings）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/` | 创建AI发现（AI服务回调） |
| GET | `/` | 分页查询AI发现 |
| GET | `/{id}` | 获取发现详情 |
| PUT | `/{id}/review` | 审核发现（确认/驳回） |
| GET | `/stats/{taskId}` | 获取任务发现统计 |

### 预警管理（/inspection/alerts）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/` | 发送预警 |
| GET | `/` | 分页查询预警 |
| PUT | `/{id}/read` | 标记已读 |
| PUT | `/read-all` | 全部标记已读 |
| GET | `/unread-count` | 获取未读数量 |

### AI推理服务（直接调用 :8090）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/health` | 健康检查 |
| POST | `/detect/image` | 图片检测（上传文件） |
| POST | `/detect/video` | 视频检测（上传文件+抽帧） |
| POST | `/detect/frame` | 图片检测（URL方式） |

---

## 航线引擎

系统内置航线生成引擎 `FlightPlanEngine`，支持四类自动化航线：

### 1. 叶片到货检查（Blade Arrival）

生成3条航线，分别检查叶片三个面：
- **前缘（Leading Edge）**：35°俯仰角，沿叶片前缘飞行
- **后缘（Trailing Edge）**：-35°俯仰角，沿叶片后缘飞行
- **主梁（Spar Cap）**：90°俯仰角，正对叶片表面

### 2. 360°环绕到货检查（Orbital Arrival）

- 30-45°仰角，绕风机360°旋转
- 每隔10°设置一个航点
- 自动调整云台角度保持目标在画面中心

### 3. 叶片安装监督（Blade Install）

- 针对叶片安装关键点进行检查
- 每个关键点停留2秒进行拍摄
- 覆盖根部、中部、尖部等关键位置

### 4. 整体验收检查（Acceptance）

- 覆盖塔筒基础、机舱、轮毂、叶片
- 从低到高逐层检查
- 包含全景和细节两个视角

### 航点数据格式

```json
{
  "lat": 31.2304,
  "lng": 121.4737,
  "altitude": 80.0,
  "speed": 3.0,
  "heading": 90.0,
  "gimbalPitch": -45.0,
  "stayTime": 2.0,
  "action": "start-recording"
}
```

---

## 快速开始

### 环境要求

| 工具 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | OpenJDK或Oracle JDK |
| Maven | 3.8+ | 构建工具 |
| Node.js | 18+ | 前端构建 |
| Docker Desktop | 最新 | 容器运行环境 |
| Python | 3.11+ | AI推理服务（可选） |
| Git | 最新 | 版本控制 |

### Windows启动

```bash
# 1. 克隆项目
git clone <repository-url>
cd wind-inspection-system

# 2. 运行启动脚本
scripts\start-dev.bat
```

### Linux/Mac启动

```bash
# 1. 克隆项目
git clone <repository-url>
cd wind-inspection-system

# 2. 添加执行权限
chmod +x scripts/start-dev.sh

# 3. 运行启动脚本
./scripts/start-dev.sh
```

### 手动启动

```bash
# 1. 启动基础设施
cd docker
docker-compose up -d

# 2. 等待服务就绪（约15秒）
docker-compose ps  # 确认所有服务healthy

# 3. 编译后端
cd ..
mvn clean install -DskipTests

# 4. 启动后端服务（按顺序）
# 终端1：API网关
cd services/gateway && mvn spring-boot:run

# 终端2：系统管理服务
cd services/admin-service && mvn spring-boot:run

# 终端3：巡检业务服务
cd services/inspection-service && mvn spring-boot:run

# 终端4：媒体文件服务
cd services/media-service && mvn spring-boot:run

# 5. 启动前端
cd apps/web
npm install
npm run dev

# 6. 启动AI服务（可选）
cd services/ai
pip install -r requirements.txt
uvicorn app.main:app --port 8090
```

### 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| Web前端 | http://localhost:5173 | Vue管理端 |
| API网关 | http://localhost:8080 | 统一入口 |
| Nacos控制台 | http://localhost:8848/nacos | 注册中心 |
| MinIO控制台 | http://localhost:9001 | 对象存储 |
| AI服务 | http://localhost:8090/docs | FastAPI文档 |

### 默认账户

| 服务 | 用户名 | 密码 |
|------|--------|------|
| 系统管理员 | ADMIN001 | admin123 |
| Nacos | nacos | nacos |
| MinIO | minioadmin | minio123456 |
| PostgreSQL | wind | wind123456 |
| Redis | - | redis123456 |

---

## 开发指南

### 后端开发

1. 使用 IntelliJ IDEA 打开项目根目录
2. 安装 Lombok 插件
3. 配置 JDK 17
4. Maven 自动下载依赖
5. 运行各服务的 `*Application.java` 启动类

### 前端开发

```bash
cd apps/web
npm install
npm run dev
```

前端使用 Vite 开发服务器，支持热更新。API请求通过 Vite 代理转发到网关：

```
/api/* → http://localhost:8080/* → Gateway路由
```

### 新增接口步骤

1. 在对应服务的 `entity/` 下创建实体类
2. 在 `mapper/` 下创建Mapper接口
3. 在 `dto/` 下创建请求/响应DTO
4. 在 `service/` 下创建服务接口和实现
5. 在 `controller/` 下创建REST控制器
6. 在前端 `api/` 下创建API调用函数
7. 在 `views/` 下创建页面组件

---

## 部署

### Docker部署

```bash
# 编译后端
mvn clean package -DskipTests

# 启动所有服务
cd docker
docker-compose up -d

# 查看日志
docker-compose logs -f ai-service
```

### 生产环境配置

1. **数据库**：修改 `docker-compose.yml` 中的数据库密码
2. **LDAP**：配置 `admin-service` 的 LDAP 服务器地址
3. **MinIO**：配置访问密钥和存储桶
4. **Nacos**：配置集群模式
5. **HTTPS**：在 Nginx 中配置 SSL 证书
6. **AI模型**：将训练好的 ONNX 模型放入 `services/ai/weights/`

### 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `DB_HOST` | 数据库地址 | 127.0.0.1 |
| `DB_PORT` | 数据库端口 | 5432 |
| `DB_USER` | 数据库用户 | wind |
| `DB_PASSWORD` | 数据库密码 | wind123456 |
| `REDIS_HOST` | Redis地址 | 127.0.0.1 |
| `REDIS_PORT` | Redis端口 | 6379 |
| `REDIS_PASSWORD` | Redis密码 | redis123456 |
| `NACOS_ADDR` | Nacos地址 | 127.0.0.1:8848 |
| `MINIO_ENDPOINT` | MinIO地址 | http://127.0.0.1:9000 |
| `MINIO_ACCESS_KEY` | MinIO密钥 | minioadmin |
| `MINIO_SECRET_KEY` | MinIO密码 | minio123456 |
| `AI_SERVICE_URL` | AI服务地址 | http://localhost:8090 |

---

## 常见问题

### Docker启动失败

检查 Docker Desktop 是否运行，确认端口未被占用：

```bash
# 检查端口占用
netstat -ano | findstr "5432 6379 9000 8848"
```

### 数据库连接失败

确认 PostgreSQL 已启动，检查 `application.yml` 中的连接配置。

### Maven编译失败

```bash
# 清理并重新编译
mvn clean install -DskipTests

# 如果依赖下载慢，配置阿里云镜像
# 在 ~/.m2/settings.xml 中添加阿里云仓库
```

### 前端启动失败

```bash
# 清除缓存重新安装
rm -rf node_modules package-lock.json
npm install
npm run dev
```

### AI服务启动失败

```bash
# 安装依赖
pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple

# 如果没有GPU，CPU模式也可运行（推理速度较慢）
```

---

## 开发进度

| 阶段 | 内容 | 状态 |
|------|------|------|
| 阶段1 | 基础架构与系统管理 | 已完成 |
| 阶段2 | 核心业务功能 | 已完成 |
| 阶段3 | 媒体管理与大文件处理 | 已完成 |
| 阶段4 | AI算法开发与集成 | 已完成 |
| 阶段5 | 移动端APP开发 | 待开始 |
| 阶段6 | 报告、大屏与收尾 | 待开始 |

---

## 联系方式

- 项目负责人：[待填写]
- 技术支持：[待填写]
