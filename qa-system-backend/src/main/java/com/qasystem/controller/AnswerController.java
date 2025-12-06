package com.qasystem.controller;

import com.qasystem.common.response.Result;
import com.qasystem.dto.AnswerDTO;
import com.qasystem.dto.CreateAnswerRequest;
import com.qasystem.service.AnswerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 🎯 回答控制器 - 处理教师对学生问题的回答相关操作
 * 
 * 📖 功能说明：
 * 回答模块是师生答疑系统的核心功能之一，连接教师和学生的知识传递桥梁。
 * 本控制器负责处理回答的全生命周期管理：
 * 1. 查询回答 - 获取某个问题下的所有回答列表
 * 2. 创建回答 - 教师对问题进行回答
 * 3. 编辑回答 - 教师修改自己的回答内容
 * 4. 删除回答 - 教师或管理员删除不当回答
 * 5. 采纳回答 - 学生标记最佳答案
 * 
 * 🔒 权限控制：
 * - 查询回答：所有登录用户
 * - 创建回答：仅教师（TEACHER角色）
 * - 编辑回答：仅回答者本人
 * - 删除回答：回答者本人或管理员
 * - 采纳回答：仅提问者本人（学生）
 * 
 * 🌍 RESTful 设计：
 * GET    /api/v1/answers/question/{questionId}  获取问题的所有回答
 * POST   /api/v1/answers                         创建新回答
 * PUT    /api/v1/answers/{id}                    更新回答
 * DELETE /api/v1/answers/{id}                    删除回答
 * PUT    /api/v1/answers/{id}/accept             采纳回答
 * 
 * 📝 核心概念：
 * - 回答等级：普通回答、被采纳回答（学生认可的最佳答案）
 * - 回答状态：正常、已删除（软删除）
 * - 排序规则：采纳的回答置顶，其他按时间倒序
 * - 通知机制：回答后通知提问者，采纳后通知回答者
 * 
 * 📝 注解说明：
 * @Slf4j - Lombok注解，自动生成日志对象log
 * @RestController - Spring注解，标识这是REST控制器，自动序列化返回值为JSON
 * @RequestMapping - 定义基础路径/api/v1/answers，所有方法路径都是相对此路径
 * @RequiredArgsConstructor - Lombok注解，为final字段生成构造函数，实现依赖注入
 * 
 * @author 师生答疑系统开发团队
 * @since 1.0.0
 */
@Slf4j  // 自动生成日志对象，用于记录操作日志和调试信息
@RestController  // 标识这是一个REST风格的控制器，返回JSON数据而不是视图
@RequestMapping("/api/v1/answers")  // 定义所有回答接口的基础路径
@RequiredArgsConstructor  // 自动为final字段生成构造函数，Spring会自动注入依赖
public class AnswerController {

    // 回答服务层接口，处理所有回答相关的业务逻辑
    // final确保注入后不可修改，保证线程安全
    private final AnswerService answerService;

