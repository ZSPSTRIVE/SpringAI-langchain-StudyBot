package com.qasystem.service;

import com.qasystem.dto.ChangePasswordRequest;
import com.qasystem.dto.UpdateProfileRequest;
import com.qasystem.dto.UserProfileDTO;

/**
 * ProfileService - 个人中心服务接口
 * 
 * 🎯 作用：管理用户的个人资料和账户设置
 * 就像一个“个人档案管理员”，负责处理查看资料、修改信息、修改密码等操作。
 * 
 * 📝 主要功能：
 * 1. 查看个人资料：获取当前用户的完整信息
 * 2. 更新资料：修改姓名、邮箱、手机等信息
 * 3. 修改密码：需要验证原密码
 * 
 * 💡 使用场景：
 * - 用户进入个人中心查看资料
 * - 编辑个人信息
 * - 修改登录密码
 */
public interface ProfileService {

    /**
     * 获取当前用户个人信息
     * 
     * 🎯 功能：查询用户的完整个人资料
     * 包括基本信息和角色特定信息（学生/教师）。
     * 
     * @param userId 用户ID
     * @return 用户完整信息，包含所有字段
     * 
     * 💬 使用场景：
     * - 用户点击头像进入个人中心
     * - 页面展示用户的所有信息
     * - 学生显示学号、专业、班级等
     * - 教师显示工号、职称、研究方向等
     */
    UserProfileDTO getCurrentUserProfile(Long userId);

    /**
     * 更新个人信息
     * 
     * 🎯 功能：用户修改自己的个人资料
     * 只能修改非敏感信息，如姓名、邮箱、手机、头像等。
     * 不能修改用户名、角色、学号/工号等关键信息。
     * 
     * @param userId 用户ID
     * @param request 更新请求，包含要修改的字段
     * @return 更新后的用户信息
     * 
     * 💬 使用场景：
     * - 用户在个人中心点击“编辑资料”
     * - 修改邮箱、手机、头像等信息
     * - 学生可以更新专业、班级等
     * - 教师可以更新职称、研究方向、办公室、个人简介等
     */
    UserProfileDTO updateProfile(Long userId, UpdateProfileRequest request);

    /**
     * 修改密码
     * 
     * 🎯 功能：用户主动修改登录密码
     * 需要验证原密码，防止他人盗用账号后修改密码。
     * 
     * @param userId 用户ID
     * @param request 修改密码请求，包含原密码、新密码、确认密码
     * @throws 异常 如果原密码错误或新密码不符合规则
     * 
     * 💬 使用场景：
     * - 用户在个人中心点击“修改密码”
     * - 填写原密码、新密码和确认密码
     * - 提交后验证原密码是否正确
     * - 修改成功后需要重新登录
     * 
     * ⚠️ 注意：
     * - 与重置密码（ResetPassword）不同，这里必须验证原密码
     * - 新密码必须符合强度要求：6-20位，包含字母和数字
     */
    void changePassword(Long userId, ChangePasswordRequest request);
}

