package com.up72.game.dao.impl;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import com.up72.game.dao.ClubUserUseMapper;
import com.up72.game.dto.resp.ClubUserUse;
import com.up72.server.mina.utils.MyBatisUtils;

/**
 * Created by admin on 2017/6/23.
 */
public class ClubUserUseMapperImpl implements ClubUserUseMapper {

	@Override
	public int insert(ClubUserUse clubUserUse) {

		int num = 0;
		SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = ClubUserUseMapper.class.getName()+".insert";
                System.out.println("sql name ==>>" + sqlName);
                num = session.insert(sqlName, clubUserUse);
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
	public Integer sumMoneyByClubIdAndDate(String createTime, Integer clubId) {
		
		Integer num = null;
		SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = ClubUserUseMapper.class.getName()+".sumMoneyByClubIdAndDate";
                System.out.println("sql name ==>>" + sqlName);
                Map<String, Object> map =new HashMap<String, Object>();
                map.put("createTime",createTime);
                map.put("clubId",clubId);
                num = session.selectOne(sqlName,map);
                session.close();
            }
        }catch (Exception e){
            System.out.println("sumMoneyByClubIdAndDate数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return num;
	}

	@Override
	public Integer countJuNumByClubIdAndDate(String createTime, Integer clubId) {

		Integer num = null;
		SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = ClubUserUseMapper.class.getName()+".countJuNumByClubIdAndDate";
                System.out.println("sql name ==>>" + sqlName);
                Map<String, Object> map =new HashMap<String, Object>();
                map.put("createTime",createTime);
                map.put("clubId",clubId);
                num = session.selectOne(sqlName,map);
                session.close();
            }
        }catch (Exception e){
            System.out.println("countJuNumByClubIdAndDate数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return num;
	}

	@Override
	public Integer todayUse(Integer clubId, Integer userId) {
		Integer num = null;
		SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = ClubUserUseMapper.class.getName()+".todayUse";
                System.out.println("sql name ==>>" + sqlName);
                Map<String, Object> map =new HashMap<String, Object>();
                map.put("userId",userId);
                map.put("clubId",clubId);
                map.put("morning", getTimesmorning());
                map.put("night", getTimesNight());
                num = session.selectOne(sqlName,map);
                if(num==null)
                	num=0;
                session.close();
            }
        }catch (Exception e){
            System.out.println("todayUse数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return num;
	}
	//获取0点时间戳
		public static Long getTimesmorning(){
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.MILLISECOND, 0);
			return cal.getTimeInMillis();
		}
		//获取24点时间戳
		public static Long getTimesNight(){
			return getTimesmorning()+86400000;
		}
}
