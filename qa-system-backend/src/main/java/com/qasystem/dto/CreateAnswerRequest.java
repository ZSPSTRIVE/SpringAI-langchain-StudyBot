package com.qasystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * CreateAnswerRequest - 创建回答请求数据传输对象
 * 
 * 🎯 作用：教师回答问题时从前端接收的数据
 * 就像教师在答疑平台填写一张"回答表单"，包含要回答的问题ID、回答内容和配图。
 * 
 * 📝 字段说明：
 * - questionId: 问题ID（必填），表示要回答哪个问题
 * - content: 回答内容（必填），详细的答案，可以包含Markdown格式
 * - images: 图片URL列表（可选），教师可以上传解题步骤图、代码示例图等
 * 
 * 💬 请求示例：
 * {
 *   "questionId": 123,
 *   "content": "你的问题是对递归理解不够深入。递归的核心是...",
 *   "images": ["/uploads/answer/2024/11/solution.jpg"]
 * }
 * 
 * 💡 使用场景：
 * - 教师在"问题详情"页面填写回答后点击"提交回答"
 * - 前端会调用 POST /api/answers 接口，传递此对象
 * - 后端接收后创建Answer实体并保存，同时更新问题的回答数
 * - 学生会收到"您的问题有新回答"的通知
 */
@Data
public class CreateAnswerRequest {

    /**
     * 问题ID（必填）
     * 指定要回答的问题，前端从问题详情页获取
     * 示例：123, 456
     */
    @NotNull(message = "问题ID不能为空")
    private Long questionId;

    /**
     * 回答内容（必填）
     * 教师的详细回答，支持Markdown格式
     * 建议包含：问题分析、解题思路、详细步骤、注意事项
     * 示例："这个问题的关键在于理解递归的三要素：1.递归终止条件..."
     */
    @NotBlank(message = "回答内容不能为空")
    private String content;

    /**
     * 回答配图列表（可选）
     * 存储图片的URL地址，如解题步骤图、代码示例、示意图等
     * 示例：["/uploads/answer/2024/11/step1.png", "/uploads/answer/2024/11/step2.png"]
     * 注意：图片需要先通过文件上传接口上传，获得URL后再填入这里
     */
    private List<String> images;
}

