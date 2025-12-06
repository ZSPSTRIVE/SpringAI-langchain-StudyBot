package com.qasystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qasystem.entity.Follow;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

/**
 * FollowMapper接口 - 关注关系数据访问层
 * 
 * 🎯 作用说明：
 * 这个接口负责操作数据库中的follow表，就像一个"关注关系管理员"。
 * 管理着学生关注教师的关系，就像微博/微信中的关注功能。
 * 学生可以关注自己喜欢的教师，方便快速查看该教师的最新回答。
 * 
 * 📚 系统中的作用：
 * 1. 学生关注教师：记录关注关系
 * 2. 取消关注：删除关注记录
 * 3. 查看我的关注：显示关注的所有教师
 * 4. 查看粉丝：教师查看有多少学生关注了自己
 * 
 * 🏗️ 技术架构：
 * - 继承自MyBatis-Plus的BaseMapper<Follow>
 * - BaseMapper自动提供17个基础方法
 * - 我们添加了3个常用查询方法：
 *   1. findByFollowerAndFollowee：检查是否已关注
 *   2. findByFollower：查询某学生关注的所有教师
 *   3. findByFollowee：查询关注某教师的所有学生
 * 
 * 📊 对应数据库表: follow
 * 
 * 🔗 关联关系：
 * - 关联student表：follow.follower_id = student.id （关注者是学生）
 * - 关联teacher表：follow.followee_id = teacher.id （被关注者是教师）
 * 
 * @author QA System Team
 * @version 1.0
 */
@Mapper
public interface FollowMapper extends BaseMapper<Follow> {

    /**
     * 查询是否已关注
     * 
     * 🎯 方法作用：
     * 检查某个学生是否已经关注了某个教师，就像在微博中检查“是否关注”。
     * 用于显示关注按钮的状态：如果已关注，显示“已关注”；如果未关注，显示“+关注”。
     * 
     * @param followerId 关注者ID（学生ID）
     * @param followeeId 被关注者ID（教师ID）
     * @return Optional<Follow> 包装的关注记录
     *         - 如果已关注：Optional.of(follow)
     *         - 如果未关注：Optional.empty()
     */
    default Optional<Follow> findByFollowerAndFollowee(Long followerId, Long followeeId) {
        return Optional.ofNullable(
            selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowerId, followerId)
                .eq(Follow::getFolloweeId, followeeId))
        );
    }

    /**
     * 查询用户关注的所有教师
     * 
     * 🎯 方法作用：
     * 查询某个学生关注的所有教师，按关注时间降序排列（最新关注的在最前）。
     * 就像在微博中查看“我的关注”列表。
     * 
     * @param followerId 关注者ID（学生ID）
     * @return List<Follow> 关注记录列表
     *         - 按关注时间降序排列
     *         - 如果没有关注任何教师，返回空列表
     */
    default List<Follow> findByFollower(Long followerId) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowerId, followerId)
                .orderByDesc(Follow::getCreateTime));
    }

    /**
     * 查询关注某教师的所有学生
     * 
     * 🎯 方法作用：
     * 查询关注某个教师的所有学生（粉丝列表），按关注时间降序排列。
     * 就像在微博中查看“我的粉丝”列表。
     * 
     * @param followeeId 被关注者ID（教师ID）
     * @return List<Follow> 关注记录列表
     *         - 按关注时间降序排列
     *         - 如果没有人关注，返回空列表
     */
    default List<Follow> findByFollowee(Long followeeId) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Follow>()
                .eq(Follow::getFolloweeId, followeeId)
                .orderByDesc(Follow::getCreateTime));
    }
}

