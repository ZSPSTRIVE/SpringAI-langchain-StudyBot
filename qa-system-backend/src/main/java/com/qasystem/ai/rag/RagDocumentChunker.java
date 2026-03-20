package com.qasystem.ai.rag;

import com.qasystem.config.RagProperties;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RagDocumentChunker {

    private final RagProperties ragProperties;

    public List<RagChunkDraft> chunk(RagIndexDocument document) {
        if (document == null || !StringUtils.hasText(document.content())) {
            return List.of();
        }

        List<TextSegment> segments = splitDocument(document);
        List<RagChunkDraft> result = new ArrayList<>();

        for (int i = 0; i < segments.size(); i++) {
            TextSegment segment = segments.get(i);
            String text = segment.text() == null ? "" : segment.text().trim();
            if (text.length() < ragProperties.getMinChunkLength()) {
                continue;
            }

            Metadata metadata = segment.metadata().copy()
                    .put("knowledgeBaseId", document.knowledgeBaseId())
                    .put("documentId", document.documentId())
                    .put("sourceType", document.sourceType())
                    .put("sourceRef", document.sourceRef())
                    .put("title", document.title())
                    .put("knowledgePoint", nullToGeneral(document.knowledgePoint()))
                    .put("chunkIndex", i);

            String chunkId = document.documentId() + "#" + i;
            metadata.put("chunkId", chunkId);

            result.add(new RagChunkDraft(
                    document.knowledgeBaseId(),
                    document.documentId(),
                    chunkId,
                    document.sourceType(),
                    document.sourceRef(),
                    document.title(),
                    nullToGeneral(document.knowledgePoint()),
                    i,
                    TextSegment.from(text, metadata)
            ));
        }

        return result;
    }

    private List<TextSegment> splitDocument(RagIndexDocument document) {
        Metadata metadata = new Metadata()
                .put("knowledgeBaseId", document.knowledgeBaseId())
                .put("documentId", document.documentId())
                .put("sourceType", document.sourceType())
                .put("sourceRef", document.sourceRef())
                .put("title", document.title())
                .put("knowledgePoint", nullToGeneral(document.knowledgePoint()));

        Document source = Document.from(document.content(), metadata);
        if (document.content().length() <= ragProperties.getChunkSize()) {
            return List.of(source.toTextSegment());
        }
        return DocumentSplitters.recursive(
                ragProperties.getChunkSize(),
                ragProperties.getChunkOverlap()
        ).split(source);
    }

    private String nullToGeneral(String knowledgePoint) {
        return StringUtils.hasText(knowledgePoint)
                ? knowledgePoint.trim()
                : InterviewKnowledgePoint.GENERAL.getCode();
    }
}
