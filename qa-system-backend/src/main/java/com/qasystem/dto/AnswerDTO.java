package com.qasystem.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 回答DTO
 */
@Data
public class AnswerDTO {
    private Long id;
    private Long questionId;
    private Long teacherId;
    private String teacherName;
    private String teacherTitle;
    private String content;
    private List<String> images;
    private Integer isAccepted;
    private Integer likeCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

