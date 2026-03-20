package com.qasystem.ai.rag.source;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qasystem.ai.rag.InterviewKnowledgePoint;
import com.qasystem.ai.rag.RagIndexDocument;
import com.qasystem.config.RagProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeedKnowledgeSourceProvider implements RagSourceProvider {

    private static final String RESOURCE_PATTERN = "classpath*:knowledge/general/*.jsonl";

    private final RagProperties ragProperties;
    private final ObjectMapper objectMapper;

    @Override
    public String sourceType() {
        return "seed_internet";
    }

    @Override
    public boolean enabled() {
        return ragProperties.getSources().isSeedMarkdownEnabled();
    }

    @Override
    public List<RagIndexDocument> load(String knowledgeBaseId) {
        List<RagIndexDocument> result = new ArrayList<>();
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(RESOURCE_PATTERN);
            for (Resource resource : resources) {
                if (!resource.exists()) {
                    continue;
                }
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!StringUtils.hasText(line)) {
                            continue;
                        }

                        SeedKnowledgeRecord record = objectMapper.readValue(line, SeedKnowledgeRecord.class);
                        String title = clean(record.title);
                        String summary = clean(record.summary);
                        if (!StringUtils.hasText(record.id) || !StringUtils.hasText(title) || !StringUtils.hasText(summary)) {
                            continue;
                        }

                        Map<String, Object> metadata = new HashMap<>();
                        metadata.put("sourceUrl", clean(record.sourceUrl));
                        metadata.put("domain", clean(record.domain));
                        metadata.put("sourceSite", clean(record.sourceSite));
                        metadata.put("language", clean(record.language));
                        metadata.put("source", "internet_seed");

                        result.add(new RagIndexDocument(
                                knowledgeBaseId,
                                "seed:" + record.id.trim(),
                                sourceType(),
                                record.id.trim(),
                                title,
                                buildContent(title, summary, record.domain),
                                normalizeKnowledgePoint(record.domain),
                                clean(record.sourceUrl),
                                metadata
                        ));
                    }
                }
            }
        } catch (Exception ex) {
            log.warn("Load seed knowledge failed. reason={}", ex.getMessage());
        }
        return result;
    }

    private String normalizeKnowledgePoint(String domain) {
        if (!StringUtils.hasText(domain)) {
            return InterviewKnowledgePoint.GENERAL.getCode();
        }
        InterviewKnowledgePoint detected = InterviewKnowledgePoint.fromCode(domain);
        return detected == InterviewKnowledgePoint.GENERAL
                ? InterviewKnowledgePoint.detect(domain).getCode()
                : detected.getCode();
    }

    private String buildContent(String title, String summary, String domain) {
        InterviewKnowledgePoint knowledgePoint = InterviewKnowledgePoint.fromCode(normalizeKnowledgePoint(domain));
        return "知识主题: " + title + "\n"
                + "所属领域: " + knowledgePoint.getDisplayName() + "\n"
                + "核心总结: " + summary + "\n"
                + "面试关注点: " + interviewFocus(knowledgePoint);
    }

    private String interviewFocus(InterviewKnowledgePoint knowledgePoint) {
        return switch (knowledgePoint) {
            case DATA_STRUCTURES -> "定义、核心操作、时间复杂度、空间复杂度、典型应用与常见对比。";
            case ALGORITHMS -> "核心思路、复杂度分析、边界条件、优化手段和适用场景。";
            case COMPUTER_NETWORKS -> "协议层次、交互流程、关键报文、性能问题与高频追问。";
            case OPERATING_SYSTEMS -> "进程线程、调度、内存管理、同步互斥和系统调用语义。";
            case DATABASES -> "数据模型、索引结构、事务隔离、执行计划与工程取舍。";
            case COMPUTER_ARCHITECTURE -> "硬件组成、指令执行路径、缓存层次和性能影响因素。";
            case SIGNALS_AND_SYSTEMS -> "系统性质、卷积、频域分析、典型变换与常见模型。";
            case DIGITAL_SIGNAL_PROCESSING -> "采样定理、量化误差、滤波器、频谱分析和实现复杂度。";
            case JAVA_BACKEND -> "运行时机制、框架职责、并发与分布式场景下的工程实践。";
            case GENERAL -> "定义、关键特性、适用边界和易混淆概念。";
        };
    }

    private String clean(String value) {
        return StringUtils.hasText(value) ? value.replaceAll("\\s+", " ").trim() : null;
    }

    private static class SeedKnowledgeRecord {
        public String id;
        public String domain;
        public String title;
        public String summary;
        public String sourceUrl;
        public String sourceSite;
        public String language;
    }
}
