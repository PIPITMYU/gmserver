package com.up72.game.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import com.up72.game.dao.ClubUserMapper;
import com.up72.game.dto.resp.ClubUser;
import com.up72.server.mina.function.TCPGameFunctions;
import com.up72.server.mina.utils.MyBatisUtils;

/**
 * Created by admin on 2017/6/23.
 */
public class ClubUserMapperImpl implements ClubUserMapper {

	@Override
	public int insert(ClubUser clubUser) {
		
		int num = 0;
		SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = ClubUserMapper.class.getName()+".insert";
                System.out.println("sql name ==>>" + sqlName);
                num = session.insert(sqlName, clubUser);
                MyBatisUtils.closeSessionAndCommit();
            }
        }catch (Exception e){
            System.out.println("insert数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return num;
	}
	
	@Override
	public Integer countByUserId(Long userId) {

		Integer num = null;
		SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = ClubUserMapper.class.getName()+".countByUserId";
                System.out.println("sql name ==>>" + sqlName);
                num = session.selectOne(sqlName,userId);
                session.close();
            }
        }catch (Exception e){
            System.out.println("countByUserId数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return num;
	}

	@Override
	public List<ClubUser> selectClubByUserId(Long userId) {
		
		List<ClubUser> list = null;
		SqlSession session = MyBatisUtils.getSession();
		try {
            if (session != null){
                String sqlName = ClubUserMapper.class.getName()+".selectClubByUserId";
                System.out.println("sql name ==>>" + sqlName);
                Map<String, Object> map =new HashMap<String, Object>();
                map.put("userId",userId);
                list = session.selectList(sqlName, map);
                session.close();
            }
        }catch (Exception e){
            System.out.println("selectClubByUserId数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return list;
	}

	@Override
	public Integer countByClubId(Integer clubId, Integer status) {
		
		Integer num = null;
		SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = ClubUserMapper.class.getName()+".countByClubId";
                System.out.println("sql name ==>>" + sqlName);
                Map<String, Object> map =new HashMap<String, Object>();
                map.put("clubId",clubId);
                map.put("status",status);
                num = session.selectOne(sqlName,map);
                session.close();
            }
        }catch (Exception e){
            System.out.println("countByClubId数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return num;
	}

	@Override
	public ClubUser selectUserByUserIdAndClubId(Long userId, Integer clubId) {

		ClubUser clubUser = null;
		SqlSession session = MyBatisUtils.getSession();
		try {
            if (session != null){
                String sqlName = ClubUserMapper.class.getName()+".selectUserByUserIdAndClubId";
                System.out.println("sql name ==>>" + sqlName);
                Map<String, Object> map =new HashMap<String, Object>();
                map.put("userId",userId);
                map.put("clubId",clubId);
                clubUser = session.selectOne(sqlName, map);
                session.close();
            }
        }catch (Exception e){
            System.out.println("selectUserByUserIdAndClubId数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return clubUser;
	}

	@Override
	public int updateById(ClubUser clubUser) {

		int num = 0;
		SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = ClubUserMapper.class.getName()+".updateById";
                System.out.println("sql name ==>>" + sqlName);
                num = session.update(sqlName, clubUser);
                MyBatisUtils.closeSessionAndCommit();
            }
        }catch (Exception e){
            System.out.println("updateById数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return num;
	}

	@Override
	public Integer allUsers(Integer clubId) {
		Integer num = 0;
		SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = ClubUserMapper.class.getName()+".allUsers";
                System.out.println("sql name ==>>" + sqlName);
                num = session.selectOne(sqlName, clubId);
                MyBatisUtils.closeSessionAndCommit();
            }
        }catch (Exception e){
            System.out.println("allUsers数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return num;
	}

	@Override
	public Integer selectUserState(Integer clubId, Long userId) {
		Integer num = 0;
		SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = ClubUserMapper.class.getName()+".selectUserState";
                System.out.println("sql name ==>>" + sqlName);
                Map<String, Object> map =new HashMap<String, Object>();
                map.put("userId",userId);
                map.put("clubId",clubId);
                num = session.selectOne(sqlName, map);
                MyBatisUtils.closeSessionAndCommit();
            }
        }catch (Exception e){
            System.out.println("selectUserState数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return num;
	}
	@Override
	public Integer todayGames(Integer clubId) {
		Integer num = 0;
		SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = ClubUserMapper.class.getName()+".todayGames";
                System.out.println("sql name ==>>" + sqlName);
                HashMap<String,Object> map = new HashMap<String, Object>();
        		map.put("clubId", clubId);
        		map.put("morning", TCPGameFunctions.getTimesmorning());
        		map.put("night", TCPGameFunctions.getTimesNight());
                num = session.selectOne(sqlName, map);
                MyBatisUtils.closeSessionAndCommit();
            }
        }catch (Exception e){
            System.out.println("todayGames数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return num;
	}

	@Override
	public List<Integer> todayPerson(Integer clubId) {
		List<Integer>  num = new ArrayList<Integer>();
		SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = ClubUserMapper.class.getName()+".todayPerson";
                System.out.println("sql name ==>>" + sqlName);
                HashMap<String,Object> map = new HashMap<String, Object>();
        		map.put("clubId", clubId);
        		map.put("morning", TCPGameFunctions.getTimesmorning());
        		map.put("night", TCPGameFunctions.getTimesNight());
                num = session.selectList(sqlName, map);
                MyBatisUtils.closeSessionAndCommit();
            }
        }catch (Exception e){
            System.out.println("todayPerson数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return num;
	}
}
