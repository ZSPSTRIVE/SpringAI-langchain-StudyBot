package com.qasystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeOverviewResponse {

    private String knowledgeBaseId;
    private Integer sourceDocumentCount;
    private Integer indexedDocumentCount;
    private Integer chunkCount;
    private List<String> enabledSourceTypes;
    private List<SyncTask> recentTasks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncTask {
        private String taskId;
        private String taskType;
        private String status;
        private Integer sourceDocumentCount;
        private Integer documentCount;
        private Integer chunkCount;
        private Integer failedDocumentCount;
        private String message;
        private LocalDateTime startedAt;
        private LocalDateTime finishedAt;
    }
}
