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
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class LangChainConfig {

    private static final double DEFAULT_TEMPERATURE = 0.7D;
    private static final int DEFAULT_MAX_TOKENS = 2000;
    private static final int DEFAULT_TIMEOUT_SECONDS = 60;

    private final AiModelConfigService aiModelConfigService;
    private final LocalAiProviderProperties localAiProviderProperties;
    private final JdbcTemplate jdbcTemplate;

    @Value("${langchain4j.open-ai.api-key:}")
    private String openAiApiKey;

    @Value("${langchain4j.open-ai.base-url:}")
    private String openAiBaseUrl;

    @Value("${langchain4j.open-ai.model-name:}")
    private String openAiModelName;

    @Value("${langchain4j.open-ai.temperature:0.7}")
    private Double openAiTemperature;

    @Value("${langchain4j.open-ai.max-tokens:2000}")
    private Integer openAiMaxTokens;

    @Value("${qa.ai.fail-fast-on-missing-model:false}")
    private boolean failFastOnMissingModel;

    private volatile RuntimeModelConfig resolvedRuntimeConfig;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        RuntimeModelConfig config = resolveRuntimeConfig();
        if (!config.isComplete()) {
            return buildDisabledChatModel();
        }

        return OpenAiChatModel.builder()
                .apiKey(config.apiKey())
                .baseUrl(config.baseUrl())
                .modelName(config.modelName())
                .temperature(config.temperature())
                .maxTokens(config.maxTokens())
                .timeout(Duration.ofSeconds(config.timeoutSeconds()))
                .maxRetries(2)
                .logRequests(false)
                .logResponses(false)
                .build();
    }

    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        RuntimeModelConfig config = resolveRuntimeConfig();
        if (!config.isComplete()) {
            return buildDisabledStreamingChatModel();
        }

        return OpenAiStreamingChatModel.builder()
                .apiKey(config.apiKey())
                .baseUrl(config.baseUrl())
                .modelName(config.modelName())
                .temperature(config.temperature())
                .maxTokens(config.maxTokens())
                .timeout(Duration.ofSeconds(config.timeoutSeconds()))
                .logRequests(false)
                .logResponses(false)
                .build();
    }

    private RuntimeModelConfig resolveRuntimeConfig() {
        RuntimeModelConfig cached = resolvedRuntimeConfig;
        if (cached != null) {
            return cached;
        }

        synchronized (this) {
            if (resolvedRuntimeConfig != null) {
                return resolvedRuntimeConfig;
            }

            RuntimeModelConfig databaseConfig = resolveFromDatabase();
            if (databaseConfig != null) {
                log.info("Use AI model config from database. provider={}, model={}",
                        databaseConfig.provider(), databaseConfig.modelName());
                resolvedRuntimeConfig = databaseConfig;
                return databaseConfig;
            }

            RuntimeModelConfig localProviderConfig = resolveFromLocalProviders();
            if (localProviderConfig != null) {
                log.info("Use AI model config from local ai.providers. provider={}, model={}",
                        localProviderConfig.provider(), localProviderConfig.modelName());
                resolvedRuntimeConfig = localProviderConfig;
                return localProviderConfig;
            }

            RuntimeModelConfig ymlConfig = resolveFromApplicationYml();
            if (ymlConfig != null) {
                log.info("Use AI model config from application.yml. model={}", ymlConfig.modelName());
                resolvedRuntimeConfig = ymlConfig;
                return ymlConfig;
            }

            String message = "AI provider is not configured. Configure one of: active ai_model_config, "
                    + "local ai.providers[], or langchain4j.open-ai.*";
            if (failFastOnMissingModel) {
                throw new IllegalStateException(message);
            }

            log.warn("{} Application will start with AI features disabled.", message);
            resolvedRuntimeConfig = RuntimeModelConfig.unavailable();
            return resolvedRuntimeConfig;
        }
    }

    private RuntimeModelConfig resolveFromDatabase() {
        try {
            if (!aiModelConfigTableExists()) {
                log.info("ai_model_config table is absent, skip DB model lookup.");
                return null;
            }

            AiModelConfig activeConfig = aiModelConfigService.getActiveConfig();
            if (activeConfig == null) {
                return null;
            }

            RuntimeModelConfig config = new RuntimeModelConfig(
                    trim(activeConfig.getProvider()),
                    trim(activeConfig.getApiKey()),
                    trim(activeConfig.getBaseUrl()),
                    trim(activeConfig.getModelName()),
                    defaultTemperature(activeConfig.getTemperature()),
                    defaultMaxTokens(activeConfig.getMaxTokens()),
                    DEFAULT_TIMEOUT_SECONDS
            );

            if (!config.isComplete()) {
                log.warn("Active AI model config from database is incomplete, fallback to other sources.");
                return null;
            }
            return config;
        } catch (Exception ex) {
            log.warn("Load active AI model config from DB failed, fallback to local/yml. reason={}", ex.getMessage());
            return null;
        }
    }

    private RuntimeModelConfig resolveFromLocalProviders() {
        if (!localAiProviderProperties.isEnable()) {
            return null;
        }

        List<LocalAiProviderProperties.ProviderProperties> providers = localAiProviderProperties.getProviders();
        if (providers == null || providers.isEmpty()) {
            return null;
        }

        return providers.stream()
                .filter(Objects::nonNull)
                .filter(LocalAiProviderProperties.ProviderProperties::isEnabled)
                .sorted(Comparator.comparingInt(this::providerPriority))
                .map(this::toRuntimeModelConfig)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private RuntimeModelConfig toRuntimeModelConfig(LocalAiProviderProperties.ProviderProperties provider) {
        RuntimeModelConfig config = new RuntimeModelConfig(
                trim(provider.getName()),
                trim(provider.getApiKey()),
                trim(provider.getBaseUrl()),
                trim(provider.getModel()),
                defaultTemperature(provider.getTemperature()),
                defaultMaxTokens(provider.getMaxTokens()),
                defaultTimeout(provider.getTimeout())
        );

        if (!config.isComplete()) {
            log.warn("Skip local AI provider because api-key/base-url/model is incomplete. provider={}",
                    safeProviderName(provider.getName()));
            return null;
        }
        return config;
    }

    private RuntimeModelConfig resolveFromApplicationYml() {
        RuntimeModelConfig config = new RuntimeModelConfig(
                "openai-compatible",
                trim(openAiApiKey),
                trim(openAiBaseUrl),
                trim(openAiModelName),
                defaultTemperature(openAiTemperature),
                defaultMaxTokens(openAiMaxTokens),
                DEFAULT_TIMEOUT_SECONDS
        );

        return config.isComplete() ? config : null;
    }

    private ChatLanguageModel buildDisabledChatModel() {
        return messages -> {
            throw buildMissingConfigException();
        };
    }

    private StreamingChatLanguageModel buildDisabledStreamingChatModel() {
        return (messages, handler) -> {
            IllegalStateException exception = buildMissingConfigException();
            if (handler != null) {
                handler.onError(exception);
                return;
            }
            throw exception;
        };
    }

    private IllegalStateException buildMissingConfigException() {
        return new IllegalStateException(
                "AI provider is not configured. Set a DB active model, local ai.providers[], "
                        + "or langchain4j.open-ai.* before calling AI endpoints."
        );
    }

    private int providerPriority(LocalAiProviderProperties.ProviderProperties provider) {
        return provider.getPriority() == null ? Integer.MAX_VALUE : provider.getPriority();
    }

    private double defaultTemperature(Double value) {
        return value == null ? DEFAULT_TEMPERATURE : value;
    }

    private int defaultMaxTokens(Integer value) {
        return value == null || value <= 0 ? DEFAULT_MAX_TOKENS : value;
    }

    private int defaultTimeout(Integer value) {
        return value == null || value <= 0 ? DEFAULT_TIMEOUT_SECONDS : value;
    }

    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }

    private String safeProviderName(String value) {
        return StringUtils.hasText(value) ? value.trim() : "unknown";
    }

    private boolean aiModelConfigTableExists() {
        return Boolean.TRUE.equals(jdbcTemplate.execute((ConnectionCallback<Boolean>) connection ->
                tableExists(connection, connection.getCatalog(), connection.getSchema(), "ai_model_config")
                        || tableExists(connection, connection.getCatalog(), null, "ai_model_config")
        ));
    }

    private boolean tableExists(java.sql.Connection connection, String catalog, String schema, String tableName)
            throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getTables(catalog, schema, null, new String[]{"TABLE"})) {
            while (rs.next()) {
                String name = rs.getString("TABLE_NAME");
                if (tableName.equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    private record RuntimeModelConfig(
            String provider,
            String apiKey,
            String baseUrl,
            String modelName,
            Double temperature,
            Integer maxTokens,
            Integer timeoutSeconds
    ) {

        private static RuntimeModelConfig unavailable() {
            return new RuntimeModelConfig(
                    "disabled",
                    "",
                    "",
                    "",
                    DEFAULT_TEMPERATURE,
                    DEFAULT_MAX_TOKENS,
                    DEFAULT_TIMEOUT_SECONDS
            );
        }

        private boolean isComplete() {
            return StringUtils.hasText(apiKey)
                    && StringUtils.hasText(baseUrl)
                    && StringUtils.hasText(modelName);
        }
    }
}
