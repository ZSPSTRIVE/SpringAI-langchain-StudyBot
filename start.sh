#!/bin/bash

# 师生答疑系统 v2.0 启动脚本
# 用于快速启动开发环境

set -e

echo "=========================================="
echo "  师生答疑系统 v2.0 - 启动脚本"
echo "=========================================="
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查命令是否存在
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# 检查端口是否被占用
check_port() {
    if lsof -Pi :$1 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
        return 0
    else
        return 1
    fi
}

# 检查必要的命令
echo "🔍 检查环境依赖..."
if ! command_exists java; then
    echo -e "${RED}❌ 错误: 未找到 Java，请安装 JDK 17+${NC}"
    exit 1
fi

if ! command_exists mvn; then
    echo -e "${RED}❌ 错误: 未找到 Maven，请安装 Maven 3.6+${NC}"
    exit 1
fi

if ! command_exists node; then
    echo -e "${RED}❌ 错误: 未找到 Node.js，请安装 Node.js 18+${NC}"
    exit 1
fi

echo -e "${GREEN}✅ 环境检查通过${NC}"
echo ""

# 检查MySQL和Redis
echo "🔍 检查服务依赖..."
if ! check_port 3306; then
    echo -e "${YELLOW}⚠️  警告: MySQL (3306端口) 未运行，请先启动 MySQL${NC}"
    echo "   提示: 可使用 Docker 快速启动: docker run -d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root mysql:8.0"
fi

if ! check_port 6379; then
    echo -e "${YELLOW}⚠️  警告: Redis (6379端口) 未运行，请先启动 Redis${NC}"
    echo "   提示: 可使用 Docker 快速启动: docker run -d -p 6379:6379 redis:7-alpine"
fi
echo ""

# 询问启动模式
echo "请选择启动模式:"
echo "  1) 仅启动后端"
echo "  2) 仅启动前端"
echo "  3) 同时启动前后端 (推荐)"
echo ""
read -p "请输入选项 [1-3]: " mode

case $mode in
    1)
        echo ""
        echo "📦 启动后端服务..."
        cd qa-system-backend
        mvn clean spring-boot:run
        ;;
    2)
        echo ""
        echo "🎨 启动前端服务..."
        cd qa-system-frontend
        
        # 检查是否已安装依赖
        if [ ! -d "node_modules" ]; then
            echo "📥 安装前端依赖..."
            npm install
        fi
        
        npm run dev
        ;;
    3)
        echo ""
        echo "🚀 同时启动前后端服务..."
        
        # 启动后端（后台运行）
        echo "📦 启动后端服务（后台运行）..."
        cd qa-system-backend
        mvn clean spring-boot:run > ../backend.log 2>&1 &
        BACKEND_PID=$!
        echo "   后端 PID: $BACKEND_PID"
        cd ..
        
        # 等待后端启动
        echo "⏳ 等待后端服务启动..."
        sleep 10
        
        # 启动前端
        echo "🎨 启动前端服务..."
        cd qa-system-frontend
        
        # 检查是否已安装依赖
        if [ ! -d "node_modules" ]; then
            echo "📥 安装前端依赖..."
            npm install
        fi
        
        npm run dev
        ;;
    *)
        echo -e "${RED}❌ 无效选项${NC}"
        exit 1
        ;;
esac

echo ""
echo -e "${GREEN}=========================================="
echo "  启动完成！"
echo "==========================================${NC}"
echo ""
echo "访问地址:"
echo "  - 前端: http://localhost:5173"
echo "  - 后端: http://localhost:8080"
echo "  - API文档: http://localhost:8080/actuator"
echo ""
echo "默认账号:"
echo "  - 管理员: admin / admin123"
echo ""

