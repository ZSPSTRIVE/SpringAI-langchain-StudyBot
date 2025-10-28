# 师生答疑系统重构执行文档 - Phase 1

## 📋 项目概述

### 现有系统分析

**技术栈现状：**
- 后端：Spring Boot 2.2.2 + MyBatis-Plus 2.3 + MySQL 5.7
- 前端管理端：Vue 2.6 + Element UI 2.13
- 前端用户端：原生HTML + LayUI + jQuery
- 认证：简单Token机制
- 存储：本地文件上传

**核心业务模块：**
1. 用户系统：学生(xuesheng)、教师(laoshi)、管理员(users)
2. 问答系统：学生问题(xueshengwenti)、教师回答(laoshihuida)
3. 社交系统：关注列表(guanzhuliebiao)、交流区(forum)、收藏(storeup)
4. 内容管理：科目类型(kemuleixing)、教师信息(laoshixinxi)
5. 互动功能：评论系统(discuss*)、点赞/点踩、点击统计

### 重构目标

**升级后技术栈：**
- 后端：Spring Boot 3.2+ + Spring Security 6 + JWT + MyBatis-Plus 3.5
- 前端：Vue 3 + Vite + Pinia + Element Plus + TypeScript
- 缓存：Redis 7.0+（缓存、会话、分布式锁）
- 存储：腾讯云COS（图片、富文本资源）
- 部署：Docker + Nginx + Docker Compose

---

## Phase 1: 用户体系与鉴权基础 (Sprint 1-2)

### 1.1 后端基础架构搭建

#### 任务 1.1.1: 初始化Spring Boot 3项目

**执行步骤：**

```bash
# 创建新项目目录
mkdir qa-system-refactor
cd qa-system-refactor
```

**pom.xml 配置：**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.1</version>
        <relativePath/>
    </parent>
    
    <groupId>com.qasystem</groupId>
    <artifactId>qa-system-backend</artifactId>
    <version>2.0.0</version>
    <name>qa-system-backend</name>
    <description>师生答疑系统 - 重构版后端</description>
    
    <properties>
        <java.version>17</java.version>
        <mybatis-plus.version>3.5.5</mybatis-plus.version>
        <jwt.version>0.12.3</jwt.version>
        <hutool.version>5.8.25</hutool.version>
        <cos.version>5.6.155</cos.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- Spring Security -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        
        <!-- Spring Data Redis -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        
        <!-- MySQL -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <!-- MyBatis-Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>
        
        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        
        <!-- Hutool工具类 -->
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
            <version>${hutool.version}</version>
        </dependency>
        
        <!-- 腾讯云COS -->
        <dependency>
            <groupId>com.qcloud</groupId>
            <artifactId>cos_api</artifactId>
            <version>${cos.version}</version>
        </dependency>
        
        <!-- Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Actuator (监控) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        
        <!-- Micrometer (指标) -->
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
        </dependency>
        
        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

**项目结构：**

