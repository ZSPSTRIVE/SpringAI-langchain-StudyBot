package com.qasystem.controller;

import com.qasystem.common.response.Result;
import com.qasystem.dto.QuestionDTO;
import com.qasystem.service.CollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ⭐ 收藏控制器 - 用户收藏功能，保存感兴趣的问题和回答
 * 
 * 📖 功能说明：
 * 收藏模块允许用户收藏感兴趣的内容，方便后续查阅和学习。
 * 本控制器主要功能包括：
 * 1. 收藏 - 将问题或回答添加到收藏夹
 * 2. 取消收藏 - 从收藏夹移除
 * 3. 状态检查 - 查询是否已收藏
 * 4. 列表查询 - 查看所有收藏的问题
 * 5. 数量统计 - 获取收藏总数
 * 
 * 🔒 权限控制：
 * - 所有接口需要用户登录
 * - 用户只能管理自己的收藏
 * 
 * 🌍 RESTful 设计：
 * POST   /api/v1/collections              收藏内容
 * DELETE /api/v1/collections              取消收藏
 * GET    /api/v1/collections/check        检查是否已收藏
 * GET    /api/v1/collections/questions    获取收藏的问题列表
 * GET    /api/v1/collections/count        获取收藏数量
 * 
 * 📝 收藏类型：
 * - QUESTION：收藏问题
 * - ANSWER：收藏回答（可扩展）
 * 
 * @author 师生答疑系统开发团队
 * @since 1.0.0
 */
@Slf4j  // 自动生成日志对象
@RestController  // 标识REST控制器
@RequestMapping("/api/v1/collections")  // 定义收藏接口的基础路径
@RequiredArgsConstructor  // 为final字段生成构造函数
public class CollectionController {

    // 收藏服务层接口
    private final CollectionService collectionService;

    /**
     * ⭐ 收藏 - 将问题或回答添加到收藏夹
     * 
     * 业务流程：
     * 1. 从认证对象获取当前用户ID
     * 2. 验证目标类型（QUESTION/ANSWER）和目标ID
     * 3. 检查目标内容是否存在
     * 4. 检查是否已经收藏（防止重复收藏）
     * 5. 创建收藏记录，保存到数据库
     * 6. 更新目标内容的收藏数量+1
     * 7. 记录收藏时间
     * 
     * 使用场景：
     * - 学生收藏有价值的问题供后续学习
     * - 教师收藏典型问题供教学参考
     * - 收藏精彩回答供分享学习
     * 
     * 请求示例：
     * POST /api/v1/collections?targetType=QUESTION&targetId=123
     * Headers:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiI...
     * 
     * 成功响应：
     * {
     *     "code": 200,
     *     "message": "收藏成功",
     *     "data": null
     * }
     * 
     * @param targetType 收藏目标类型：QUESTION（问题）或ANSWER（回答）
     * @param targetId 目标ID，问题ID或回答ID
     * @param authentication 认证对象，包含当前用户ID
     * @return Result<Void> 无数据返回
     * @throws ResourceNotFoundException 当目标内容不存在时抛出
     * @throws BusinessException 当已经收藏过时抛出
     */
    @PostMapping  // 处理POST请求
    public Result<Void> collect(
            @RequestParam String targetType,  // 从请求参数获取目标类型
            @RequestParam Long targetId,  // 从请求参数获取目标ID
            Authentication authentication) {  // 认证对象，自动注入
        // 从认证对象获取当前用户ID
        Long userId = (Long) authentication.getPrincipal();
        // 记录收藏操作日志
        log.info("收藏: userId={}, type={}, targetId={}", userId, targetType, targetId);
        // 调用服务层执行收藏
        collectionService.collect(userId, targetType, targetId);
        // 返回成功响应
        return Result.success("收藏成功", null);
    }

    /**
     * ❌ 取消收藏 - 从收藏夹移除内容
     * 
     * 业务流程：
     * 1. 从认证对象获取当前用户ID
     * 2. 验证目标类型和目标ID
     * 3. 查询收藏记录是否存在
     * 4. 删除收藏记录
     * 5. 更新目标内容的收藏数量-1
     * 
     * 使用场景：
     * - 收藏夹清理，移除不需要的内容
     * - 误收藏后的撤销操作
     * 
     * 请求示例：
     * DELETE /api/v1/collections?targetType=QUESTION&targetId=123
     * Headers:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiI...
     * 
     * 成功响应：
     * {
     *     "code": 200,
     *     "message": "取消收藏成功",
     *     "data": null
     * }
     * 
     * @param targetType 收藏目标类型
     * @param targetId 目标ID
     * @param authentication 认证对象
     * @return Result<Void> 无数据返回
     * @throws ResourceNotFoundException 当收藏记录不存在时抛出
     */
    @DeleteMapping  // 处理DELETE请求
    public Result<Void> uncollect(
            @RequestParam String targetType,
            @RequestParam Long targetId,
            Authentication authentication) {
        // 获取当前用户ID
        Long userId = (Long) authentication.getPrincipal();
        // 记录取消收藏日志
        log.info("取消收藏: userId={}, type={}, targetId={}", userId, targetType, targetId);
        // 调用服务层取消收藏
        collectionService.uncollect(userId, targetType, targetId);
        // 返回成功响应
        return Result.success("取消收藏成功", null);
    }

