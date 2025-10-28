package com.qasystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qasystem.entity.Forum;

/**
 * 论坛Service接口
 */
public interface ForumService extends IService<Forum> {
    
    /**
     * 分页查询帖子列表
     */
    IPage<Forum> getForumPage(Integer page, Integer limit, Forum query);
    
    /**
     * 获取帖子详情（包含子评论）
     */
    Forum getForumWithChildren(Long id);
    
    /**
     * 创建帖子或评论
     */
    Forum createForum(Forum forum, Long userId, String username);
    
    /**
     * 更新帖子
     */
    void updateForum(Forum forum);
    
    /**
     * 删除帖子
     */
    void deleteForum(Long[] ids);
}

