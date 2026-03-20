package com.qasystem.ai.rag;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qasystem.ai.rag.source.RagSourceProvider;
import com.qasystem.config.RagProperties;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagIngestionService {

    private static final String DEFAULT_DESCRIPTION = "Interview-oriented knowledge base seeded from internet notes, Q&A data, and uploaded documents.";
    private static final String STATUS_SKIPPED = "SKIPPED";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_PARTIAL_SUCCESS = "PARTIAL_SUCCESS";
    private static final String STATUS_FAILED = "FAILED";

    private final RagProperties ragProperties;
    private final RagMetadataRepository ragMetadataRepository;
    private final RagDocumentChunker ragDocumentChunker;
    private final RagVectorStoreProvider ragVectorStoreProvider;
    private final List<RagSourceProvider> ragSourceProviders;
    private final ObjectMapper objectMapper;

    @Lazy
    private final EmbeddingModel ragEmbeddingModel;

    public void initializeKnowledgeBase(String knowledgeBaseId) {
        if (!ragProperties.isEnabled()) {
            return;
        }
        String resolvedKnowledgeBaseId = resolveKnowledgeBaseId(knowledgeBaseId);
        ragMetadataRepository.initializeSchema();
        ragMetadataRepository.ensureKnowledgeBase(resolvedKnowledgeBaseId, DEFAULT_DESCRIPTION);
    }

    public RagSyncResult seedDefaultKnowledgeBase() {
        return syncKnowledgeBase(
                ragProperties.getDefaultKnowledgeBase(),
                enabledSourceTypes().stream()
                        .filter(sourceType -> sourceType.startsWith("seed_"))
                        .toList()
        );
    }

    public RagSyncResult fullSyncDefaultKnowledgeBase() {
        return syncKnowledgeBase(ragProperties.getDefaultKnowledgeBase(), List.of());
    }

    public RagSyncResult syncKnowledgeBase(String knowledgeBaseId, List<String> requestedSourceTypes) {
        String resolvedKnowledgeBaseId = resolveKnowledgeBaseId(knowledgeBaseId);
        initializeKnowledgeBase(resolvedKnowledgeBaseId);

        List<RagSourceProvider> providers = resolveProviders(requestedSourceTypes);
        List<String> sourceTypes = providers.stream().map(RagSourceProvider::sourceType).toList();
        if (providers.isEmpty()) {
            return new RagSyncResult(
                    null,
                    resolvedKnowledgeBaseId,
                    List.of(),
                    STATUS_SKIPPED,
                    ragMetadataRepository.countDocuments(resolvedKnowledgeBaseId),
                    ragMetadataRepository.countIndexedDocuments(resolvedKnowledgeBaseId),
                    ragMetadataRepository.countChunks(resolvedKnowledgeBaseId),
                    0,
                    "No enabled RAG sources selected."
            );
        }

        String taskId = ragMetadataRepository.startSyncTask(resolvedKnowledgeBaseId, "FULL_SYNC", sourceTypes);
        int sourceDocumentCount = 0;
        int documentCount = 0;
        int chunkCount = 0;
        int failedDocuments = 0;

        try {
            for (RagSourceProvider provider : providers) {
                List<RagIndexDocument> documents = provider.load(resolvedKnowledgeBaseId);
                sourceDocumentCount += documents.size();
                for (RagIndexDocument document : documents) {
                    try {
                        chunkCount += upsertDocument(document);
                        documentCount++;
                    } catch (Exception ex) {
                        failedDocuments++;
                        log.warn("RAG sync document failed. sourceType={}, documentId={}, reason={}",
                                provider.sourceType(), document.documentId(), ex.getMessage());
                    }
                }
            }

            String status = determineSyncStatus(sourceDocumentCount, documentCount, chunkCount, failedDocuments);
            String message = buildSummaryMessage(status, sourceDocumentCount, documentCount, chunkCount, failedDocuments);
            ragMetadataRepository.finishSyncTask(taskId, status, sourceDocumentCount, documentCount, chunkCount, failedDocuments, message);
            return new RagSyncResult(
                    taskId,
                    resolvedKnowledgeBaseId,
                    sourceTypes,
                    status,
                    sourceDocumentCount,
                    documentCount,
                    chunkCount,
                    failedDocuments,
                    message
            );
        } catch (Exception ex) {
            ragMetadataRepository.failSyncTask(taskId, ex.getMessage());
            throw new IllegalStateException("RAG knowledge sync failed: " + ex.getMessage(), ex);
        }
    }

    public List<String> enabledSourceTypes() {
        return ragSourceProviders.stream()
                .filter(RagSourceProvider::enabled)
                .map(RagSourceProvider::sourceType)
                .sorted()
                .toList();
    }

    private List<RagSourceProvider> resolveProviders(List<String> requestedSourceTypes) {
        Set<String> requested = normalizeSourceTypes(requestedSourceTypes);
        Predicate<RagSourceProvider> sourcePredicate = requested.isEmpty()
                ? RagSourceProvider::enabled
                : provider -> provider.enabled() && requested.contains(provider.sourceType().toLowerCase(Locale.ROOT));

        return ragSourceProviders.stream()
                .filter(sourcePredicate)
                .sorted((left, right) -> left.sourceType().compareToIgnoreCase(right.sourceType()))
                .toList();
    }

    private int upsertDocument(RagIndexDocument document) {
        removeExistingDocument(document);

        List<RagChunkDraft> chunkDrafts = ragDocumentChunker.chunk(document);
        ragMetadataRepository.saveDocument(document, chunkDrafts.size());
        if (chunkDrafts.isEmpty()) {
            throw new IllegalStateException("No chunks generated after parsing.");
        }

        List<TextSegment> segments = chunkDrafts.stream()
                .map(RagChunkDraft::segment)
                .toList();
        List<String> vectorIds = null;
        try {
            List<Embedding> embeddings = embedSegments(segments);
            vectorIds = ragVectorStoreProvider.getStore().addAll(embeddings, segments);
        } catch (Exception ex) {
            log.warn("RAG vector indexing degraded to metadata-only chunks. documentId={}, reason={}",
                    document.documentId(), ex.getMessage());
        }

        List<RagStoredChunk> storedChunks = toStoredChunks(chunkDrafts, vectorIds);
        ragMetadataRepository.saveChunks(storedChunks);
        return storedChunks.size();
    }

    private void removeExistingDocument(RagIndexDocument document) {
        List<String> vectorIds = ragMetadataRepository.findVectorIdsByDocument(document.knowledgeBaseId(), document.documentId()).stream()
                .filter(StringUtils::hasText)
                .toList();
        if (!vectorIds.isEmpty()) {
            try {
                ragVectorStoreProvider.getStore().removeAll(vectorIds);
            } catch (Exception ex) {
                log.warn("RAG vector cleanup skipped. documentId={}, reason={}",
                        document.documentId(), ex.getMessage());
            }
        }
        ragMetadataRepository.deleteDocument(document.knowledgeBaseId(), document.documentId());
    }

    private List<RagStoredChunk> toStoredChunks(List<RagChunkDraft> chunkDrafts, List<String> vectorIds) {
        List<RagStoredChunk> storedChunks = new ArrayList<>(chunkDrafts.size());
        for (int i = 0; i < chunkDrafts.size(); i++) {
            RagChunkDraft chunkDraft = chunkDrafts.get(i);
            String vectorId = vectorIds != null && vectorIds.size() > i ? vectorIds.get(i) : null;
            storedChunks.add(new RagStoredChunk(
                    chunkDraft.knowledgeBaseId(),
                    chunkDraft.documentId(),
                    chunkDraft.chunkId(),
                    vectorId,
                    chunkDraft.sourceType(),
                    chunkDraft.sourceRef(),
                    chunkDraft.title(),
                    chunkDraft.segment().text(),
                    chunkDraft.knowledgePoint(),
                    chunkDraft.chunkIndex(),
                    toJson(chunkDraft.segment().metadata().toMap())
            ));
        }
        return storedChunks;
    }

    @SuppressWarnings("unchecked")
    private List<Embedding> embedSegments(List<TextSegment> segments) {
        List<Embedding> embeddings = new ArrayList<>(segments.size());
        int batchSize = Math.max(1, ragProperties.getEmbeddingBatchSize());
        for (int start = 0; start < segments.size(); start += batchSize) {
            int end = Math.min(start + batchSize, segments.size());
            List<TextSegment> batch = segments.subList(start, end);
            List<Embedding> batchEmbeddings = (List<Embedding>) ragEmbeddingModel.embedAll(batch).content();
            embeddings.addAll(batchEmbeddings);
        }
        return embeddings;
    }

    private String determineSyncStatus(int sourceDocumentCount, int documentCount, int chunkCount, int failedDocuments) {
        if (sourceDocumentCount <= 0) {
            return STATUS_SKIPPED;
        }
        if (documentCount <= 0 && chunkCount <= 0 && failedDocuments > 0) {
            return STATUS_FAILED;
        }
        if (failedDocuments > 0) {
            return STATUS_PARTIAL_SUCCESS;
        }
        return STATUS_SUCCESS;
    }

    private String buildSummaryMessage(String status,
                                       int sourceDocumentCount,
                                       int documentCount,
                                       int chunkCount,
                                       int failedDocuments) {
        if (STATUS_SKIPPED.equals(status)) {
            return "No source documents were discovered for this sync run.";
        }
        if (STATUS_FAILED.equals(status)) {
            return "FAILED: scanned " + sourceDocumentCount + " documents, indexed 0 documents / 0 chunks, "
                    + failedDocuments + " failed during parsing, chunking, embedding, or indexing.";
        }
        if (STATUS_PARTIAL_SUCCESS.equals(status)) {
            return "PARTIAL_SUCCESS: scanned " + sourceDocumentCount + " documents, indexed "
                    + documentCount + " documents / " + chunkCount + " chunks, "
                    + failedDocuments + " skipped due to errors.";
        }
        return "SUCCESS: indexed " + documentCount + " documents / " + chunkCount
                + " chunks from " + sourceDocumentCount + " scanned documents.";
    }

    private Set<String> normalizeSourceTypes(Collection<String> requestedSourceTypes) {
        if (requestedSourceTypes == null || requestedSourceTypes.isEmpty()) {
            return Set.of();
        }
        return requestedSourceTypes.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String resolveKnowledgeBaseId(String knowledgeBaseId) {
        return StringUtils.hasText(knowledgeBaseId)
                ? knowledgeBaseId.trim()
                : ragProperties.getDefaultKnowledgeBase();
    }

    private String toJson(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException ex) {
            return metadata.toString();
        }
    }
}
