package com.leo.rms.commons.base;

import java.io.Serializable;

/**
 * 下拉框
 *@company：ktmt
 *@package com.leo.rms.commons.base
 *@title 文件名：Combo.java
 *@author (作者): lixr
 *@date 日期：2015年7月9日上午11:22:01
 *@version (版本信息)： Copyright 2015 版权所有
 */
@SuppressWarnings("serial")
public class Combo implements Serializable {
	private Integer id;
	private String sid;
	private String name;
	private Integer pid;
	private String spid;
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
	public Integer getPid() {
		return pid;
	}
	public void setPid(Integer pid) {
		this.pid = pid;
	}
	public String getSid() {
		return sid;
	}
	public void setSid(String sid) {
		this.sid = sid;
	}
	public String getSpid() {
		return spid;
	}
	public void setSpid(String spid) {
		this.spid = spid;
	}
	
	
	
}
