/*
 * Powered By [up72-framework]
 * Web Site: http://www.up72.com
 * Since 2006 - 2017
 */

package com.up72.game.service;

import java.util.List;

import com.up72.game.dto.resp.Feedback;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.PlayerRecord;
import com.up72.game.model.PlayerMoneyRecord;
import com.up72.game.model.SystemMessage;
import com.up72.game.model.User;


/**
 * 接口
 * 
 * @author up72
 * @version 1.0
 * @since 1.0
 */
public interface IUserService {

    void save(User user);

    void update(User user);

    Player getById(java.lang.Long id);

    Player getByOpenId(String openId,String cid);

    List<PlayerRecord> findPlayerRecordByUserId(java.lang.Long userId,Integer start,Integer limit);

    void userFeedback(Feedback feedback);

    void updateMoney(Integer money,String userId);

    Player isExistUserId(Long userId);

    void updateUserAgree(Long userId);

    String getNotice();

    List<SystemMessage> getSystemMessage(java.lang.Long userId, Integer start, Integer limit);

    void insertPlayRecord(PlayerRecord playRecord);
    
    void insertPlayerMoneyRecord(PlayerMoneyRecord mr);
    
    String getConectUs();
    
    Integer getUserMoneyByUserId(Long userId);
}
