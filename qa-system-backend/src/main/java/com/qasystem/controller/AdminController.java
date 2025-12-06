package com.qasystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qasystem.common.response.Result;
import com.qasystem.dto.CreateUserRequest;
import com.qasystem.dto.ResetPasswordRequest;
import com.qasystem.dto.UserProfileDTO;
import com.qasystem.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 🔑 管理员控制器 - 系统管理员的后台管理功能
 * 
 * 📖 功能说明：
 * 管理员模块是系统运营的核心，提供对系统用户、数据的全面管理。
 * 本控制器主要功能包括：
 * 1. 数据统计 - 查看系统整体运营数据
 * 2. 用户管理 - 分页查询学生和教师列表
 * 3. 状态管理 - 启用/禁用/封禁用户账号
 * 4. 用户删除 - 删除违规用户
 * 5. 密码重置 - 帮助用户重置密码
 * 6. 账号创建 - 批量创建学生/教师账号
 * 
 * 🔒 权限控制：
 * - 所有接口仅允许管理员角色（ROLE_ADMIN）访问
 * - 通过Spring Security在路由层面统一拦截
 * - 敏感操作需要记录审计日志
 * 
 * 🌍 RESTful 设计：
 * GET    /api/v1/admin/statistics              获取数据统计
 * GET    /api/v1/admin/students                 分页查询学生
 * GET    /api/v1/admin/teachers                 分页查询教师
 * PUT    /api/v1/admin/users/{userId}/status    更新用户状态
 * DELETE /api/v1/admin/users/{userId}           删除用户
 * PUT    /api/v1/admin/users/{userId}/reset-password  重置密码
 * POST   /api/v1/admin/students                 创建学生账号
 * POST   /api/v1/admin/teachers                 创建教师账号
 * 
 * 📝 安全考虑：
 * - 所有操作必须记录审计日志
 * - 敏感信息（如密码）不允许记录到日志
 * - 删除操作使用软删除，保留数据用于审计
 * - 密码重置需选择强密码
 * 
 * @author 师生答疑系统开发团队
 * @since 1.0.0
 */
@Slf4j  // 自动生成日志对象log，用于记录管理操作日志
@RestController  // 标识这是一个REST控制器，返回JSON数据
@RequestMapping("/api/v1/admin")  // 定义管理员接口的基础路径
@RequiredArgsConstructor  // 为final字段生成构造函数，实现依赖注入
public class AdminController {

    // 管理员服务层接口，处理所有管理相关的业务逻辑
    // final确保注入后不可修改
    private final AdminService adminService;

    /**
     * 📊 获取系统数据统计 - 仪表盘首页的关键指标
     * 
     * 业务流程：
     * 1. 统计用户数据：学生总数、教师总数、今日新增用户
     * 2. 统计内容数据：问题总数、回答总数、待解决问题数
     * 3. 统计活跃数据：今日活跃用户数、本周新增问题数
     * 4. 统计系统资源：存储空间使用情况、数据库连接池状态
     * 5. 计算趋势数据：用户增长趋势、问题增长趋势
     * 6. 组装所有统计数据为Map返回
     * 
     * 返回数据结构：
     * - userStats: 用户统计（学生数、教师数、增长趋势）
     * - contentStats: 内容统计（问题数、回答数、解决率）
     * - activityStats: 活跃统计（DAU、MAU、活跃趋势）
     * - systemStats: 系统统计（存储、性能、负载）
     * 
     * 使用场景：
     * - 管理员后台首页仪表盘
     * - 系统运营数据分析
     * - 日报/周报/月报生成
     * 
     * 权限要求：
     * - 仅管理员可以访问
     * - 普通用户无权查看系统统计数据
     * 
     * 请求示例：
     * GET /api/v1/admin/statistics
     * Headers:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiI...
     * 
     * 成功响应示例：
     * {
     *     "code": 200,
     *     "message": "成功",
     *     "data": {
     *         "userStats": {
     *             "totalStudents": 1250,
     *             "totalTeachers": 80,
     *             "todayNewUsers": 15,
     *             "userGrowthRate": 5.2
     *         },
     *         "contentStats": {
     *             "totalQuestions": 3420,
     *             "totalAnswers": 8650,
     *             "pendingQuestions": 156,
     *             "resolveRate": 87.3
     *         },
     *         "activityStats": {
     *             "dau": 420,
     *             "mau": 980,
     *             "thisWeekQuestions": 186
     *         },
     *         "systemStats": {
     *             "storageUsed": "2.3GB",
     *             "dbConnectionPool": "8/20"
     *         }
     *     }
     * }
     * 
     * @return Result<Map<String, Object>> 统一响应对象，包含多维度统计数据
     *         Map的key为统计类别，value为对应的统计指标
     */
    @GetMapping("/statistics")  // 处理GET请求，完整路径：/api/v1/admin/statistics
    public Result<Map<String, Object>> getStatistics() {
        // 记录统计数据查询日志
        // 用于监控管理员的操作频率
        log.info("获取数据统计");
        
        // 调用服务层获取统计数据
        // 服务层会从多个数据源汇总统计指标
        Map<String, Object> statistics = adminService.getStatistics();
        
        // 返回统计数据
        // 前端可以根据这些数据绘制图表和仪表盘
        return Result.success(statistics);
    }

