package com.up72.server.mina.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.mina.core.session.IoSession;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.server.mina.function.MessageFunctions;
import com.up72.server.mina.function.TCPGameFunctions;
import com.up72.server.mina.main.MinaServerManager;

/**
 * Created by Administrator on 2017/7/28.
 */
@Component
public class ScheduledTask {
	
	
//    @Scheduled(cron = "0 0 3 * * ?")
    @Scheduled(cron = Cnst.CLEAN_3)
    public void testTaskWithDate() {
        System.out.println("每天凌晨3点清理任务开始执行 ");
        Set<String> roomIds = TCPGameFunctions.getSameKeys(Cnst.REDIS_PREFIX_ROOMMAP);
        long startTime;
        //从房间角度清理
        if (roomIds!=null&&roomIds.size()>0){
            for(String roomId:roomIds){
                RoomResp room = TCPGameFunctions.getRoomRespByRoomId(roomId);
                List<Player> players = TCPGameFunctions.getPlayerList(room);
                startTime = Long.parseLong(room.getCreateTime());
                long currentTime = new Date().getTime();
                if ((currentTime-startTime)>=Cnst.ROOM_OVER_TIME){//房间存在超过24小时，强制解散
                	//这里不能对玩家有任何操作，因为不知道玩家是否已经在其他房间里了
//                	if (players!=null&&players.size()>0) {
//                		for(Player p:players){
//                			if (p.getRoomId()==null) {
//                        		p.initPlayer(null,null,null,Cnst.PLAYER_STATE_DATING,0,0,0);
//							}
//                    	}
//                		TCPGameFunctions.setPlayersList(players);
//					}
//                	TCPGameFunctions.updateRedisData(room,null);
                	
                    MessageFunctions.updateDatabasePlayRecord(room);//解散房间
                    TCPGameFunctions.deleteByKey(Cnst.REDIS_PREFIX_ROOMMAP.concat(String.valueOf(roomId)));
                    //通知在线玩家房间被解散
                    MessageFunctions.interface_100111(Cnst.REQ_STATE_12, players,Integer.valueOf(roomId));
                }
            }
        }
        System.out.println("每天3点任务执行结束");
    }
    
//    @Scheduled(cron = "0 0 0/1 * * ?")
    @Scheduled(cron = Cnst.CLEAN_EVERY_HOUR)
    public void JVMCount() {
        System.out.println("每小时清理任务开始…… ");
        try {
        } catch (Exception e) {
        	System.out.println("每小时清理任务异常");
        } finally {
            cleanUserEveryHour();
            testTaskWithDate();
            BackFileUtil.deletePlayRecord();
        }
        System.out.println("每小时清理任务结束");
        Thread.currentThread().interrupt();
    }
    
    @Scheduled(cron = Cnst.COUNT_EVERY_TEN_MINUTE)
    public void onlineNumCount(){
    	Map<String,Object> ipcounts = new HashMap<String, Object>();
    	Map<String,Object> temp = new HashMap<String, Object>();
    	int count = MinaServerManager.tcpServer.getSessions().size();
    	temp.put("onlineNum", String.valueOf(count));
    	List<Long> userIds = new ArrayList<Long>();
    	
    	Iterator<IoSession> iterator = MinaServerManager.tcpServer.getSessions().values().iterator();
    	while(iterator.hasNext()){
    		IoSession se = iterator.next();
    		try {
        		Long userId = (Long) se.getAttribute(Cnst.USER_SESSION_USER_ID);
        		if (userId!=null) {
        			userIds.add(userId);
				}else{
					se.close(true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    	temp.put("userIds", userIds);
    	
    	ipcounts.put(Cnst.SERVER_IP,temp);
    	TCPGameFunctions.setStringByKey(Cnst.REDIS_ONLINE_NUM_COUNT.concat(Cnst.SERVER_IP), JSONObject.toJSONString(ipcounts));
    	System.out.println("每10分钟统计在线人数完成");
    }
    
    public static void cleanUserEveryHour(){
    	Set<String> openIds = TCPGameFunctions.getSameKeys(Cnst.REDIS_PREFIX_OPENIDUSERMAP);
        if (openIds!=null&&openIds.size()>0) {
			for(String openId:openIds){
				Long userId = TCPGameFunctions.getUserIdByOpenId(openId);
				Player player = TCPGameFunctions.getPlayerByUserId(String.valueOf(userId));
				if (player==null) {
					TCPGameFunctions.deleteByKey(Cnst.REDIS_PREFIX_OPENIDUSERMAP.concat(openId));
				}else{
					IoSession se =  MinaServerManager.tcpServer.getSessions().get(player.getSessionId());
					long t = player.getLastHeartTimeLong();
			    	long now = new Date().getTime();
					if (player.getPlayStatus().equals(Cnst.PLAYER_STATE_DATING)&&(now-t)>Cnst.HEART_TIME&&player.getStatus().equals(Cnst.PLAYER_LINE_STATE_INLINE)) {
						if (se!=null) {
							se.close(true);
						}
						TCPGameFunctions.deleteByKey(Cnst.REDIS_PREFIX_OPENIDUSERMAP.concat(openId));
						TCPGameFunctions.deleteByKey(Cnst.REDIS_PREFIX_USER_ID_USER_MAP.concat(String.valueOf(userId)));
						player = null;
					}
				}
			}
		}
    }
    

}