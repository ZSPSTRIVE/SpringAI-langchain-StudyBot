package com.qasystem.service;

import com.qasystem.dto.UserProfileDTO;

import java.util.List;

/**
 * 关注服务接口
 */
public interface FollowService {

    /**
     * 关注教师
     */
    void followTeacher(Long studentId, Long teacherId);

    /**
     * 取消关注
     */
    void unfollowTeacher(Long studentId, Long teacherId);

    /**
     * 检查是否已关注
     */
    boolean isFollowing(Long studentId, Long teacherId);

    /**
     * 获取用户关注的所有教师
     */
    List<UserProfileDTO> getFollowingTeachers(Long studentId);

    /**
     * 获取教师的粉丝列表
     */
    List<UserProfileDTO> getFollowers(Long teacherId);

    /**
     * 获取关注数量
     */
    long getFollowingCount(Long studentId);

    /**
     * 获取粉丝数量
     */
    long getFollowersCount(Long teacherId);
}

