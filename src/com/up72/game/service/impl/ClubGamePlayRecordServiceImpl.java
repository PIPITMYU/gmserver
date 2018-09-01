/*
 * Powered By [up72-framework]
 * Web Site: http://www.up72.com
 * Since 2006 - 2017
 */

package com.up72.game.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.up72.game.dao.ClubGamePlayRecordMapper;
import com.up72.game.dao.impl.ClubGamePlayRecordMapperImpl;
import com.up72.game.dto.resp.PlayerRecord;
import com.up72.game.service.IClubGamePlayRecordService;

/**
 * DAO实现
 * 
 * @author up72
 * @version 1.0
 * @since 1.0
 */
@Service
@Transactional
public class ClubGamePlayRecordServiceImpl implements IClubGamePlayRecordService {


    private ClubGamePlayRecordMapper clubGamePlayRecordMapper = new ClubGamePlayRecordMapperImpl();
    
    @Override
    public void insertPlayRecord(PlayerRecord playRecord) {
    	
    	clubGamePlayRecordMapper.insertPlayRecord(playRecord);
    }
    /**
     * 根据俱乐部id和时间查询 活跃牌友数
     */
	@Override
	public Integer countActNumByClubIdAndDate(String createTime, Integer clubId) {
		
		return clubGamePlayRecordMapper.countActNumByClubIdAndDate(createTime, clubId);
	}
	/**
	 * 根据俱乐部id，人员id和时间查询 总局数
	 */
	@Override
	public Integer countJuNumByClubIdAndDateAndUserId(String createTime,Integer clubId, Long userId) {
		
		return clubGamePlayRecordMapper.countJuNumByClubIdAndDateAndUserId(createTime, clubId, userId);
	}
	/**
	 * 根据俱乐部id，人员id和时间查询 其参与的 战绩 分页
	 */
	@Override
	public List<PlayerRecord> findPlayerRecordByUserId(Long userId,Integer start, Integer limit,Integer clubId,Long startTime,Long endTime) {
		
		return clubGamePlayRecordMapper.findPlayerRecordByUserId(userId, start, limit, clubId, startTime, endTime);
	}
	/**
	 * 根据俱乐部id，人员id和时间查询  总分数
	 */
	@Override
	public Integer findScoreByUserIdAndClubId(Long userId,Integer clubId, String createTime) {
		
		return clubGamePlayRecordMapper.findScoreByUserIdAndClubId(userId, clubId, createTime);
	}
    
}
