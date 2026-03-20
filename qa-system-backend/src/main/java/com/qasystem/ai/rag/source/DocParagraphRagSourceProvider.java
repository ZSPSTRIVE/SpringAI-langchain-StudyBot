package com.qasystem.ai.rag.source;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qasystem.ai.rag.InterviewKnowledgePoint;
import com.qasystem.ai.rag.RagIndexDocument;
import com.qasystem.config.RagProperties;
import com.qasystem.entity.DocDocument;
import com.qasystem.entity.DocParagraph;
import com.qasystem.mapper.DocDocumentMapper;
import com.qasystem.mapper.DocParagraphMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocParagraphRagSourceProvider implements RagSourceProvider {

    private final RagProperties ragProperties;
    private final DocDocumentMapper docDocumentMapper;
    private final DocParagraphMapper docParagraphMapper;

    @Override
    public String sourceType() {
        return "doc_paragraph";
    }

    @Override
    public boolean enabled() {
        return ragProperties.getSources().isDocParagraphEnabled();
    }

    @Override
    public List<RagIndexDocument> load(String knowledgeBaseId) {
        List<DocDocument> documents = docDocumentMapper.selectList(new LambdaQueryWrapper<DocDocument>()
                .orderByDesc(DocDocument::getUpdatedAt)
                .last("LIMIT " + ragProperties.getSources().getDocumentLimit()));

        List<RagIndexDocument> result = new ArrayList<>();
        for (DocDocument document : documents) {
            List<DocParagraph> paragraphs = docParagraphMapper.selectList(
                    new LambdaQueryWrapper<DocParagraph>()
                            .eq(DocParagraph::getDocumentId, document.getId())
                            .orderByAsc(DocParagraph::getParagraphIndex)
            );

            if (paragraphs == null || paragraphs.isEmpty()) {
                continue;
            }

            StringBuilder contentBuilder = new StringBuilder();
            for (DocParagraph paragraph : paragraphs) {
                if (StringUtils.hasText(paragraph.getOriginalText())) {
                    contentBuilder.append(paragraph.getOriginalText().trim()).append('\n');
                }
            }

            String content = contentBuilder.toString().trim();
            if (!StringUtils.hasText(content)) {
                continue;
            }

            String knowledgePoint = InterviewKnowledgePoint.detect(
                    safe(document.getTitle()) + " " + content
            ).getCode();

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("documentId", document.getId());
            metadata.put("paragraphCount", paragraphs.size());
            metadata.put("status", document.getStatus());

            result.add(new RagIndexDocument(
                    knowledgeBaseId,
                    "doc:" + document.getId(),
                    sourceType(),
                    String.valueOf(document.getId()),
                    safe(document.getTitle()),
                    content,
                    knowledgePoint,
                    document.getFileUrl(),
                    metadata
            ));
        }

        log.info("Loaded {} document seed documents for RAG", result.size());
        return result;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
