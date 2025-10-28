package com.qasystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qasystem.dto.UserProfileDTO;
import com.qasystem.entity.*;
import com.qasystem.mapper.*;
import com.qasystem.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 管理员Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    private final QuestionMapper questionMapper;
    private final AnswerMapper answerMapper;
    private final PasswordEncoder passwordEncoder;
    private final com.qasystem.common.util.RedisUtil redisUtil;

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 用户统计
        Long studentCount = userMapper.selectCount(
            new LambdaQueryWrapper<User>().eq(User::getRole, "STUDENT")
        );
        Long teacherCount = userMapper.selectCount(
            new LambdaQueryWrapper<User>().eq(User::getRole, "TEACHER")
        );
        Long totalUsers = studentCount + teacherCount;
        
        // 问题统计
        Long totalQuestions = questionMapper.selectCount(null);
        Long pendingQuestions = questionMapper.selectCount(
            new LambdaQueryWrapper<Question>().eq(Question::getStatus, "PENDING")
        );
        Long resolvedQuestions = questionMapper.selectCount(
            new LambdaQueryWrapper<Question>().eq(Question::getStatus, "RESOLVED")
        );
        
        // 回答统计
        Long totalAnswers = answerMapper.selectCount(null);
        
        statistics.put("studentCount", studentCount);
        statistics.put("teacherCount", teacherCount);
        statistics.put("totalUsers", totalUsers);
        statistics.put("totalQuestions", totalQuestions);
        statistics.put("pendingQuestions", pendingQuestions);
        statistics.put("resolvedQuestions", resolvedQuestions);
        statistics.put("totalAnswers", totalAnswers);
        
        // 今日数据（简化版，实际应该按日期查询）
        statistics.put("todayQuestions", 0);
        statistics.put("todayAnswers", 0);
        
        return statistics;
    }

    @Override
    public IPage<UserProfileDTO> getStudentPage(Integer page, Integer size, String keyword, String status) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(User::getRole, "STUDENT");
        
        if (status != null && !status.isEmpty()) {
            wrapper.eq(User::getStatus, status);
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                .like(User::getUsername, keyword)
                .or().like(User::getRealName, keyword)
                .or().like(User::getEmail, keyword)
            );
        }
        
        wrapper.orderByDesc(User::getCreateTime);
        
        IPage<User> userPage = userMapper.selectPage(pageParam, wrapper);
        
        // 转换为DTO并关联学生信息
        Page<UserProfileDTO> dtoPage = new Page<>(page, size, userPage.getTotal());
        dtoPage.setRecords(
            userPage.getRecords().stream().map(user -> {
                UserProfileDTO dto = convertToDTO(user);
                
                // 关联学生信息
                Student student = studentMapper.selectOne(
                    new LambdaQueryWrapper<Student>().eq(Student::getUserId, user.getId())
                );
                if (student != null) {
                    dto.setStudentNo(student.getStudentNo());
                    dto.setMajor(student.getMajor());
                    dto.setClassName(student.getClassName());
                    dto.setGrade(student.getGrade());
                    dto.setCollege(student.getCollege());
                }
                
                return dto;
            }).toList()
        );
        
        return dtoPage;
    }

    @Override
    public IPage<UserProfileDTO> getTeacherPage(Integer page, Integer size, String keyword, String status) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(User::getRole, "TEACHER");
        
        if (status != null && !status.isEmpty()) {
            wrapper.eq(User::getStatus, status);
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                .like(User::getUsername, keyword)
                .or().like(User::getRealName, keyword)
                .or().like(User::getEmail, keyword)
            );
        }
        
        wrapper.orderByDesc(User::getCreateTime);
        
        IPage<User> userPage = userMapper.selectPage(pageParam, wrapper);
        
        // 转换为DTO并关联教师信息
        Page<UserProfileDTO> dtoPage = new Page<>(page, size, userPage.getTotal());
        dtoPage.setRecords(
            userPage.getRecords().stream().map(user -> {
                UserProfileDTO dto = convertToDTO(user);
                
                // 关联教师信息
                Teacher teacher = teacherMapper.selectOne(
                    new LambdaQueryWrapper<Teacher>().eq(Teacher::getUserId, user.getId())
                );
                if (teacher != null) {
                    dto.setTeacherNo(teacher.getTeacherNo());
                    dto.setTitle(teacher.getTitle());
                    dto.setCollege(teacher.getCollege());
                    dto.setResearch(teacher.getResearch());
                    dto.setOffice(teacher.getOffice());
                    dto.setBio(teacher.getBio());
                }
                
                return dto;
            }).toList()
        );
        
        return dtoPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long userId, String status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        user.setStatus(status);
        userMapper.updateById(user);
        
        log.info("用户状态已更新: userId={}, status={}", userId, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 删除用户（逻辑删除）
        userMapper.deleteById(userId);
        
        // 删除关联的学生或教师信息
        if ("STUDENT".equals(user.getRole())) {
            studentMapper.delete(
                new LambdaQueryWrapper<Student>().eq(Student::getUserId, userId)
            );
        } else if ("TEACHER".equals(user.getRole())) {
            teacherMapper.delete(
                new LambdaQueryWrapper<Teacher>().eq(Teacher::getUserId, userId)
            );
        }
        
        log.info("用户已删除: userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetUserPassword(Long userId, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 加密并更新密码
        String encodedPassword = passwordEncoder.encode(newPassword);
        log.info("重置密码 - userId: {}, 原始密码长度: {}, 加密后密码: {}...", 
                userId, newPassword.length(), encodedPassword.substring(0, 20));
        
        user.setPassword(encodedPassword);
        userMapper.updateById(user);
        
        // 清除Redis缓存，确保新密码立即生效
        try {
            String userKey = "user:info:" + userId;
            redisUtil.delete(userKey);
            log.info("已清除用户缓存: {}", userKey);
        } catch (Exception e) {
            log.warn("清除用户缓存失败，但密码已更新: {}", e.getMessage());
        }
        
        log.info("用户密码已重置成功: userId={}, username={}", userId, user.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserProfileDTO createStudent(com.qasystem.dto.CreateUserRequest request) {
        // 检查用户名是否已存在
        Long count = userMapper.selectCount(
            new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );
        if (count > 0) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setRole("STUDENT");
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setGender(request.getGender() != null ? request.getGender() : "U");
        user.setStatus("ACTIVE");
        userMapper.insert(user);
        
        // 创建学生信息
        Student student = new Student();
        student.setUserId(user.getId());
        student.setStudentNo(request.getStudentNo());
        student.setMajor(request.getMajor());
        student.setClassName(request.getClassName());
        student.setGrade(request.getGrade());
        student.setCollege(request.getCollege());
        studentMapper.insert(student);
        
        log.info("学生账号创建成功: userId={}, username={}", user.getId(), user.getUsername());
        
        // 返回DTO
        UserProfileDTO dto = convertToDTO(user);
        dto.setStudentNo(student.getStudentNo());
        dto.setMajor(student.getMajor());
        dto.setClassName(student.getClassName());
        dto.setGrade(student.getGrade());
        dto.setCollege(student.getCollege());
        
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserProfileDTO createTeacher(com.qasystem.dto.CreateUserRequest request) {
        // 检查用户名是否已存在
        Long count = userMapper.selectCount(
            new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );
        if (count > 0) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setRole("TEACHER");
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setGender(request.getGender() != null ? request.getGender() : "U");
        user.setStatus("ACTIVE");
        userMapper.insert(user);
        
        // 创建教师信息
        Teacher teacher = new Teacher();
        teacher.setUserId(user.getId());
        teacher.setTeacherNo(request.getTeacherNo());
        teacher.setTitle(request.getTitle());
        teacher.setCollege(request.getCollege());
        teacher.setResearch(request.getResearch());
        teacher.setOffice(request.getOffice());
        teacherMapper.insert(teacher);
        
        log.info("教师账号创建成功: userId={}, username={}", user.getId(), user.getUsername());
        
        // 返回DTO
        UserProfileDTO dto = convertToDTO(user);
        dto.setTeacherNo(teacher.getTeacherNo());
        dto.setTitle(teacher.getTitle());
        dto.setCollege(teacher.getCollege());
        dto.setResearch(teacher.getResearch());
        dto.setOffice(teacher.getOffice());
        
        return dto;
    }

    private UserProfileDTO convertToDTO(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setRole(user.getRole());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAvatar(user.getAvatar());
        dto.setGender(user.getGender());
        dto.setStatus(user.getStatus());
        dto.setCreateTime(user.getCreateTime());
        dto.setUpdateTime(user.getUpdateTime());
        return dto;
    }
}

