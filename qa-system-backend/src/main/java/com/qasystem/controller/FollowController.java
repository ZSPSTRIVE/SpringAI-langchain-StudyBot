package com.qasystem.controller;

import com.qasystem.common.response.Result;
import com.qasystem.dto.UserProfileDTO;
import com.qasystem.service.FollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 关注控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    /**
     * 关注教师
     */
    @PostMapping("/teacher/{teacherId}")
    public Result<Void> followTeacher(
            @PathVariable Long teacherId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("关注教师: userId={}, teacherId={}", userId, teacherId);
        
        followService.followTeacher(userId, teacherId);
        return Result.success("关注成功", null);
    }

    /**
     * 取消关注
     */
    @DeleteMapping("/teacher/{teacherId}")
    public Result<Void> unfollowTeacher(
            @PathVariable Long teacherId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("取消关注: userId={}, teacherId={}", userId, teacherId);
        
        followService.unfollowTeacher(userId, teacherId);
        return Result.success("取消关注成功", null);
    }

    /**
     * 检查是否已关注
     */
    @GetMapping("/teacher/{teacherId}/check")
    public Result<Boolean> checkFollowing(
            @PathVariable Long teacherId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        boolean isFollowing = followService.isFollowing(userId, teacherId);
        return Result.success(isFollowing);
    }

    /**
     * 获取关注的教师列表
     */
    @GetMapping("/teachers")
    public Result<List<UserProfileDTO>> getFollowingTeachers(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("获取关注列表: userId={}", userId);
        
        List<UserProfileDTO> teachers = followService.getFollowingTeachers(userId);
        return Result.success(teachers);
    }

    /**
     * 获取关注数量
     */
    @GetMapping("/count")
    public Result<Long> getFollowingCount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        long count = followService.getFollowingCount(userId);
        return Result.success(count);
    }
}

