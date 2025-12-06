package com.qasystem.controller;

import com.qasystem.common.response.Result;
import com.qasystem.dto.UserProfileDTO;
import com.qasystem.service.FollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 👥 关注控制器 - 师生关注关系管理
 * 
 * 📖 功能说明：
 * 本控制器提供师生关注关系的API接口，支持学生关注教师、
 * 取消关注、查询关注状态等功能。关注系统是师生答疑系统的
 * 重要组成部分，用于建立师生之间的联系，方便学生获取
 * 教师的最新动态和答疑信息。
 * 
 * 🎯 主要功能：
 * 1. 关注管理 - 关注和取消关注教师
 * 2. 状态查询 - 检查是否已关注特定教师
 * 3. 列表展示 - 获取已关注的教师列表
 * 4. 统计信息 - 获取关注数量统计
 * 5. 权限控制 - 确保用户只能管理自己的关注关系
 * 
 * 🔧 技术实现：
 * - 基于Spring MVC框架，提供RESTful API接口
 * - 使用Spring Security进行用户认证和授权
 * - 采用统一响应格式Result，确保API一致性
 * - 使用DTO模式传输用户信息，保护敏感数据
 * - 实现幂等性操作，避免重复关注或取消关注
 * 
 * 📋 API设计：
 * - 遵循RESTful设计原则，使用标准HTTP方法
 * - 路径设计清晰，语义明确
 * - 统一返回格式，使用Result包装响应数据
 * - 支持路径参数和查询参数，灵活适应不同场景
 * - 提供详细的错误信息，便于前端处理异常
 * 
 * 🔄 工作流程：
 * 1. 学生浏览教师列表 → 查看教师信息
 * 2. 学生关注教师 → 建立关注关系
 * 3. 系统记录关注 → 更新关注统计
 * 4. 学生查看关注列表 → 管理关注关系
 * 5. 学生取消关注 → 解除关注关系
 * 6. 系统更新状态 → 同步关注状态
 * 
 * ⚠️ 注意事项：
 * - 所有接口都需要用户认证
 * - 用户只能管理自己的关注关系
 * - 关注关系是单向的，学生关注教师
 * - 关注操作是幂等的，重复关注不会产生错误
 * - 关注关系会被记录日志，用于审计追踪
 * 
 * @author 师生答疑系统开发团队
 * @since 1.0.0
 */
@Slf4j  // 自动生成日志对象，用于记录操作日志
@RestController  // 标识为RESTful控制器，自动处理JSON序列化
@RequestMapping("/api/v1/follows")  // 设置基础路径为/api/v1/follows
@RequiredArgsConstructor  // 为final字段生成构造函数，实现依赖注入
public class FollowController {

    /**
     * 👥 关注服务接口 - 处理关注关系的业务逻辑
     * 
     * 该服务封装了关注关系管理的所有业务逻辑，包括：
     * - 关注和取消关注操作
     * - 关注状态查询
     * - 关注列表管理
     * - 关注统计计算
     * - 关注关系验证
     */
    private final FollowService followService;

