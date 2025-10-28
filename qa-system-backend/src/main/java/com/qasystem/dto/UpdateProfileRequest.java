package com.qasystem.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

/**
 * 更新个人信息请求DTO
 */
@Data
public class UpdateProfileRequest {
    
    private String realName;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    private String phone;
    private String avatar;
    private String gender;
    
    // 学生特定信息
    private String major;
    private String className;
    private Integer grade;
    private String college;
    
    // 教师特定信息
    private String title;
    private String research;
    private String office;
    private String bio;
}

