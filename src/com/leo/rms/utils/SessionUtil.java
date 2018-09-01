package com.leo.rms.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.leo.rms.commons.constant.Constants;
import com.leo.rms.corecode.login.domain.User;

public class SessionUtil {
	

	/**
	 * 
	 *@author (作者): zhoucong 
	 *@date 日期： 2014年1月6日下午6:21:18.
	 *@method:setCurrentUser
	 *@description 此方法描述的是：向 session中添加 User
	 */
	public static void setCurrentUser(HttpServletRequest request, 
			HttpServletResponse response, User user) {
		request.getSession().setAttribute(Constants.USER_SESSION_INFO, user);
		HttpSession sess = request.getSession();
		Cookie cookie = new Cookie("JSESSIONID", sess.getId());
		cookie.setMaxAge(sess.getMaxInactiveInterval());
		cookie.setPath("/");
		response.addCookie(cookie);
	}
	
	
	/**
	 * 
	 *@description 此方法描述的是：想session 添加临时属性
	 *@author mf-zhoucong 
	 *@version 2014年12月6日上午12:12:43.
	 *@param [String] key 
	 *@param [String] value 
	 */
	public static void setCurrentTemp(HttpServletRequest request, 
			HttpServletResponse response, String key, Object value ) {
		request.getSession().setAttribute( key , value);
		HttpSession sess = request.getSession();
		Cookie cookie = new Cookie("JSESSIONID", sess.getId());
		cookie.setMaxAge(sess.getMaxInactiveInterval());
		cookie.setPath("/");
		response.addCookie(cookie);
	}
	
	/**
	 * 
	 *@author (作者): zhoucong 
	 *@date 日期： 2014年1月6日下午6:18:29.
	 *@method:getCurrenUser
	 *@description 此方法描述的是：获取 session User
	 */
	public static User getCurrenUser(HttpServletRequest request){
		Object o  = request.getSession().getAttribute(Constants.USER_SESSION_INFO);
		return o == null ? null : (User) o;
	}
	
	
	/**
	 * 
	 * @param request
	 * @param key
	 * @return
	 */
	public static Object getCurrenTemp(HttpServletRequest request, String key){
		Object obj  = request.getSession().getAttribute( key );
		return obj;
	}
	
	/**
	 * 
	 *@author (作者): zhoucong 
	 *@date 日期： 2014年1月6日下午6:44:29.
	 *@method:getCurrenUserSysId
	 *@description 此方法描述的是：获得用户  user表ID
	 */
//	public static Integer getCurrenUserSysId(HttpServletRequest request){
//		User user = getCurrenUser( request );
//		if( user == null ){
//			return null;
//		}
//		return user.getId();
//	}
	
	/**
	 * 
	 *@author (作者): zhoucong 
	 *@date 日期： 2014年1月6日下午6:45:31.
	 *@method:getCurrenUserEmpId
	 *@description 此方法描述的是：获得用户  employee表ID
	 */
//	public static Integer getCurrenUserEmpId(HttpServletRequest request){
//		User user = getCurrenUser( request );
//		if( user == null ){
//			return null;
//		}
//		return user.getEmp_id();
//	}
	
	/**
	 * 
	 *@author (作者): zhoucong 
	 *@date 日期： 2014年1月6日下午6:47:47.
	 *@method:getCurrenUserTrueName
	 *@description 此方法描述的是：获得 当前用户的  真实姓名
	 */
//	public static String getCurrenUserTrueName(HttpServletRequest request){
//		User user = getCurrenUser( request );
//		if( user == null ){
//			return null;
//		}
//		return user.getName();
//	}
	
	/**
	 * 
	 *@author (作者): zhoucong 
	 *@date 日期： 2013年12月26日下午4:16:52.
	 *@method:clearCurrentUser
	 *@description 此方法描述的是：删除session 中当前用户 并且清空session
	 */
	public static void clearCurrentUser( HttpServletRequest request ,HttpServletResponse response ){
		request.getSession().invalidate(); 
		clear(request, response);
	}
	/**
	 * 
	 *@author (作者): zhoucong 
	 *@date 日期： 2014年1月6日下午6:16:24.
	 *@method:clear
	 *@description 此方法描述的是：清空 cookie
	 */
	public static void clear(HttpServletRequest req,HttpServletResponse res) {
		Cookie[] cookies = req.getCookies();
		if(cookies != null){
			for(int i = 0,len = cookies.length; i < len; i++) {
				Cookie cookie = new Cookie(cookies[i].getName(),null);
				cookie.setMaxAge(0);
				cookie.setPath("/");
				res.addCookie(cookie);
			}
		}
	}

	/**
	 * 
	 *@author (作者): zhoucong 
	 *@date 日期： 2014年1月6日下午6:15:44.
	 *@method:getIpAddr
	 *@description 此方法描述的是：获得当前用户  IP
	 */
	public static String getIpAddr( HttpServletRequest request ) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
	
}
