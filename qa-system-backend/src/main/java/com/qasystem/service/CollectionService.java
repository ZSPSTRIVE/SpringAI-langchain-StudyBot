package com.qasystem.service;

import com.qasystem.dto.QuestionDTO;

import java.util.List;

/**
 * CollectionService - 收藏服务接口
 * 
 * 🎯 作用：管理用户的收藏功能
 * 就像浏览器的“收藏夹”或者记事本的“书签”，用户可以收藏感兴趣的问题。
 * 
 * 📝 主要功能：
 * 1. 收藏管理：添加收藏、取消收藏
 * 2. 收藏查询：查看收藏列表、检查是否已收藏
 * 3. 数据统计：收藏总数
 * 
 * 💡 使用场景：
 * - 用户看到好的问题想保存下来
 * - 点击“收藏”按钮，以后可以在“我的收藏”中查看
 * - 不需要的时候可以取消收藏
 */
public interface CollectionService {

    /**
     * 收藏内容
     * 
     * 🎯 功能：用户收藏某个内容（问题、回答等）
     * 就像在浏览器中点击“收藏到书签”。
     * 
     * @param userId 用户ID
     * @param targetType 收藏类型，如"QUESTION"（问题）、"ANSWER"（回答）
     * @param targetId 收藏目标的ID，如问题ID或回答ID
     * 
     * 💬 使用场景：
     * - 用户查看问题时点击“收藏”按钮
     * - 或者看到一个很好的回答也可以收藏
     * - 收藏后按钮状态变为“已收藏”
     */
    void collect(Long userId, String targetType, Long targetId);

    /**
     * 取消收藏
     * 
     * 🎯 功能：移除收藏
     * 就像从书签夹中删除一个书签。
     * 
     * @param userId 用户ID
     * @param targetType 收藏类型
     * @param targetId 收藏目标ID
     * 
     * 💬 使用场景：
     * - 用户再次点击“已收藏”按钮，取消收藏
     * - 或者在“我的收藏”页面中删除某个收藏
     */
    void uncollect(Long userId, String targetType, Long targetId);

    /**
     * 检查是否已收藏
     * 
     * 🎯 功能：判断用户是否已经收藏了某个内容
     * 用于显示按钮状态（收藏/已收藏）。
     * 
     * @param userId 用户ID
     * @param targetType 收藏类型
     * @param targetId 收藏目标ID
     * @return true-已收藏，false-未收藏
     * 
     * 💬 使用场景：
     * - 用户打开问题详情页时，检查是否已收藏
     * - 如果已收藏，按钮显示“已收藏”状态
     * - 如果未收藏，按钮显示“收藏”状态
     */
    boolean isCollected(Long userId, String targetType, Long targetId);

    /**
     * 获取用户收藏的问题列表
     * 
     * 🎯 功能：查询用户所有收藏的问题
     * 就像打开浏览器的书签夹，查看所有保存的网页。
     * 
     * @param userId 用户ID
     * @return 收藏的问题列表，按收藏时间降序
     * 
     * 💬 使用场景：
     * - 用户点击个人中心的“我的收藏”
     * - 显示所有收藏过的问题
     * - 可以快速回到之前感兴趣的问题
     */
    List<QuestionDTO> getCollectedQuestions(Long userId);

    /**
     * 获取收藏总数
     * 
     * 🎯 功能：统计用户收藏了多少个内容
     * 
     * @param userId 用户ID
     * @return 收藏数量
     * 
     * 💬 使用场景：
     * - 在个人中心显示“我的收藏 (15)”
     * - 让用户知道自己收藏了多少内容
     */
    long getCollectionCount(Long userId);
}

