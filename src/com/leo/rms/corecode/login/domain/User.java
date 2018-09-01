package com.leo.rms.corecode.login.domain;

import java.util.List;


public class User {
	
	private Integer user_id;
	
	private Integer role_type;
	
	private String cn_name;
	
	private String phone;
	
	private String login_name;
	
	private List<String> role_code;
	
	private List<String> r_ype;
	
	private List<Menu> menus;
	
	
	private Integer school_id;

	public Integer getUser_id() {
		return user_id;
	}

	public void setUser_id(Integer user_id) {
		this.user_id = user_id;
	}

	public String getCn_name() {
		return cn_name;
	}

	public void setCn_name(String cn_name) {
		this.cn_name = cn_name;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getLogin_name() {
		return login_name;
	}

	public void setLogin_name(String login_name) {
		this.login_name = login_name;
	}


	public List<String> getR_ype() {
		return r_ype;
	}

	public void setR_ype(List<String> r_ype) {
		this.r_ype = r_ype;
	}

	public List<String> getRole_code() {
		return role_code;
	}

	public void setRole_code(List<String> role_code) {
		this.role_code = role_code;
	}

	public Integer getRole_type() {
		return role_type;
	}

	public void setRole_type(Integer role_type) {
		this.role_type = role_type;
	}

	public Integer getSchool_id() {
		return school_id;
	}

	public void setSchool_id(Integer school_id) {
		this.school_id = school_id;
	}

	public List<Menu> getMenus() {
		return menus;
	}

	public void setMenus(List<Menu> menus) {
		this.menus = menus;
	}
	
}
