package com.up72.server.mina.function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.mina.core.session.IoSession;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.server.mina.bean.DissolveRoom;
import com.up72.server.mina.bean.InfoCount;
import com.up72.server.mina.bean.ProtocolData;
import com.up72.server.mina.main.MinaServerManager;
import com.up72.server.mina.utils.BackFileUtil;
import com.up72.server.mina.utils.MahjongUtils;

/**
 * Created by Administrator on 2017/7/13. 游戏中
 */

public class GameFunctions extends TCPGameFunctions {

	/**
	 * 用户点击准备，用在小结算那里，
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100200(IoSession session, ProtocolData readData)
			throws Exception {
		logger.I("准备,interfaceId -> 100200");
		JSONObject obj = JSONObject.parseObject(readData.getJsonString());
		Integer interfaceId = obj.getInteger("interfaceId");
		Long userId = obj.getLong("userId");
		Integer roomId = obj.getInteger("roomSn");

		RoomResp room = getRoomRespByRoomId(String.valueOf(roomId));
		Player currentPlayer = null;
		List<Player> players = getPlayerList(room);
		for (Player p : players) {
			if (p.getUserId().equals(userId)) {
				currentPlayer = p;
				break;
			}
		}
		if (currentPlayer == null) {
			return;
		}

		if (room.getStatus().equals(Cnst.ROOM_STATE_GAMIING)) {
			return;
		}
		if (!currentPlayer.getPlayStatus().equals(Cnst.PLAYER_STATE_XJS)
				&& !currentPlayer.getPlayStatus().equals(Cnst.PLAYER_STATE_IN)) {
			return;
		}

		currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_PREPARED);

		boolean allPrepared = true;

		for (Player p : players) {
			if (!p.getPlayStatus().equals(Cnst.PLAYER_STATE_PREPARED)) {
				allPrepared = false;
			}
		}

		room.setCurrentMjList(null);

//		if (allPrepared && players != null && players.size() == 4) {
		if (allPrepared && players != null && players.size() == room.getMaxNum()) {//2,3人麻将
			startGame(room, players);
			BackFileUtil.write(null, interfaceId, room, players, null);// 写入文件内容
		}

		List<Map<String, Object>> info = new ArrayList<Map<String, Object>>();
		for (Player p : players) {
			Map<String, Object> i = new HashMap<String, Object>();
			i.put("userId", p.getUserId());
			i.put("playStatus", p.getPlayStatus());
			info.add(i);
		}
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());

		for (Player p : players) {
			updateRedisData(null, p);
		}
		updateRedisData(room, null);

		for (Player p : players) {
			IoSession se = MinaServerManager.tcpServer.getSessions().get(
					p.getSessionId());
			if (se != null && se.isConnected()) {
				se.write(pd);
			}
		}
	}

	/**
	 * 开局发牌
	 * 
	 * @param roomId
	 */
	public static void startGame(RoomResp room, List<Player> players) {

		// 关闭解散房间计时任务
		notifyDisRoomTask(room, Cnst.DIS_ROOM_TYPE_1,true);

		room.setStatus(Cnst.ROOM_STATE_GAMIING);
		room.setCurrentMjList(MahjongUtils.xiPai(MahjongUtils.initMahjongs()));

		if (room.getXiaoJuNum() == null) {
			room.setXiaoJuNum(1);
		} else {
			room.setXiaoJuNum(room.getXiaoJuNum() + 1);
		}
        room.setXiaoJuStartTime(new Date().getTime());

		for (Player p : players) {
			if (p.getZhuang()) {
				p.setNeedFaPai(true);
			} else {
				p.setNeedFaPai(false);
			}
		}

		for (Player p : players) {
			p.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
			p.setCurrentMjList(MahjongUtils.paiXu(MahjongUtils.faPai(
					room.getCurrentMjList(), 13)));
			if (p.getZhuang()) {
				p.setZhuangNum(p.getZhuangNum() == null ? 1
						: p.getZhuangNum() + 1);
			}
		}

	}

