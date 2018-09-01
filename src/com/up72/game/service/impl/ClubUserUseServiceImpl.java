/*
 * Powered By [up72-framework]
 * Web Site: http://www.up72.com
 * Since 2006 - 2017
 */

package com.up72.game.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.up72.game.dao.ClubUserUseMapper;
import com.up72.game.dao.impl.ClubUserUseMapperImpl;
import com.up72.game.dto.resp.ClubUserUse;
import com.up72.game.service.IClubUserUseService;

/**
 * DAO实现
 */
@Service
@Transactional
public class ClubUserUseServiceImpl implements IClubUserUseService {

    private ClubUserUseMapper clubUserUseMapper = new ClubUserUseMapperImpl();

	@Override
	public int insert(ClubUserUse clubUserUse) {
		
		return clubUserUseMapper.insert(clubUserUse);
	}
	/**
	 * 根据俱乐部id和时间 查询消费房卡数
	 */
	@Override
	public Integer sumMoneyByClubIdAndDate(String createTime, Integer clubId) {
		
		return clubUserUseMapper.sumMoneyByClubIdAndDate(createTime, clubId);
	}
	/**
     * 根据俱乐部id和时间查询开局数
     */
	@Override
	public Integer countJuNumByClubIdAndDate(String createTime, Integer clubId) {
		
		return clubUserUseMapper.countJuNumByClubIdAndDate(createTime, clubId);
	}
	/**
	 * 查询每人每日
	 */
	@Override
	public Integer todayUse(Integer clubId, Integer userId) {
		// TODO Auto-generated method stub
		return clubUserUseMapper.todayUse(clubId, userId);
	}
    
}
