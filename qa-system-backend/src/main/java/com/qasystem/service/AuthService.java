package com.qasystem.service;

import com.qasystem.dto.LoginRequest;
import com.qasystem.dto.LoginResponse;
import com.qasystem.dto.RegisterRequest;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户注册
     */
    LoginResponse register(RegisterRequest request);

    /**
     * 用户登出
     */
    void logout(String token);

    /**
     * 刷新Token
     */
    LoginResponse refreshToken(String refreshToken);
}

