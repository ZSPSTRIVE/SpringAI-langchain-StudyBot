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
 * 管理员控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * 获取数据统计
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        log.info("获取数据统计");
        Map<String, Object> statistics = adminService.getStatistics();
        return Result.success(statistics);
    }

    /**
     * 分页查询学生列表
     */
    @GetMapping("/students")
    public Result<IPage<UserProfileDTO>> getStudentPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        log.info("分页查询学生: page={}, size={}, keyword={}, status={}", page, size, keyword, status);
        IPage<UserProfileDTO> result = adminService.getStudentPage(page, size, keyword, status);
        return Result.success(result);
    }

    /**
     * 分页查询教师列表
     */
    @GetMapping("/teachers")
    public Result<IPage<UserProfileDTO>> getTeacherPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        log.info("分页查询教师: page={}, size={}, keyword={}, status={}", page, size, keyword, status);
        IPage<UserProfileDTO> result = adminService.getTeacherPage(page, size, keyword, status);
        return Result.success(result);
    }

    /**
     * 更新用户状态
     */
    @PutMapping("/users/{userId}/status")
    public Result<Void> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam String status) {
        log.info("更新用户状态: userId={}, status={}", userId, status);
        adminService.updateUserStatus(userId, status);
        return Result.success("更新成功", null);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/users/{userId}")
    public Result<Void> deleteUser(@PathVariable Long userId) {
        log.info("删除用户: userId={}", userId);
        adminService.deleteUser(userId);
        return Result.success("删除成功", null);
    }

    /**
     * 重置用户密码（手动输入）
     */
    @PutMapping("/users/{userId}/reset-password")
    public Result<Void> resetUserPassword(
            @PathVariable Long userId,
            @Valid @RequestBody ResetPasswordRequest request) {
        log.info("重置用户密码: userId={}, newPassword=***", userId);
        adminService.resetUserPassword(userId, request.getNewPassword());
        return Result.success("密码重置成功", null);
    }

    /**
     * 创建学生账号
     */
    @PostMapping("/students")
    public Result<UserProfileDTO> createStudent(@Valid @RequestBody CreateUserRequest request) {
        log.info("创建学生账号: username={}", request.getUsername());
        UserProfileDTO created = adminService.createStudent(request);
        return Result.success("创建成功", created);
    }

    /**
     * 创建教师账号
     */
    @PostMapping("/teachers")
    public Result<UserProfileDTO> createTeacher(@Valid @RequestBody CreateUserRequest request) {
        log.info("创建教师账号: username={}", request.getUsername());
        UserProfileDTO created = adminService.createTeacher(request);
        return Result.success("创建成功", created);
    }
}

