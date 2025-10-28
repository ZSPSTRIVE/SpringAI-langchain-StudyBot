package com.qasystem.controller;

import com.qasystem.common.response.Result;
import com.qasystem.dto.SubjectDTO;
import com.qasystem.entity.Subject;
import com.qasystem.service.SubjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 科目控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    /**
     * 获取所有启用的科目
     */
    @GetMapping
    public Result<List<SubjectDTO>> getAllActiveSubjects() {
        log.info("获取所有启用的科目");
        List<SubjectDTO> subjects = subjectService.getAllActiveSubjects();
        return Result.success(subjects);
    }

    /**
     * 根据ID获取科目
     */
    @GetMapping("/{id}")
    public Result<SubjectDTO> getSubjectById(@PathVariable Long id) {
        log.info("获取科目详情: id={}", id);
        SubjectDTO subject = subjectService.getSubjectById(id);
        return Result.success(subject);
    }

    /**
     * 创建科目 (管理员)
     */
    @PostMapping
    public Result<SubjectDTO> createSubject(@RequestBody Subject subject) {
        log.info("创建科目: name={}", subject.getName());
        SubjectDTO created = subjectService.createSubject(subject);
        return Result.success("创建成功", created);
    }

    /**
     * 更新科目 (管理员)
     */
    @PutMapping("/{id}")
    public Result<SubjectDTO> updateSubject(@PathVariable Long id, @RequestBody Subject subject) {
        log.info("更新科目: id={}", id);
        SubjectDTO updated = subjectService.updateSubject(id, subject);
        return Result.success("更新成功", updated);
    }

    /**
     * 删除科目 (管理员)
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteSubject(@PathVariable Long id) {
        log.info("删除科目: id={}", id);
        subjectService.deleteSubject(id);
        return Result.success("删除成功", null);
    }
}

