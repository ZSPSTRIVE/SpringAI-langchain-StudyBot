package com.qasystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qasystem.common.response.Result;
import com.qasystem.entity.DocConfig;
import com.qasystem.entity.DocDocument;
import com.qasystem.service.DocAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 📄 文档查重与AI降重管理控制器 - 管理员接口
 * 
 * 📖 功能说明：
 * 本控制器提供文档查重与AI降重功能的管理员API接口，面向系统管理员。
 * 管理员可以配置查重与降重参数，查看所有用户的文档列表，
 * 管理文档内容，监控系统使用情况，以及处理异常情况。
 * 这些功能帮助管理员维护系统正常运行，优化服务质量。
 * 
 * 🎯 主要功能：
 * 1. 查重与降重配置管理 - 设置相似度阈值、AI模型参数等
 * 2. 文档列表查询 - 分页查看所有用户的文档信息
 * 3. 文档详情查看 - 查看文档内容和查重报告
 * 4. 文档管理 - 删除不当内容，处理用户投诉
 * 5. 系统监控 - 查看使用统计，分析系统性能
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
 * 2. 查看系统配置 → 调整查重和降重参数
 * 3. 查询文档列表 → 筛选需要处理的文档
 * 4. 查看文档详情 → 评估内容合规性
 * 5. 执行管理操作 → 删除或修改文档
 * 6. 监控系统状态 → 分析使用数据和性能指标
 * 
 * ⚠️ 注意事项：
 * - 所有接口都需要管理员权限，普通用户无法访问
 * - 配置修改会影响所有用户，请谨慎操作
 * - 文档删除是不可逆操作，请确认后再执行
 * - 系统配置有默认值，修改前请了解其含义
 * - 管理操作会被记录日志，用于审计追踪
 * 
 * @author 师生答疑系统开发团队
 * @since 1.0.0
 */
@Slf4j  // 自动生成日志对象，用于记录操作日志
@RestController  // 标识为RESTful控制器，自动处理JSON序列化
@RequestMapping("/api/v1/admin/doc")  // 设置基础路径为/api/v1/admin/doc
@RequiredArgsConstructor  // 为final字段生成构造函数，实现依赖注入
public class DocAdminController {

    /**
     * 📄 文档管理服务接口 - 处理文档查重与AI降重的管理业务逻辑
     * 
     * 该服务封装了文档管理的所有业务逻辑，包括：
     * - 系统配置管理
     * - 文档查询和统计
     * - 文档内容管理
     * - 用户行为监控
     * - 系统性能分析
     */
    private final DocAdminService docAdminService;

    /**
     * ⚙️ 获取查重与降重配置（分组结构）
     * 
     * 📖 功能说明：
     * 获取系统当前的查重与降重配置，以分组结构返回。
     * 配置包括相似度阈值、AI模型参数、文件上传限制等，
     * 这些配置影响整个系统的行为，管理员可以查看和修改。
     * 
     * 🔧 技术实现：
     * - 从数据库查询所有配置项
     * - 按功能模块分组组织配置
     * - 添加配置说明和默认值信息
     * - 格式化数据，便于前端展示
     * - 缓存配置信息，提高访问性能
     * 
     * 🔄 返回结果：
     * @return 包含以下分组配置的Map对象：
     *         - similarity: 相似度相关配置
     *           - threshold: 相似度阈值
     *           - algorithm: 相似度算法
     *         - aiModel: AI模型相关配置
     *           - provider: AI服务提供商
     *           - model: 模型名称
     *           - temperature: 温度参数
     *         - fileUpload: 文件上传相关配置
     *           - maxSize: 最大文件大小
     *           - allowedTypes: 允许的文件类型
     *         - system: 系统相关配置
     *           - cacheExpire: 缓存过期时间
     *           - maxVersions: 最大版本数
     * 
     * 📝 使用示例：
     * 前端调用示例：
     * ```javascript
     * fetch('/api/v1/admin/doc/config', {
     *   headers: {
     *     'Authorization': 'Bearer ' + adminToken
     *   }
     * })
     * .then(response => response.json())
     * .then(data => {
     *   console.log('系统配置:', data);
     *   const config = data.data;
     *   // 显示相似度阈值
     *   document.getElementById('similarity-threshold').value = config.similarity.threshold;
     *   // 显示AI模型配置
     *   document.getElementById('ai-provider').value = config.aiModel.provider;
     *   document.getElementById('ai-model').value = config.aiModel.model;
     *   document.getElementById('ai-temperature').value = config.aiModel.temperature;
     * });
     * ```
     * 
     * ⚠️ 注意事项：
     * - 需要管理员权限才能访问
     * - 配置信息是只读的，修改需要调用保存接口
     * - 配置信息会缓存5分钟，修改后需要等待缓存过期
     * - 某些配置修改需要重启服务才能生效
     * - 敏感配置信息会被过滤，不会返回给前端
     */
    @GetMapping("/config")
    public Result<Map<String, Object>> getConfig() {
        // 调用服务层获取配置信息
        Map<String, Object> config = docAdminService.getConfig();
        // 返回配置信息
        return Result.success(config);
    }

