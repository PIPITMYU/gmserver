package com.up72.game.dao;


import java.util.List;

import org.springframework.stereotype.Repository;

import com.up72.game.dto.resp.PlayerRecord;

/**
 * DAO
 * 
 * @author up72
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface ClubGamePlayRecordMapper {

	public void insertPlayRecord(PlayerRecord playRecord);
	
	public Integer countActNumByClubIdAndDate(String createTime,Integer clubId);
	
	public Integer countJuNumByClubIdAndDateAndUserId(String createTime,Integer clubId,Long userId);
	
	public List<PlayerRecord> findPlayerRecordByUserId(Long userId,Integer start,Integer limit,Integer clubId,Long startTime,Long endTime);
	
	public Integer findScoreByUserIdAndClubId(Long userId,Integer clubId,String createTime);

}
