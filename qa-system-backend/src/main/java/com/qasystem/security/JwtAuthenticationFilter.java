package com.qasystem.security;

import com.qasystem.common.util.JwtUtil;
import com.qasystem.common.util.RedisUtil;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT认证过滤器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String HEADER_AUTH = "Authorization";

    /**
     * 跳过异步分发请求的过滤
     * SSE/异步请求完成后的ASYNC dispatch不需要再次验证
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getDispatcherType() == DispatcherType.ASYNC;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        log.info("JWT过滤器处理请求: {}", requestURI);
        
        try {
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt)) {
                log.info("检测到JWT Token: {}", jwt.substring(0, Math.min(20, jwt.length())) + "...");
                
                if (jwtUtil.validateToken(jwt)) {
                    log.info(" Token验证通过");
                    
                    // 检查Token是否在黑名单中
                    String tokenKey = "token:blacklist:" + jwt;
                    if (Boolean.TRUE.equals(redisUtil.hasKey(tokenKey))) {
                        log.warn(" Token已被列入黑名单");
                        filterChain.doFilter(request, response);
                        return;
                    }

                    Long userId = jwtUtil.getUserIdFromToken(jwt);
                    String username = jwtUtil.getUsernameFromToken(jwt);
                    String role = jwtUtil.getRoleFromToken(jwt);

                    // 创建认证对象
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 设置到Spring Security上下文
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.info("设置用户认证成功: userId={}, username={}, role={}", userId, username, role);
                } else {
                    log.error(" Token验证失败！");
                }
            } else {
                log.info("⚠未检测到Token，跳过认证");
            }
        } catch (Exception ex) {
            log.error(" JWT认证异常: {}", ex.getMessage(), ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取JWT
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(HEADER_AUTH);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}

