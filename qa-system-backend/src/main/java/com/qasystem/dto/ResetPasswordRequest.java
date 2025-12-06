package com.qasystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * ResetPasswordRequest - 重置密码请求数据传输对象
 * 
 * 🎯 作用：用户忘记密码后重置密码时使用
 * 跟修改密码（ChangePasswordRequest）不同，重置密码不需要原密码，
 * 但需要通过邮箱验证或管理员重置。
 * 
 * 📝 字段说明：
 * - newPassword: 新密码，6-20位
 * 
 * 💬 请求示例：
 * {
 *   "newPassword": "newPass123"
 * }
 * 
 * 💡 使用场景：
 * 方式1：邮箱重置
 * 1. 用户点击“忘记密码”，输入邮箱
 * 2. 系统发送重置链接到邮箱（包含token）
 * 3. 用户点击链接，跳转到重置密码页面
 * 4. 输入新密码后调用 POST /api/auth/reset-password?token=xxx
 * 
 * 方式2：管理员重置
 * 1. 管理员在后台管理页面查找用户
 * 2. 点击“重置密码”，输入新密码
 * 3. 调用 POST /api/admin/users/{userId}/reset-password
 * 
 * ⚠️ 注意：与ChangePasswordRequest的区别
 * - ChangePasswordRequest: 用户主动修改，需要原密码验证
 * - ResetPasswordRequest: 忘记密码后重置，需要邮箱验证或管理员权限
 */
@Data
public class ResetPasswordRequest {
    
    /**
     * 新密码（必填）
     * 长度限制：6-20位
     * 示例：newPass123, MyPassword2024
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    private String newPassword;
}