	/**
	 * 出牌
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100201(IoSession session, ProtocolData readData)
			throws Exception {
		logger.I("出牌,interfaceId -> 100201");
		JSONObject obj = JSONObject.parseObject(readData.getJsonString());
		// Integer interfaceId = obj.getInteger("interfaceId");
		Integer roomId = obj.getInteger("roomSn");
		Long userId = obj.getLong("userId");

		RoomResp room = getRoomRespByRoomId(String.valueOf(roomId));
		Player currentPlayer = null;
		List<Player> players = getPlayerList(room);
		for (Player p : players) {
			if (p.getUserId().equals(userId)) {
				currentPlayer = p;
				break;
			}
		}
		if (currentPlayer == null) {
			return;
		}

//		if (!currentPlayer.getPlayStatus().equals(Cnst.PLAYER_STATE_CHU)) {
//			System.err.println("玩家出牌，当前状态不对！");
//			return;
//		}
		// 设置递增id
		Integer wsw_sole_action_id = obj.getInteger("wsw_sole_action_id");
		if (!room.getWsw_sole_action_id().equals(wsw_sole_action_id)) {
			MessageFunctions.interface_100108(session);
			return;
		} else {
			room.setWsw_sole_action_id(wsw_sole_action_id + 1);
		}

//		int size = currentPlayer.getCurrentMjList().size();
//		if (size != 1 && size != 4 && size != 7 && size != 10 && size != 13) {
//			System.err.println("当前出牌用户手牌个数有 误：" + size);
//		}

		Integer[][] paiInfo = getIntegerList(obj.getString("paiInfo"));

		currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
		room.setLastUserId(currentPlayer.getUserId());
		room.setLastPengGangUser(null);// 发牌时，把抢杠胡的参数置空
		room.setLastPai(new Integer[][] { { paiInfo[0][0], paiInfo[0][1] } });
		// 需要检测出的哪张牌是不是发的那个，如果不是，需要把发的哪张牌加入手牌集合
		if (currentPlayer.getLastFaPai() != null
				&& paiInfo[0][0].equals(currentPlayer.getLastFaPai()[0][0])
				&& paiInfo[0][1].equals(currentPlayer.getLastFaPai()[0][1])) {
			currentPlayer.getChuList().add(
					new Integer[][] { { currentPlayer.getLastFaPai()[0][0],
							currentPlayer.getLastFaPai()[0][1] } });

		} else {
 			for (int i = 0; i < currentPlayer.getCurrentMjList().size(); i++) {
				if (currentPlayer.getCurrentMjList().get(i)[0][0]
						.equals(paiInfo[0][0])
						&& currentPlayer.getCurrentMjList().get(i)[0][1]
								.equals(paiInfo[0][1])) {
					currentPlayer.getChuList().add(
							currentPlayer.getCurrentMjList().get(i));
					currentPlayer.getCurrentMjList().remove(i);
					break;
				}
			}
			if (currentPlayer.getLastFaPai() != null) {
				currentPlayer.getCurrentMjList().add(
						new Integer[][] { { currentPlayer.getLastFaPai()[0][0],
								currentPlayer.getLastFaPai()[0][1] } });
				currentPlayer.setCurrentMjList(MahjongUtils.paiXu(currentPlayer
						.getCurrentMjList()));
			}
		}
		currentPlayer.setLastFaPai(null);
		currentPlayer.setNeedFaPai(false);
		currentPlayer.setChuPaiNum(currentPlayer.getChuPaiNum() == null ? 1
				: currentPlayer.getChuPaiNum() + 1);

		Player nextUser = null;
		Integer maxNum = room.getMaxNum();
		//此处可以在cnst设置一个map，通过最大人数，的值获取对应的位置，2特殊，位置改为东1和南3
		if (currentPlayer.getPosition().equals(MahjongUtils.getLastPosition(maxNum))) {
			nextUser = players.get(0);
		} else {
			nextUser = players.get(currentPlayer.getPosition());
		}
		
		List<Player> others = new ArrayList<Player>();
		//设置过杠胡信息
		for (int i = 0; i < players.size(); i++) {
			if (!players.get(i).getUserId().equals(userId)) {// 非当前用户之外的其他三家
				players.get(i).setHasGang(0);// 将其它3家刚刚有杠设置为false
				others.add(players.get(i));
			}
		}
		currentPlayer.setKouMark(false);
//		检测自己的动作
		if(currentPlayer.getKouTing()==1 && MahjongUtils.isMenQing(currentPlayer)){//1：不是扣听状态  2：必须是门清
			if(MahjongUtils.checkTing(currentPlayer)){//自己有动作
				currentPlayer.setKouMark(true);
				Map<String, Object> currentActions = new LinkedHashMap<>();
				currentActions.put(String.valueOf(Cnst.ACTION_TING),  currentPlayer.getUserId());// 胡的动作
				currentActions.put(String.valueOf(Cnst.ACTION_GUO), new ArrayList<>());// 胡的动作
				currentPlayer.setCurrentActions(currentActions);
			}else{
				// 给其他玩家检测动作
				checkOthersAction(others, paiInfo, currentPlayer, nextUser, room,
						players);
			}
		}else{//自己已经扣听，检测其他玩家
			// 给其他玩家检测动作
			checkOthersAction(others, paiInfo, currentPlayer, nextUser, room,
					players);
		}
		String sss=new String() ;
		if(maxNum==2){
			sss = players.get(0).getNeedFaPai() + "_"
					+ players.get(1).getNeedFaPai() ;
		}else if(maxNum==3){
			sss = players.get(0).getNeedFaPai() + "_"
					+ players.get(1).getNeedFaPai() + "_"
					+ players.get(2).getNeedFaPai() ;
			
		}else if (maxNum==4){
			sss = players.get(0).getNeedFaPai() + "_"
					+ players.get(1).getNeedFaPai() + "_"
					+ players.get(2).getNeedFaPai() + "_"
					+ players.get(3).getNeedFaPai();
			
		}
		while (true) {
			for (Player p : players) {
				deleteByKey(Cnst.REDIS_PREFIX_USER_ID_USER_MAP.concat(String
						.valueOf(p.getUserId())));
				updateRedisData(null, p);
			}
			updateRedisData(room, null);
			RoomResp room1 = getRoomRespByRoomId(String.valueOf(roomId));
			List<Player> players1 = getPlayerList(room1);

			String mmm=new String();
			StringBuffer mmm1 = new StringBuffer();
			if(maxNum==2){
				mmm=mmm1.append(players1.get(0).getNeedFaPai()).append("_")
				.append(players1.get(1).getNeedFaPai()).toString();
			}else if(maxNum==3){
				mmm=mmm1.append(players1.get(0).getNeedFaPai()).append("_")
				.append(players1.get(1).getNeedFaPai()).append("_")
				.append(players1.get(2).getNeedFaPai()).toString();
			}else if(maxNum==4){
				mmm=mmm1.append(players1.get(0).getNeedFaPai()).append("_")
				.append(players1.get(1).getNeedFaPai()).append("_")
				.append(players1.get(2).getNeedFaPai()).append("_")
				.append(players1.get(3).getNeedFaPai()).toString();
				
			}
			if (!mmm.equals(sss)) {
				continue;
			} else {
				break;
			}
		}
		if(currentPlayer.getCurrentActions()!=null  && currentPlayer.getCurrentActions().size() > 0 ){
			MessageFunctions.interface_100105(currentPlayer.getUserId(), paiInfo,
					roomId, currentPlayer, room, players);
		}else{
			MessageFunctions.interface_100105(currentPlayer.getUserId(), paiInfo,
					roomId, getActionPlayer(players), room, players);
		}

	}

	public static Player getActionPlayer(List<Player> players) {
		// 在按照优先级移除之后，只有一个玩家有动作
		Player actionPlayer = null;
		for (Player p : players) {
			if (p.getCurrentActions() != null
					&& p.getCurrentActions().size() > 0) {
				actionPlayer = p;
				break;
			}
		}
		return actionPlayer;
	}

	/**
	 * 对比两个玩家的动作，把优先级低的玩家动作清空
	 * 
	 * @param others
	 */
	private static void removeActions(List<Player> others, Player chuUser) {
		a: for (int i = 0; i < others.size(); i++) {
			Player p1 = others.get(i);
			if (p1.getUserId().equals(chuUser.getUserId())) {
				continue;
			}
			if (p1.getCurrentActions() != null
					&& p1.getCurrentActions().size() > 0) {
				for (int j = i + 1; j < others.size(); j++) {
					Player p2 = others.get(j);
					if (p2.getUserId().equals(chuUser.getUserId())) {
						continue;
					}
					if (p2.getCurrentActions() != null
							&& p2.getCurrentActions().size() > 0) {
						Integer p1Act = 0;
						Integer[] p1as = new Integer[p1.getCurrentActions()
								.keySet().size()];
						Integer p2Act = 0;
						Integer[] p2as = new Integer[p2.getCurrentActions()
								.keySet().size()];
						int num = 0;
						for (String act1 : p1.getCurrentActions().keySet()) {
							p1as[num++] = Integer.valueOf(act1);
						}
						num = 0;
						for (String act2 : p2.getCurrentActions().keySet()) {
							p2as[num++] = Integer.valueOf(act2);
						}
						Arrays.sort(p1as);
						Arrays.sort(p2as);
						p1Act = p1as[p1as.length - 1];
						p2Act = p2as[p2as.length - 1];
						if (p1Act.equals(p2Act)) {// 两家都胡牌，分局圈风确定向下推
							Integer circleWind = chuUser.getPosition();
							// 玩家的风向跟出牌人的位置对比，都大于牌人的位置，则取大的；都小于牌人的位置则取小的；一大一小则取大
							Integer wind1 = p1.getPosition();
							Integer wind2 = p2.getPosition();
							Integer[] winds = new Integer[3];
							winds[0] = wind1;
							winds[1] = wind2;
							winds[2] = circleWind;
							Arrays.sort(winds);
							if (winds[0].equals(circleWind)) {
								if (winds[1].equals(wind1)) {
									p2.setCurrentActions(null);
								} else {
									p1.setCurrentActions(null);
									continue a;
								}
							} else if (winds[1].equals(circleWind)) {
								if (winds[2].equals(wind1)) {
									p2.setCurrentActions(null);
								} else {
									p1.setCurrentActions(null);
									continue a;
								}
							} else if (winds[2].equals(circleWind)) {
								if (winds[0].equals(wind1)) {
									p2.setCurrentActions(null);
								} else {
									p1.setCurrentActions(null);
									continue a;
								}
							}
						} else if (p1Act > p2Act) {// 玩家1优先级高
							p2.setCurrentActions(null);
						} else if (p1Act < p2Act) {// 玩家2优先级高
							p1.setCurrentActions(null);
							continue a;
						}
					}
				}
			}
		}
	}

