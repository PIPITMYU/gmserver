package com.leo.rms.utils;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.view.AbstractView;


/**
 * 
 *@company：ktmt
 *@package com.meif.rosis.utils
 *@title 文件名：ArrayJsonView.java
 *@author (作者): zhoucong 
 *@date 日期：2014年1月6日下午1:41:07
 *@version (版本信息)： Copyright 2014 版权所有
 */
public class ArrayJsonView extends AbstractView {

	protected void renderMergedOutputModel(Map<String, Object> map,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		Collection<?> collect = map.values();
		//只要一个LIST
		if( collect.size() != 1 ) return ;
		List<?> list = (List<?>) collect.toArray()[0];
		PrintWriter out = response.getWriter();
		out.append( JsonSpread.toJSONString( list.toArray()) );
	}

}
