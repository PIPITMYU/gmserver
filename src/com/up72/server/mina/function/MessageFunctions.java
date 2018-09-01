package com.up72.server.mina.function;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.mina.core.session.IoSession;

import com.alibaba.fastjson.JSONObject;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.PlayerRecord;
import com.up72.game.dto.resp.RoomResp;
import com.up72.server.mina.bean.InfoCount;
import com.up72.server.mina.bean.ProtocolData;
import com.up72.server.mina.main.MinaServerManager;
import com.up72.server.mina.utils.BackFileUtil;
import com.up72.server.mina.utils.MahjongUtils;

/**
 * Created by Administrator on 2017/7/10. 推送消息类
 */
public class MessageFunctions extends TCPGameFunctions {

	/**
	 * 发送玩家信息
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100100(IoSession session, ProtocolData readData)
			throws Exception {
		JSONObject obj = JSONObject.parseObject(readData.getJsonString());
		Integer interfaceId = obj.getInteger("interfaceId");
		Map<String, Object> info = new HashMap<>();
		if (interfaceId.equals(100100)) {// 刚进入游戏主动请求
			String openId = obj.getString("openId");
			Player currentPlayer = null;
			String cid = null;
			if (openId == null) {
				Long userId = obj.getLong("userId");
				if (userId == null) {
					illegalRequest(interfaceId, session);
					return;
				} else {
					currentPlayer = getPlayerByUserId(String.valueOf(session
							.getAttribute(Cnst.USER_SESSION_USER_ID)));
				}
			} else {
				String ip = (String) session.getAttribute(Cnst.USER_SESSION_IP);
				cid = obj.getString("cId");
				currentPlayer = HallFunctions.getPlayerInfos(openId, ip, cid,
						session);
			}
			if (currentPlayer == null) {
				illegalRequest(interfaceId, session);
				return;
			}

			// 更新心跳为最新上线时间
			currentPlayer.setLastHeartTimeLong(new Date().getTime());
			if (cid != null) {
				currentPlayer.setCid(cid);
			}
			currentPlayer.setSessionId(session.getId());// 更新sesisonId
			session.setAttribute(Cnst.USER_SESSION_USER_ID,
					currentPlayer.getUserId());
			if (openId != null) {
				setUserIdByOpenId(openId,
						String.valueOf(currentPlayer.getUserId()));
			}

			RoomResp room = null;
			List<Player> players = null;
			if (currentPlayer.getRoomId() != null) {// 玩家下有roomId，证明在房间中
				room = getRoomRespByRoomId(String.valueOf(currentPlayer
						.getRoomId()));
				if (room != null
						&& !room.getStatus().equals(Cnst.ROOM_STATE_YJS)) {
					info.put("roomInfo", getRoomInfo(room));
					players = getPlayerList(room);
					for (int m = 0; m < players.size(); m++) {
						Player p = players.get(m);
						if (p.getUserId().equals(currentPlayer.getUserId())) {
							players.set(m, currentPlayer);
							break;
						}
					}

					List<Map<String, Object>> anotherUsers = new ArrayList<>();
					for (Player pp : players) {
						if (!pp.getUserId().equals(currentPlayer.getUserId())) {
							anotherUsers.add(getAnotherUsers(pp));
						}
					}
					info.put("anotherUsers", anotherUsers);

				} else {
					currentPlayer.initPlayer(null, null, null,
							Cnst.PLAYER_STATE_DATING, 0, 0, 0);
				}
			}

			updateRedisData(room, currentPlayer);
			info.put("currentUser", getCurrentUserMap(currentPlayer));

			if (room != null) {
				info.put("wsw_sole_main_id", room.getWsw_sole_main_id());
				info.put("wsw_sole_action_id", room.getWsw_sole_action_id());
				Map<String, Object> roomInfo = (Map<String, Object>) info
						.get("roomInfo");
				List<Map<String, Object>> anotherUsers = (List<Map<String, Object>>) info
						.get("anotherUsers");

				info.remove("roomInfo");
				info.remove("anotherUsers");

				JSONObject result = getJSONObj(interfaceId, 1, info);
				ProtocolData pd = new ProtocolData(interfaceId,
						result.toJSONString());
				session.write(pd);

				info.remove("currentUser");
				info.put("roomInfo", roomInfo);
				result = getJSONObj(interfaceId, 1, info);
				pd = new ProtocolData(interfaceId, result.toJSONString());
				session.write(pd);

				info.remove("roomInfo");
				info.put("anotherUsers", anotherUsers);
				result = getJSONObj(interfaceId, 1, info);
				pd = new ProtocolData(interfaceId, result.toJSONString());
				session.write(pd);

				MessageFunctions.interface_100109(players,
						Cnst.PLAYER_LINE_STATE_INLINE,
						currentPlayer.getUserId(), session,
						currentPlayer.getPlayStatus());
			} else {
				JSONObject result = getJSONObj(interfaceId, 1, info);
				ProtocolData pd = new ProtocolData(interfaceId,
						result.toJSONString());
				session.write(pd);
			}
		} else if (interfaceId != 100100) {
			Player currentPlayer = getPlayerByUserId(String.valueOf(session
					.getAttribute(Cnst.USER_SESSION_USER_ID)));
			if (currentPlayer == null) {// 如果session中，没有用户，关闭连接
				session.close(true);
				return;
			}
			RoomResp room = getRoomRespByRoomId(String.valueOf(currentPlayer
					.getRoomId()));
			List<Player> players = getPlayerList(room);
			for (int m = 0; m < players.size(); m++) {
				Player p = players.get(m);
				if (p.getUserId().equals(currentPlayer.getUserId())) {
					players.set(m, currentPlayer);
					break;
				}
			}

			room.setWsw_sole_main_id(room.getWsw_sole_main_id() + 1);
			updateRedisData(room, null);
			for (Player p : players) {
				info = new HashMap<>();
				info.put("wsw_sole_main_id", room.getWsw_sole_main_id());
				info.put("wsw_sole_action_id", room.getWsw_sole_action_id());

				info.put("roomInfo", getRoomInfo(room));
				JSONObject result = getJSONObj(100100, 1, info);
				ProtocolData pd = new ProtocolData(interfaceId,
						result.toJSONString());
				IoSession se = MinaServerManager.tcpServer.getSessions().get(
						p.getSessionId());
				if (se != null && se.isConnected()) {
					se.write(pd);
				}
				info.remove("roomInfo");

				info.put("currentUser", getCurrentUserMap(p));
				result = getJSONObj(100100, 1, info);
				pd = new ProtocolData(interfaceId, result.toJSONString());
				if (se != null && se.isConnected()) {
					se.write(pd);
				}
				info.remove("currentUser");

				List<Map<String, Object>> anotherUsers = new ArrayList<>();
				for (Player ops : players) {
					if (!ops.getUserId().equals(p.getUserId())) {
						anotherUsers.add(getAnotherUsers(ops));
					}
				}
				info.put("anotherUsers", anotherUsers);
				result = getJSONObj(100100, 1, info);
				pd = new ProtocolData(interfaceId, result.toJSONString());
				if (se != null && se.isConnected()) {
					se.write(pd);
				}
				info.remove("anotherUsers");
			}
		} else {
			session.close(true);
		}

	}

	// 封装房间信息
	public static Map<String, Object> getRoomInfo(RoomResp room) {
		Map<String, Object> roomInfo = new HashMap<>();
		roomInfo.put("userId", room.getCreateId());
		roomInfo.put("openName", room.getOpenName());
		roomInfo.put("roomSn", room.getRoomId());
		roomInfo.put("status", room.getStatus());
		roomInfo.put("lastNum", room.getLastNum()-1);
		roomInfo.put("totalNum", room.getCircleNum());
		roomInfo.put("maxScore", room.getMaxScoreInRoom());
		roomInfo.put("status", room.getStatus());
		roomInfo.put("cnrrMJNum", room.getCurrentMjList() == null ? 0 : room
				.getCurrentMjList().size());
		roomInfo.put("circleWind", room.getCircleWind());
		roomInfo.put("roomType", room.getRoomType());
		roomInfo.put("lastPai", room.getLastPai());
		roomInfo.put("lastUserId", room.getLastUserId());
		roomInfo.put("ct", room.getCreateTime());
		roomInfo.put("xjst", room.getXiaoJuStartTime());
		roomInfo.put("dissolveRoom", room.getDissolveRoom());
		roomInfo.put("maxNum", room.getMaxNum());
		return roomInfo;
	}

	// 封装其他玩家信息
	private static Map<String, Object> getAnotherUsers(Player p) {
		Map<String, Object> anotherUsers = new HashMap<>();
		anotherUsers.put("userId", p.getUserId());
		anotherUsers.put("gender", p.getGender());
		anotherUsers.put("position", p.getPosition());
		anotherUsers.put("score", p.getScore());
		anotherUsers.put("money", p.getMoney());
		anotherUsers.put("status", p.getStatus());
		anotherUsers.put("playStatus", p.getPlayStatus());
		anotherUsers.put("openName", p.getUserName());
		anotherUsers.put("openImg", p.getUserImg());
		anotherUsers.put("ip", p.getIp());
		anotherUsers.put("joinIndex", p.getJoinIndex());
		anotherUsers.put("zhuang", p.getZhuang());
		anotherUsers.put("needFaPai", p.getNeedFaPai());
		anotherUsers.put("kouTing",p.getKouTing());
		if (p.getLastFaPai() != null) {
			anotherUsers.put("lastFaPai", new Integer[][] { { -1, -1 } });
		}
		Map<String, Object> paiInfos = new HashMap<>();
		paiInfos.put("currentMjList", p.getCurrentMjList() == null ? null : p
				.getCurrentMjList().size());
		paiInfos.put("chuList", p.getChuList());
		paiInfos.put("chiList", p.getChiList());
		paiInfos.put("pengList", p.getPengList());
		paiInfos.put("gangListType4", p.getGangListType4());
		paiInfos.put("gangListType3", p.getGangListType3());
		paiInfos.put("gangListType5", p.getGangListType5());
		if (p.getGangListType5() == null) {
			paiInfos.put("gangListType5", null);
		} else {
			List<Map<String, String>> list = new ArrayList<>();
			for (InfoCount info : p.getGangListType5()) {
				Map<String, String> map = new HashMap<>();
				map.put("t", info.getT().toString());
				map.put("l", null);
				list.add(map);
			}
			paiInfos.put("gangListType5", list);
		}

		anotherUsers.put("paiInfos", paiInfos);
		return anotherUsers;
	}

	// 封装当前玩家信息
	public static Map<String, Object> getCurrentUserMap(Player p) {
		Map<String, Object> currentUser = new HashMap<>();
		currentUser.put("version", String.valueOf(Cnst.version));
		currentUser.put("userId", p.getUserId());
		currentUser.put("position", p.getPosition());
		currentUser.put("score", p.getScore());
		currentUser.put("status", p.getStatus());
		currentUser.put("playStatus", p.getPlayStatus());
		currentUser.put("openName", p.getUserName());
		currentUser.put("gender", p.getGender());
		currentUser.put("openImg", p.getUserImg());
		currentUser.put("ip", p.getIp());
		currentUser.put("userAgree", p.getUserAgree());
		currentUser.put("money", p.getMoney());
		currentUser.put("joinIndex", p.getJoinIndex());
		currentUser.put("notice", p.getNotice());
		currentUser.put("actions", p.getCurrentActions());
		currentUser.put("zhuang", p.getZhuang());
		currentUser.put("lastFaPai", p.getLastFaPai());
		currentUser.put("needFaPai", p.getNeedFaPai());
		currentUser.put("kouTing",p.getKouTing());
		// 牌的信息
		Map<String, Object> paiInfos = new HashMap<>();
		paiInfos.put("currentMjList", p.getCurrentMjList());
		paiInfos.put("chuList", p.getChuList());
		paiInfos.put("chiList", p.getChiList());
		paiInfos.put("pengList", p.getPengList());
		paiInfos.put("gangListType4", p.getGangListType4());
		paiInfos.put("gangListType3", p.getGangListType3());
		paiInfos.put("gangListType5", p.getGangListType5());
		currentUser.put("paiInfos", paiInfos);
		return currentUser;
	}

	private static Map<String, Object> getPaiInfo(Player p, boolean isSelf) {
		Map<String, Object> paiInfos = new HashMap<>();
		paiInfos.put("chuList", p.getChuList());
		paiInfos.put("chiList", p.getChiList());
		paiInfos.put("pengList", p.getPengList());
		paiInfos.put("gangListType4", p.getGangListType4());
		paiInfos.put("gangListType3", p.getGangListType3());
		if (isSelf) {
			paiInfos.put("currentMjList", p.getCurrentMjList());
			paiInfos.put("gangListType5", p.getGangListType5());
		} else {
			paiInfos.put("currentMjList", p.getCurrentMjList() == null ? null
					: p.getCurrentMjList().size());
			List<Map<String, String>> list = new ArrayList<>();
			for (InfoCount info : p.getGangListType5()) {
				Map<String, String> map = new HashMap<>();
				map.put("t", info.getT().toString());
				map.put("l", null);
				list.add(map);
			}
			paiInfos.put("gangListType5", list);
		}
		return paiInfos;

	}

	/**
	 * 发牌推送
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100101(IoSession session, ProtocolData readData)
			throws Exception {
		JSONObject obj = JSONObject.parseObject(readData.getJsonString());
		Integer interfaceId = obj.getInteger("interfaceId");
		Player currentPlayer = getPlayerByUserId(String.valueOf(session
				.getAttribute(Cnst.USER_SESSION_USER_ID)));
		RoomResp room = getRoomRespByRoomId(String.valueOf(currentPlayer
				.getRoomId()));
		List<Player> players = getPlayerList(room);
		for (int m = 0; m < players.size(); m++) {
			if (players.get(m).getUserId().equals(currentPlayer.getUserId())) {
				currentPlayer = players.get(m);
			}
		}

		room.setWsw_sole_action_id(room.getWsw_sole_action_id() + 1);
		room.setLastPengGangUser(null);// 发牌时，把抢杠胡的东西置空

		if (room.getCurrentMjList().size() == 0) {

			Map<String, Object> info = new HashMap<>();
			info.put("reqState", Cnst.REQ_STATE_1);

			JSONObject result = getJSONObj(interfaceId, 1, info);
			ProtocolData pd = new ProtocolData(interfaceId,
					result.toJSONString());
			session.write(pd);
			if (room.getStatus().equals(Cnst.ROOM_STATE_XJS)) {
				return;
			}
			liuJu(currentPlayer.getRoomId(), session);
			return;
		}

		currentPlayer.setNeedFaPai(false);
		currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_CHU);// 出牌状态

		Integer[][] pai = MahjongUtils.faPai(room.getCurrentMjList(), 1).get(0);
		currentPlayer.setLastFaPai(pai);
		currentPlayer.setZhuaPaiNum(currentPlayer.getZhuaPaiNum() + 1);
		boolean hasAction = GameFunctions.checkActions(currentPlayer, pai,
				false, currentPlayer, room, players);
		if (hasAction) {
			currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
		}
		updateRedisData(room, currentPlayer);

		Map<String, Object> users = new HashMap<String, Object>();

		for (int i = 0; i < players.size(); i++) {
			Player p = players.get(i);
			Map<String, Object> info = new HashMap<>();
			if (p.getUserId().equals(currentPlayer.getUserId())) {// 给自己的信息
				info.put("userId", String.valueOf(p.getUserId()));
				info.put("pai", pai);
				if (hasAction) {
					Map<String, Object> actionInfo = new HashMap<String, Object>();
					actionInfo.put("actions", p.getCurrentActions());
					actionInfo.put("userId", p.getUserId());
					info.put("actionInfo", actionInfo);
				}
			} else {
				if (hasAction) {
					info.put("actionInfo", 1);
				}
				info.put("userId", String.valueOf(currentPlayer.getUserId()));
			}

			info.put("needFaPai", p.getNeedFaPai());

			info.put("playStatus", p.getPlayStatus());
			info.put("mjNum", room.getCurrentMjList().size());
			info.put("wsw_sole_main_id", room.getWsw_sole_main_id());
			info.put("wsw_sole_action_id", room.getWsw_sole_action_id());

			JSONObject result = getJSONObj(100101, 1, info);
			ProtocolData pd = new ProtocolData(100101, result.toJSONString());
			IoSession se = MinaServerManager.tcpServer.getSessions().get(
					p.getSessionId());

			// 写文件用
			if (p.getUserId().equals(currentPlayer.getUserId())) {
				users.put("faPaiUser", info);
			}

			if (se != null && se.isConnected()) {
				se.write(pd);
			}
		}

		BackFileUtil.write(null, 100101, room, players, users);// 写入文件内容
	}

	/**
	 * 流局结算（小）
	 */
	public static void liuJu(Integer roomId, IoSession session) {
		RoomResp room = getRoomRespByRoomId(String.valueOf(roomId));
		room.setStatus(Cnst.ROOM_STATE_XJS);
		List<Player> players = getPlayerList(room);
		Map<String, Object> info = new HashMap<>();
		// Integer lastNum = room.getLastNum();
		// room.setLastNum(lastNum - 1);
		room.setLastHuang(true);
		List<Map<String, Object>> userInfos = new ArrayList<>();
		for (Player p : players) {
			p.setPlayStatus(Cnst.PLAYER_STATE_XJS);
			Map<String, Object> map = new HashMap<>();
			map.put("userId", p.getUserId());
			map.put("currentMjList", p.getCurrentMjList());
			map.put("chuList", p.getChuList());
			map.put("chiList", p.getChiList());
			map.put("pengList", p.getPengList());
			map.put("gangListType4", p.getGangListType4());
			map.put("gangListType3", p.getGangListType3());
			map.put("gangListType5", p.getGangListType5());
			map.put("isWin", false);
			map.put("isDian", false);
			// 流局没有分
			map.put("winScore", 0);
			map.put("gangScore", 0);
			map.put("pengScore", 0);
			p.setScore(p.getScore());
			map.put("score", p.getScore());
			userInfos.add(map);
		}
		info.put("userInfos", userInfos);
		Integer maxNum = room.getMaxNum();
		//流局，连庄，全是不会发生改变
		//2,3人麻将会改变
		Integer lastCircle = room.getLastNum();
		if(maxNum<4){
			//获取剩余局数
			//获取庄的的位置
			Integer position=0;
			for (Player p : players) {
				p.getUserId().equals(room.getZhuangId());
				position=p.getPosition();
			}
			//玩家是否庄
			if (position == MahjongUtils.getLastPosition(maxNum)) {//2,3人麻将
				room.setZhuangId(players.get(0).getUserId());
				Integer circleWind = room.getCircleWind();
				if (circleWind == MahjongUtils.getLastPosition(maxNum)) {//2,3人麻将
					circleWind = 1;
				} else {
					circleWind++;
				}
				room.setCircleWind(circleWind);
			} else {
				if(maxNum==2){//2人麻将，位置为1,3
					room.setZhuangId(players.get(maxNum-1).getUserId());
				}else{//3人麻将
					room.setZhuangId(players.get(position).getUserId());
				}
			}
			//设置庄的人
			for (Player p : players) {
				if (p.getUserId().equals(room.getZhuangId())) {
					p.setZhuang(true);
				} else {
					p.setZhuang(false);
				}
			}
			//局数改变
			lastCircle--;
			room.setLastNum(lastCircle);
			if (lastCircle - 1 < 0) {
				room.setStatus(Cnst.ROOM_STATE_YJS);
			} else {
				room.setStatus(Cnst.ROOM_STATE_XJS);
			}
//			setOverInfo(room, players);
			if (room.getStatus().equals(Cnst.ROOM_STATE_YJS)) {
				room.setHasInsertRecord(true);
				if (room.getStatus().equals(Cnst.ROOM_STATE_YJS)) {
			        room.setHasInsertRecord(true);
			     // 说明是俱乐部创建房间
		 			if (null != room.getClubId()
		 					&& String.valueOf(room.getRoomId()).length() > 6) {
		 				// 向数据库添加 玩家积分记录
		 				updateClubDatabasePlayRecord(room);
		 			} else {
		 				// 向数据库添加 玩家积分记录
		 				updateDatabasePlayRecord(room);
		 			}
				}
			}
		}else{
			//4人麻将不会改变
//			lastCircle = room.getLastNum();
//			info.put("lastNum", lastCircle-1 );
		}
		info.put("lastNum", lastCircle-1 );

		setOverInfo(room, players);
		if (room.getStatus().equals(Cnst.ROOM_STATE_YJS)) {
			room.setHasInsertRecord(true);
			updateDatabasePlayRecord(room);
		}
		ProtocolData pd = null;
		for (Player p : players) {
			// 清空玩家的牌数据
			p.initPlayer(p.getRoomId(), p.getPosition(), p.getZhuang(),
					p.getPlayStatus(), p.getScore(), p.getHuNum(),
					p.getLoseNum());

			Integer interfaceId = 100102;
			JSONObject result = getJSONObj(interfaceId, 1, info);
			pd = new ProtocolData(interfaceId, result.toJSONString());
			IoSession se = MinaServerManager.tcpServer.getSessions().get(
					p.getSessionId());
			if (se != null && se.isConnected()) {
				se.write(pd);
			}
			updateRedisData(null, p);
		}

		room.initRoom();
		updateRedisData(room, null);
		BackFileUtil.write(pd, 100102, room, players, info);// 写入文件内容

	}

