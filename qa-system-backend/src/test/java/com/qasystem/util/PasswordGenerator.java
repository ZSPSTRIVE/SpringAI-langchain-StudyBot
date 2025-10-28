package com.qasystem.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成工具
 * 用于生成BCrypt加密的测试密码
 */
public class PasswordGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String password = "123456";
        String encodedPassword = encoder.encode(password);
        
        System.out.println("原始密码: " + password);
        System.out.println("加密后的密码: " + encodedPassword);
        System.out.println();
        System.out.println("SQL语句:");
        System.out.println("UPDATE user SET password = '" + encodedPassword + "' WHERE username IN ('admin', 'student1', 'teacher1');");
        
        // 验证
        boolean matches = encoder.matches(password, encodedPassword);
        System.out.println("\n验证结果: " + (matches ? "成功" : "失败"));
    }
}

