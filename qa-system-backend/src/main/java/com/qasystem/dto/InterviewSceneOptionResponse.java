package com.qasystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewSceneOptionResponse {

    private String code;
    private String displayName;
    private String retrievalMode;
    private Boolean useMilvus;
    private Boolean useKeyword;
    private String description;
}
