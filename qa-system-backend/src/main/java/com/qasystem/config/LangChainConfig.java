package com.qasystem.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.dashscope.QwenChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * LangChain4j配置
 */
@Configuration
public class LangChainConfig {
    
    @Value("${langchain4j.dashscope.api-key}")
    private String apiKey;
    
    @Value("${langchain4j.dashscope.model-name:qwen-plus}")
    private String modelName;
    
    @Value("${langchain4j.dashscope.temperature:0.7}")
    private Double temperature;
    
    @Value("${langchain4j.dashscope.max-tokens:2000}")
    private Integer maxTokens;
    
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return QwenChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature.floatValue())
                .maxTokens(maxTokens)
                .build();
    }
}

