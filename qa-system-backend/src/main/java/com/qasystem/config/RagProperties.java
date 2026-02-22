package com.qasystem.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * RAG 运行参数配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "qa.rag")
public class RagProperties {

    /**
     * 是否启用 RAG。
     */
    private boolean enabled = true;

    /**
     * 召回条数上限。
     */
    private int topK = 3;

    /**
     * 是否优先使用 MySQL FULLTEXT 检索，失败时自动回退 LIKE。
     */
    private boolean preferFulltext = true;

    /**
     * query 提取关键词上限。
     */
    private int maxKeywords = 5;

    /**
     * 拼装给大模型的上下文最大字符数。
     */
    private int maxContextLength = 1500;

    /**
     * 单个候选答案片段最大字符数。
     */
    private int snippetLength = 500;
}
