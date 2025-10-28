package com.qasystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qasystem.entity.Forum;
import com.qasystem.service.ForumService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 论坛控制器（兼容旧系统API）
 */
@Slf4j
@RestController
@RequestMapping("/forum")
@RequiredArgsConstructor
public class ForumController {

    private final ForumService forumService;

    /**
     * 前端列表（公开访问）
     */
    @GetMapping("/flist")
    public Map<String, Object> flist(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            Forum query) {
        log.info("📋 查询论坛帖子列表: page={}, limit={}", page, limit);
        
        IPage<Forum> pageResult = forumService.getForumPage(page, limit, query);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");
        
        Map<String, Object> data = new HashMap<>();
        data.put("total", pageResult.getTotal());
        data.put("list", pageResult.getRecords());
        result.put("data", data);
        
        return result;
    }

    /**
     * 后端分页（需要认证）
     */
    @GetMapping("/page")
    public Map<String, Object> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            Forum query,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getPrincipal();
        log.info("📋 后端查询论坛帖子: page={}, limit={}, userId={}", page, limit, userId);
        
        // 非管理员只能看自己的帖子
        // 这里简化处理，后续可以根据角色判断
        query.setUserid(userId);
        
        IPage<Forum> pageResult = forumService.getForumPage(page, limit, query);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");
        
        Map<String, Object> data = new HashMap<>();
        data.put("total", pageResult.getTotal());
        data.put("list", pageResult.getRecords());
        result.put("data", data);
        
        return result;
    }

    /**
     * 获取帖子详情（公开访问）
     */
    @GetMapping("/info/{id}")
    public Map<String, Object> info(@PathVariable Long id) {
        log.info("📄 获取帖子详情: id={}", id);
        
        Forum forum = forumService.getById(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");
        result.put("data", forum);
        
        return result;
    }

    /**
     * 获取帖子详情（包含评论树，公开访问）
     */
    @GetMapping("/list/{id}")
    public Map<String, Object> detail(@PathVariable Long id) {
        log.info("📄 获取帖子详情（含评论）: id={}", id);
        
        Forum forum = forumService.getForumWithChildren(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");
        result.put("data", forum);
        
        return result;
    }

    /**
     * 创建帖子或评论（需要认证）
     */
    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody Forum forum, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        
        // 从用户信息中获取用户名（简化处理）
        String username = userId.toString();
        
        log.info("✍️ 创建帖子/评论: userId={}, parentid={}", userId, forum.getParentid());
        
        Forum created = forumService.createForum(forum, userId, username);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "发布成功");
        result.put("data", created);
        
        return result;
    }

    /**
     * 保存帖子（需要认证）
     */
    @PostMapping("/save")
    public Map<String, Object> save(@RequestBody Forum forum, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        String username = userId.toString();
        
        log.info("💾 保存帖子: userId={}", userId);
        
        Forum created = forumService.createForum(forum, userId, username);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "保存成功");
        result.put("data", created);
        
        return result;
    }

    /**
     * 更新帖子（需要认证）
     */
    @PostMapping("/update")
    public Map<String, Object> update(@RequestBody Forum forum) {
        log.info("📝 更新帖子: id={}", forum.getId());
        
        forumService.updateForum(forum);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "更新成功");
        
        return result;
    }

    /**
     * 删除帖子（需要认证）
     */
    @PostMapping("/delete")
    public Map<String, Object> delete(@RequestBody Long[] ids) {
        log.info("🗑️ 删除帖子: ids={}", (Object) ids);
        
        forumService.deleteForum(ids);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "删除成功");
        
        return result;
    }
}

