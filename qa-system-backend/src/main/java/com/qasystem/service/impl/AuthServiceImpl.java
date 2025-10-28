package com.qasystem.service.impl;

import com.qasystem.common.util.JwtUtil;
import com.qasystem.common.util.RedisUtil;
import com.qasystem.dto.LoginRequest;
import com.qasystem.dto.LoginResponse;
import com.qasystem.dto.RegisterRequest;
import com.qasystem.entity.Student;
import com.qasystem.entity.Teacher;
import com.qasystem.entity.User;
import com.qasystem.mapper.StudentMapper;
import com.qasystem.mapper.TeacherMapper;
import com.qasystem.mapper.UserMapper;
import com.qasystem.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;

    @Override
    public LoginResponse login(LoginRequest request) {
        // 查询用户
        User user = userMapper.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 检查用户状态
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new RuntimeException("账号已被禁用或锁定");
        }

        // 生成Token
        String accessToken = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        // 缓存用户信息到Redis
        String userKey = "user:info:" + user.getId();
        redisUtil.set(userKey, user, 7, TimeUnit.DAYS);

        log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(7 * 24 * 60 * 60L)
                .userInfo(LoginResponse.UserInfo.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .realName(user.getRealName())
                        .role(user.getRole())
                        .email(user.getEmail())
                        .avatar(user.getAvatar())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userMapper.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userMapper.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setRole(request.getRole());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus("ACTIVE");
        user.setGender("U");
        
        userMapper.insert(user);

        // 根据角色创建扩展信息
        if ("STUDENT".equals(request.getRole())) {
            Student student = new Student();
            student.setUserId(user.getId());
            // 如果学号为空，则不设置（避免唯一索引冲突）
            if (request.getStudentNo() != null && !request.getStudentNo().trim().isEmpty()) {
                student.setStudentNo(request.getStudentNo());
            }
            student.setMajor(request.getMajor());
            student.setClassName(request.getClassName());
            student.setGrade(request.getGrade());
            student.setCollege(request.getCollege());
            studentMapper.insert(student);
        } else if ("TEACHER".equals(request.getRole())) {
            Teacher teacher = new Teacher();
            teacher.setUserId(user.getId());
            // 如果工号为空，则不设置（避免唯一索引冲突）
            if (request.getTeacherNo() != null && !request.getTeacherNo().trim().isEmpty()) {
                teacher.setTeacherNo(request.getTeacherNo());
            }
            teacher.setTitle(request.getTitle());
            teacher.setCollege(request.getCollege());
            teacher.setResearch(request.getResearch());
            teacher.setOffice(request.getOffice());
            teacherMapper.insert(teacher);
        }

        log.info("用户注册成功: userId={}, username={}, role={}", user.getId(), user.getUsername(), user.getRole());

        // 自动登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(request.getUsername());
        loginRequest.setPassword(request.getPassword());
        return login(loginRequest);
    }

    @Override
    public void logout(String token) {
        // 将Token加入黑名单
        String tokenKey = "token:blacklist:" + token;
        redisUtil.set(tokenKey, true, 7, TimeUnit.DAYS);
        
        log.info("用户登出成功");
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("刷新Token无效或已过期");
        }

        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        String username = jwtUtil.getUsernameFromToken(refreshToken);

        // 查询用户信息
        User user = userMapper.selectById(userId);
        if (user == null || !"ACTIVE".equals(user.getStatus())) {
            throw new RuntimeException("用户不存在或已被禁用");
        }

        // 生成新的Token
        String newAccessToken = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(7 * 24 * 60 * 60L)
                .userInfo(LoginResponse.UserInfo.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .realName(user.getRealName())
                        .role(user.getRole())
                        .email(user.getEmail())
                        .avatar(user.getAvatar())
                        .build())
                .build();
    }
}

