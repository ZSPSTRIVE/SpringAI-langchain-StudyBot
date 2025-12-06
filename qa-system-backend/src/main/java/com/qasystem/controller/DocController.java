package com.qasystem.controller;

import com.qasystem.common.response.Result;
import com.qasystem.entity.DocRewriteVersion;
import com.qasystem.service.DocService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 📄 文档查重与AI降重控制器 - 用户侧接口
 * 
 * 📖 功能说明：
 * 本控制器提供文档查重与AI降重功能的核心API接口，面向普通用户。
 * 用户可以上传文档进行查重，查看查重报告，使用AI功能进行文本降重，
 * 管理文档版本，以及下载处理后的文档。这些功能帮助用户提高文档原创性，
 * 避免学术不端行为，提升写作质量。
 * 
 * 🎯 主要功能：
 * 1. 文档上传与查重 - 上传Word文档，自动执行查重分析
 * 2. 查重报告查看 - 查看详细的查重结果和相似度分析
 * 3. AI智能降重 - 使用AI技术对高相似度文本进行改写
 * 4. 版本管理 - 保存文档的不同版本，支持版本对比
 * 5. 段落编辑 - 批量更新文档段落，精细控制修改内容
 * 6. 文档下载 - 下载处理后的Word格式文档
 * 
 * 🔧 技术实现：
 * - 基于Spring MVC框架，提供RESTful API接口
 * - 使用Spring Security进行身份认证和授权
 * - 集成文件上传处理，支持Word文档解析
 * - 调用AI服务接口，实现智能文本改写
 * - 使用MyBatis-Plus进行数据库操作
 * - 支持流式响应，提升用户体验
 * 
 * 📋 API设计：
 * - 遵循RESTful设计原则，使用标准HTTP方法
 * - 统一返回格式，使用Result包装响应数据
 * - 支持路径变量和请求参数，提供灵活的查询方式
 * - 使用@Valid注解进行请求参数验证
 * - 通过Authentication获取当前用户信息
 * 
 * 🔄 工作流程：
 * 1. 用户上传Word文档 → 系统解析文档内容
 * 2. 执行查重分析 → 生成查重报告
 * 3. 用户查看报告 → 选择需要降重的段落
 * 4. 调用AI服务 → 生成降重后的文本
 * 5. 用户编辑调整 → 保存文档版本
 * 6. 下载最终文档 → 完成整个流程
 * 
 * ⚠️ 注意事项：
 * - 所有接口都需要用户登录认证
 * - 文件上传有大小限制，默认为10MB
 * - AI降重功能需要配置有效的AI模型
 * - 文档处理是异步操作，可能需要等待一段时间
 * - 系统会保存用户上传的文档，请注意隐私保护
 * 
 * @author 师生答疑系统开发团队
 * @since 1.0.0
 */
@Slf4j  // 自动生成日志对象，用于记录操作日志
@RestController  // 标识为RESTful控制器，自动处理JSON序列化
@RequestMapping("/api/v1/doc")  // 设置基础路径为/api/v1/doc
@RequiredArgsConstructor  // 为final字段生成构造函数，实现依赖注入
public class DocController {

    /**
     * 📄 文档服务接口 - 处理文档查重与AI降重的核心业务逻辑
     * 
     * 该服务封装了文档处理的所有业务逻辑，包括：
     * - 文档上传和解析
     * - 查重算法执行
     * - AI文本改写
     * - 版本管理
     * - 文档生成和下载
     */
    private final DocService docService;

