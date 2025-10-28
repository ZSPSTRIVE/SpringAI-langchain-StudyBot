package com.qasystem.service;

import com.qasystem.dto.ChangePasswordRequest;
import com.qasystem.dto.UpdateProfileRequest;
import com.qasystem.dto.UserProfileDTO;

/**
 * 个人中心服务接口
 */
public interface ProfileService {

    /**
     * 获取当前用户个人信息
     */
    UserProfileDTO getCurrentUserProfile(Long userId);

    /**
     * 更新个人信息
     */
    UserProfileDTO updateProfile(Long userId, UpdateProfileRequest request);

    /**
     * 修改密码
     */
    void changePassword(Long userId, ChangePasswordRequest request);
}

