package com.qasystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qasystem.common.response.Result;
import com.qasystem.entity.DocSensitiveWord;
import com.qasystem.service.DocSensitiveWordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 🚫 文档敏感词管理控制器 - 管理员接口
 * 
 * 📖 功能说明：
 * 本控制器提供文档敏感词管理功能的管理员API接口，面向系统管理员。
 * 敏感词管理是文档查重与AI降重系统的重要组成部分，
 * 用于识别和过滤文档中的不当内容，确保文档内容的合规性。
 * 管理员可以添加、修改、删除敏感词，并查看敏感词使用统计。
 * 
 * 🎯 主要功能：
 * 1. 敏感词查询 - 分页查询系统中的所有敏感词
 * 2. 敏感词管理 - 添加、修改、删除敏感词
 * 3. 敏感词分类 - 按类别组织和管理敏感词
 * 4. 敏感词启用/禁用 - 控制敏感词的生效状态
 * 5. 使用统计 - 查看敏感词的命中率和使用情况
 * 
 * 🔧 技术实现：
 * - 基于Spring MVC框架，提供RESTful API接口
 * - 使用Spring Security进行管理员权限验证
 * - 集成MyBatis-Plus分页插件，支持高效分页查询
 * - 提供灵活的查询条件，支持多字段组合查询
 * - 使用统一异常处理，确保API响应一致性
 * 
 * 📋 API设计：
 * - 遵循RESTful设计原则，使用标准HTTP方法
 * - 统一返回格式，使用Result包装响应数据
 * - 支持分页查询，使用page和size参数
 * - 提供灵活的过滤条件，支持多字段查询
 * - 所有接口都需要管理员权限
 * 
 * 🔄 工作流程：
 * 1. 管理员登录系统 → 获取管理员权限
 * 2. 查询敏感词列表 → 了解当前敏感词配置
 * 3. 添加新敏感词 → 扩展敏感词库
 * 4. 修改现有敏感词 → 调整敏感词规则
 * 5. 禁用/启用敏感词 → 控制敏感词生效状态
 * 6. 查看使用统计 → 评估敏感词效果
 * 
 * ⚠️ 注意事项：
 * - 所有接口都需要管理员权限，普通用户无法访问
 * - 敏感词修改会影响所有文档的查重结果
 * - 敏感词删除是不可逆操作，请确认后再执行
 * - 敏感词配置有默认值，修改前请了解其含义
 * - 管理操作会被记录日志，用于审计追踪
 * 
 * @author 师生答疑系统开发团队
 * @since 1.0.0
 */
@Slf4j  // 自动生成日志对象，用于记录操作日志
@RestController  // 标识为RESTful控制器，自动处理JSON序列化
@RequestMapping("/api/v1/admin/doc/sensitive-words")  // 设置基础路径为/api/v1/admin/doc/sensitive-words
@RequiredArgsConstructor  // 为final字段生成构造函数，实现依赖注入
public class DocSensitiveWordAdminController {

    /**
     * 🚫 敏感词管理服务接口 - 处理敏感词的业务逻辑
     * 
     * 该服务封装了敏感词管理的所有业务逻辑，包括：
     * - 敏感词的增删改查
     * - 敏感词分类管理
     * - 敏感词启用/禁用控制
     * - 敏感词使用统计
     * - 敏感词匹配算法
     */
    private final DocSensitiveWordService docSensitiveWordService;

