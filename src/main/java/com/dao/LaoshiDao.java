package com.dao;

import com.entity.LashingEntity;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import java.util.List;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;

import org.apache.ibatis.annotations.Param;
import com.entity.vo.LaoshiVO;
import com.entity.view.LashingView;


/**
 * 老师
 * 
 * @author 
 * @email 
 * @date
 */
public interface LaoshiDao extends BaseMapper<LashingEntity> {
	
	List<LaoshiVO> selectListVO(@Param("ew") Wrapper<LashingEntity> wrapper);
	
	LaoshiVO selectVO(@Param("ew") Wrapper<LashingEntity> wrapper);
	
	List<LashingView> selectListView(@Param("ew") Wrapper<LashingEntity> wrapper);

	List<LashingView> selectListView(Pagination page, @Param("ew") Wrapper<LashingEntity> wrapper);
	
	LashingView selectView(@Param("ew") Wrapper<LashingEntity> wrapper);
	
}