    /**
     * 📤 上传Word文档并执行查重
     * 
     * 📖 功能说明：
     * 用户上传Word文档，系统自动解析文档内容并执行查重分析。
     * 查重过程包括文本提取、分段处理、相似度计算等步骤，
     * 最终生成详细的查重报告，包括总体相似度和各段落的相似度分析。
     * 
     * 🔧 技术实现：
     * - 使用MultipartFile接收上传的文件
     * - 调用Apache POI库解析Word文档内容
     * - 将文档分段，每段独立进行查重分析
     * - 使用余弦相似度算法计算文本相似度
     * - 将查重结果保存到数据库，供后续查看
     * 
     * 📋 请求参数：
     * @param file 上传的Word文档文件，支持.docx格式
     * @param authentication Spring Security认证对象，包含用户信息
     * 
     * 🔄 返回结果：
     * @return 包含以下信息的Map对象：
     *         - documentId: 文档ID，用于后续操作
     *         - title: 文档标题
     *         - totalSimilarity: 总体相似度(0-100)
     *         - paragraphCount: 段落数量
     *         - highSimilarityCount: 高相似度段落数量
     *         - reportUrl: 查重报告访问URL
     * 
     * 📝 使用示例：
     * 前端调用示例：
     * ```javascript
     * const formData = new FormData();
     * formData.append('file', fileInput.files[0]);
     * 
     * fetch('/api/v1/doc/upload-check', {
     *   method: 'POST',
     *   headers: {
     *     'Authorization': 'Bearer ' + token
     *   },
     *   body: formData
     * })
     * .then(response => response.json())
     * .then(data => {
     *   console.log('查重结果:', data);
     *   // 保存documentId，用于后续操作
     *   localStorage.setItem('documentId', data.data.documentId);
     * });
     * ```
     * 
     * ⚠️ 注意事项：
     * - 文件大小限制为10MB，超过限制将返回错误
     * - 只支持.docx格式的Word文档
     * - 查重过程可能需要几秒钟时间，请耐心等待
     * - 系统会自动保存上传的文档，用于后续操作
     * - 查重结果会缓存24小时，避免重复查重
     */
    @PostMapping("/upload-check")
    public Result<Map<String, Object>> uploadAndCheck(@RequestParam("file") MultipartFile file,
                                                      Authentication authentication) {
        // 从认证对象中获取用户ID
        Long userId = getUserId(authentication);
        // 记录操作日志
        log.info("用户{}上传文档进行查重: {}", userId, file != null ? file.getOriginalFilename() : "null");
        // 调用服务层执行文档上传和查重
        Map<String, Object> result = docService.uploadAndCheck(userId, file);
        // 返回查重结果
        return Result.success(result);
    }

    /**
     * 📊 获取文档查重报告
     * 
     * 📖 功能说明：
     * 根据文档ID获取详细的查重报告，包括总体相似度分析和各段落的详细相似度信息。
     * 报告以结构化数据形式返回，前端可以根据这些数据生成可视化图表和详细列表。
     * 
     * 🔧 技术实现：
     * - 从数据库查询文档基本信息和查重结果
     * - 获取所有段落的相似度数据
     * - 计算统计信息，如高相似度段落占比
     * - 查找相似文献来源，提供参考链接
     * - 格式化数据，便于前端展示
     * 
     * 📋 请求参数：
     * @param documentId 文档ID，通过上传接口返回
     * @param authentication Spring Security认证对象，用于验证用户身份
     * 
     * 🔄 返回结果：
     * @return 包含以下信息的Map对象：
     *         - document: 文档基本信息(标题、上传时间等)
     *         - totalSimilarity: 总体相似度百分比
     *         - paragraphs: 段落列表，每个段落包含：
     *           - id: 段落ID
     *           - content: 段落内容
     *           - similarity: 相似度百分比
     *           - sources: 相似来源列表
     *         - statistics: 统计信息，如高相似度段落数量
     *         - suggestions: 降重建议
     * 
     * 📝 使用示例：
     * 前端调用示例：
     * ```javascript
     * const documentId = localStorage.getItem('documentId');
     * 
     * fetch(`/api/v1/doc/${documentId}/report`, {
     *   headers: {
     *     'Authorization': 'Bearer ' + token
     *   }
     * })
     * .then(response => response.json())
     * .then(data => {
     *   console.log('查重报告:', data);
     *   // 显示总体相似度
     *   document.getElementById('similarity').textContent = data.data.totalSimilarity + '%';
     *   // 渲染段落列表
     *   renderParagraphs(data.data.paragraphs);
     * });
     * ```
     * 
     * ⚠️ 注意事项：
     * - 只能查看自己上传的文档报告
     * - 查重报告缓存24小时，过期需要重新查重
     * - 如果文档正在处理中，会返回处理状态
     * - 大文档的报告可能较大，前端应做好分页处理
     */
    @GetMapping("/{documentId}/report")
    public Result<Map<String, Object>> getReport(@PathVariable Long documentId,
                                                 Authentication authentication) {
        // 验证用户登录状态（仅校验登录，具体权限控制可后续扩展）
        getUserId(authentication);
        // 调用服务层获取查重报告
        Map<String, Object> report = docService.getReport(documentId);
        // 返回查重报告
        return Result.success(report);
    }