```
qa-system-backend/
├── src/main/java/com/qasystem/
│   ├── QaSystemApplication.java          # 启动类
│   ├── common/                           # 公共模块
│   │   ├── annotation/                   # 注解
│   │   │   ├── RequireRole.java         # 角色权限注解
│   │   │   └── RateLimiter.java         # 限流注解
│   │   ├── aspect/                       # 切面
│   │   │   ├── LogAspect.java           # 日志切面
│   │   │   └── RateLimiterAspect.java   # 限流切面
│   │   ├── constant/                     # 常量
│   │   │   ├── RedisKeyConstant.java    # Redis Key常量
│   │   │   └── RoleConstant.java        # 角色常量
│   │   ├── enums/                        # 枚举
│   │   │   ├── ResultCode.java          # 响应码枚举
│   │   │   └── UserRole.java            # 用户角色枚举
│   │   ├── exception/                    # 异常
│   │   │   ├── BusinessException.java   # 业务异常
│   │   │   └── GlobalExceptionHandler.java # 全局异常处理
│   │   └── util/                         # 工具类
│   │       ├── JwtUtil.java             # JWT工具
│   │       ├── RedisUtil.java           # Redis工具
│   │       └── MD5Util.java             # MD5工具
│   ├── config/                           # 配置类
│   │   ├── RedisConfig.java             # Redis配置
│   │   ├── SecurityConfig.java          # Spring Security配置
│   │   ├── CorsConfig.java              # 跨域配置
│   │   ├── MyBatisPlusConfig.java       # MyBatis-Plus配置
│   │   └── CosConfig.java               # 腾讯云COS配置
│   ├── security/                         # 安全模块
│   │   ├── JwtAuthenticationFilter.java # JWT认证过滤器
│   │   ├── JwtAuthenticationEntryPoint.java # JWT认证入口
│   │   ├── UserDetailsServiceImpl.java  # 用户详情服务
│   │   └── SecurityUser.java            # 安全用户对象
│   ├── controller/                       # 控制器
│   │   ├── AuthController.java          # 认证控制器
│   │   ├── UserController.java          # 用户控制器
│   │   └── AdminController.java         # 管理员控制器
│   ├── service/                          # 服务层
│   │   ├── AuthService.java             # 认证服务
│   │   ├── UserService.java             # 用户服务
│   │   └── impl/                        # 实现类
│   ├── mapper/                           # 数据访问层
│   │   ├── UserMapper.java
│   │   ├── StudentMapper.java
│   │   └── TeacherMapper.java
│   ├── entity/                           # 实体类
│   │   ├── User.java                    # 管理员实体
│   │   ├── Student.java                 # 学生实体
│   │   └── Teacher.java                 # 教师实体
│   └── dto/                              # 数据传输对象
│       ├── request/                      # 请求DTO
│       │   ├── LoginRequest.java
│       │   ├── RegisterRequest.java
│       │   └── UpdatePasswordRequest.java
│       └── response/                     # 响应DTO
│           ├── LoginResponse.java
│           └── UserInfoResponse.java
└── src/main/resources/
    ├── application.yml                   # 主配置文件
    ├── application-dev.yml              # 开发环境配置
    ├── application-prod.yml             # 生产环境配置
    ├── logback-spring.xml               # 日志配置
    └── mapper/                          # MyBatis XML映射文件
```

---

#### 任务 1.1.2: application.yml 配置

**application.yml:**

```yaml
spring:
  application:
    name: qa-system-backend
  
  profiles:
    active: dev
  
  # 数据源配置
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:qa_system_v2}?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:root}
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  
  # Redis配置
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: 0
      timeout: 3000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
          max-wait: -1ms
  
  # 文件上传配置
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 50MB
      enabled: true

# MyBatis-Plus配置
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.qasystem.entity
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: false
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

# JWT配置
jwt:
  secret: ${JWT_SECRET:qa-system-secret-key-2025-very-long-and-secure}
  expiration: 604800000  # 7天（毫秒）
  refresh-expiration: 2592000000  # 30天（毫秒）

# 腾讯云COS配置
cos:
  secret-id: ${COS_SECRET_ID:your-secret-id}
  secret-key: ${COS_SECRET_KEY:your-secret-key}
  region: ${COS_REGION:ap-beijing}
  bucket-name: ${COS_BUCKET:qa-system-1234567890}
  cdn-domain: ${COS_CDN:https://cdn.example.com}

# Actuator监控配置
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true

# 日志配置
logging:
  level:
    root: INFO
    com.qasystem: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{50} - %msg%n"
```

---

### 1.2 数据库重新设计与迁移

#### 任务 1.2.1: 优化数据库表结构

**创建新数据库迁移SQL：**

