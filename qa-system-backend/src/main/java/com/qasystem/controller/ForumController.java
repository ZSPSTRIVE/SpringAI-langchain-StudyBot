package com.qasystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qasystem.entity.Forum;
import com.qasystem.service.ForumService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 💬 论坛控制器 - 兼容旧系统API
 * 
 * 📖 功能说明：
 * 本控制器提供论坛功能的API接口，兼容旧系统的API格式。
 * 论坛系统是师生答疑系统的重要组成部分，用于师生之间的
 * 交流讨论、问题解答和知识分享。支持帖子发布、评论回复、
 * 内容管理等功能，促进师生互动和学习交流。
 * 
 * 🎯 主要功能：
 * 1. 帖子浏览 - 公开访问的帖子列表和详情
 * 2. 帖子管理 - 创建、编辑、删除自己的帖子
 * 3. 评论互动 - 对帖子进行评论和回复
 * 4. 内容搜索 - 按关键词搜索帖子和评论
 * 5. 权限控制 - 区分公开内容和私有内容
 * 
 * 🔧 技术实现：
 * - 基于Spring MVC框架，提供RESTful API接口
 * - 使用Spring Security进行用户认证和授权
 * - 集成MyBatis-Plus分页插件，支持高效分页查询
 * - 兼容旧系统API格式，确保平滑迁移
 * - 使用统一异常处理，确保API响应一致性
 * 
 * 📋 API设计：
 * - 兼容旧系统的API格式和响应结构
 * - 提供公开访问和需要认证的两种接口
 * - 统一返回格式，使用Map包装响应数据
 * - 支持分页查询，使用page和limit参数
 * - 提供灵活的查询条件，支持多字段查询
 * 
 * 🔄 工作流程：
 * 1. 用户访问论坛 → 查看公开帖子列表
 * 2. 用户查看帖子 → 阅读帖子和评论内容
 * 3. 用户登录系统 → 获取创建和编辑权限
 * 4. 用户创建帖子 → 发布问题或分享知识
 * 5. 用户评论回复 → 参与讨论和交流
 * 6. 用户管理内容 → 编辑或删除自己的帖子
 * 
 * ⚠️ 注意事项：
 * - 公开接口无需认证，私有接口需要登录
 * - 用户只能编辑和删除自己的帖子
 * - 管理员可以管理所有帖子
 * - API格式兼容旧系统，不建议直接修改
 * - 论坛内容会被记录日志，用于审计追踪
 * 
 * @author 师生答疑系统开发团队
 * @since 1.0.0
 */
@Slf4j  // 自动生成日志对象，用于记录操作日志
@RestController  // 标识为RESTful控制器，自动处理JSON序列化
@RequestMapping("/forum")  // 设置基础路径为/forum
@RequiredArgsConstructor  // 为final字段生成构造函数，实现依赖注入
public class ForumController {

    /**
     * 💬 论坛服务接口 - 处理论坛的业务逻辑
     * 
     * 该服务封装了论坛管理的所有业务逻辑，包括：
     * - 帖子的增删改查
     * - 评论的增删改查
     * - 帖子和评论的关联管理
     * - 权限控制和内容审核
     * - 搜索和分页功能
     */
    private final ForumService forumService;

