package com.leo.rms.commons.constant;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Constants implements Serializable{
	

	/**
	 * session user key 
	 */
	public final static String USER_SESSION_INFO = "userSessionInfo";
	
	/**
	 * session appuser key 
	 */
	public final static String APP_USER_SESSION_INFO = "appuserSessionInfo";
	/**
	 * session current menu key 
	 */
	public final static String CURRENT_USER_MENU_SESSION_INFO = "C_U_M_S_I";
	
	/**
	 * session verification code key
	 */
	public final static String VALIDATE_CODE_KEY = "S_V_C_K";
	
	/**
	 * system default initialize password
	 */
	public final static String INIT_PASSWORD = "111111";
	
	/**
	 * system gender constants
	 * 
	 */
	public final static String GENDER_MAN = "1";
	public final static String GENDER_WOMAN = "2";
	public final static String GENDER_UNKNOW = "9";
	
	/**
	 * 时间格式
	 */
	public static final String DATE_TIME_TYPE1_24H = "yyyy-MM-dd HH:mm:ss";
	public static final String DATE_TYPE3 = "yyyy年MM月dd日";
	public static final String DATE_TYPE2 = "yyyy.MM.dd";
	public static final String TIME_TYPE1 = "HH:mm:ss";
	
	/*  system status constants  */
	/**
	 * 未激活
	 */
	public static final String SYS_STATUS_0 = "0";
	/**
	 * 激活
	 */
	public static final String SYS_STATUS_1 = "1";
	/**
	 * 注销
	 */
	public static final String SYS_STATUS_2 = "2";
	
	
	/* 数据 权限  常量 */
	/**
	 * 个人权限
	 */
	public static final Integer IS_PERSONAL_ROLE = 0; 
	/**
	 * 部门权限
	 */
	public static final Integer IS_DEPT_ROLE = 1; 
	/**
	 * 管理员 权限
	 */
	public static final Integer IS_SYSTEM_ROLE = 2; 
	
	/* 操作 权限  常量 */
	/**
	 * 管理员
	 */
	public static final Integer IS_SYSTEM_ADMIN_ROLE = 1; 
	/**
	 * 普通用户
	 */
	public static final Integer IS_PERSONAL_COMMON_ROLE = 0; 
	
	
}