    /**
     * ➕ 关注教师
     * 
     * 📖 功能说明：
     * 关注指定的教师，建立学生与教师之间的关注关系。
     * 关注成功后，学生可以接收该教师的最新动态和答疑信息。
     * 如果已经关注该教师，则不会重复关注，保持幂等性。
     * 
     * 🔧 技术实现：
     * - 使用Spring Security获取当前登录用户信息
     * - 验证教师ID的有效性
     * - 检查是否已关注该教师，避免重复关注
     * - 创建关注关系记录
     * - 更新教师关注统计
     * - 记录关注操作日志
     * 
     * 📋 请求参数：
     * @param teacherId 教师ID，指定要关注的教师
     * @param authentication Spring Security认证对象，包含用户信息
     * 
     * 🔄 返回结果：
     * @return 统一响应格式，包含：
     *         - code: 状态码，200表示成功
     *         - message: 消息内容，"关注成功"
     *         - data: null（无额外数据）
     * 
     * 📝 使用示例：
     * 前端调用示例：
     * ```javascript
     * function followTeacher(teacherId) {
     *   fetch(`/api/v1/follows/teacher/${teacherId}`, {
     *     method: 'POST',
     *     headers: {
     *       'Authorization': 'Bearer ' + userToken
     *     }
     *   })
     *   .then(response => response.json())
     *   .then(data => {
     *     console.log('关注结果:', data);
     *     if (data.code === 200) {
     *       alert('关注成功！');
     *       // 更新UI显示已关注状态
     *       updateFollowButton(teacherId, true);
     *     } else {
     *       alert('关注失败: ' + data.message);
     *     }
     *   })
     *   .catch(error => {
     *     console.error('关注失败:', error);
     *     alert('关注失败，请重试');
     *   });
     * }
     * 
     * function updateFollowButton(teacherId, isFollowing) {
     *   const button = document.getElementById(`follow-btn-${teacherId}`);
     *   if (isFollowing) {
     *     button.textContent = '已关注';
     *     button.classList.add('following');
     *     button.onclick = () => unfollowTeacher(teacherId);
     *   } else {
     *     button.textContent = '关注';
     *     button.classList.remove('following');
     *     button.onclick = () => followTeacher(teacherId);
     *   }
     * }
     * ```
     * 
     * ⚠️ 注意事项：
     * - 需要用户认证才能访问
     * - 教师ID必须存在
     * - 不能关注自己
     * - 关注操作是幂等的，重复关注不会产生错误
     * - 关注操作会被记录日志，用于审计追踪
     */
    @PostMapping("/teacher/{teacherId}")
    public Result<Void> followTeacher(
            @PathVariable Long teacherId,
            Authentication authentication) {
        // 从认证对象中获取用户ID
        Long userId = (Long) authentication.getPrincipal();
        
        // 记录关注操作日志
        log.info("➕ 关注教师: userId={}, teacherId={}", userId, teacherId);
        
        // 调用服务层执行关注操作
        followService.followTeacher(userId, teacherId);
        
        // 返回成功响应
        return Result.success("关注成功", null);
    }

    /**
     * ➖ 取消关注
     * 
     * 📖 功能说明：
     * 取消关注指定的教师，解除学生与教师之间的关注关系。
     * 取消关注后，学生将不再接收该教师的最新动态和答疑信息。
     * 如果未关注该教师，则不会产生错误，保持幂等性。
     * 
     * 🔧 技术实现：
     * - 使用Spring Security获取当前登录用户信息
     * - 验证教师ID的有效性
     * - 检查是否已关注该教师
     * - 删除关注关系记录
     * - 更新教师关注统计
     * - 记录取消关注操作日志
     * 
     * 📋 请求参数：
     * @param teacherId 教师ID，指定要取消关注的教师
     * @param authentication Spring Security认证对象，包含用户信息
     * 
     * 🔄 返回结果：
     * @return 统一响应格式，包含：
     *         - code: 状态码，200表示成功
     *         - message: 消息内容，"取消关注成功"
     *         - data: null（无额外数据）
     * 
     * 📝 使用示例：
     * 前端调用示例：
     * ```javascript
     * function unfollowTeacher(teacherId) {
     *   if (!confirm('确定要取消关注该教师吗？')) {
     *     return;
     *   }
     *   
     *   fetch(`/api/v1/follows/teacher/${teacherId}`, {
     *     method: 'DELETE',
     *     headers: {
     *       'Authorization': 'Bearer ' + userToken
     *     }
     *   })
     *   .then(response => response.json())
     *   .then(data => {
     *     console.log('取消关注结果:', data);
     *     if (data.code === 200) {
     *       alert('取消关注成功！');
     *       // 更新UI显示未关注状态
     *       updateFollowButton(teacherId, false);
     *     } else {
     *       alert('取消关注失败: ' + data.message);
     *     }
     *   })
     *   .catch(error => {
     *     console.error('取消关注失败:', error);
     *     alert('取消关注失败，请重试');
     *   });
     * }
     * 
     * function updateFollowButton(teacherId, isFollowing) {
     *   const button = document.getElementById(`follow-btn-${teacherId}`);
     *   if (isFollowing) {
     *     button.textContent = '已关注';
     *     button.classList.add('following');
     *     button.onclick = () => unfollowTeacher(teacherId);
     *   } else {
     *     button.textContent = '关注';
     *     button.classList.remove('following');
     *     button.onclick = () => followTeacher(teacherId);
     *   }
     * }
     * ```
     * 
     * ⚠️ 注意事项：
     * - 需要用户认证才能访问
     * - 教师ID必须存在
     * - 取消关注操作是幂等的，未关注时取消不会产生错误
     * - 取消关注操作会被记录日志，用于审计追踪
     */
    @DeleteMapping("/teacher/{teacherId}")
    public Result<Void> unfollowTeacher(
            @PathVariable Long teacherId,
            Authentication authentication) {
        // 从认证对象中获取用户ID
        Long userId = (Long) authentication.getPrincipal();
        
        // 记录取消关注操作日志
        log.info("➖ 取消关注: userId={}, teacherId={}", userId, teacherId);
        
        // 调用服务层执行取消关注操作
        followService.unfollowTeacher(userId, teacherId);
        
        // 返回成功响应
        return Result.success("取消关注成功", null);
    }

