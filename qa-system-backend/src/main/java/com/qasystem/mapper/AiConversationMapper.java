package com.qasystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qasystem.entity.AiConversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * AI对话Mapper
 */
@Mapper
public interface AiConversationMapper extends BaseMapper<AiConversation> {
    
    /**
     * 获取用户的会话历史（按时间倒序）
     */
    @Select("SELECT * FROM ai_conversation WHERE user_id = #{userId} AND session_id = #{sessionId} ORDER BY created_at ASC")
    List<AiConversation> getSessionHistory(Long userId, String sessionId);
    
    /**
     * 获取用户的所有会话列表
     */
    @Select("SELECT DISTINCT session_id, MAX(created_at) as created_at FROM ai_conversation WHERE user_id = #{userId} GROUP BY session_id ORDER BY created_at DESC LIMIT #{limit}")
    List<AiConversation> getUserSessions(Long userId, Integer limit);
}