	/**
	 * 玩家动作
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100202(IoSession session, ProtocolData readData)
			throws Exception {
		logger.I("玩家动作,interfaceId -> 100202");
		JSONObject obj = JSONObject.parseObject(readData.getJsonString());
		Integer roomId = obj.getInteger("roomSn");
		Long userId = obj.getLong("userId");
		Integer action = obj.getInteger("action");
		Long toUserId = obj.getLong("toUserId");

		RoomResp room = getRoomRespByRoomId(String.valueOf(roomId));
		Player currentPlayer = null;
		List<Player> players = getPlayerList(room);
		for (Player p : players) {
			if (p.getUserId().equals(userId)) {
				currentPlayer = p;
				break;
			}
		}
		if (currentPlayer == null) {
			return;
		}

		Integer[][] actionPai = getIntegerList(obj.getString("actionPai"));
		if (actionPai == null || actionPai[0][0] == null
				|| actionPai[0][1] == null) {
			actionPai = null;
		}
		Integer[][] pais = getIntegerList(obj.getString("pais"));

		userId = currentPlayer.getUserId();

//		if (currentPlayer.getCurrentActions() == null
//				|| currentPlayer.getCurrentActions().size() == 0
//				|| !currentPlayer.getCurrentActions().containsKey(
//						String.valueOf(action))) {
//			System.err.println("玩家动作，当前状态不对！");
//			return;
//		}

		// 设置递增id
		Integer wsw_sole_action_id = obj.getInteger("wsw_sole_action_id");
		if (!room.getWsw_sole_action_id().equals(wsw_sole_action_id)) {
			MessageFunctions.interface_100108(session);
			return;
		} else {
			room.setWsw_sole_action_id(wsw_sole_action_id + 1);
		}
		
		//获取房间最大人数
		Integer maxNum = room.getMaxNum();
		
		Boolean isNextUser = false;
		Player nextUser = null;// 下一个发牌的人
		Player chuPlayer = null;// 最后出牌人

		currentPlayer.setNeedFaPai(false);

		Map<String, Object> userActionMap = new HashMap<String, Object>();

		updateRedisData(null, currentPlayer);
		
		// 清空玩家动作
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i).getUserId().equals(room.getLastUserId())) {
				chuPlayer = players.get(i);
				if (i == (maxNum-1)) {//2，3人麻将
					nextUser = players.get(0);
				} else {
					nextUser = players.get(i + 1);
				}
				if (nextUser.getUserId().equals(currentPlayer.getUserId())) {
					isNextUser = true;
				}
			}
			if (players.get(i).getUserId().equals(userId)) {
				userActionMap = players.get(i).getCurrentActions();
			}
			players.get(i).setCurrentActions(null);
			updateRedisData(null, players.get(i));
		}
		
		if (nextUser == null) {// 说明是首轮发牌，还没有人出牌，计算当前玩家的下家
//			if (currentPlayer.getPosition().equals(Cnst.WIND_NORTH)) {//
			if (currentPlayer.getPosition().equals( MahjongUtils.getLastPosition(maxNum))) {// 2，3人麻将，最后一人的位置都是3
				nextUser = players.get(0);
			} else {
				nextUser = players.get(currentPlayer.getPosition());
			}
		}

		InfoCount info = new InfoCount();
		info.setActionType(action);
		info.setUserId(currentPlayer.getUserId());
		info.setToUserId(toUserId);
		info.setT(new Date().getTime());

		List<Integer[][]> list = new ArrayList<>();
		switch (action) {
		case Cnst.ACTION_CHI:
			list.add(new Integer[][] { { pais[0][0], pais[0][1] } });
			list.add(new Integer[][] { { pais[1][0], pais[1][1] } });
			MahjongUtils.chi(currentPlayer.getCurrentMjList(), list);
			list.add(new Integer[][] { { actionPai[0][0], actionPai[0][1] } });
			info.setL(list);
			currentPlayer.getChiList().add(info);
			currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_CHU);
			chuPlayer.getChuList().remove(chuPlayer.getChuList().size() - 1);// 把出牌人的最后一张从出牌list中移除
			room.setLastPai(null);
			room.setLastUserId(null);

			for (Player p : players) {
				updateRedisData(null, p);
			}
			updateRedisData(room, null);
			MessageFunctions.interface_100104(players, userId, action,
					chuPlayer.getUserId(), null, session);
			break;
		case Cnst.ACTION_PENG:
			MahjongUtils.peng(currentPlayer.getCurrentMjList(), actionPai);
			list.add(new Integer[][] { { actionPai[0][0], actionPai[0][1] } });
			list.add(new Integer[][] { { actionPai[0][0], actionPai[0][1] } });
			list.add(new Integer[][] { { actionPai[0][0], actionPai[0][1] } });
			info.setL(list);
			currentPlayer.getPengList().add(info);
			currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_CHU);
			chuPlayer.getChuList().remove(chuPlayer.getChuList().size() - 1);// 把出牌人的最后一张从出牌list中移除
			room.setLastPai(null);
			room.setLastUserId(null);

			for (Player p : players) {
				updateRedisData(null, p);
			}
			updateRedisData(room, null);
			MessageFunctions.interface_100104(players, userId, action,
					chuPlayer.getUserId(), null, session);
			break;
		case Cnst.ACTION_GANG:
			for (int i = 0; i < pais.length; i++) {
				list.add(new Integer[][] { { pais[i][0], pais[i][1] } });
			}
			Integer gangType = MahjongUtils.gang(currentPlayer, list);
			// 1：点杠(size=1) 2： 碰杠(size=3) 3：暗杠(size=4)
			switch (gangType) {
			case 4:
				currentPlayer.setHasGang(1);// 明杠杠后点炮
				list.add(new Integer[][] { { pais[0][0], pais[0][1] } });
				list.add(new Integer[][] { { pais[0][0], pais[0][1] } });
				list.add(new Integer[][] { { pais[0][0], pais[0][1] } });
				info.setL(list);
				//出牌的玩家移除最后那张牌
				chuPlayer.getChuList().remove(chuPlayer.getChuList().size()-1);
				currentPlayer.getGangListType4().add(info);
				currentPlayer.setNeedFaPai(true);
				currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
				for (Player p : players) {
					updateRedisData(null, p);
				}
				updateRedisData(room, null);
				MessageFunctions.interface_100104(players, userId, action,
						currentPlayer.getUserId(), gangType, session);
				break;
			case 3:// 碰杠已经加在了碰的集合里面 ---- 需要考虑抢杠胡
				currentPlayer.setHasGang(1);// 名杠杠后点炮
				if (checkQiangGangHu(players, currentPlayer, nextUser,
						list.get(0), room, action)) {
					room.setLastQiangGangPai(new Integer[][]{{pais[0][0],pais[0][1]}});//如果有动作记录抢杠胡的牌
					currentPlayer.setNeedFaPai(false);
				} else {
					currentPlayer.setNeedFaPai(true);
				}
				currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
				for (Player p : players) {
					updateRedisData(null, p);
				}
				updateRedisData(room, null);
				MessageFunctions.interface_100104(players, userId, action,
						currentPlayer.getUserId(), gangType, session);
				break;
			case 5:
				currentPlayer.setHasGang(2);// 暗杠杠后点炮
				info.setL(list);
				currentPlayer.getGangListType5().add(info);

				if (currentPlayer.getLastFaPai() != null) {
					currentPlayer.getCurrentMjList().add(
							new Integer[][] { {
									currentPlayer.getLastFaPai()[0][0],
									currentPlayer.getLastFaPai()[0][1] } });
					currentPlayer.setCurrentMjList(MahjongUtils
							.paiXu(currentPlayer.getCurrentMjList()));
					currentPlayer.setLastFaPai(null);
				}

				currentPlayer.setNeedFaPai(true);
				currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_WAIT);

				for (Player p : players) {
					updateRedisData(null, p);
				}
				updateRedisData(room, null);
				MessageFunctions.interface_100104(players, userId, action,
						currentPlayer.getUserId(), gangType, session);
				break;

			}
			break;
		case Cnst.ACTION_TING:
		
			// 向过的人里面添加自己
			if (room.getGuoUserIds() == null) {
				room.setGuoUserIds(new ArrayList<Long>());
			}
			currentPlayer.setKouTing(2);//设置状态
			currentPlayer.setKouMark(false);;//设置状态
			room.getGuoUserIds().add(currentPlayer.getUserId());
			boolean action1 = false;
			for (Player p : players) {
				if (!room.getGuoUserIds().contains(p.getUserId())) {// 检测没有过的人是否有动作
					if (checkActions(p, actionPai, p.getUserId()
							.equals(nextUser.getUserId()),
							chuPlayer, room, players)) {
						action1 = true;
					}
				}
			}
			if (action1) {// 有动作，推送大接口
				List<Player> othersList = new ArrayList<Player>();
				for (Player pppp : players) {
					if (!chuPlayer.getUserId().equals(pppp.getUserId())) {
						othersList.add(pppp);
					}
				}
				removeActions(othersList, chuPlayer);
			} else {// 没有动作，发牌
					// 置空过的人
				room.setGuoUserIds(null);
				nextUser.setNeedFaPai(true);
				currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
			}

			for (Player p : players) {
				updateRedisData(null, p);
			}
			updateRedisData(room, null);
			MessageFunctions.interface_100104(players, userId, action,
					toUserId, null, session);
			break;
		case Cnst.ACTION_HU:
			Player dianUser = null;
			if (room.getLastPengGangUser() != null) {// 说明是抢杠胡
				Long dianUserId = room.getLastPengGangUser();
				for (Player p : players) {
					if (p.getUserId().equals(dianUserId)) {
						dianUser = p;
						dianUser.setHasGang(0);//他的杠被别人拿了，没有了
						break;
					}
				}
				// 获取动作牌
				Integer[][] g3 = dianUser.getGangListType3()
						.get(dianUser.getGangListType3().size() - 1).getL()
						.get(0);
				// 移除被强的杠
				dianUser.getGangListType3().remove(
						dianUser.getGangListType3().size() - 1);// 移除最后一个杠
				// 添加到碰
				List<InfoCount> pengList = dianUser.getPengList();// 获取原来的碰集合
				InfoCount infop = new InfoCount();
				list.add(new Integer[][] { { g3[0][0], g3[0][1] } });
				list.add(new Integer[][] { { g3[0][0], g3[0][1] } });
				list.add(new Integer[][] { { g3[0][0], g3[0][1] } });
				infop.setL(list);
				pengList.add(infop);// 添加被抢杠的碰
				dianUser.setPengList(pengList);
				actionPai = new Integer[][] { { g3[0][0], g3[0][1] } };
			} else {
				if (toUserId.equals(userId)) {// 自摸
					currentPlayer
							.setZimoNum(currentPlayer.getZimoNum() == null ? 1
									: currentPlayer.getZimoNum() + 1);
					dianUser = currentPlayer;
				} else if (dianUser == null) {// 点炮
					chuPlayer.getChuList().remove(
							chuPlayer.getChuList().size() - 1);// 把出牌人的最后一张从出牌list中移除
					dianUser = chuPlayer;
				}
			}
			// 将最后的那张牌加入手牌
			for (Player p : players) {
				updateRedisData(null, p);
			}
			updateRedisData(room, null);
			MessageFunctions.interface_100104(players, userId, action,
					dianUser.getUserId(), null, session);
			for (Player p : players) {
				if (p.getUserId().equals(currentPlayer.getUserId())) {
					p.setIsHu(true);
					p.setIsDian(false);
				} else {
					p.setIsHu(false);
					if (!dianUser.getUserId().equals(currentPlayer.getUserId())
							&& p.getUserId().equals(dianUser.getUserId())) {
						p.setDianNum(p.getDianNum() == null ? 1 : p
								.getDianNum() + 1);
						p.setIsDian(true);
					} else {
						p.setIsDian(false);
					}
				}
				p.setPlayStatus(Cnst.PLAYER_STATE_XJS);
			}
			for (Player p : players) {
				updateRedisData(null, p);
			}
			updateRedisData(room, null);
			MessageFunctions.hu(currentPlayer, dianUser, actionPai, session,players);
			break;
		case Cnst.ACTION_GUO:
			if (room.getLastPengGangUser() != null// 处理过杠胡 1--过杠的时候存在胡 和过，此时点过
					&& !currentPlayer.getUserId().equals(room.getLastPengGangUser())) {//
				if (room.getGuoUserIds() == null) {
					room.setGuoUserIds(new ArrayList<>());
				}
				toUserId = room.getLastPengGangUser();
				room.getGuoUserIds().add(currentPlayer.getUserId());
				Long lastGangUid = room.getLastPengGangUser();
				for (Player p : players) {
					// 过杠得玩家
					if (p.getUserId().equals(lastGangUid)) {
						if (chuPlayer == null) {
							chuPlayer = p;
						}
					}
				}
				// 过杠胡的处理
				List<Player> others=new ArrayList<Player>();
				for (Player p : players) {
					if (!room.getGuoUserIds().contains(p.getUserId())
							&& p.getUserId() != lastGangUid) {// 检测没有过的人是否有动（已经检测过的人和本身过喜得玩家不检测）
						others.add(p);
					}
				}
				if (checkOtherQiangGangHuAction(others, room.getLastQiangGangPai(), currentPlayer, nextUser, room, players)){
					currentPlayer.setNeedFaPai(false);
					currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
				}else{
					room.setLastQiangGangPai(null);
					if (room.getLastPengGangUser() != null) {
						for (Player p : players) {
							if (p.getUserId().equals(
									room.getLastPengGangUser())) {
								p.setNeedFaPai(true);
								p.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
							}
						}
					}
				}
				for (Player p : players) {
					updateRedisData(null, p);
				}
				updateRedisData(room, null);
				MessageFunctions.interface_100104(players, userId, action,
						toUserId, null, session);
			}else if(currentPlayer.getLastFaPai() == null
					&& room.getLastPai() != null) {// 弃别人的牌

				// 向过的人里面添加自己
				if (room.getGuoUserIds() == null) {
					room.setGuoUserIds(new ArrayList<Long>());
				}
				room.getGuoUserIds().add(currentPlayer.getUserId());
				if (isNextUser) {// 自己是下家
					if (userActionMap.containsKey(String
							.valueOf(Cnst.ACTION_HU))) {// 胡牌
														// 弃，因为胡牌的优先级较高，如果胡牌过了之后，要检测其他玩家动作
						boolean hasAction = false;
						for (Player p : players) {
							if (!room.getGuoUserIds().contains(p.getUserId())) {// 检测没有过的人是否有动作
								if (checkActions(p, actionPai, p.getUserId()
										.equals(nextUser.getUserId()),
										chuPlayer, room, players)) {
									hasAction = true;
								}
							}
						}
						if (hasAction) {// 有动作，推送大接口
							List<Player> othersList = new ArrayList<Player>();
							for (Player pppp : players) {
								if (!chuPlayer.getUserId().equals(
										pppp.getUserId())) {
									othersList.add(pppp);
								}
							}

							removeActions(othersList, chuPlayer);

						} else {// 没有动作，发牌
								// 置空过的人
							room.setGuoUserIds(null);
							currentPlayer.setNeedFaPai(true);
							currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
						}

						for (Player p : players) {
							updateRedisData(null, p);
						}
						updateRedisData(room, null);
						MessageFunctions.interface_100104(players, userId,
								action, toUserId, null, session);
					} else {// 非胡牌 弃
							// 置空过的人
						room.setGuoUserIds(null);
						currentPlayer.setNeedFaPai(true);
						currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
						for (Player p : players) {
							updateRedisData(null, p);
						}
						updateRedisData(room, null);
						MessageFunctions.interface_100104(players, userId,
								action, toUserId, null, session);

					}
				} else {// 不是下家 检测其他
					currentPlayer.setKouMark(false);;//设置状态
					boolean hasAction = false;
					for (Player p : players) {
						if (!room.getGuoUserIds().contains(p.getUserId())) {// 检测没有过的人是否有动作
							if (checkActions(p, actionPai, p.getUserId()
									.equals(nextUser.getUserId()),
									chuPlayer, room, players)) {
								hasAction = true;
							}
						}
					}
					if (hasAction) {// 有动作，推送大接口
						List<Player> othersList = new ArrayList<Player>();
						for (Player pppp : players) {
							if (!chuPlayer.getUserId().equals(pppp.getUserId())) {
								othersList.add(pppp);
							}
						}
						removeActions(othersList, chuPlayer);
					} else {// 没有动作，发牌
							// 置空过的人
						room.setGuoUserIds(null);
						nextUser.setNeedFaPai(true);
						currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
					}

					for (Player p : players) {
						updateRedisData(null, p);
					}
					updateRedisData(room, null);
					MessageFunctions.interface_100104(players, userId, action,
							toUserId, null, session);
				}
			} else {// 弃自己的牌
					// 置空过的人
				room.setGuoUserIds(null);
				nextUser.setNeedFaPai(false);
				currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_CHU);

				for (Player p : players) {
					updateRedisData(null, p);
				}
				updateRedisData(room, null);
				MessageFunctions.interface_100104(players, userId, action,
						toUserId, null, session);
			}
			break;
		}
	}

	// 检测抢杠胡
	private static boolean checkQiangGangHu(List<Player> players,
			Player currentPlayer, Player nextUser, Integer[][] pai,
			RoomResp room, Integer action) {
		boolean canHu = false;
		// 过杠胡逻辑
		List<Player> others = new ArrayList<Player>();
		for (Player p : players) {
			if (!p.getUserId().equals(currentPlayer.getUserId())) {
				others.add(p);
			}
		}
		if (checkOtherQiangGangHuAction(others, new Integer[][] { { pai[0][0],
				pai[0][1] } }, currentPlayer, nextUser, room, players)) {// 玩家有动作
			// 查看玩家的动作是不是胡，如果是胡，则构成过杠胡；
			Player p = getActionPlayer(players);
			if (p.getCurrentActions() != null
					&& p.getCurrentActions().size() > 0) {// 如果其他玩家有胡的动作
				// 抢杠胡：能过
				canHu = true;
				Long[] lastPengGangUser = new Long[1];
				if (action.equals(Cnst.ACTION_GANG)) {// 抢杠胡
					lastPengGangUser[0] = currentPlayer.getUserId();
				}
				room.setLastPengGangUser(currentPlayer.getUserId());// 设置过杠胡的人
			} else {
				p.setCurrentActions(null);
			}
		}
		return canHu;
	}

	/**
	 * 检测有没有胡动作
	 * 
	 * @param others
	 * @param integers
	 * @param currentPlayer
	 * @param nextUser
	 * @param room
	 * @param players
	 * @return
	 */
	private static boolean checkOtherQiangGangHuAction(List<Player> others,
			Integer[][] paiInfo, Player currentPlayer, Player nextUser,
			RoomResp room, List<Player> players) {
		// 给其他玩家检测动作
		Boolean hasAction = false;
		for (Player ps : others) {
			if (checkGuoGangHuActions(ps, new Integer[][] { { paiInfo[0][0],
					paiInfo[0][1] } },
					ps.getUserId().equals(nextUser.getUserId()), currentPlayer,
					room, players)) {
				hasAction = true;
			}
		}
		// 检测玩家动作优先级,删除优先级低的玩家动作
		removeActions(others, currentPlayer);
		if (hasAction) {// 有玩家有动作
			// 添加自己为过的人（出牌人对自己的牌肯定不能有动作）
			if (room.getGuoUserIds() == null) {
				room.setGuoUserIds(new ArrayList<Long>());
			}
			room.getGuoUserIds().add(currentPlayer.getUserId());
		}
		return hasAction;
	}

