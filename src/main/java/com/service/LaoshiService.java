package com.service;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.IService;
import com.utils.PageUtils;
import com.entity.LashingEntity;
import java.util.List;
import java.util.Map;
import com.entity.vo.LaoshiVO;
import org.apache.ibatis.annotations.Param;
import com.entity.view.LashingView;


/**
 * 老师
 *
 * @author 
 * @email 
 * @date
 */
public interface LaoshiService extends IService<LashingEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<LaoshiVO> selectListVO(Wrapper<LashingEntity> wrapper);
   	
   	LaoshiVO selectVO(@Param("ew") Wrapper<LashingEntity> wrapper);
   	
   	List<LashingView> selectListView(Wrapper<LashingEntity> wrapper);
   	
   	LashingView selectView(@Param("ew") Wrapper<LashingEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<LashingEntity> wrapper);
   	
}