	/**
	 * 胡牌结算（小）
	 */
	public static void hu(Player huUser, Player toUser, Integer[][] pai,
			IoSession session, List<Player> players) {
		RoomResp room = getRoomRespByRoomId(String.valueOf(huUser.getRoomId()));
		Map<Integer, Integer> fenMap;
		Map<Integer, Integer> fanMap;
		Map<String, HashMap<Integer, Integer>> checkHuInfo = MahjongUtils.checkHuInfo(huUser,
				toUser, pai, players, room);
		//记录分数
		fenMap=checkHuInfo.get("fenMap");
		//获取番数
		fanMap = checkHuInfo.get("fanMap");
		Integer fenShu=0;
		Integer fanShu=0;
		updateRedisData(null, huUser);
		//显示胡发
		StringBuffer huInfo = new StringBuffer();
		//处理基本分
		Boolean hasHZ=false;
		Set<Integer> fenSet = fenMap.keySet();
		//多写一次是为了让红中放在前面
		for (Integer integer : fenSet) {
			if(integer.equals(Cnst.HUTYPE_HONGZHONG)){
				huInfo.append(integer + "_");
				hasHZ=true;
			}
		}
		for (Integer integer : fenSet) {
			if(!integer.equals(Cnst.HUTYPE_HONGZHONG)){
				huInfo.append(integer + "_");
				fenShu+= fenMap.get(integer);
			}
		}
		if(hasHZ){
			fenShu=fenShu*Cnst.HU_FAN__HONGZHONG;
		}
		//处理番数
		if(fanMap.size()!=0){
			Set<Integer> fanSet = fanMap.keySet();
			for (Integer integer : fanSet) {
				huInfo.append(integer + "_");
				fanShu += fanMap.get(integer);
			}
		}
		//通过番数获取倍数
		fanShu = getScoreFromFan(fanShu);
		//对牌进行处理
		MahjongUtils.paiXu(huUser.getCurrentMjList());
		huUser.getCurrentMjList().add(pai);// 加上赢得那张牌
		// 获取房间封顶分
		Integer maxScore = room.getMaxScoreInRoom();
		// 判断是否自摸，用以计算杠分
		boolean zimo = false;
		if (huUser.getUserId().equals(toUser.getUserId())) {// 自摸
			zimo = true;
		} else {// 点炮，设置最后一张牌为别人出的牌
			huUser.setLastFaPai(pai);
		}
		// 设置输的玩家分数
		Integer shuFen = fenShu*fanShu;
		// 设置赢得玩家输的番数
		Integer winHuFen = 0;
		// 处理输的玩家分数
		for (Player p : players) {
			if (zimo) {// 是自摸
				if (huUser.getZhuang()) {// 赢的人是庄
					if (!p.getUserId().equals(huUser.getUserId())) {// 不是胡的人
						//庄的话，输的分数翻倍
						Integer fen=shuFen*2;
						if(fen>maxScore){//输的玩家翻倍
							fen=maxScore;
						}
						//输的玩家扣，数钱翻倍
						if(p.getKouTing()==2){
							fen=fen*2;
						}
						p.setHuScore(-fen);
						winHuFen += fen;
					}
				} else {// 赢的人不是庄家
					if (!p.getUserId().equals(huUser.getUserId())) {// 不是胡的人
						if (p.getZhuang()) {// 输的人是庄
							Integer fen = shuFen*2 ;// 输的玩家翻倍
							if (fen > maxScore) {
								fen = maxScore;
							}
							if(p.getKouTing()==2){
								fen=fen*2;
							}
							p.setHuScore(-fen);
							winHuFen += fen;
						} else {
							Integer fen = shuFen;
							if (fen > maxScore) {
								fen = maxScore;
							}
							if(p.getKouTing()==2){
								fen=fen*2;
							}
							p.setHuScore(-fen);
							winHuFen += fen;
						}
					}
				}
			} else {// 点炮
				if (huUser.getZhuang()) {// 赢的人是庄
					if (!p.getUserId().equals(huUser.getUserId())) {// 不是胡的人
						if (p.getIsDian()) {// 是输的人
							Integer fen = shuFen*2 ;// 输的玩家翻倍
							if (fen > maxScore) {
								fen = maxScore;
							}
							if(p.getKouTing()==2){
								fen=fen*2;
							}
							p.setHuScore(-fen);
							winHuFen += fen;
						}
					}
				} else {// 赢的人不是庄家
					if (!p.getUserId().equals(huUser.getUserId())) {// 不是胡的人
						if (p.getIsDian()) {// 是输的人
							if (p.getZhuang()) {// 输的人是庄
								Integer fen = shuFen*2 ;// 输的玩家翻倍
								if (fen > maxScore) {
									fen = maxScore;
								}
								if(p.getKouTing()==2){
									fen=fen*2;
								}
								p.setHuScore(-fen);
								winHuFen += fen;
							} else {
								Integer fen = shuFen;// 输的玩家
								if (fen > maxScore) {
									fen = maxScore;
								}
								if(p.getKouTing()==2){
									fen=fen*2;
								}
								p.setHuScore(-fen);
								winHuFen += fen;
							}
						}
					}
				}
			}
		}
		huUser.setHuScore(winHuFen);
		Map<String, Object> info = new HashMap<>();
		List<Map<String, Object>> userInfos = new ArrayList<>();
		Player zhuangUser = null;
		for (Player p : players) {
			if (p.getZhuang()) {
				zhuangUser = p;
			}
			Map<String, Object> userInfo = new HashMap<>();
			userInfo.put("userId", p.getUserId());
			userInfo.put("currentMjList", p.getCurrentMjList());
			userInfo.put("chuList", p.getChuList());
			userInfo.put("chiList", p.getChiList());
			userInfo.put("pengList", p.getPengList());
			userInfo.put("gangListType4", p.getGangListType4());
			userInfo.put("gangListType3", p.getGangListType3());
			userInfo.put("gangListType5", p.getGangListType5());
			userInfo.put("isWin", p.getIsHu());
			userInfo.put("isDian", p.getIsDian());
			userInfo.put("winScore", p.getHuScore());
			p.setScore(p.getScore() + p.getHuScore());
			userInfo.put("score", p.getScore());
 			if (p.getIsHu()) {
				userInfo.put("winInfo",
						huInfo.substring(0, huInfo.length() - 1));
				//如果有封顶番，显示封顶番
				userInfo.put("fanInfo", shuFen>maxScore?maxScore:shuFen);
				if (p.getHuNum() == null) {
					p.setHuNum(1);
				} else {
					p.setHuNum(p.getHuNum() + 1);
				}
			} else {
				if (p.getLoseNum() == null) {
					p.setLoseNum(1);
				} else {
					p.setLoseNum(p.getLoseNum() + 1);
				}
			}
			userInfos.add(userInfo);
		}
		info.put("userInfos", userInfos);
		//获取房间圈数
		Integer maxNum = room.getMaxNum();
		Integer lastCircle = room.getLastNum();
		Integer position=0;
		//2,3人麻将是根据局数取计算（庄不能连续坐）
		if(maxNum<4){//说明是2，3人麻将
			//房间的圈风
			position=zhuangUser.getPosition();
			//玩家是否庄
			if (position == MahjongUtils.getLastPosition(maxNum)) {//2,3人麻将
				room.setZhuangId(players.get(0).getUserId());
				Integer circleWind = room.getCircleWind();
				if (circleWind == MahjongUtils.getLastPosition(maxNum)) {//2,3人麻将
					circleWind = 1;
				} else {
					circleWind++;
				}
				room.setCircleWind(circleWind);
			} else {
				if(maxNum==2){//2人麻将，位置为1,3
					room.setZhuangId(players.get(maxNum-1).getUserId());
				}else{//3人麻将
					room.setZhuangId(players.get(position).getUserId());
				}
			}
			//设置庄的人
			for (Player p : players) {
				if (p.getUserId().equals(room.getZhuangId())) {
					p.setZhuang(true);
				} else {
					p.setZhuang(false);
				}
			}
			//局数改变
			lastCircle--;
		}else{
			if (!huUser.getZhuang()) {//下面只可能是4人麻将了
				// 关于位置，赢得人是庄，连庄，位置不变
				position = zhuangUser.getPosition();
//			if (position == 4) {
				if (position == MahjongUtils.getLastPosition(maxNum)) {//2,3人麻将
					//轮了一圈，圈数改变
					lastCircle--;
					room.setZhuangId(players.get(0).getUserId());
					Integer circleWind = room.getCircleWind();
//				if (circleWind == 4) {
					if (circleWind == MahjongUtils.getLastPosition(maxNum)) {//2,3人麻将
						circleWind = 1;
					} else {
						circleWind++;
					}
					room.setCircleWind(circleWind);
				} else {
					room.setZhuangId(players.get(position).getUserId());
				}
				for (Player p : players) {
					if (p.getUserId().equals(room.getZhuangId())) {
						p.setZhuang(true);
					} else {
						p.setZhuang(false);
					}
				}
			}
		}
		room.setLastNum(lastCircle);
		info.put("lastNum", lastCircle - 1);
		if (lastCircle - 1 < 0) {
			room.setStatus(Cnst.ROOM_STATE_YJS);
		} else {
			room.setStatus(Cnst.ROOM_STATE_XJS);
		}

		setOverInfo(room, players);
		
		if (room.getStatus().equals(Cnst.ROOM_STATE_YJS)) {
			room.setHasInsertRecord(true);
			if (room.getStatus().equals(Cnst.ROOM_STATE_YJS)) {
		        room.setHasInsertRecord(true);
		     // 说明是俱乐部创建房间
	 			if (null != room.getClubId()
	 					&& String.valueOf(room.getRoomId()).length() > 6) {
	 				// 向数据库添加 玩家积分记录
	 				updateClubDatabasePlayRecord(room);
	 			} else {
	 				// 向数据库添加 玩家积分记录
	 				updateDatabasePlayRecord(room);
	 			}
			}
		}
		ProtocolData pd = null;
		
		for (Player p : players) {
			// 清空玩家的牌数据
			p.initPlayer(p.getRoomId(), p.getPosition(), p.getZhuang(),
					p.getPlayStatus(), p.getScore(), p.getHuNum(),
					p.getLoseNum());

			IoSession se = MinaServerManager.tcpServer.getSessions().get(
					p.getSessionId());
			if (se != null && se.isConnected()) {
				JSONObject result = getJSONObj(100102, 1, info);
				pd = new ProtocolData(100102, result.toJSONString());
				se.write(pd);
			}
			updateRedisData(null, p);
		}
		room.initRoom();
		updateRedisData(room, null);
		BackFileUtil.write(pd, 100102, room, players, info);// 写入文件内容
	}