	/**
	 * 过杠胡动作检测
	 * 
	 * @param ps
	 * @param integers
	 * @param equals
	 * @param currentPlayer
	 * @param room
	 * @param players
	 * @return
	 */
	private static boolean checkGuoGangHuActions(Player p, Integer[][] pai,
			boolean isNextUser, Player chuUser, RoomResp room,
			List<Player> players) {
		Map<String, Object> currentActions = p.getCurrentActions();
		if (currentActions == null) {
			currentActions = new LinkedHashMap<>();
		}
		p.setCurrentActions(currentActions);
		// 别人出牌
		if (!p.getUserId().equals(chuUser.getUserId())) {// 检测胡
			// 胡检测
			if (MahjongUtils.checkHuNew(p, pai)) {// 此时的牌是发牌
				currentActions.put(String.valueOf(Cnst.ACTION_HU),  chuUser.getUserId());// 胡的动作
			}
		}
		if (currentActions.size() == 0) {
			p.setCurrentActions(null);
			return false;
		} else {
			currentActions.put(String.valueOf(Cnst.ACTION_GUO),
					new ArrayList<>());
			return true;
		}
	}

	public static Integer[][] getIntegerList(String str) {
		if (str == null) {
			return null;
		}
		JSONArray arr = JSONArray.parseArray(str);
		Integer[][] list = new Integer[arr.size()][2];
		for (int i = 0; i < arr.size(); i++) {
			JSONArray arr2 = arr.getJSONArray(i);
			list[i][0] = (Integer) arr2.get(0);
			list[i][1] = (Integer) arr2.get(1);
		}
		return list;
	}

