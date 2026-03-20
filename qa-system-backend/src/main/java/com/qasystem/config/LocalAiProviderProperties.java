package com.qasystem.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class LocalAiProviderProperties {

    private boolean enable = true;
    private String key;
    private final List<ProviderProperties> providers = new ArrayList<>();
    private final FailoverProperties failover = new FailoverProperties();

    @Data
    public static class ProviderProperties {
        private String name;
        private Integer priority = 100;
        private boolean enabled = true;
        private String apiKey;
        private String baseUrl;
        private String model;
        private Double temperature = 0.7D;
        private Integer maxTokens = 2000;
        private Integer timeout = 60;
    }

    @Data
    public static class FailoverProperties {
        private Integer failureThreshold = 5;
        private Integer timeout = 60;
        private Integer healthCheckInterval = 30;
        private Integer retryCount = 3;
        private Integer retryInterval = 1000;
    }
}
