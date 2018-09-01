package com.up72.game.dao;


import org.springframework.stereotype.Repository;

import com.up72.game.dto.resp.ClubUserUse;

/**
 * DAO
 */
@Repository
public interface ClubUserUseMapper {

	public int insert(ClubUserUse clubUserUse);
	
	public Integer sumMoneyByClubIdAndDate(String createTime,Integer clubId);
	
	public Integer countJuNumByClubIdAndDate(String createTime,Integer clubId);
	
	public Integer todayUse(Integer clubId,Integer userId);
	
}