	/**
	 * 如果返回true，代表有action，并且已经在player的属性中了
	 */
	private static Boolean checkOthersAction(List<Player> others,
			Integer[][] paiInfo, Player currentPlayer, Player nextUser,
			RoomResp room, List<Player> players) {
		// 给其他玩家检测动作
		Boolean hasAction = false;
		for (Player ps : others) {
			if (checkActions(ps, new Integer[][] { { paiInfo[0][0],
				paiInfo[0][1] } },
				ps.getUserId().equals(nextUser.getUserId()), currentPlayer,
				room, players)) {
				hasAction = true;
			}
		}
		// 检测玩家动作优先级,删除优先级低的玩家动作
		removeActions(others, currentPlayer);

		if (hasAction) {// 有玩家有动作

			// 添加自己为过的人（出牌人对自己的牌肯定不能有动作）
			if (room.getGuoUserIds() == null) {
				room.setGuoUserIds(new ArrayList<Long>());
			}
			room.getGuoUserIds().add(currentPlayer.getUserId());
		} else {// 没有动作了,推发牌
				// 出牌提示
			nextUser.setNeedFaPai(true);
			room.setGuoUserIds(null);
		}
		return hasAction;
	}

	/**
	 * 动作检测
	 * 
	 * @param p
	 * @param pai
	 * @param isNextUser
	 * @param chuUser
	 * @param room
	 * @param players
	 * @return
	 */
	public static boolean checkActions(Player p, Integer[][] pai,
			boolean isNextUser, Player chuUser, RoomResp room,
			List<Player> players) {
		Map<String, Object> currentActions = p.getCurrentActions();
		if (currentActions == null) {
			currentActions = new LinkedHashMap<>();
		}
		p.setCurrentActions(currentActions);
		if (p.getUserId().equals(chuUser.getUserId())) {// 自摸检测，只需要检测杠或者胡
			if(p.getKouTing()==2){
				// 胡检测
				if (MahjongUtils.checkHuNew(p, pai)) {
					currentActions.put(String.valueOf(Cnst.ACTION_HU), chuUser.getUserId());
				}
			}else{
				// 胡检测
				if (MahjongUtils.checkHuNew(p, pai)) {// 此时的牌是发牌
					currentActions.put(String.valueOf(Cnst.ACTION_HU),  p.getUserId());// 胡的动作
				}
				// 杠检测
				List<Integer[][]> gangs = MahjongUtils.checkGang(p, chuUser, pai);
				if (gangs != null && gangs.size() > 0) {
					currentActions.put(String.valueOf(Cnst.ACTION_GANG), gangs);
				}
			}
		} else {// 出牌过程中检测
			if(p.getKouTing()==2){
				// 胡检测
				if (MahjongUtils.checkHuNew(p, pai)) {
					currentActions.put(String.valueOf(Cnst.ACTION_HU), chuUser.getUserId());
				}
				
			}else{
				if(p.getCurrentMjList().size()==4  && !MahjongUtils.checkCanPiao(p)){//如果手牌只有4张,并且不是飘胡，只能检测杠和胡
					// 胡检测
					if (MahjongUtils.checkHuNew(p, pai)) {
						currentActions.put(String.valueOf(Cnst.ACTION_HU), chuUser.getUserId());
					}
					// 杠牌检测
					List<Integer[][]> gangs = MahjongUtils.checkGang(p, chuUser, pai);
					if (gangs != null && gangs.size() > 0) {
						currentActions.put(String.valueOf(Cnst.ACTION_GANG), gangs);
					}
				}else{//其他情况正常检测
					// 胡检测
					if (MahjongUtils.checkHuNew(p, pai)) {
						currentActions.put(String.valueOf(Cnst.ACTION_HU), chuUser.getUserId());
					}
					// 杠牌检测
					List<Integer[][]> gangs = MahjongUtils.checkGang(p, chuUser, pai);
					if (gangs != null && gangs.size() > 0) {
						currentActions.put(String.valueOf(Cnst.ACTION_GANG), gangs);
					}
					// 碰牌检测
					List<Integer[][]> pengs = MahjongUtils.checkPeng(p, pai);
					if (pengs != null && pengs.size() > 0) {
						currentActions.put(String.valueOf(Cnst.ACTION_PENG), pengs);
					}
					if (isNextUser) {
						// 吃牌检测
						List<Integer[][]> chis = MahjongUtils.checkChi(p, pai);
						if (chis != null && chis.size() > 0) {
							currentActions.put(String.valueOf(Cnst.ACTION_CHI), chis);
						}
					}
				}
			}
		}
		if (currentActions.size() == 0) {
			p.setCurrentActions(null);
			return false;
		} else {
			currentActions.put(String.valueOf(Cnst.ACTION_GUO),
					new ArrayList<>());
			return true;
		}
	}