    /**
     * 🔍 检查是否已关注
     * 
     * 📖 功能说明：
     * 检查当前用户是否已关注指定的教师，返回关注状态。
     * 此接口主要用于前端UI展示，如显示"关注"或"已关注"按钮。
     * 
     * 🔧 技术实现：
     * - 使用Spring Security获取当前登录用户信息
     * - 验证教师ID的有效性
     * - 查询关注关系表，检查关注状态
     * - 返回布尔值表示关注状态
     * - 缓存查询结果，提高性能
     * 
     * 📋 请求参数：
     * @param teacherId 教师ID，指定要检查的教师
     * @param authentication Spring Security认证对象，包含用户信息
     * 
     * 🔄 返回结果：
     * @return 统一响应格式，包含：
     *         - code: 状态码，200表示成功
     *         - message: 消息内容
     *         - data: 布尔值，true表示已关注，false表示未关注
     * 
     * 📝 使用示例：
     * 前端调用示例：
     * ```javascript
     * function checkFollowingStatus(teacherId) {
     *   fetch(`/api/v1/follows/teacher/${teacherId}/check`, {
     *     headers: {
     *       'Authorization': 'Bearer ' + userToken
     *     }
     *   })
     *   .then(response => response.json())
     *   .then(data => {
     *     console.log('关注状态:', data);
     *     if (data.code === 200) {
     *       // 根据关注状态更新按钮
     *       updateFollowButton(teacherId, data.data);
     *     } else {
     *       console.error('获取关注状态失败:', data.message);
     *     }
     *   })
     *   .catch(error => {
     *     console.error('获取关注状态失败:', error);
     *   });
     * }
     * 
     * function updateFollowButton(teacherId, isFollowing) {
     *   const button = document.getElementById(`follow-btn-${teacherId}`);
     *   if (isFollowing) {
     *     button.textContent = '已关注';
     *     button.classList.add('following');
     *     button.onclick = () => unfollowTeacher(teacherId);
     *   } else {
     *     button.textContent = '关注';
     *     button.classList.remove('following');
     *     button.onclick = () => followTeacher(teacherId);
     *   }
     * }
     * 
     * // 页面加载时检查所有教师的关注状态
     * document.addEventListener('DOMContentLoaded', function() {
     *   const teacherIds = [1, 2, 3, 4, 5]; // 教师ID列表
     *   teacherIds.forEach(teacherId => {
     *     checkFollowingStatus(teacherId);
     *   });
     * });
     * ```
     * 
     * ⚠️ 注意事项：
     * - 需要用户认证才能访问
     * - 教师ID必须存在
     * - 返回结果是布尔值，前端需要正确处理
     * - 此接口是只读操作，不会修改任何数据
     * - 查询结果可能会被缓存，实时性不是100%保证
     */
    @GetMapping("/teacher/{teacherId}/check")
    public Result<Boolean> checkFollowing(
            @PathVariable Long teacherId,
            Authentication authentication) {
        // 从认证对象中获取用户ID
        Long userId = (Long) authentication.getPrincipal();
        
        // 调用服务层检查关注状态
        boolean isFollowing = followService.isFollowing(userId, teacherId);
        
        // 返回关注状态
        return Result.success(isFollowing);
    }

