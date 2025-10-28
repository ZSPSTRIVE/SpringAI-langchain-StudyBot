package com.qasystem.service;

import com.qasystem.dto.SubjectDTO;
import com.qasystem.entity.Subject;

import java.util.List;

/**
 * 科目服务接口
 */
public interface SubjectService {

    /**
     * 获取所有启用的科目
     */
    List<SubjectDTO> getAllActiveSubjects();

    /**
     * 根据ID获取科目
     */
    SubjectDTO getSubjectById(Long id);

    /**
     * 创建科目
     */
    SubjectDTO createSubject(Subject subject);

    /**
     * 更新科目
     */
    SubjectDTO updateSubject(Long id, Subject subject);

    /**
     * 删除科目
     */
    void deleteSubject(Long id);
}

