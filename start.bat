@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

:: 师生答疑系统 v2.0 启动脚本 (Windows)

echo ==========================================
echo   师生答疑系统 v2.0 - 启动脚本
echo ==========================================
echo.

:: 检查Java
where java >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 错误: 未找到 Java，请安装 JDK 17+
    pause
    exit /b 1
)

:: 检查Maven
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 错误: 未找到 Maven，请安装 Maven 3.6+
    pause
    exit /b 1
)

:: 检查Node.js
where node >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 错误: 未找到 Node.js，请安装 Node.js 18+
    pause
    exit /b 1
)

echo ✅ 环境检查通过
echo.

:: 询问启动模式
echo 请选择启动模式:
echo   1) 仅启动后端
echo   2) 仅启动前端
echo   3) 同时启动前后端 (推荐)
echo.
set /p mode=请输入选项 [1-3]: 

if "%mode%"=="1" goto backend
if "%mode%"=="2" goto frontend
if "%mode%"=="3" goto both
goto invalid

:backend
echo.
echo 📦 启动后端服务...
cd qa-system-backend
call mvn clean spring-boot:run
goto end

:frontend
echo.
echo 🎨 启动前端服务...
cd qa-system-frontend

if not exist "node_modules" (
    echo 📥 安装前端依赖...
    call npm install
)

call npm run dev
goto end

:both
echo.
echo 🚀 同时启动前后端服务...

:: 启动后端（新窗口）
echo 📦 启动后端服务...
start "QA-System-Backend" cmd /c "cd qa-system-backend && mvn clean spring-boot:run"

:: 等待后端启动
echo ⏳ 等待后端服务启动...
timeout /t 15 /nobreak >nul

:: 启动前端
echo 🎨 启动前端服务...
cd qa-system-frontend

if not exist "node_modules" (
    echo 📥 安装前端依赖...
    call npm install
)

call npm run dev
goto end

:invalid
echo ❌ 无效选项
pause
exit /b 1

:end
echo.
echo ==========================================
echo   启动完成！
echo ==========================================
echo.
echo 访问地址:
echo   - 前端: http://localhost:5173
echo   - 后端: http://localhost:8080
echo   - API文档: http://localhost:8080/actuator
echo.
echo 默认账号:
echo   - 管理员: admin / admin123
echo.
pause

