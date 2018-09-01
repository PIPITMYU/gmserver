package com.up72.server.mina.function;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.mina.core.session.IoSession;

import redis.clients.jedis.Jedis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.leo.rms.utils.StringUtils;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.ClubInfo;
import com.up72.game.dto.resp.ClubUserUse;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.game.model.PlayerMoneyRecord;
import com.up72.game.model.Room;
import com.up72.game.service.IClubGamePlayRecordService;
import com.up72.game.service.IClubGameRoomService;
import com.up72.game.service.IClubInfoService;
import com.up72.game.service.IClubUserService;
import com.up72.game.service.IClubUserUseService;
import com.up72.game.service.IRoomService;
import com.up72.game.service.IUserService;
import com.up72.game.service.IUserService_login;
import com.up72.game.service.impl.ClubGamePlayRecordServiceImpl;
import com.up72.game.service.impl.ClubGameRoomServiceImpl;
import com.up72.game.service.impl.ClubInfoServiceImpl;
import com.up72.game.service.impl.ClubUserServiceImpl;
import com.up72.game.service.impl.ClubUserUseServiceImpl;
import com.up72.game.service.impl.RoomServiceImpl;
import com.up72.game.service.impl.UserServiceImpl;
import com.up72.game.service.impl.UserService_loginImpl;
import com.up72.server.mina.bean.ProtocolData;
import com.up72.server.mina.utils.CommonUtil;
import com.up72.server.mina.utils.MyLog;
import com.up72.server.mina.utils.PostUtil;
import com.up72.server.mina.utils.TaskUtil;
import com.up72.server.mina.utils.redis.MyRedis;

public class TCPGameFunctions {

    public static final MyLog logger = MyLog.getLogger(TCPGameFunctions.class);
    public static IUserService userService = new UserServiceImpl();
    public static IUserService_login userService_login = new UserService_loginImpl();
    public static IRoomService roomService = new RoomServiceImpl();
    
//    public static ConcurrentHashMap<Integer, RoomResp> roomMap = new ConcurrentHashMap<>(); //房间信息
//    public static ConcurrentHashMap<Long, Integer> userRoomNumberMap = new ConcurrentHashMap<>(); //用户房间号码信息
//    public static ConcurrentHashMap<Integer, List<Player>> roomUserMap = new ConcurrentHashMap<>(); //房间人员信息
//    public static ConcurrentHashMap<Long, IoSession> ioSessionMap = new ConcurrentHashMap<>(); //玩家——session数据
//    public static ConcurrentHashMap<String, Long> openIdUserMap = new ConcurrentHashMap<>(); //openId-user数据

    //由于需要线程notify，需要保存线程的锁，所以保留这两个静态变量
    //独立id，对应相对的任务，无论什么type的任务，id是唯一的
    public static ConcurrentHashMap<Integer, TaskUtil.DissolveRoomTask> disRoomIdMap = new ConcurrentHashMap<>(); //解散房间的任务
    //如果房间开局或者解散时没超过5分钟就有结果了，才会向这个集合里放数据，数据格式为id--1
    public static ConcurrentHashMap<Integer, Integer> disRoomIdResultInfo = new ConcurrentHashMap<>(); //房间解散状态集合
    
    
 // 俱乐部相关
 	public static IClubInfoService clubInfoService = new ClubInfoServiceImpl();
 	public static IClubUserService clubUserService = new ClubUserServiceImpl();
 	public static IClubUserUseService clubUserUseService = new ClubUserUseServiceImpl();
 	public static IClubGameRoomService clubGameRoomService = new ClubGameRoomServiceImpl();
 	public static IClubGamePlayRecordService clubGamePlayRecordService = new ClubGamePlayRecordServiceImpl();
    
    
    /**
     * 更新redis中的数据
     * @param players
     * @param room
     * @param player
     */
    public synchronized static boolean updateRedisData(RoomResp room,Player player){
    	boolean result = true;
    	if (room!=null) {
//    		deleteByKey(Cnst.REDIS_PREFIX_ROOMMAP.concat(String.valueOf(room.getRoomId())));
    		result = setRoomRespByRoomId(String.valueOf(room.getRoomId()), room);
		}
    	if (player!=null) {
//    		deleteByKey(Cnst.REDIS_PREFIX_USER_ID_USER_MAP.concat(String.valueOf(player.getUserId())));
    		result = setPlayerByUserId(player);
		}
    	return result;
    }
    
