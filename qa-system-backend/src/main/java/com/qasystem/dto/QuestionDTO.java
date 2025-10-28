package com.qasystem.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 问题DTO
 */
@Data
public class QuestionDTO {
    private Long id;
    private Long subjectId;
    private String subjectName;
    private Long studentId;
    private String studentName;
    private String title;
    private String content;
    private List<String> images;
    private String status;
    private Integer viewCount;
    private Integer isTop;
    private Integer isFeatured;
    private Integer answerCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