    /**
     * 📋 获取问题的所有回答 - 查看某个问题下的完整回答列表
     * 
     * 业务流程：
     * 1. 接收问题ID参数
     * 2. 调用服务层查询该问题的所有回答
     * 3. 按照规则排序：已采纳的回答在最前面，其他按创建时间倒序
     * 4. 过滤已删除的回答（软删除）
     * 5. 组装回答数据：包含回答者信息、回答内容、时间、是否被采纳等
     * 6. 返回回答列表
     * 
     * 排序逻辑：
     * - 第一优先级：被采纳的回答（accepted=true）置顶
     * - 第二优先级：其他回答按创建时间降序
     * - 这样学生能第一眼看到最佳答案
     * 
     * 权限说明：
     * - 公开接口，所有登录用户都可以查看
     * - 未登录用户看不到回答（防止爬虫）
     * - 已删除的回答只有管理员可见
     * 
     * 请求示例：
     * GET /api/v1/answers/question/123
     * Headers:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiI...
     * 
     * 成功响应示例：
     * {
     *     "code": 200,
     *     "message": "成功",
     *     "data": [
     *         {
     *             "id": 1,
     *             "questionId": 123,
     *             "content": "这个问题的解决方法是...",
     *             "teacherId": 5,
     *             "teacherName": "张老师",
     *             "teacherAvatar": "http://...",
     *             "accepted": true,
     *             "createdAt": "2024-01-15T10:30:00",
     *             "updatedAt": "2024-01-15T11:00:00"
     *         },
     *         {
     *             "id": 2,
     *             "questionId": 123,
     *             "content": "另一种思路是...",
     *             "teacherId": 6,
     *             "teacherName": "李老师",
     *             "teacherAvatar": "http://...",
     *             "accepted": false,
     *             "createdAt": "2024-01-15T14:20:00",
     *             "updatedAt": "2024-01-15T14:20:00"
     *         }
     *     ]
     * }
     * 
     * 空列表响应（没有回答）：
     * {
     *     "code": 200,
     *     "message": "成功",
     *     "data": []
     * }
     * 
     * 前端使用建议：
     * - 被采纳的回答用特殊样式高亮显示（如绿色边框、勾选图标）
     * - 显示回答数量"共N个回答"
     * - 如果没有回答，提示"暂无回答，等待老师解答"
     * - 教师用户可以看到"添加回答"按钮
     * 
     * @param questionId 问题ID
     *                   @PathVariable - 从URL路径获取
     *                   示例：GET /answers/question/123 中的 123
     * @return Result<List<AnswerDTO>> 包含回答列表的统一响应对象
     *         List<AnswerDTO> - 回答数据传输对象列表，包含回答详情和教师信息
     *         按采纳状态和时间排序，采纳的回答在最前
     * @throws ResourceNotFoundException 当问题不存在时抛出
     */
    @GetMapping("/question/{questionId}")  // 处理GET请求，完整路径：/api/v1/answers/question/{questionId}
    public Result<List<AnswerDTO>> getAnswersByQuestionId(@PathVariable Long questionId) {
        // 记录查询操作日志，包含问题ID
        // 用于监控接口调用频率和问题的热度
        log.info("获取问题的所有回答: questionId={}", questionId);
        
        // 调用服务层查询回答列表
        // 服务层会处理排序、过滤已删除、组装教师信息等逻辑
        List<AnswerDTO> answers = answerService.getAnswersByQuestionId(questionId);
        
        // 返回成功响应，包含回答列表
        // 即使列表为空也返回200状态码（业务正常）
        return Result.success(answers);
    }

    /**
     * ✏️ 创建回答 - 教师对学生问题进行回答
     * 
     * 业务流程：
     * 1. 验证当前用户是否为教师角色（只有教师可以回答）
     * 2. 验证请求参数：问题ID、回答内容不能为空
     * 3. 检查问题是否存在且状态为PENDING/ANSWERED（已关闭的问题不能回答）
     * 4. 检查内容安全：过滤敏感词、XSS攻击、SQL注入等
     * 5. 创建回答记录，保存到数据库
     * 6. 更新问题状态为ANSWERED（如果是第一个回答）
     * 7. 更新问题的answer_count字段（回答数+1）
     * 8. 清除相关缓存（问题详情、回答列表）
     * 9. 异步发送通知：通知提问者有新回答
     * 10. 异步记录积分：教师回答问题可获得积分
     * 11. 返回创建的回答详情
     * 
     * 数据验证：
     * - questionId: 必填，必须存在且未关闭
     * - content: 必填，长度限制在 10-10000 字符
     * - 不允许纯空格或特殊字符
     * - 自动过滤HTML标签防止XSS攻击
     * 
     * 业务规则：
     * - 只有教师可以回答问题，学生不能回答
     * - 同一个教师可以对同一个问题多次回答（补充说明）
     * - 已关闭的问题不能再回答
     * - 已删除的问题不能回答
     * - 回答内容不能包含敏感词
     * 
     * 通知机制：
     * - 系统通知：提问者收到"您的问题有新回答"
     * - 邮件通知：如果开启了邮件通知
     * - 站内消息：实时推送给在线的提问者
     * 
     * 权限控制：
     * - 必须是教师角色（ROLE_TEACHER）
     * - 必须通过身份验证（登录状态）
     * - 禁言的教师不能回答
     * 
     * 请求示例：
     * POST /api/v1/answers
     * Headers:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiI...
     *   Content-Type: application/json
     * Body:
     * {
     *     "questionId": 123,
     *     "content": "这个问题的解决方法是这样的：\n1. 首先..."
     * }
     * 
     * 成功响应示例：
     * {
     *     "code": 200,
     *     "message": "回答成功",
     *     "data": {
     *         "id": 456,
     *         "questionId": 123,
     *         "content": "这个问题的解决方法是这样的：...",
     *         "teacherId": 5,
     *         "teacherName": "张老师",
     *         "teacherAvatar": "http://...",
     *         "accepted": false,
     *         "createdAt": "2024-01-15T10:30:00",
     *         "updatedAt": "2024-01-15T10:30:00"
     *     }
     * }
     * 
     * 错误响应示例：
     * {
     *     "code": 403,
     *     "message": "只有教师可以回答问题",
     *     "data": null
     * }
     * 
     * @param authentication 认证对象，包含当前登录教师的ID和角色信息
     *                       Spring Security自动注入，无需手动传递
     * @param request 创建回答请求对象
     *                @Valid - 启用参数校验，自动验证@NotNull, @Size等注解
     *                @RequestBody - 从请求体获取JSON数据并反序列化
     *                包含字段：questionId(问题ID), content(回答内容)
     * @return Result<AnswerDTO> 统一响应对象，包含创建成功的回答详情
     * @throws AccessDeniedException 当非教师角色尝试回答时抛出
     * @throws ResourceNotFoundException 当问题不存在时抛出
     * @throws BusinessException 当问题已关闭或内容不合规时抛出
     */
    @PostMapping  // 处理POST请求，完整路径：/api/v1/answers
    public Result<AnswerDTO> createAnswer(
            Authentication authentication,  // 认证对象，由Spring Security自动注入
            @Valid @RequestBody CreateAnswerRequest request) {  // 请求体，启用校验
        // 从认证对象获取当前教师的用户ID
        // 服务层会验证该用户是否为教师角色
        Long userId = (Long) authentication.getPrincipal();
        
        // 记录创建回答的操作日志
        // 包含教师ID和问题ID，用于统计教师的活跃度
        log.info("创建回答: userId={}, questionId={}", userId, request.getQuestionId());
        
        // 调用服务层创建回答
        // 服务层会处理所有业务逻辑：权限验证、数据验证、保存、通知等
        AnswerDTO created = answerService.createAnswer(userId, request);
        
        // 返回成功响应，包含创建的回答完整信息
        // 前端可以直接将返回的回答添加到回答列表中
        return Result.success("回答成功", created);
    }

