/*
 * Powered By [up72-framework]
 * Web Site: http://www.up72.com
 * Since 2006 - 2017
 */

package com.up72.game.service.impl;

import java.util.HashMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.up72.game.dao.ClubGameRoomMapper;
import com.up72.game.dao.impl.ClubGameRoomMapperImpl;
import com.up72.game.model.Room;
import com.up72.game.service.IClubGameRoomService;

/**
 * DAO实现
 * 
 * @author up72
 * @version 1.0
 * @since 1.0
 */
@Service
@Transactional
public class ClubGameRoomServiceImpl implements IClubGameRoomService {


    private ClubGameRoomMapper clubGameRoomMapper =new ClubGameRoomMapperImpl();

    public void save(HashMap<String, Object> map) {
    	
    	clubGameRoomMapper.save(map);
    }


	@Override
	public HashMap<String,Object> findServerIpAndXiaoJu(Integer roomId,Long time) {

		return clubGameRoomMapper.findServerIpAndXiaoJu(roomId,time);
	}

	@Override
	public void updateRoomState(Integer roomId, Long time, Integer xiaoJuNum) {
		clubGameRoomMapper.updateRoomState(roomId, time, xiaoJuNum);
		
	}
    
}
