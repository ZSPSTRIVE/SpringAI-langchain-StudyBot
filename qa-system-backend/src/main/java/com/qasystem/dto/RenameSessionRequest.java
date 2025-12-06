package com.qasystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * RenameSessionRequest - 重命名AI会话请求数据传输对象
 * 
 * 🎯 作用：用户重命名AI聊天会话时使用
 * 就像用户与AI聊天后，可以给这次对话起个名字，方便以后查找。
 * 
 * 📝 字段说明：
 * - title: 新的会话标题
 * 
 * 💬 请求示例：
 * {
 *   "title": "二叉树学习笔记"
 * }
 * 
 * 💡 使用场景：
 * - 用户在AI聊天历史列表中选择一个会话
 * - 点击“重命名”按钮，输入新标题
 * - 调用 PUT /api/ai/session/{sessionId}/rename 接口
 * - 会话列表中显示新标题，替代默认的“对话1”、“对话2”等
 * 
 * 💡 常见标题示例：
 * - "二叉树学习笔记"
 * - "数据结构算法复习"
 * - "递归问题解答"
 * - "Java面试题讨论"
 */
@Data
public class RenameSessionRequest {
    
    /**
     * 新的会话标题（必填）
     * 建议使用有意义的名称，方便以后回顾对话内容
     * 示例：“二叉树学习笔记”、“算法面试准备”
     */
    @NotBlank(message = "标题不能为空")
    private String title;
}