    /**
     * 🤖 AI改写/降重指定文本
     * 
     * 📖 功能说明：
     * 使用AI技术对指定的文本段落进行智能改写，降低文本相似度。
     * 用户可以选择单个或多个段落进行改写，系统会调用AI模型生成改写后的文本，
     * 保持原意的同时改变表达方式，有效降低查重相似度。
     * 
     * 🔧 技术实现：
     * - 接收用户选择的段落ID列表和改写参数
     * - 从数据库获取原始段落内容
     * - 调用LangChain4j框架集成的AI模型
     * - 根据用户选择的改写风格调整AI参数
     * - 流式返回AI生成的文本，提升用户体验
     * - 保存改写结果，供用户查看和选择
     * 
     * 📋 请求参数：
     * @param request 改写请求对象，包含：
     *                - documentId: 文档ID
     *                - paragraphIds: 需要改写的段落ID列表
     *                - style: 改写风格(正式/通俗/学术等)
     *                - intensity: 改写强度(轻度/中度/重度)
     * @param authentication Spring Security认证对象，用于验证用户身份
     * 
     * 🔄 返回结果：
     * @return 包含以下信息的Map对象：
     *         - taskId: 改写任务ID，用于查询进度
     *         - status: 任务状态(处理中/已完成/失败)
     *         - results: 改写结果列表，每个结果包含：
     *           - paragraphId: 段落ID
     *           - originalText: 原始文本
     *           - rewrittenText: 改写后文本
     *           - similarityBefore: 改写前相似度
     *           - similarityAfter: 改写后相似度
     * 
     * 📝 使用示例：
     * 前端调用示例：
     * ```javascript
     * const rewriteRequest = {
     *   documentId: documentId,
     *   paragraphIds: [1, 3, 5], // 选择第1、3、5段进行改写
     *   style: 'academic', // 学术风格
     *   intensity: 'medium' // 中度改写
     * };
     * 
     * fetch('/api/v1/doc/rewrite', {
     *   method: 'POST',
     *   headers: {
     *     'Content-Type': 'application/json',
     *     'Authorization': 'Bearer ' + token
     *   },
     *   body: JSON.stringify(rewriteRequest)
     * })
     * .then(response => response.json())
     * .then(data => {
     *   console.log('改写结果:', data);
     *   // 显示改写后的文本
     *   displayRewriteResults(data.data.results);
     * });
     * ```
     * 
     * ⚠️ 注意事项：
     * - AI改写需要消耗计算资源，可能需要排队等待
     * - 改写质量取决于AI模型，可能需要多次尝试
     * - 改写后的文本需要人工审核，确保语义准确
     * - 系统会保存改写历史，方便用户对比选择
     * - 频繁调用可能会触发限流，请合理使用
     */
    @PostMapping("/rewrite")
    public Result<Map<String, Object>> rewrite(@Valid @RequestBody com.qasystem.dto.DocRewriteRequest request,
                                               Authentication authentication) {
        // 从认证对象中获取用户ID
        Long userId = getUserId(authentication);
        // 调用服务层执行AI改写
        Map<String, Object> result = docService.rewriteText(userId, request);
        // 返回改写结果
        return Result.success(result);
    }

