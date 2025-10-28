package com.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.List;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.utils.PageUtils;
import com.utils.Query;


import com.dao.LaoshiDao;
import com.entity.LashingEntity;
import com.service.LaoshiService;
import com.entity.vo.LaoshiVO;
import com.entity.view.LashingView;

@Service("laoshiService")
public class LaoshiServiceImpl extends ServiceImpl<LaoshiDao, LashingEntity> implements LaoshiService {
	
	
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<LashingEntity> page = this.selectPage(
                new Query<LashingEntity>(params).getPage(),
                new EntityWrapper<LashingEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<LashingEntity> wrapper) {
		  Page<LashingView> page =new Query<LashingView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}
    
    @Override
	public List<LaoshiVO> selectListVO(Wrapper<LashingEntity> wrapper) {
 		return baseMapper.selectListVO(wrapper);
	}
	
	@Override
	public LaoshiVO selectVO(Wrapper<LashingEntity> wrapper) {
 		return baseMapper.selectVO(wrapper);
	}
	
	@Override
	public List<LashingView> selectListView(Wrapper<LashingEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public LashingView selectView(Wrapper<LashingEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}

}
