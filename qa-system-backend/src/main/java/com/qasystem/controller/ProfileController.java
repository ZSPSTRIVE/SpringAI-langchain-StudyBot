package com.qasystem.controller;

import com.qasystem.common.response.Result;
import com.qasystem.dto.ChangePasswordRequest;
import com.qasystem.dto.UpdateProfileRequest;
import com.qasystem.dto.UserProfileDTO;
import com.qasystem.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 👤 个人中心控制器 - 用户个人信息管理和账号设置
 * 
 * 📖 功能说明：
 * 个人中心模块提供用户对自己账号的管理功能。
 * 本控制器主要功能包括：
 * 1. 信息查询 - 查看自己的详细信息
 * 2. 信息更新 - 修改姓名、邮箱、手机、头像等
 * 3. 密码修改 - 自助修改登录密码
 * 
 * 🔒 权限控制：
 * - 所有接口需要用户登录后访问
 * - 用户只能查看和修改自己的信息
 * - 不能修改其他用户的信息
 * 
 * 🌍 RESTful 设计：
 * GET /api/v1/profile/me         获取当前用户信息
 * PUT /api/v1/profile/me         更新个人信息
 * PUT /api/v1/profile/password   修改密码
 * 
 * 📝 注意事项：
 * - 敏感信息修改需要二次验证（如手机号、邮箱）
 * - 密码修改需要验证旧密码
 * - 重要信息变更需要记录审计日志
 * - 头像上传需要限制文件大小和格式
 * 
 * @author 师生答疑系统开发团队
 * @since 1.0.0
 */
@Slf4j  // 自动生成日志对象log，用于记录用户操作
@RestController  // 标识这是一个REST控制器，返回JSON数据
@RequestMapping("/api/v1/profile")  // 定义个人中心接口的基础路径
@RequiredArgsConstructor  // 为final字段生成构造函数，实现依赖注入
public class ProfileController {

    // 个人中心服务层接口，处理所有用户信息相关的业务逻辑
    // final确保注入后不可修改
    private final ProfileService profileService;

    /**
     * 🔍 获取当前用户个人信息 - 查看自己的详细资料
     * 
     * 业务流程：
     * 1. 从认证对象获取当前登录用户ID
     * 2. 根据用户ID查询数据库获取用户信息
     * 3. 组装用户信息：基本信息 + 统计数据
     * 4. 过滤敏感信息（密码、删除标记等）
     * 5. 返回用户资料
     * 
     * 返回信息包含：
     * - 基本信息：用户名、姓名、邮箱、手机、头像、角色
     * - 状态信息：账号状态、注册时间、最后登录时间
     * - 统计数据：
     *   * 学生：提问数、被采纳数、收藏数
     *   * 教师：回答数、被采纳数、信誉分
     * 
     * 使用场景：
     * - 用户打开个人中心页面
     * - 前端需要显示用户信息
     * - 获取用户的统计数据
     * 
     * 请求示例：
     * GET /api/v1/profile/me
     * Headers:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiI...
     * 
     * 成功响应示例：
     * {
     *     "code": 200,
     *     "message": "成功",
     *     "data": {
     *         "id": 123,
     *         "username": "student001",
     *         "realName": "张三",
     *         "email": "zhang@example.com",
     *         "phone": "138****5678",
     *         "avatar": "http://cdn.example.com/avatar/123.jpg",
     *         "role": "STUDENT",
     *         "status": "ACTIVE",
     *         "createdAt": "2024-09-01T08:00:00",
     *         "lastLoginAt": "2024-11-17T10:00:00",
     *         "statistics": {
     *             "questionCount": 15,
     *             "acceptedCount": 8,
     *             "collectionCount": 23
     *         }
     *     }
     * }
     * 
     * @param authentication 认证对象，包含当前登录用户的ID
     *                       Spring Security自动注入，无需手动传递
     * @return Result<UserProfileDTO> 统一响应对象，包含用户完整信息
     */
    @GetMapping("/me")  // 处理GET请求，完整路径：/api/v1/profile/me
    public Result<UserProfileDTO> getCurrentUserProfile(Authentication authentication) {
        // 从认证对象获取当前登录用户的ID
        // Spring Security在用户登录后会自动将用户ID存入认证对象
        Long userId = (Long) authentication.getPrincipal();
        
        // 记录获取用户信息的日志
        // 用于监控用户活跃度和接口调用频率
        log.info("获取用户信息: userId={}", userId);
        
        // 调用服务层查询用户信息
        // 服务层会从数据库查询并组装完整的用户信息
        UserProfileDTO profile = profileService.getCurrentUserProfile(userId);
        
        // 返回用户信息
        // 前端收到后可以显示在个人中心页面
        return Result.success(profile);
    }

