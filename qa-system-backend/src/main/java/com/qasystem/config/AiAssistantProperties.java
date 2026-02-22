package com.qasystem.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 助手业务配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "qa.ai")
public class AiAssistantProperties {

    /**
     * 对话历史携带条数上限。
     */
    private int maxHistorySize = 10;

    /**
     * LLM 调用重试次数（不含首轮）。
     */
    private int maxRequestRetries = 2;

    /**
     * 重试退避毫秒数。
     */
    private long retryBackoffMillis = 400;

    /**
     * 用户输入最大长度。
     */
    private int maxUserMessageLength = 2000;

    /**
     * 会话标题最大长度。
     */
    private int sessionTitleMaxLength = 30;

    /**
     * 分类到推荐资源的映射，可通过配置覆盖。
     * key 为 QuestionCategory.code。
     */
    private Map<String, List<RecommendationItem>> recommendationRules = new HashMap<>();

    @Data
    public static class RecommendationItem {
        private String title;
        private String description;
        private String url;
        private String type = "article";
    }
}
