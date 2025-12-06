package com.qasystem.service;

import com.qasystem.dto.AnswerDTO;
import com.qasystem.dto.CreateAnswerRequest;

import java.util.List;

/**
 * AnswerService - 回答服务接口
 * 
 * 🎯 作用：处理所有与回答相关的业务逻辑
 * 就像一个“回答管理员”，负责处理教师回答问题、学生采纳答案等操作。
 * 
 * 📝 主要功能：
 * 1. 回答查询：查看某个问题的所有回答
 * 2. 回答管理：创建、编辑、删除回答
 * 3. 采纳机制：学生采纳最佳答案
 * 
 * 💡 使用流程：
 * 1. 查看问题时加载回答列表 → getAnswersByQuestionId()
 * 2. 教师提交回答 → createAnswer()
 * 3. 教师编辑自己的回答 → updateAnswer()
 * 4. 学生采纳满意的回答 → acceptAnswer()
 * 5. 删除回答 → deleteAnswer()
 */
public interface AnswerService {

    /**
     * 根据问题ID获取所有回答
     * 
     * 🎯 功能：查询某个问题下的所有回答
     * 被采纳的回答会排在最前面，其他按时间顺序排列。
     * 
     * @param questionId 问题ID
     * @return 回答列表，包含回答内容、教师信息、点赞数、是否被采纳等
     * 
     * 💬 使用场景：
     * - 用户进入问题详情页后，页面下方显示所有回答
     * - 被采纳的回答会有特殊标记并显示在最前面
     */
    List<AnswerDTO> getAnswersByQuestionId(Long questionId);

    /**
     * 创建回答
     * 
     * 🎯 功能：教师回答学生的问题
     * 就像教师看到学生的问题后，写下详细的答案并提交。
     * 
     * @param teacherId 回答的教师ID，从当前登录用户获取
     * @param request 回答请求数据，包含问题ID、回答内容、配图等
     * @return 创建成功的回答详情
     * 
     * 💬 使用场景：
     * - 教师在问题详情页点击“回答问题”
     * - 填写回答内容，可以上传解题步骤图
     * - 点击“提交回答”，调用此方法
     * - 学生会收到“您的问题有新回答”的通知
     */
    AnswerDTO createAnswer(Long teacherId, CreateAnswerRequest request);

    /**
     * 更新回答
     * 
     * 🎯 功能：教师修改自己的回答
     * 只有回答的作者（回答的教师）才能编辑，其他人无权限。
     * 
     * @param id 要更新的回答ID
     * @param teacherId 当前操作的教师ID，用于权限验证
     * @param request 新的回答数据
     * @return 更新后的回答详情
     * 
     * 💬 使用场景：
     * - 教师发现自己的回答有错误或需要补充
     * - 在自己的回答下点击“编辑”按钮
     * - 修改内容后提交，调用此方法
     */
    AnswerDTO updateAnswer(Long id, Long teacherId, CreateAnswerRequest request);

    /**
     * 删除回答
     * 
     * 🎯 功能：删除某个回答
     * 只有回答作者或管理员才能删除。
     * 
     * @param id 要删除的回答ID
     * @param userId 当前操作的用户ID，用于权限验证
     * 
     * 💬 使用场景：
     * - 教师想删除自己的回答（可能是回答错误或重复）
     * - 管理员删除违规回答
     */
    void deleteAnswer(Long id, Long userId);

    /**
     * 采纳回答
     * 
     * 🎯 功能：学生将某个回答设置为“采纳答案”
     * 就像评选“最佳答案”，被采纳的回答会显示在最前面，教师也会获得更多积分。
     * 每个问题只能采纳一个回答，采纳新回答会取消之前的采纳。
     * 
     * @param id 要采纳的回答ID
     * @param studentId 学生ID，只有问题作者才能采纳回答
     * 
     * 💬 使用场景：
     * - 学生查看自己问题的回答列表
     * - 找到最满意的回答，点击“采纳”按钮
     * - 该回答会显示“已采纳”标记并排在第一位
     * - 问题状态可能变为“已解决”
     */
    void acceptAnswer(Long id, Long studentId);
}

