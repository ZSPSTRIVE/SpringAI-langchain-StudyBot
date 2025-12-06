package com.qasystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qasystem.common.response.Result;
import com.qasystem.dto.CreateQuestionRequest;
import com.qasystem.dto.QuestionDTO;
import com.qasystem.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * ❓ 【问题管理控制器】处理学生问题相关的HTTP请求
 * 
 * 📖 功能说明：
 * 1. 问题查询 - 分页查询、按科目/状态/关键词筛选
 * 2. 问题详情 - 查看单个问题的完整信息，自动增加浏览量
 * 3. 发布问题 - 学生发起新问题，支持图片上传
 * 4. 修改问题 - 只能修改待回答(PENDING)状态的问题
 * 5. 删除问题 - 提问者或管理员可以删除
 * 6. 关闭问题 - 提问者主动关闭，不再接受回答
 * 
 * 🔒 权限控制：
 * - 查询接口：公开访问，无需认证
 * - 发布问题：需要学生角色
 * - 修改问题：只有提问者本人可以修改
 * - 删除问题：提问者或ADMIN管理员
 * - 关闭问题：只有提问者本人
 * 
 * 🌍 RESTful设计：
 * - 基础路径：/api/v1/questions
 * - GET    /                - 分页查询问题列表
 * - GET    /{id}            - 获取问题详情
 * - POST   /                - 创建新问题
 * - PUT    /{id}            - 更新问题
 * - DELETE /{id}            - 删除问题
 * - PUT    /{id}/close      - 关闭问题
 * 
 * 📝 注解说明：
 * @Slf4j                  - Lombok注解，自动生成日志对象log
 * @RestController         - REST风格控制器，返回JSON格式数据
 * @RequestMapping         - 定义控制器基础URL路径
 * @RequiredArgsConstructor - 自动生成构造函数，注入依赖
 * 
 * @author 师生答疑系统开发团队
 * @version 2.0.0
 * @since 2024
 */
@Slf4j  // 启用日志记录，使用log对象记录操作日志
@RestController  // 标识为REST风格控制器，所有方法返回值都会被序列化为JSON
@RequestMapping("/api/v1/questions")  // 定义该控制器的基础访问路径
@RequiredArgsConstructor  // 自动生成包含final字段的构造函数，用于依赖注入
public class QuestionController {

    // 注入问题服务：处理具体的问题业务逻辑
    // final修饰符确保依赖不可变，线程安全
    private final QuestionService questionService;