	/**
	 * 玩家申请解散房间
	 * 
	 * @param session
	 * @param readData
	 * @throws Exception
	 */
	public static void interface_100203(IoSession session, ProtocolData readData)
			throws Exception {
		logger.I("玩家请求解散房间,interfaceId -> 100203");
		JSONObject obj = JSONObject.parseObject(readData.getJsonString());
		Integer interfaceId = obj.getInteger("interfaceId");
		Integer roomId = obj.getInteger("roomSn");
		Long userId = obj.getLong("userId");
		RoomResp room = getRoomRespByRoomId(String.valueOf(roomId));
		if (room.getDissolveRoom() != null) {
			return;
		}
		DissolveRoom dis = new DissolveRoom();
		dis.setDissolveTime(new Date().getTime());
		dis.setUserId(userId);
		List<Map<String, Object>> othersAgree = new ArrayList<>();
		List<Player> players = getPlayerList(room);
		for (Player p : players) {
			if (!p.getUserId().equals(userId)) {
				Map<String, Object> map = new HashMap<>();
				map.put("userId", p.getUserId());
				map.put("agree", 0);// 1同意；2解散；0等待
				othersAgree.add(map);
			}
		}
		dis.setOthersAgree(othersAgree);
		room.setDissolveRoom(dis);

		Map<String, Object> info = new HashMap<>();
		info.put("dissolveTime", dis.getDissolveTime());
		info.put("userId", dis.getUserId());
		info.put("othersAgree", dis.getOthersAgree());
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		for (Player p : players) {
			IoSession se = session.getService().getManagedSessions()
					.get(p.getSessionId());
			if (se != null && se.isConnected()) {
			 	se.write(pd);
			}
		}

		for (Player p : players) {
			updateRedisData(null, p);
		}
		updateRedisData(room, null);
		// 解散房间超时任务开启
		startDisRoomTask(room.getRoomId(), Cnst.DIS_ROOM_TYPE_2);
	}

