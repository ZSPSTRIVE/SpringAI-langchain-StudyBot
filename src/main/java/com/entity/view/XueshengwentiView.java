package com.entity.view;

import com.entity.XueshengwentiEntity;

import com.baomidou.mybatisplus.annotations.TableName;
import org.apache.commons.beanutils.BeanUtils;
import java.lang.reflect.InvocationTargetException;

import java.io.Serializable;
 

/**
 * 科目问题
 * 后端返回视图实体辅助类   
 * （通常后端关联的表或者自定义的字段需要返回使用）
 * @author 
 * @email 
 * @date
 */
@TableName("xueshengwenti")
public class XueshengwentiView  extends XueshengwentiEntity implements Serializable {
	private static final long serialVersionUID = 1L;


 
 	public XueshengwentiView(XueshengwentiEntity xueshengwentiEntity){
 	try {
			BeanUtils.copyProperties(this, xueshengwentiEntity);
		} catch (IllegalAccessException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 		
	}
}
