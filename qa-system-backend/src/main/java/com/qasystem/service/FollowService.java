package com.qasystem.service;

import com.qasystem.dto.UserProfileDTO;

import java.util.List;

/**
 * FollowService - 关注服务接口
 * 
 * 🎯 作用：管理学生对教师的关注功能
 * 就像微博或微信的“关注”功能，学生可以关注喜欢的教师，关注后能收到教师的动态。
 * 
 * 📝 主要功能：
 * 1. 关注管理：关注教师、取消关注
 * 2. 关注查询：查看关注列表、粉丝列表
 * 3. 数据统计：关注数、粉丝数
 * 
 * 💡 使用场景：
 * - 学生遇到回答很好的教师，点击“关注”
 * - 关注后可以在“我关注的教师”中看到他们的回答
 * - 教师可以查看自己有多少粉丝
 */
public interface FollowService {

    /**
     * 关注教师
     * 
     * 🎯 功能：学生关注某个教师
     * 就像在社交媒体上点击“关注”按钮。
     * 
     * @param studentId 学生ID
     * @param teacherId 教师ID
     * 
     * 💬 使用场景：
     * - 学生看到某个教师的回答很专业
     * - 点击教师主页的“关注”按钮
     * - 关注后可以快速找到这个教师的其他回答
     */
    void followTeacher(Long studentId, Long teacherId);

    /**
     * 取消关注
     * 
     * 🎯 功能：学生取消对某个教师的关注
     * 就像在社交媒体上点击“取消关注”。
     * 
     * @param studentId 学生ID
     * @param teacherId 教师ID
     * 
     * 💬 使用场景：
     * - 学生再次点击“已关注”按钮，取消关注
     * - 或者在“我关注的教师”列表中取消关注
     */
    void unfollowTeacher(Long studentId, Long teacherId);

    /**
     * 检查是否已关注
     * 
     * 🎯 功能：判断学生是否已经关注了某个教师
     * 用于显示按钮状态（关注/已关注）。
     * 
     * @param studentId 学生ID
     * @param teacherId 教师ID
     * @return true-已关注，false-未关注
     * 
     * 💬 使用场景：
     * - 学生进入教师主页时，检查是否已关注
     * - 如果已关注，按钮显示“已关注”
     * - 如果未关注，按钮显示“关注”
     */
    boolean isFollowing(Long studentId, Long teacherId);

    /**
     * 获取学生关注的所有教师
     * 
     * 🎯 功能：查询学生关注了哪些教师
     * 就像查看微博的“我的关注”列表。
     * 
     * @param studentId 学生ID
     * @return 关注的教师列表
     * 
     * 💬 使用场景：
     * - 学生点击个人中心的“我关注的教师”
     * - 显示所有关注的教师信息
     * - 可以快速访问教师主页或查看他们的回答
     */
    List<UserProfileDTO> getFollowingTeachers(Long studentId);

    /**
     * 获取教师的粉丝列表
     * 
     * 🎯 功能：查询哪些学生关注了这个教师
     * 就像查看微博的“粉丝列表”。
     * 
     * @param teacherId 教师ID
     * @return 粉丝列表（关注该教师的学生）
     * 
     * 💬 使用场景：
     * - 教师查看自己有哪些粉丝
     * - 了解哪些学生关注了自己
     */
    List<UserProfileDTO> getFollowers(Long teacherId);

    /**
     * 获取学生关注的教师数量
     * 
     * 🎯 功能：统计学生关注了多少个教师
     * 
     * @param studentId 学生ID
     * @return 关注数量
     * 
     * 💬 使用场景：
     * - 在学生个人主页显示“关注 (8)”
     */
    long getFollowingCount(Long studentId);

    /**
     * 获取教师的粉丝数量
     * 
     * 🎯 功能：统计有多少学生关注了这个教师
     * 
     * @param teacherId 教师ID
     * @return 粉丝数量
     * 
     * 💬 使用场景：
     * - 在教师主页显示“粉丝 (125)”
     * - 反映教师的受欢迎程度
     */
    long getFollowersCount(Long teacherId);
}

