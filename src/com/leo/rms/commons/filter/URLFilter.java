package com.leo.rms.commons.filter;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

public class URLFilter implements Filter{

private String[] includeUrls;
	
	private String[] excludeUrls;
	
	private String noLoginPage;
	
	@Override
	public void destroy() { }

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse) res;
		
		if (hasInIncludes(request)) {
			if (hasLogin(request)) {
				chain.doFilter( req,  res );
			}else{
				if (hasInExcludes(request)) {
					chain.doFilter( req,  res );
				}else{
					response.setContentType("text/html;charset=utf-8");
					PrintWriter out = response.getWriter();
					out.println("<script language='javascript' type='text/javascript'>");
					out.println("window.location.href='" + request.getContextPath() + noLoginPage+ "?_="+System.currentTimeMillis()+ " ' " );
					out.println("</script>");
				}
			}
		}else{
			response.setContentType("text/html;charset=utf-8");
			PrintWriter out = response.getWriter();
			out.println("<script language='javascript' type='text/javascript'>");
			out.println("window.location.href='" + request.getContextPath() + noLoginPage+ "?_t="+System.currentTimeMillis()+ " ' " );
			out.println("</script>");
		}
		
	}

	private boolean hasLogin(HttpServletRequest request) {
		return false;
	}
	
	private boolean hasInExcludes(HttpServletRequest request) {
		String url = getRequestUrl( request );
		if (excludeUrls != null) {
			for (String str: excludeUrls) {
				if (url.matches(str)) {
					return true;
				}
			}
		}
		return false;
	}

	
	private boolean hasInIncludes(HttpServletRequest request) {
		String url = getRequestUrl(	request	);
		if (includeUrls != null) {
			for (String str: includeUrls) {
				if (url.matches(str)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private String getRequestUrl(HttpServletRequest request) {
		String contextPath = request.getContextPath(),
				 requestUri = request.getRequestURI(),
				 requestUrl = StringUtils.replace(requestUri, contextPath, "", 1);
		return requestUrl;
	}
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		
		String includeUrls = filterConfig.getInitParameter("includeUrls");
		this.includeUrls = toArray( includeUrls );
		String excludeUrls = filterConfig.getInitParameter("excludeUrls");
		this.excludeUrls = toArray( excludeUrls );
		this.noLoginPage = filterConfig.getInitParameter("noLoginPage");
		
	}

	private String[] toArray(String str) {
		if (str == null) {
			return null;
		}
		String[] temp = str.split(",");
		String[] ary = new String[temp.length];
		int i = 0;
		for (String s: temp) {
			s = s.replace("\n", " ");
			ary[i++] = s.trim();
		}
		return ary;
	}
}
