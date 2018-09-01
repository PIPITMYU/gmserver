package com.up72.server.mina.function;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.mina.core.session.IoSession;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.server.mina.bean.ProtocolData;
import com.up72.server.mina.main.MinaServerManager;
import com.up72.server.mina.tcp.MinaTCPServer;
import com.up72.server.mina.utils.BackFileUtil;
import com.up72.server.mina.utils.MyLog;
import com.up72.server.mina.utils.ScheduledTask;

public class TCPFunctionExecutor {

    private static final MyLog log = MyLog.getLogger(TCPFunctionExecutor.class);

    public synchronized static void execute(IoSession session, ProtocolData readData)
            throws IOException, NoSuchMethodException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException,
            ClassNotFoundException, InstantiationException,Exception {


		if (readData.getJsonString().equals(MinaTCPServer.HEART_BEAT)) {
        	heart(session, readData);
			return ;
		}
    	
        int interfaceId = readData.getInterfaceId();
        switch (interfaceId) {

            //大厅消息段
            case 100002: HallFunctions.interface_100002(session,readData);break;
            case 100003: HallFunctions.interface_100003(session,readData);break;
            case 100004: HallFunctions.interface_100004(session,readData);break;
            case 100005: HallFunctions.interface_100005(session,readData);break;
            case 100006: HallFunctions.interface_100006(session,readData);break;
            case 100007: HallFunctions.interface_100007(session,readData);break;
            case 100008: HallFunctions.interface_100008(session,readData);break;
            case 100009: HallFunctions.interface_100009(session,readData);break;
            case 100010: HallFunctions.interface_100010(session,readData);break;
            case 100011: HallFunctions.interface_100011(session,readData);break;
            case 100012: HallFunctions.interface_100012(session,readData);break;
            case 100013: HallFunctions.interface_100013(session,readData);break;
            case 100014: HallFunctions.interface_100014(session,readData);break;
            case 100015: HallFunctions.interface_100015(session,readData);break;

            //推送消息段
            case 100100: MessageFunctions.interface_100100(session,readData);break;//大接口
            case 100101:break;
            case 100102:break;
            case 100103: MessageFunctions.interface_100103(session,readData);break;//大结算

            //游戏中消息段
            case 100200: GameFunctions.interface_100200(session,readData);break;
            case 100201: GameFunctions.interface_100201(session,readData);break;
            case 100202: GameFunctions.interface_100202(session,readData);break;
            case 100203: GameFunctions.interface_100203(session,readData);break;
            case 100204: GameFunctions.interface_100204(session,readData);break;
            case 100205: GameFunctions.interface_100205(session,readData);break;
            case 100206: GameFunctions.interface_100206(session,readData);break;
            case 100207: GameFunctions.interface_100207(session,readData);break;
            

    		// 俱乐部：
    		case 500000:
    			ClubInfoFunctions.interface_500000(session, readData);
    			break;// 加入俱乐部
    		case 500001:
    			ClubInfoFunctions.interface_500001(session, readData);
    			break;// 查询俱乐部信息
    		case 500002:
    			ClubInfoFunctions.interface_500002(session, readData);
    			break;// 我的俱乐部
    		case 500003:
    			ClubInfoFunctions.interface_500003(session, readData);
    			break;// 俱乐部详情
    		case 500006:
    			ClubInfoFunctions.interface_500006(session, readData);
    			break;// 我的成绩
    		case 500007:
    			ClubInfoFunctions.interface_500007(session, readData);
    			break;// 申请离开
    		case 500004:
    			ClubInfoFunctions.interface_500004(session, readData);
    			break;// 俱乐部创建房间
    		case 500005:
    			ClubInfoFunctions.interface_500005(session, readData);
    			break;// 俱乐部加入房间
    		case 600000:
    			PayFunction.interface_600000(session, readData);
    			break;// 请求支付
    		case 600001:
    			PayFunction.interface_600001(session, readData);
    			break;// 用户绑定代理
    		case 600002:
    			PayFunction.interface_600002(session, readData);
    			break;// 支付页面
    		case 600003:
    			PayFunction.interface_600003(session, readData);
    			break;// 三方支付接口

            //强制解散房间
            case 999800:disRoomForce(session, readData);break;
            //查看在线房间列表
            case 999801:onLineRooms(session, readData);break;
            //在线人数以及房间数
            case 999802:onLineNum(session, readData);break;
            //清理单个在线玩家
            case 999803:cleanUserInfo(session, readData);break;
            
            //心跳
            case 100000: heart(session, readData);;break;
            
            //更改玩家手牌
            case 999997://本方法测试有效
//            	if(DataLoader.jdbcName_work.equals("localhost")){
//            		session.close(true);
//            	}else{
                	changePlayerMj(session, readData);
//            	}
            	break;

            //查看房间内剩余的牌
            case 999998: 
            	//本方法测试有效
//            	if(DataLoader.jdbcName_work.equals("localhost")){
//            		session.close(true);
//            	}else{
                	seeRoonPais(session, readData);
//            	}
            	break;
            //设置房间里剩余的牌
            case 999999: 
//            	if(DataLoader.jdbcName_work.equals("localhost")){
//            		session.close(true);
//            	}else{
                	setRoomPais(session, readData);
//            	}
            	break;
            default:
                Player user=  TCPGameFunctions.getPlayerByUserId(String.valueOf(session.getAttribute(Cnst.USER_SESSION_USER_ID)));
                if(user == null) {
                    
                } else {
                    log.I("未知interfaceId"+interfaceId);
                    MessageFunctions.illegalRequest(interfaceId,session);//非法请求
                }   break;
        }
    }
    
