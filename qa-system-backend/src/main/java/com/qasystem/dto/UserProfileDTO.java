package com.qasystem.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * UserProfileDTO - 用户个人信息数据传输对象
 * 
 * 🎯 作用：返回给前端的用户完整资料
 * 用于在个人中心页面展示用户的所有信息，包括基本信息和角色特定信息。
 * 
 * 📝 字段分类：
 * 1. 公共信息：userId, username, realName, role, email, phone, avatar, gender, status
 * 2. 学生信息：studentNo, major, className, grade, college
 * 3. 教师信息：teacherNo, title, research, office, bio
 * 4. 时间信息：createTime, updateTime
 * 
 * 💬 响应示例（学生）：
 * {
 *   "userId": 10,
 *   "username": "zhangsan",
 *   "realName": "张三",
 *   "role": "STUDENT",
 *   "email": "zhangsan@qq.com",
 *   "phone": "13800138000",
 *   "avatar": "/uploads/avatar/xxx.jpg",
 *   "gender": "MALE",
 *   "status": "ACTIVE",
 *   "studentNo": "2021001",
 *   "major": "计算机科学",
 *   "className": "计科1班",
 *   "grade": 2021,
 *   "college": "计算机学院",
 *   "createTime": "2024-09-01T10:00:00",
 *   "updateTime": "2024-11-17T15:30:00"
 * }
 */
@Data
public class UserProfileDTO {
    
    /** 用户ID */
    private Long userId;
    
    /** 用户名，登录名称 */
    private String username;
    
    /** 真实姓名 */
    private String realName;
    
    /** 用户角色：STUDENT/TEACHER/ADMIN */
    private String role;
    
    /** 邮箱地址 */
    private String email;
    
    /** 手机号码 */
    private String phone;
    
    /** 头像图URL */
    private String avatar;
    
    /** 性别：MALE-男，FEMALE-女，OTHER-其他 */
    private String gender;
    
    /** 
     * 用户状态
     * ACTIVE - 正常活跃
     * INACTIVE - 未激活
     * BANNED - 已封禁
     */
    private String status;
    
    // ==================== 学生特定信息 ====================
    
    /** 学号 */
    private String studentNo;
    
    /** 专业 */
    private String major;
    
    /** 班级 */
    private String className;
    
    /** 年级 */
    private Integer grade;
    
    /** 学院 */
    private String college;
    
    // ==================== 教师特定信息 ====================
    
    /** 工号 */
    private String teacherNo;
    
    /** 职称：助教、讲师、副教授、教授 */
    private String title;
    
    /** 研究方向 */
    private String research;
    
    /** 办公室位置 */
    private String office;
    
    /** 个人简介 */
    private String bio;
    
    // ==================== 时间信息 ====================
    
    /** 账号创建时间（注册时间） */
    private LocalDateTime createTime;
    
    /** 信息更新时间（最后修改资料的时间） */
    private LocalDateTime updateTime;
}

