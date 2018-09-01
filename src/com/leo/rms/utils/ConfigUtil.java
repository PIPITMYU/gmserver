package com.leo.rms.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * 
 *@company：ktmt
 *@package com.meif.rosis.utils
 *@title 文件名：ConfigUtil.java
 *@author (作者): zhoucong 
 *@date 日期：2014年1月6日下午3:08:54
 *@version (版本信息)： Copyright 2014 版权所有
 */
public class ConfigUtil {
	private static Properties props = new Properties();
	private static String[] p = new String[]{"../properties/config.properties"/*,
															"../properties/ftp.properties",
															"../properties/constant.properties",
															"../properties/system.properties"*/};
	static {
		// 获得加载ConfigUtil的类加载器(ClassLoader)。
		// 类加载器负责查找.class文件，将其转换成
		// 对应的class对象，存放在方法区。
		//ClassLoader loader = ConfigUtil.class.getClassLoader();
		// 类加载器可以依据classpath去搜索资源文件,转换成一个
		// 输入流。
		InputStream in = null;
		//InputStream in = loader.getResourceAsStream("url.properties");
		for (int i = 0; i < p.length; i++) {
			 in =  ConfigUtil.class.getResourceAsStream( p[i] );
			 try {
				 if(in!= null){
					 props.load(in);
				 }
			 } catch (IOException e) {
				 e.printStackTrace();
			 }
		}
	}
	
	/**
	 * 
	 *@author (作者): zhoucong 
	 *@date 日期： 2014年1月6日下午3:09:11.
	 *@method:getPropertyValueByKey
	 *@description 此方法描述的是：
	 */
	public static String getPropertyValueByKey(String key) {
		return props.getProperty( key );
	}
	/**
	 * 
	 *@description 此方法描述的是：
	 *@author mf-zhoucong 
	 *@version 2014年4月8日下午4:07:12.
	 */
	public static Properties getProperties(String fileName){
		InputStream is=null;
		try{
			is=ConfigUtil.class.getResourceAsStream(fileName);
		}catch(Exception e){
			throw new RuntimeException("找不到相应的文件！"+fileName);
		}
		
		Properties properties=new Properties();
		try {
			properties.load(is);
			if(is!=null)is.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return properties;
	}

	public static void main(String[] args) {
//		ConfigUtil c = new ConfigUtil();
//		System.out.println( c.getPropertyValueByKey("picture.downloadpath.activity") );
//		System.out.println( c.getPropertyValueByKey("ftp.picture.service") );
//		System.out.println( c.getPropertyValueByKey("sqlserver.jdbc.driverClassName") );
//		System.out.println( c.getPropertyValueByKey("circle.member.count.level.1") );
//		Properties properties=ConfigUtil.getProperties("../properties/db.properties");
//		System.out.println(properties.get("DB_DRIVER"));
	}
	
}