    /**
     * 📝 更新回答 - 教师修改自己的回答内容
     * 
     * 业务流程：
     * 1. 验证回答ID是否存在
     * 2. 验证是否是回答者本人（只能编辑自己的回答）
     * 3. 验证新内容：不能为空、长度限制、敏感词过滤
     * 4. 检查回答是否已被采纳（被采纳的回答有更严格的编辑限制）
     * 5. 更新回答内容和更新时间
     * 6. 清除相关缓存（回答详情、回答列表、问题详情）
     * 7. 异步通知：如果是重大修改，通知提问者
     * 8. 返回更新后的回答详情
     * 
     * 编辑规则：
     * - 只能编辑自己的回答，不能编辑其他教师的回答
     * - 管理员也不能编辑其他教师的回答（只能删除）
     * - 被采纳的回答可以编辑，但会显示“已编辑”标记
     * - 已删除的回答不能编辑
     * 
     * 时间限制：
     * - 建议在创建后24小时内编辑，超过时间显示警告
     * - 如果回答已被大量阅读，编辑时需谨慎
     * - 被采纳的回答编辑需谨慎，避免改变原意
     * 
     * 权限控制：
     * - 必须是回答者本人
     * - 管理员不能编辑他人的回答
     * - 禁言的教师不能编辑
     * 
     * 请求示例：
     * PUT /api/v1/answers/456
     * Headers:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiI...
     *   Content-Type: application/json
     * Body:
     * {
     *     "questionId": 123,
     *     "content": "更新后的回答内容，补充更详细的解释..."
     * }
     * 
     * 成功响应示例：
     * {
     *     "code": 200,
     *     "message": "更新成功",
     *     "data": {
     *         "id": 456,
     *         "questionId": 123,
     *         "content": "更新后的回答内容...",
     *         "teacherId": 5,
     *         "teacherName": "张老师",
     *         "teacherAvatar": "http://...",
     *         "accepted": true,
     *         "createdAt": "2024-01-15T10:30:00",
     *         "updatedAt": "2024-01-15T14:30:00"
     *     }
     * }
     * 
     * 错误响应示例：
     * {
     *     "code": 403,
     *     "message": "只能编辑自己的回答",
     *     "data": null
     * }
     * 
     * @param id 回答ID
     *           @PathVariable - 从URL路径获取
     *           示例：PUT /answers/456 中的 456
     * @param authentication 认证对象，包含当前教师的ID
     *                       用于验证是否是回答者本人
     * @param request 更新请求对象，包含新的回答内容
     *                @Valid - 启用参数校验
     *                @RequestBody - 从请求体获取JSON数据
     * @return Result<AnswerDTO> 统一响应对象，包含更新后的回答详情
     * @throws AccessDeniedException 当非回答者本人尝试编辑时抛出
     * @throws ResourceNotFoundException 当回答不存在时抛出
     * @throws BusinessException 当回答已删除或内容不合规时抛出
     */
    @PutMapping("/{id}")  // 处理PUT请求，完整路径：/api/v1/answers/{id}
    public Result<AnswerDTO> updateAnswer(
            @PathVariable Long id,  // 从URL路径获取回答ID
            Authentication authentication,  // 认证对象，自动注入
            @Valid @RequestBody CreateAnswerRequest request) {  // 请求体，启用校验
        // 从认证对象获取当前用户ID
        // 服务层会验证该用户是否是回答者本人
        Long userId = (Long) authentication.getPrincipal();
        
        // 记录更新回答的操作日志
        // 包含回答ID和操作用户ID，用于审计
        log.info("更新回答: id={}, userId={}", id, userId);
        
        // 调用服务层更新回答
        // 服务层会验证权限、更新数据、清除缓存、发送通知
        AnswerDTO updated = answerService.updateAnswer(id, userId, request);
        
        // 返回成功响应，包含更新后的完整信息
        // 前端可以直接替换列表中的旧数据
        return Result.success("更新成功", updated);
    }