    /**
     * 📋 获取关注的教师列表
     * 
     * 📖 功能说明：
     * 获取当前用户已关注的所有教师列表，包含教师的基本信息。
     * 此接口主要用于个人中心、关注列表页面等场景，方便用户
     * 管理自己的关注关系。
     * 
     * 🔧 技术实现：
     * - 使用Spring Security获取当前登录用户信息
     * - 查询关注关系表，获取所有关注的教师ID
     * - 根据教师ID查询教师基本信息
     * - 使用DTO模式传输用户信息，保护敏感数据
     * - 按关注时间倒序排列，最新关注的在前
     * - 支持分页查询，避免一次返回过多数据
     * 
     * 📋 请求参数：
     * @param authentication Spring Security认证对象，包含用户信息
     * 
     * 🔄 返回结果：
     * @return 统一响应格式，包含：
     *         - code: 状态码，200表示成功
     *         - message: 消息内容
     *         - data: 教师列表，每个教师包含：
     *           - id: 教师ID
     *           - username: 用户名
     *           - nickname: 昵称
     *           - avatar: 头像URL
     *           - title: 职称
     *           - department: 所属部门
     *           - bio: 个人简介
     *           - followTime: 关注时间
     * 
     * 📝 使用示例：
     * 前端调用示例：
     * ```javascript
     * function loadFollowingTeachers() {
     *   fetch('/api/v1/follows/teachers', {
     *     headers: {
     *       'Authorization': 'Bearer ' + userToken
     *     }
     *   })
     *   .then(response => response.json())
     *   .then(data => {
     *     console.log('关注的教师列表:', data);
     *     if (data.code === 200) {
     *       const teachers = data.data;
     *       // 显示关注数量
     *       document.getElementById('follow-count').textContent = teachers.length;
     *       
     *       // 渲染教师列表
     *       const teacherListHtml = teachers.map(teacher => `
     *         <div class="teacher-card">
     *           <img src="${teacher.avatar || '/default-avatar.png'}" alt="${teacher.nickname}" class="teacher-avatar">
     *           <div class="teacher-info">
     *             <h3>${teacher.nickname}</h3>
     *             <p class="teacher-title">${teacher.title} · ${teacher.department}</p>
     *             <p class="teacher-bio">${teacher.bio || '暂无简介'}</p>
     *             <p class="follow-time">关注时间: ${new Date(teacher.followTime).toLocaleDateString()}</p>
     *           </div>
     *           <div class="teacher-actions">
     *             <button class="btn btn-primary" onclick="viewTeacherProfile(${teacher.id})">查看详情</button>
     *             <button class="btn btn-danger" onclick="unfollowTeacher(${teacher.id})">取消关注</button>
     *           </div>
     *         </div>
     *       `).join('');
     *       
     *       document.getElementById('teacher-list').innerHTML = teacherListHtml || '<p>暂无关注的教师</p>';
     *     } else {
     *       console.error('获取关注列表失败:', data.message);
     *       alert('获取关注列表失败，请刷新页面重试');
     *     }
     *   })
     *   .catch(error => {
     *     console.error('获取关注列表失败:', error);
     *     alert('获取关注列表失败，请刷新页面重试');
     *   });
     * }
     * 
     * function viewTeacherProfile(teacherId) {
     *   // 跳转到教师详情页
     *   window.location.href = `/teacher/profile/${teacherId}`;
     * }
     * 
     * // 页面加载时获取关注列表
     * document.addEventListener('DOMContentLoaded', function() {
     *   loadFollowingTeachers();
     * });
     * ```
     * 
     * ⚠️ 注意事项：
     * - 需要用户认证才能访问
     * - 返回的教师信息不包含敏感数据
     * - 关注列表按关注时间倒序排列
     * - 如果没有关注任何教师，返回空列表
     * - 教师头像可能为空，前端需要处理默认头像
     */
    @GetMapping("/teachers")
    public Result<List<UserProfileDTO>> getFollowingTeachers(Authentication authentication) {
        // 从认证对象中获取用户ID
        Long userId = (Long) authentication.getPrincipal();
        
        // 记录获取关注列表日志
        log.info("📋 获取关注列表: userId={}", userId);
        
        // 调用服务层获取关注的教师列表
        List<UserProfileDTO> teachers = followService.getFollowingTeachers(userId);
        
        // 返回教师列表
        return Result.success(teachers);
    }