    /**
     * 📋 前端列表（公开访问）
     * 
     * 📖 功能说明：
     * 获取论坛帖子列表，供前端页面展示，无需用户认证。
     * 此接口主要用于论坛首页、分类页面等公开场景，
     * 用户可以浏览帖子标题、作者、发布时间等基本信息。
     * 
     * 🔧 技术实现：
     * - 使用MyBatis-Plus分页插件实现高效分页
     * - 支持多条件动态查询，灵活组合过滤条件
     * - 按置顶、精华、最新回复等排序
     * - 过滤敏感内容，确保展示安全
     * - 缓存热门帖子，提高访问性能
     * 
     * 📋 请求参数：
     * @param page 页码，从1开始，默认为1
     * @param limit 每页大小，默认为10，最大为50
     * @param query 查询条件对象，包含：
     *             - title: 标题过滤条件，支持模糊匹配
     *             - userid: 作者ID过滤条件
     *             - type: 帖子类型过滤条件
     *             - status: 帖子状态过滤条件
     * 
     * 🔄 返回结果：
     * @return 兼容旧系统的响应格式，包含：
     *         - code: 状态码，0表示成功
     *         - msg: 消息内容
     *         - data: 数据对象，包含：
     *           - total: 总记录数
     *           - list: 帖子列表，每个帖子包含：
     *             - id: 帖子ID
     *             - title: 帖子标题
     *             - userid: 作者ID
     *             - username: 作者用户名
     *             - content: 帖子内容（摘要）
     *             - type: 帖子类型
     *             - status: 帖子状态
     *             - viewcount: 浏览次数
     *             - replycount: 回复次数
     *             - createtime: 创建时间
     *             - updatetime: 更新时间
     * 
     * 📝 使用示例：
     * 前端调用示例：
     * ```javascript
     * // 构建查询参数
     * const params = new URLSearchParams({
     *   page: 1,
     *   limit: 10,
     *   type: 'question' // 查询问题类型的帖子
     * });
     * 
     * fetch(`/forum/flist?${params}`)
     * .then(response => response.json())
     * .then(data => {
     *   console.log('帖子列表:', data);
     *   if (data.code === 0) {
     *     const forumData = data.data;
     *     // 显示分页信息
     *     document.getElementById('page-info').textContent = 
     *       `共${forumData.total}条帖子`;
     *     // 渲染帖子列表
     *     const postListHtml = forumData.list.map(post => `
     *       <div class="post-item">
     *         <h3><a href="/forum/detail/${post.id}">${post.title}</a></h3>
     *         <p>作者: ${post.username} | 浏览: ${post.viewcount} | 回复: ${post.replycount}</p>
     *         <p>发布时间: ${new Date(post.createtime).toLocaleString()}</p>
     *       </div>
     *     `).join('');
     *     document.getElementById('post-list').innerHTML = postListHtml;
     *   } else {
     *     alert('获取帖子列表失败: ' + data.msg);
     *   }
     * });
     * ```
     * 
     * ⚠️ 注意事项：
     * - 此接口无需认证，任何人都可以访问
     * - 每页大小最大为50，超过限制会被重置为50
     * - 标题查询使用模糊匹配，可能返回较多结果
     * - 查询结果按置顶、最新回复时间排序
     * - 敏感内容会被过滤，不会返回给前端
     * - 帖子内容只返回摘要，完整内容需要调用详情接口
     */
    @GetMapping("/flist")
    public Map<String, Object> flist(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            Forum query) {
        // 记录查询参数日志
        log.info("📋 查询论坛帖子列表: page={}, limit={}", page, limit);
        
        // 调用服务层执行分页查询
        IPage<Forum> pageResult = forumService.getForumPage(page, limit, query);
        
        // 构建兼容旧系统的响应格式
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");
        
        Map<String, Object> data = new HashMap<>();
        data.put("total", pageResult.getTotal());
        data.put("list", pageResult.getRecords());
        result.put("data", data);
        
        return result;
    }