    /**
     * ✏️ 更新个人信息 - 用户修改自己的资料
     * 
     * 业务流程：
     * 1. 从认证对象获取当前用户ID
     * 2. 验证请求参数：姓名、邮箱、手机等格式
     * 3. 检查新邮箱/手机是否被其他用户占用
     * 4. 如果修改敏感信息（邮箱/手机），需要验证码验证
     * 5. 更新用户信息到数据库
     * 6. 清除用户信息缓存
     * 7. 记录审计日志（重要信息变更）
     * 8. 返回更新后的用户信息
     * 
     * 可以修改的字段：
     * - 姓名：realName（有长度限制）
     * - 邮箱：email（需验证码验证，不能重复）
     * - 手机：phone（需验证码验证，不能重复）
     * - 头像：avatar（需先上传图片获取URL）
     * - 简介：bio（有长度限制）
     * 
     * 不能修改的字段：
     * - 用户名：username（注册后不可修改）
     * - 角色：role（由管理员管理）
     * - 状态：status（由管理员管理）
     * - 注册时间：createdAt（系统生成）
     * 
     * 安全考虑：
     * - 修改邮箱/手机需要验证码验证
     * - 防止恶意用户占用别人的邮箱/手机
     * - 记录重要信息的变更日志
     * 
     * 请求示例：
     * PUT /api/v1/profile/me
     * Headers:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiI...
     *   Content-Type: application/json
     * Body:
     * {
     *     "realName": "张三丰",
     *     "bio": "热爱学习的计算机科学专业学生"
     * }
     * 
     * 成功响应示例：
     * {
     *     "code": 200,
     *     "message": "更新成功",
     *     "data": {
     *         "id": 123,
     *         "username": "student001",
     *         "realName": "张三丰",
     *         "bio": "热爱学习的计算机科学专业学生",
     *         ...
     *     }
     * }
     * 
     * @param authentication 认证对象，包含当前用户ID
     * @param request 更新请求对象，包含要修改的字段
     *                @Valid - 启用参数校验
     *                @RequestBody - 从请求体获取JSON数据
     * @return Result<UserProfileDTO> 统一响应对象，包含更新后的用户信息
     * @throws BusinessException 当邮箱/手机已被占用或格式不正确时抛出
     */
    @PutMapping("/me")  // 处理PUT请求，完整路径：/api/v1/profile/me
    public Result<UserProfileDTO> updateProfile(
            Authentication authentication,  // 认证对象，自动注入
            @Valid @RequestBody UpdateProfileRequest request) {  // 请求体，启用校验
        // 从认证对象获取当前用户ID
        // 确保用户只能修改自己的信息
        Long userId = (Long) authentication.getPrincipal();
        
        // 记录更新操作日志
        // 重要操作需要记录日志用于审计
        log.info("更新用户信息: userId={}", userId);
        
        // 调用服务层更新用户信息
        // 服务层会验证数据、更新数据库、清除缓存
        UserProfileDTO profile = profileService.updateProfile(userId, request);
        
        // 返回更新后的用户信息
        // 前端可以直接更新界面显示
        return Result.success("更新成功", profile);
    }

    /**
     * 🔒 修改密码 - 用户自助修改登录密码
     * 
     * 业务流程：
     * 1. 从认证对象获取当前用户ID
     * 2. 验证旧密码是否正确（防止他人恶意修改）
     * 3. 验证新密码格式：长度、复杂度要求
     * 4. 检查新密码与旧密码是否相同
     * 5. 检查新密码是否与历史密码重复
     * 6. 对新密码进行BCrypt加密
     * 7. 更新数据库中的密码
     * 8. 清除该用户的所有token（除当前会话），其他设备需重新登录
     * 9. 记录审计日志（不记录密码内容）
     * 10. 异步发送邮件/短信通知用户密码已变更
     * 
     * 密码要求：
     * - 长度至少8位
     * - 必须包含大写字母、小写字母、数字
     * - 建议包含特殊字符
     * - 不能与旧密码相同
     * - 不能与最近3次历史密码相同
     * 
     * 安全考虑：
     * - 必须验证旧密码，防止他人恶意修改
     * - 密码不能记录到日志中
     * - 密码必须加密存储
     * - 修改后其他设备需重新登录（防止账号被盗）
     * - 发送通知告知用户密码已变更
     * 
     * 请求示例：
     * PUT /api/v1/profile/password
     * Headers:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiI...
     *   Content-Type: application/json
     * Body:
     * {
     *     "oldPassword": "OldPass123!",
     *     "newPassword": "NewPass456!",
     *     "confirmPassword": "NewPass456!"
     * }
     * 
     * 成功响应：
     * {
     *     "code": 200,
     *     "message": "密码修改成功",
     *     "data": null
     * }
     * 
     * 错误响应示例：
     * {
     *     "code": 400,
     *     "message": "旧密码不正确",
     *     "data": null
     * }
     * 
     * ⚠️ 注意事项：
     * - 密码修改成功后，其他设备会被强制退出
     * - 建议前端在修改前提示用户确认
     * - 修改后会收到邮件/短信通知
     * 
     * @param authentication 认证对象，包含当前用户ID
     * @param request 密码修改请求对象
     *                @Valid - 启用参数校验
     *                @RequestBody - 从请求体获取JSON数据
     *                包含字段：oldPassword(旧密码), newPassword(新密码), confirmPassword(确认密码)
     * @return Result<Void> 无数据返回，只返回操作结果
     * @throws BusinessException 当旧密码错误或新密码不符合要求时抛出
     */
    @PutMapping("/password")  // 处理PUT请求，完整路径：/api/v1/profile/password
    public Result<Void> changePassword(
            Authentication authentication,  // 认证对象，自动注入
            @Valid @RequestBody ChangePasswordRequest request) {  // 请求体，启用校验
        // 从认证对象获取当前用户ID
        // 确保用户只能修改自己的密码
        Long userId = (Long) authentication.getPrincipal();
        
        // 记录密码修改操作日志
        // 非常重要：注意不记录密码内容，只记录操作
        log.info("修改密码: userId={}", userId);
        
        // 调用服务层修改密码
        // 服务层会验证旧密码、加密新密码、清除token、发送通知
        profileService.changePassword(userId, request);
        
        // 返回成功响应
        // 前端收到后应提示用户密码已修改，其他设备需重新登录
        return Result.success("密码修改成功", null);
    }
}