    public static void cleanUserInfo(IoSession session,ProtocolData readData){
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Long userId = obj.getLong("userId");
        Player currentPlayer = TCPGameFunctions.getPlayerByUserId(String.valueOf(userId));
        if (currentPlayer!=null) {
        	//只清理在大厅中的用户
			if (currentPlayer.getPlayStatus().equals(Cnst.PLAYER_STATE_DATING)) {
				TCPGameFunctions.deleteByKey(Cnst.REDIS_PREFIX_USER_ID_USER_MAP.concat(String.valueOf(currentPlayer.getUserId())));
			}
		}
        
        
        
    }
    
    private static void changePlayerMj(IoSession session,ProtocolData readData){
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Integer roomId = obj.getInteger("roomSn");
        Long userId = obj.getLong("userId");
        String currentMjs = obj.getString("currentMjs");
        RoomResp room = TCPGameFunctions.getRoomRespByRoomId(String.valueOf(roomId));
        if (room!=null) {
        	if (room.getStatus().equals(Cnst.ROOM_STATE_GAMIING)) {
		        Player p = TCPGameFunctions.getPlayerByUserId(String.valueOf(userId));
		        if (p!=null) {
					p.setCurrentMjList(JSONArray.parseArray(currentMjs, Integer[][].class));
			        TCPGameFunctions.updateRedisData(null, p);
				}
			}
        }
    }
    
    private static void seeRoonPais(IoSession session,ProtocolData readData){
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Integer roomId = obj.getInteger("roomSn");
        RoomResp room = TCPGameFunctions.getRoomRespByRoomId(String.valueOf(roomId));
        if (room!=null) {
            JSONObject result = TCPGameFunctions.getJSONObj(interfaceId,1,room.getCurrentMjList());
            ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
            session.write(pd);
		}
    }
    
    private static void setRoomPais(IoSession session,ProtocolData readData){
    	JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Integer roomId = obj.getInteger("roomSn");
        String currentMjs = obj.getString("currentMjs");
        RoomResp room = TCPGameFunctions.getRoomRespByRoomId(String.valueOf(roomId));
        if (room!=null) {
        	room.setCurrentMjList(JSONArray.parseArray(currentMjs, Integer[][].class));
            TCPGameFunctions.updateRedisData(room, null);
		}
    }
    
