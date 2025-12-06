package com.qasystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qasystem.dto.CreateUserRequest;
import com.qasystem.dto.UserProfileDTO;

import java.util.Map;

/**
 * AdminService - 管理员服务接口
 * 
 * 🎯 作用：管理员后台管理功能
 * 就像一个“系统管理员”，负责管理用户、查看统计数据、维护系统等。
 * 
 * 📝 主要功能：
 * 1. 数据统计：查看系统整体数据
 * 2. 用户管理：查询、创建、删除用户
 * 3. 账户管理：封禁/解封、重置密码
 * 
 * 💡 使用场景：
 * - 管理员登录后台管理系统
 * - 查看系统数据统计
 * - 管理学生和教师账号
 */
public interface AdminService {
    
    /**
     * 获取系统数据统计
     * 
     * 🎯 功能：查询系统整体的关键指标
     * 就像查看企业的“数据看板”，一眼知道系统运营情况。
     * 
     * @return 统计数据，包含：
     *         - totalUsers: 总用户数
     *         - totalStudents: 学生总数
     *         - totalTeachers: 教师总数
     *         - totalQuestions: 问题总数
     *         - totalAnswers: 回答总数
     *         - todayQuestions: 今日新增问题
     *         - pendingQuestions: 待解决问题数
     * 
     * 💬 使用场景：
     * - 管理员登录后台，首页展示系统概览
     * - 查看各项数据指标和趋势
     */
    Map<String, Object> getStatistics();
    
    /**
     * 分页查询学生列表
     * 
     * 🎯 功能：查询所有学生账号，支持搜索和筛选
     * 
     * @param page 页码
     * @param size 每页数量
     * @param keyword 搜索关键词，在用户名、姓名、学号中搜索
     * @param status 用户状态，ACTIVE/INACTIVE/BANNED
     * @return 学生列表分页结果
     * 
     * 💬 使用场景：
     * - 管理员在“学生管理”页面查看所有学生
     * - 搜索某个学生的信息
     * - 筛选被封禁的学生账号
     */
    IPage<UserProfileDTO> getStudentPage(Integer page, Integer size, String keyword, String status);
    
    /**
     * 分页查询教师列表
     * 
     * 🎯 功能：查询所有教师账号，支持搜索和筛选
     * 
     * @param page 页码
     * @param size 每页数量
     * @param keyword 搜索关键词，在用户名、姓名、工号中搜索
     * @param status 用户状态，ACTIVE/INACTIVE/BANNED
     * @return 教师列表分页结果
     * 
     * 💬 使用场景：
     * - 管理员在“教师管理”页面查看所有教师
     * - 搜索某个教师的信息
     */
    IPage<UserProfileDTO> getTeacherPage(Integer page, Integer size, String keyword, String status);
    
    /**
     * 更新用户状态
     * 
     * 🎯 功能：管理员封禁或解封用户账号
     * 就像“冻结账户”或“解除冻结”。
     * 
     * @param userId 用户ID
     * @param status 新状态：ACTIVE-正常，INACTIVE-未激活，BANNED-已封禁
     * 
     * 💬 使用场景：
     * - 封禁违规用户：设置为BANNED，用户无法登录
     * - 解封用户：设置为ACTIVE，恢复正常使用
     */
    void updateUserStatus(Long userId, String status);
    
    /**
     * 删除用户
     * 
     * 🎯 功能：管理员永久删除用户账号
     * 注意：谨慎操作，建议使用“封禁”而非删除。
     * 
     * @param userId 用户ID
     * 
     * 💬 使用场景：
     * - 删除测试账号或废弃账号
     * - 建议：正式环境下使用“封禁”功能，保留数据用于审计
     */
    void deleteUser(Long userId);
    
    /**
     * 重置用户密码
     * 
     * 🎯 功能：管理员帮助用户重置密码
     * 不需要验证原密码，管理员可以直接设置新密码。
     * 
     * @param userId 用户ID
     * @param newPassword 新密码
     * 
     * 💬 使用场景：
     * - 用户忘记密码且邮箱无法接收重置邮件
     * - 管理员在后台手动设置一个临时密码
     * - 通知用户后，用户登录后再自己修改
     * 
     * ⚠️ 注意：
     * - 与用户自己修改密码（ChangePassword）不同
     * - 这里是管理员强制重置，不需要原密码
     */
    void resetUserPassword(Long userId, String newPassword);
    
    /**
     * 创建学生账号
     * 
     * 🎯 功能：管理员批量创建学生账号
     * 就像学校开学时批量录入新生信息。
     * 
     * @param request 创建用户请求，包含学生的所有信息
     * @return 创建成功的学生信息
     * 
     * 💬 使用场景：
     * - 管理员导入学生名单Excel表格
     * - 或者手动一个一个添加学生账号
     * - 设置初始密码，学生首次登录后修改
     */
    UserProfileDTO createStudent(CreateUserRequest request);
    
    /**
     * 创建教师账号
     * 
     * 🎯 功能：管理员批量创建教师账号
     * 就像学校人事部门为新入职教师开通账号。
     * 
     * @param request 创建用户请求，包含教师的所有信息
     * @return 创建成功的教师信息
     * 
     * 💬 使用场景：
     * - 管理员导入教师名单Excel表格
     * - 或者手动添加教师账号
     * - 设置初始密码，教师首次登录后修改
     */
    UserProfileDTO createTeacher(CreateUserRequest request);
}