	//通过番获取分数
	public static Integer getScoreFromFan(Integer fanShu){
		Integer score=1;
		if(fanShu>0){//番数是大于0的
			for (int i = 1; i <=fanShu; i++) {
				score*=2;
			}
		}
		
		return score;
	}
	
	
	
	public static void main(String[] args) {
		Integer scoreFromFan = getScoreFromFan(4);
		System.out.println(scoreFromFan);
	}
	
	
	
	
	
	
	
	public static void setOverInfo(RoomResp room, List<Player> players) {
		List<Map<String, Object>> overInfoOld = room.getOverInfo();
		List<Map<String, Object>> xiaoJieSuanInfo = new ArrayList<Map<String, Object>>();

		List<Map<String, Object>> overInfo = new ArrayList<Map<String, Object>>();
		if (players != null && players.size() > 0) {
			for (int i = 0; i < players.size(); i++) {
				Player p = players.get(i);
				Map<String, Object> info = new HashMap<String, Object>();
				info.put("userId", p.getUserId());
				info.put("score", p.getScore());
				info.put("huNum", p.getHuNum() == null ? 0 : p.getHuNum());
				info.put("loseNum", p.getLoseNum() == null ? 0 : p.getLoseNum());
				info.put("dianNum", p.getDianNum() == null ? 0 : p.getDianNum());
				info.put("zhuangNum",
						p.getZhuangNum() == null ? 0 : p.getZhuangNum());
				info.put("zimoNum", p.getZimoNum() == null ? 0 : p.getZimoNum());
				info.put("position", p.getPosition());
				info.put("xjn", room.getXiaoJuNum());
				overInfo.add(info);

				Map<String, Object> xjsi = new LinkedHashMap<String, Object>();
				xjsi.put("openName", p.getUserName());
				xjsi.put("openImg", p.getUserImg());
				if (overInfoOld == null) {
					xjsi.put("score", p.getScore());
				} else {
					xjsi.put("score",p.getScore()- (Integer) overInfoOld.get(i).get("score"));
				}
				xiaoJieSuanInfo.add(xjsi);
			}
		}
		room.setOverInfo(null);
		room.setOverInfo(overInfo);

		BackFileUtil.writeXiaoJieSuanInfo(room, xiaoJieSuanInfo);// 写入文件

	}

