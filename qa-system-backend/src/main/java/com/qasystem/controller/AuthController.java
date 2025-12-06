package com.qasystem.controller;

import com.qasystem.common.response.Result;
import com.qasystem.dto.LoginRequest;
import com.qasystem.dto.LoginResponse;
import com.qasystem.dto.RegisterRequest;
import com.qasystem.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 【认证授权控制器】处理用户身份认证和授权相关的HTTP请求
 * 
 * 📖 功能说明：
 * 1. 用户登录认证 - 验证用户名密码，发放JWT令牌
 * 2. 用户注册功能 - 创建新用户账号（学生/教师/管理员）
 * 3. 用户登出功能 - 清除Token缓存，实现安全退出
 * 4. Token刷新机制 - 延长用户登录状态
 * 5. 健康检查接口 - 监控服务运行状态
 * 
 * 🔒 安全机制：
 * - JWT Token认证：使用RS256算法签名，避免伪造
 * - 密码加密存储：BCrypt算法，加盐哈希
 * - 输入参数校验：@Valid注解自动验证
 * - 防止CSRF攻击：Token机制天然防护
 * - 接口访问控制：所有接口无需认证（SecurityConfig中配置）
 * 
 * 🌐 RESTful设计：
 * - 基础路径：/api/v1/auth
 * - POST /login    - 登录操作
 * - POST /register - 注册操作
 * - POST /logout   - 登出操作
 * - POST /refresh  - 刷新Token
 * - GET  /health   - 健康检查
 * 
 * 📝 注解说明：
 * @Slf4j                  - Lombok注解，自动生成日志对象log
 * @RestController         - 组合注解，@Controller + @ResponseBody
 * @RequestMapping         - 定义控制器的基础URL路径
 * @RequiredArgsConstructor - Lombok注解，为final字段生成构造函数
 * 
 * @author QA System Team
 * @version 2.0.0
 * @since 2024
 */
@Slf4j  // 启用日志记录，使用log对象记录操作日志
@RestController  // 标识为REST风格控制器，所有方法返回值都会被序列化为JSON
@RequestMapping("/api/v1/auth")  // 定义该控制器的基础访问路径
@RequiredArgsConstructor  // 自动生成包含final字段的构造函数，用于依赖注入
public class AuthController {

    // 注入认证服务：处理具体的认证业务逻辑
    // final修饰符确保依赖不可变，线程安全
    private final AuthService authService;

    /**
     * 🔐 用户登录接口 - 验证身份并发放JWT令牌
     * 
     * 业务流程：
     * 1. 接收前端传来的用户名和密码
     * 2. 调用AuthService进行身份验证
     * 3. 验证成功后生成JWT Token
     * 4. 返回用户信息和Token给前端
     * 
     * 请求示例：
     * POST /api/v1/auth/login
     * Content-Type: application/json
     * {
     *     "username": "student001",
     *     "password": "password123"
     * }
     * 
     * 响应示例：
     * {
     *     "code": 200,
     *     "message": "登录成功",
     *     "data": {
     *         "token": "eyJhbGciOiJIUzI1NiI...",
     *         "refreshToken": "refresh_token_string",
     *         "expiresIn": 7200,
     *         "user": {
     *             "id": 1,
     *             "username": "student001",
     *             "role": "STUDENT",
     *             "email": "student@example.com"
     *         }
     *     }
     * }
     * 
     * @param request 登录请求对象，包含用户名和密码
     *                @Valid - 启用JSR-303参数校验
     *                @RequestBody - 从HTTP请求体中反序列化JSON数据
     * @return Result<LoginResponse> 统一响应封装，包含Token和用户信息
     * @throws AuthenticationException 当用户名或密码错误时抛出
     */
    @PostMapping("/login")  // 处理POST请求，完整路径：/api/v1/auth/login
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // 记录登录日志，不记录密码以保护安全
        log.info("用户登录请求: username={}", request.getUsername());
        
        // 调用服务层处理登录逻辑
        // 包括：查询用户、验证密码、生成Token、记录登录日志
        LoginResponse response = authService.login(request);
        
