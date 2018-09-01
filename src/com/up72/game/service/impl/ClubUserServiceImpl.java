/*
 * Powered By [up72-framework]
 * Web Site: http://www.up72.com
 * Since 2006 - 2017
 */

package com.up72.game.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.up72.game.dao.ClubUserMapper;
import com.up72.game.dao.impl.ClubUserMapperImpl;
import com.up72.game.dto.resp.ClubUser;
import com.up72.game.service.IClubUserService;

/**
 * DAO实现
 */
@Service
@Transactional
public class ClubUserServiceImpl implements IClubUserService {

    private ClubUserMapper clubUserMapper = new ClubUserMapperImpl();

    /**
     * 加入俱乐部
     */
	@Override
	public int insert(ClubUser clubUser) {
		
		return clubUserMapper.insert(clubUser);
	}
	/**
     * 查询我加入俱乐部的数量
     */
	@Override
	public Integer countByUserId(Long userId) {
		
		return clubUserMapper.countByUserId(userId);
	}
	/**
     * 查询我加入俱乐部详情
     */
	@Override
	public List<ClubUser> selectClubByUserId(Long userId) {
		
		return clubUserMapper.selectClubByUserId(userId);
	}
	/**
     * 根据俱乐部id查询人数
     */
	@Override
	public Integer countByClubId(Integer clubId, Integer status) {
		
		return clubUserMapper.countByClubId(clubId,status);
	}
	/**
     * 根据俱乐部id和用户id查询
     */
	@Override
	public ClubUser selectUserByUserIdAndClubId(Long userId, Integer clubId) {
		
		return clubUserMapper.selectUserByUserIdAndClubId(userId, clubId);
	}
	/**
     * 根据id修改记录
     */
	@Override
	public int updateById(ClubUser clubUser) {
		
		return clubUserMapper.updateById(clubUser);
	}
	/**
     * 查询俱乐部总人数
     */
	@Override
	public Integer allUsers(Integer clubId) {
		// TODO Auto-generated method stub
		return clubUserMapper.allUsers(clubId);
	}
	/**
     * 查询当前user状态
     */
	@Override
	public Integer selectUserState(Integer clubId, Long userId) {
		// TODO Auto-generated method stub
		return clubUserMapper.selectUserState(clubId, userId);
	}
	@Override
	public List<Integer> todayPerson(Integer clubId) {
		return clubUserMapper.todayPerson(clubId);
	}
	@Override
	public Integer todayGames(Integer clubId) {
		return clubUserMapper.todayGames(clubId);

	}

    
}
