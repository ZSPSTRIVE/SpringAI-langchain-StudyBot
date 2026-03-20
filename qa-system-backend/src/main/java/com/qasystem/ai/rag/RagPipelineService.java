package com.qasystem.ai.rag;

import com.qasystem.config.RagProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagPipelineService {

    private final RagRetriever ragRetriever;
    private final RagContextAssembler ragContextAssembler;
    private final RagQueryFactory ragQueryFactory;
    private final RagProperties ragProperties;

    public RagContextResult buildContext(String userQuestion) {
        return buildContext(userQuestion, null, null, null);
    }

    public RagContextResult buildContext(String userQuestion, String knowledgeBaseId, String knowledgePoint) {
        return buildContext(userQuestion, knowledgeBaseId, knowledgePoint, null);
    }

    public RagContextResult buildContext(String userQuestion,
                                         String knowledgeBaseId,
                                         String knowledgePoint,
                                         String messageType) {
        if (!StringUtils.hasText(userQuestion)) {
            return RagContextResult.empty();
        }

        RagQuery query = ragQueryFactory.build(userQuestion, knowledgeBaseId, knowledgePoint, messageType);
        if (!ragProperties.isEnabled() || !query.useRag()) {
            return RagContextResult.empty(query);
        }

        try {
            List<RagCandidate> candidates = ragRetriever.retrieve(query, ragProperties.getTopK());
            return ragContextAssembler.assemble(query, candidates, ragProperties.getMaxContextLength());
        } catch (Exception ex) {
            log.warn("RAG pipeline execute failed, fallback to no-context. reason={}", ex.getMessage());
            return RagContextResult.empty(query);
        }
    }
}