    /**
     * ✔️ 检查是否已收藏 - 查询某个内容的收藏状态
     * 
     * 业务流程：
     * 1. 从认证对象获取当前用户ID
     * 2. 根据用户ID、目标类型、目标ID查询收藏记录
     * 3. 返回true（已收藏）或false（未收藏）
     * 
     * 使用场景：
     * - 问题详情页显示收藏按钮的状态
     * - 列表页显示每个问题是否已收藏
     * 
     * 请求示例：
     * GET /api/v1/collections/check?targetType=QUESTION&targetId=123
     * Headers:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiI...
     * 
     * 成功响应示例：
     * {
     *     "code": 200,
     *     "message": "成功",
     *     "data": true
     * }
     * 
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @param authentication 认证对象
     * @return Result<Boolean> 返回true表示已收藏，false表示未收藏
     */
    @GetMapping("/check")  // 处理GET请求
    public Result<Boolean> checkCollected(
            @RequestParam String targetType,
            @RequestParam Long targetId,
            Authentication authentication) {
        // 获取当前用户ID
        Long userId = (Long) authentication.getPrincipal();
        // 调用服务层检查是否已收藏
        boolean isCollected = collectionService.isCollected(userId, targetType, targetId);
        // 返回检查结果
        return Result.success(isCollected);
    }

    /**
     * 📋 获取收藏的问题列表 - 查看用户的所有收藏
     * 
     * 业务流程：
     * 1. 从认证对象获取当前用户ID
     * 2. 查询该用户的所有收藏记录
     * 3. 根据收藏记录查询对应的问题详情
     * 4. 过滤已删除的问题
     * 5. 按收藏时间降序排序
     * 6. 组装问题信息返回
     * 
     * 使用场景：
     * - 用户打开“我的收藏”页面
     * - 查看自己收藏过的所有问题
     * - 复习之前收藏的学习资料
     * 
     * 请求示例：
     * GET /api/v1/collections/questions
     * Headers:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiI...
     * 
     * 成功响应示例：
     * {
     *     "code": 200,
     *     "message": "成功",
     *     "data": [
     *         {
     *             "id": 123,
     *             "title": "如何理解Java中的多态？",
     *             "content": "...",
     *             "subjectName": "面向对象程序设计",
     *             "status": "ANSWERED",
     *             "answerCount": 5,
     *             "collectedAt": "2024-11-15T10:30:00"
     *         }
     *     ]
     * }
     * 
     * @param authentication 认证对象，包含当前用户ID
     * @return Result<List<QuestionDTO>> 收藏的问题列表
     */
    @GetMapping("/questions")  // 处理GET请求
    public Result<List<QuestionDTO>> getCollectedQuestions(Authentication authentication) {
        // 获取当前用户ID
        Long userId = (Long) authentication.getPrincipal();
        // 记录查询日志
        log.info("获取收藏列表: userId={}", userId);
        // 调用服务层查询收藏的问题
        List<QuestionDTO> questions = collectionService.getCollectedQuestions(userId);
        // 返回问题列表
        return Result.success(questions);
    }

    /**
     * 📊 获取收藏数量 - 统计用户的总收藏数
     * 
     * 业务流程：
     * 1. 从认证对象获取当前用户ID
     * 2. 查询该用户的所有收藏记录数量
     * 3. 过滤已删除的内容
     * 4. 返回总数
     * 
     * 使用场景：
     * - 个人中心显示收藏数量
     * - 用户主页显示统计数据
     * 
     * 请求示例：
     * GET /api/v1/collections/count
     * Headers:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiI...
     * 
     * 成功响应示例：
     * {
     *     "code": 200,
     *     "message": "成功",
     *     "data": 23
     * }
     * 
     * @param authentication 认证对象
     * @return Result<Long> 收藏总数
     */
    @GetMapping("/count")  // 处理GET请求
    public Result<Long> getCollectionCount(Authentication authentication) {
        // 获取当前用户ID
        Long userId = (Long) authentication.getPrincipal();
        // 调用服务层获取收藏数量
        long count = collectionService.getCollectionCount(userId);
        // 返回收藏数量
        return Result.success(count);
    }
}

