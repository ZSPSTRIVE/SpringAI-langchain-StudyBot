package com.qasystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qasystem.entity.Emoji;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 表情Mapper
 */
@Mapper
public interface EmojiMapper extends BaseMapper<Emoji> {

    /**
     * 获取表情包下的表情
     */
    default List<Emoji> findByPackId(Long packId) {
        return selectList(new LambdaQueryWrapper<Emoji>()
                .eq(Emoji::getPackId, packId)
                .orderByAsc(Emoji::getSortOrder));
    }

    /**
     * 根据表情代码查找
     */
    default Emoji findByCode(String code) {
        return selectOne(new LambdaQueryWrapper<Emoji>()
                .eq(Emoji::getCode, code));
    }

    /**
     * 获取热门表情
     */
    default List<Emoji> findPopular(int limit) {
        return selectList(new LambdaQueryWrapper<Emoji>()
                .orderByDesc(Emoji::getUseCount)
                .last("LIMIT " + limit));
    }
}
