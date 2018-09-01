/*
 * Powered By [up72-framework]
 * Web Site: http://www.up72.com
 * Since 2006 - 2017
 */

package com.up72.game.service.impl;

import java.util.*;

import com.up72.game.dao.impl.UserMapperImpl;
import com.up72.game.dao.impl.UserMapper_loginImpl;
import com.up72.game.dto.resp.Feedback;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.PlayerRecord;
import com.up72.game.model.*;
import com.up72.game.dao.*;
import com.up72.game.service.*;
import com.up72.framework.util.page.PageBounds;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.up72.game.service.IUserService;
import com.up72.framework.util.page.PageList;
import com.up72.framework.util.page.Page;

/**
 * DAO实现
 * 
 * @author up72
 * @version 1.0
 * @since 1.0
 */
@Service
@Transactional
public class UserServiceImpl implements IUserService {


    private UserMapper userMapper = new UserMapperImpl();

    public void save(User user) {
        userMapper.insert(user);
    }

    public void update(User user) {
        userMapper.update(user);
    }

    @Transactional(readOnly = true)
    public Player getById(java.lang.Long id) {
        return userMapper.findById(id);
    }

    @Override
    public Player getByOpenId(String openId,String cid) {
        return userMapper.findByOpenId(openId,cid);
    }

    @Override
    public List<PlayerRecord> findPlayerRecordByUserId(Long userId,Integer start,Integer limit) {
        return userMapper.findPlayerRecordByUserId(userId,start,limit);
    }

    @Override
    public void userFeedback(Feedback feedback) {
        userMapper.userFeedback(feedback);
    }

    @Override
    public void updateMoney(Integer money, String userId) {
        userMapper.updateMoney(money,userId);
    }

    @Override
    public Player isExistUserId(Long userId) {
        return userMapper.isExistUserId(userId);
    }

    @Override
    public void updateUserAgree(Long userId) {
        userMapper.updateUserAgree(userId);
    }

    @Override
    public String getNotice() {
        return userMapper.getNotice();
    }

    @Override
    public List<SystemMessage> getSystemMessage(Long userId, Integer start, Integer limit) {
        return userMapper.getSystemMessage(userId,start,limit);
    }

    @Override
    public void insertPlayRecord(PlayerRecord playRecord) {
        userMapper.insertPlayRecord(playRecord);
    }
    
    @Override
    public void insertPlayerMoneyRecord(PlayerMoneyRecord mr) {
    	userMapper.insertPlayerMoneyRecord(mr);
    	
    }
    
    @Override
    public String getConectUs() {
    	return userMapper.getConectUs();
    }
    @Override
    public Integer getUserMoneyByUserId(Long userId) {
    	return userMapper.getUserMoneyByUserId(userId);
    }
    
    
}
