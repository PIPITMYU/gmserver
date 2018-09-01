package com.up72.game.dao;


import java.util.List;

import org.springframework.stereotype.Repository;

import com.up72.game.dto.resp.ClubUser;

/**
 * DAO
 */
@Repository
public interface ClubUserMapper {

	public int insert(ClubUser clubUser);
	
	public Integer countByUserId(Long userId);
	
	public Integer countByClubId(Integer clubId,Integer status);
	
	public List<ClubUser> selectClubByUserId(Long userId);
	
	public ClubUser selectUserByUserIdAndClubId(Long userId,Integer clubId);
	
	public int updateById(ClubUser clubUser);
	
	public Integer allUsers(Integer clubId);
	
	public Integer selectUserState(Integer clubId,Long userId);

	public Integer todayGames(Integer clubId);

	public List<Integer> todayPerson(Integer clubId);
}