	/**
	 * 同意或者拒绝解散房间
	 * 
	 * @param session
	 * @param readData
	 * @throws Exception
	 */
	public static void interface_100204(IoSession session, ProtocolData readData)
			throws Exception {
		logger.I("同意或者拒绝解散房间,interfaceId -> 100203");
		JSONObject obj = JSONObject.parseObject(readData.getJsonString());
		Integer interfaceId = obj.getInteger("interfaceId");
		Integer roomId = obj.getInteger("roomSn");
		Long userId = obj.getLong("userId");
		Integer userAgree = obj.getInteger("userAgree");
		RoomResp room = getRoomRespByRoomId(String.valueOf(roomId));
		if (room == null) {// 房间已经自动解散
			Map<String, Object> info = new HashMap<>();
			info.put("reqState", Cnst.REQ_STATE_4);
			JSONObject result = getJSONObj(interfaceId, 1, info);
			ProtocolData pd = new ProtocolData(interfaceId,
					result.toJSONString());
			session.write(pd);
			return;
		}
		if (room.getDissolveRoom() == null) {
			Map<String, Object> info = new HashMap<>();
			info.put("reqState", Cnst.REQ_STATE_7);
			JSONObject result = getJSONObj(interfaceId, 1, info);
			ProtocolData pd = new ProtocolData(interfaceId,
					result.toJSONString());
			session.write(pd);
			return;
		}
		List<Map<String, Object>> othersAgree = room.getDissolveRoom()
				.getOthersAgree();
		for (Map m : othersAgree) {
			if (String.valueOf(m.get("userId")).equals(String.valueOf(userId))) {
				m.put("agree", userAgree);
				break;
			}
		}
		Map<String, Object> info = new HashMap<>();
		info.put("dissolveTime", room.getDissolveRoom().getDissolveTime());
		info.put("userId", room.getDissolveRoom().getUserId());
		info.put("othersAgree", room.getDissolveRoom().getOthersAgree());
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());

		if (userAgree == 2) {
			room.setDissolveRoom(null);
		}
		int agreeNum = 0;
		int rejectNunm = 0;

		for (Map m : othersAgree) {
			if (m.get("agree").equals(1)) {
				agreeNum++;
			} else if (m.get("agree").equals(2)) {
				rejectNunm++;
			}
		}
		Integer maxNum = room.getMaxNum();
		updateRedisData(room, null);

		List<Player> players = getPlayerList(room);

