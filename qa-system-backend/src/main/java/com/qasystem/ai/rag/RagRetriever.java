package com.qasystem.ai.rag;

import java.util.List;

public interface RagRetriever {

    List<RagCandidate> retrieve(RagQuery query, int topK);
}
