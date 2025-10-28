package com.qasystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 师生答疑系统启动类
 * 
 * @author QA System Team
 * @version 2.0.0
 */
@SpringBootApplication
@MapperScan("com.qasystem.mapper")
public class QaSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(QaSystemApplication.class, args);
        System.out.println("""
            
            ========================================
            师生答疑系统 v2.0 启动成功！
            ========================================
            """);
    }
}