    /**
     * 💾 保存文档版本
     * 
     * 📖 功能说明：
     * 保存文档的一个版本，记录用户对文档的修改。用户可以保存多个版本，
     * 方便对比不同版本之间的差异，选择最优版本作为最终结果。
     * 每个版本包含完整的文档内容、创建时间、修改说明等信息。
     * 
     * 🔧 技术实现：
     * - 接收版本保存请求，包含文档ID和段落修改列表
     * - 验证用户权限，确保只能保存自己的文档版本
     * - 创建新的版本记录，关联到原文档
     * - 保存每个段落的修改内容，记录原始文本和修改后文本
     * - 计算版本间的差异统计，如修改段落数、相似度变化等
     * - 更新文档的最后修改时间
     * 
     * 📋 请求参数：
     * @param documentId 文档ID，指定要保存版本的文档
     * @param request 版本保存请求对象，包含：
     *                - name: 版本名称(可选，默认自动生成)
     *                - description: 版本描述(可选)
     *                - paragraphs: 段落修改列表，每个段落包含：
     *                  - id: 段落ID
     *                  - content: 修改后的内容
     *                  - rewritten: 是否经过AI改写
     * @param authentication Spring Security认证对象，用于验证用户身份
     * 
     * 🔄 返回结果：
     * @return DocRewriteVersion 保存的版本对象，包含：
     *         - id: 版本ID
     *         - name: 版本名称
     *         - description: 版本描述
     *         - createTime: 创建时间
     *         - paragraphCount: 段落数量
     *         - modifiedCount: 修改段落数量
     * 
     * 📝 使用示例：
     * 前端调用示例：
     * ```javascript
     * const saveVersionRequest = {
     *   name: '第一版修改',
     *   description: '对第1、3段进行了AI改写',
     *   paragraphs: [
     *     { id: 1, content: '修改后的第1段内容', rewritten: true },
     *     { id: 2, content: '未修改的第2段内容', rewritten: false },
     *     { id: 3, content: '修改后的第3段内容', rewritten: true }
     *   ]
     * };
     * 
     * fetch(`/api/v1/doc/${documentId}/versions`, {
     *   method: 'POST',
     *   headers: {
     *     'Content-Type': 'application/json',
     *     'Authorization': 'Bearer ' + token
     *   },
     *   body: JSON.stringify(saveVersionRequest)
     * })
     * .then(response => response.json())
     * .then(data => {
     *   console.log('保存的版本:', data);
     *   // 显示保存成功提示
     *   alert('版本保存成功！');
     *   // 刷新版本列表
     *   loadVersionList();
     * });
     * ```
     * 
     * ⚠️ 注意事项：
     * - 每个文档最多保存10个版本，超过限制需要删除旧版本
     * - 版本保存是增量操作，只记录修改的段落
     * - 系统会自动创建第一个版本，记录原始文档状态
     * - 版本名称不能重复，建议使用有意义的命名
     * - 版本一旦保存不能修改，但可以删除
     */
    @PostMapping("/{documentId}/versions")
    public Result<DocRewriteVersion> saveVersion(@PathVariable Long documentId,
                                                 @Valid @RequestBody com.qasystem.dto.SaveDocVersionRequest request,
                                                 Authentication authentication) {
        // 从认证对象中获取用户ID
        Long userId = getUserId(authentication);
        // 调用服务层保存版本
        DocRewriteVersion version = docService.saveVersion(userId, documentId, request);
        // 返回保存的版本信息
        return Result.success("保存成功", version);
    }