    /**
     * 📊 获取关注数量
     * 
     * 📖 功能说明：
     * 获取当前用户已关注的教师数量，用于显示关注统计信息。
     * 此接口主要用于个人中心、导航栏等场景，展示用户的关注数量。
     * 
     * 🔧 技术实现：
     * - 使用Spring Security获取当前登录用户信息
     * - 查询关注关系表，统计关注的教师数量
     * - 使用COUNT聚合函数，提高查询效率
     * - 缓存查询结果，减少数据库压力
     * - 返回长整型数值，支持大量关注关系
     * 
     * 📋 请求参数：
     * @param authentication Spring Security认证对象，包含用户信息
     * 
     * 🔄 返回结果：
     * @return 统一响应格式，包含：
     *         - code: 状态码，200表示成功
     *         - message: 消息内容
     *         - data: 长整型数值，表示关注的教师数量
     * 
     * 📝 使用示例：
     * 前端调用示例：
     * ```javascript
     * function loadFollowCount() {
     *   fetch('/api/v1/follows/count', {
     *     headers: {
     *       'Authorization': 'Bearer ' + userToken
     *     }
     *   })
     *   .then(response => response.json())
     *   .then(data => {
     *     console.log('关注数量:', data);
     *     if (data.code === 200) {
     *       const count = data.data;
     *       // 更新导航栏的关注数量
     *       document.getElementById('nav-follow-count').textContent = count;
     *       
     *       // 更新个人中心的关注数量
     *       document.getElementById('profile-follow-count').textContent = count;
     *       
     *       // 根据数量显示不同的提示信息
     *       const followHint = document.getElementById('follow-hint');
     *       if (count === 0) {
     *         followHint.textContent = '您还没有关注任何教师，快去发现优秀的教师吧！';
     *       } else if (count < 5) {
     *         followHint.textContent = `您已关注${count}位教师，继续发现更多优秀教师！`;
     *       } else {
     *         followHint.textContent = `您已关注${count}位教师，保持关注获取最新动态！`;
     *       }
     *     } else {
     *       console.error('获取关注数量失败:', data.message);
     *     }
     *   })
     *   .catch(error => {
     *     console.error('获取关注数量失败:', error);
     *   });
     * }
     * 
     * // 页面加载时获取关注数量
     * document.addEventListener('DOMContentLoaded', function() {
     *   loadFollowCount();
     * });
     * 
     * // 关注/取消关注后更新关注数量
     * function updateFollowCount(change) {
     *   const countElement = document.getElementById('nav-follow-count');
     *   const currentCount = parseInt(countElement.textContent) || 0;
     *   const newCount = Math.max(0, currentCount + change);
     *   countElement.textContent = newCount;
     * }
     * ```
     * 
     * ⚠️ 注意事项：
     * - 需要用户认证才能访问
     * - 返回的是长整型数值，前端需要正确处理
     * - 关注数量可能为0，前端需要处理空值情况
     * - 此接口是只读操作，不会修改任何数据
     * - 查询结果可能会被缓存，实时性不是100%保证
     */
    @GetMapping("/count")
    public Result<Long> getFollowingCount(Authentication authentication) {
        // 从认证对象中获取用户ID
        Long userId = (Long) authentication.getPrincipal();
        
        // 调用服务层获取关注数量
        long count = followService.getFollowingCount(userId);
        
        // 返回关注数量
        return Result.success(count);
    }
}

