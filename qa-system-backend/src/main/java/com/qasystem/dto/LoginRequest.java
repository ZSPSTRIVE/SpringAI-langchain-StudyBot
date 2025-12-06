package com.qasystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * LoginRequest - 登录请求数据传输对象
 * 
 * 🎯 作用：用户登录时从前端接收的数据对象
 * 就像一张"登录表单"，用户填写用户名和密码后提交给后端。
 * 
 * 📝 字段说明：
 * - username: 用户名（必填，不能为空）
 * - password: 密码（必填，不能为空）
 * 
 * 💬 请求示例：
 * {
 *   "username": "zhangsan",
 *   "password": "123456"
 * }
 * 
 * ⚠️ 注意：密码应该在前端加密或HTTPS传输
 */
@Data
public class LoginRequest {

    /**
     * 用户名
     * 必填字段，不能为空或只包含空格
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     * 必填字段，不能为空或只包含空格
     * 后端会与数据库中的加密密码进行比对
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}

