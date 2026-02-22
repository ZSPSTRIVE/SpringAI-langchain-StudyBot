package com.qasystem.common.util;

// JJWT库 - Java JWT(JSON Web Token)的实现库
import io.jsonwebtoken.*;
// 密钥生成工具
import io.jsonwebtoken.security.Keys;
// Lombok的日志注解
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;
// 从配置文件读取属性值
import org.springframework.beans.factory.annotation.Value;
// 标记为Spring组件
import org.springframework.stereotype.Component;

// 加密密钥接口
import javax.crypto.SecretKey;
// 字符编码
import java.nio.charset.StandardCharsets;
// 日期处理
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 🔐 JWT工具类 - 用于生成和验证JWT令牌
 * 
 * 📖 功能说明：
 * JWT (JSON Web Token) 是一种开放标准(RFC 7519)，用于在各方之间安全地传输信息。
 * 本工具类提供JWT令牌的生成、验证和解析功能，为师生答疑系统提供无状态的身份认证机制。
 * 
 * 🎯 JWT是什么？
 * JWT是一种紧凑且自包含的方式，用于在各方之间安全地传输信息作为JSON对象。
 * 它可以被验证和信任，因为它是数字签名的。JWT可以使用秘密(使用HMAC算法)或使用RSA或ECDSA的公钥/私钥对进行签名。
 * 
 * 🔍 JWT的结构：
 * JWT由三部分组成，用点号(.)分隔：
 * 1. Header（头部）：记录令牌类型和加密算法，例如：{"alg":"HS512","typ":"JWT"}
 * 2. Payload（负载）：记录用户信息（userId, username, role等）和元数据
 * 3. Signature（签名）：防止数据被篡改，使用密钥对Header和Payload进行加密生成
 * 
 * 📝 JWT示例：
 * eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoiemhhbmdzYW4iLCJyb2xlIjoiU1RVREVOVCJ9.xxx
 * 
 * 🚀 JWT vs Session：
 * - Session需要在服务器端存储，占用内存，不适合分布式系统
 * - JWT无状态，服务器不需要存储，适合微服务和分布式架构
 * - JWT可以跨域使用，支持移动端和Web端
 * - JWT包含用户信息，减少数据库查询
 * 
 * 🛠️ 本工具类的核心功能：
 * 1. 生成访问Token（短期有效，例如2小时）- 用于API访问认证
 * 2. 生成刷新Token（长期有效，例如7天）- 用于获取新的访问Token
 * 3. 验证Token是否有效 - 检查签名、过期时间等
 * 4. 从Token中解析出用户信息 - 提取userId、username、role等
 * 
 * 🔄 工作流程：
 * 1. 用户登录 → 验证账号密码 → 生成访问Token和刷新Token
 * 2. 前端保存Token → 每次API请求携带访问Token（通常在Authorization头中）
 * 3. 后端拦截器验证Token → 解析用户信息 → 放行请求
 * 4. 访问Token过期 → 使用刷新Token获取新的访问Token
 * 5. 刷新Token过期 → 用户重新登录
 * 
 * ⚠️ 安全注意事项：
 * - JWT密钥必须足够复杂且保密，防止被破解
 * - 敏感信息不应存储在JWT中，因为Payload只是Base64编码，不是加密
 * - 访问Token应设置较短的有效期，降低被盗用的风险
 * - 应使用HTTPS传输JWT，防止中间人攻击
 * - 实现Token黑名单机制，支持主动撤销Token
 * 
 * @author 师生答疑系统开发团队
 * @since 1.0.0
 * @see <a href="https://tools.ietf.org/html/rfc7519">JWT RFC 7519规范</a>
 */
// @Slf4j：Lombok注解，自动生成log日志对象，可以直接使用log.info()等方法
@Slf4j
// @Component：告诉Spring这是一个组件，会被Spring管理，其他地方可以注入使用
@Component
public class JwtUtil {
    private static final int HS512_MIN_KEY_BYTES = 64;

