package com.entity.view;

import com.entity.KemuleixingEntity;

import com.baomidou.mybatisplus.annotations.TableName;
import org.apache.commons.beanutils.BeanUtils;
import java.lang.reflect.InvocationTargetException;

import java.io.Serializable;
 

/**
 * 科目类型
 * 后端返回视图实体辅助类   
 * （通常后端关联的表或者自定义的字段需要返回使用）
 * @author 
 * @email 
 * @date
 */
@TableName("kemuleixing")
public class KemuleixingView  extends KemuleixingEntity implements Serializable {
	private static final long serialVersionUID = 1L;


 
 	public KemuleixingView(KemuleixingEntity kemuleixingEntity){
 	try {
			BeanUtils.copyProperties(this, kemuleixingEntity);
		} catch (IllegalAccessException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 		
	}
}
