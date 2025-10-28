package com.service;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.IService;
import com.utils.PageUtils;
import com.entity.DiscussxueshengwentiEntity;
import java.util.List;
import java.util.Map;
import com.entity.vo.DiscussxueshengwentiVO;
import org.apache.ibatis.annotations.Param;
import com.entity.view.DiscussxueshengwentiView;


/**
 * 问题评论表
 *
 * @author 
 * @email 
 * @date
 */
public interface DiscussxueshengwentiService extends IService<DiscussxueshengwentiEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<DiscussxueshengwentiVO> selectListVO(Wrapper<DiscussxueshengwentiEntity> wrapper);
   	
   	DiscussxueshengwentiVO selectVO(@Param("ew") Wrapper<DiscussxueshengwentiEntity> wrapper);
   	
   	List<DiscussxueshengwentiView> selectListView(Wrapper<DiscussxueshengwentiEntity> wrapper);
   	
   	DiscussxueshengwentiView selectView(@Param("ew") Wrapper<DiscussxueshengwentiEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<DiscussxueshengwentiEntity> wrapper);
   	
}

