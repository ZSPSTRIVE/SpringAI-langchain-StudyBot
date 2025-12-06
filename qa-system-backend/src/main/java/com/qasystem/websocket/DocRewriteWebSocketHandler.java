package com.qasystem.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qasystem.dto.DocRewriteRequest;
import com.qasystem.service.DocService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;

/**
 * 文档降重 WebSocket 处理器：
 * 客户端发送改写请求，服务端调用AI进行改写，并通过WebSocket按块推送结果，实现流式体验。
 *
 * 协议约定：
 * 客户端发送：{"documentId":1,"paragraphId":101,"text":"原文...","style":"ACADEMIC"}
 * 服务端推送：
 *   {"type":"start"}
 *   {"type":"chunk","content":"部分改写文本"}
 *   ...
 *   {"type":"end"}
 *   或 {"type":"error","message":"错误信息"}
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocRewriteWebSocketHandler extends TextWebSocketHandler {

    private final DocService docService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        try {
            JsonNode root = objectMapper.readTree(message.getPayload());
            DocRewriteRequest request = new DocRewriteRequest();
            if (root.hasNonNull("documentId")) {
                request.setDocumentId(root.get("documentId").asLong());
            }
            if (root.hasNonNull("paragraphId")) {
                request.setParagraphId(root.get("paragraphId").asLong());
            }
            if (root.hasNonNull("text")) {
                request.setText(root.get("text").asText());
            }
            if (root.hasNonNull("style")) {
                request.setStyle(root.get("style").asText());
            }

            // 简化：从查询参数中读取 userId（前端可通过 ?userId=xxx 传递）
            Long userId = null;
            if (session.getUri() != null && session.getUri().getQuery() != null) {
                String[] parts = session.getUri().getQuery().split("&");
                for (String p : parts) {
                    if (p.startsWith("userId=")) {
                        userId = Long.parseLong(p.substring("userId=".length()));
                    }
                }
            }
            if (userId == null) {
                userId = 0L;
            }

            log.info("WebSocket 文档降重请求: userId={}, documentId={}, paragraphId={}, style={}",
                    userId, request.getDocumentId(), request.getParagraphId(), request.getStyle());

            session.sendMessage(new TextMessage("{\"type\":\"start\"}"));

            Map<String, Object> result = docService.rewriteText(userId, request);
            String rewritten = (String) result.get("rewrittenText");
            if (rewritten == null) {
                rewritten = "";
            }

            // 将改写结果按小块推送，模拟流式输出体验
            int chunkSize = 50;
            for (int i = 0; i < rewritten.length(); i += chunkSize) {
                int end = Math.min(rewritten.length(), i + chunkSize);
                String chunk = rewritten.substring(i, end)
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\n", "\\n");
                String json = "{\"type\":\"chunk\",\"content\":\"" + chunk + "\"}";
                session.sendMessage(new TextMessage(json));
            }

            session.sendMessage(new TextMessage("{\"type\":\"end\"}"));
        } catch (Exception e) {
            log.error("WebSocket 文档降重处理失败", e);
            String msg = e.getMessage() == null ? "文档降重失败" : e.getMessage().replace("\"", "'");
            session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"" + msg + "\"}"));
        }
    }
}
