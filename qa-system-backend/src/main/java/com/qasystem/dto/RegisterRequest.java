package com.qasystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 注册请求DTO
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "用户名只能包含字母、数字、下划线，长度4-20位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{6,20}$", 
             message = "密码必须包含字母和数字，长度6-20位")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "^(STUDENT|TEACHER)$", message = "角色必须是STUDENT或TEACHER")
    private String role;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    private String phone;

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
