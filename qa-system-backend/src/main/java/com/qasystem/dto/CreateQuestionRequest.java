package com.qasystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * CreateQuestionRequest - 创建问题请求数据传输对象
 * 
 * 🎯 作用：学生提问时从前端接收的数据
 * 就像学生在答疑平台填写一张"提问表单"，包含问题的科目、标题、详细内容和配图。
 * 
 * 📝 字段说明：
 * - subjectId: 科目ID（必填），表示这个问题属于哪个科目，如"高等数学"、"数据结构"
 * - title: 问题标题（必填），简短描述问题，如"二叉树遍历算法不理解"
 * - content: 问题内容（必填），详细描述问题，可以包含Markdown格式
 * - images: 图片URL列表（可选），学生可以上传题目截图、错误截图等
 * 
 * 💬 请求示例：
 * {
 *   "subjectId": 1,
 *   "title": "二叉树的前序遍历怎么实现？",
 *   "content": "老师您好，我在学习二叉树时遇到问题，不理解前序遍历的递归实现...",
 *   "images": ["/uploads/question/2024/11/img1.jpg", "/uploads/question/2024/11/img2.jpg"]
 * }
 * 
 * 💡 使用场景：
 * - 学生在"提问"页面填写表单后点击"发布问题"
 * - 前端会调用 POST /api/questions 接口，传递此对象
 * - 后端接收后创建Question实体并保存到数据库
 */
@Data
public class CreateQuestionRequest {

    /**
     * 科目ID（必填）
     * 从科目列表中选择，如：1-高等数学、2-数据结构、3-算法设计
     * 用于将问题分类，方便教师筛选自己擅长的问题
     */
    @NotNull(message = "科目不能为空")
    private Long subjectId;

    /**
     * 问题标题（必填）
     * 简短概括问题，建议20-100字
     * 示例："如何理解递归的执行过程？"、"指针和引用的区别是什么？"
     */
    @NotBlank(message = "标题不能为空")
    private String title;

    /**
     * 问题详细内容（必填）
     * 详细描述问题，可以使用Markdown格式
     * 建议包含：问题背景、已尝试的方法、具体困惑点
     * 示例："我在写递归函数时总是理解不了返回值如何传递，比如这段代码..."
     */
    @NotBlank(message = "内容不能为空")
    private String content;

    /**
     * 问题配图列表（可选）
     * 存储图片的URL地址，如题目截图、代码截图、错误提示截图
     * 示例：["/uploads/question/2024/11/code.png", "/uploads/question/2024/11/error.png"]
     * 注意：图片需要先通过文件上传接口上传，获得URL后再填入这里
     */
    private List<String> images;
}