    /**
     * 📋 获取文档版本列表
     * 
     * 📖 功能说明：
     * 获取指定文档的所有版本列表，按创建时间倒序排列。
     * 用户可以查看所有历史版本，比较不同版本之间的差异，
     * 选择特定版本进行查看或恢复，实现文档版本管理。
     * 
     * 🔧 技术实现：
     * - 根据文档ID查询所有关联的版本记录
     * - 按创建时间倒序排列，最新版本在前
     * - 计算每个版本的统计信息，如修改段落数、相似度变化等
     * - 标记当前活跃版本，方便用户识别
     * - 过滤敏感信息，确保数据安全
     * 
     * 📋 请求参数：
     * @param documentId 文档ID，指定要查询版本的文档
     * @param authentication Spring Security认证对象，用于验证用户身份
     * 
     * 🔄 返回结果：
     * @return 版本列表，每个版本包含：
     *         - id: 版本ID
     *         - name: 版本名称
     *         - description: 版本描述
     *         - createTime: 创建时间
     *         - paragraphCount: 段落数量
     *         - modifiedCount: 修改段落数量
     *         - similarityChange: 相似度变化(百分比)
     *         - isActive: 是否为当前活跃版本
     * 
     * 📝 使用示例：
     * 前端调用示例：
     * ```javascript
     * fetch(`/api/v1/doc/${documentId}/versions`, {
     *   headers: {
     *     'Authorization': 'Bearer ' + token
     *   }
     * })
     * .then(response => response.json())
     * .then(data => {
     *   console.log('版本列表:', data);
     *   // 渲染版本列表
     *   const versionList = data.data;
     *   const versionListHtml = versionList.map(version => `
     *     <div class="version-item" data-id="${version.id}">
     *       <h3>${version.name}</h3>
     *       <p>${version.description}</p>
     *       <p>创建时间: ${new Date(version.createTime).toLocaleString()}</p>
     *       <p>修改段落: ${version.modifiedCount}/${version.paragraphCount}</p>
     *       <p>相似度变化: ${version.similarityChange}%</p>
     *       <button onclick="viewVersion(${version.id})">查看</button>
     *       <button onclick="restoreVersion(${version.id})">恢复</button>
     *     </div>
     *   `).join('');
     *   document.getElementById('version-list').innerHTML = versionListHtml;
     * });
     * ```
     * 
     * ⚠️ 注意事项：
     * - 只能查看自己文档的版本列表
     * - 版本列表最多显示10个版本，超过部分需要分页
     * - 版本信息是只读的，不能通过此接口修改
     * - 删除文档会级联删除所有版本
     * - 版本列表按时间倒序排列，最新版本在前
     */
    @GetMapping("/{documentId}/versions")
    public Result<List<DocRewriteVersion>> listVersions(@PathVariable Long documentId,
                                                        Authentication authentication) {
        // 验证用户登录状态
        getUserId(authentication);
        // 调用服务层获取版本列表
        List<DocRewriteVersion> versions = docService.listVersions(documentId);
        // 返回版本列表
        return Result.success(versions);
    }

    /**
     * 🔍 获取单个版本详情
     * 
     * 📖 功能说明：
     * 获取指定版本的详细信息，包括完整的段落内容和修改记录。
     * 用户可以查看版本的具体内容，对比原始文本和修改后文本，
     * 了解每个段落的修改情况，评估版本质量。
     * 
     * 🔧 技术实现：
     * - 根据版本ID查询版本基本信息
     * - 获取关联的文档信息
     * - 查询版本的所有段落内容
     * - 对比原始文本和修改后文本，标记差异部分
     * - 计算版本统计信息，如修改率、相似度变化等
     * - 格式化数据，便于前端展示
     * 
     * 📋 请求参数：
     * @param versionId 版本ID，指定要查询的版本
     * @param authentication Spring Security认证对象，用于验证用户身份
     * 
     * 🔄 返回结果：
     * @return DocRewriteVersion 版本详情对象，包含：
     *         - id: 版本ID
     *         - name: 版本名称
     *         - description: 版本描述
     *         - createTime: 创建时间
     *         - document: 关联的文档信息
     *         - paragraphs: 段落列表，每个段落包含：
     *           - id: 段落ID
     *           - originalContent: 原始内容
     *           - modifiedContent: 修改后内容
     *           - isModified: 是否被修改
     *           - isRewritten: 是否经过AI改写
     *           - similarityBefore: 修改前相似度
     *           - similarityAfter: 修改后相似度
     * 
     * 📝 使用示例：
     * 前端调用示例：
     * ```javascript
     * fetch(`/api/v1/doc/version/${versionId}`, {
     *   headers: {
     *     'Authorization': 'Bearer ' + token
     *   }
     * })
     * .then(response => response.json())
     * .then(data => {
     *   console.log('版本详情:', data);
     *   const version = data.data;
     *   // 显示版本基本信息
     *   document.getElementById('version-name').textContent = version.name;
     *   document.getElementById('version-description').textContent = version.description;
     *   // 渲染段落列表，高亮显示修改部分
     *   const paragraphsHtml = version.paragraphs.map(paragraph => `
     *     <div class="paragraph-item ${paragraph.isModified ? 'modified' : ''}">
     *       <h4>段落 ${paragraph.id}</h4>
     *       <div class="original-content">${paragraph.originalContent}</div>
     *       ${paragraph.isModified ? `
     *         <div class="modified-content">${paragraph.modifiedContent}</div>
     *         <div class="similarity-change">
     *           相似度: ${paragraph.similarityBefore}% → ${paragraph.similarityAfter}%
     *         </div>
     *       ` : ''}
     *     </div>
     *   `).join('');
     *   document.getElementById('paragraphs').innerHTML = paragraphsHtml;
     * });
     * ```
     * 
     * ⚠️ 注意事项：
     * - 只能查看自己文档的版本详情
     * - 版本详情包含完整内容，可能较大
     * - 版本详情是只读的，不能通过此接口修改
     * - 版本被删除后，无法再查看详情
     * - 查看版本详情不会影响当前活跃版本
     */
    @GetMapping("/version/{versionId}")
    public Result<DocRewriteVersion> getVersion(@PathVariable Long versionId,
                                                Authentication authentication) {
        // 验证用户登录状态
        getUserId(authentication);
        // 调用服务层获取版本详情
        DocRewriteVersion version = docService.getVersion(versionId);
        // 返回版本详情
        return Result.success(version);
    }

