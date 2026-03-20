package com.qasystem.ai.rag;

import com.qasystem.config.RagProperties;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class RagVectorStoreProvider {

    private final RagProperties ragProperties;

    @Lazy
    private final EmbeddingModel ragEmbeddingModel;

    private volatile MilvusEmbeddingStore store;

    public MilvusEmbeddingStore getStore() {
        MilvusEmbeddingStore current = store;
        if (current != null) {
            return current;
        }

        synchronized (this) {
            if (store == null) {
                store = MilvusEmbeddingStore.builder()
                        .uri(ragProperties.getMilvus().getUri())
                        .collectionName(ragProperties.getMilvus().getCollectionName())
                        .databaseName(ragProperties.getMilvus().getDatabaseName())
                        .dimension(resolveDimension())
                        .indexType(resolveIndexType())
                        .metricType(resolveMetricType())
                        .autoFlushOnInsert(ragProperties.getMilvus().isAutoFlushOnInsert())
                        .retrieveEmbeddingsOnSearch(ragProperties.getMilvus().isRetrieveEmbeddingsOnSearch())
                        .build();
            }
            return store;
        }
    }

    public int resolveDimension() {
        Integer configured = ragProperties.getEmbedding().getDimension();
        if (configured != null && configured > 0) {
            return configured;
        }
        return ragEmbeddingModel.dimension();
    }

    private MetricType resolveMetricType() {
        String configured = normalize(ragProperties.getMilvus().getMetricType());
        if (!StringUtils.hasText(configured)) {
            return MetricType.COSINE;
        }
        return MetricType.valueOf(configured);
    }

    private IndexType resolveIndexType() {
        String configured = normalize(ragProperties.getMilvus().getIndexType());
        if (!StringUtils.hasText(configured)) {
            return IndexType.IVF_FLAT;
        }
        return IndexType.valueOf(configured);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }
}
