package com.qasystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 创建问题请求DTO
 */
@Data
public class CreateQuestionRequest {

    @NotNull(message = "科目不能为空")
    private Long subjectId;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private List<String> images;
}