    /**
     * 💾 保存查重与降重配置
     * 
     * 📖 功能说明：
     * 保存管理员修改的查重与降重配置，更新系统参数。
     * 配置修改会立即生效（除需要重启的配置外），
     * 影响所有用户的查重和降重体验。
     * 
     * 🔧 技术实现：
     * - 接收分组配置数据
     * - 验证配置参数的有效性
     * - 转换为数据库存储格式
     * - 批量更新数据库中的配置项
     * - 清除相关缓存，确保配置立即生效
     * - 记录配置修改日志，便于审计追踪
     * 
     * 📋 请求参数：
     * @param config 分组配置对象，包含：
     *              - similarity: 相似度相关配置
     *              - aiModel: AI模型相关配置
     *              - fileUpload: 文件上传相关配置
     *              - system: 系统相关配置
     * 
     * 🔄 返回结果：
     * @return 操作结果消息，包含：
     *         - message: 操作结果描述
     *         - data: null（无额外数据）
     * 
     * 📝 使用示例：
     * 前端调用示例：
     * ```javascript
     * const config = {
     *   similarity: {
     *     threshold: 70,
     *     algorithm: 'cosine'
     *   },
     *   aiModel: {
     *     provider: 'openai',
     *     model: 'gpt-3.5-turbo',
     *     temperature: 0.7
     *   },
     *   fileUpload: {
     *     maxSize: 10,
     *     allowedTypes: ['docx']
     *   },
     *   system: {
     *     cacheExpire: 24,
     *     maxVersions: 10
     *   }
     * };
     * 
     * fetch('/api/v1/admin/doc/config', {
     *   method: 'PUT',
     *   headers: {
     *     'Content-Type': 'application/json',
     *     'Authorization': 'Bearer ' + adminToken
     *   },
     *   body: JSON.stringify(config)
     * })
     * .then(response => response.json())
     * .then(data => {
     *   console.log('保存结果:', data);
     *   if (data.success) {
     *     alert('配置保存成功！');
     *   } else {
     *     alert('配置保存失败：' + data.message);
     *   }
     * });
     * ```
     * 
     * ⚠️ 注意事项：
     * - 需要管理员权限才能访问
     * - 配置修改会影响所有用户，请谨慎操作
     * - 某些配置修改需要重启服务才能生效
     * - 配置修改会被记录日志，用于审计追踪
     * - 如果配置参数无效，会返回详细错误信息
     * - 系统会验证配置的合理性，防止设置危险值
     */
    @PutMapping("/config")
    public Result<Void> saveConfig(@RequestBody Map<String, Object> config) {
        // 调用服务层保存配置
        docAdminService.saveConfig(config);
        // 返回保存成功结果
        return Result.success("保存成功", null);
    }

