package com.qasystem.service.impl;

import com.qasystem.common.util.RedisUtil;
import com.qasystem.dto.UserProfileDTO;
import com.qasystem.entity.Follow;
import com.qasystem.entity.User;
import com.qasystem.mapper.FollowMapper;
import com.qasystem.mapper.TeacherMapper;
import com.qasystem.mapper.UserMapper;
import com.qasystem.service.FollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 关注服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowMapper followMapper;
    private final UserMapper userMapper;
    private final TeacherMapper teacherMapper;
    private final RedisUtil redisUtil;

    private static final String FOLLOW_KEY = "follow:";

    @Override
    @Transactional
    public void followTeacher(Long studentId, Long teacherId) {
        // 验证教师是否存在
        User teacher = userMapper.selectById(teacherId);
        if (teacher == null || !"TEACHER".equals(teacher.getRole())) {
            throw new RuntimeException("教师不存在");
        }

        // 检查是否已关注
        if (followMapper.findByFollowerAndFollowee(studentId, teacherId).isPresent()) {
            throw new RuntimeException("已经关注过该教师");
        }

        // 创建关注记录
        Follow follow = new Follow();
        follow.setFollowerId(studentId);
        follow.setFolloweeId(teacherId);
        followMapper.insert(follow);

        // 清除缓存
        redisUtil.delete(FOLLOW_KEY + studentId);

        log.info("关注成功: studentId={}, teacherId={}", studentId, teacherId);
    }

    @Override
    @Transactional
    public void unfollowTeacher(Long studentId, Long teacherId) {
        Follow follow = followMapper.findByFollowerAndFollowee(studentId, teacherId)
                .orElseThrow(() -> new RuntimeException("未关注该教师"));

        followMapper.deleteById(follow.getId());

        // 清除缓存
        redisUtil.delete(FOLLOW_KEY + studentId);

        log.info("取消关注成功: studentId={}, teacherId={}", studentId, teacherId);
    }

    @Override
    public boolean isFollowing(Long studentId, Long teacherId) {
        return followMapper.findByFollowerAndFollowee(studentId, teacherId).isPresent();
    }

    @Override
    public List<UserProfileDTO> getFollowingTeachers(Long studentId) {
        List<Follow> follows = followMapper.findByFollower(studentId);
        
        return follows.stream()
                .map(follow -> {
                    User teacher = userMapper.selectById(follow.getFolloweeId());
                    if (teacher == null) return null;
                    
                    UserProfileDTO dto = new UserProfileDTO();
                    BeanUtils.copyProperties(teacher, dto);
                    dto.setUserId(teacher.getId());
                    
                    // 获取教师职称
                    teacherMapper.findByUserId(teacher.getId()).ifPresent(t -> {
                        dto.setTeacherNo(t.getTeacherNo());
                        dto.setTitle(t.getTitle());
                        dto.setResearch(t.getResearch());
                        dto.setCollege(t.getCollege());
                    });
                    
                    return dto;
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserProfileDTO> getFollowers(Long teacherId) {
        List<Follow> follows = followMapper.findByFollowee(teacherId);
        
        return follows.stream()
                .map(follow -> {
                    User student = userMapper.selectById(follow.getFollowerId());
                    if (student == null) return null;
                    
                    UserProfileDTO dto = new UserProfileDTO();
                    BeanUtils.copyProperties(student, dto);
                    dto.setUserId(student.getId());
                    
                    return dto;
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    @Override
    public long getFollowingCount(Long studentId) {
        return followMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowerId, studentId)
        );
    }

    @Override
    public long getFollowersCount(Long teacherId) {
        return followMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Follow>()
                .eq(Follow::getFolloweeId, teacherId)
        );
    }
}

