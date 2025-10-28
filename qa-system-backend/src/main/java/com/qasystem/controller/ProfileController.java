package com.qasystem.controller;

import com.qasystem.common.response.Result;
import com.qasystem.dto.ChangePasswordRequest;
import com.qasystem.dto.UpdateProfileRequest;
import com.qasystem.dto.UserProfileDTO;
import com.qasystem.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 个人中心控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    /**
     * 获取当前用户个人信息
     */
    @GetMapping("/me")
    public Result<UserProfileDTO> getCurrentUserProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("获取用户信息: userId={}", userId);
        
        UserProfileDTO profile = profileService.getCurrentUserProfile(userId);
        return Result.success(profile);
    }

    /**
     * 更新个人信息
     */
    @PutMapping("/me")
    public Result<UserProfileDTO> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("更新用户信息: userId={}", userId);
        
        UserProfileDTO profile = profileService.updateProfile(userId, request);
        return Result.success("更新成功", profile);
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("修改密码: userId={}", userId);
        
        profileService.changePassword(userId, request);
        return Result.success("密码修改成功", null);
    }
}

