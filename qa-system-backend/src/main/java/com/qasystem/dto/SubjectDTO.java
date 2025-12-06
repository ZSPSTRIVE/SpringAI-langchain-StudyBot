package com.qasystem.dto;

import lombok.Data;

/**
 * SubjectDTO - 科目数据传输对象
 * 
 * 🎯 作用：返回给前端的科目信息
 * 用于在科目列表、提问页面选择科目等场景。
 * 科目就像"课程分类"，帮助组织问题，方便教师筛选擅长的科目。
 * 
 * 📝 字段说明：
 * - id: 科目ID
 * - name: 科目名称，如"高等数学"、"数据结构"
 * - code: 科目代码，唯一标识符
 * - description: 科目描述
 * - icon: 科目图标URL
 * - sortOrder: 排序顺序，数值越小越靠前
 * - status: 科目状态（ACTIVE/INACTIVE）
 * 
 * 💬 响应示例：
 * {
 *   "id": 1,
 *   "name": "高等数学",
 *   "code": "MATH101",
 *   "description": "包括微积分、线性代数等内容",
 *   "icon": "/uploads/subject/math.png",
 *   "sortOrder": 1,
 *   "status": "ACTIVE"
 * }
 * 
 * 💡 使用场景：
 * - 学生提问时选择科目，从科目下拉列表中选择
 * - 教师设置擅长科目，可以多选
 * - 首页展示科目分类，点击科目查看该科目下的所有问题
 */
@Data
public class SubjectDTO {
    /** 科目ID */
    private Long id;
    
    /** 科目名称，如"高等数学"、"数据结构"、"算法设计" */
    private String name;
    
    /** 科目代码，唯一标识，如"MATH101"、"CS201" */
    private String code;
    
    /** 科目描述，简要介绍科目内容 */
    private String description;
    
    /** 科目图标URL，用于前端显示 */
    private String icon;
    
    /** 排序顺序，数值越小越靠前，用于控制列表中的显示顺序 */
    private Integer sortOrder;
    
    /** 
     * 科目状态
     * ACTIVE - 启用，学生可以选择这个科目提问
     * INACTIVE - 停用，不再显示在科目列表中
     */
    private String status;
}

