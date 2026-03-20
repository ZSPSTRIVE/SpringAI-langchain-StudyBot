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
public class KnowledgeSyncResponse {

    private String taskId;
    private String knowledgeBaseId;
    private List<String> sourceTypes;
    private String status;
    private Integer sourceDocumentCount;
    private Integer documentCount;
    private Integer chunkCount;
    private Integer failedDocumentCount;
    private String message;
}
