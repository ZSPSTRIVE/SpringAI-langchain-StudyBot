package com.qasystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qasystem.dto.CreateUserRequest;
import com.qasystem.dto.UserProfileDTO;

import java.util.Map;

/**
 * 管理员Service接口
 */
public interface AdminService {
    
    /**
     * 获取数据统计
     */
    Map<String, Object> getStatistics();
    
    /**
     * 分页查询学生
     */
    IPage<UserProfileDTO> getStudentPage(Integer page, Integer size, String keyword, String status);
    
    /**
     * 分页查询教师
     */
    IPage<UserProfileDTO> getTeacherPage(Integer page, Integer size, String keyword, String status);
    
    /**
     * 更新用户状态
     */
    void updateUserStatus(Long userId, String status);
    
    /**
     * 删除用户
     */
    void deleteUser(Long userId);
    
    /**
     * 重置用户密码（手动输入）
     */
    void resetUserPassword(Long userId, String newPassword);
    
    /**
     * 创建学生账号
     */
    UserProfileDTO createStudent(CreateUserRequest request);
    
    /**
     * 创建教师账号
     */
    UserProfileDTO createTeacher(CreateUserRequest request);
}