    /**
     * 🗑️ 删除回答 - 教师删除自己的回答，或管理员删除任何回答
     * 
     * 业务流程：
     * 1. 验证回答ID是否存在
     * 2. 验证权限：
     *    - 回答者本人可以删除自己的回答
     *    - 管理员可以删除任何回答（审核不当内容）
     * 3. 检查回答是否被采纳：
     *    - 如果被采纳，需要更新问题的accepted_answer_id为null
     *    - 更新问题状态回到ANSWERED或PENDING
     * 4. 执行软删除（设置deleted标志）
     * 5. 更新问题的answer_count字段（回答数-1）
     * 6. 清除相关缓存（回答列表、问题详情）
     * 7. 异步通知：如果是被采纳的回答，通知提问者
     * 8. 记录审计日志
     * 
     * 为什么使用软删除？
     * - 保留数据用于审计和统计
     * - 防止误删，必要时可以恢复
     * - 保持数据关联的完整性
     * - 符合法规要求
     * 
     * 删除限制：
     * - 被采纳的回答删除时会有警告提示
     * - 删除后不可恢复（用户视角）
     * - 已被大量阅读的回答删除时需谨慎
     * 
     * 权限控制：
     * - 回答者本人可以删除自己的回答
     * - 管理员可以删除任何回答
     * - 提问者不能删除别人的回答
     * 
     * 请求示例：
     * DELETE /api/v1/answers/456
     * Headers:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiI...
     * 
     * 成功响应：
     * {
     *     "code": 200,
     *     "message": "删除成功",
     *     "data": null
     * }
     * 
     * 错误响应示例：
     * {
     *     "code": 403,
     *     "message": "无权删除此回答",
     *     "data": null
     * }
     * 
     * @param id 回答ID
     *           @PathVariable - 从URL路径获取
     *           示例：DELETE /answers/456 中的 456
     * @param authentication 认证对象，包含当前用户ID和角色
     *                       用于验证删除权限
     * @return Result<Void> 无数据返回，只返回操作结果
     * @throws AccessDeniedException 当权限不足时抛出
     * @throws ResourceNotFoundException 当回答不存在时抛出
     */
    @DeleteMapping("/{id}")  // 处理DELETE请求，完整路径：/api/v1/answers/{id}
    public Result<Void> deleteAnswer(
            @PathVariable Long id,  // 从URL路径获取回答ID
            Authentication authentication) {  // 认证对象，自动注入
        // 从认证对象获取当前用户ID
        // 服务层会验证是否是回答者本人或管理员
        Long userId = (Long) authentication.getPrincipal();
        
        // 记录删除操作日志，非常重要
        // 删除操作必须记录日志，用于审计和追溯
        log.info("删除回答: id={}, userId={}", id, userId);
        
        // 调用服务层删除回答
        // 服务层会处理权限验证、软删除、更新问题统计、清除缓存等
        answerService.deleteAnswer(id, userId);
        
        // 返回成功响应，无需返回数据
        // DELETE操作通常只返回成功状态
        return Result.success("删除成功", null);
    }

