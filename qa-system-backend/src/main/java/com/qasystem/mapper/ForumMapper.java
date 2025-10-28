package com.qasystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qasystem.entity.Forum;
import org.apache.ibatis.annotations.Mapper;

/**
 * 论坛Mapper接口
 */
@Mapper
public interface ForumMapper extends BaseMapper<Forum> {
}

