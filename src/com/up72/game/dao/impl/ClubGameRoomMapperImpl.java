package com.up72.game.dao.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import com.up72.game.dao.ClubGameRoomMapper;
import com.up72.game.dao.RoomMapper;
import com.up72.game.model.Room;
import com.up72.server.mina.utils.MyBatisUtils;

/**
 * Created by admin on 2017/6/23.
 */
public class ClubGameRoomMapperImpl implements ClubGameRoomMapper {

    @Override
    public void save(HashMap<String, Object> map) {
        System.out.println("保存房间信息");
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null) {
                String sqlName = ClubGameRoomMapper.class.getName() + ".save";
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


	@Override
	public HashMap<String,Object> findServerIpAndXiaoJu(Integer roomId,Long time) {
		HashMap<String,Object> serverIp = new HashMap<String, Object>();
		SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = ClubGameRoomMapper.class.getName()+".findServerIpAndXiaoJu";
                System.out.println("sql name ==>>" + sqlName);
                HashMap<String,Object> map = new HashMap<String, Object>();
                map.put("roomId", roomId);
                map.put("time", time);
                serverIp = session.selectOne(sqlName,map);
                session.close();
            }
        }catch (Exception e){
            System.out.println("findServerIp数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return serverIp;
	}

	@Override
	public void updateRoomState(Integer roomId, Long time, Integer xiaoJuNum) {
		SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null) {
                String sqlName = ClubGameRoomMapper.class.getName() + ".updateRoomState";
                Map<Object, Object> map =new HashMap<>();
                map.put("roomId",roomId);
                map.put("time", time);
                map.put("xiaoJuNum", xiaoJuNum);
                session.update(sqlName,map);
                MyBatisUtils.closeSessionAndCommit();
            }
        } catch (Exception e) {
            System.out.println("数据库操作出错！");
        } finally {
            session.close();
        }
	}

}