    /**
     * 心跳操作
     * @param session
     * @param readData
     */
    public static void heart(IoSession session,ProtocolData readData) throws Exception{
        Player p = TCPGameFunctions.getPlayerByUserId(String.valueOf(session.getAttribute(Cnst.USER_SESSION_USER_ID)));
        Long time = new Date().getTime();
        if (p==null) {
			return;
		}else{//存在用户
	        p.setLastHeartTimeLong(time);
	        TCPGameFunctions.updateRedisData(null,p);
			if (p.getRoomId()!=null) {//存在房间号
				RoomResp room = TCPGameFunctions.getRoomRespByRoomId(String.valueOf(p.getRoomId()));
				if (room!=null&&!room.getStatus().equals(Cnst.ROOM_STATE_YJS)) {
					List<Player> players = TCPGameFunctions.getPlayerList(p.getRoomId());
					if (players!=null&&players.size()>0) {
						if (p.getStatus().equals(Cnst.PLAYER_LINE_STATE_OUT)) {
							p.setStatus(Cnst.PLAYER_LINE_STATE_INLINE);
							MessageFunctions.interface_100109(players, Cnst.PLAYER_LINE_STATE_INLINE, p.getUserId(),session,p.getPlayStatus());
						}else{
							List<Player> needPlayer = new ArrayList<Player>();
							for(Player pp:players){
								if (pp!=null&&pp.getLastHeartTimeLong()!=null) {
									long t = pp.getLastHeartTimeLong();
									if ((time-t)>Cnst.HEART_TIME&&pp.getStatus().equals(Cnst.PLAYER_LINE_STATE_INLINE)) {
										needPlayer.add(pp);
									}
								}
							}
							if (needPlayer.size()>0) {
								for(Player pp:needPlayer){
									pp.setStatus(Cnst.PLAYER_LINE_STATE_OUT);
							        TCPGameFunctions.updateRedisData(null,pp);
									MessageFunctions.interface_100109(players, Cnst.PLAYER_LINE_STATE_OUT, pp.getUserId(),session,pp.getPlayStatus());
								}
							}
						}
					}
				}else{
//					p.setStatus(Cnst.PLAYER_LINE_STATE_INLINE);
//					p.initPlayer(null, null, null, Cnst.PLAYER_STATE_DATING,0,0,0);
				}
			}else{//不存在房间号
//				p.setStatus(Cnst.PLAYER_LINE_STATE_INLINE);
//				p.initPlayer(null, null, null, Cnst.PLAYER_STATE_DATING,0,0,0);
			}
	        TCPGameFunctions.updateRedisData(null,p);
		} 
    }
    
