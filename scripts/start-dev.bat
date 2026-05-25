@echo off
chcp 65001 >nul

echo ==========================================
echo   风机施工安装监测与质量巡检系统
echo   开发环境启动脚本
echo ==========================================

REM 检查Docker是否安装
docker --version >nul 2>&1
if errorlevel 1 (
    echo 错误: Docker未安装，请先安装Docker Desktop
    pause
    exit /b 1
)

REM 检查Java是否安装
java -version >nul 2>&1
if errorlevel 1 (
    echo 错误: Java未安装，请先安装JDK 17
    pause
    exit /b 1
)

REM 检查Maven是否安装
mvn -version >nul 2>&1
if errorlevel 1 (
    echo 错误: Maven未安装，请先安装Maven
    pause
    exit /b 1
)

REM 检查Node.js是否安装
node --version >nul 2>&1
if errorlevel 1 (
    echo 错误: Node.js未安装，请先安装Node.js 18+
    pause
    exit /b 1
)

echo 环境检查通过！

REM 启动Docker服务
echo 正在启动Docker服务...
cd ..\docker
docker-compose up -d

REM 等待服务启动
echo 等待服务启动...
timeout /t 10 /nobreak

REM 检查服务状态
echo 检查服务状态...
docker-compose ps

REM 创建MinIO Bucket
echo 创建MinIO存储桶...
docker exec wind-minio mc alias set local http://localhost:9000 minioadmin minio123456
docker exec wind-minio mc mb local/wind-inspection --ignore-existing

REM 编译后端项目
echo 编译后端项目...
cd ..
mvn clean install -DskipTests

echo.
echo ==========================================
echo   系统启动完成！
echo ==========================================
echo.
echo 访问地址：
echo   - 前端: http://localhost:5173
echo   - 后端API: http://localhost:8081
echo   - API网关: http://localhost:8080
echo   - Nacos控制台: http://localhost:8848
echo   - MinIO控制台: http://localhost:9001
echo.
echo 默认账户：
echo   - 管理员: ADMIN001 / admin123
echo   - Nacos: nacos / nacos
echo   - MinIO: minioadmin / minio123456
echo.
echo 请在IDE中分别启动以下服务：
echo   1. GatewayApplication (端口8080)
echo   2. AdminServiceApplication (端口8081)
echo   3. 前端: cd apps/web ^&^& npm run dev
echo.
pause