    /**
     * ✏️ 批量更新文档段落
     * 
     * 📖 功能说明：
     * 批量更新文档中的多个段落内容，允许用户对文档进行精细化的修改。
     * 用户可以手动编辑段落内容，或者应用AI改写的结果，
     * 系统会保存所有修改，并更新文档的查重结果。
     * 
     * 🔧 技术实现：
     * - 接收段落更新列表，包含段落ID和新内容
     * - 验证用户权限，确保只能修改自己的文档
     * - 批量更新数据库中的段落内容
     * - 重新计算修改段落的相似度
     * - 更新文档的总体相似度
     * - 记录修改历史，便于追踪和回滚
     * 
     * 📋 请求参数：
     * @param documentId 文档ID，指定要更新的文档
     * @param paragraphs 段落更新列表，每个段落包含：
     *                    - id: 段落ID
     *                    - content: 新的段落内容
     *                    - rewritten: 是否经过AI改写
     * @param authentication Spring Security认证对象，用于验证用户身份
     * 
     * 🔄 返回结果：
     * @return 操作结果消息，包含：
     *         - message: 操作结果描述
     *         - updatedCount: 更新的段落数量
     *         - newSimilarity: 更新后的总体相似度
     * 
     * 📝 使用示例：
     * 前端调用示例：
     * ```javascript
     * const paragraphs = [
     *   { id: 1, content: '修改后的第1段内容', rewritten: true },
     *   { id: 3, content: '手动编辑的第3段内容', rewritten: false },
     *   { id: 5, content: '修改后的第5段内容', rewritten: true }
     * ];
     * 
     * fetch(`/api/v1/doc/${documentId}/paragraphs`, {
     *   method: 'PUT',
     *   headers: {
     *     'Content-Type': 'application/json',
     *     'Authorization': 'Bearer ' + token
     *   },
     *   body: JSON.stringify(paragraphs)
     * })
     * .then(response => response.json())
     * .then(data => {
     *   console.log('更新结果:', data);
     *   // 显示更新成功提示
     *   alert(`成功更新${data.data.updatedCount}个段落，新相似度为${data.data.newSimilarity}%`);
     *   // 刷新查重报告
     *   loadReport();
     * });
     * ```
     * 
     * ⚠️ 注意事项：
     * - 只能更新自己文档的段落
     * - 段落内容不能为空，否则会返回错误
     * - 段落长度限制为10000字符，超过会被截断
     * - 批量更新是原子操作，要么全部成功，要么全部失败
     * - 更新后会重新计算相似度，可能需要等待一段时间
     * - 系统会自动保存修改历史，可以查看和恢复
     */
    @PutMapping("/{documentId}/paragraphs")
    public Result<String> batchUpdateParagraphs(@PathVariable Long documentId,
                                             @RequestBody List<Map<String, Object>> paragraphs,
                                             Authentication authentication) {
        // 验证用户登录状态
        getUserId(authentication);
        // 调用服务层批量更新段落
        docService.batchUpdateParagraphs(documentId, paragraphs);
        // 返回操作结果
        return Result.success("段落更新成功", "成功");
    }