    /**
     * 通过房间号或者房中的玩家id列表，去redis中找齐所有玩家并返回
     * @param roomInfo
     * @return
     */
    public synchronized static List<Player> getPlayerList(Object roomInfo){
    	List<Player> players = new ArrayList<Player>();
    	RoomResp room = null;
    	if (roomInfo instanceof Integer) {//传入的是roomId
    		room = getRoomRespByRoomId(String.valueOf(roomInfo));
			
		}else if(roomInfo instanceof RoomResp){//传入的是roomResp对象
			room = (RoomResp) roomInfo;
		}
    	if (room!=null) {
			Jedis jedis = null;
	    	try {
	    		Long[] userIds = room.getPlayerIds();
	    		jedis = MyRedis.getRedisClient().getJedis();
				for(int i=0;i<userIds.length;i++){
					if (userIds[i]!=null) {
			    		Player p = (Player) JSON.parseObject(jedis.get(Cnst.REDIS_PREFIX_USER_ID_USER_MAP.concat(String.valueOf(userIds[i]))), Player.class);
						if (p!=null) {
							players.add(p);
						}
					}
				}
			

			} catch (Exception e) {
				MyRedis.getRedisClient().returnBrokenJedis(jedis);
				e.printStackTrace();
			}finally{
				if (jedis!=null) {
					MyRedis.getRedisClient().returnJedis(jedis);
				}
			}
			
		}
    	return players!=null&&players.size()>0?players:null;
    }
    
    
    /************************通过对应key值get对应对象***************************/
    
    public synchronized static String getStringByKey(String key){
    	String value = null;
    	Jedis jedis = null;
    	try {
    		jedis = MyRedis.getRedisClient().getJedis();
    		value = jedis.get(key);
		} catch (Exception e) {
			e.printStackTrace();
			MyRedis.getRedisClient().returnBrokenJedis(jedis);
		}finally{
			if (jedis!=null) {
				MyRedis.getRedisClient().returnJedis(jedis);
			}
		}
    	return value;
    }
    
    public synchronized static Set<String> getSameKeys(String patten){
    	Set<String> keys = null;
    	Jedis jedis = null;
    	try {
    		jedis = MyRedis.getRedisClient().getJedis();
    		keys = jedis.keys(patten.concat("*"));
		} catch (Exception e) {
			e.printStackTrace();
			MyRedis.getRedisClient().returnBrokenJedis(jedis);
		}finally{
			if (jedis!=null) {
				MyRedis.getRedisClient().returnJedis(jedis);
			}
		}
    	Set<String> result = null;
    	if (keys!=null&&keys.size()>0) {
    		result = new HashSet<String>();
			for(String str:keys){
				str = str.replace(patten, "");
				result.add(str);
			}
		}
    	return result;
    }
    
