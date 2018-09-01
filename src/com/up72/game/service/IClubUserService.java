/*
 * Powered By [up72-framework]
 * Web Site: http://www.up72.com
 * Since 2006 - 2017
 */

package com.up72.game.service;

import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.up72.game.dto.resp.ClubUser;


/**
 * 接口
 */
public interface IClubUserService {

	public int insert(ClubUser clubUser);
	
	public Integer countByUserId(Long userId);
	
	public Integer countByClubId(Integer clubId,Integer status);

	public List<ClubUser> selectClubByUserId(Long userId);
	
	public ClubUser selectUserByUserIdAndClubId(Long userId, Integer clubId);
	
	public int updateById(ClubUser clubUser);
	
	public Integer allUsers(Integer clubId);
	
	public Integer selectUserState(Integer clubId,Long userId);

	//今日活跃人数
	List<Integer> todayPerson(Integer clubId);
	//今日局数
	Integer todayGames(Integer clubId);
	
}
