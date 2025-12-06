package com.qasystem.service.impl;

import com.qasystem.common.util.RedisUtil;
import com.qasystem.dto.ChangePasswordRequest;
import com.qasystem.dto.UpdateProfileRequest;
import com.qasystem.dto.UserProfileDTO;
import com.qasystem.entity.Student;
import com.qasystem.entity.Teacher;
import com.qasystem.entity.User;
import com.qasystem.mapper.StudentMapper;
import com.qasystem.mapper.TeacherMapper;
import com.qasystem.mapper.UserMapper;
import com.qasystem.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * 👤 个人中心服务实现类
 * 
 * 🎯 核心功能：查看个人信息、修改个人资料、修改密码
 * 💾 缓存策略：个人信息缓存7天，修改后自动清除缓存
 * 🔒 权限控制：用户只能查看和修改自己的信息
 * ⚠️ 事务处理：修改资料和密码时同时更新用户表和扩展表
 * @author 师生答疑系统开发团队
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserMapper userMapper;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    private final PasswordEncoder passwordEncoder;
    private final RedisUtil redisUtil;

    private static final String USER_PROFILE_CACHE_KEY = "profile:";
    private static final long CACHE_EXPIRE_DAYS = 7;

    /** 🔍 获取当前用户信息：先从缓存获取，未命中再查数据库，关联学生/教师扩展信息 */
    @Override
    public UserProfileDTO getCurrentUserProfile(Long userId) {
        // 先从缓存获取
        String cacheKey = USER_PROFILE_CACHE_KEY + userId;
        UserProfileDTO cachedProfile = redisUtil.get(cacheKey, UserProfileDTO.class);
        if (cachedProfile != null) {
            log.debug("从缓存获取用户信息: userId={}", userId);
            return cachedProfile;
        }

        // 查询用户基本信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        UserProfileDTO profile = new UserProfileDTO();
        BeanUtils.copyProperties(user, profile);
        profile.setUserId(user.getId());

        // 根据角色查询扩展信息
        if ("STUDENT".equals(user.getRole())) {
            studentMapper.findByUserId(userId).ifPresent(student -> {
                profile.setStudentNo(student.getStudentNo());
                profile.setMajor(student.getMajor());
                profile.setClassName(student.getClassName());
                profile.setGrade(student.getGrade());
                profile.setCollege(student.getCollege());
            });
        } else if ("TEACHER".equals(user.getRole())) {
            teacherMapper.findByUserId(userId).ifPresent(teacher -> {
                profile.setTeacherNo(teacher.getTeacherNo());
                profile.setTitle(teacher.getTitle());
                profile.setResearch(teacher.getResearch());
                profile.setOffice(teacher.getOffice());
                profile.setBio(teacher.getBio());
            });
        }

        // 缓存到Redis
        redisUtil.set(cacheKey, profile, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);

        return profile;
    }

    /** ✏️ 修改个人资料：更新基本信息和角色扩展信息，清除缓存 */
    @Override
    @Transactional
    public UserProfileDTO updateProfile(Long userId, UpdateProfileRequest request) {
        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 更新基本信息
        if (request.getRealName() != null) {
            user.setRealName(request.getRealName());
        }
        if (request.getEmail() != null) {
            // 检查邮箱是否已被其他用户使用
            userMapper.findByEmail(request.getEmail()).ifPresent(existUser -> {
                if (!existUser.getId().equals(userId)) {
                    throw new RuntimeException("邮箱已被使用");
                }
            });
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }

        userMapper.updateById(user);

        // 根据角色更新扩展信息
        if ("STUDENT".equals(user.getRole())) {
            studentMapper.findByUserId(userId).ifPresent(student -> {
                if (request.getMajor() != null) {
                    student.setMajor(request.getMajor());
                }
                if (request.getClassName() != null) {
                    student.setClassName(request.getClassName());
                }
                if (request.getGrade() != null) {
                    student.setGrade(request.getGrade());
                }
                if (request.getCollege() != null) {
                    student.setCollege(request.getCollege());
                }
                studentMapper.updateById(student);
            });
        } else if ("TEACHER".equals(user.getRole())) {
            teacherMapper.findByUserId(userId).ifPresent(teacher -> {
                if (request.getTitle() != null) {
                    teacher.setTitle(request.getTitle());
                }
                if (request.getResearch() != null) {
                    teacher.setResearch(request.getResearch());
                }
                if (request.getOffice() != null) {
                    teacher.setOffice(request.getOffice());
                }
                if (request.getBio() != null) {
                    teacher.setBio(request.getBio());
                }
                teacherMapper.updateById(teacher);
            });
        }

        // 清除缓存
        String cacheKey = USER_PROFILE_CACHE_KEY + userId;
        redisUtil.delete(cacheKey);
        
        // 也清除用户信息缓存
        redisUtil.delete("user:info:" + userId);

        log.info("用户信息更新成功: userId={}", userId);

        // 返回更新后的信息
        return getCurrentUserProfile(userId);
    }

    /** 🔑 修改密码：验证原密码，检查新密码一致性，使用BCrypt加密 */
    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        // 验证两次密码是否一致
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }

        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证原密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);

        log.info("密码修改成功: userId={}", userId);
    }
}