    /**
     * 📋 获取所有配置原始列表（可用于调试或高级配置页面）
     * 
     * 📖 功能说明：
     * 获取所有配置项的原始列表，不进行分组处理。
     * 此接口主要用于系统调试、问题排查或高级配置页面，
     * 提供更底层的配置信息，方便专业人员进行精细调整。
     * 
     * 🔧 技术实现：
     * - 从数据库查询所有配置项
     * - 不进行分组处理，保持原始结构
     * - 包含配置的元数据信息，如创建时间、修改时间等
     * - 按配置键排序，便于查找
     * - 过滤敏感配置信息，确保安全
     * 
     * 🔄 返回结果：
     * @return 配置项列表，每个配置项包含：
     *         - id: 配置ID
     *         - configKey: 配置键
     *         - configValue: 配置值
     *         - configType: 配置类型
     *         - description: 配置描述
     *         - isSystem: 是否为系统配置
     *         - createTime: 创建时间
     *         - updateTime: 修改时间
     * 
     * 📝 使用示例：
     * 前端调用示例：
     * ```javascript
     * fetch('/api/v1/admin/doc/config/list', {
     *   headers: {
     *     'Authorization': 'Bearer ' + adminToken
     *   }
     * })
     * .then(response => response.json())
     * .then(data => {
     *   console.log('配置列表:', data);
     *   const configs = data.data;
     *   // 创建配置表格
     *   const tableHtml = configs.map(config => `
     *     <tr>
     *       <td>${config.configKey}</td>
     *       <td>${config.configValue}</td>
     *       <td>${config.description}</td>
     *       <td>${config.isSystem ? '系统' : '用户'}</td>
     *       <td>${new Date(config.updateTime).toLocaleString()}</td>
     *       <td>
     *         <button onclick="editConfig('${config.configKey}')">编辑</button>
     *         ${config.isSystem ? '' : '<button onclick="deleteConfig(' + config.id + ')">删除</button>'}
     *       </td>
     *     </tr>
     *   `).join('');
     *   document.getElementById('config-table-body').innerHTML = tableHtml;
     * });
     * ```
     * 
     * ⚠️ 注意事项：
     * - 需要管理员权限才能访问
     * - 此接口返回原始配置，可能包含技术细节
     * - 系统配置不能删除，只能修改
     * - 敏感配置信息会被过滤，不会返回给前端
     * - 修改配置建议使用分组接口，此接口主要用于查看
     */
    @GetMapping("/config/list")
    public Result<List<DocConfig>> listAllConfigs() {
        // 调用服务层获取所有配置
        List<DocConfig> configs = docAdminService.listAllConfigs();
        // 返回配置列表
        return Result.success(configs);
    }

