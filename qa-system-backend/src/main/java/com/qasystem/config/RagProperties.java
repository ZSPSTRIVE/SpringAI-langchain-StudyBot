package com.qasystem.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "qa.rag")
public class RagProperties {

    private boolean enabled = true;
    private String defaultKnowledgeBase = "intern-rag-playbook";
    private boolean bootstrapSchemaOnStartup = true;
    private boolean seedOnStartup = true;
    private boolean fullSyncOnStartup = false;

    private int topK = 4;
    private int denseTopK = 6;
    private int keywordTopK = 6;
    private int maxKeywords = 8;
    private int maxContextLength = 2200;
    private int snippetLength = 320;
    private int chunkSize = 480;
    private int chunkOverlap = 80;
    private int minChunkLength = 40;
    private int embeddingBatchSize = 16;
    private double minVectorScore = 0.55D;

    private final SourceProperties sources = new SourceProperties();
    private final MilvusProperties milvus = new MilvusProperties();
    private final EmbeddingProperties embedding = new EmbeddingProperties();

    @Data
    public static class SourceProperties {
        private boolean seedMarkdownEnabled = true;
        private boolean questionAnswerEnabled = true;
        private boolean docParagraphEnabled = true;
        private int questionLimit = 300;
        private int documentLimit = 200;
    }

    @Data
    public static class MilvusProperties {
        private String uri = "http://127.0.0.1:19530";
        private String collectionName = "qa_rag_chunk_store";
        private String databaseName = "default";
        private String metricType = "COSINE";
        private String indexType = "IVF_FLAT";
        private boolean autoFlushOnInsert = true;
        private boolean retrieveEmbeddingsOnSearch = false;
    }

    @Data
    public static class EmbeddingProperties {
        private String provider = "local";
        private String openAiApiKey = "";
        private String openAiBaseUrl = "https://api.siliconflow.cn/v1";
        private String openAiModelName = "BAAI/bge-m3";
        private Integer dimension;
    }
}
