package com.qasystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * SaveDocVersionRequest - 文档版本保存请求数据传输对象
 * 
 * 🎯 作用：学生保存文档的某个版本时使用
 * 就像在Word中保存文件时可以选择“另存为”，系统会保留多个版本方便对比和恢复。
 * 
 * 📝 字段说明：
 * - content: 文档全文内容
 * - style: 使用的降重风格，记录这个版本是用哪种风格生成的
 * - remark: 版本备注，记录修改内容或目的
 * 
 * 💬 请求示例：
 * {
 *   "content": "这是整篇论文的内容，包含所有段落...",
 *   "style": "ACADEMIC",
 *   "remark": "第二版，修改了第3章的逻辑结构"
 * }
 * 
 * 💡 使用场景：
 * - 学生在文档编辑页面对多个段落AI降重后
 * - 点击“保存版本”按钮，填写备注
 * - 调用 POST /api/doc/{documentId}/version 接口
 * - 系统保存当前文档状态为一个版本
 * - 后续可以查看版本历史，比对不同版本或恢复到某个版本
 * 
 * 💡 版本管理作用：
 * 1. 备份：防止丢失或误删
 * 2. 对比：查看不同版本之间的差异
 * 3. 恢复：可以恢复到之前的任何版本
 * 4. 历史：记录文档的修改轨迹
 */
@Data
public class SaveDocVersionRequest {

    /**
     * 当前版本的全文内容（必填）
     * 可以是纯文本或JSON结构（包含段落、格式等信息）
     * 示例：“这是第一章内容...\n\n这是第二章内容...”
     */
    @NotBlank(message = "版本内容不能为空")
    private String content;

    /**
     * 降重风格（默认ACADEMIC）
     * 记录该版本使用的AI降重风格
     * ACADEMIC-学术风格, FLUENCY-流畅性, EXPAND-扩展式, LOGIC_ENHANCE-逻辑增强
     */
    private String style = "ACADEMIC";

    /**
     * 版本备注（可选）
     * 记录这个版本的修改内容、目的等
     * 示例：“初稿”、“修改了第3章逻辑”、“完成AI降重的版本”
     */
    private String remark;
}
