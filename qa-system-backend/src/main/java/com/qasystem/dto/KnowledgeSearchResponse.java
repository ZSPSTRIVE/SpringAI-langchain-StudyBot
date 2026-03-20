package com.qasystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeSearchResponse {

    private String knowledgeBaseId;
    private String knowledgePoint;
    private List<String> keywords;
    private List<SearchHit> hits;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchHit {
        private String chunkId;
        private String documentId;
        private String title;
        private String snippet;
        private String knowledgePoint;
        private String knowledgePointLabel;
        private String sourceType;
        private String sourceRef;
        private Double score;
    }
}
