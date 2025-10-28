package com.qasystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建用户请求DTO
 */
@Data
public class CreateUserRequest {
    
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    @NotBlank(message = "真实姓名不能为空")
    private String realName;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    private String phone;
    private String gender;
    
    // 学生特定字段
    private String studentNo;
    private String major;
    private String className;
    private Integer grade;
    private String college;
    
    // 教师特定字段
    private String teacherNo;
    private String title;
    private String research;
    private String office;
}

