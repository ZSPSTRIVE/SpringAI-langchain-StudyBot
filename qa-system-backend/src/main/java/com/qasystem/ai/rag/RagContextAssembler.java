package com.qasystem.ai.rag;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class RagContextAssembler {

    public RagContextResult assemble(RagQuery query, List<RagCandidate> candidates, int maxContextLength) {
        if (query == null || candidates == null || candidates.isEmpty() || maxContextLength <= 0) {
            return RagContextResult.empty(query);
        }

        StringBuilder contextBuilder = new StringBuilder();
        List<String> citations = new ArrayList<>();
        int usedLength = 0;

        for (int i = 0; i < candidates.size(); i++) {
            RagCandidate candidate = candidates.get(i);
            if (candidate == null || !StringUtils.hasText(candidate.snippet())) {
                continue;
            }

            String knowledgePointLabel = InterviewKnowledgePoint.fromCode(candidate.knowledgePoint()).getDisplayName();
            String citation = "[" + (i + 1) + "] "
                    + candidate.title()
                    + " | " + knowledgePointLabel
                    + " | " + safe(candidate.sourceType())
                    + ":" + safe(candidate.sourceRef());

            String block = "参考片段 " + citation + "\n"
                    + "标题: " + safe(candidate.title()) + "\n"
                    + "知识点: " + knowledgePointLabel + "\n"
                    + "内容: " + candidate.snippet().trim() + "\n\n";

            if (usedLength + block.length() > maxContextLength) {
                break;
            }

            contextBuilder.append(block);
            citations.add(citation);
            usedLength += block.length();
        }

        if (contextBuilder.length() == 0) {
            return RagContextResult.empty(query);
        }

        return new RagContextResult(
                contextBuilder.toString().trim(),
                citations,
                citations.size(),
                query.interviewScene(),
                query.retrievalMode(),
                query.routeReason()
        );
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