    /**
     * 📄 分页查询文档列表
     * 
     * 📖 功能说明：
     * 分页查询系统中的所有文档，支持多条件过滤。
     * 管理员可以查看所有用户的文档信息，包括文档标题、
     * 上传用户、相似度、状态等，方便进行内容管理和监控。
     * 
     * 🔧 技术实现：
     * - 使用MyBatis-Plus分页插件实现高效分页
     * - 支持多条件动态查询，灵活组合过滤条件
     * - 使用LIKE查询实现模糊匹配
     * - 关联查询用户信息，减少数据库访问次数
     * - 添加查询索引，提高查询性能
     * 
     * 📋 请求参数：
     * @param page 页码，从1开始，默认为1
     * @param size 每页大小，默认为10，最大为100
     * @param userId 用户ID过滤条件，可选
     * @param title 标题过滤条件，支持模糊匹配，可选
     * @param status 状态过滤条件，可选
     * 
     * 🔄 返回结果：
     * @return 分页结果对象，包含：
     *         - records: 文档记录列表，每个文档包含：
     *           - id: 文档ID
     *           - title: 文档标题
     *           - userId: 上传用户ID
     *           - username: 上传用户名
     *           - totalSimilarity: 总体相似度
     *           - status: 文档状态
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
     *   title: '论文', // 查询标题包含"论文"的文档
     *   status: 'high_similarity' // 查询高相似度的文档
     * });
     * 
     * fetch(`/api/v1/admin/doc/documents?${params}`, {
     *   headers: {
     *     'Authorization': 'Bearer ' + adminToken
     *   }
     * })
     * .then(response => response.json())
     * .then(data => {
     *   console.log('文档列表:', data);
     *   const pageResult = data.data;
     *   // 显示分页信息
     *   document.getElementById('page-info').textContent = 
     *     `第${pageResult.current}页，共${pageResult.pages}页，总计${pageResult.total}条记录`;
     *   // 渲染文档列表
     *   const documentListHtml = pageResult.records.map(doc => `
     *     <tr>
     *       <td>${doc.id}</td>
     *       <td>${doc.title}</td>
     *       <td>${doc.username}</td>
     *       <td>${doc.totalSimilarity}%</td>
     *       <td>${doc.status}</td>
     *       <td>${new Date(doc.createTime).toLocaleString()}</td>
     *       <td>
     *         <button onclick="viewDocument(${doc.id})">查看</button>
     *         <button onclick="deleteDocument(${doc.id})">删除</button>
     *       </td>
     *     </tr>
     *   `).join('');
     *   document.getElementById('document-table-body').innerHTML = documentListHtml;
     * });
     * ```
     * 
     * ⚠️ 注意事项：
     * - 需要管理员权限才能访问
     * - 每页大小最大为100，超过限制会被重置为100
     * - 标题查询使用模糊匹配，可能返回较多结果
     * - 查询结果按创建时间倒序排列
     * - 大量数据查询可能较慢，建议合理使用过滤条件
     */
    @GetMapping("/documents")
    public Result<IPage<DocDocument>> pageDocuments(@RequestParam(defaultValue = "1") Integer page,
                                                    @RequestParam(defaultValue = "10") Integer size,
                                                    @RequestParam(required = false) Long userId,
                                                    @RequestParam(required = false) String title,
                                                    @RequestParam(required = false) String status) {
        // 记录查询参数日志
        log.info("分页查询文档: page={}, size={}, userId={}, title={}, status={}",
                page, size, userId, title, status);
        // 调用服务层执行分页查询
        IPage<DocDocument> result = docAdminService.pageDocuments(page, size, userId, title, status);
        // 返回分页结果
        return Result.success(result);
    }

    /**
     * 🔍 获取单个文档详情（包含段落统计）
     * 
     * 📖 功能说明：
     * 获取指定文档的详细信息，包括文档基本信息和段落统计。
     * 管理员可以查看文档的完整内容、查重报告、版本历史等，
     * 用于评估文档质量和处理用户投诉。
     * 
     * 🔧 技术实现：
     * - 根据文档ID查询文档基本信息
     * - 获取文档的所有段落信息
     * - 计算段落统计信息，如高相似度段落数量
     * - 查询文档的版本历史
     * - 获取用户的操作日志
     * - 格式化数据，便于前端展示
     * 
     * 📋 请求参数：
     * @param documentId 文档ID，指定要查询的文档
     * 
     * 🔄 返回结果：
     * @return 文档详情对象，包含：
     *         - document: 文档基本信息
     *           - id: 文档ID
     *           - title: 文档标题
     *           - userId: 上传用户ID
     *           - username: 上传用户名
     *           - totalSimilarity: 总体相似度
     *           - status: 文档状态
     *           - createTime: 创建时间
     *           - updateTime: 更新时间
     *         - statistics: 统计信息
     *           - paragraphCount: 段落数量
     *           - highSimilarityCount: 高相似度段落数量
     *           - mediumSimilarityCount: 中等相似度段落数量
     *           - lowSimilarityCount: 低相似度段落数量
     *         - versions: 版本历史列表
     *         - recentOperations: 最近操作记录
     * 
     * 📝 使用示例：
     * 前端调用示例：
     * ```javascript
     * const documentId = 123;
     * 
     * fetch(`/api/v1/admin/doc/documents/${documentId}`, {
     *   headers: {
     *     'Authorization': 'Bearer ' + adminToken
     *   }
     * })
     * .then(response => response.json())
     * .then(data => {
     *   console.log('文档详情:', data);
     *   const detail = data.data;
     *   // 显示文档基本信息
     *   document.getElementById('document-title').textContent = detail.document.title;
     *   document.getElementById('document-user').textContent = detail.document.username;
     *   document.getElementById('document-similarity').textContent = detail.document.totalSimilarity + '%';
     *   // 显示统计信息
     *   document.getElementById('paragraph-count').textContent = detail.statistics.paragraphCount;
     *   document.getElementById('high-similarity-count').textContent = detail.statistics.highSimilarityCount;
     *   // 显示版本历史
     *   const versionListHtml = detail.versions.map(version => `
     *     <div class="version-item">
     *       <h4>${version.name}</h4>
     *       <p>${version.description}</p>
     *       <p>创建时间: ${new Date(version.createTime).toLocaleString()}</p>
     *     </div>
     *   `).join('');
     *   document.getElementById('version-history').innerHTML = versionListHtml;
     * });
     * ```
     * 
     * ⚠️ 注意事项：
     * - 需要管理员权限才能访问
     * - 文档详情包含完整内容，可能较大
     * - 查看文档详情不会影响文档状态
     * - 文档被删除后，无法再查看详情
     * - 频繁查询可能影响性能，建议合理使用
     */
    @GetMapping("/documents/{documentId}")
    public Result<Map<String, Object>> getDocumentDetail(@PathVariable Long documentId) {
        // 调用服务层获取文档详情
        Map<String, Object> detail = docAdminService.getDocumentDetail(documentId);
        // 返回文档详情
        return Result.success(detail);
    }
    
