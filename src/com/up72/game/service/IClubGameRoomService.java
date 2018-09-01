/*
 * Powered By [up72-framework]
 * Web Site: http://www.up72.com
 * Since 2006 - 2017
 */

package com.up72.game.service;

import java.util.HashMap;

import com.up72.game.model.Room;


/**
 * 接口
 * 
 * @author up72
 * @version 1.0
 * @since 1.0
 */
public interface IClubGameRoomService {

    void save(HashMap<String, Object> map);

    void updateRoomState(Integer roomId,Long time,Integer xiaoJuNum);
    
    HashMap<String,Object> findServerIpAndXiaoJu(Integer roomId,Long time);

}
