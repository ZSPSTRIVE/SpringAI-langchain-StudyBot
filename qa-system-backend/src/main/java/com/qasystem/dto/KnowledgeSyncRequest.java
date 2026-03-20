package com.qasystem.dto;

import lombok.Data;

import java.util.List;

@Data
public class KnowledgeSyncRequest {

    private String knowledgeBaseId;

    private List<String> sourceTypes;
}
