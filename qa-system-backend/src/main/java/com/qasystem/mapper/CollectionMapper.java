package com.qasystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qasystem.entity.UserCollection;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

/**
 * CollectionMapper接口 - 收藏数据访问层
 * 
 * 🎯 作用说明：
 * 这个接口负责操作数据库中的user_collection表，就像一个"书签管理员"。
 * 管理着用户收藏的内容，就像浏览器的书签功能或微博的收藏。
 * 用户可以收藏问题、答案等内容，方便后续查看。
 * 
 * 📚 系统中的作用：
 * 1. 收藏问题：学生收藏有价值的问题
 * 2. 收藏答案：收藏优秀的答案
 * 3. 查看我的收藏：显示所有收藏内容
 * 4. 取消收藏：删除收藏记录
 * 
 * 🏗️ 技术架构：
 * - 继承自MyBatis-Plus的BaseMapper<UserCollection>
 * - BaseMapper自动提供17个基础方法
 * - 我们添加了2个常用查询方法：
 *   1. findByUserAndTarget：检查是否已收藏
 *   2. findByUserAndType：查询用户的所有收藏（可按类型筛选）
 * 
 * 📊 对应数据库表: user_collection
 * 
 * 🔗 关联关系：
 * - 关联user表：collection.user_id = user.id （收藏者）
 * - 关联question表：collection.target_id = question.id （当target_type='QUESTION'）
 * - 关联answer表：collection.target_id = answer.id （当target_type='ANSWER'）
 * 
 * @author QA System Team
 * @version 1.0
 */
@Mapper
public interface CollectionMapper extends BaseMapper<UserCollection> {

    /**
     * 查询是否已收藏
     * 
     * 🎯 方法作用：
     * 检查某个用户是否已经收藏了某个内容，就像检查“是否已收藏”。
     * 用于显示收藏按钮的状态：如果已收藏，显示实心星星；如果未收藏，显示空心星星。
     * 
     * @param userId 用户ID
     * @param targetType 目标类型（"QUESTION"或"ANSWER"）
     * @param targetId 目标ID（问题ID或答案ID）
     * @return Optional<UserCollection> 包装的收藏记录
     *         - 如果已收藏：Optional.of(collection)
     *         - 如果未收藏：Optional.empty()
     */
    default Optional<UserCollection> findByUserAndTarget(Long userId, String targetType, Long targetId) {
        return Optional.ofNullable(
            selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserCollection>()
                .eq(UserCollection::getUserId, userId)
                .eq(UserCollection::getTargetType, targetType)
                .eq(UserCollection::getTargetId, targetId))
        );
    }

    /**
     * 查询用户的所有收藏
     * 
     * 🎯 方法作用：
     * 查询某个用户的所有收藏，可以按类型筛选，按收藏时间降序排列。
     * 就像在浏览器中查看“我的书签”。
     * 
     * @param userId 用户ID
     * @param targetType 目标类型（可选）
     *        - null：查询所有类型的收藏
     *        - "QUESTION"：只查询收藏的问题
     *        - "ANSWER"：只查询收藏的答案
     * @return List<UserCollection> 收藏记录列表
     *         - 按收藏时间降序排列
     *         - 如果没有收藏，返回空列表
     */
    default List<UserCollection> findByUserAndType(Long userId, String targetType) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserCollection>()
                .eq(UserCollection::getUserId, userId)
                .eq(targetType != null, UserCollection::getTargetType, targetType)
                .orderByDesc(UserCollection::getCreateTime));
    }
}

