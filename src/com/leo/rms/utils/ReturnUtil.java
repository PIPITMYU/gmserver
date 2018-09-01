package com.leo.rms.utils;

import java.util.List;
import java.util.Map;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

public class ReturnUtil {
	
	
	public static ModelAndView Json_ok(  ){
		ModelMap model = new ModelMap();
		model.put("result", 1);
		return new ModelAndView( new MappingJackson2JsonView(), model ); 
	}
	
	public static ModelAndView Json_ok( String msg ){
		ModelMap model = new ModelMap();
		model.put("result", 1);
		if( msg != null && "".equals( msg ) ){
			model.put("msg", msg);
		}
		return new ModelAndView( new MappingJackson2JsonView(), model ); 
	}
	
	public static ModelAndView Json_ok( String key , Object obj ){
		ModelMap model = new ModelMap();
		model.put("result", 1);
		if( obj != null  ){
			model.put(key, obj);
		}
		return new ModelAndView( new MappingJackson2JsonView(), model ); 
	}

	public static ModelAndView Json_ok( String key , List<?> list ){
		ModelMap model = new ModelMap();
		model.put("result", 1);
		model.put( key, list);
		return new ModelAndView( new MappingJackson2JsonView(), model);
	}
	
	public static ModelAndView Json_ok( String key , Map<String ,Object> map ){
		ModelMap model = new ModelMap();
		model.put("result", 1);
		model.put( key, map);
		return new ModelAndView( new MappingJackson2JsonView(), model);
	}
	
	public static ModelAndView Json_error(){
		ModelMap model = new ModelMap();
		model.put("result", 0);
		return new ModelAndView( new MappingJackson2JsonView(), model);
	}
	
	public static ModelAndView Json_error( String message ){
		ModelMap model = new ModelMap();
		model.put("result", 0);
		model.put("msg", message);
		return new ModelAndView( new MappingJackson2JsonView(), model);
	}
	
	public static ModelAndView Json_error_code( String code , String key , String msg ){
		ModelMap model = new ModelMap();
		model.put("error-code", code);
		if( msg != null && "".equals( msg ) ){
			model.put( key , msg);
		}
		return new ModelAndView( new MappingJackson2JsonView(), model);
	}
	
	public static ModelAndView Json_Array(String key , List<?> list ){
		ModelMap model = new ModelMap();
		model.put("result", 0);
		model.put( key , list);
		return new ModelAndView( new MappingJackson2JsonView(), model);
	}
	
	public static ModelAndView Json_Array( List<?> list ){
		ModelAndView mav = new ModelAndView( new ArrayJsonView());
		mav.addObject("list", list);
		return mav ;
	}
	
}
