package com.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.baomidou.mybatisplus.annotations.TableField;


/**
 * 互助区
 * 数据库通用操作实体类（普通增删改查）
 * @author 
 * @email 
 * @date
 */
@TableName("forum")
public class ForumEntity<T> implements Serializable {
	private static final long serialVersionUID = 1L;


	public ForumEntity() {
		
	}

	/**
	 * 主键id
	 */
	@TableId
	private Long id;
	/**
	 * 帖子标题
	 */
					
	private String title;
	
	/**
	 * 帖子内容
	 */
					
	private String content;
	
	/**
	 * 父节点id
	 */
					
	private Long parentid;
	
	/**
	 * 用户id
	 */
					
	private Long userid;
	
	/**
	 * 用户名
	 */
					
	private String username;
	
	/**
	 * 状态
	 */
					
	private String isdone;
	
	
	@JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat
	private Date addtime;

	public Date getAddtime() {
		return addtime;
	}
	public void setAddtime(Date addtime) {
		this.addtime = addtime;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	@TableField(exist = false)
	private List<ForumEntity> childs;
	
	public List<ForumEntity> getChilds() {
		return childs;
	}

	public void setChilds(List<ForumEntity> childs) {
		this.childs = childs;
	}
	/**
	 * 设置：帖子标题
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	/**
	 * 获取：帖子标题
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * 设置：帖子内容
	 */
	public void setContent(String content) {
		this.content = content;
	}
	/**
	 * 获取：帖子内容
	 */
	public String getContent() {
		return content;
	}
	/**
	 * 设置：父节点id
	 */
	public void setParentid(Long parentid) {
		this.parentid = parentid;
	}
	/**
	 * 获取：父节点id
	 */
	public Long getParentid() {
		return parentid;
	}
	/**
	 * 设置：用户id
	 */
	public void setUserid(Long userid) {
		this.userid = userid;
	}
	/**
	 * 获取：用户id
	 */
	public Long getUserid() {
		return userid;
	}
	/**
	 * 设置：用户名
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	/**
	 * 获取：用户名
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * 设置：状态
	 */
	public void setIsdone(String isdone) {
		this.isdone = isdone;
	}
	/**
	 * 获取：状态
	 */
	public String getIsdone() {
		return isdone;
	}

}
