package com.up72.game.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import com.up72.game.dao.ClubInfoMapper;
import com.up72.game.dto.resp.ClubInfo;
import com.up72.server.mina.utils.MyBatisUtils;

/**
 * Created by admin on 2017/6/23.
 */
public class ClubInfoMapperImpl implements ClubInfoMapper {

	@Override
	public ClubInfo selectByClubId(Integer clubId) {
		
		ClubInfo clubinfo = null;
		SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = ClubInfoMapper.class.getName()+".selectByClubId";
                System.out.println("sql name ==>>" + sqlName);
                Map<Object, Object> map =new HashMap<>();
                map.put("clubId",clubId);
                clubinfo = session.selectOne(sqlName,map);
                session.close();
            }
        }catch (Exception e){
            System.out.println("selectByClubId数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return clubinfo;
	}

	@Override
	public List<ClubInfo> selectAll() {

		List<ClubInfo> list = null;
		SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = ClubInfoMapper.class.getName()+".selectAll";
                System.out.println("sql name ==>>" + sqlName);
                list = session.selectList(sqlName);
                session.close();
            }
        }catch (Exception e){
            System.out.println("selectAll数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return list;
	}
	/**
	 * 根据俱乐部id修改信息
	 */
	@Override
	public int updateByClubId(ClubInfo clubInfo) {

		int num = 0;
		SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null) {
                String sqlName = ClubInfoMapper.class.getName() + ".updateByClubId";
                num = session.update(sqlName,clubInfo);
                MyBatisUtils.closeSessionAndCommit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return num;
	}

	@Override
	public String selectCreateName(Integer userId) {
		String createName = null;
		SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = ClubInfoMapper.class.getName()+".selectCreateName";
                System.out.println("sql name ==>>" + sqlName);
                createName = session.selectOne(sqlName,userId);
                session.close();
            }
        }catch (Exception e){
            System.out.println("selectByClubId数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return createName;
	}

}
