package com.up72.game.dao.impl;

import java.util.HashMap;

import org.apache.ibatis.session.SqlSession;

import com.up72.game.dao.PayMapper;
import com.up72.game.dao.RoomMapper;
import com.up72.server.mina.utils.MyBatisUtils;

public class PayMapperImpl implements PayMapper{

	@Override
	public Integer findDaiLByUserId(Long userId) {
		Integer result = 0;
    	SqlSession session = MyBatisUtils.getSession();
            try {
                if (session != null){
                    String sqlName = PayMapper.class.getName()+".findDaiLByUserId";
                    System.out.println("sql name ==>>" + sqlName);
                    result = session.selectOne(sqlName,userId);
                    session.close();
                }
            }catch (Exception e){
                System.out.println("getMyCreateRoomTotal数据库操作出错！");
                e.printStackTrace();
            }finally {
                session.close();
            }
            return result;
	}

	@Override
	public Integer findDaiLByCode(Long code) {
		Integer result = 0;
    	SqlSession session = MyBatisUtils.getSession();
            try {
                if (session != null){
                    String sqlName = PayMapper.class.getName()+".findDaiLByCode";
                    System.out.println("sql name ==>>" + sqlName);
                    result = session.selectOne(sqlName,code);
                    session.close();
                }
            }catch (Exception e){
                System.out.println("getMyCreateRoomTotal数据库操作出错！");
                e.printStackTrace();
            }finally {
                session.close();
            }
            return result;
	}

	@Override
	public void bindDaiL(HashMap<String, Object> map) {
		 System.out.println("绑定代理");
	        SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null) {
	                String sqlName = PayMapper.class.getName() + ".bindDaiL";
	                session.insert(sqlName, map);
	                MyBatisUtils.closeSessionAndCommit();
	            }
	        } catch (Exception e) {
	            System.out.println("insert room数据库操作出错！");
	            e.printStackTrace();
	        } finally {
	            session.close();
	        }
	}

}
