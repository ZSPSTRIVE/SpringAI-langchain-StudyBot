package com.qasystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 创建回答请求DTO
 */
@Data
public class CreateAnswerRequest {

    @NotNull(message = "问题ID不能为空")
    private Long questionId;

    @NotBlank(message = "回答内容不能为空")
    private String content;

    private List<String> images;
}

