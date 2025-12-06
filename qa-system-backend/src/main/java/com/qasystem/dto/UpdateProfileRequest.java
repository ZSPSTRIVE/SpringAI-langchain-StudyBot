package com.qasystem.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

/**
 * UpdateProfileRequest - 更新个人信息请求数据传输对象
 * 
 * 🎯 作用：用户更新个人资料时从前端接收的数据
 * 就像用户在"个人中心"页面编辑资料后保存。
 * 注意：这里不包含用户名、密码、角色等敏感信息，这些需要单独的接口修改。
 * 
 * 📝 字段分类：
 * 1. 公共信息：realName, email, phone, avatar, gender
 * 2. 学生信息：major, className, grade, college
 * 3. 教师信息：title, research, office, bio
 * 
 * 💬 请求示例（学生）：
 * {
 *   "realName": "张三",
 *   "email": "zhangsan@qq.com",
 *   "phone": "13800138000",
 *   "avatar": "/uploads/avatar/new.jpg",
 *   "gender": "MALE",
 *   "major": "软件工程",
 *   "className": "软工1班",
 *   "grade": 2021,
 *   "college": "软件学院"
 * }
 * 
 * 💡 使用场景：
 * - 用户在个人中心修改资料后点击"保存"
 * - 前端会调用 PUT /api/user/profile 接口
 * - 所有字段均为可选，只更新填写了的字段
 */
@Data
public class UpdateProfileRequest {
    
    /** 真实姓名 */
    private String realName;
    
    /** 邮箱地址，需要验证格式 */
    @Email(message = "邮箱格式不正确")
    private String email;
    
    /** 手机号码 */
    private String phone;
    
    /** 头像图URL，需要先通过文件上传接口上传 */
    private String avatar;
    
    /** 性别：MALE-男，FEMALE-女，OTHER-其他 */
    private String gender;
    
    // ==================== 学生特定信息 ====================
    
    /** 专业 */
    private String major;
    
    /** 班级 */
    private String className;
    
    /** 年级 */
    private Integer grade;
    
    /** 学院 */
    private String college;
    
    // ==================== 教师特定信息 ====================
    
    /** 职称：助教、讲师、副教授、教授 */
    private String title;
    
    /** 研究方向 */
    private String research;
    
    /** 办公室位置 */
    private String office;
    
    /** 个人简介，教师可以填写个人研究成果、教学经历等 */
    private String bio;
}