    /**
     * 🔑 JWT的加密密钥 - 从配置文件读取
     * 
     * 📖 功能说明：
     * 这个密钥是JWT安全性的核心，用于对JWT进行签名和验证。
     * 签名确保了JWT的完整性和真实性，防止数据被篡改。
     * 
     * 🔐 安全机制：
     * - 使用HMAC-SHA512算法对JWT进行签名
     * - 密钥长度至少应为256位(32字节)以保证安全性
     * - 生产环境应使用强随机生成的密钥
     * - 密钥泄露将导致整个认证体系失效
     * 
     * 📋 配置示例：
     * 在application.yml中配置：
     * ```yaml
     * jwt:
     *   secret: myVeryStrongSecretKeyThatShouldBeAtLeast32CharactersLong
     * ```
     * 
     * ⚠️ 注意事项：
     * - 密钥必须保密，不应提交到版本控制系统
     * - 生产环境应从环境变量或密钥管理系统读取
     * - 密钥变更会导致所有已签发的Token失效
     * - 建议定期轮换密钥以提高安全性
     */
    @Value("${jwt.secret}")
    private String secret;

    private SecretKey secretKey;

    /**
     * ⏰ 访问Token的有效期（毫秒）
     * 
     * 📖 功能说明：
     * 访问Token是短期有效的认证令牌，用于API访问控制。
     * 较短的有效期可以降低Token被盗用的安全风险。
     * 
     * ⚖️ 有效期权衡：
     * - 时间太短：用户体验差，需要频繁刷新Token
     * - 时间太长：安全风险高，Token被盗用后有效期长
     * 
     * 📊 推荐设置：
     * - 高安全要求：15-30分钟
     * - 一般安全要求：1-2小时
     * - 低安全要求：4-8小时
     * 
     * 📋 配置示例：
     * 在application.yml中配置：
     * ```yaml
     * jwt:
     *   expiration: 7200000  # 2小时，单位：毫秒
     * ```
     * 
     * 🔄 计算公式：
     * 2小时 = 2 * 60 * 60 * 1000 = 7200000毫秒
     * 30分钟 = 30 * 60 * 1000 = 1800000毫秒
     */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 🔄 刷新Token的有效期（毫秒）
     * 
     * 📖 功能说明：
     * 刷新Token是长期有效的令牌，专门用于获取新的访问Token。
     * 当访问Token过期时，客户端可以使用刷新Token获取新的访问Token，
     * 而无需用户重新输入账号密码，提升用户体验的同时保持安全性。
     * 
     * 🔄 工作机制：
     * 1. 用户登录成功，同时获取访问Token和刷新Token
     * 2. 访问Token过期后，使用刷新Token请求新的访问Token
     * 3. 刷新Token验证成功，生成新的访问Token返回给客户端
     * 4. 刷新Token过期后，用户需要重新登录
     * 
     * 📊 推荐设置：
     * - 高安全要求：3-7天
     * - 一般安全要求：7-14天
     * - 低安全要求：14-30天
     * 
     * 📋 配置示例：
     * 在application.yml中配置：
     * ```yaml
     * jwt:
     *   refresh-expiration: 604800000  # 7天，单位：毫秒
     * ```
     * 
     * 🔄 计算公式：
     * 7天 = 7 * 24 * 60 * 60 * 1000 = 604800000毫秒
     * 30天 = 30 * 24 * 60 * 60 * 1000 = 2592000000毫秒
     */
    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    @PostConstruct
    public void initSecretKey() {
        String normalized = secret == null ? "" : secret.trim();
        if (normalized.isEmpty()) {
            throw new IllegalStateException("jwt.secret must not be empty.");
        }

        byte[] keyBytes = normalized.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < HS512_MIN_KEY_BYTES) {
            throw new IllegalStateException(
                    "jwt.secret is too short for HS512: " + (keyBytes.length * 8) + " bits. " +
                    "Please set JWT_SECRET to at least 64 bytes (512 bits)."
            );
        }

        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT secret initialized. keyLength={} bits", keyBytes.length * 8);
    }

    /**
     * 🎫 生成访问Token - 用户登录后调用此方法生成Token
     * 
     * 📖 功能说明：
     * 为已认证的用户生成访问Token，包含用户身份信息和权限信息。
     * 访问Token是短期有效的，用于在有效期内访问需要认证的API接口。
     * 
     * 🔄 工作流程：
     * 1. 用户输入账号密码登录
     * 2. 系统验证账号密码的正确性
     * 3. 验证成功后，调用此方法生成Token
     * 4. 将Token返回给前端
     * 5. 前端将Token保存在本地存储中
     * 6. 后续API请求在Authorization头中携带Token
     * 
     * 📦 Token内容：
     * - Header：算法类型(HS512)和令牌类型(JWT)
     * - Payload：用户ID、用户名、角色、签发时间、过期时间
     * - Signature：使用密钥对Header和Payload的签名
     * 
     * 📝 参数说明：
     * @param userId 用户ID - 数据库中的唯一标识，用于关联用户数据
     * @param username 用户名 - 登录账号，用于显示和识别
     * @param role 用户角色 - 用于权限控制(STUDENT/TEACHER/ADMIN)
     * 
     * @return JWT Token字符串，格式：header.payload.signature
     *         示例：eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoiemhhbmdzYW4ifQ.xxx
     * 
     * 🔄 使用示例：
     * ```java
     * // 用户登录成功后生成Token
     * String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
     * 
     * // 返回给前端
     * return ApiResponse.success(token);
     * ```
     */
    public String generateToken(Long userId, String username, String role) {
        // 创建Claims（声明/负载）- 存放用户信息的Map
        // Claims就像一个包裹，里面放着各种用户信息
        Map<String, Object> claims = new HashMap<>();
        // 存入用户ID - 用于识别用户身份，关联数据库记录
        claims.put("userId", userId);
        // 存入用户名 - 用于显示和识别，提升用户体验
        claims.put("username", username);
        // 存入角色 - 用于权限控制，实现不同角色的功能访问限制
        claims.put("role", role);
        
        // 获取当前时间 - 作为Token的签发时间
        Date now = new Date();
        // 计算过期时间 = 当前时间 + 有效期
        // 例如：当前是14:00，有效期2小时，则过期时间是16:00
        Date expiryDate = new Date(now.getTime() + expiration);
        
        // 使用Jwts.builder()构建 JWT Token
        // 这是一个链式调用，一步步构建Token
        return Jwts.builder()
                // 设置声明/负载 - 将用户信息放入Token
                .claims(claims)
                // 设置Subject（主题）- 通常放用户名或用户ID，用于标识Token的主体
                .subject(username)
                // 设置颁发时间 - Token的生成时间，用于审计和调试
                .issuedAt(now)
                // 设置过期时间 - Token到这个时间后就无效了，强制用户重新认证
                .expiration(expiryDate)
                // 设置签名 - 使用密钥和HS512算法对Token签名，防止被篡改
                // HS512：HMAC-SHA512加密算法，安全性较高，性能较好
                .signWith(getSecretKey(), Jwts.SIG.HS512)
                // 构建并生成最终的Token字符串
                .compact();
    }

    /**
     * 🔄 生成刷新Token - 用于获取新的访问Token
     * 
     * 📖 功能说明：
     * 生成刷新Token，当访问Token过期时，客户端可以使用此Token获取新的访问Token，
     * 而无需用户重新登录，提升用户体验。
     * 
     * 🔒 安全机制：
     * - 刷新Token比访问Token有效期更长
     * - 刷新Token通常存储在更安全的位置（如HttpOnly Cookie）
     * - 刷新Token使用次数应有限制，防止滥用
     * - 刷新Token应包含"refresh"类型标识，与访问Token区分
     * 
     * 📦 Token内容：
     * - Header：算法类型(HS512)和令牌类型(JWT)
     * - Payload：用户ID、用户名、Token类型(refresh)、签发时间、过期时间
     * - Signature：使用密钥对Header和Payload的签名
     * 
     * 📝 参数说明：
     * @param userId 用户ID - 用于关联用户数据，确保刷新Token只能被原用户使用
     * @param username 用户名 - 用于验证用户身份
     * 
     * @return 刷新Token字符串，格式与访问Token相同，但Payload内容不同
     * 
     * 🔄 使用示例：
     * ```java
     * // 用户登录成功后生成Token对
     * String accessToken = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
     * String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());
     * 
     * // 返回Token对给前端
     * Map<String, String> tokens = new HashMap<>();
     * tokens.put("accessToken", accessToken);
     * tokens.put("refreshToken", refreshToken);
     * return ApiResponse.success(tokens);
     * ```
     */
    public String generateRefreshToken(Long userId, String username) {
        // 获取当前时间 - 作为Token的签发时间
        Date now = new Date();
        // 计算过期时间 = 当前时间 + 刷新Token有效期
        // 刷新Token的有效期通常比访问Token长得多
        Date expiryDate = new Date(now.getTime() + refreshExpiration);
        
        // 使用Jwts.builder()构建刷新Token
        return Jwts.builder()
                // 设置Subject（主题）- 用户名，用于标识Token的主体
                .subject(username)
                // 添加用户ID声明 - 用于关联用户数据
                .claim("userId", userId)
                // 添加Token类型声明 - 标识这是刷新Token，与访问Token区分
                .claim("type", "refresh")
                // 设置颁发时间 - Token的生成时间
                .issuedAt(now)
                // 设置过期时间 - 刷新Token的过期时间，通常较长
                .expiration(expiryDate)
                // 设置签名 - 使用相同的密钥和算法签名
                .signWith(getSecretKey(), Jwts.SIG.HS512)
                // 构建并生成最终的刷新Token字符串
                .compact();
    }

    /**
     * 🆔 从Token中获取用户ID
     * 
     * 📖 功能说明：
     * 从JWT Token中解析出用户ID，用于识别当前操作的用户身份。
     * 用户ID是数据库中的主键，用于关联用户的所有数据和操作记录。
     * 
     * 🔄 工作流程：
     * 1. 接收JWT Token字符串
     * 2. 验证Token的签名和有效期
     * 3. 解析Token中的Claims
     * 4. 从Claims中提取userId字段
     * 5. 返回用户ID
     * 
     * 📝 参数说明：
     * @param token JWT Token字符串，格式：header.payload.signature
     * 
     * @return 用户ID，如果Token无效或解析失败，可能返回null
     * 
     * 🔄 使用示例：
     * ```java
     * // 从请求头中获取Token
     * String token = request.getHeader("Authorization").replace("Bearer ", "");
     * 
     * // 解析用户ID
     * Long userId = jwtUtil.getUserIdFromToken(token);
     * 
     * // 查询用户信息
     * User user = userService.getById(userId);
     * ```
     * 
     * ⚠️ 异常处理：
     * - Token格式错误：可能抛出MalformedJwtException
     * - Token已过期：可能抛出ExpiredJwtException
     * - Token签名无效：可能抛出SecurityException
     * - 建议在调用前先验证Token有效性
     */
    public Long getUserIdFromToken(String token) {
        // 解析Token中的所有Claims
        Claims claims = getAllClaimsFromToken(token);
        // 从Claims中获取userId字段，并转换为Long类型
        return claims.get("userId", Long.class);
    }

    /**
     * 👤 从Token中获取用户名
     * 
     * 📖 功能说明：
     * 从JWT Token中解析出用户名，用于显示和识别用户。
     * 用户名通常是用户的登录账号，具有唯一性。
     * 
     * 🔄 工作流程：
     * 1. 接收JWT Token字符串
     * 2. 验证Token的签名和有效期
     * 3. 解析Token中的Claims
     * 4. 从Claims的Subject字段中提取用户名
     * 5. 返回用户名
     * 
     * 📝 参数说明：
     * @param token JWT Token字符串，格式：header.payload.signature
     * 
     * @return 用户名，如果Token无效或解析失败，可能返回null
     * 
     * 🔄 使用示例：
     * ```java
     * // 从请求头中获取Token
     * String token = request.getHeader("Authorization").replace("Bearer ", "");
     * 
     * // 解析用户名
     * String username = jwtUtil.getUsernameFromToken(token);
     * 
     * // 记录操作日志
     * log.info("User {} performed action", username);
     * ```
     * 
     * ⚠️ 异常处理：
     * - Token格式错误：可能抛出MalformedJwtException
     * - Token已过期：可能抛出ExpiredJwtException
     * - Token签名无效：可能抛出SecurityException
     * - 建议在调用前先验证Token有效性
     */
    public String getUsernameFromToken(String token) {
        // 解析Token中的所有Claims
        Claims claims = getAllClaimsFromToken(token);
        // 从Claims的Subject字段中获取用户名
        return claims.getSubject();
    }

    /**
     * 🎭 从Token中获取角色
     * 
     * 📖 功能说明：
     * 从JWT Token中解析出用户角色，用于权限控制和功能访问限制。
     * 角色决定了用户可以访问的资源和执行的操作。
     * 
     * 🎭 角色说明：
     * - STUDENT：学生角色，可以提问、查看回答、评价回答质量
     * - TEACHER：教师角色，可以回答问题、管理自己的回答
     * - ADMIN：管理员角色，可以管理所有用户、问题和系统配置
     * 
     * 🔄 工作流程：
     * 1. 接收JWT Token字符串
     * 2. 验证Token的签名和有效期
     * 3. 解析Token中的Claims
     * 4. 从Claims中提取role字段
     * 5. 返回用户角色
     * 
     * 📝 参数说明：
     * @param token JWT Token字符串，格式：header.payload.signature
     * 
     * @return 用户角色，如果Token无效或解析失败，可能返回null
     * 
     * 🔄 使用示例：
     * ```java
     * // 从请求头中获取Token
     * String token = request.getHeader("Authorization").replace("Bearer ", "");
     * 
     * // 解析用户角色
     * String role = jwtUtil.getRoleFromToken(token);
     * 
     * // 权限检查
     * if ("TEACHER".equals(role)) {
     *     // 允许回答问题
     * } else {
     *     // 拒绝访问
     *     throw new UnauthorizedException("只有教师可以回答问题");
     * }
     * ```
     * 
     * ⚠️ 异常处理：
     * - Token格式错误：可能抛出MalformedJwtException
     * - Token已过期：可能抛出ExpiredJwtException
     * - Token签名无效：可能抛出SecurityException
     * - 建议在调用前先验证Token有效性
     */
    public String getRoleFromToken(String token) {
        // 解析Token中的所有Claims
        Claims claims = getAllClaimsFromToken(token);
        // 从Claims中获取role字段，并转换为String类型
        return claims.get("role", String.class);
    }

    /**
     * ✅ 验证Token是否有效
     * 
     * 📖 功能说明：
     * 验证JWT Token的完整性和有效性，包括签名验证、格式检查和过期时间验证。
     * 这是API访问控制的关键步骤，确保只有合法的Token才能通过验证。
     * 
     * 🔍 验证内容：
     * 1. 签名验证：使用密钥验证Token签名，确保Token未被篡改
     * 2. 格式检查：验证Token是否符合JWT格式
     * 3. 过期时间：检查Token是否已过期
     * 4. 算法支持：验证Token使用的签名算法是否受支持
     * 
     * 🔄 工作流程：
     * 1. 使用密钥创建JWT解析器
     * 2. 尝试解析并验证Token
     * 3. 捕获并处理各种可能的异常
     * 4. 返回验证结果
     * 
     * 📝 参数说明：
     * @param token JWT Token字符串，格式：header.payload.signature
     * 
     * @return 验证结果，true表示Token有效，false表示Token无效
     * 
     * 🔄 使用示例：
     * ```java
     * // 从请求头中获取Token
     * String token = request.getHeader("Authorization").replace("Bearer ", "");
     * 
     * // 验证Token有效性
     * if (jwtUtil.validateToken(token)) {
     *     // Token有效，继续处理请求
     *     chain.doFilter(request, response);
     * } else {
     *     // Token无效，返回401未授权错误
     *     response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
     * }
     * ```
     * 
     * 🚨 异常处理：
     * - SecurityException：签名验证失败，Token可能被篡改
     * - MalformedJwtException：Token格式错误，可能被截断或修改
     * - ExpiredJwtException：Token已过期，需要刷新或重新登录
     * - UnsupportedJwtException：不支持的Token类型或算法
     * - IllegalArgumentException：Token为空或格式不正确
     */
    public boolean validateToken(String token) {
        try {
            // 创建JWT解析器并设置验证密钥
            Jwts.parser()
                    // 设置验证密钥，用于验证Token签名
                    .verifyWith(getSecretKey())
                    // 构建解析器
                    .build()
                    // 解析并验证Token签名
                    .parseSignedClaims(token);
            // 验证通过，返回true
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            // 签名验证失败或Token格式错误
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            // Token已过期
            log.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            // 不支持的Token类型或算法
            log.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            // Token为空或格式不正确
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        // 验证失败，返回false
        return false;
    }

    /**
     * ⏰ 检查Token是否过期
     * 
     * 📖 功能说明：
     * 检查JWT Token是否已过期，用于判断Token是否需要刷新。
     * 此方法不会验证Token的签名，只检查过期时间。
     * 
     * 🔄 工作流程：
     * 1. 解析Token中的Claims
     * 2. 获取Token的过期时间
     * 3. 比较过期时间与当前时间
     * 4. 返回比较结果
     * 
     * 📝 参数说明：
     * @param token JWT Token字符串，格式：header.payload.signature
     * 
     * @return 过期状态，true表示Token已过期，false表示Token未过期
     * 
     * 🔄 使用示例：
     * ```java
     * // 检查访问Token是否即将过期（例如剩余时间少于5分钟）
     * String accessToken = getAccessToken();
     * if (isTokenExpired(accessToken) || isTokenExpiringSoon(accessToken, 5)) {
     *     // 使用刷新Token获取新的访问Token
     *     String newAccessToken = refreshAccessToken(refreshToken);
     *     updateAccessToken(newAccessToken);
     * }
     * ```
     * 
     * ⚠️ 注意事项：
     * - 此方法不验证Token签名，只检查过期时间
     * - 如果Token格式错误，可能会抛出异常
     * - 建议在调用前先验证Token格式是否正确
     */
    public boolean isTokenExpired(String token) {
        try {
            // 解析Token中的所有Claims
            Claims claims = getAllClaimsFromToken(token);
            // 获取Token的过期时间
            Date expiration = claims.getExpiration();
            // 比较过期时间与当前时间
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            // Token已过期，直接返回true
            return true;
        }
    }

    /**
     * 📦 获取所有Claims - 解析Token中的所有声明
     * 
     * 📖 功能说明：
     * 解析JWT Token中的所有Claims（声明），返回包含所有用户信息和元数据的Claims对象。
     * 这是其他解析方法的基础，负责实际的Token解析工作。
     * 
     * 🔒 安全机制：
     * - 使用密钥验证Token签名，确保Token未被篡改
     * - 验证Token格式，确保符合JWT标准
     * - 检查Token是否过期，拒绝使用过期Token
     * 
     * 📝 参数说明：
     * @param token JWT Token字符串，格式：header.payload.signature
     * 
     * @return Claims对象，包含Token中的所有声明和信息
     * 
     * 🔄 使用示例：
     * ```java
     * // 获取Token中的所有Claims
     * Claims claims = getAllClaimsFromToken(token);
     * 
     * // 访问各种声明
     * Long userId = claims.get("userId", Long.class);
     * String username = claims.getSubject();
     * String role = claims.get("role", String.class);
     * Date issuedAt = claims.getIssuedAt();
     * Date expiration = claims.getExpiration();
     * ```
     * 
     * 🚨 异常处理：
     * - 此方法可能会抛出各种JWT相关异常
     * - 调用方应妥善处理这些异常
     * - 建议在调用前先验证Token格式是否正确
     */
    private Claims getAllClaimsFromToken(String token) {
        // 创建JWT解析器并设置验证密钥
        return Jwts.parser()
                // 设置验证密钥，用于验证Token签名
                .verifyWith(getSecretKey())
                // 构建解析器
                .build()
                // 解析并验证Token签名
                .parseSignedClaims(token)
                // 获取Token中的Claims
                .getPayload();
    }

    /**
     * 🔑 获取密钥 - 将字符串密钥转换为SecretKey对象
     * 
     * 📖 功能说明：
     * 将配置文件中的字符串密钥转换为JJWT库所需的SecretKey对象。
     * 这是JWT签名和验证的关键组件，确保密钥的正确格式和编码。
     * 
     * 🔒 安全机制：
     * - 使用UTF-8编码确保密钥的一致性
     * - 使用HMAC-SHA算法要求的密钥格式
     * - 密钥长度至少应为256位(32字节)以保证安全性
     * 
     * 📋 密钥要求：
     * - 密钥应为强随机字符串
     * - 密钥长度至少32字节(256位)
     * - 密钥应包含大小写字母、数字和特殊字符
     * - 密钥不应是常见单词或可预测模式
     * 
     * @return SecretKey对象，用于JWT签名和验证
     * 
     * 🔄 使用示例：
     * ```java
     * // 获取密钥
     * SecretKey key = getSecretKey();
     * 
     * // 使用密钥签名Token
     * String token = Jwts.builder()
     *     .subject(username)
     *     .signWith(key, Jwts.SIG.HS512)
     *     .compact();
     * 
     * // 使用密钥验证Token
     * Jws<Claims> claims = Jwts.parser()
     *     .verifyWith(key)
     *     .build()
     *     .parseSignedClaims(token);
     * ```
     * 
     * ⚠️ 注意事项：
     * - 密钥必须保密，不应在日志或错误信息中暴露
     * - 密钥变更会导致所有已签发的Token失效
     * - 生产环境应从安全的位置获取密钥，如环境变量或密钥管理系统
     */
    private SecretKey getSecretKey() {
        return secretKey;
    }
}

