package com.leo.rms.commons.base;

import java.io.Serializable;
import java.util.List;

public class BaseTree implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private Integer id;
	private String text;
	private String icon;
	private String url;
	private Integer type;
	
	private List<BaseTree> subMenu;
	
	private Integer user_id;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public List<BaseTree> getSubMenu() {
		return subMenu;
	}
	public void setSubMenu(List<BaseTree> subMenu) {
		this.subMenu = subMenu;
	}
	public Integer getUser_id() {
		return user_id;
	}
	public void setUser_id(Integer user_id) {
		this.user_id = user_id;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
}