		if (agreeNum == (maxNum-1) || rejectNunm >= 1) {
			if (agreeNum == (maxNum-1)) {
				MessageFunctions.setOverInfo(room, players);
				room.setHasInsertRecord(true);
				room.setStatus(Cnst.ROOM_STATE_YJS);
				if(String.valueOf(roomId).length()>6){
					//俱乐部房间
					MessageFunctions.updateClubDatabasePlayRecord(room);
				}else{
					MessageFunctions.updateDatabasePlayRecord(room);
				}
				for (Player p : players) {
					p.initPlayer(null, null, null, Cnst.PLAYER_STATE_DATING, 0,
							0, 0);
				}
				BackFileUtil.write(null, 100103, room, null, null);// 写入文件内容
			}

			// 关闭超时任务
			notifyDisRoomTask(room, Cnst.DIS_ROOM_TYPE_2,false);
			for (Player p : players) {
				updateRedisData(null, p);
			}
			updateRedisData(room, null);

		}

		for (Player p : players) {
			IoSession se = session.getService().getManagedSessions()
					.get(p.getSessionId());
			if (se != null && se.isConnected()) {
				se.write(pd);
			}
		}
	}

	/**
	 * 退出房间
	 * 
	 * @param session
	 * @param readData
	 * @throws Exception
	 */
	public static void interface_100205(IoSession session, ProtocolData readData)
			throws Exception {
		logger.I("退出房间,interfaceId -> 100205");
		JSONObject obj = JSONObject.parseObject(readData.getJsonString());
		Integer interfaceId = obj.getInteger("interfaceId");
		Integer roomId = obj.getInteger("roomSn");
		Long userId = obj.getLong("userId");
		RoomResp room = getRoomRespByRoomId(String.valueOf(roomId));
		if (room == null) {
			roomDoesNotExist(interfaceId, session);
			return;
		}
		if (room.getStatus().equals(Cnst.ROOM_STATE_CREATED)) {
			List<Player> players = getPlayerList(room);
			Map<String, Object> info = new HashMap<>();
			info.put("userId", userId);
			if (room.getCreateId().equals(userId)) {// 房主退出，
				if (room.getRoomType().equals(Cnst.ROOM_TYPE_1)) {// 房主模式
//					int circle = room.getCircleNum();
					//俱乐部更新redis
                    if(null != room.getClubId() && String.valueOf(room.getRoomId()).length() > 6){
                    	TCPGameFunctions.hdel(room.getClubId()+"", String.valueOf(room.getRoomId()));
                    }
                    Integer needMoney = room.getNeedMoney();
                    notifyDisRoomTask(room, Cnst.DIS_ROOM_TYPE_1, false);
					room = null;
					info.put("type", Cnst.EXIST_TYPE_DISSOLVE);
					
					for (Player p : players) {
						if (p.getUserId().equals(userId)) {
//							p.setMoney(p.getMoney() + Cnst.moneyMap.get(circle));
							p.setMoney(p.getMoney() + needMoney);
							break;
						}
					}
					
					deleteByKey(Cnst.REDIS_PREFIX_ROOMMAP.concat(String
							.valueOf(roomId)));

					for (Player p : players) {
						p.initPlayer(null, null, null,
								Cnst.PLAYER_STATE_DATING, 0, 0, 0);
					}
				} else {// 自由模式，走正常退出
					info.put("type", Cnst.EXIST_TYPE_EXIST);
					existRoom(room, players, userId);
					updateRedisData(room, null);
				}
			} else {// 正常退出
				info.put("type", Cnst.EXIST_TYPE_EXIST);
				existRoom(room, players, userId);
				updateRedisData(room, null);
			}
			JSONObject result = getJSONObj(interfaceId, 1, info);
			ProtocolData pd = new ProtocolData(interfaceId,
					result.toJSONString());

			for (Player p : players) {
				updateRedisData(null, p);
			}

			for (Player p : players) {
				IoSession se = session.getService().getManagedSessions()
						.get(p.getSessionId());
				if (se != null && se.isConnected()) {
					se.write(pd);
				}
			}

		} else {
			roomIsGaming(interfaceId, session);
		}
	}

	private static void existRoom(RoomResp room, List<Player> players,
			Long userId) {
		for (Player p : players) {
			if (p.getUserId().equals(userId)) {
				p.initPlayer(null, null, null, Cnst.PLAYER_STATE_DATING, 0, 0,
						0);
				break;
			}
		}
		Long[] pids = room.getPlayerIds();
		if (pids != null) {
			for (int i = 0; i < pids.length; i++) {
				if (userId.equals(pids[i])) {
					pids[i] = null;
					break;
				}
			}
		}
	}

	/**
	 * 语音表情
	 * 
	 * @param session
	 * @param readData
	 * @throws Exception
	 */
	public static void interface_100206(IoSession session, ProtocolData readData)
			throws Exception {
		logger.I("语音表情,interfaceId -> 100206");
		JSONObject obj = JSONObject.parseObject(readData.getJsonString());
		Integer interfaceId = obj.getInteger("interfaceId");
		Integer roomId = obj.getInteger("roomSn");
		Long userId = obj.getLong("userId");
		String type = obj.getString("type");
		String idx = obj.getString("idx");
		Map<String, Object> info = new HashMap<>();
		info.put("roomId", roomId);
		info.put("userId", userId);
		info.put("type", type);
		info.put("idx", idx);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		List<Player> players = getPlayerList(roomId);
		for (Player p : players) {
			if (!p.getUserId().equals(userId)) {
				IoSession se = session.getService().getManagedSessions()
						.get(p.getSessionId());
				if (se != null && se.isConnected()) {
					se.write(pd);
				}
			}
		}
	}

	/**
	 * 补牌指令
	 * 
	 * @param session
	 * @param readData
	 * @throws Exception
	 */
	public static void interface_100207(IoSession session, ProtocolData readData)
			throws Exception {

		logger.I("补牌指令,interfaceId -> 100207");
		JSONObject obj = JSONObject.parseObject(readData.getJsonString());
		Integer interfaceId = obj.getInteger("interfaceId");
		Long userId = obj.getLong("userId");
		Integer wsw_sole_action_id = obj.getInteger("wsw_sole_action_id");

		Player currentPlayer = getPlayerByUserId(String.valueOf(session
				.getAttribute(Cnst.USER_SESSION_USER_ID)));

		RoomResp room = getRoomRespByRoomId(String.valueOf(currentPlayer
				.getRoomId()));

		if (room == null
				|| !room.getWsw_sole_action_id().equals(wsw_sole_action_id)) {
			MessageFunctions.interface_100108(session);
			return;
		}
		Map<String, Object> info = new HashMap<>();
		info.put("reqState", Cnst.REQ_STATE_1);

		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);

		updateRedisData(room, currentPlayer);
		MessageFunctions.interface_100101(session, readData);
	}

}
