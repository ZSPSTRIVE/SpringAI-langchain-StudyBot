package com.qasystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qasystem.entity.Forum;
import org.apache.ibatis.annotations.Mapper;

/**
 * ForumMapper接口 - 论坛数据访问层
 * 
 * 🎯 作用说明：
 * 这个接口负责操作数据库中的forum表，用于兼容旧版本系统的论坛功能。
 * 就像一个"老系统数据库管理员"，保留着之前系统的论坛数据。
 * 
 * 📚 系统中的作用：
 * 1. 数据迁移：从旧系统论坛表读取数据
 * 2. 兼容性：保持与旧系统的数据兼容
 * 3. 备用表：如果需要回退到旧系统，数据仍然可用
 * 
 * 🏗️ 技术架构：
 * - 继承自MyBatis-Plus的BaseMapper<Forum>
 * - 只使用BaseMapper提供的基础方法，没有自定义方法
 * - 这是一个简单的论坛实体接口，用于数据兼容
 * 
 * 📊 对应数据库表: forum
 * 
 * 💡 使用场景：
 * 1. 数据迁移脚本：从forum表读取数据，转换到question/answer表
 * 2. 历史数据查询：查询旧系统中的论坛帖子
 * 3. 兼容性接口：为老用户提供访问历史数据的方法
 * 
 * ⚠️ 重要提示：
 * 1. 这是一个兼容性接口，不建议在新代码中直接使用
 * 2. 新系统应使用Question和Answer实体，而不是Forum
 * 3. 此接口主要用于数据迁移和历史数据访问
 * 4. 未来可能会逐步废弃这个表，当数据迁移完成后
 * 
 * @author QA System Team
 * @version 1.0
 */
@Mapper
public interface ForumMapper extends BaseMapper<Forum> {
    // 只使用BaseMapper提供的基础方法，没有自定义方法
    // 这个接口主要用于兼容旧系统的forum表
}

