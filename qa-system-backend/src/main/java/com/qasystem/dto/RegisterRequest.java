package com.qasystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * RegisterRequest - 注册请求数据传输对象
 * 
 * 🎯 作用：用户注册时从前端接收的数据
 * 就像一张“入学登记表”，包含学生或教师的所有信息。
 * 
 * 📝 字段分类：
 * 1. 公共字段：username, password, realName, role, email, phone
 * 2. 学生字段：studentNo, major, className, grade, college
 * 3. 教师字段：teacherNo, title, research, office
 * 
 * 💬 请求示例（学生）：
 * {
 *   "username": "zhangsan",
 *   "password": "abc123",
 *   "realName": "张三",
 *   "role": "STUDENT",
 *   "email": "zhangsan@qq.com",
 *   "phone": "13800138000",
 *   "studentNo": "2021001",
 *   "major": "计算机科学",
 *   "className": "计科1班",
 *   "grade": 2021,
 *   "college": "计算机学院"
 * }
 */
@Data
public class RegisterRequest {

    /**
     * 用户名（必填）
     * 规则：4-20位，只能包含字母、数字、下划线
     * 示例：zhangsan, student_2021, Teacher123
     */
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "用户名只能包含字母、数字、下划线，长度4-20位")
    private String username;

    /**
     * 密码（必填）
     * 规则：6-20位，必须包含字母和数字，可包含特殊字符@$!%*#?&
     * 示例：abc123, Pass123!, MyPwd2024
     */
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{6,20}$", 
             message = "密码必须包含字母和数字，长度6-20位")
    private String password;

    /**
     * 真实姓名（必填）
     * 中文姓名或英文姓名
     * 示例：张三, Zhang San
     */
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    /**
     * 用户角色（必填）
     * 只能是STUDENT（学生）或TEACHER（教师）
     * 注意：不同角色需要填写不同的额外字段
     */
    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "^(STUDENT|TEACHER)$", message = "角色必须是STUDENT或TEACHER")
    private String role;

    /**
     * 邮箱地址（必填）
     * 用于找回密码、接收通知
     * 示例：zhangsan@qq.com, teacher@school.edu.cn
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号码（可选）
     * 示例：13800138000
     */
    private String phone;

    // ==================== 学生特定字段（role=STUDENT时填写） ====================
    
    /**
     * 学号（学生必填）
     * 示例：2021001, 20210101
     */
    private String studentNo;
    
    /**
     * 专业（学生可选）
     * 示例：计算机科学与技术, 软件工程
     */
    private String major;
    
    /**
     * 班级（学生可选）
     * 示例：计科1班, 软工2班
     */
    private String className;
    
    /**
     * 年级（学生可选）
     * 示例：2021, 2022
     */
    private Integer grade;
    
    /**
     * 学院（学生可选）
     * 示例：计算机学院, 软件学院
     */
    private String college;

    // ==================== 教师特定字段（role=TEACHER时填写） ====================
    
    /**
     * 工号（教师必填）
     * 示例：T2021001, T001
     */
    private String teacherNo;
    
    /**
     * 职称（教师可选）
     * 示例：助教, 讲师, 副教授, 教授
     */
    private String title;
    
    /**
     * 研究方向（教师可选）
     * 示例：人工智能, 数据库系统
     */
    private String research;
    
    /**
     * 办公室位置（教师可选）
     * 示例：理科楼301, A栋203
     */
    private String office;
}