    /**
     * 📋 分页查询问题列表 - 支持多维度筛选
     * 
     * 业务流程：
     * 1. 接收分页参数、筛选条件
     * 2. 调用服务层查询数据库
     * 3. 返回分页结果（包含总数、当前页、数据列表）
     * 
     * 筛选功能：
     * - 按科目筛选：只查询指定科目的问题（如“数据结构”）
     * - 按状态筛选：PENDING(待回答)/ANSWERED(已回答)/CLOSED(已关闭)
     * - 按关键词搜索：在问题标题和内容中模糊搜索
     * 
     * 请求示例：
     * GET /api/v1/questions?page=1&size=10&subjectId=1&status=PENDING&keyword=数据结构
     * 
     * 响应示例：
     * {
     *     "code": 200,
     *     "message": "success",
     *     "data": {
     *         "records": [  // 当前页的问题列表
     *             {
     *                 "id": 1,
     *                 "title": "如何实现二叉树？",
     *                 "content": "...",
     *                 "status": "PENDING",
     *                 "studentName": "张三",
     *                 "subjectName": "数据结构",
     *                 "viewCount": 150,
     *                 "answerCount": 3,
     *                 "createTime": "2024-01-15 10:30:00"
     *             }
     *         ],
     *         "total": 100,      // 总记录数
     *         "size": 10,         // 每页条数
     *         "current": 1,       // 当前页
     *         "pages": 10         // 总页数
     *     }
     * }
     * 
     * @param page 页码，从1开始
     *             defaultValue = "1" - 默认第1页，前端不传时使用默认值
     *             @RequestParam - 从 URL 查询参数中获取
     * @param size 每页条数，默认10条
     *             defaultValue = "10" - 默认每页显示10条数据
     *             建议范围：5-50，避免数据量过大影响性能
     * @param subjectId 科目 ID，可选参数
     *                  required = false - 非必填，不传则查询所有科目
     *                  使用场景：查询“数据结构”科目下的所有问题
     * @param status 问题状态，可选参数
     *               可选值：PENDING(待回答), ANSWERED(已回答), CLOSED(已关闭)
     *               不传则查询所有状态的问题
     * @param keyword 搜索关键词，可选参数
     *                在问题标题和内容中进行模糊搜索
     *                支持中文、英文、数字等字符
     * @return Result<IPage<QuestionDTO>> 统一响应封装，包含分页数据
     *         IPage - MyBatis Plus的分页对象，包含总数、当前页、数据列表
     *         QuestionDTO - 问题数据传输对象，包含学生姓名、科目名、回答数等
     */
    @GetMapping  // 处理GET请求，完整路径：/api/v1/questions
    public Result<IPage<QuestionDTO>> getQuestionPage(
            @RequestParam(defaultValue = "1") Integer page,  // 页码参数，默认1
            @RequestParam(defaultValue = "10") Integer size,  // 每页条数，默认10
            @RequestParam(required = false) Long subjectId,  // 科目ID，可为null
            @RequestParam(required = false) String status,  // 状态，可为null
            @RequestParam(required = false) String keyword) {  // 关键词，可为null
        // 记录查询日志，方便排查问题和统计分析
        log.info("分页查询问题: page={}, size={}, subjectId={}, status={}, keyword={}", 
                 page, size, subjectId, status, keyword);
        
        // 调用服务层查询数据
        // 服务层会根据参数构建查询条件，进行分页查询
        IPage<QuestionDTO> result = questionService.getQuestionPage(page, size, subjectId, status, keyword);
        
        // 返回成功响应，包装成统一格式
        // Result.success()会自动设置code=200, message="success"
        return Result.success(result);
    }

    /**
     * 🔍 获取问题详情 - 查看完整信息，自动增加浏览量
     * 
     * 业务流程：
     * 1. 根据ID查询问题详情
     * 2. 自动增加浏览次数viewCount + 1
     * 3. 返回问题完整信息（包含学生姓名、科目名、回答数等）
     * 
     * 缓存策略：
     * - 问题详情会缓存到24小时（Redis）
     * - 问题更新/删除时自动清除缓存
     * - 首次查询从数据库加载，后续从缓存读取
     * 
     * 请求示例：
     * GET /api/v1/questions/123
     * 
     * 响应示例：
     * {
     *     "code": 200,
     *     "message": "success",
     *     "data": {
     *         "id": 123,
     *         "title": "如何实现二叉树？",
     *         "content": "详细问题描述...",
     *         "status": "PENDING",
     *         "studentId": 1001,
     *         "studentName": "张三",
     *         "subjectId": 1,
     *         "subjectName": "数据结构",
     *         "viewCount": 151,  // 浏览量已自动+1
     *         "answerCount": 3,
     *         "images": ["http://example.com/image1.jpg"],
     *         "createTime": "2024-01-15 10:30:00",
     *         "updateTime": "2024-01-15 11:00:00"
     *     }
     * }
     * 
     * @param id 问题ID
     *           @PathVariable - 从 URL 路径中获取参数
     *           示例：/questions/123 中的 123
     * @return Result<QuestionDTO> 统一响应封装，包含问题详细信息
//     * @throws ResourceNotFoundException 当问题不存在时抛出
     */
    @GetMapping("/{id}")  // 处理GET请求，{id}是路径变量
    public Result<QuestionDTO> getQuestionById(@PathVariable Long id) {  // 从 URL 路径获取id
        // 记录查询日志
        log.info("获取问题详情: id={}", id);
        
        // 增加浏览次数：viewCount + 1
        // 这是一个异步操作，不影响查询性能
        questionService.incrementViewCount(id);
        
        // 查询问题详情
        // 服务层会先从缓存查询，未命中再查数据库
        QuestionDTO question = questionService.getQuestionById(id);
        
        // 返回成功响应
        return Result.success(question);
    }

