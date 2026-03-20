package com.qasystem.config;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Locale;

@Configuration
@RequiredArgsConstructor
public class RagEmbeddingConfig {

    private final RagProperties ragProperties;

    @Bean
    @Lazy
    public EmbeddingModel ragEmbeddingModel() {
        String provider = normalizeProvider(ragProperties.getEmbedding().getProvider());
        if ("openai".equals(provider)) {
            return OpenAiEmbeddingModel.builder()
                    .apiKey(trim(ragProperties.getEmbedding().getOpenAiApiKey()))
                    .baseUrl(trim(ragProperties.getEmbedding().getOpenAiBaseUrl()))
                    .modelName(trim(ragProperties.getEmbedding().getOpenAiModelName()))
                    .dimensions(ragProperties.getEmbedding().getDimension())
                    .timeout(Duration.ofSeconds(60))
                    .maxRetries(2)
                    .logRequests(false)
                    .logResponses(false)
                    .build();
        }
        return new AllMiniLmL6V2EmbeddingModel();
    }

    private String normalizeProvider(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase(Locale.ROOT) : "";
    }

    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }
}
