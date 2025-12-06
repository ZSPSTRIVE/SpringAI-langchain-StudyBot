package com.qasystem.service;

import com.qasystem.dto.SubjectDTO;
import com.qasystem.entity.Subject;

import java.util.List;

/**
 * SubjectService - 科目服务接口
 * 
 * 🎯 作用：管理系统中的科目分类
 * 就像学校的“课程目录”，管理所有可以提问的科目分类，如高等数学、数据结构等。
 * 
 * 📝 主要功能：
 * 1. 科目查询：获取启用的科目列表
 * 2. 科目管理：创建、编辑、删除科目（管理员）
 * 
 * 💡 使用场景：
 * - 学生提问时需要选择科目
 * - 首页展示科目分类，点击查看该科目下的问题
 * - 管理员在后台管理科目
 */
public interface SubjectService {

    /**
     * 获取所有启用的科目
     * 
     * 🎯 功能：查询所有状态为ACTIVE的科目
     * 就像获取学校当前开设的课程列表。
     * 
     * @return 科目列表，按sortOrder排序
     * 
     * 💬 使用场景：
     * - 学生提问时的科目下拉列表
     * - 首页展示科目分类
     * - 问题列表的科目筛选器
     */
    List<SubjectDTO> getAllActiveSubjects();

    /**
     * 根据ID获取科目详情
     * 
     * 🎯 功能：查询某个科目的详细信息
     * 
     * @param id 科目ID
     * @return 科目详情，如果不存在则抛出异常
     * 
     * 💬 使用场景：
     * - 查看科目详情页
     * - 验证科目是否存在
     */
    SubjectDTO getSubjectById(Long id);

    /**
     * 创建新科目
     * 
     * 🎯 功能：管理员添加新的科目分类
     * 就像学校新开设一门课程。
     * 
     * @param subject 科目实体，包含名称、代码、描述等
     * @return 创建成功的科目
     * 
     * 💬 使用场景：
     * - 管理员在后台“科目管理”页面点击“添加科目”
     * - 填写科目名称、代码、描述等信息
     * - 提交后调用此方法
     */
    SubjectDTO createSubject(Subject subject);

    /**
     * 更新科目信息
     * 
     * 🎯 功能：管理员修改科目的信息
     * 
     * @param id 科目ID
     * @param subject 新的科目数据
     * @return 更新后的科目
     * 
     * 💬 使用场景：
     * - 管理员修改科目名称、描述或排序顺序
     * - 启用或停用某个科目
     */
    SubjectDTO updateSubject(Long id, Subject subject);

    /**
     * 删除科目
     * 
     * 🎯 功能：管理员删除科目
     * 注意：如果科目下已有问题，应该禁止删除或者将问题转移到其他科目。
     * 
     * @param id 科目ID
     * 
     * 💬 使用场景：
     * - 管理员删除不再需要的科目
     * - 建议使用“停用”而非删除，以保留历史数据
     */
    void deleteSubject(Long id);
}