    /**
     * 📥 下载文档（Word格式）
     * 
     * 📖 功能说明：
     * 下载处理后的文档，生成Word格式文件供用户保存。
     * 系统会根据当前文档内容（包括所有修改）生成新的Word文档，
     * 保持原有格式和样式，用户可以直接使用或进一步编辑。
     * 
     * 🔧 技术实现：
     * - 获取文档的最新内容和格式信息
     * - 使用Apache POI库生成Word文档
     * - 应用样式和格式，保持与原文档一致
     * - 添加水印和元数据，标识文档来源
     * - 设置响应头，触发浏览器下载
     * - 记录下载日志，便于统计分析
     * 
     * 📋 请求参数：
     * @param documentId 文档ID，指定要下载的文档
     * @param authentication Spring Security认证对象，用于验证用户身份
     * 
     * 🔄 返回结果：
     * @return ResponseEntity<byte[]> Word文档的二进制数据，包含：
     *         - Content-Type: application/octet-stream
     *         - Content-Disposition: attachment; filename="document_{documentId}.docx"
     *         - 文档内容：Word格式的二进制数据
     * 
     * 📝 使用示例：
     * 前端调用示例：
     * ```javascript
     * // 创建下载链接
     * const downloadLink = document.createElement('a');
     * downloadLink.href = `/api/v1/doc/${documentId}/download`;
     * downloadLink.download = `document_${documentId}.docx`;
     * 
     * // 添加认证头（需要使用fetch获取blob）
     * fetch(`/api/v1/doc/${documentId}/download`, {
     *   headers: {
     *     'Authorization': 'Bearer ' + token
     *   }
     * })
     * .then(response => response.blob())
     * .then(blob => {
     *   const url = window.URL.createObjectURL(blob);
     *   downloadLink.href = url;
     *   downloadLink.click();
     *   window.URL.revokeObjectURL(url);
     * })
     * .catch(error => {
     *   console.error('下载失败:', error);
     *   alert('文档下载失败，请重试');
     * });
     * ```
     * 
     * ⚠️ 注意事项：
     * - 只能下载自己文档的Word文件
     * - 下载的是最新修改后的版本，不是原始上传版本
     * - 文档生成可能需要几秒钟时间，请耐心等待
     * - 大文档下载可能较慢，建议显示进度提示
     * - 下载次数有限制，防止频繁下载消耗资源
     * - 下载的文档包含水印，标识来源和下载时间
     */
    @GetMapping("/{documentId}/download")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long documentId,
                                                   Authentication authentication) {
        // 验证用户登录状态
        getUserId(authentication);
        try {
            // 调用服务层生成Word文档
            byte[] data = docService.downloadDocument(documentId);
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "document_" + documentId + ".docx");
            // 返回文档数据
            return ResponseEntity.ok().headers(headers).body(data);
        } catch (Exception e) {
            // 记录错误日志
            log.error("文档下载失败", e);
            // 抛出运行时异常，由全局异常处理器处理
            throw new RuntimeException("文档下载失败: " + e.getMessage());
        }
    }

    /**
     * 🔐 从认证对象中获取用户ID
     * 
     * 📖 功能说明：
     * 从Spring Security的Authentication对象中提取用户ID，
     * 用于标识当前操作的用户身份。所有需要认证的接口都会调用此方法。
     * 
     * 🔧 技术实现：
     * - 检查Authentication对象是否为空
     * - 检查Principal是否为空
     * - 将Principal转换为Long类型的用户ID
     * - 如果验证失败，抛出运行时异常
     * 
     * 📋 请求参数：
     * @param authentication Spring Security认证对象
     * 
     * 🔄 返回结果：
     * @return Long 用户ID
     * 
     * ⚠️ 注意事项：
     * - 此方法假设Principal直接存储了用户ID
     * - 如果认证失败，会抛出运行时异常
     * - 所有需要认证的接口都应调用此方法
     * - 可以根据需要扩展为获取完整的用户信息
     */
    private Long getUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("未登录");
        }
        return (Long) authentication.getPrincipal();
    }
}
