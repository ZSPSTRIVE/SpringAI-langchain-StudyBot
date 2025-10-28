package com.qasystem.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户个人信息DTO
 */
@Data
public class UserProfileDTO {
    
    private Long userId;
    private String username;
    private String realName;
    private String role;
    private String email;
    private String phone;
    private String avatar;
    private String gender;
    private String status;
    
    // 学生特定信息
    private String studentNo;
    private String major;
    private String className;
    private Integer grade;
    private String college;
    
    // 教师特定信息
    private String teacherNo;
    private String title;
    private String research;
    private String office;
    private String bio;
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