    /**
     * 强制解散房间
     * @param session
     * @param readData
     * @throws Exception
     */
    public static void disRoomForce(IoSession session,ProtocolData readData) throws Exception{
    	JSONObject obj = JSONObject.parseObject(readData.getJsonString());
    	Integer interfaceId = obj.getInteger("interfaceId");
    	Integer roomId = obj.getInteger("roomSn");
    	System.out.println("*******强制解散房间"+roomId);
		if (roomId!=null) {
			RoomResp room = TCPGameFunctions.getRoomRespByRoomId(String.valueOf(roomId));
			if (room!=null) {
				List<Player> players = TCPGameFunctions.getPlayerList(room);
				MessageFunctions.setOverInfo(room,players);
				room.setStatus(Cnst.ROOM_STATE_YJS);
				MessageFunctions.updateDatabasePlayRecord(room);
				MessageFunctions.deleteByKey(Cnst.REDIS_PREFIX_ROOMMAP.concat(String.valueOf(roomId)));//删除房间
				if (players!=null&&players.size()>0) {
					for(Player p:players){
				        p.initPlayer(null,null,null,Cnst.PLAYER_STATE_DATING,0,0,0);
				        MessageFunctions.updateRedisData(null, p);
					}
					for(Player p:players){
				        IoSession se = MinaServerManager.tcpServer.getSessions().get(p.getSessionId());
                        if(se!=null&&se.isConnected()){
                            MessageFunctions.interface_100100(se, new ProtocolData(100100,"{\"interfaceId\":\"100100\",\"userId\":\""+p.getUserId()+"\"}"));
                        }
			        }
				}

		        BackFileUtil.write(null, 100103, room,null,null);//写入文件内容
			}else{
				System.out.println("*******强制解散房间"+roomId+"，房间不存在");
			}
		}

        Map<String,Object> info = new HashMap<>();
        info.put("reqState",Cnst.REQ_STATE_1);
		JSONObject result = MessageFunctions.getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
		
    }
    
    
    
    
    
    
    /**
     * 在线房间列表
     * @param session
     * @param readData
     */
    public static void onLineRooms(IoSession session,ProtocolData readData){
    	JSONObject obj = JSONObject.parseObject(readData.getJsonString());
    	Integer interfaceId = obj.getInteger("interfaceId");
    	Integer roomId = obj.getInteger("roomSn");
    	Integer page = obj.getInteger("page");
    	Map<String,Object> infos = new HashMap<String, Object>();
		List<Map<String,Object>> rooms = new ArrayList<>();
		int pages = 0;//总页数
		try {
			if (roomId==null||roomId.equals("")) {
				Set<String> keys = TCPGameFunctions.getSameKeys(Cnst.REDIS_PREFIX_ROOMMAP);
				if (keys!=null&&keys.size()>0) {
					pages = keys.size()%10==0?keys.size()/10:keys.size()/10+1;
					int startNum = 0;
					int endNum = 0;
					if (page==1) {
						startNum = 1;
						endNum = 10;
					}else{
						startNum = 10*(page-1)+1;
						endNum = 10*page;
					}
					
					int num = 0;
					for(String key:keys){
						num++;
						if (num>endNum) {
							break;
						}
						if (num>=startNum&&num<=endNum) {
							Map<String,Object> oneRoom = new HashMap<String, Object>();
							RoomResp room = TCPGameFunctions.getRoomRespByRoomId(key.replace(Cnst.REDIS_PREFIX_ROOMMAP, ""));
							if (room!=null&&!room.getStatus().equals(Cnst.ROOM_STATE_YJS)) {
								List<Player> players = new ArrayList<Player>();
								if (room!=null) {
									Long[] pids = room.getPlayerIds();
									if (pids!=null&&pids.length>0) {
										for(Long pid:pids){
											Player p = TCPGameFunctions.getPlayerByUserId(String.valueOf(pid));
											if (p!=null) {
												players.add(p);
											}
										}
									}
								}
								oneRoom.put("roomInfo", room);
								oneRoom.put("playersInfo", players);
								rooms.add(oneRoom);
							}
							
						}
					}
				}
			}else{
				pages = 1;
				Map<String,Object> oneRoom = new HashMap<String, Object>();
				RoomResp room = TCPGameFunctions.getRoomRespByRoomId(String.valueOf(roomId));
				if (room!=null&&!room.getStatus().equals(Cnst.ROOM_STATE_YJS)) {
					List<Player> players = new ArrayList<Player>();
					if (room!=null) {
						Long[] pids = room.getPlayerIds();
						if (pids!=null&&pids.length>0) {
							for(Long pid:pids){
								Player p = TCPGameFunctions.getPlayerByUserId(String.valueOf(pid));
								if (p!=null) {
									players.add(p);
								}
							}
						}
					}
					oneRoom.put("roomInfo", room);
					oneRoom.put("playersInfo", players);
					rooms.add(oneRoom);
				}
				
			}
			
		} catch (Exception e) {

		}
		infos.put("pages", pages);
		infos.put("rooms", rooms);
		JSONObject result = MessageFunctions.getJSONObj(interfaceId,1,infos);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }
    
    /**
     * 在线房间数以及在线人数
     * @param session
     * @param readData
     */
    public static void onLineNum(IoSession session,ProtocolData readData){
    	JSONObject obj = JSONObject.parseObject(readData.getJsonString());
    	Integer interfaceId = obj.getInteger("interfaceId");
    	ScheduledTask.cleanUserEveryHour();//执行清理
    	Map<Long, IoSession> maps = MinaServerManager.tcpServer.getSessions();
    	Map<String,Object> info = new HashMap<>();
    	if (maps!=null) {
    		info.put("userNum", maps.size());
		}
    	Set<String> keys = TCPGameFunctions.getSameKeys(Cnst.REDIS_PREFIX_ROOMMAP);
    	if (keys!=null) {
    		info.put("roomNum", keys.size());
		}

		JSONObject result = MessageFunctions.getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }

    public static void beneficiate(IoSession s, int protocol_num) {
        log.I("s.getCurrentWriteRequest() --> " + s.getFilterChain());
        log.I("s.getRemoteAddress() --> " + s.getRemoteAddress());
        log.I("s.getServiceAddress() --> " + s.getServiceAddress());
        log.I("请 求 进 来 :"
                + "\n\tinterfaceId -> [ " + protocol_num + " ]");
    }
}