    /**
     * 🗑️ 删除文档（级联删除段落和版本）
     * 
     * 📖 功能说明：
     * 删除指定的文档，包括所有相关的段落和版本记录。
     * 此操作不可逆，请谨慎执行。删除后，用户将无法访问
     * 该文档及其所有相关数据。
     * 
     * 🔧 技术实现：
     * - 验证文档是否存在
     * - 检查删除权限
     * - 开启数据库事务
     * - 删除文档的所有版本记录
     * - 删除文档的所有段落记录
     * - 删除文档记录
     * - 删除相关的缓存数据
     * - 记录删除操作日志
     * 
     * 📋 请求参数：
     * @param documentId 文档ID，指定要删除的文档
     * 
     * 🔄 返回结果：
     * @return 操作结果消息，包含：
     *         - message: 操作结果描述
     *         - data: "删除成功"
     * 
     * 📝 使用示例：
     * 前端调用示例：
     * ```javascript
     * function deleteDocument(documentId) {
     *   // 确认删除
     *   if (!confirm('确定要删除这个文档吗？此操作不可撤销！')) {
     *     return;
     *   }
     *   
     *   fetch(`/api/v1/admin/doc/documents/${documentId}`, {
     *     method: 'DELETE',
     *     headers: {
     *       'Authorization': 'Bearer ' + adminToken
     *     }
     *   })
     *   .then(response => response.json())
     *   .then(data => {
     *     console.log('删除结果:', data);
     *     if (data.success) {
     *       alert('文档删除成功！');
     *       // 刷新文档列表
     *       loadDocumentList();
     *     } else {
     *       alert('文档删除失败：' + data.message);
     *     }
     *   })
     *   .catch(error => {
     *     console.error('删除失败:', error);
     *     alert('文档删除失败，请重试');
     *   });
     * }
     * ```
     * 
     * ⚠️ 注意事项：
     * - 需要管理员权限才能访问
     * - 删除操作不可逆，请谨慎执行
     * - 删除会级联删除所有相关数据，包括段落和版本
     * - 删除操作会被记录日志，用于审计追踪
     * - 如果文档正在被处理，删除可能会失败
     * - 删除后，用户将无法访问该文档及其所有相关数据
     */
    @DeleteMapping("/documents/{documentId}")
    public Result<String> deleteDocument(@PathVariable Long documentId) {
        // 调用服务层删除文档
        docAdminService.deleteDocument(documentId);
        // 返回删除成功结果
        return Result.success("删除成功");
    }
}
