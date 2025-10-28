package com.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import com.utils.ValidatorUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.annotation.IgnoreAuth;

import com.entity.XueshengwentiEntity;
import com.entity.view.XueshengwentiView;

import com.service.XueshengwentiService;
import com.service.TokenService;
import com.utils.PageUtils;
import com.utils.R;
import com.utils.MD5Util;
import com.utils.MPUtil;
import com.utils.CommonUtil;


/**
 * 科目问题
 * 后端接口
 * @author 
 * @email 
 * @date
 */
@RestController
@RequestMapping("/xueshengwenti")
public class XueshengwentiController {
    @Autowired
    private XueshengwentiService xueshengwentiService;
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,XueshengwentiEntity xueshengwenti,
		HttpServletRequest request){
		String tableName = request.getSession().getAttribute("tableName").toString();
		if(tableName.equals("xuesheng")) {
			xueshengwenti.setXueshengzhanghao((String)request.getSession().getAttribute("username"));
		}
        EntityWrapper<XueshengwentiEntity> ew = new EntityWrapper<XueshengwentiEntity>();
		PageUtils page = xueshengwentiService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, xueshengwenti), params), params));
        return R.ok().put("data", page);
    }
    
    /**
     * 前端列表
     */
	@IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,XueshengwentiEntity xueshengwenti, HttpServletRequest request){
        EntityWrapper<XueshengwentiEntity> ew = new EntityWrapper<XueshengwentiEntity>();
		PageUtils page = xueshengwentiService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, xueshengwenti), params), params));
        return R.ok().put("data", page);
    }

	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( XueshengwentiEntity xueshengwenti){
       	EntityWrapper<XueshengwentiEntity> ew = new EntityWrapper<XueshengwentiEntity>();
      	ew.allEq(MPUtil.allEQMapPre( xueshengwenti, "xueshengwenti")); 
        return R.ok().put("data", xueshengwentiService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(XueshengwentiEntity xueshengwenti){
        EntityWrapper< XueshengwentiEntity> ew = new EntityWrapper< XueshengwentiEntity>();
 		ew.allEq(MPUtil.allEQMapPre( xueshengwenti, "xueshengwenti")); 
		XueshengwentiView xueshengwentiView =  xueshengwentiService.selectView(ew);
		return R.ok("查询科目问题成功").put("data", xueshengwentiView);
    }
	
    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        XueshengwentiEntity xueshengwenti = xueshengwentiService.selectById(id);
		xueshengwenti.setClicknum(xueshengwenti.getClicknum()+1);
		xueshengwenti.setClicktime(new Date());
		xueshengwentiService.updateById(xueshengwenti);
        return R.ok().put("data", xueshengwenti);
    }

    /**
     * 前端详情
     */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        XueshengwentiEntity xueshengwenti = xueshengwentiService.selectById(id);
		xueshengwenti.setClicknum(xueshengwenti.getClicknum()+1);
		xueshengwenti.setClicktime(new Date());
		xueshengwentiService.updateById(xueshengwenti);
        return R.ok().put("data", xueshengwenti);
    }
    


    /**
     * 赞或踩
     */
    @RequestMapping("/thumbsup/{id}")
    public R vote(@PathVariable("id") String id,String type){
        XueshengwentiEntity xueshengwenti = xueshengwentiService.selectById(id);
        if(type.equals("1")) {
        	xueshengwenti.setThumbsupnum(xueshengwenti.getThumbsupnum()+1);
        } else {
        	xueshengwenti.setCrazilynum(xueshengwenti.getCrazilynum()+1);
        }
        xueshengwentiService.updateById(xueshengwenti);
        return R.ok("投票成功");
    }

    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody XueshengwentiEntity xueshengwenti, HttpServletRequest request){
    	xueshengwenti.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());
    	//ValidatorUtils.validateEntity(xueshengwenti);
        xueshengwentiService.insert(xueshengwenti);
        return R.ok();
    }
    
    /**
     * 前端保存
     */
	@IgnoreAuth
    @RequestMapping("/add")
    public R add(@RequestBody XueshengwentiEntity xueshengwenti, HttpServletRequest request){
    	xueshengwenti.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());
    	//ValidatorUtils.validateEntity(xueshengwenti);
        xueshengwentiService.insert(xueshengwenti);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody XueshengwentiEntity xueshengwenti, HttpServletRequest request){
        //ValidatorUtils.validateEntity(xueshengwenti);
        xueshengwentiService.updateById(xueshengwenti);//全部更新
        return R.ok();
    }
    

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        xueshengwentiService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }
    
    /**
     * 提醒接口
     */
	@RequestMapping("/remind/{columnName}/{type}")
	public R remindCount(@PathVariable("columnName") String columnName, HttpServletRequest request, 
						 @PathVariable("type") String type,@RequestParam Map<String, Object> map) {
		map.put("column", columnName);
		map.put("type", type);
		
		if(type.equals("2")) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Calendar c = Calendar.getInstance();
			Date remindStartDate = null;
			Date remindEndDate = null;
			if(map.get("remindstart")!=null) {
				Integer remindStart = Integer.parseInt(map.get("remindstart").toString());
				c.setTime(new Date()); 
				c.add(Calendar.DAY_OF_MONTH,remindStart);
				remindStartDate = c.getTime();
				map.put("remindstart", sdf.format(remindStartDate));
			}
			if(map.get("remindend")!=null) {
				Integer remindEnd = Integer.parseInt(map.get("remindend").toString());
				c.setTime(new Date());
				c.add(Calendar.DAY_OF_MONTH,remindEnd);
				remindEndDate = c.getTime();
				map.put("remindend", sdf.format(remindEndDate));
			}
		}
		
		Wrapper<XueshengwentiEntity> wrapper = new EntityWrapper<XueshengwentiEntity>();
		if(map.get("remindstart")!=null) {
			wrapper.ge(columnName, map.get("remindstart"));
		}
		if(map.get("remindend")!=null) {
			wrapper.le(columnName, map.get("remindend"));
		}

		String tableName = request.getSession().getAttribute("tableName").toString();
		if(tableName.equals("xuesheng")) {
			wrapper.eq("xueshengzhanghao", (String)request.getSession().getAttribute("username"));
		}

		int count = xueshengwentiService.selectCount(wrapper);
		return R.ok().put("count", count);
	}
	
	/**
     * 前端智能排序
     */@IgnoreAuth
	@RequestMapping("/autoSort")
	public R autoSort(@RequestParam Map<String, Object> params, XueshengwentiEntity xueshengwenti, HttpServletRequest request, String pre) {
		// 使用 EntityWrapper 包装查询条件
		EntityWrapper<XueshengwentiEntity> ew = new EntityWrapper<XueshengwentiEntity>();

		// 直接使用 params 来存储新的参数，避免不必要的 newMap 和 param 处理
		if (pre == null) {
			pre = ""; // 如果 pre 为 null，设置为空字符串，避免后续拼接错误
		}

		// 将 pre 添加到 params 中，处理前缀拼接
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			String key = entry.getKey();
			String newKey = key;

			if (pre.endsWith(".")) {
				params.put(pre + newKey, entry.getValue());
			} else if (StringUtils.isEmpty(pre)) {
				params.put(newKey, entry.getValue());
			} else {
				params.put(pre + "." + newKey, entry.getValue());
			}
		}

		// 设置默认排序参数
		params.put("sort", "clicknum");
		params.put("order", "desc");

		// 进行分页查询并返回结果，优化查询条件
		PageUtils page = xueshengwentiService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, xueshengwenti), params), params));

		// 返回查询结果
		return R.ok().put("data", page);
	}
}
