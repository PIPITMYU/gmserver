/*
 * Powered By [up72-framework]
 * Web Site: http://www.up72.com
 * Since 2006 - 2017
 */

package com.up72.game.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.up72.game.dao.ClubInfoMapper;
import com.up72.game.dao.impl.ClubInfoMapperImpl;
import com.up72.game.dto.resp.ClubInfo;
import com.up72.game.service.IClubInfoService;

/**
 * DAO实现
 */
@Service
@Transactional
public class ClubInfoServiceImpl implements IClubInfoService {

    private ClubInfoMapper clubInfoMapper =new ClubInfoMapperImpl();

    /**
     * 根据俱乐部id查询信息
     */
	@Override
	public ClubInfo selectByClubId(Integer clubId) {
		
		return clubInfoMapper.selectByClubId(clubId);
	}
	/**
     * 查询所有
     */
	@Override
	public List<ClubInfo> selectAll() {
		
		return clubInfoMapper.selectAll();
	}
	/**
     * 根据俱乐部id 更新信息
     */
	@Override
	public int updateByClubId(ClubInfo clubInfo) {
		
		return clubInfoMapper.updateByClubId(clubInfo);
	}
	@Override
	public String selectCreateName(Integer userId) {
		// TODO Auto-generated method stub
		return clubInfoMapper.selectCreateName(userId);
	}

    
}
