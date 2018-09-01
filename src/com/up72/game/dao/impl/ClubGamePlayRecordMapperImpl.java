package com.up72.game.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import com.up72.game.dao.ClubGamePlayRecordMapper;
import com.up72.game.dto.resp.PlayerRecord;
import com.up72.server.mina.function.TCPGameFunctions;
import com.up72.server.mina.utils.MyBatisUtils;

/**
 * Created by admin on 2017/6/22.
 */
public class ClubGamePlayRecordMapperImpl implements ClubGamePlayRecordMapper {

    @Override
    public void insertPlayRecord(PlayerRecord playRecord) {
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null) {
                String sqlName = ClubGamePlayRecordMapper.class.getName() + ".insertPlayRecord";
                session.insert(sqlName, playRecord);
                MyBatisUtils.closeSessionAndCommit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

	@Override
	public Integer countActNumByClubIdAndDate(String createTime, Integer clubId) {

		Integer num = null;
		SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = ClubGamePlayRecordMapper.class.getName()+".countActNumByClubIdAndDate";
                System.out.println("sql name ==>>" + sqlName);
                Map<String, Object> map =new HashMap<String, Object>();
                map.put("createTime",createTime);
                map.put("clubId",clubId);
                num = session.selectOne(sqlName,map);
                session.close();
            }
        }catch (Exception e){
            System.out.println("countActNumByClubIdAndDate数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return num;
	}

	@Override
	public Integer countJuNumByClubIdAndDateAndUserId(String createTime,Integer clubId, Long userId) {
		
		Integer num = null;
		SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = ClubGamePlayRecordMapper.class.getName()+".countJuNumByClubIdAndDateAndUserId";
                System.out.println("sql name ==>>" + sqlName);
                Map<String, Object> map =new HashMap<String, Object>();
                map.put("createTime",createTime);
                map.put("clubId",clubId);
                map.put("userId",userId);
                num = session.selectOne(sqlName,map);
                session.close();
            }
        }catch (Exception e){
            System.out.println("countJuNumByClubIdAndDateAndUserId数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return num;
	}

	@Override
	public List<PlayerRecord> findPlayerRecordByUserId(Long userId,Integer start, Integer limit,Integer clubId,Long startTime,Long endTime) {
		
		List<PlayerRecord> result = new ArrayList<>();
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = ClubGamePlayRecordMapper.class.getName()+".findPlayerRecordByUserId";
                System.out.println("sql name ==>>" + sqlName);
                Map<Object, Object> map =new HashMap<>();
                map.put("userId",userId);
                map.put("start",start);
                map.put("limit",limit);
                map.put("clubId",clubId);
                map.put("startTime",startTime);
                map.put("endTime",endTime);
                result = session.selectList(sqlName,map);
                session.close();
            }
        }catch (Exception e){
            System.out.println("findPlayerRecordByUserId数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return result;
	}

	@Override
	public Integer findScoreByUserIdAndClubId(Long userId,Integer clubId, String createTime) {

		Integer num = null;
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = ClubGamePlayRecordMapper.class.getName()+".findScoreByUserIdAndClubId";
                System.out.println("sql name ==>>" + sqlName);
                Map<Object, Object> map =new HashMap<>();
                map.put("userId",userId);
                map.put("clubId",clubId);
                map.put("createTime",createTime);
                map.put("morning",TCPGameFunctions.getTimesmorning());
                map.put("night",TCPGameFunctions.getTimesNight());
                num = session.selectOne(sqlName, map);
                session.close();
            }
        }catch (Exception e){
            System.out.println("findScoreByUserIdAndClubId数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return num;
	}
    
}
