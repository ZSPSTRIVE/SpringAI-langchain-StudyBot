package com.qasystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qasystem.entity.Forum;
import com.qasystem.mapper.ForumMapper;
import com.qasystem.service.ForumService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 论坛Service实现类
 */
@Slf4j
@Service
public class ForumServiceImpl extends ServiceImpl<ForumMapper, Forum> implements ForumService {

    @Override
    public IPage<Forum> getForumPage(Integer page, Integer limit, Forum query) {
        Page<Forum> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<Forum> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询顶级帖子（parentid为0或null）
        wrapper.and(w -> w.eq(Forum::getParentid, 0).or().isNull(Forum::getParentid));
        
        // 根据查询条件过滤
        if (query != null) {
            if (query.getTitle() != null && !query.getTitle().isEmpty()) {
                wrapper.like(Forum::getTitle, query.getTitle());
            }
            if (query.getContent() != null && !query.getContent().isEmpty()) {
                wrapper.like(Forum::getContent, query.getContent());
            }
            if (query.getUserid() != null) {
                wrapper.eq(Forum::getUserid, query.getUserid());
            }
            if (query.getIsdone() != null && !query.getIsdone().isEmpty()) {
                wrapper.eq(Forum::getIsdone, query.getIsdone());
            }
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(Forum::getAddtime);
        
        return this.page(pageParam, wrapper);
    }

    @Override
    public Forum getForumWithChildren(Long id) {
        Forum forum = this.getById(id);
        if (forum != null) {
            buildChildTree(forum);
        }
        return forum;
    }

    /**
     * 递归构建子评论树
     */
    private void buildChildTree(Forum forum) {
        List<Forum> children = this.list(
            new LambdaQueryWrapper<Forum>().eq(Forum::getParentid, forum.getId())
        );
        
        if (children != null && !children.isEmpty()) {
            forum.setChilds(children);
            for (Forum child : children) {
                buildChildTree(child);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Forum createForum(Forum forum, Long userId, String username) {
        // 设置用户信息
        forum.setUserid(userId);
        forum.setUsername(username);
        
        // 如果没有设置parentid，默认为0（顶级帖子）
        if (forum.getParentid() == null) {
            forum.setParentid(0L);
        }
        
        // 设置创建时间
        forum.setAddtime(LocalDateTime.now());
        
        // 如果是顶级帖子且没有设置状态，默认为"开放"
        if (forum.getParentid() == 0 && (forum.getIsdone() == null || forum.getIsdone().isEmpty())) {
            forum.setIsdone("开放");
        }
        
        this.save(forum);
        return forum;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateForum(Forum forum) {
        this.updateById(forum);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteForum(Long[] ids) {
        this.removeByIds(Arrays.asList(ids));
    }
}

