package com.qasystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse {

    private String response;
    private String sessionId;
    private String category;
    private List<ResourceRecommendation> recommendations;
    private Integer tokensUsed;
    private String interviewScene;
    private String interviewSceneLabel;
    private String retrievalMode;
    private Integer ragRecallCount;
    private String routeReason;
    private Long conversationId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceRecommendation {
        private String title;
        private String description;
        private String url;
        private String type;
    }
}
