package com.up72.game.dao.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import com.up72.game.dao.UserMapper;
import com.up72.game.dao.UserMapper_login;
import com.up72.game.dto.resp.Player;
import com.up72.server.mina.utils.MyBatisUtils_login;

/**
 * Created by admin on 2017/6/22.
 */
public class UserMapper_loginImpl implements UserMapper_login {

    @Override
    public Player getUserInfoByOpenId(String openId){
        Player result = null;
        System.out.println("getUserInfoByOpenId openId" + openId);
        SqlSession session = MyBatisUtils_login.getSession();
        try {
            if (session != null){
                String sqlName = UserMapper_login.class.getName()+".getUserInfoByOpenId";
                System.out.println("sql name ==>>" + sqlName);
                Map map =new HashMap<>();
                map.put("openId",openId);
                result = session.selectOne(sqlName, map);
                session.close();
            }
        }catch (Exception e){
            System.out.println("findByOpenId数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return result;
    }

}
