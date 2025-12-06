package com.qasystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LoginResponse - 登录响应数据传输对象
 * 
 * 🎯 作用：用户登录成功后返回给前端的数据
 * 包含登录凭证（token）和用户基本信息，就像“门禁卡+工牌”。
 * 
 * 📝 字段说明：
 * - accessToken: 访问令牌，用于后续请求身份验证
 * - refreshToken: 刷新令牌，用于获取新的accessToken
 * - tokenType: 令牌类型，通常为"Bearer"
 * - expiresIn: token过期时间（秒）
 * - userInfo: 用户信息
 * 
 * 💬 响应示例：
 * {
 *   "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *   "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *   "tokenType": "Bearer",
 *   "expiresIn": 7200,
 *   "userInfo": {
 *     "userId": 1,
 *     "username": "zhangsan",
 *     "realName": "张三",
 *     "role": "STUDENT",
 *     "email": "zhangsan@qq.com",
 *     "avatar": "/uploads/avatar/xxx.jpg"
 *   }
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /** 访问令牌，有效期2小时 */
    private String accessToken;

    /** 刷新令牌，用于获取新的accessToken */
    private String refreshToken;

    /** 令牌类型，固定为"Bearer" */
    private String tokenType;

    /** token过期时间（秒），默认7200秒（2小时） */
    private Long expiresIn;

    /** 用户基本信息 */
    private UserInfo userInfo;

    /**
     * 用户信息内部类
     * 包含用户的基本资料，用于前端显示
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        /** 用户ID */
        private Long userId;
        
        /** 用户名 */
        private String username;
        
        /** 真实姓名 */
        private String realName;
        
        /** 角色：STUDENT/TEACHER/ADMIN */
        private String role;
        
        /** 邮箱 */
        private String email;
        
        /** 头像地址 */
        private String avatar;
    }
}

