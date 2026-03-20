package com.qasystem.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import jakarta.servlet.DispatcherType;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security配置
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("🔧 配置Spring Security - 论坛路径 /forum/** 已设置为公开访问");
        
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                .authorizeHttpRequests(auth -> auth
                        // 允许异步分发请求（SSE等）
                        .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()
                        
                        // 公开接口（无需认证）
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        
                        // CORS预检请求（OPTIONS）必须允许
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        
                        // 论坛接口（旧系统路径，公开访问列表和详情，发布需要认证）- 必须在anyRequest之前
                        .requestMatchers(HttpMethod.GET, "/forum/flist").permitAll()
                        .requestMatchers(HttpMethod.GET, "/forum/list/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/forum/page").permitAll()
                        .requestMatchers(HttpMethod.GET, "/forum/query").permitAll()
                        .requestMatchers(HttpMethod.GET, "/forum/info/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/forum/detail/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/forum/add").authenticated()
                        .requestMatchers(HttpMethod.POST, "/forum/save").authenticated()
                        .requestMatchers(HttpMethod.POST, "/forum/update").authenticated()
                        .requestMatchers(HttpMethod.POST, "/forum/delete").authenticated()
                        .requestMatchers("/forum/**").permitAll()
                        
                        // WebSocket 通道（文档降重、聊天）
                        .requestMatchers("/ws/**").permitAll()
                        
                        // 聊天接口（需要认证）
                        .requestMatchers("/api/v1/chat/**").authenticated()
                        
                        // SSE 流式接口（异步请求需要特殊处理）
                        .requestMatchers("/api/ai/chat/stream").authenticated()
                        .requestMatchers("/api/ai/knowledge/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/ai/interview/scenes").hasRole("ADMIN")
                        
                        // 科目相关（查询公开，管理需要管理员）
                        .requestMatchers(HttpMethod.GET, "/api/v1/subjects/**").permitAll()
                        .requestMatchers("/api/v1/subjects/**").hasRole("ADMIN")
                        
                        // 问题相关（查询公开，发布需要学生，删除需要作者或管理员）
                        .requestMatchers(HttpMethod.GET, "/api/v1/questions").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/questions/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/questions").hasAnyRole("STUDENT", "ADMIN")
                        .requestMatchers("/api/v1/questions/**").authenticated()
                        
                        // 回答相关（查询公开，创建需要教师，操作需要作者或管理员）
                        .requestMatchers(HttpMethod.GET, "/api/v1/answers/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/answers").hasAnyRole("TEACHER", "ADMIN")
                        .requestMatchers("/api/v1/answers/**").authenticated()
                        
                        // 关注和收藏（需要认证）
                        .requestMatchers("/api/v1/follows/**").authenticated()
                        .requestMatchers("/api/v1/collections/**").authenticated()
                        
                        // 个人中心（需要认证）
                        .requestMatchers("/api/v1/profile/**").authenticated()
                        
                        // 文件上传（需要认证）
                        .requestMatchers("/api/v1/upload/**").authenticated()
                        
                        // 静态资源（公开访问）
                        .requestMatchers("/uploads/**").permitAll()
                        
                        // 管理员接口
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        
                        // 其他接口需要认证
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允许所有来源（开发环境）
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // 允许发送Cookie
        configuration.setAllowCredentials(true);
        
        // 暴露的响应头
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "X-Requested-With",
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"
        ));
        
        // 预检请求缓存时间（秒）
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        log.info("🌐 CORS配置已启用 - 允许所有来源");
        
        return source;
    }
}

