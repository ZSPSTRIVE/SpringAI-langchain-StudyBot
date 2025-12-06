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
 * 📚 科目控制器 - 问题分类管理，管理学科科目信息
 * 
 * 📖 功能说明：
 * 科目模块用于管理问题的学科分类，如高等数学、计算机网络、数据结构等。
 * 本控制器主要功能包括：
 * 1. 科目查询 - 获取可用的科目列表，供学生提问时选择
 * 2. 科目详情 - 查看单个科目的详细信息
 * 3. 科目创建 - 管理员添加新科目
 * 4. 科目编辑 - 管理员修改科目信息
 * 5. 科目删除 - 管理员停用或删除科目
 * 
 * 🔒 权限控制：
 * - 查询科目：所有登录用户
 * - 创建/编辑/删除：仅管理员
 * 
 * 🌍 RESTful 设计：
 * GET    /api/v1/subjects        获取所有启用科目
 * GET    /api/v1/subjects/{id}   获取科目详情
 * POST   /api/v1/subjects        创建科目（管理员）
 * PUT    /api/v1/subjects/{id}   更新科目（管理员）
 * DELETE /api/v1/subjects/{id}   删除科目（管理员）
 * 
 * 📝 业务规则：
 * - 科目名称不能重复
 * - 科目可以启用/禁用，不直接物理删除
 * - 科目底下有问题时不能删除，只能禁用
 * - 科目支持分级（可扩展）
 * 
 * @author 师生答疑系统开发团队
 * @since 1.0.0
 */
@Slf4j  // 自动生成日志对象
@RestController  // 标识这是一个REST控制器
@RequestMapping("/api/v1/subjects")  // 定义科目接口的基础路径
@RequiredArgsConstructor  // 为final字段生成构造函数
public class SubjectController {

    // 科目服务层接口
    private final SubjectService subjectService;

    /**
     * 📋 获取所有启用的科目 - 供学生提问时选择科目
     * 
     * 业务流程：
     * 1. 查询所有状态为“启用”的科目
     * 2. 按排序字段或名称排序
     * 3. 过滤已删除或禁用的科目
     * 4. 组装科目信息：ID、名称、描述、问题数等
     * 5. 返回科目列表
     * 
     * 请求示例：
     * GET /api/v1/subjects
     * 
     * 成功响应示例：
     * {
     *     "code": 200,
     *     "message": "成功",
     *     "data": [
     *         {
     *             "id": 1,
     *             "name": "高等数学",
     *             "description": "涵盖微积分、线性代数等内容",
     *             "questionCount": 156,
     *             "createdAt": "2024-01-01T00:00:00"
     *         },
     *         {
     *             "id": 2,
     *             "name": "数据结构",
     *             "description": "线性表、树、图等数据结构",
     *             "questionCount": 203,
     *             "createdAt": "2024-01-01T00:00:00"
     *         }
     *     ]
     * }
     * 
     * @return Result<List<SubjectDTO>> 统一响应对象，包含科目列表
     */
    @GetMapping  // 处理GET请求，完整路径：/api/v1/subjects
    public Result<List<SubjectDTO>> getAllActiveSubjects() {
        // 记录查询日志
        log.info("获取所有启用的科目");
        // 调用服务层查询启用的科目
        List<SubjectDTO> subjects = subjectService.getAllActiveSubjects();
        // 返回科目列表
        return Result.success(subjects);
    }

    /**
     * 🔍 根据ID获取科目详情 - 查看科目的完整信息
     * 
     * 业务流程：
     * 1. 根据科目ID查询数据库
     * 2. 验证科目是否存在
     * 3. 组装科目详细信息：基本信息 + 统计数据
     * 4. 返回科目详情
     * 
     * 请求示例：
     * GET /api/v1/subjects/1
     * 
     * 成功响应示例：
     * {
     *     "code": 200,
     *     "message": "成功",
     *     "data": {
     *         "id": 1,
     *         "name": "高等数学",
     *         "description": "涵盖微积分、线性代数等内容",
     *         "questionCount": 156,
     *         "status": "ACTIVE",
     *         "createdAt": "2024-01-01T00:00:00"
     *     }
     * }
     * 
     * @param id 科目ID，从URL路径获取
     * @return Result<SubjectDTO> 统一响应对象，包含科目详情
     * @throws ResourceNotFoundException 当科目不存在时抛出
     */
    @GetMapping("/{id}")  // 处理GET请求，完整路径：/api/v1/subjects/{id}
    public Result<SubjectDTO> getSubjectById(@PathVariable Long id) {
        // 记录查询日志，包含科目ID
        log.info("获取科目详情: id={}", id);
        // 调用服务层查询科目详情
        SubjectDTO subject = subjectService.getSubjectById(id);
        // 返回科目详情
        return Result.success(subject);
    }