	/**
	 * 大结算
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100103(IoSession session, ProtocolData readData) {
		JSONObject obj = JSONObject.parseObject(readData.getJsonString());
		Integer interfaceId = obj.getInteger("interfaceId");
		Integer roomId = obj.getInteger("roomSn");
		RoomResp room = getRoomRespByRoomId(String.valueOf(roomId));
		List<Map<String, Object>> info = new ArrayList<>();

		List<Map<String, Object>> overInfoList = room.getOverInfo();
		if (overInfoList != null && overInfoList.size() > 0) {
			for (Map<String, Object> infoMap : overInfoList) {
				info.add(infoMap);
			}
			JSONObject result = getJSONObj(interfaceId, 1, info);
			ProtocolData pd = new ProtocolData(interfaceId,
					result.toJSONString());
			System.err.println(pd);
			session.write(pd);

			Player p = getPlayerByUserId(String.valueOf(session
					.getAttribute(Cnst.USER_SESSION_USER_ID)));
			p.initPlayer(null, null, null, Cnst.PLAYER_STATE_DATING, 0, 0, 0);

			if (room.getOutNum() == null) {
				room.setOutNum(1);
			} else {
				room.setOutNum(room.getOutNum() + 1);
			}

			if (room.getHasInsertRecord() == null || !room.getHasInsertRecord()) {
				room.setHasInsertRecord(true);
				if (null != room.getClubId()
						&& String.valueOf(room.getRoomId()).length() > 6) {
					// 向数据库添加 玩家积分记录
					updateClubDatabasePlayRecord(room);
				} else {
					// 向数据库添加 玩家积分记录
					updateDatabasePlayRecord(room);
				}
			}
			updateRedisData(room, p);
			
//			if (room.getOutNum() == 4) {
			if (room.getOutNum() == room.getMaxNum()) {//2,3人麻将----可能有问题
				deleteByKey(Cnst.REDIS_PREFIX_ROOMMAP.concat(String
						.valueOf(roomId)));
				room = null;
			}
		}
	}

	public static void updateDatabasePlayRecord(RoomResp room) {
		if (room == null)
			return;
		Integer roomId = room.getRoomId();
		// 刷新数据库
		roomService.updateRoomState(roomId);

		PlayerRecord playerRecord = new PlayerRecord();
		playerRecord.setRoomId(roomId);
		playerRecord.setStartTime(String.valueOf(room.getCreateTime()));
		playerRecord.setEndTime(String.valueOf(new Date().getTime()));

		List<Map<String, Object>> overInfoList = room.getOverInfo();
		if (overInfoList != null && overInfoList.size() > 0) {
			for (Map<String, Object> infoMap : overInfoList) {
				if ((int) (infoMap.get("position")) == Cnst.WIND_EAST) {
					playerRecord.setEastUserId(String.valueOf(infoMap
							.get("userId")));
					playerRecord.setEastUserMoneyRecord((int) infoMap
							.get("score"));
					playerRecord.setEastUserMoneyRemain((int) infoMap
							.get("score"));
				} else if ((int) (infoMap.get("position")) == Cnst.WIND_SOUTH) {
					playerRecord.setSouthUserId(String.valueOf(infoMap
							.get("userId")));
					playerRecord.setSouthUserMoneyRecord((int) infoMap
							.get("score"));
					playerRecord.setSouthUserMoneyRemain((int) infoMap
							.get("score"));
				} else if ((int) (infoMap.get("position")) == Cnst.WIND_WEST) {
					playerRecord.setWestUserId(String.valueOf(infoMap
							.get("userId")));
					playerRecord.setWestUserMoneyRecord((int) infoMap
							.get("score"));
					playerRecord.setWestUserMoneyRemain((int) infoMap
							.get("score"));
				} else if ((int) (infoMap.get("position")) == Cnst.WIND_NORTH) {
					playerRecord.setNorthUserId(String.valueOf(infoMap
							.get("userId")));
					playerRecord.setNorthUserMoneyRecord((int) infoMap
							.get("score"));
					playerRecord.setNorthUserMoneyRemain((int) infoMap
							.get("score"));
				} else {
					return;
				}
			}
		} else {
			return;
		}
		userService.insertPlayRecord(playerRecord);
	}

	/**
	 * 给其他玩家推送动作提示
	 * 
	 * @param players
	 * @param userId
	 * @param action
	 */
	public static void interface_100104(List<Player> players, Long userId,
			Integer action, Long toUserId, Integer gangType, IoSession session) {

		Integer interfaceId = 100104;
		Player actionUser = null;
		Player toUser = new Player();
		Player hasActionUser = null;
		List<Map<String, Object>> playStatusInfo = new ArrayList<Map<String, Object>>();
		for (Player p : players) {
			Map<String, Object> pi = new HashMap<String, Object>();
			pi.put("userId", p.getUserId());
			pi.put("playStatus", p.getPlayStatus());
			//设置扣听状态
			pi.put("kouTing", p.getKouTing());
			playStatusInfo.add(pi);
			if (p.getUserId().equals(userId)) {
				actionUser = p;
			}
			if (p.getUserId().equals(toUserId)) {
				toUser = p;
			}
			if (p.getCurrentActions() != null
					&& p.getCurrentActions().size() > 0) {
				hasActionUser = p;
			}
		}
		Integer[][] fuyi = new Integer[][] { { -1, -1 } };

		Map<String, Object> users = new HashMap<String, Object>();
		RoomResp room = getRoomRespByRoomId(String.valueOf(actionUser
				.getRoomId()));
		for (Player p : players) {
			IoSession se = MinaServerManager.tcpServer.getSessions().get(
					p.getSessionId());
			if (se != null && se.isConnected()) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("userId", userId);
				map.put("action", action);
				map.put("wsw_sole_main_id", room.getWsw_sole_main_id());
				map.put("wsw_sole_action_id", room.getWsw_sole_action_id());

				if (action == 4) {
					map.put("huType", userId.equals(toUserId) ? 1 : 2);
				}

				// 传递自己牌的信息
				if (p.getUserId().equals(userId)) {
					map.put("lastFaPai", p.getLastFaPai());
					map.put("paiInfos", getPaiInfo(actionUser, true));
				} else {
					map.put("lastFaPai", fuyi);
					map.put("paiInfos", getPaiInfo(actionUser, false));
				}

				// 过杠胡会传牌信息
				if (room.getLastPengGangUser() != null
						&& (action.equals(Cnst.ACTION_HU))) {// 过喜碰和过喜过才进
					if (p.getUserId().equals(toUserId)) {
						map.put("topaiInfos", getPaiInfo(toUser, true));
					} else {
						map.put("topaiInfos", getPaiInfo(toUser, false));
					}
				} else {// 不是不传牌信息
					map.put("topaiInfos", null);
				}

				if (hasActionUser != null ) {// 还有玩家继续有动作 或者是扣听
//				if (hasActionUser != null ) {// 还有玩家继续有动作 
					if (hasActionUser.getUserId().equals(p.getUserId())) {
						Map<String, Object> actionInfo = new HashMap<String, Object>();
						actionInfo.put("actions", p.getCurrentActions());
						actionInfo.put("userId", p.getUserId());
						map.put("actionInfo", actionInfo);
					} else {
						map.put("actionInfo", 1);
					}
				}
				map.put("toUserId", toUserId);
				map.put("needFaPai", p.getNeedFaPai());
				map.put("playStatusInfo", playStatusInfo);

				JSONObject result = getJSONObj(interfaceId, 1, map);
				ProtocolData pd = new ProtocolData(interfaceId,
						result.toJSONString());

				// 写文件用
				if (map.containsKey("actionInfo")
						&& !(map.get("actionInfo") instanceof Integer)) {
					users.put("hasActionUser", map);
				} else if (p.getUserId().equals(userId)) {
					users.put("actionUser", map);
				} else if (p.getNeedFaPai()) {
					users.put("needFaUser", map);
				}
				se.write(pd);
			}
		}
		BackFileUtil.write(null, interfaceId, room, players, users);// 写入文件内容
	}

	/**
	 * 给其他玩家推送出牌提示 如果actionPlayer==null的话，所有玩家都没有动作
	 * 
	 * @param others
	 * @param userId
	 * @param paiInfo
	 */
	public static void interface_100105(Long userId, Integer[][] paiInfo,
			Integer roomId, Player actionPlayer, RoomResp room,
			List<Player> players) {
		Integer interfaceId = 100105;

		Map<String, Object> users = new HashMap<String, Object>();

		/* 给其他玩家推送出牌提示 */
		for (Player p : players) {
			Map<String, Object> map = new JSONObject();
			map.put("userId", userId);
			map.put("paiInfo", paiInfo);
			map.put("wsw_sole_main_id", room.getWsw_sole_main_id());
			map.put("wsw_sole_action_id", room.getWsw_sole_action_id());
			map.put("needFaPai", p.getNeedFaPai());
			map.put("playStatus", p.getPlayStatus());

			if (actionPlayer != null) {
				if (actionPlayer.getUserId().equals(p.getUserId())) {
					Map<String, Object> actionInfo = new HashMap<String, Object>();
					actionInfo.put("actions", p.getCurrentActions());
					actionInfo.put("userId", p.getUserId());
					map.put("actionInfo", actionInfo);
				} else {
					map.put("actionInfo", 1);
				}
			}

			JSONObject result = getJSONObj(interfaceId, 1, map);
			ProtocolData pd = new ProtocolData(interfaceId,
					result.toJSONString());
			IoSession se = MinaServerManager.tcpServer.getSessions().get(
					p.getSessionId());
			// 写文件用
			if (map.containsKey("actionInfo")
					&& !(map.get("actionInfo") instanceof Integer)) {
				//因为下面是elseif 判断，所以自己加一个扣听时候的操作
				if(p.getKouMark()){//此时是判断自己的的扣听动作
					if (p.getUserId().equals(userId)) {
						Map<String, Object> map1 = new JSONObject();
						map1.put("userId", userId);
						map1.put("paiInfo", paiInfo);
						map1.put("wsw_sole_main_id", room.getWsw_sole_main_id());
						map1.put("wsw_sole_action_id", room.getWsw_sole_action_id());
						map1.put("needFaPai", p.getNeedFaPai());
						map1.put("playStatus", p.getPlayStatus());
						users.put("chuUser", map1);
					}
				}
				users.put("hasActionUser", map);
			} else if (p.getUserId().equals(userId)) {
					users.put("chuUser", map);
			} else if (p.getNeedFaPai()) {
				users.put("needFaUser", map);
			}
			if (se != null && se.isConnected()) {
				se.write(pd);
			}
		}
		BackFileUtil.write(null, interfaceId, room, players, users);// 写入文件内容

	}

	/**
	 * 多地登陆提示
	 * 
	 * @param session
	 */
	public static void interface_100106(IoSession session) {
		Integer interfaceId = 100106;
		JSONObject result = getJSONObj(interfaceId, 1, "out");
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
		session.close(true);
	}

	/**
	 * 玩家被踢/房间被解散提示
	 * 
	 * @param session
	 */
	public static void interface_100107(Long userId, String type,
			List<Player> players) {
		Integer interfaceId = 100107;
		Map<String, Object> info = new HashMap<String, Object>();

		if (players == null || players.size() == 0) {
			return;
		}
		info.put("userId", userId);
		info.put("type", type);

		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		for (Player p : players) {
			IoSession se = MinaServerManager.tcpServer.getSessions().get(
					p.getSessionId());
			if (se != null && se.isConnected()) {
				se.write(pd);
			}
		}
	}

	/**
	 * 方法id不符合
	 * 
	 * @param session
	 */
	public static void interface_100108(IoSession session) {
		Integer interfaceId = 100108;
		Map<String, Object> info = new HashMap<String, Object>();
		info.put("reqState", Cnst.REQ_STATE_9);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
	}

	/**
	 * 用户离线/上线提示
	 * 
	 * @param state
	 */
	public static void interface_100109(List<Player> players, String status,
			Long userId, IoSession session, String playState) {
		Integer interfaceId = 100109;
		Map<String, Object> info = new HashMap<String, Object>();
		info.put("userId", userId);
		info.put("status", status);
		info.put("playStatus", playState);

		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());

		if (players != null && players.size() > 0) {
			for (Player p : players) {
				if (p != null && !p.getUserId().equals(userId)) {
					IoSession se = MinaServerManager.tcpServer.getSessions()
							.get(p.getSessionId());
					if (se != null && se.isConnected()) {
						se.write(pd);
					}
				}

			}
		}
	}

	/**
	 * 后端主动解散房间推送
	 * 
	 * @param reqState
	 * @param players
	 */
	public static void interface_100111(int reqState, List<Player> players,
			Integer roomId) {
		Integer interfaceId = 100111;
		Map<String, Object> info = new HashMap<String, Object>();
		info.put("reqState", reqState);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		if (players != null && players.size() > 0) {
			for (Player p : players) {
				if (p.getRoomId() != null && p.getRoomId().equals(roomId)) {
					IoSession se = MinaServerManager.tcpServer.getSessions()
							.get(p.getSessionId());
					if (se != null && se.isConnected()) {
						se.write(pd);
					}
				}
			}
		}

	}
	   //俱乐部
    //俱乐部相关
    /**
	 * 俱乐部 向数据库添加玩家分数信息
	 */
	public static void updateClubDatabasePlayRecord(RoomResp room) {
		if (room == null)
			return;
		Integer roomId = room.getRoomId();
		// 刷新数据库
		clubGameRoomService.updateRoomState(roomId, Long.parseLong(room.getCreateTime()), room.getXiaoJuNum());
		PlayerRecord playerRecord = new PlayerRecord();
		playerRecord.setRoomId(roomId);
		playerRecord.setClubId(room.getClubId());// 俱乐部id
		playerRecord.setStartTime(String.valueOf(room.getCreateTime()));
		playerRecord.setEndTime(String.valueOf(new Date().getTime()));
		//删除记录分数的key
		Long[] playerIds = room.getPlayerIds();
//		for (Long long1 : playerIds) {
//			deleteJedisScore(String.valueOf(long1));
//		}
		List<Map<String, Object>> overInfoList = room.getOverInfo();
		if (overInfoList != null && overInfoList.size() > 0) {
			for (Map<String, Object> infoMap : overInfoList) {
				if ((int) (infoMap.get("position")) == Cnst.WIND_EAST) {
					playerRecord.setEastUserId(String.valueOf(infoMap
							.get("userId")));
					
					playerRecord.setEastUserMoneyRecord((int) infoMap
							.get("score"));
					playerRecord.setEastUserMoneyRemain((int) infoMap
							.get("score"));
				} else if ((int) (infoMap.get("position")) == Cnst.WIND_SOUTH) {
					
					playerRecord.setSouthUserId(String.valueOf(infoMap
							.get("userId")));
					playerRecord.setSouthUserMoneyRecord((int) infoMap
							.get("score"));
					playerRecord.setSouthUserMoneyRemain((int) infoMap
							.get("score"));
				} else if ((int) (infoMap.get("position")) == Cnst.WIND_WEST) {
					
					playerRecord.setWestUserId(String.valueOf(infoMap
							.get("userId")));
					playerRecord.setWestUserMoneyRecord((int) infoMap
							.get("score"));
					playerRecord.setWestUserMoneyRemain((int) infoMap
							.get("score"));
				} else if ((int) (infoMap.get("position")) == Cnst.WIND_NORTH) {
					
					playerRecord.setNorthUserId(String.valueOf(infoMap
							.get("userId")));
					playerRecord.setNorthUserMoneyRecord((int) infoMap
							.get("score"));
					playerRecord.setNorthUserMoneyRemain((int) infoMap
							.get("score"));
				} else {
					return;
				}
			}
		} else {
			return;
		}
		clubGamePlayRecordService.insertPlayRecord(playerRecord);
	}
    
    
    
}