    /**
     * 通过userId从redis中获取玩家信息
     * @param userId
     * @return
     */
    public synchronized static Player getPlayerByUserId(String userId){
    	Player player = null;
    	Jedis jedis = null;
    	try {
    		jedis = MyRedis.getRedisClient().getJedis();
    		player = (Player) JSON.parseObject(jedis.get(Cnst.REDIS_PREFIX_USER_ID_USER_MAP.concat(userId)), Player.class);
		} catch (Exception e) {
			e.printStackTrace();
			MyRedis.getRedisClient().returnBrokenJedis(jedis);
		}finally{
			if (jedis!=null) {
				MyRedis.getRedisClient().returnJedis(jedis);
			}
		}
    	return player;
    }
    
    
    /**
     * 通过roomId从redis中获取房间信息
     * @param roomId
     * @return
     */
    public synchronized static RoomResp getRoomRespByRoomId(String roomId){
    	RoomResp room = null;
    	Jedis jedis = null;
    	try {
    		jedis = MyRedis.getRedisClient().getJedis();
    		room = JSON.parseObject(jedis.get(Cnst.REDIS_PREFIX_ROOMMAP.concat(roomId)),RoomResp.class);
		} catch (Exception e) {
			e.printStackTrace();
			MyRedis.getRedisClient().returnBrokenJedis(jedis);
		}finally{
			if (jedis!=null) {
				MyRedis.getRedisClient().returnJedis(jedis);
			}
		}
    	return room;
    }
    
    
    /**
     * 通过openId获取玩家的userId
     * @param openId
     * @return
     */
    public synchronized static Long getUserIdByOpenId(String openId){
    	Long userId = null;
    	Jedis jedis = null;
    	try {
    		jedis = MyRedis.getRedisClient().getJedis();
    		userId = Long.valueOf(jedis.get(Cnst.REDIS_PREFIX_OPENIDUSERMAP.concat(openId)));
		} catch (Exception e) {
			e.printStackTrace();
			MyRedis.getRedisClient().returnBrokenJedis(jedis);
		}finally{
			if (jedis!=null) {
				MyRedis.getRedisClient().returnJedis(jedis);
			}
		}
    	return userId;
    }
    
    
    
    
    

    /************************通过对应key值set对应对象***************************/
    
