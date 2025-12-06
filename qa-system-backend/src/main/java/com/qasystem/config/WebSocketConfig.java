package com.qasystem.config;

import com.qasystem.websocket.DocRewriteWebSocketHandler;
import com.qasystem.websocket.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket配置：用于文档降重流式改写和实时聊天
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final DocRewriteWebSocketHandler docRewriteWebSocketHandler;
    private final ChatWebSocketHandler chatWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 文档降重WebSocket
        registry.addHandler(docRewriteWebSocketHandler, "/ws/doc-rewrite")
                .setAllowedOriginPatterns("*");
        
        // 聊天WebSocket
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .setAllowedOriginPatterns("*");
    }
}