    /**
     * 📄 分页查询敏感词
     * 
     * 📖 功能说明：
     * 分页查询系统中的所有敏感词，支持多条件过滤。
     * 管理员可以查看所有敏感词的详细信息，包括敏感词内容、
     * 分类、状态等，方便进行敏感词管理和监控。
     * 
     * 🔧 技术实现：
     * - 使用MyBatis-Plus分页插件实现高效分页
     * - 支持多条件动态查询，灵活组合过滤条件
     * - 使用LIKE查询实现模糊匹配
     * - 按分类和状态进行精确匹配
     * - 添加查询索引，提高查询性能
     * 
     * 📋 请求参数：
     * @param page 页码，从1开始，默认为1
     * @param size 每页大小，默认为10，最大为100
     * @param keyword 关键词过滤条件，支持模糊匹配，可选
     * @param category 分类过滤条件，可选
     * @param enabled 状态过滤条件，可选
     * 
     * 🔄 返回结果：
     * @return 分页结果对象，包含：
     *         - records: 敏感词记录列表，每个敏感词包含：
     *           - id: 敏感词ID
     *           - word: 敏感词内容
     *           - category: 敏感词分类
     *           - enabled: 是否启用
     *           - createTime: 创建时间
     *           - updateTime: 更新时间
     *         - total: 总记录数
     *         - size: 每页大小
     *         - current: 当前页码
     *         - pages: 总页数
     * 
     * 📝 使用示例：
     * 前端调用示例：
     * ```javascript
     * // 构建查询参数
     * const params = new URLSearchParams({
     *   page: 1,
     *   size: 10,
     *   keyword: '暴力', // 查询包含"暴力"的敏感词
     *   category: '政治', // 查询政治类别的敏感词
     *   enabled: true // 查询启用的敏感词
     * });
     * 
     * fetch(`/api/v1/admin/doc/sensitive-words/page?${params}`, {
     *   headers: {
     *     'Authorization': 'Bearer ' + adminToken
     *   }
     * })
     * .then(response => response.json())
     * .then(data => {
     *   console.log('敏感词列表:', data);
     *   const pageResult = data.data;
     *   // 显示分页信息
     *   document.getElementById('page-info').textContent = 
     *     `第${pageResult.current}页，共${pageResult.pages}页，总计${pageResult.total}条记录`;
     *   // 渲染敏感词列表
     *   const wordListHtml = pageResult.records.map(word => `
     *     <tr>
     *       <td>${word.id}</td>
     *       <td>${word.word}</td>
     *       <td>${word.category}</td>
     *       <td>${word.enabled ? '启用' : '禁用'}</td>
     *       <td>${new Date(word.createTime).toLocaleString()}</td>
     *       <td>
     *         <button onclick="editWord(${word.id})">编辑</button>
     *         <button onclick="toggleWord(${word.id}, ${!word.enabled})">${word.enabled ? '禁用' : '启用'}</button>
     *         <button onclick="deleteWord(${word.id})">删除</button>
     *       </td>
     *     </tr>
     *   `).join('');
     *   document.getElementById('word-table-body').innerHTML = wordListHtml;
     * });
     * ```
     * 
     * ⚠️ 注意事项：
     * - 需要管理员权限才能访问
     * - 每页大小最大为100，超过限制会被重置为100
     * - 关键词查询使用模糊匹配，可能返回较多结果
     * - 查询结果按创建时间倒序排列
     * - 大量数据查询可能较慢，建议合理使用过滤条件
     */
    @GetMapping("/page")
    public Result<IPage<DocSensitiveWord>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean enabled) {
        // 记录查询参数日志
        log.info("分页查询敏感词: page={}, size={}, keyword={}, category={}, enabled={}",
                page, size, keyword, category, enabled);
        // 调用服务层执行分页查询
        IPage<DocSensitiveWord> result = docSensitiveWordService.page(page, size, keyword, category, enabled);
        // 返回分页结果
        return Result.success(result);
    }

    /**
     * ➕ 新增敏感词
     * 
     * 📖 功能说明：
     * 添加新的敏感词到系统中，扩展敏感词库。
     * 新增的敏感词将立即生效，用于后续的文档查重和内容过滤。
     * 
     * 🔧 技术实现：
     * - 验证敏感词参数的有效性
     * - 检查敏感词是否已存在
     * - 保存敏感词到数据库
     * - 更新敏感词缓存
     * - 记录添加操作日志
     * 
     * 📋 请求参数：
     * @param word 敏感词对象，包含：
     *            - word: 敏感词内容（必填）
     *            - category: 敏感词分类（必填）
     *            - enabled: 是否启用（可选，默认为true）
     *            - description: 敏感词描述（可选）
     * 
     * 🔄 返回结果：
     * @return 创建的敏感词对象，包含：
     *         - id: 敏感词ID
     *         - word: 敏感词内容
     *         - category: 敏感词分类
     *         - enabled: 是否启用
     *         - description: 敏感词描述
     *         - createTime: 创建时间
     *         - updateTime: 更新时间
     * 
     * 📝 使用示例：
     * 前端调用示例：
     * ```javascript
     * const newWord = {
     *   word: '不当词汇',
     *   category: '其他',
     *   enabled: true,
     *   description: '不当词汇的描述'
     * };
     * 
     * fetch('/api/v1/admin/doc/sensitive-words', {
     *   method: 'POST',
     *   headers: {
     *     'Content-Type': 'application/json',
     *     'Authorization': 'Bearer ' + adminToken
     *   },
     *   body: JSON.stringify(newWord)
     * })
     * .then(response => response.json())
     * .then(data => {
     *   console.log('创建结果:', data);
     *   if (data.success) {
     *     alert('敏感词创建成功！');
     *     // 刷新敏感词列表
     *     loadWordList();
     *   } else {
     *     alert('敏感词创建失败：' + data.message);
     *   }
     * })
     * .catch(error => {
     *   console.error('创建失败:', error);
     *   alert('敏感词创建失败，请重试');
     * });
     * ```
     * 
     * ⚠️ 注意事项：
     * - 需要管理员权限才能访问
     * - 敏感词内容不能为空
     * - 敏感词分类不能为空
     * - 敏感词内容不能重复
     * - 新增的敏感词会立即生效
     * - 敏感词添加会被记录日志，用于审计追踪
     */
    @PostMapping
    public Result<DocSensitiveWord> create(@RequestBody DocSensitiveWord word) {
        // 记录添加敏感词日志
        log.info("新增敏感词: word={}", word.getWord());
        // 调用服务层创建敏感词
        DocSensitiveWord created = docSensitiveWordService.create(word);
        // 返回创建成功结果
        return Result.success("创建成功", created);
    }

    /**
     * ✏️ 更新敏感词
     * 
     * 📖 功能说明：
     * 修改已存在的敏感词信息，包括敏感词内容、分类、状态等。
     * 修改后的敏感词将立即生效，用于后续的文档查重和内容过滤。
     * 
     * 🔧 技术实现：
     * - 验证敏感词ID和参数的有效性
     * - 检查敏感词是否存在
     * - 检查敏感词内容是否与其他敏感词重复
     * - 更新敏感词信息
     * - 更新敏感词缓存
     * - 记录修改操作日志
     * 
     * 📋 请求参数：
     * @param id 敏感词ID，指定要修改的敏感词
     * @param word 敏感词对象，包含：
     *            - word: 敏感词内容（可选）
     *            - category: 敏感词分类（可选）
     *            - enabled: 是否启用（可选）
     *            - description: 敏感词描述（可选）
     * 
     * 🔄 返回结果：
     * @return 更新后的敏感词对象，包含：
     *         - id: 敏感词ID
     *         - word: 敏感词内容
     *         - category: 敏感词分类
     *         - enabled: 是否启用
     *         - description: 敏感词描述
     *         - createTime: 创建时间
     *         - updateTime: 更新时间
     * 
     * 📝 使用示例：
     * 前端调用示例：
     * ```javascript
     * const wordId = 123;
     * const updatedWord = {
     *   word: '修改后的词汇',
     *   category: '修改后的分类',
     *   enabled: false,
     *   description: '修改后的描述'
     * };
     * 
     * fetch(`/api/v1/admin/doc/sensitive-words/${wordId}`, {
     *   method: 'PUT',
     *   headers: {
     *     'Content-Type': 'application/json',
     *     'Authorization': 'Bearer ' + adminToken
     *   },
     *   body: JSON.stringify(updatedWord)
     * })
     * .then(response => response.json())
     * .then(data => {
     *   console.log('更新结果:', data);
     *   if (data.success) {
     *     alert('敏感词更新成功！');
     *     // 刷新敏感词列表
     *     loadWordList();
     *   } else {
     *     alert('敏感词更新失败：' + data.message);
     *   }
     * })
     * .catch(error => {
     *   console.error('更新失败:', error);
     *   alert('敏感词更新失败，请重试');
     * });
     * ```
     * 
     * ⚠️ 注意事项：
     * - 需要管理员权限才能访问
     * - 敏感词ID必须存在
     * - 敏感词内容不能与其他敏感词重复
     * - 修改后的敏感词会立即生效
     * - 敏感词修改会被记录日志，用于审计追踪
     */
    @PutMapping("/{id}")
    public Result<DocSensitiveWord> update(@PathVariable Long id, @RequestBody DocSensitiveWord word) {
        // 记录更新敏感词日志
        log.info("更新敏感词: id={}, word={}", id, word.getWord());
        // 调用服务层更新敏感词
        DocSensitiveWord updated = docSensitiveWordService.update(id, word);
        // 返回更新成功结果
        return Result.success("更新成功", updated);
    }

    /**
     * 🗑️ 删除敏感词
     * 
     * 📖 功能说明：
     * 删除指定的敏感词，从敏感词库中移除。
     * 此操作不可逆，请谨慎执行。删除后，该敏感词将不再
     * 参与文档查重和内容过滤。
     * 
     * 🔧 技术实现：
     * - 验证敏感词ID的有效性
     * - 检查敏感词是否存在
     * - 检查删除权限
     * - 删除敏感词记录
     * - 更新敏感词缓存
     * - 记录删除操作日志
     * 
     * 📋 请求参数：
     * @param id 敏感词ID，指定要删除的敏感词
     * 
     * 🔄 返回结果：
     * @return 操作结果消息，包含：
     *         - message: 操作结果描述
     *         - data: null（无额外数据）
     * 
     * 📝 使用示例：
     * 前端调用示例：
     * ```javascript
     * function deleteWord(wordId) {
     *   // 确认删除
     *   if (!confirm('确定要删除这个敏感词吗？此操作不可撤销！')) {
     *     return;
     *   }
     *   
     *   fetch(`/api/v1/admin/doc/sensitive-words/${wordId}`, {
     *     method: 'DELETE',
     *     headers: {
     *       'Authorization': 'Bearer ' + adminToken
     *     }
     *   })
     *   .then(response => response.json())
     *   .then(data => {
     *     console.log('删除结果:', data);
     *     if (data.success) {
     *       alert('敏感词删除成功！');
     *       // 刷新敏感词列表
     *       loadWordList();
     *     } else {
     *       alert('敏感词删除失败：' + data.message);
     *     }
     *   })
     *   .catch(error => {
     *     console.error('删除失败:', error);
     *     alert('敏感词删除失败，请重试');
     *   });
     * }
     * ```
     * 
     * ⚠️ 注意事项：
     * - 需要管理员权限才能访问
     * - 删除操作不可逆，请谨慎执行
     * - 删除后，该敏感词将不再参与文档查重和内容过滤
     * - 删除操作会被记录日志，用于审计追踪
     * - 如果敏感词正在被使用，删除可能会影响查重结果
     * - 删除后，相关文档的查重结果可能会发生变化
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        // 记录删除敏感词日志
        log.info("删除敏感词: id={}", id);
        // 调用服务层删除敏感词
        docSensitiveWordService.delete(id);
        // 返回删除成功结果
        return Result.success("删除成功", null);
    }
}