        // 返回成功响应，包装成统一格式
        return Result.success("登录成功", response);
    }

    /**
     * 👥 用户注册接口 - 创建新用户账号
     * 
     * 业务流程：
     * 1. 验证注册信息合法性（用户名、邮箱、密码强度）
     * 2. 检查用户名和邮箱是否已存在
     * 3. 密码BCrypt加密存储
     * 4. 根据角色创建对应的用户记录（Student/Teacher表）
     * 5. 自动登录并返回Token
     * 
     * 支持的用户角色：
     * - STUDENT：学生，可以提问
     * - TEACHER：教师，可以回答问题
     * - ADMIN：管理员，拥有所有权限（通常不开放注册）
     * 
     * 请求示例：
     * POST /api/v1/auth/register
     * {
     *     "username": "newstudent",
     *     "password": "SecurePass123!",
     *     "email": "student@school.edu",
     *     "role": "STUDENT",
     *     "realName": "张三",
     *     "studentId": "2024001"  // 学生需要学号
     * }
     * 
     * @param request 注册请求对象，包含用户基本信息和角色信息
     * @return Result<LoginResponse> 注册成功后自动登录，返回Token
     * @throws BusinessException 当用户名/邮箱已存在或参数不合法时抛出
     */
    @PostMapping("/register")  // 处理注册请求
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        // 记录注册日志，不记录敏感信息
        log.info("用户注册请求: username={}, role={}", request.getUsername(), request.getRole());
        
        // 调用服务层处理注册逻辑
        LoginResponse response = authService.register(request);
        
        // 注册成功后直接返回Token，免去再次登录
        return Result.success("注册成功", response);
    }

    /**
     * 🚪 用户登出接口 - 安全退出系统
     * 
     * 业务流程：
     * 1. 从请求头获取JWT Token
     * 2. 将Token加入黑名单（Redis缓存）
     * 3. 清除用户相关缓存数据
     * 4. 记录登出日志
     * 
     * 安全考虑：
     * - Token黑名单机制防止Token被继续使用
     * - 黑名单过期时间与Token过期时间一致
     * - 前端需同时清除本地存储的Token
     * 
     * 请求示例：
     * POST /api/v1/auth/logout
     * Headers:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiI...
     * 
     * @param authorization 请求头中的认证信息，格式："Bearer {token}"
     *                      @RequestHeader - 从请求头中获取指定参数
     * @return Result<Void> 登出成功响应，不返回数据
     */
    @PostMapping("/logout")  // 使用POST方法，因为登出会改变服务器状态
    public Result<Void> logout(@RequestHeader("Authorization") String authorization) {
        // 提取真实的Token：移除"Bearer "前缀（7个字符）
        // 完整格式："Bearer eyJhbGciOiJIUzI1NiI..."
        String token = authorization.substring(7);
        
        // 调用服务层处理登出逻辑
        authService.logout(token);
        
        // 返回成功响应，data为null表示无需返回数据
        return Result.success("登出成功", null);
    }

    /**
     * 🔄 Token刷新接口 - 延长用户登录状态
     * 
     * 业务流程：
     * 1. 验证Refresh Token的有效性
     * 2. 检查Refresh Token是否过期
     * 3. 生成新的Access Token
     * 4. 可选：同时更新Refresh Token（滚动刷新）
     * 
     * Token机制说明：
     * - Access Token：短期令牌（2小时），用于API访问
     * - Refresh Token：长期令牌（7天），仅用于刷新Access Token
     * - 双 Token机制提高安全性：减少Access Token暴露风险
     * 
     * 请求示例：
     * POST /api/v1/auth/refresh?refreshToken=refresh_token_string
     * 
     * @param refreshToken 刷新令牌，用于获取新的Access Token
     *                     @RequestParam - 从请求参数中获取
     * @return Result<LoginResponse> 新的Token信息和用户信息
     * @throws TokenExpiredException Refresh Token过期或无效时抛出
     */
    @PostMapping("/refresh")  // Token刷新接口
    public Result<LoginResponse> refreshToken(@RequestParam String refreshToken) {
        // 调用服务层刷新Token
        // 验证Refresh Token并生成新的Access Token
        LoginResponse response = authService.refreshToken(refreshToken);
        
        // 返回新的Token信息
        return Result.success("Token刷新成功", response);
    }

    /**
     * 🏥 健康检查接口 - 监控服务运行状态
     * 
     * 用途说明：
     * 1. 负载均衡器健康检查
     * 2. 监控系统定期探测
     * 3. Docker/K8s容器健康探针
     * 4. 前端判断服务是否可用
     * 
     * 扩展建议：
     * - 可以添加数据库连接检查
     * - 可以添加Redis连接检查  
     * - 可以返回系统版本信息
     * - 可以添加基础指标（CPU、内存、磁盘）
     * 
     * 请求示例：
     * GET /api/v1/auth/health
     * 
     * 响应示例：
     * {
     *     "code": 200,
     *     "message": "success",  
     *     "data": "服务正常运行"
     * }
     * 
     * @return Result<String> 健康状态信息
     */
    @GetMapping("/health")  // GET请求，无需认证，公开访问
    public Result<String> health() {
        // 简单返回服务状态
        // 生产环境可以增加更多检查项
        return Result.success("服务正常运行");
    }
}

