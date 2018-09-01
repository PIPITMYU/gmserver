/*
 * Powered By [up72-framework]
 * Web Site: http://www.up72.com
 * Since 2006 - 2017
 */

package com.up72.game.service;

import java.util.List;

import com.up72.game.dto.resp.PlayerRecord;


/**
 * 接口
 * 
 * @author up72
 * @version 1.0
 * @since 1.0
 */
public interface IClubGamePlayRecordService {

    void insertPlayRecord(PlayerRecord playRecord);
    
    public Integer countActNumByClubIdAndDate(String createTime, Integer clubId);
    
    public Integer countJuNumByClubIdAndDateAndUserId(String createTime,Integer clubId, Long userId);
    
    public List<PlayerRecord> findPlayerRecordByUserId(Long userId,Integer start, Integer limit,Integer clubId,Long startTime,Long endTime);
    
    public Integer findScoreByUserIdAndClubId(Long userId,Integer clubId, String createTime);
    
}
