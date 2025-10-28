package com.entity.view;

import com.entity.DiscusslaoshixinxiEntity;

import com.baomidou.mybatisplus.annotations.TableName;
import org.apache.commons.beanutils.BeanUtils;
import java.lang.reflect.InvocationTargetException;

import java.io.Serializable;
 


@TableName("discusslaoshixinxi")
public class DiscusslaoshixinxiView  extends DiscusslaoshixinxiEntity implements Serializable {
	private static final long serialVersionUID = 1L;


 
 	public DiscusslaoshixinxiView(DiscusslaoshixinxiEntity discusslaoshixinxiEntity){
 	try {
			BeanUtils.copyProperties(this, discusslaoshixinxiEntity);
		} catch (IllegalAccessException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 		
	}
}