    /**
     * ➕ 创建科目 - 管理员添加新的学科科目
     * 
     * 业务流程：
     * 1. 验证请求参数：科目名称不能为空
     * 2. 检查科目名称是否已存在（名称不能重复）
     * 3. 设置默认值：状态=ACTIVE，问题数=0
     * 4. 保存科目到数据库
     * 5. 记录审计日志
     * 6. 返回创建的科目信息
     * 
     * 权限要求：仅管理员可以创建科目
     * 
     * 请求示例：
     * POST /api/v1/subjects
     * Body:
     * {
     *     "name": "操作系统",
     *     "description": "进程管理、内存管理、文件系统等"
     * }
     * 
     * @param subject 科目对象，包含科目名称和描述
     * @return Result<SubjectDTO> 统一响应对象，包含创建的科目信息
     * @throws BusinessException 当科目名称已存在时抛出
     */
    @PostMapping  // 处理POST请求，完整路径：/api/v1/subjects
    public Result<SubjectDTO> createSubject(@RequestBody Subject subject) {
        // 记录创建操作日志，包含科目名称
        log.info("创建科目: name={}", subject.getName());
        // 调用服务层创建科目
        SubjectDTO created = subjectService.createSubject(subject);
        // 返回创建的科目信息
        return Result.success("创建成功", created);
    }

    /**
     * ✏️ 更新科目 - 管理员修改科目信息
     * 
     * 业务流程：
     * 1. 根据ID查询科目是否存在
     * 2. 验证请求参数
     * 3. 如果修改科目名称，检查新名称是否已被其他科目占用
     * 4. 更新科目信息
     * 5. 清除相关缓存
     * 6. 记录审计日志
     * 7. 返回更新后的科目信息
     * 
     * 权限要求：仅管理员可以更新科目
     * 
     * 请求示例：
     * PUT /api/v1/subjects/1
     * Body:
     * {
     *     "name": "高等数学上",
     *     "description": "更新后的描述"
     * }
     * 
     * @param id 科目ID，从URL路径获取
     * @param subject 科目对象，包含要更新的字段
     * @return Result<SubjectDTO> 统一响应对象，包含更新后的科目信息
     * @throws ResourceNotFoundException 当科目不存在时抛出
     * @throws BusinessException 当科目名称已被占用时抛出
     */
    @PutMapping("/{id}")  // 处理PUT请求，完整路径：/api/v1/subjects/{id}
    public Result<SubjectDTO> updateSubject(@PathVariable Long id, @RequestBody Subject subject) {
        // 记录更新操作日志，包含科目ID
        log.info("更新科目: id={}", id);
        // 调用服务层更新科目
        SubjectDTO updated = subjectService.updateSubject(id, subject);
        // 返回更新后的科目信息
        return Result.success("更新成功", updated);
    }

    /**
     * 🗑️ 删除科目 - 管理员移除不再使用的科目
     * 
     * 业务流程：
     * 1. 根据ID查询科目是否存在
     * 2. 检查该科目底下是否有问题
     * 3. 如果有问题，不允许删除，只能禁用
     * 4. 如果没有问题，执行软删除（设置deleted=true）
     * 5. 清除相关缓存
     * 6. 记录审计日志
     * 
     * 删除规则：
     * - 使用软删除，不物理删除数据
     * - 有问题的科目不能删除，只能禁用
     * - 删除后在科目列表中不再显示
     * 
     * 权限要求：仅管理员可以删除科目
     * 
     * 请求示例：
     * DELETE /api/v1/subjects/1
     * 
     * 成功响应：
     * {
     *     "code": 200,
     *     "message": "删除成功",
     *     "data": null
     * }
     * 
     * @param id 科目ID，从URL路径获取
     * @return Result<Void> 无数据返回
     * @throws ResourceNotFoundException 当科目不存在时抛出
     * @throws BusinessException 当科目底下有问题时抛出
     */
    @DeleteMapping("/{id}")  // 处理DELETE请求，完整路径：/api/v1/subjects/{id}
    public Result<Void> deleteSubject(@PathVariable Long id) {
        // 记录删除操作日志，包含科目ID
        log.info("删除科目: id={}", id);
        // 调用服务层删除科目
        subjectService.deleteSubject(id);
        // 返回成功响应
        return Result.success("删除成功", null);
    }
}

