package com.leo.rms.commons.base;

import java.io.Serializable;

public class BaseForm implements Serializable{

	
	 /**
	 *
	 *@field serialVersionUID:TODO(description here)
	 *@author (作者): zhoucong(16953767@qq.com)
	 *
	 */
	 
	private static final long serialVersionUID = 1L;
	
	private Integer id;
	private String name;
	
	private Integer start ;
	private Integer limit ;
	
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getStart() {
		return start;
	}
	public void setStart(Integer start) {
		this.start = start;
	}
	public Integer getLimit() {
		return limit;
	}
	public void setLimit(Integer limit) {
		this.limit = limit;
	}
}
