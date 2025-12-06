package com.qasystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DocRewriteRequest - 文档AI改写/降重请求数据传输对象
 * 
 * 🎯 作用：学生使用AI对文档进行改写和降重时使用
 * 就像学生写论文时，将一段文字交给AI，AI会用不同的表达方式重写，降低重复率。
 * 
 * 📝 字段说明：
 * - documentId: 文档ID，用于统计和版本管理
 * - paragraphId: 段落ID，记录改写的具体段落
 * - text: 要改写的原文内容
 * - style: 改写风格，不同风格AI会用不同的方式表达
 * 
 * 💬 请求示例：
 * {
 *   "documentId": 100,
 *   "paragraphId": 25,
 *   "text": "人工智能是计算机科学的一个重要分支，它研究如何让计算机模拟人类的智能行为。",
 *   "style": "ACADEMIC"
 * }
 * 
 * 💡 改写风格说明：
 * - ACADEMIC: 学术风格，更正式、严谨，适合论文
 * - FLUENCY: 流畅性改写，使语句更通顺、易读
 * - EXPAND: 扩展式，增加详细描述，使内容更丰富
 * - LOGIC_ENHANCE: 逻辑增强，优化逻辑结构和论述条理
 * 
 * 💡 使用场景：
 * - 学生在文档编辑页面选中一段文字
 * - 点击“AI降重”按钮，选择改写风格
 * - 调用 POST /api/doc/rewrite 接口
 * - AI返回改写后的文字，学生可以选择是否采用
 */
@Data
public class DocRewriteRequest {

    /**
     * 文档ID（可选）
     * 用于统计该文档的AI降重次数，也方便版本管理
     */
    private Long documentId;

    /**
     * 段落ID（可选）
     * 记录改写的具体段落，便于追踪和对比
     */
    private Long paragraphId;

    /**
     * 待改写的原文内容（必填）
     * 学生需要降重的原始文字
     * 示例：“人工智能是计算机科学的一个分支...”
     */
    @NotBlank(message = "改写内容不能为空")
    private String text;

    /**
     * 降重改写风格（默认ACADEMIC）
     * - ACADEMIC: 学术风格，严谨正式，适合论文
     * - FLUENCY: 流畅性改写，增强可读性
     * - EXPAND: 扩展式，增加内容详尽度
     * - LOGIC_ENHANCE: 逻辑增强，优化条理结构
     */
    private String style = "ACADEMIC";
}
