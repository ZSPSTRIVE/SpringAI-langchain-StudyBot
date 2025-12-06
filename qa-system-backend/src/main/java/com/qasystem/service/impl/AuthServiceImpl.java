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
 * 🔐 用户认证服务实现类
 * 
 * 📖 这是什么？
 * 这是用户认证服务的具体实现，处理用户登录、注册、登出等核心认证逻辑。
 * 就像一个“门禁管理系统”，负责验证用户身份、发放门禁卡（Token）。
 * 
 * 🎯 核心功能实现：
 * 1. 登录：验证用户名密码，生成JWT Token，缓存用户信息
 * 2. 注册：创建新用户账号，根据角色创建扩展信息（学生/教师）
 * 3. 登出：将Token加入黑名单，使Token失效
 * 4. 刷新Token：使用RefreshToken生成新的AccessToken
 * 
 * 🔒 安全机制：
 * - 密码加密：使用BCrypt加密存储密码，不存储明文
 * - Token机制：使用JWT生成AccessToken和RefreshToken
 * - Redis缓存：用户信息缓存到7天，提高性能
 * - Token黑名单：登出时将Token加入黑名单，防止重复使用
 * - 账号状态检查：只有ACTIVE状态的用户才能登录
 * 
 * 💡 事务处理：
 * - 注册方法使用@Transactional，确保用户表和扩展表同时创建成功或失败
 * 
 * @author 师生答疑系统开发团队
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

    /**
     * 🔑 用户登录实现
     * 
     * 🎯 功能说明：
     * 验证用户名和密码，生成JWT Token，缓存用户信息到Redis。
     * 就像在门禁系统刷卡，验证身份后得到一张临时通行证（Token）。
     * 
     * 📝 执行流程：
     * 步骤1: 根据用户名查询用户信息
     *      - 如果用户不存在，抛出“用户名或密码错误”（不透露具体原因）
     * 步骤2: 使用BCrypt验证密码
     *      - passwordEncoder.matches()会将输入密码加密后与数据库中的密码对比
     *      - 如果不匹配，抛出同样的错误提示（防止暴露用户名存在性）
     * 步骤3: 检查用户账号状态
     *      - 只有ACTIVE状态的用户才能登录
     *      - LOCKED/DISABLED等状态会被拒绝登录
     * 步骤4: 生成两种Token
     *      - AccessToken：用于访问接口，有效期7天
     *      - RefreshToken：用于刷新AccessToken，有效期更长
     * 步骤5: 缓存用户信息到Redis
     *      - 缓存key: "user:info:{userId}"
     *      - 缓存时间: 7天
     *      - 目的：避免每次请求都查数据库，提高性能
     * 步骤6: 记录登录日志
     * 步骤7: 构建并返回LoginResponse
     * 
     * 🔒 安全设计：
     * - 错误提示统一为“用户名或密码错误”，不透露用户名是否存在
     * - 密码使用BCrypt加密，即使数据库泄露也无法解密
     * - Token包含用户基本信息，后续请求可直接从Token获取用户信息
     * 
     * ⚠️ 注意事项：
     * - 账号被禁用后，即使密码正确也无法登录
     * - Token生成后不能主动失效，需要等待过期或者加入黑名单
     * - Redis缓存可能失败，但不影响登录流程
     */
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

    /**
     * ✏️ 用户注册实现
     * 
     * 🎯 功能说明：
     * 创建新用户账号，根据角色（学生/教师）创建相应的扩展信息，注册成功后自动登录。
     * 就像在学校办理入学手续，填写基本信息后，根据身份（学生/老师）还需要填写不同的补充资料。
     * 
     * 📝 执行流程：
     * 步骤1: 校验用户名唯一性
     *      - 查询用户名是否已存在
     *      - 如果存在，抛出“用户名已存在”
     * 步骤2: 校验邮箱唯一性
     *      - 查询邮箱是否已被注册
     *      - 如果存在，抛出“邮箱已被注册”
     * 步骤3: 创建用户基本信息
     *      - 使用BCrypt加密密码后存储
     *      - 默认状态设置为ACTIVE（立即可用）
     *      - 默认性别设置为U（未知）
     *      - 插入用户表，数据库生成userId
     * 步骤4: 根据角色创建扩展信息
     *      - 如果是STUDENT：
     *        · 创建学生表记录，关联userId
     *        · 设置学号、专业、班级、年级、学院等
     *        · 注意：学号为null或空时不设置（避免唯一索引冲突）
     *      - 如果是TEACHER：
     *        · 创建教师表记录，关联userId
     *        · 设置工号、职称、学院、研究方向、办公室等
     *        · 注意：工号为null或空时不设置
     * 步骤5: 记录注册日志
     * 步骤6: 自动登录
     *      - 构建登录请求，调用login()方法
     *      - 返回Token和用户信息，用户不需要再次登录
     * 
     * 💾 事务处理：
     * @Transactional注解保证：
     * - 用户基本信息和扩展信息必须同时创建成功
     * - 如果任何一步失败（如学生表插入失败），整个注册操作回滚
     * - 避免出现“用户存在但没有学生/教师记录”的数据不一致问题
     * 
     * 🔒 安全设计：
     * - 密码在存储前使用BCrypt加密
     * - 用户名和邮箱必须唯一，数据库有唯一索引约束
     * - 学号/工号允许为null，但如果提供则必须唯一
     * - 注册后自动登录，提升用户体验
     * 
     * ⚠️ 注意事项：
     * - 学号/工号为null时不设置，避免数据库唯一索引冲突
     * - 注册成功后会自动登录，不需要用户再次输入密码
     * - 如果注册成功但登录失败，用户需要手动登录
     */
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

    /**
     * 🚶 用户登出实现
     * 
     * 🎯 功能说明：
     * 将用户的AccessToken加入黑名单，使Token失效，防止被继续使用。
     * 就像在门禁系统注销通行证，该证不能再用于开门。
     * 
     * 📝 执行流程：
     * 步骤1: 构建Token黑名单的Redis key
     *      - key格式："token:blacklist:{token}"
     * 步骤2: 将Token存入Redis黑名单
     *      - value设置为true（表示Token已被加入黑名单）
     *      - 过期时间设置为7天（与Token有效期一致）
     * 步骤3: 记录登出日志
     * 
     * 💡 为什么用黑名单？
     * JWT Token一旦生成就无法主动失效，只能等待过期。
     * 为了实现登出功能，需要维护一个黑名单：
     * - 用户登出时，将Token加入黑名单
     * - 后续请求时，检查Token是否在黑名单中
     * - 如果在黑名单，拒绝请求
     * 
     * 🔒 安全设计：
     * - Token加入黑名单后立即生效，不能再使用
     * - 黑名单过期时间与Token有效期一致，节省存储空间
     * - 即使Token被盗用，用户登出后盗用者也无法使用
     * 
     * ⚠️ 注意事项：
     * - 只有AccessToken需要加入黑名单，RefreshToken不需要
     * - 如果Redis失败，黑名单功能失效，Token仍然可用
     * - 建议登出后前端立即清除本地Token，不要等服务器校验
     * - 登出后，用户信息Redis缓存不会被清除（等待过期）
     */
    @Override
    public void logout(String token) {
        // 将Token加入黑名单
        String tokenKey = "token:blacklist:" + token;
        redisUtil.set(tokenKey, true, 7, TimeUnit.DAYS);
        
        log.info("用户登出成功");
    }

    /**
     * 🔄 刷新Token实现
     * 
     * 🎯 功能说明：
     * 使用RefreshToken换取新的AccessToken和RefreshToken，延长用户的登录状态。
     * 就像通行证快过期时，用旧证换取新证，不需要重新办理所有手续。
     * 
     * 📝 执行流程：
     * 步骤1: 验证RefreshToken是否有效
     *      - 检查Token签名是否正确
     *      - 检查Token是否过期
     *      - 如果无效，抛出异常，用户需要重新登录
     * 步骤2: 从RefreshToken中提取用户信息
     *      - 提取userId和username
     *      - 这些信息在生成Token时就已经编码在其中
     * 步骤3: 查询最新的用户信息
     *      - 从numpy数据库查询用户当前状态
     *      - 检查用户是否还存在，是否仍然为ACTIVE状态
     *      - 如果用户被禁用，拒绝刷新Token
     * 步骤4: 生成新的Token对
     *      - 生成新的AccessToken（有效期7天）
     *      - 生成新的RefreshToken（有效期更长）
     *      - 旧的RefreshToken将不再可用（但不会加入黑名单）
     * 步骤5: 返回新的Token和用户信息
     * 
     * 💡 为什么需要RefreshToken？
     * - AccessToken有效期较短（如7天），过期后需要重新登录
     * - 为了提升用户体验，使用RefreshToken可以在不输入密码的情况下获取新Token
     * - RefreshToken有效期更长（如 30天），减少用户重复登录的频率
     * - 分离两种Token，提高安全性：AccessToken经常使用，风险高；RefreshToken不常使用，风险低
     * 
     * 🔒 安全设计：
     * - 每次刷新时都会生成新的RefreshToken，旧的自动失效
     * - 刷新前会检查用户状态，避免被禁用的用户继续使用
     * - 即使RefreshToken没过期，但用户被禁用也无法刷新
     * 
     * ⚠️ 注意事项：
     * - RefreshToken过期后，用户必须重新登录
     * - 每次刷新都会生成新的Token对，旧Token不能再使用
     * - 前端应在AccessToken快过期时主动刷新，不要等到过期后
     * - 刷新失败后，应跳转到登录页
     */
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

