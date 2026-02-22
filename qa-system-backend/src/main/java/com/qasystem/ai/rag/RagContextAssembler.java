package com.qasystem.ai.rag;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 负责把召回候选拼装成可喂给 LLM 的上下文。
 */
@Component
public class RagContextAssembler {

    public RagContextResult assemble(List<RagCandidate> candidates, int maxContextLength) {
        if (candidates == null || candidates.isEmpty() || maxContextLength <= 0) {
            return RagContextResult.empty();
        }

        StringBuilder contextBuilder = new StringBuilder();
        List<String> citations = new ArrayList<>();
        int used = 0;

        for (int i = 0; i < candidates.size(); i++) {
            RagCandidate candidate = candidates.get(i);
            if (!StringUtils.hasText(candidate.snippet())) {
                continue;
            }

            String citation = "[" + (i + 1) + "] #" + candidate.questionId() + " " + candidate.title();
            String block = "参考片段 " + citation + "\n"
                    + "标题: " + candidate.title() + "\n"
                    + "内容: " + candidate.snippet() + "\n\n";

            if (used + block.length() > maxContextLength) {
                break;
            }

            contextBuilder.append(block);
            citations.add(citation);
            used += block.length();
        }

        if (contextBuilder.length() == 0) {
            return RagContextResult.empty();
        }
        return new RagContextResult(contextBuilder.toString().trim(), citations, citations.size());
    }
}
