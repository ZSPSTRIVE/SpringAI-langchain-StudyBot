package com.qasystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qasystem.entity.Forum;

/**
 * ForumService - 论坛服务接口
 * 
 * 🎯 作用：管理论坛讨论区功能
 * 就像一个“论坛管理员”，处理帖子发布、评论、查询等操作。
 * 论坛是一个更开放的交流区，不像问答那么正式，学生和教师可以在这里随意交流。
 * 
 * 📝 主要功能：
 * 1. 帖子管理：创建、编辑、删除帖子
 * 2. 评论管理：回复帖子，查看评论
 * 3. 帖子查询：分页查询、查看详情
 * 
 * 💡 使用场景：
 * - 用户在论坛区浏览帖子
 * - 发表新帖子或回复别人的帖子
 * - 查看帖子详情和所有回复
 */
public interface ForumService extends IService<Forum> {
    
    /**
     * 分页查询论坛帖子列表
     * 
     * 🎯 功能：查询论坛中的所有帖子
     * 就像浏览贴吧或论坛的帖子列表。
     * 
     * @param page 页码
     * @param limit 每页数量
     * @param query 查询条件，可以筛选作者、分类等
     * @return 帖子列表分页结果
     * 
     * 💬 使用场景：
     * - 用户进入论坛页面，显示所有帖子
     * - 按时间排序，最新的帖子在最前面
     */
    IPage<Forum> getForumPage(Integer page, Integer limit, Forum query);
    
    /**
     * 获取帖子详情（包含所有评论）
     * 
     * 🎯 功能：查询某个帖子的完整内容和所有回复
     * 就像点开一个贴吧帖子，可以看到楼主发表的内容和所有楼层回复。
     * 
     * @param id 帖子ID
     * @return 帖子完整信息，包括所有子评论
     * 
     * 💬 使用场景：
     * - 用户点击帖子标题进入详情页
     * - 显示帖子内容和所有评论
     */
    Forum getForumWithChildren(Long id);
    
    /**
     * 创建帖子或评论
     * 
     * 🎯 功能：用户发表新帖子或回复帖子
     * 就像在论坛中发帖或者回复别人的帖子。
     * 
     * @param forum 帖子实体，包含标题、内容等
     * @param userId 用户ID
     * @param username 用户名
     * @return 创建成功的帖子
     * 
     * 💬 使用场景：
     * - 用户点击“发帖”，填写标题和内容后提交
     * - 或者在帖子详情页回复其他人
     */
    Forum createForum(Forum forum, Long userId, String username);
    
    /**
     * 更新帖子
     * 
     * 🎯 功能：用户编辑自己的帖子
     * 只有作者或管理员可以编辑。
     * 
     * @param forum 更新后的帖子数据
     * 
     * 💬 使用场景：
     * - 用户发现自己的帖子有错误，点击“编辑”修改
     */
    void updateForum(Forum forum);
    
    /**
     * 删除帖子
     * 
     * 🎯 功能：删除一个或多个帖子
     * 只有作者或管理员可以删除。
     * 
     * @param ids 帖子ID数组，支持批量删除
     * 
     * 💬 使用场景：
     * - 用户删除自己的帖子
     * - 管理员删除违规帖子
     * - 管理员批量删除多个帖子
     */
    void deleteForum(Long[] ids);
}

