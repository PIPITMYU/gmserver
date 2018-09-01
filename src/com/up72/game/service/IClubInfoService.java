/*
 * Powered By [up72-framework]
 * Web Site: http://www.up72.com
 * Since 2006 - 2017
 */

package com.up72.game.service;

import java.util.List;

import com.up72.game.dto.resp.ClubInfo;


/**
 * 接口
 */
public interface IClubInfoService {

	public ClubInfo selectByClubId(Integer clubId);
	
	public List<ClubInfo> selectAll();
	
	public int updateByClubId(ClubInfo clubInfo);
	public String selectCreateName(Integer userId);
}
