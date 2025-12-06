package com.qasystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * CreateUserRequest - 创建用户请求数据传输对象
 * 
 * 🎯 作用：管理员创建用户时从前端接收的数据
 * 这是管理员专用的接口，与普通用户注册（RegisterRequest）不同。
 * 管理员可以批量创建学生、教师账号。
 * 
 * 📝 字段分类：
 * 1. 公共字段：username, password, realName, email, phone, gender
 * 2. 学生字段：studentNo, major, className, grade, college
 * 3. 教师字段：teacherNo, title, research, office
 * 
 * 💬 请求示例（管理员创建学生）：
 * {
 *   "username": "student001",
 *   "password": "temp123",
 *   "realName": "李四",
 *   "email": "lisi@school.edu.cn",
 *   "phone": "13900139000",
 *   "gender": "MALE",
 *   "studentNo": "2024001",
 *   "major": "软件工程",
 *   "className": "软工1班",
 *   "grade": 2024,
 *   "college": "软件学院"
 * }
 * 
 * 💡 使用场景：
 * - 管理员在后台管理页面批量导入学生/教师账号
 * - 调用 POST /api/admin/users 接口
 * - 创建后用户首次登录需要修改密码
 * 
 * ⚠️ 注意：与RegisterRequest的区别
 * 1. CreateUserRequest由管理员使用，需要管理员权限
 * 2. RegisterRequest由普通用户自己注册使用，无需权限
 * 3. CreateUserRequest可能没有那么多验证规则，管理员批量创建时更灵活
 */
@Data
public class CreateUserRequest {
    
    /** 用户名（必填） */
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    /** 密码（必填），管理员可以设置临时密码，用户首次登录后修改 */
    @NotBlank(message = "密码不能为空")
    private String password;
    
    /** 真实姓名（必填） */
    @NotBlank(message = "真实姓名不能为空")
    private String realName;
    
    /** 邮箱地址 */
    @Email(message = "邮箱格式不正确")
    private String email;
    
    /** 手机号码 */
    private String phone;
    
    /** 性别：MALE/FEMALE/OTHER */
    private String gender;
    
    // ==================== 学生特定字段 ====================
    
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
    
    // ==================== 教师特定字段 ====================
    
    /** 工号 */
    private String teacherNo;
    
    /** 职称 */
    private String title;
    
    /** 研究方向 */
    private String research;
    
    /** 办公室位置 */
    private String office;
}