    /**
     * ➕ 创建新问题 - 学生发起提问，支持图片上传
     * 
     * 业务流程：
     * 1. 从认证信息中获取当前登录用户ID
     * 2. 验证请求参数（标题、内容、科目等）
     * 3. 验证科目是否存在
     * 4. 创建问题记录，默认状态为PENDING（待回答）
     * 5. 处理图片列表：序列化为JSON字符串存储
     * 6. 返回创建结果
     * 
     * 权限限制：
     * - 只有学生角色可以发起问题
     * - 需要登录认证（JWT Token）
     * - 每个用户每天可发起的问题数量可能有限制
     * 
     * 字段验证规则：
     * - 标题：必填，长度5-200字符
     * - 内容：必填，长度10-5000字符
     * - 科目ID：必填，必须是有效的科目
     * - 图片列表：可选，最多5张
     * 
     * 请求示例：
     * POST /api/v1/questions
     * Headers:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiI...
     * Body:
     * {
     *     "subjectId": 1,
     *     "title": "如何实现二叉树的遍历？",
     *     "content": "我在学习数据结构时遇到了问题，不知道如何实现二叉树的前序、中序、后序遍历...",
     *     "images": [
     *         "https://example.com/images/question1.png",
     *         "https://example.com/images/question2.png"
     *     ]
     * }
     * 
     * 响应示例：
     * {
     *     "code": 200,
     *     "message": "提问成功",
     *     "data": {
     *         "id": 456,
     *         "subjectId": 1,
     *         "subjectName": "数据结构",
     *         "studentId": 1001,
     *         "studentName": "张三",
     *         "title": "如何实现二叉树的遍历？",
     *         "content": "...",
     *         "status": "PENDING",
     *         "viewCount": 0,
     *         "answerCount": 0,
     *         "images": ["https://example.com/images/question1.png"],
     *         "createTime": "2024-01-15 14:30:00"
     *     }
     * }
     * 
     * @param authentication Spring Security认证对象，自动注入
     *                       包含当前登录用户的身份信息
     *                       getPrincipal() 返回用户ID（在JwtFilter中设置）
     * @param request 创建问题请求对象
     *                @Valid - 启用JSR-303参数校验，自动验证标题、内容等字段
     *                @RequestBody - 从请求体中反序列化JSON数据
     * @return Result<QuestionDTO> 统一响应封装，包含创建的问题信息
//     * @throws ValidationException 当参数校验失败时抛出
//     * @throws ResourceNotFoundException 当科目不存在时抛出
     */
    @PostMapping  // 处理POST请求，完整路径：/api/v1/questions
    public Result<QuestionDTO> createQuestion(
            Authentication authentication,  // Spring Security认证对象，自动注入
            @Valid @RequestBody CreateQuestionRequest request) {  // 请求体，自动验证
        // 从认证信息中获取当前登录用户的ID
        // authentication.getPrincipal() 返回的是在 JwtFilter 中设置的用户ID
        Long userId = (Long) authentication.getPrincipal();
        
        // 记录创建问题的日志，包含用户ID和问题标题
        // 方便追踪用户行为和排查问题
        log.info("创建问题: userId={}, title={}", userId, request.getTitle());
        
        // 调用服务层创建问题
        // 服务层会验证科目、创建问题记录、处理图片列表
        QuestionDTO created = questionService.createQuestion(userId, request);
        
        // 返回成功响应，自定义成功消息为"提问成功"
        // 返回创建的问题完整信息，包含生成的ID、创建时间等
        return Result.success("提问成功", created);
    }

