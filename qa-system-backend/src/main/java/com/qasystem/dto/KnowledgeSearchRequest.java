package com.qasystem.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class KnowledgeSearchRequest {

    @NotBlank(message = "检索内容不能为空")
    private String query;

    private String knowledgeBaseId;

    private String knowledgePoint;

    @Min(value = 1, message = "limit 最小为 1")
    @Max(value = 20, message = "limit 最大为 20")
    private Integer limit = 6;
}
