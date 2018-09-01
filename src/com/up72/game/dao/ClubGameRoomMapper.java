package com.up72.game.dao;


import java.util.HashMap;

import org.springframework.stereotype.Repository;

import com.up72.game.model.Room;

/**
 * DAO
 * 
 * @author up72
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface ClubGameRoomMapper {

    void save(HashMap<String, Object> map);

    void updateRoomState(Integer roomId,Long time,Integer xiaoJuNum);
    
    HashMap<String,Object> findServerIpAndXiaoJu(Integer roomId,Long time);
    
    
}