    /**
     * 👨‍🎓 分页查询学生列表 - 管理员查看和管理所有学生账号
     * 
     * 业务流程：
     * 1. 接收分页参数：页码、每页数量
     * 2. 接收查询条件：关键词搜索（姓名/学号/邮箱）、状态筛选
     * 3. 构建查询条件：角色=STUDENT + 关键词 + 状态
     * 4. 执行分页查询，按注册时间降序
     * 5. 组装学生信息：基本信息 + 统计数据（提问数、采纳数）
     * 6. 返回分页结果
     * 
     * 搜索功能：
     * - keyword支持模糊搜索：姓名、学号、邮箱、手机号
     * - status支持筛选：ACTIVE(正常)、DISABLED(禁用)、BANNED(封禁)
     * - 支持按注册时间、最后登录时间排序
     * 
     * 请求示例：
     * GET /api/v1/admin/students?page=1&size=20&keyword=张三&status=ACTIVE
     * 
     * @param page 页码，默认1，从RequestParam获取
     * @param size 每页数量，默认10
     * @param keyword 搜索关键词，可选
     * @param status 状态筛选，可选
     * @return Result<IPage<UserProfileDTO>> 分页结果，包含学生列表和总数
     */
    @GetMapping("/students")  // 处理GET请求
    public Result<IPage<UserProfileDTO>> getStudentPage(
            @RequestParam(defaultValue = "1") Integer page,  // 默认第1页
            @RequestParam(defaultValue = "10") Integer size,  // 默认每页10条
            @RequestParam(required = false) String keyword,  // 搜索关键词，可选
            @RequestParam(required = false) String status) {  // 状态筛选，可选
        // 记录查询日志，包含所有查询条件
        log.info("分页查询学生: page={}, size={}, keyword={}, status={}", page, size, keyword, status);
        // 调用服务层执行分页查询
        IPage<UserProfileDTO> result = adminService.getStudentPage(page, size, keyword, status);
        // 返回分页结果
        return Result.success(result);
    }

