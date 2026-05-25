#!/bin/bash

# 风机巡检系统开发环境启动脚本

echo "=========================================="
echo "  风机施工安装监测与质量巡检系统"
echo "  开发环境启动脚本"
echo "=========================================="

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo -e "${RED}错误: Docker未安装，请先安装Docker Desktop${NC}"
    exit 1
fi

# 检查Docker Compose是否安装
if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}错误: Docker Compose未安装${NC}"
    exit 1
fi

# 检查Java是否安装
if ! command -v java &> /dev/null; then
    echo -e "${RED}错误: Java未安装，请先安装JDK 17${NC}"
    exit 1
fi

# 检查Maven是否安装
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}错误: Maven未安装，请先安装Maven${NC}"
    exit 1
fi

# 检查Node.js是否安装
if ! command -v node &> /dev/null; then
    echo -e "${RED}错误: Node.js未安装，请先安装Node.js 18+${NC}"
    exit 1
fi

echo -e "${GREEN}环境检查通过！${NC}"

# 启动Docker服务
echo -e "${YELLOW}正在启动Docker服务...${NC}"
cd ../docker
docker-compose up -d

# 等待服务启动
echo -e "${YELLOW}等待服务启动...${NC}"
sleep 10

# 检查服务状态
echo -e "${YELLOW}检查服务状态...${NC}"
docker-compose ps

# 创建MinIO Bucket
echo -e "${YELLOW}创建MinIO存储桶...${NC}"
docker exec wind-minio mc alias set local http://localhost:9000 minioadmin minio123456
docker exec wind-minio mc mb local/wind-inspection --ignore-existing

# 编译后端项目
echo -e "${YELLOW}编译后端项目...${NC}"
cd ../
mvn clean install -DskipTests

# 启动后端服务
echo -e "${YELLOW}启动后端服务...${NC}"
cd services/admin-service
mvn spring-boot:run &
ADMIN_PID=$!

cd ../gateway
mvn spring-boot:run &
GATEWAY_PID=$!

# 启动前端项目
echo -e "${YELLOW}启动前端项目...${NC}"
cd ../../apps/web
npm install
npm run dev &
WEB_PID=$!

echo ""
echo -e "${GREEN}=========================================="
echo "  系统启动完成！"
echo "=========================================="
echo ""
echo "访问地址："
echo "  - 前端: http://localhost:5173"
echo "  - 后端API: http://localhost:8081"
echo "  - API网关: http://localhost:8080"
echo "  - Nacos控制台: http://localhost:8848"
echo "  - MinIO控制台: http://localhost:9001"
echo ""
echo "默认账户："
echo "  - 管理员: ADMIN001 / admin123"
echo "  - Nacos: nacos / nacos"
echo "  - MinIO: minioadmin / minio123456"
echo ""
echo "按 Ctrl+C 停止所有服务${NC}"

# 等待用户中断
trap "echo -e '${YELLOW}正在停止服务...${NC}'; kill $ADMIN_PID $GATEWAY_PID $WEB_PID 2>/dev/null; docker-compose down; echo -e '${GREEN}服务已停止${NC}'; exit 0" INT
wait
