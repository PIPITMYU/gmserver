package com.up72.game.dao;


import java.util.List;

import org.springframework.stereotype.Repository;

import com.up72.game.dto.resp.ClubInfo;

/**
 * DAO
 */
@Repository
public interface ClubInfoMapper {

	
	public ClubInfo selectByClubId(Integer clubId);
	
	public List<ClubInfo> selectAll();
	
	public int updateByClubId(ClubInfo clubInfo);
	
	//userID找俱乐部创始人姓名
	public String selectCreateName(Integer userId);
	
}