    /**
     * ✅ 采纳回答 - 学生将某个回答标记为最佳答案
     * 
     * 业务流程：
     * 1. 验证回答ID是否存在
     * 2. 验证当前用户是否是提问者本人（只有提问者可以采纳）
     * 3. 检查该问题是否已有被采纳的回答：
     *    - 如果有，先取消之前的采纳（只能有一个被采纳的回答）
     * 4. 设置当前回答为被采纳状态（accepted=true）
     * 5. 更新问题的accepted_answer_id字段
     * 6. 更新问题状态为RESOLVED（已解决）
     * 7. 清除相关缓存（回答列表、问题详情）
     * 8. 异步发送通知：通知被采纳的教师
     * 9. 异步记录积分：被采纳的教师获得额外积分奖励
     * 10. 更新教师的统计数据（被采纳次数+1）
     * 
     * 采纳规则：
     * - 每个问题只能有一个被采纳的回答
     * - 只有提问者本人可以采纳回答
     * - 可以更改采纳（取消旧的，采纳新的）
     * - 已删除的回答不能被采纳
     * - 关闭的问题不能采纳回答
     * 
     * 采纳的意义：
     * - 对教师：认可和鼓励，增加信誉积分
     * - 对学生：标记问题已解决，方便以后查找
     * - 对其他用户：快速找到最佳答案
     * - 对系统：提高内容质量，优化推荐算法
     * 
     * 显示效果：
     * - 被采纳的回答显示在列表顶部
     * - 显示特殊标识：绿色勾选框、"已采纳"徽章
     * - 问题状态更新为"已解决"
     * 
     * 权限控制：
     * - 只有提问者本人可以采纳回答
     * - 教师不能采纳其他人问题的回答
     * - 管理员也不能代替学生采纳
     * 
     * 请求示例：
     * PUT /api/v1/answers/456/accept
     * Headers:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiI...
     * Body: 无
     * 
     * 成功响应：
     * {
     *     "code": 200,
     *     "message": "采纳成功",
     *     "data": null
     * }
     * 
     * 错误响应示例：
     * {
     *     "code": 403,
     *     "message": "只有提问者可以采纳回答",
     *     "data": null
     * }
     * 
     * ⚠️ 注意事项：
     * - 采纳后可以更改，但不建议频繁更改
     * - 采纳会影响教师的信誉分，请谨慎操作
     * - 建议在前端提示用户确认后再采纳
     * 
     * @param id 回答ID
     *           @PathVariable - 从URL路径获取
     *           示例：PUT /answers/456/accept 中的 456
     * @param authentication 认证对象，包含当前学生的ID
     *                       用于验证是否是提问者本人
     * @return Result<Void> 无数据返回，只返回操作结果
     * @throws AccessDeniedException 当非提问者尝试采纳时抛出
     * @throws ResourceNotFoundException 当回答或问题不存在时抛出
     * @throws BusinessException 当问题已关闭或回答已删除时抛出
     */
    @PutMapping("/{id}/accept")  // 处理PUT请求，完整路径：/api/v1/answers/{id}/accept
    public Result<Void> acceptAnswer(
            @PathVariable Long id,  // 从URL路径获取回答ID
            Authentication authentication) {  // 认证对象，自动注入
        // 从认证对象获取当前用户ID
        // 服务层会验证该用户是否是提问者本人
        Long userId = (Long) authentication.getPrincipal();
        
        // 记录采纳操作日志
        // 采纳是重要操作，需记录用于统计和审计
        log.info("采纳回答: id={}, userId={}", id, userId);
        
        // 调用服务层采纳回答
        // 服务层会处理权限验证、更新状态、取消旧采纳、通知、积分等
        answerService.acceptAnswer(id, userId);
        
        // 返回成功响应
        // 前端收到响应后应更新界面：显示采纳标记、调整排序
        return Result.success("采纳成功", null);
    }
}

