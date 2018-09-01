/*
 * Powered By [up72-framework]
 * Web Site: http://www.up72.com
 * Since 2006 - 2017
 */

package com.up72.game.service;

import com.up72.game.dto.resp.ClubUserUse;


/**
 * 接口
 */
public interface IClubUserUseService {

	public int insert(ClubUserUse clubUserUse);
	
	public Integer sumMoneyByClubIdAndDate(String createTime, Integer clubId);
	
	public Integer countJuNumByClubIdAndDate(String createTime, Integer clubId);
	
	public  Integer todayUse(Integer clubId,Integer userId);
	
}
