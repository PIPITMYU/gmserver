package com.leo.rms.utils;

import java.util.UUID;

/**
 * 
 *@company：ktmt
 *@package com.bjmf.srd.utils
 *@title 文件名：UUIDUtil.java
 *@author (作者): zhoucong 
 *@date 日期：2013年12月28日下午4:52:07
 *@version (版本信息)： Copyright 2013 版权所有
 */
public class UUIDUtil {
	
	/**
	 * 
	 *@author (作者): zhoucong 
	 *@date 日期： 2013年12月28日下午4:52:37.
	 *@method:getUuidKey
	 *@description 此方法描述的是：UUID
	 */
	public static String getUuidKey() {
		return UUID.randomUUID().toString();
	}
	
	/**
     * 获得一个去掉"-"符号的UUID
     * 
     * @return
     */
    public static String getUuid() {
        String s = UUID.randomUUID().toString();
        // 去掉"-"符号
        return s.replace("-", "");
    }
	
	

}