```sql
-- ==========================================
-- 师生答疑系统 v2.0 - 数据库迁移脚本
-- ==========================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 删除旧数据库（可选，生产环境慎用）
-- DROP DATABASE IF EXISTS qa_system_v2;

CREATE DATABASE IF NOT EXISTS qa_system_v2 
  DEFAULT CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

USE qa_system_v2;

-- ==========================================
-- 1. 用户体系表
-- ==========================================

-- 1.1 管理员表 (users -> sys_admin)
DROP TABLE IF EXISTS `sys_admin`;
CREATE TABLE `sys_admin` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
  `nickname` VARCHAR(100) COMMENT '昵称',
  `email` VARCHAR(100) COMMENT '邮箱',
  `phone` VARCHAR(20) COMMENT '手机号',
  `avatar` VARCHAR(500) COMMENT '头像URL',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `last_login_time` DATETIME COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) COMMENT '最后登录IP',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_username` (`username`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统管理员表';

-- 1.2 学生表 (xuesheng -> sys_student)
DROP TABLE IF EXISTS `sys_student`;
CREATE TABLE `sys_student` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '学生账号',
  `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
  `real_name` VARCHAR(100) COMMENT '真实姓名',
  `nickname` VARCHAR(100) COMMENT '昵称',
  `gender` TINYINT COMMENT '性别: 0-女 1-男 2-其他',
  `phone` VARCHAR(20) COMMENT '手机号',
  `email` VARCHAR(100) COMMENT '邮箱',
  `avatar` VARCHAR(500) COMMENT '头像URL',
  `student_no` VARCHAR(50) COMMENT '学号',
  `class_name` VARCHAR(100) COMMENT '班级',
  `major` VARCHAR(100) COMMENT '专业',
  `grade` VARCHAR(20) COMMENT '年级',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `last_login_time` DATETIME COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) COMMENT '最后登录IP',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_student_no` (`student_no`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生表';

-- 1.3 教师表 (laoshi -> sys_teacher)
DROP TABLE IF EXISTS `sys_teacher`;
CREATE TABLE `sys_teacher` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '教师账号',
  `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
  `real_name` VARCHAR(100) COMMENT '真实姓名',
  `nickname` VARCHAR(100) COMMENT '昵称',
  `gender` TINYINT COMMENT '性别: 0-女 1-男 2-其他',
  `phone` VARCHAR(20) COMMENT '手机号',
  `email` VARCHAR(100) COMMENT '邮箱',
  `avatar` VARCHAR(500) COMMENT '头像URL',
  `teacher_no` VARCHAR(50) COMMENT '工号',
  `department` VARCHAR(100) COMMENT '所属系部',
  `title` VARCHAR(50) COMMENT '职称',
  `introduction` TEXT COMMENT '个人简介',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `last_login_time` DATETIME COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) COMMENT '最后登录IP',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_teacher_no` (`teacher_no`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教师表';

-- ==========================================
-- 2. 数据迁移脚本（从旧表迁移到新表）
-- ==========================================

-- 迁移管理员数据
INSERT INTO `sys_admin` (username, password, nickname, status, create_time)
SELECT 
  username,
  CONCAT('$2a$10$', password) as password,  -- 注意：需要重新加密
  username as nickname,
  1 as status,
  addtime as create_time
FROM springboot7vkr2.users
WHERE deleted = 0;

-- 迁移学生数据
INSERT INTO `sys_student` (username, password, real_name, gender, phone, avatar, status, create_time)
SELECT 
  xueshengzhanghao as username,
  CONCAT('$2a$10$', mima) as password,  -- 注意：需要重新加密
  xueshengxingming as real_name,
  CASE xingbie WHEN '男' THEN 1 WHEN '女' THEN 0 ELSE 2 END as gender,
  shoujihaoma as phone,
  touxiang as avatar,
  1 as status,
  addtime as create_time
FROM springboot7vkr2.xuesheng;

-- 迁移教师数据
INSERT INTO `sys_teacher` (username, password, real_name, gender, phone, avatar, status, create_time)
SELECT 
  laoshizhanghao as username,
  CONCAT('$2a$10$', mima) as password,  -- 注意：需要重新加密
  laoshixingming as real_name,
  CASE xingbie WHEN '男' THEN 1 WHEN '女' THEN 0 ELSE 2 END as gender,
  shoujihaoma as phone,
  touxiang as avatar,
  1 as status,
  addtime as create_time
FROM springboot7vkr2.laoshi;

SET FOREIGN_KEY_CHECKS = 1;
```

**Redis Key设计规范：**

```
# 用户相关
user:info:{userId}              # 用户基本信息缓存，TTL: 30分钟
user:roles:{userId}             # 用户角色缓存，TTL: 1小时
user:session:{token}            # 用户会话信息，TTL: 7天
user:token:blacklist:{token}    # Token黑名单，TTL: 7天

# 认证相关
auth:captcha:{uuid}             # 验证码，TTL: 5分钟
auth:login:fail:{username}      # 登录失败次数，TTL: 15分钟
auth:sms:code:{phone}           # 短信验证码，TTL: 5分钟

# 限流相关
rate_limit:api:{userId}:{api}   # API限流，TTL: 1分钟
rate_limit:ip:{ip}              # IP限流，TTL: 1分钟
```

---

### 1.3 Spring Security + JWT 认证实现

#### 任务 1.3.1: JWT工具类

**JwtUtil.java:**

```java
package com.qasystem.common.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    /**
     * 生成访问Token
     */
    public String generateToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSecretKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 生成刷新Token
     */
    public String generateRefreshToken(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration);
        
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("type", "refresh")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSecretKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.getSubject();
    }

    /**
     * 从Token中获取角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("role", String.class);
    }

    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 检查Token是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * 获取所有Claims
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 获取密钥
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
```

#### 任务 1.3.2: Spring Security配置

**SecurityConfig.java:**

```java
package com.qasystem.config;

import com.qasystem.security.JwtAuthenticationEntryPoint;
import com.qasystem.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * 认证提供者
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * 安全过滤链配置
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（使用JWT不需要）
            .csrf(AbstractHttpConfigurer::disable)
            
            // 配置异常处理
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )
            
            // 无状态会话（不使用Session）
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 配置URL访问权限
            .authorizeHttpRequests(auth -> auth
                // 公开端点
                .requestMatchers(
                    "/api/v1/auth/**",
                    "/actuator/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                ).permitAll()
                
                // 管理员端点
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                
                // 教师端点
                .requestMatchers("/api/v1/teacher/**").hasAnyRole("TEACHER", "ADMIN")
                
                // 学生端点
                .requestMatchers("/api/v1/student/**").hasAnyRole("STUDENT", "ADMIN")
                
                // 其他请求需要认证
                .anyRequest().authenticated()
            )
            
            // 添加JWT过滤器
            .addFilterBefore(jwtAuthenticationFilter, 
                           UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

**完整代码请查看后续任务...**

---

### 1.4 核心API接口实现

#### API规范

所有API遵循RESTful设计规范：
- **基础路径**: `/api/v1`
- **响应格式**: 统一JSON格式

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1234567890
}
```

#### 认证相关API (AuthController)

| Method | Path | 描述 | 权限 |
|--------|------|------|------|
| POST | `/api/v1/auth/login` | 用户登录 | 公开 |
| POST | `/api/v1/auth/register` | 用户注册 | 公开 |
| POST | `/api/v1/auth/refresh` | 刷新Token | 公开 |
| POST | `/api/v1/auth/logout` | 用户登出 | 认证 |
| GET | `/api/v1/auth/captcha` | 获取验证码 | 公开 |

**示例代码将在下一部分提供...**

---

## ✅ Phase 1 完成标准

- [ ] Spring Boot 3项目成功启动
- [ ] 数据库连接成功，旧数据迁移完成
- [ ] Redis连接成功，可以读写数据
- [ ] JWT Token生成和验证通过单元测试
- [ ] 登录/注册接口返回正确响应
- [ ] Postman测试所有认证接口通过
- [ ] 管理员、教师、学生可以正确登录
- [ ] Token黑名单机制生效
- [ ] API日志正确记录到文件
- [ ] Actuator监控端点可访问

---

**下一步**: Phase 1 详细实现代码 (controller/service/entity等完整代码)