    public synchronized static boolean setPlayersList(List<Player> players){
    	boolean result = true;

		Jedis jedis = MyRedis.getRedisClient().getJedis();
		
    	try {
    		if (players!=null&&players.size()>0) {
				for(Player p:players){
					jedis.set(Cnst.REDIS_PREFIX_USER_ID_USER_MAP.concat(String.valueOf(p.getUserId())), JSON.toJSONString(p));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			MyRedis.getRedisClient().returnBrokenJedis(jedis);
			result = false;
		}finally{
			if (jedis!=null) {
				MyRedis.getRedisClient().returnJedis(jedis);
			}
		}
    	
    	
    	return result;
    }
    
    
    public synchronized static boolean setStringByKey(String key,String value){
    	boolean result = true;
    	Jedis jedis = null;
    	try {
    		jedis = MyRedis.getRedisClient().getJedis();
    		jedis.set(StringUtils.getBytes(key), StringUtils.getBytes(value));
		} catch (Exception e) {
			result = false;
			MyRedis.getRedisClient().returnBrokenJedis(jedis);
		}finally{
			if (jedis!=null) {
				MyRedis.getRedisClient().returnJedis(jedis);
			}
		}
    	return result;
    }
    
    /**
     * 通过roomId从redis中获取房间信息
     * @param roomId
     * @return
     */
    public synchronized static boolean setRoomRespByRoomId(String roomId,RoomResp room){
    	boolean result = true;
    	Jedis jedis = null;
    	try {
    		jedis = MyRedis.getRedisClient().getJedis();
    		jedis.set(Cnst.REDIS_PREFIX_ROOMMAP.concat(roomId), JSON.toJSONString(room));
		} catch (Exception e) {
			result = false;
			MyRedis.getRedisClient().returnBrokenJedis(jedis);
		}finally{
			if (jedis!=null) {
				MyRedis.getRedisClient().returnJedis(jedis);
			}
		}
    	return result;
    }
    
    /**
     * 通过roomId从redis中获取房间信息
     * @param roomId
     * @return
     */
    public synchronized static boolean setPlayerByUserId(Player player){
    	boolean result = true;
    	Jedis jedis = null;
    	try {
    		jedis = MyRedis.getRedisClient().getJedis();
    		jedis.set(Cnst.REDIS_PREFIX_USER_ID_USER_MAP.concat(String.valueOf(player.getUserId())), JSON.toJSONString(player));
		} catch (Exception e) {
			result = false;
			MyRedis.getRedisClient().returnBrokenJedis(jedis);
		}finally{
			if (jedis!=null) {
				MyRedis.getRedisClient().returnJedis(jedis);
			}
		}
    	return result;
    }
    
    /**
     * 通过openId获取玩家的userId
     * @param openId
     * @return
     */
    public synchronized static boolean setUserIdByOpenId(String openId,String userId){
    	boolean result = true;
    	Jedis jedis = null;
    	try {
    		jedis = MyRedis.getRedisClient().getJedis();
    		jedis.set(Cnst.REDIS_PREFIX_OPENIDUSERMAP.concat(openId), userId);
		} catch (Exception e) {
			result = false;
			MyRedis.getRedisClient().returnBrokenJedis(jedis);
		}finally{
			if (jedis!=null) {
				MyRedis.getRedisClient().returnJedis(jedis);
			}
		}
    	return result;
    }
    
    
    public synchronized static boolean deleteByKey(String key){
    	boolean result = true;
    	Jedis jedis = null;
    	try {
    		jedis = MyRedis.getRedisClient().getJedis();
    		jedis.del(StringUtils.getBytes(key));
		} catch (Exception e) {
			result = false;
			MyRedis.getRedisClient().returnBrokenJedis(jedis);
		}finally{
			if (jedis!=null) {
				MyRedis.getRedisClient().returnJedis(jedis);
			}
		}
    	return result;
    }
    
    
    
    
    

    public static void function_1(IoSession session, ProtocolData readData) {
        try {
            session.getService().broadcast(readData);
        } catch (Exception e) {
            logger.E("function_1 异常", e);
        }
    }

    /**
     * 微信登录
     *
     * @throws IOException
     */
    public static String getThirdLoginPath() throws IOException {
        int appId = 0;
        String path = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + appId + "&redirect_uri=" + URLEncoder.encode("http://zhyxxy.zhlm.com/zhibo/jsp/wx/common/wx/zbAutoLoginAuthorize.jsp") + "&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect";
        return path;
    }

    /**
     * 获取统一格式的返回obj
     * @param interfaceId
     * @param state
     * @param object
     * @return
     */
    public static JSONObject getJSONObj(Integer interfaceId,Integer state,Object object){
        JSONObject obj = new JSONObject();
        obj.put("interfaceId",interfaceId);
        obj.put("state",state);
        obj.put("message","");
        obj.put("info",object);
        obj.put("others","");
        return obj;
    }


    /**
     * 房间不存在
     * @param interfaceId
     * @param session
     */
    public static void roomDoesNotExist(Integer interfaceId,IoSession session){
        Map<String,Object> info = new HashMap<>();
        info.put("reqState", Cnst.REQ_STATE_4);
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }

    /**
     * 玩家在其他房间
     * @param interfaceId
     * @param session
     */
    public static void playerExistOtherRoom(Integer interfaceId,IoSession session){
        Map<String,Object> info = new HashMap<>();
        info.put("reqState",Cnst.REQ_STATE_3);
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }

    /**
     * 房间已满
     * @param interfaceId
     * @param session
     */
    public static void roomFully(Integer interfaceId,IoSession session){
        Map<String,Object> info = new HashMap<>();
        info.put("reqState",Cnst.REQ_STATE_5);
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }

    /**
     * 玩家房卡不足
     * @param interfaceId
     * @param session
     */
    public static void playerMoneyNotEnough(Integer interfaceId,IoSession session,Integer roomType){
        Map<String,Object> info = new HashMap<>();
        info.put("reqState",Cnst.REQ_STATE_2);//余额不足，请及时充值
        info.put("roomType",roomType);//余额不足，请及时充值
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }
    
    /**
     * 代开房间不能超过10个
     * @param interfaceId
     * @param session
     */
    public static void roomEnough(Integer interfaceId,IoSession session){
        Map<String,Object> info = new HashMap<>();
        info.put("reqState",Cnst.REQ_STATE_11);
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }

    /**
     * 非法请求
     * @param session
     * @param interfaceId
     */
    public static void illegalRequest(Integer interfaceId,IoSession session){
        JSONObject result = getJSONObj(interfaceId,0,null);
        result.put("message","非法请求！");
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
        session.close(true);
    }

    /**
     * 游戏中，不能退出房间
     * @param interfaceId
     * @param session
     */
    public static void roomIsGaming(Integer interfaceId,IoSession session){
        Map<String,Object> info = new HashMap<>();
        info.put("reqState",Cnst.REQ_STATE_6);
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }


    /**
     * 开启等待解散房间任务
     * @param roomId
     * @param type
     */
    public static void startDisRoomTask(int roomId,int type){
        RoomResp room = getRoomRespByRoomId(String.valueOf(roomId));
        Integer createDisId = null;
        while(true){
            createDisId = CommonUtil.getGivenRamdonNum(8);
            if (!disRoomIdMap.containsKey(createDisId)) {
				break;
			}
        }
        if (type==Cnst.DIS_ROOM_TYPE_1){
            room.setCreateDisId(createDisId);
        }else if(type==Cnst.DIS_ROOM_TYPE_2){
            room.setApplyDisId(createDisId);
        }
        
        TaskUtil.DissolveRoomTask task = new TaskUtil().new DissolveRoomTask(roomId,type,createDisId);
        disRoomIdMap.put(createDisId, task);
        updateRedisData(room, null);
        new Thread(task).start();
    }

    /**
     * 关闭解散房间任务
     * @param roomId
     * @param type
     */
    public static void notifyDisRoomTask(RoomResp room,int type,boolean needAddRoomToDB){
        if (room==null) {
			return;
		}
        Integer taskId = 0;
        if (type==Cnst.DIS_ROOM_TYPE_1){
            taskId = room.getCreateDisId();
            room.setCreateDisId(null);
        }else if (type==Cnst.DIS_ROOM_TYPE_2){
            taskId = room.getApplyDisId();
            room.setApplyDisId(null);
            notifyDisRoomTask(room, Cnst.DIS_ROOM_TYPE_1,needAddRoomToDB);
        }
        if (taskId==null) {
			return;
		}
        TaskUtil.DissolveRoomTask task = disRoomIdMap.get(taskId);
        disRoomIdResultInfo.put(taskId, Cnst.DIS_ROOM_RESULT);
        if (task!=null) {
        	if (type==Cnst.DIS_ROOM_TYPE_1 && needAddRoomToDB ) {
        		if(null != room.getClubId() && String.valueOf(room.getRoomId()).length() > 6){
        			//首先向数据库添加房间记录
        			addClubRoomToDB(room);
        		}else{
        			//首先向数据库添加房间记录
        			addRoomToDB(room);
        		}
			}
        	//移除解散任务
        	synchronized (task){
        		task.notify();
        	}
		}
    }
    
    public static void addRoomToDB(RoomResp room){
    	List<Player> players = getPlayerList(room);
    	
    	int maxNum = room.getMaxNum();
    	
    	//扣除房主房卡
      	userService.updateMoney(userService.getUserMoneyByUserId(room.getCreateId())-room.getNeedMoney(), String.valueOf(room.getCreateId()));
         
         //添加玩家消费记录
         PlayerMoneyRecord mr = new PlayerMoneyRecord();
         mr.setUserId(room.getCreateId());
         mr.setMoney(room.getNeedMoney());
         mr.setType(100);
         mr.setCreateTime(new Date().getTime());
         userService.insertPlayerMoneyRecord(mr);

         /* 向数据库添加房间信息*/
         Room r = new Room();
         r.setRoomId(room.getRoomId());
         r.setCreateId(room.getCreateId());
         r.setCreateTime(room.getCreateTime());
         r.setIsPlaying(1);
         r.setMaxScore(room.getMaxScoreInRoom());
         r.setRoomType(room.getRoomType());
         r.setCircleNum(room.getCircleNum());
         r.setMaxNum(maxNum);//房间最大
         if(maxNum==2){
        	 r.setUserId1(players.get(0).getUserId());
        	 r.setUserId3(players.get(1).getUserId());
         }else if(maxNum==3){
        	 r.setUserId1(players.get(0).getUserId());
        	 r.setUserId2(players.get(1).getUserId());
        	 r.setUserId3(players.get(2).getUserId());
         }else if(maxNum==4){
        	 r.setUserId1(players.get(0).getUserId());
        	 r.setUserId2(players.get(1).getUserId());
        	 r.setUserId3(players.get(2).getUserId());
        	 r.setUserId4(players.get(3).getUserId());
         }
         roomService.save(r);
         
       //统计消费
 		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
		 			PostUtil.doCount(room.getCreateId(),room.getNeedMoney(),room.getRoomType(),room.getRoomId());
		 		} catch (Exception e) {
		 			System.out.println("调用统计借口出错");
		 			e.printStackTrace();
		 		}finally{
		 			Thread.currentThread().interrupt();
		 		};
			}
		}).start();
         
    }
	// 俱乐部相关

	/**
	 * 参数错误
	 */
	public static void parameterError(Integer interfaceId, IoSession session) {
		JSONObject result = getJSONObj(interfaceId, 0, null);
		result.put("message", "参数错误！");
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
		session.close(true);
	}

	/**
	 * 是否参数错误 跳转链接
	 */
	public static void isParameterError(Integer interfaceId, IoSession session,
			boolean flag) {
		if (!flag) {
			parameterError(interfaceId, session);
			return;
		}
	}

	/**
	 * 验证是否是数字
	 */
	public static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if (!isNum.matches()) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 通过clubId从redis中获取俱乐部信息
	 */
	public synchronized static ClubInfo getClubInfoByClubId(String clubId) {
		ClubInfo info = null;
		Jedis jedis = null;
		try {
			jedis = MyRedis.getRedisClient().getJedis();
			String string = jedis.get(Cnst.REDIS_PREFIX_CLUBMAP.concat(clubId));
			if (string != null) {
				info = JSON.parseObject(string, ClubInfo.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
			MyRedis.getRedisClient().returnBrokenJedis(jedis);
		} finally {
			if (jedis != null) {
				MyRedis.getRedisClient().returnJedis(jedis);
			}
		}
		return info;
	}

	/**
	 * 通过clubId更新redis中俱乐部信息
	 */
	public synchronized static boolean setClubInfoByClubId(String clubId,
			ClubInfo clubInfo) {
		boolean result = true;
		Jedis jedis = null;
		try {
			jedis = MyRedis.getRedisClient().getJedis();
			jedis.set(Cnst.REDIS_PREFIX_CLUBMAP.concat(clubId),
					JSON.toJSONString(clubInfo));
		} catch (Exception e) {
			result = false;
			MyRedis.getRedisClient().returnBrokenJedis(jedis);
		} finally {
			if (jedis != null) {
				MyRedis.getRedisClient().returnJedis(jedis);
			}
		}
		return result;
	}

	/**
	 * 通过clubId获取redis中俱乐部未开局房间信息
	 */
	public synchronized static JSONArray getClubRoomListByClubId(String clubId) {
		Jedis jedis = null;
		JSONArray jsonArray = null;
		try {
			jedis = MyRedis.getRedisClient().getJedis();
			String string = jedis.get(Cnst.REDIS_PREFIX_CLUBMAP_LIST
					.concat(clubId));
			if (string != null) {
				jsonArray = JSONArray.parseArray(string);
			}
		} catch (Exception e) {
			MyRedis.getRedisClient().returnBrokenJedis(jedis);
		} finally {
			if (jedis != null) {
				MyRedis.getRedisClient().returnJedis(jedis);
			}
		}
		return jsonArray;
	}

	/**
	 * 通过clubId更新redis中俱乐部未开局房间信息
	 */
//	public synchronized static boolean setClubRoomListByClubId(String clubId,
//			JSONArray jsonArray) {
//		boolean result = true;
//		Jedis jedis = null;
//		try {
//			jedis = MyRedis.getRedisClient().getJedis();
//			jedis.set(Cnst.REDIS_PREFIX_CLUBMAP_LIST.concat(clubId),
//					JSON.toJSONString(jsonArray));
//		} catch (Exception e) {
//			result = false;
//			MyRedis.getRedisClient().returnBrokenJedis(jedis);
//		} finally {
//			if (jedis != null) {
//				MyRedis.getRedisClient().returnJedis(jedis);
//			}
//		}
//		return result;
//	}

	/**
	 * 获取支付集合
	 * 
	 * @return
	 */
	public synchronized static JSONArray getPayList() {
		Jedis jedis = null;
		JSONArray jsonArray = null;
		try {
			jedis = MyRedis.getRedisClient().getJedis();
			String string = jedis.get(Cnst.REDIS_PAY_ORDERNUM);
			if (string != null) {
				jsonArray = JSONArray.parseArray(string);
			}
		} catch (Exception e) {
			MyRedis.getRedisClient().returnBrokenJedis(jedis);
		} finally {
			if (jedis != null) {
				MyRedis.getRedisClient().returnJedis(jedis);
			}
		}
		return jsonArray;
	}

	/**
	 * 更新redis订单集合
	 * 
	 * @return
	 */
	public synchronized static boolean setPayList(JSONArray array) {
		boolean result = true;
		Jedis jedis = null;
		try {
			jedis = MyRedis.getRedisClient().getJedis();
			jedis.set(Cnst.REDIS_PAY_ORDERNUM, JSON.toJSONString(array));
		} catch (Exception e) {
			result = false;
			MyRedis.getRedisClient().returnBrokenJedis(jedis);
		} finally {
			if (jedis != null) {
				MyRedis.getRedisClient().returnJedis(jedis);
			}
		}
		return result;
	}


	// /**
	// * 初始化俱乐部信息到redis中
	// */
	public synchronized static boolean initClubList() {
		boolean result = true;
		Jedis jedis = null;
		try {
			jedis = MyRedis.getRedisClient().getJedis();
			List<ClubInfo> list = clubInfoService.selectAll();// 查询出所有俱乐部的信息
			for (ClubInfo info : list) {
				jedis.set(Cnst.REDIS_PREFIX_CLUBMAP.concat(String.valueOf(info
						.getClubId())), JSON.toJSONString(info));
			}
		} catch (Exception e) {
			result = false;
			MyRedis.getRedisClient().returnBrokenJedis(jedis);
		} finally {
			if (jedis != null) {
				MyRedis.getRedisClient().returnJedis(jedis);
			}
		}
		return result;
	}
	
	
	public  static void hset(String key, String field, String value,Integer timeout) {
		Jedis jedis = null;
		try {
			jedis = MyRedis.getRedisClient().getJedis();
			jedis.hset(Cnst.REDIS_PREFIX_CLUBMAP_LIST.concat(key), field, value);
			if (timeout!=null) {
				jedis.expire(Cnst.REDIS_PREFIX_CLUBMAP_LIST.concat(key), timeout);
			}
		} catch (Exception e) {
			e.printStackTrace();
			MyRedis.getRedisClient().returnBrokenJedis(jedis);
		} finally {
			if (jedis != null) {
				MyRedis.getRedisClient().returnJedis(jedis);
			}
		}
	}

public  static String hget(String key, String field) {
		String result = null;
		Jedis jedis = null;
		try {
			jedis = MyRedis.getRedisClient().getJedis();
            result = jedis.hget(Cnst.REDIS_PREFIX_CLUBMAP_LIST.concat(key), field);
		} catch (Exception e) {
			e.printStackTrace();
			MyRedis.getRedisClient().returnBrokenJedis(jedis);
		} finally {
			if (jedis != null) {
				MyRedis.getRedisClient().returnJedis(jedis);
			}
		}
		return result;
	}

public  static boolean hdel(String key,String... field){
    	boolean result = true;
    	Jedis jedis = null;
    	try {
    		jedis = MyRedis.getRedisClient().getJedis();
    		jedis.hdel(Cnst.REDIS_PREFIX_CLUBMAP_LIST.concat(key),field);
		} catch (Exception e) {
			result = false;
			MyRedis.getRedisClient().returnBrokenJedis(jedis);
		}finally{
			if (jedis!=null) {
				MyRedis.getRedisClient().returnJedis(jedis);
			}
		}
    	return result;
    }

public  static Map<String,String> hgetAll(String key) {
    Map<String, String> result = null;
	Jedis jedis = null;
	try {
		jedis = MyRedis.getRedisClient().getJedis();
        result = jedis.hgetAll(Cnst.REDIS_PREFIX_CLUBMAP_LIST.concat(key));
	} catch (Exception e) {
		e.printStackTrace();
		MyRedis.getRedisClient().returnBrokenJedis(jedis);
	} finally {
		if (jedis != null) {
			MyRedis.getRedisClient().returnJedis(jedis);
		}
	}
	return result;
}

	 /**
     * 俱乐部 添加房间信息 扣除房卡 添加消费信息
     */
    public static void addClubRoomToDB(RoomResp room){
    	
    
    	
    	TCPGameFunctions.hdel(room.getClubId()+"", String.valueOf(room.getRoomId()));
		int circle = room.getCircleNum();
		//查询俱乐部信息
		ClubInfo club = clubInfoService.selectByClubId(room.getClubId());
		//扣除房卡并保存    如果房间创建房间在线面时间之内，那么不扣房卡
		Long freeEnd = club.getFreeEnd();
		Long createTime = Long.valueOf(room.getCreateTime());
		if(createTime<freeEnd){//限免时间,不需要扣钱
			
		}else{//不是限免时间，需要扣钱
			//扣除房卡并保存
			club.setRoomCardNum(club.getRoomCardNum()-room.getNeedMoney());
			clubInfoService.updateByClubId(club);
		}
		setClubInfoByClubId(room.getClubId().toString(), club);//更新redis数据
		 //添加玩家消费记录
		ClubUserUse clubUserUse = new ClubUserUse();
		clubUserUse.setClubId(room.getClubId());
		clubUserUse.setUserId(room.getCreateId());
		clubUserUse.setRoomId(room.getRoomId());
		clubUserUse.setMoney(room.getNeedMoney());
		clubUserUse.setCreateTime(System.currentTimeMillis());
		clubUserUseService.insert(clubUserUse);

		 /* 向数据库添加房间信息*/
		 HashMap<String, Object> map = new HashMap<String, Object>();
		 map.put("createId", room.getCreateId());
		 map.put("roomId", room.getRoomId());
		 map.put("createTime", room.getCreateTime());
		 map.put("userId1", room.getPlayerIds()[0]);
		 map.put("userId2", room.getPlayerIds()[1]);
		 if(room.getMaxNum()==3){
			 map.put("userId3", room.getPlayerIds()[2]);
		 }else if(room.getMaxNum()==4){
			 map.put("userId3", room.getPlayerIds()[2]);
			 map.put("userId4", room.getPlayerIds()[3]);
		 }
		 map.put("isPlaying", 1);
		 map.put("maxScore", room.getMaxScoreInRoom());
		 map.put("roomType", room.getRoomType());
		 map.put("circleNum", room.getCircleNum());
		 map.put("clubId", room.getClubId());
		 map.put("server_ip", room.getIp());
		 map.put("oneMoney", room.getNeedMoney()/room.getMaxNum());//每个人的耗钱数
		 map.put("maxNum", room.getMaxNum());//房间最大人数
		clubGameRoomService.save(map);	
		new Thread( new Runnable() {
			@Override
			public void run() {
				 //统计消费
				try {
					PostUtil.doCount(room.getCreateId(),room.getNeedMoney(), room.getRoomType(),room.getRoomId());
				} catch (Exception e) {
					System.out.println("调用统计借口出错");
					e.printStackTrace();
				}
				Thread.currentThread().interrupt();
			}
		}).start();
    }
    
    //俱乐部今日活跃和人数
	  //获取0点时间戳
	  	public static long getTimesmorning(){
	  		Calendar cal = Calendar.getInstance();
	  		cal.set(Calendar.HOUR_OF_DAY, 0);
	  		cal.set(Calendar.SECOND, 0);
	  		cal.set(Calendar.MINUTE, 0);
	  		cal.set(Calendar.MILLISECOND, 0);
	  		return cal.getTimeInMillis();
	  	}
	  	//获取24点时间戳
	  	public static long getTimesNight(){
	  		return getTimesmorning()+86400000;
	  	}

		
    
    
    
    
    
    
    
    
    
}