    /**
     * ✏️ 更新问题 - 修改问题内容，只能修改待回答状态的问题
     * 
     * 业务流程：
     * 1. 从认证信息中获取当前用户ID
     * 2. 根据问题ID查询问题是否存在
     * 3. 验证是否是提问者本人（只能修改自己的问题）
     * 4. 验证问题状态：只能修改PENDING状态的问题
     * 5. 更新问题内容（标题、内容、图片）
     * 6. 清除Redis缓存，保证数据一致性
     * 7. 返回更新后的问题信息
     * 
     * 权限限制：
     * - 只有提问者本人可以修改
     * - 只能修改PENDING（待回答）状态的问题
     * - 已有回答或已关闭的问题不能修改
     * 
     * 为什么有这些限制？
     * - 防止提问者修改问题后导致已有的回答不匹配
     * - 保证问答的连贯性和逻辑性
     * - 已关闭的问题不应再被修改
     * 
     * 请求示例：
     * PUT /api/v1/questions/123
     * Headers:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiI...
     * Body:
     * {
     *     "subjectId": 1,
     *     "title": "如何实现二叉树的遍历？（已修改）",
     *     "content": "更新后的问题描述，添加了更详细的信息...",
     *     "images": ["https://example.com/images/updated.png"]
     * }
     * 
     * 响应示例：
     * {
     *     "code": 200,
     *     "message": "更新成功",
     *     "data": {
     *         "id": 123,
     *         "title": "如何实现二叉树的遍历？（已修改）",
     *         "content": "更新后的问题描述...",
     *         "status": "PENDING",
     *         "updateTime": "2024-01-15 15:00:00"  // 更新时间已变化
     *     }
     * }
     * 
     * 错误响应示例：
     * {
     *     "code": 403,
     *     "message": "只能修改待回答的问题",
     *     "data": null
     * }
     * 
     * @param id 问题ID
     *           @PathVariable - 从 URL 路径中获取参数
     *           示例：/questions/123 中的 123
     * @param authentication Spring Security认证对象，用于获取当前用户ID
     *                       用于验证是否是问题作者本人
     * @param request 更新请求对象
     *                @Valid - 启用参数校验
     *                @RequestBody - 从请求体反序列化
     * @return Result<QuestionDTO> 统一响应封装，包含更新后的问题信息
//     * @throws AccessDeniedException 当非提问者本人尝试修改时抛出
//     * @throws BusinessException 当问题状态不允许修改时抛出
     */
    @PutMapping("/{id}")  // 处理PUT请求，完整路径：/api/v1/questions/{id}
    public Result<QuestionDTO> updateQuestion(
            @PathVariable Long id,  // 从 URL 路径获取问题ID
            Authentication authentication,  // 认证对象，自动注入
            @Valid @RequestBody CreateQuestionRequest request) {  // 更新请求体
        // 从认证对象获取当前用户ID
        // 用于后续的权限验证：只有提问者本人可以修改
        Long userId = (Long) authentication.getPrincipal();
        
        // 记录更新操作日志，包含问题ID和用户ID
        // 方便追踪是谁修改了哪个问题
        log.info("更新问题: id={}, userId={}", id, userId);
        
        // 调用服务层更新问题
        // 服务层会验证权限、问题状态、更新内容、清除缓存
        QuestionDTO updated = questionService.updateQuestion(id, userId, request);
        
        // 返回成功响应，包含更新后的完整信息
        // 前端可以根据返回的数据更新界面显示
        return Result.success("更新成功", updated);
    }
    /**
     * 🗑️ 删除问题 - 学生删除自己的问题，管理员可删除任何问题
     * <p>
     * 业务流程：
     * 1. 从认证信息中获取当前用户ID和角色
     * 2. 根据问题ID查询问题是否存在
     * 3. 权限验证：
     * - 学生：只能删除自己的问题，且问题必须是PENDING状态
     * - 管理员：可以删除任何问题（违规内容审核）
     * 4. 执行软删除（设置deleted标志，不是物理删除）
     * 5. 清除相关缓存（问题详情、列表缓存）
     * 6. 异步处理：删除相关的收藏、通知等数据
     * <p>
     * 为什么使用软删除？
     * - 保留数据用于审计和统计分析
     * - 防止误删，必要时可以恢复
     * - 保持外    /**
     * 🔒 关闭问题 - 学生主动关闭自己的问题，表示不再需要回答
     * <p>
     * 业务流程：
     * 1. 从认证信息中获取当前用户ID
     * 2. 根据问题ID查询问题
     * 3. 验证是否是提问者本人（只能关闭自己的问题）
     * 4. 验证问题当前状态（只能关闭PENDING或ANSWERED状态的问题）
     * 5. 更新问题状态为CLOSED
     * 6. 记录关闭时间
     * 7. 清除相关缓存
     * 8. 异步发送通知（通知回答过的老师）
     * <p>
     * 使用场景：
     * - 学生已经通过其他途径解决了问题
     * - 学生发现问题描述有误，不想继续讨论
     * - 学生对已有回答满意，不需要更多回答
     * - 问题已经过时，不再需要回答
     * <p>
     * 关闭 vs 删除的区别：
     * - 关闭：问题和回答仍然可见，但不接受新回答
     * - 删除：问题不可见（软删除），相当于撤回
     * - 关闭更适合已有回答的情况，保留讨论价值
     * <p>
     * 权限限制：
     * - 只有提问者本人可以关闭问题
     * - 已关闭的问题不能重复关闭
     * - 关闭后的问题不能再接收新回答
     * - 关闭后的问题不能再编辑
     * <p>
     * 请求示例：
     * POST /api/v1/questions/123/close
     * Headers:
     * Authorization: Bearer eyJhbGciOiJIUzI1NiI...
     * Body: 无
     * <p>
     * 成功响应：
     * {
     * "code": 200,
     * "message": "问题已关闭",
     * "data": null
     * }
     * <p>
     * 错误响应示例：
     * {
     * "code": 403,
     * "message": "只能关闭自己的问题",
     * "data": null
     * }
     * <p>
     * ⚠️ 注意事项：
     * - 关闭操作不可逆，关闭后无法重新打开
     * - 关闭会影响统计数据（如未解决问题数量）
     * - 建议在关闭前提示用户确认
     *
     * @param id             问题ID
     * @param authentication 认证对象，包含当前用户ID
     *                       用于验证是否是提问者本人
     * @return Result<Void> 无数据返回，只返回操作结果
     * //     * @throws AccessDeniedException 当非提问者尝试关闭时抛出
     * //     * @throws BusinessException     当问题状态不允许关闭时抛出
     * @PathVariable - 从URL路径获取
     * 示例：POST /questions/123/close 中的 123
     */
    @PostMapping("/{id}/close")  // 处理POST请求，完整路径：/api/v1/questions/{id}/close
    public Result<Void> closeQuestion(
            @PathVariable Long id,  // 从URL路径获取问题ID
            Authentication authentication) {  // 认证对象，自动注入
        // 从认证对象获取当前用户ID
        // 用于验证是否是提问者本人，只有提问者才能关闭问题
        Long userId = (Long) authentication.getPrincipal();
        
        // 记录关闭问题的操作日志
        // 关闭是重要操作，需要记录以便追溯
        log.info("关闭问题: id={}, userId={}", id, userId);
        
        // 调用服务层关闭问题
        // 服务层会验证权限、更新状态、清除缓存、发送通知
        questionService.closeQuestion(id, userId);
        
        // 返回成功响应，提示用户问题已关闭
        // 前端收到响应后可以更新问题状态显示
        return Result.<Void>success("问题已关闭", null);
    }
}

