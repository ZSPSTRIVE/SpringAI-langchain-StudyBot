package com.qasystem.config;

import com.qasystem.entity.AiModelConfig;
import com.qasystem.service.AiModelConfigService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * LangChain4j 模型配置。
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class LangChainConfig {

    private final AiModelConfigService aiModelConfigService;

    @Value("${langchain4j.open-ai.api-key:}")
    private String defaultApiKey;

    @Value("${langchain4j.open-ai.base-url:https://api.siliconflow.cn/v1}")
    private String defaultBaseUrl;

    @Value("${langchain4j.open-ai.model-name:Qwen/Qwen2.5-7B-Instruct}")
    private String defaultModelName;

    @Value("${langchain4j.open-ai.temperature:0.7}")
    private Double defaultTemperature;

    @Value("${langchain4j.open-ai.max-tokens:2000}")
    private Integer defaultMaxTokens;

    @Value("${qa.ai.fail-fast-on-missing-model:false}")
    private boolean failFastOnMissingModel;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        RuntimeModelConfig config = resolveRuntimeConfig();
        validateRuntimeConfig(config);

        return OpenAiChatModel.builder()
                .apiKey(config.apiKey())
                .baseUrl(config.baseUrl())
                .modelName(config.modelName())
                .temperature(config.temperature())
                .maxTokens(config.maxTokens())
                .timeout(Duration.ofSeconds(60))
                .logRequests(false)
                .logResponses(false)
                .build();
    }

    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        RuntimeModelConfig config = resolveRuntimeConfig();
        validateRuntimeConfig(config);

        return OpenAiStreamingChatModel.builder()
                .apiKey(config.apiKey())
                .baseUrl(config.baseUrl())
                .modelName(config.modelName())
                .temperature(config.temperature())
                .maxTokens(config.maxTokens())
                .timeout(Duration.ofSeconds(120))
                .logRequests(false)
                .logResponses(false)
                .build();
    }

    private RuntimeModelConfig resolveRuntimeConfig() {
        AiModelConfig activeConfig = null;
        try {
            activeConfig = aiModelConfigService.getActiveConfig();
        } catch (Exception ex) {
            log.warn("Load active AI model config from DB failed, fallback to yml. reason={}", ex.getMessage());
        }

        if (activeConfig == null) {
            log.info("Use default AI model config from application.yml. model={}", defaultModelName);
            return new RuntimeModelConfig(
                    normalize(defaultApiKey),
                    normalize(defaultBaseUrl),
                    normalize(defaultModelName),
                    defaultTemperature,
                    defaultMaxTokens
            );
        }

        String apiKey = firstNonBlank(activeConfig.getApiKey(), defaultApiKey);
        String baseUrl = firstNonBlank(activeConfig.getBaseUrl(), defaultBaseUrl);
        String modelName = firstNonBlank(activeConfig.getModelName(), defaultModelName);
        Double temperature = activeConfig.getTemperature() != null ? activeConfig.getTemperature() : defaultTemperature;
        Integer maxTokens = activeConfig.getMaxTokens() != null ? activeConfig.getMaxTokens() : defaultMaxTokens;

        log.info("Use active AI model config from DB. provider={}, model={}",
                activeConfig.getProviderName(), activeConfig.getModelDisplayName());

        return new RuntimeModelConfig(
                normalize(apiKey),
                normalize(baseUrl),
                normalize(modelName),
                temperature,
                maxTokens
        );
    }

    private void validateRuntimeConfig(RuntimeModelConfig config) {
        if (StringUtils.hasText(config.apiKey())
                && StringUtils.hasText(config.baseUrl())
                && StringUtils.hasText(config.modelName())) {
            return;
        }

        String message = "AI model config is incomplete. Please check langchain4j.open-ai.* or activate a DB config.";
        if (failFastOnMissingModel) {
            throw new IllegalStateException(message);
        }
        log.warn(message);
    }

    private String firstNonBlank(String first, String second) {
        String normalizedFirst = normalize(first);
        if (StringUtils.hasText(normalizedFirst)) {
            return normalizedFirst;
        }
        return normalize(second);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private record RuntimeModelConfig(
            String apiKey,
            String baseUrl,
            String modelName,
            Double temperature,
            Integer maxTokens
    ) {
    }
}