    /**
     * 👨‍🏫 分页查询教师列表 - 管理员查看和管理所有教师账号
     * 
     * 业务流程和功能与查询学生列表类似，区别在于：
     * - 角色筛选为TEACHER
     * - 额外显示教师特有信息：回答数、被采纳数、信誉分
     * - 支持按教师的活跃度、贡献度排序
     * 
     * 请求示例：
     * GET /api/v1/admin/teachers?page=1&size=20&keyword=李老师&status=ACTIVE
     * 
     * @param page 页码，默认1
     * @param size 每页数量，默认10
     * @param keyword 搜索关键词，可选
     * @param status 状态筛选，可选
     * @return Result<IPage<UserProfileDTO>> 分页结果，包含教师列表和总数
     */
    @GetMapping("/teachers")  // 处理GET请求
    public Result<IPage<UserProfileDTO>> getTeacherPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        // 记录查询日志
        log.info("分页查询教师: page={}, size={}, keyword={}, status={}", page, size, keyword, status);
        // 调用服务层执行分页查询
        IPage<UserProfileDTO> result = adminService.getTeacherPage(page, size, keyword, status);
        // 返回分页结果
        return Result.success(result);
    }

    /**
     * 🔄 更新用户状态 - 启用/禁用/封禁用户账号
     * 
     * 业务流程：
     * 1. 验证用户ID是否存在
     * 2. 验证目标状态是否合法：ACTIVE/DISABLED/BANNED
     * 3. 检查是否是管理员自己（不能禁用自己）
     * 4. 更新用户状态
     * 5. 如果是封禁，清除该用户的所有登录token
     * 6. 记录审计日志：记录操作管理员、原状态、新状态、原因
     * 7. 异步通知用户（如果被禁用/封禁）
     * 
     * 状态说明：
     * - ACTIVE：正常状态，可以正常登录和使用
     * - DISABLED：禁用状态，不能登录，可以恢复
     * - BANNED：封禁状态，永久禁止登录，一般用于严重违规
     * 
     * 请求示例：
     * PUT /api/v1/admin/users/123/status?status=DISABLED
     * 
     * @param userId 用户ID，从URL路径获取
     * @param status 目标状态，从QueryParam获取
     * @return Result<Void> 无数据返回
     * @throws BusinessException 当尝试禁用自己或状态不合法时抛出
     */
    @PutMapping("/users/{userId}/status")  // 处理PUT请求
    public Result<Void> updateUserStatus(
            @PathVariable Long userId,  // 从URL路径获取用户ID
            @RequestParam String status) {  // 从QueryParam获取目标状态
        // 记录状态更新日志，重要操作必须记录
        log.info("更新用户状态: userId={}, status={}", userId, status);
        // 调用服务层更新状态
        adminService.updateUserStatus(userId, status);
        // 返回成功响应
        return Result.success("更新成功", null);
    }

    /**
     * 🗑️ 删除用户 - 删除违规或不再使用的用户账号
     * 
     * 业务流程：
     * 1. 验证用户ID是否存在
     * 2. 检查是否是管理员自己（不能删除自己）
     * 3. 执行软删除（设置deleted=true，保留数据）
     * 4. 清除用户的所有token，强制退出登录
     * 5. 异步处理关联数据：问题、回答、收藏等
     * 6. 记录审计日志：记录删除原因和操作人
     * 
     * 删除规则：
     * - 使用软删除，不物理删除数据
     * - 不能删除管理员自己
     * - 删除后用户无法登录
     * - 用户的历史数据保留但标记为已删除
     * 
     * 请求示例：
     * DELETE /api/v1/admin/users/123
     * 
     * @param userId 用户ID，从URL路径获取
     * @return Result<Void> 无数据返回
     * @throws BusinessException 当尝试删除自己时抛出
     */
    @DeleteMapping("/users/{userId}")  // 处理DELETE请求
    public Result<Void> deleteUser(@PathVariable Long userId) {
        // 记录删除操作日志，非常重要
        log.info("删除用户: userId={}", userId);
        // 调用服务层删除用户
        adminService.deleteUser(userId);
        // 返回成功响应
        return Result.success("删除成功", null);
    }

    /**
     * 🔑 重置用户密码 - 管理员帮助用户重置忘记的密码
     * 
     * 业务流程：
     * 1. 验证用户ID是否存在
     * 2. 验证新密码格式：长度、复杂度要求
     * 3. 对新密码进行BCrypt加密
     * 4. 更新用户密码
     * 5. 清除该用户的所有token，强制重新登录
     * 6. 记录审计日志（不记录密码内容）
     * 7. 异步发送邮件/短信通知用户密码已被重置
     * 
     * 密码要求：
     * - 长度至少8位
     * - 必须包含大写字母、小写字母、数字
     * - 建议包含特殊字符
     * - 不能与历史密码相同
     * 
     * 安全考虑：
     * - 密码不能记录到日志中
     * - 密码必须加密存储
     * - 重置后强制用户重新登录
     * - 发送通知告知用户密码已变更
     * 
     * 请求示例：
     * PUT /api/v1/admin/users/123/reset-password
     * Body: { "newPassword": "NewPass123!" }
     * 
     * @param userId 用户ID，从URL路径获取
     * @param request 密码重置请求对象，包含新密码
     *                @Valid - 启用参数校验
     * @return Result<Void> 无数据返回
     * @throws BusinessException 当密码格式不合规时抛出
     */
    @PutMapping("/users/{userId}/reset-password")  // 处理PUT请求
    public Result<Void> resetUserPassword(
            @PathVariable Long userId,  // 从URL路径获取用户ID
            @Valid @RequestBody ResetPasswordRequest request) {  // 请求体，启用校验
        // 记录密码重置日志，注意：不记录密码内容，用***代替
        log.info("重置用户密码: userId={}, newPassword=***", userId);
        // 调用服务层重置密码
        adminService.resetUserPassword(userId, request.getNewPassword());
        // 返回成功响应
        return Result.success("密码重置成功", null);
    }

    /**
     * ➕ 创建学生账号 - 管理员批量创建学生账号
     * 
     * 业务流程：
     * 1. 验证请求参数：用户名、密码、姓名、邮箱等
     * 2. 检查用户名、邮箱是否已存在
     * 3. 对密码进行BCrypt加密
     * 4. 创建学生账号，角色设置为STUDENT
     * 5. 初始化用户配置和权限
     * 6. 记录操作日志
     * 7. 异步发送欢迎邮件给新用户
     * 8. 返回创建的用户信息
     * 
     * 使用场景：
     * - 开学时批量导入新生账号
     * - 单个学生账号创建
     * - Excel批量导入学生信息
     * 
     * 请求示例：
     * POST /api/v1/admin/students
     * Body:
     * {
     *     "username": "student001",
     *     "password": "Pass123!",
     *     "realName": "张三",
     *     "email": "zhang@example.com"
     * }
     * 
     * @param request 创建用户请求对象
     *                @Valid - 启用参数校验
     * @return Result<UserProfileDTO> 返回创建的用户信息
     * @throws BusinessException 当用户名或邮箱已存在时抛出
     */
    @PostMapping("/students")  // 处理POST请求
    public Result<UserProfileDTO> createStudent(@Valid @RequestBody CreateUserRequest request) {
        // 记录创建学生账号日志
        log.info("创建学生账号: username={}", request.getUsername());
        // 调用服务层创建学生
        UserProfileDTO created = adminService.createStudent(request);
        // 返回创建的学生信息
        return Result.success("创建成功", created);
    }

    /**
     * ➕ 创建教师账号 - 管理员批量创建教师账号
     * 
     * 业务流程与创建学生类似，区别在于：
     * - 角色设置为TEACHER
     * - 需要额外设置教师特有信息：职称、教研组等
     * - 教师默认拥有更多权限：回答问题、查看统计等
     * 
     * 请求示例：
     * POST /api/v1/admin/teachers
     * Body:
     * {
     *     "username": "teacher001",
     *     "password": "Pass123!",
     *     "realName": "李老师",
     *     "email": "li@example.com"
     * }
     * 
     * @param request 创建用户请求对象
     *                @Valid - 启用参数校验
     * @return Result<UserProfileDTO> 返回创建的用户信息
     * @throws BusinessException 当用户名或邮箱已存在时抛出
     */
    @PostMapping("/teachers")  // 处理POST请求
    public Result<UserProfileDTO> createTeacher(@Valid @RequestBody CreateUserRequest request) {
        // 记录创建教师账号日志
        log.info("创建教师账号: username={}", request.getUsername());
        // 调用服务层创建教师
        UserProfileDTO created = adminService.createTeacher(request);
        // 返回创建的教师信息
        return Result.success("创建成功", created);
    }
}