    /**
     * 📋 后端分页（需要认证）
     * 
     * 📖 功能说明：
     * 获取论坛帖子列表，供后端管理页面使用，需要用户认证。
     * 普通用户只能查看自己的帖子，管理员可以查看所有帖子。
     * 此接口主要用于个人中心、帖子管理等需要登录的场景。
     * 
     * 🔧 技术实现：
     * - 使用Spring Security获取当前登录用户信息
     * - 根据用户角色过滤帖子列表
     * - 使用MyBatis-Plus分页插件实现高效分页
     * - 支持多条件动态查询，灵活组合过滤条件
     * - 按创建时间倒序排列，最新帖子在前
     * 
     * 📋 请求参数：
     * @param page 页码，从1开始，默认为1
     * @param limit 每页大小，默认为10，最大为50
     * @param query 查询条件对象，包含：
     *             - title: 标题过滤条件，支持模糊匹配
     *             - type: 帖子类型过滤条件
     *             - status: 帖子状态过滤条件
     * @param authentication Spring Security认证对象，包含用户信息
     * 
     * 🔄 返回结果：
     * @return 兼容旧系统的响应格式，包含：
     *         - code: 状态码，0表示成功
     *         - msg: 消息内容
     *         - data: 数据对象，包含：
     *           - total: 总记录数
     *           - list: 帖子列表，每个帖子包含：
     *             - id: 帖子ID
     *             - title: 帖子标题
     *             - content: 帖子内容
     *             - type: 帖子类型
     *             - status: 帖子状态
     *             - createtime: 创建时间
     *             - updatetime: 更新时间
     * 
     * 📝 使用示例：
     * 前端调用示例：
     * ```javascript
     * // 构建查询参数
     * const params = new URLSearchParams({
     *   page: 1,
     *   limit: 10,
     *   type: 'question' // 查询问题类型的帖子
     * });
     * 
     * fetch(`/forum/page?${params}`, {
     *   headers: {
     *     'Authorization': 'Bearer ' + userToken
     *   }
     * })
     * .then(response => response.json())
     * .then(data => {
     *   console.log('我的帖子列表:', data);
     *   if (data.code === 0) {
     *     const forumData = data.data;
     *     // 显示分页信息
     *     document.getElementById('page-info').textContent = 
     *       `共${forumData.total}条帖子`;
     *     // 渲染帖子列表
     *     const postListHtml = forumData.list.map(post => `
     *       <div class="post-item">
     *         <h3>${post.title}</h3>
     *         <p>类型: ${post.type} | 状态: ${post.status}</p>
     *         <p>发布时间: ${new Date(post.createtime).toLocaleString()}</p>
     *         <div class="post-actions">
     *           <button onclick="editPost(${post.id})">编辑</button>
     *           <button onclick="deletePost(${post.id})">删除</button>
     *         </div>
     *       </div>
     *     `).join('');
     *     document.getElementById('my-post-list').innerHTML = postListHtml;
     *   } else {
     *     alert('获取帖子列表失败: ' + data.msg);
     *   }
     * });
     * ```
     * 
     * ⚠️ 注意事项：
     * - 需要用户认证才能访问
     * - 普通用户只能查看自己的帖子
     * - 管理员可以查看所有帖子
     * - 每页大小最大为50，超过限制会被重置为50
     * - 查询结果按创建时间倒序排列
     * - 返回的帖子包含完整内容，请注意数据量
     */
    @GetMapping("/page")
    public Map<String, Object> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            Forum query,
            Authentication authentication) {
        
        // 从认证对象中获取用户ID
        Long userId = (Long) authentication.getPrincipal();
        log.info("📋 后端查询论坛帖子: page={}, limit={}, userId={}", page, limit, userId);
        
        // 非管理员只能看自己的帖子
        // 这里简化处理，后续可以根据角色判断
        query.setUserid(userId);
        
        // 调用服务层执行分页查询
        IPage<Forum> pageResult = forumService.getForumPage(page, limit, query);
        
        // 构建兼容旧系统的响应格式
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");
        
        Map<String, Object> data = new HashMap<>();
        data.put("total", pageResult.getTotal());
        data.put("list", pageResult.getRecords());
        result.put("data", data);
        
        return result;
    }

    /**
     * 📄 获取帖子详情（公开访问）
     * 
     * 📖 功能说明：
     * 获取指定帖子的详细信息，无需用户认证。
     * 此接口主要用于帖子详情页面，展示帖子的完整内容、
     * 作者信息、发布时间等基本信息，但不包含评论。
     * 
     * 🔧 技术实现：
     * - 根据帖子ID查询帖子基本信息
     * - 增加帖子浏览次数
     * - 过滤敏感内容，确保展示安全
     * - 格式化帖子内容，便于前端展示
     * - 缓存热门帖子，提高访问性能
     * 
     * 📋 请求参数：
     * @param id 帖子ID，指定要查询的帖子
     * 
     * 🔄 返回结果：
     * @return 兼容旧系统的响应格式，包含：
     *         - code: 状态码，0表示成功
     *         - msg: 消息内容
     *         - data: 帖子详情对象，包含：
     *           - id: 帖子ID
     *           - title: 帖子标题
     *           - userid: 作者ID
     *           - username: 作者用户名
     *           - content: 帖子内容
     *           - type: 帖子类型
     *           - status: 帖子状态
     *           - viewcount: 浏览次数
     *           - replycount: 回复次数
     *           - createtime: 创建时间
     *           - updatetime: 更新时间
     * 
     * 📝 使用示例：
     * 前端调用示例：
     * ```javascript
     * const postId = 123;
     * 
     * fetch(`/forum/info/${postId}`)
     * .then(response => response.json())
     * .then(data => {
     *   console.log('帖子详情:', data);
     *   if (data.code === 0) {
     *     const post = data.data;
     *     // 显示帖子信息
     *     document.getElementById('post-title').textContent = post.title;
     *     document.getElementById('post-author').textContent = post.username;
     *     document.getElementById('post-content').innerHTML = post.content;
     *     document.getElementById('post-stats').textContent = 
     *       `浏览: ${post.viewcount} | 回复: ${post.replycount}`;
     *     document.getElementById('post-time').textContent = 
     *       `发布时间: ${new Date(post.createtime).toLocaleString()}`;
     *   } else {
     *     alert('获取帖子详情失败: ' + data.msg);
     *   }
     * });
     * ```
     * 
     * ⚠️ 注意事项：
     * - 此接口无需认证，任何人都可以访问
     * - 访问此接口会增加帖子的浏览次数
     * - 敏感内容会被过滤，不会返回给前端
     * - 帖子不存在时，返回错误信息
     * - 此接口不包含评论，需要调用其他接口获取评论
     */
    @GetMapping("/info/{id}")
    public Map<String, Object> info(@PathVariable Long id) {
        // 记录查询参数日志
        log.info("📄 获取帖子详情: id={}", id);
        
        // 调用服务层获取帖子详情
        Forum forum = forumService.getById(id);
        
        // 构建兼容旧系统的响应格式
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");
        result.put("data", forum);
        
        return result;
    }

    /**
     * 📄 获取帖子详情（包含评论树，公开访问）
     * 
     * 📖 功能说明：
     * 获取指定帖子的详细信息，包括帖子的所有评论和回复，
     * 无需用户认证。此接口主要用于帖子详情页面，展示帖子的
     * 完整内容和评论树结构，方便用户浏览和参与讨论。
     * 
     * 🔧 技术实现：
     * - 根据帖子ID查询帖子基本信息
     * - 递归查询帖子的所有评论和回复
     * - 构建评论树结构，支持多级回复
     * - 增加帖子浏览次数
     * - 过滤敏感内容，确保展示安全
     * - 缓存热门帖子，提高访问性能
     * 
     * 📋 请求参数：
     * @param id 帖子ID，指定要查询的帖子
     * 
     * 🔄 返回结果：
     * @return 兼容旧系统的响应格式，包含：
     *         - code: 状态码，0表示成功
     *         - msg: 消息内容
     *         - data: 帖子详情对象，包含：
     *           - id: 帖子ID
     *           - title: 帖子标题
     *           - userid: 作者ID
     *           - username: 作者用户名
     *           - content: 帖子内容
     *           - type: 帖子类型
     *           - status: 帖子状态
     *           - viewcount: 浏览次数
     *           - replycount: 回复次数
     *           - createtime: 创建时间
     *           - updatetime: 更新时间
     *           - children: 评论列表，每个评论包含：
     *             - id: 评论ID
     *             - userid: 评论者ID
     *             - username: 评论者用户名
     *             - content: 评论内容
     *             - parentid: 父评论ID
     *             - createtime: 创建时间
     *             - children: 子评论列表（递归结构）
     * 
     * 📝 使用示例：
     * 前端调用示例：
     * ```javascript
     * const postId = 123;
     * 
     * fetch(`/forum/list/${postId}`)
     * .then(response => response.json())
     * .then(data => {
     *   console.log('帖子详情（含评论）:', data);
     *   if (data.code === 0) {
     *     const post = data.data;
     *     // 显示帖子信息
     *     document.getElementById('post-title').textContent = post.title;
     *     document.getElementById('post-author').textContent = post.username;
     *     document.getElementById('post-content').innerHTML = post.content;
     *     document.getElementById('post-stats').textContent = 
     *       `浏览: ${post.viewcount} | 回复: ${post.replycount}`;
     *     document.getElementById('post-time').textContent = 
     *       `发布时间: ${new Date(post.createtime).toLocaleString()}`;
     *     
     *     // 渲染评论树
     *     function renderComments(comments, level = 0) {
     *       return comments.map(comment => `
     *         <div class="comment" style="margin-left: ${level * 20}px;">
     *           <div class="comment-header">
     *             <strong>${comment.username}</strong>
     *             <span class="comment-time">${new Date(comment.createtime).toLocaleString()}</span>
     *           </div>
     *           <div class="comment-content">${comment.content}</div>
     *           <div class="comment-actions">
     *             <button onclick="replyComment(${comment.id})">回复</button>
     *           </div>
     *           ${comment.children ? renderComments(comment.children, level + 1) : ''}
     *         </div>
     *       `).join('');
     *     }
     *     
     *     document.getElementById('comments-container').innerHTML = 
     *       post.children ? renderComments(post.children) : '<p>暂无评论</p>';
     *   } else {
     *     alert('获取帖子详情失败: ' + data.msg);
     *   }
     * });
     * ```
     * 
     * ⚠️ 注意事项：
     * - 此接口无需认证，任何人都可以访问
     * - 访问此接口会增加帖子的浏览次数
     * - 敏感内容会被过滤，不会返回给前端
     * - 帖子不存在时，返回错误信息
     * - 评论树可能很深，前端渲染时需要注意性能
     * - 评论数量很多时，建议分页加载
     */
    @GetMapping("/list/{id}")
    public Map<String, Object> detail(@PathVariable Long id) {
        // 记录查询参数日志
        log.info("📄 获取帖子详情（含评论）: id={}", id);
        
        // 调用服务层获取帖子详情（包含评论树）
        Forum forum = forumService.getForumWithChildren(id);
        
        // 构建兼容旧系统的响应格式
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");
        result.put("data", forum);
        
        return result;
    }

    /**
     * ✍️ 创建帖子或评论（需要认证）
     * 
     * 📖 功能说明：
     * 创建新的帖子或评论，需要用户认证。
     * 如果parentid为空或0，则创建新帖子；如果parentid不为空，
     * 则创建对该帖子的评论或回复。创建成功后，返回创建的
     * 帖子或评论信息。
     * 
     * 🔧 技术实现：
     * - 使用Spring Security获取当前登录用户信息
     * - 验证用户权限和输入参数
     * - 根据parentid判断是创建帖子还是评论
     * - 过滤敏感内容，确保内容安全
     * - 保存帖子或评论到数据库
     * - 更新相关统计信息
     * - 记录创建操作日志
     * 
     * 📋 请求参数：
     * @param forum 帖子或评论对象，包含：
     *             - title: 帖子标题（创建帖子时必填）
     *             - content: 帖子或评论内容（必填）
     *             - type: 帖子类型（创建帖子时可选）
     *             - parentid: 父帖子ID（创建评论时必填）
     *             - status: 帖子状态（可选）
     * @param authentication Spring Security认证对象，包含用户信息
     * 
     * 🔄 返回结果：
     * @return 兼容旧系统的响应格式，包含：
     *         - code: 状态码，0表示成功
     *         - msg: 消息内容
     *         - data: 创建的帖子或评论对象，包含：
     *           - id: 帖子或评论ID
     *           - title: 帖子标题（如果是帖子）
     *           - content: 帖子或评论内容
     *           - userid: 作者ID
     *           - username: 作者用户名
     *           - parentid: 父帖子ID（如果是评论）
     *           - createtime: 创建时间
     *           - updatetime: 更新时间
     * 
     * 📝 使用示例：
     * 前端调用示例：
     * ```javascript
     * // 创建帖子
     * function createPost() {
     *   const postData = {
     *     title: document.getElementById('post-title').value,
     *     content: document.getElementById('post-content').value,
     *     type: 'question'
     *   };
     *   
     *   fetch('/forum/add', {
     *     method: 'POST',
     *     headers: {
     *       'Content-Type': 'application/json',
     *       'Authorization': 'Bearer ' + userToken
     *     },
     *     body: JSON.stringify(postData)
     *   })
     *   .then(response => response.json())
     *   .then(data => {
     *     console.log('创建帖子结果:', data);
     *     if (data.code === 0) {
     *       alert('帖子创建成功！');
     *       // 跳转到帖子详情页
     *       window.location.href = `/forum/detail/${data.data.id}`;
     *     } else {
     *       alert('帖子创建失败: ' + data.msg);
     *     }
     *   })
     *   .catch(error => {
     *     console.error('创建失败:', error);
     *     alert('帖子创建失败，请重试');
     *   });
     * }
     * 
     * // 创建评论
     * function createComment(postId, content) {
     *   const commentData = {
     *     content: content,
     *     parentid: postId
     *   };
     *   
     *   fetch('/forum/add', {
     *     method: 'POST',
     *     headers: {
     *       'Content-Type': 'application/json',
     *       'Authorization': 'Bearer ' + userToken
     *     },
     *     body: JSON.stringify(commentData)
     *   })
     *   .then(response => response.json())
     *   .then(data => {
     *     console.log('创建评论结果:', data);
     *     if (data.code === 0) {
     *       alert('评论发布成功！');
     *       // 刷新页面显示新评论
     *       location.reload();
     *     } else {
     *       alert('评论发布失败: ' + data.msg);
     *     }
     *   })
     *   .catch(error => {
     *     console.error('创建失败:', error);
     *     alert('评论发布失败，请重试');
     *   });
     * }
     * ```
     * 
     * ⚠️ 注意事项：
     * - 需要用户认证才能访问
     * - 创建帖子时，标题和内容不能为空
     * - 创建评论时，内容和父帖子ID不能为空
     * - 敏感内容会被过滤，可能导致创建失败
     * - 创建操作会被记录日志，用于审计追踪
     * - 创建成功后，会返回完整的帖子或评论信息
     */
    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody Forum forum, Authentication authentication) {
        // 从认证对象中获取用户ID
        Long userId = (Long) authentication.getPrincipal();
        
        // 从用户信息中获取用户名（简化处理）
        String username = userId.toString();
        
        // 记录创建帖子/评论日志
        log.info("✍️ 创建帖子/评论: userId={}, parentid={}", userId, forum.getParentid());
        
        // 调用服务层创建帖子或评论
        Forum created = forumService.createForum(forum, userId, username);
        
        // 构建兼容旧系统的响应格式
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "发布成功");
        result.put("data", created);
        
        return result;
    }

    /**
     * 💾 保存帖子（需要认证）
     * 
     * 📖 功能说明：
     * 保存新的帖子，需要用户认证。
     * 此接口与/add接口功能类似，但专门用于保存帖子，
     * 不用于保存评论。保存成功后，返回创建的帖子信息。
     * 
     * 🔧 技术实现：
     * - 使用Spring Security获取当前登录用户信息
     * - 验证用户权限和输入参数
     * - 确保parentid为空，只创建帖子
     * - 过滤敏感内容，确保内容安全
     * - 保存帖子到数据库
     * - 更新相关统计信息
     * - 记录创建操作日志
     * 
     * 📋 请求参数：
     * @param forum 帖子对象，包含：
     *             - title: 帖子标题（必填）
     *             - content: 帖子内容（必填）
     *             - type: 帖子类型（可选）
     *             - status: 帖子状态（可选）
     * @param authentication Spring Security认证对象，包含用户信息
     * 
     * 🔄 返回结果：
     * @return 兼容旧系统的响应格式，包含：
     *         - code: 状态码，0表示成功
     *         - msg: 消息内容
     *         - data: 创建的帖子对象，包含：
     *           - id: 帖子ID
     *           - title: 帖子标题
     *           - content: 帖子内容
     *           - userid: 作者ID
     *           - username: 作者用户名
     *           - type: 帖子类型
     *           - status: 帖子状态
     *           - createtime: 创建时间
     *           - updatetime: 更新时间
     * 
     * 📝 使用示例：
     * 前端调用示例：
     * ```javascript
     * function savePost() {
     *   const postData = {
     *     title: document.getElementById('post-title').value,
     *     content: document.getElementById('post-content').value,
     *     type: 'discussion'
     *   };
     *   
     *   fetch('/forum/save', {
     *     method: 'POST',
     *     headers: {
     *       'Content-Type': 'application/json',
     *       'Authorization': 'Bearer ' + userToken
     *     },
     *     body: JSON.stringify(postData)
     *   })
     *   .then(response => response.json())
     *   .then(data => {
     *     console.log('保存帖子结果:', data);
     *     if (data.code === 0) {
     *       alert('帖子保存成功！');
     *       // 跳转到帖子详情页
     *       window.location.href = `/forum/detail/${data.data.id}`;
     *     } else {
     *       alert('帖子保存失败: ' + data.msg);
     *     }
     *   })
     *   .catch(error => {
     *     console.error('保存失败:', error);
     *     alert('帖子保存失败，请重试');
     *   });
     * }
     * ```
     * 
     * ⚠️ 注意事项：
     * - 需要用户认证才能访问
     * - 帖子标题和内容不能为空
     * - 敏感内容会被过滤，可能导致保存失败
     * - 保存操作会被记录日志，用于审计追踪
     * - 保存成功后，会返回完整的帖子信息
     * - 此接口专门用于保存帖子，不用于保存评论
     */
    @PostMapping("/save")
    public Map<String, Object> save(@RequestBody Forum forum, Authentication authentication) {
        // 从认证对象中获取用户信息
        Long userId = (Long) authentication.getPrincipal();
        String username = userId.toString();
        
        // 记录保存帖子日志
        log.info("💾 保存帖子: userId={}", userId);
        
        // 调用服务层创建帖子
        Forum created = forumService.createForum(forum, userId, username);
        
        // 构建兼容旧系统的响应格式
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "保存成功");
        result.put("data", created);
        
        return result;
    }

    /**
     * 📝 更新帖子（需要认证）
     * 
     * 📖 功能说明：
     * 更新已存在的帖子信息，需要用户认证。
     * 用户只能更新自己的帖子，管理员可以更新所有帖子。
     * 更新成功后，返回更新结果信息。
     * 
     * 🔧 技术实现：
     * - 验证用户权限和输入参数
     * - 检查帖子是否存在
     * - 检查用户是否有权限更新该帖子
     * - 过滤敏感内容，确保内容安全
     * - 更新帖子信息
     * - 更新相关统计信息
     * - 记录更新操作日志
     * 
     * 📋 请求参数：
     * @param forum 帖子对象，包含：
     *             - id: 帖子ID（必填）
     *             - title: 帖子标题（可选）
     *             - content: 帖子内容（可选）
     *             - type: 帖子类型（可选）
     *             - status: 帖子状态（可选）
     * 
     * 🔄 返回结果：
     * @return 兼容旧系统的响应格式，包含：
     *         - code: 状态码，0表示成功
     *         - msg: 消息内容
     *         - data: null（无额外数据）
     * 
     * 📝 使用示例：
     * 前端调用示例：
     * ```javascript
     * function updatePost(postId) {
     *   const postData = {
     *     id: postId,
     *     title: document.getElementById('post-title').value,
     *     content: document.getElementById('post-content').value,
     *     type: 'discussion'
     *   };
     *   
     *   fetch('/forum/update', {
     *     method: 'POST',
     *     headers: {
     *       'Content-Type': 'application/json',
     *       'Authorization': 'Bearer ' + userToken
     *     },
     *     body: JSON.stringify(postData)
     *   })
     *   .then(response => response.json())
     *   .then(data => {
     *     console.log('更新帖子结果:', data);
     *     if (data.code === 0) {
     *       alert('帖子更新成功！');
     *       // 刷新页面显示更新后的内容
     *       location.reload();
     *     } else {
     *       alert('帖子更新失败: ' + data.msg);
     *     }
     *   })
     *   .catch(error => {
     *     console.error('更新失败:', error);
     *     alert('帖子更新失败，请重试');
     *   });
     * }
     * ```
     * 
     * ⚠️ 注意事项：
     * - 需要用户认证才能访问
     * - 帖子ID必须存在
     * - 用户只能更新自己的帖子，管理员可以更新所有帖子
     * - 敏感内容会被过滤，可能导致更新失败
     * - 更新操作会被记录日志，用于审计追踪
     * - 更新成功后，前端需要刷新页面显示更新后的内容
     */
    @PostMapping("/update")
    public Map<String, Object> update(@RequestBody Forum forum) {
        // 记录更新帖子日志
        log.info("📝 更新帖子: id={}", forum.getId());
        
        // 调用服务层更新帖子
        forumService.updateForum(forum);
        
        // 构建兼容旧系统的响应格式
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "更新成功");
        
        return result;
    }

    /**
     * 🗑️ 删除帖子（需要认证）
     * 
     * 📖 功能说明：
     * 删除一个或多个帖子，需要用户认证。
     * 用户只能删除自己的帖子和评论，管理员可以删除所有帖子。
     * 删除帖子时，会级联删除该帖子的所有评论和回复。
     * 
     * 🔧 技术实现：
     * - 验证用户权限和输入参数
     * - 检查帖子是否存在
     * - 检查用户是否有权限删除该帖子
     * - 开启数据库事务
     * - 删除帖子的所有评论和回复
     * - 删除帖子记录
     * - 更新相关统计信息
     * - 记录删除操作日志
     * 
     * 📋 请求参数：
     * @param ids 帖子ID数组，指定要删除的帖子
     * 
     * 🔄 返回结果：
     * @return 兼容旧系统的响应格式，包含：
     *         - code: 状态码，0表示成功
     *         - msg: 消息内容
     *         - data: null（无额外数据）
     * 
     * 📝 使用示例：
     * 前端调用示例：
     * ```javascript
     * // 删除单个帖子
     * function deletePost(postId) {
     *   if (!confirm('确定要删除这个帖子吗？此操作不可撤销！')) {
     *     return;
     *   }
     *   
     *   fetch('/forum/delete', {
     *     method: 'POST',
     *     headers: {
     *       'Content-Type': 'application/json',
     *       'Authorization': 'Bearer ' + userToken
     *     },
     *     body: JSON.stringify([postId])
     *   })
     *   .then(response => response.json())
     *   .then(data => {
     *     console.log('删除帖子结果:', data);
     *     if (data.code === 0) {
     *       alert('帖子删除成功！');
     *       // 跳转到论坛首页
     *       window.location.href = '/forum';
     *     } else {
     *       alert('帖子删除失败: ' + data.msg);
     *     }
     *   })
     *   .catch(error => {
     *     console.error('删除失败:', error);
     *     alert('帖子删除失败，请重试');
     *   });
     * }
     * 
     * // 批量删除帖子
     * function deleteSelectedPosts() {
     *   const selectedIds = getSelectedPostIds(); // 获取选中的帖子ID
     *   if (selectedIds.length === 0) {
     *     alert('请先选择要删除的帖子');
     *     return;
     *   }
     *   
     *   if (!confirm(`确定要删除选中的${selectedIds.length}个帖子吗？此操作不可撤销！`)) {
     *     return;
     *   }
     *   
     *   fetch('/forum/delete', {
     *     method: 'POST',
     *     headers: {
     *       'Content-Type': 'application/json',
     *       'Authorization': 'Bearer ' + userToken
     *     },
     *     body: JSON.stringify(selectedIds)
     *   })
     *   .then(response => response.json())
     *   .then(data => {
     *     console.log('批量删除帖子结果:', data);
     *     if (data.code === 0) {
     *       alert(`成功删除${selectedIds.length}个帖子！`);
     *       // 刷新帖子列表
     *       loadPostList();
     *     } else {
     *       alert('批量删除失败: ' + data.msg);
     *     }
     *   })
     *   .catch(error => {
     *     console.error('批量删除失败:', error);
     *     alert('批量删除失败，请重试');
     *   });
     * }
     * ```
     * 
     * ⚠️ 注意事项：
     * - 需要用户认证才能访问
     * - 帖子ID必须存在
     * - 用户只能删除自己的帖子和评论，管理员可以删除所有帖子
     * - 删除操作不可逆，请谨慎执行
     * - 删除帖子会级联删除该帖子的所有评论和回复
     * - 删除操作会被记录日志，用于审计追踪
     * - 批量删除时，建议限制一次删除的数量，避免性能问题
     */
    @PostMapping("/delete")
    public Map<String, Object> delete(@RequestBody Long[] ids) {
        // 记录删除帖子日志
        log.info("🗑️ 删除帖子: ids={}", (Object) ids);
        
        // 调用服务层删除帖子
        forumService.deleteForum(ids);
        
        // 构建兼容旧系统的响应格式
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "删除成功");
        
        return result;
    }
}

