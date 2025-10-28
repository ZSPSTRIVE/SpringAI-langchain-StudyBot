package com.qasystem.dto;

import lombok.Data;

/**
 * 科目DTO
 */
@Data
public class SubjectDTO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private String icon;
    private Integer sortOrder;
    private String status;
}

