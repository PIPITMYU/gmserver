package com.up72.server.mina.function;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.util.DateUtil;
import org.apache.mina.core.session.IoSession;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.leo.rms.utils.StringUtils;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.ClubInfo;
import com.up72.game.dto.resp.ClubUser;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.PlayerRecord;
import com.up72.game.dto.resp.RoomResp;
import com.up72.game.service.IClubGamePlayRecordService;
import com.up72.game.service.IClubInfoService;
import com.up72.game.service.IClubUserService;
import com.up72.game.service.IClubUserUseService;
import com.up72.game.service.IUserService;
import com.up72.game.service.impl.ClubGamePlayRecordServiceImpl;
import com.up72.game.service.impl.ClubInfoServiceImpl;
import com.up72.game.service.impl.ClubUserServiceImpl;
import com.up72.game.service.impl.ClubUserUseServiceImpl;
import com.up72.game.service.impl.UserServiceImpl;
import com.up72.server.mina.bean.ProtocolData;
import com.up72.server.mina.utils.CommonUtil;

/**
 * 俱乐部
 */

public class ClubInfoFunctions extends TCPGameFunctions {

	public static IClubInfoService clubInfoService = new ClubInfoServiceImpl();
	public static IClubUserService clubUserService = new ClubUserServiceImpl();
	public static IClubUserUseService clubUserUseService = new ClubUserUseServiceImpl();
	public static IClubGamePlayRecordService clubGamePlayRecordService = new ClubGamePlayRecordServiceImpl();
	public static IUserService userService = new UserServiceImpl();

	/**
	 * 扫描二维码查询俱乐部 "clubId":"俱乐部id", userId：玩家id
	 */
	public static void interface_500001(IoSession session, ProtocolData readData)
			throws Exception {
		logger.I("准备,interfaceId -> 500001");
		JSONObject obj = JSONObject.parseObject(readData.getJsonString());
		Integer interfaceId = obj.getInteger("interfaceId");
		Object userId = obj.get("userId");
		Object clubId = obj.get("clubId");

		isParameterError(interfaceId, session,
				StringUtils.isNotEmpty(userId.toString()));
		isParameterError(interfaceId, session,
				StringUtils.isNumeric(userId.toString()));
		isParameterError(interfaceId, session,
				StringUtils.isNotEmpty(clubId.toString()));
		isParameterError(interfaceId, session,
				StringUtils.isNumeric(clubId.toString()));

		Long sessionUserId = (Long) session
				.getAttribute(Cnst.USER_SESSION_USER_ID);
		if (!sessionUserId.equals(StringUtils.parseLong(userId))) {// 不相同
			illegalRequest(interfaceId, session);
			return;
		}
		Map<String, Object> info = new HashMap<>();
		// 通过clubId从redis中获取俱乐部信息
		ClubInfo redisClub = getClubInfoByClubId(clubId.toString());
		if (null == redisClub) {// 如果为空 从数据库查询
			redisClub = clubInfoService.selectByClubId(StringUtils
					.parseInt(clubId));// 根据俱乐部id查询
			// 保存到redis
			setClubInfoByClubId(clubId.toString(), redisClub);
		}
		if (null != redisClub) {
			info.put("clubId", redisClub.getClubId());
			info.put("name", redisClub.getClubName());
			info.put(
					"user",
					clubInfoService.selectCreateName(Integer.valueOf(redisClub
							.getCreateId() + "")));
			info.put("num", clubUserService.allUsers(redisClub.getClubId()));
			info.put("ct", redisClub.getCreateTime());
			info.put("total", redisClub.getPersonQuota());
			//新添加的俱乐部信息
			info.put("fs", redisClub.getFreeStart());
			info.put("fe", redisClub.getFreeEnd());
		}
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
		System.out.println("扫描二维码查询俱乐部成功**********************");
	}

	/**
	 * 查询我的俱乐部
	 */
	public static void interface_500002(IoSession session, ProtocolData readData)
			throws Exception {
		logger.I("准备,interfaceId -> 500002");
 		JSONObject obj = JSONObject.parseObject(readData.getJsonString());
		Integer interfaceId = obj.getInteger("interfaceId");
		Object userId = obj.get("userId");

		isParameterError(interfaceId, session,
				StringUtils.isNotEmpty(userId.toString()));
		isParameterError(interfaceId, session,
				StringUtils.isNumeric(userId.toString()));
		Long sessionUserId = (Long) session
				.getAttribute(Cnst.USER_SESSION_USER_ID);
		if (!sessionUserId.equals(StringUtils.parseLong(userId))) {// 不相同
			illegalRequest(interfaceId, session);
			return;
		}

		List<Map<String, Object>> listInfo = new ArrayList<Map<String, Object>>();
		List<ClubUser> list = clubUserService.selectClubByUserId(StringUtils
				.parseLong(userId));// 查询我加入的俱乐部信息
		if (list != null && list.size() > 0) {
			for (int a = 0; a < list.size(); a++) {
				Map<String, Object> info = new HashMap<>();
				// 通过clubId从redis中获取俱乐部信息
				ClubInfo redisClub = getClubInfoByClubId(list.get(a)
						.getClubId().toString());
				if (null == redisClub) {// 如果为空 从数据库查询
					redisClub = clubInfoService.selectByClubId(list.get(a)
							.getClubId());// 根据俱乐部id查询
					// 保存到redis
					setClubInfoByClubId(list.get(a).getClubId().toString(),
							redisClub);
				}
				if (null != redisClub) {
					info.put("clubId", redisClub.getClubId());
					info.put("user", clubInfoService.selectCreateName(Integer
							.valueOf(redisClub.getCreateId() + "")));
					info.put("name", redisClub.getClubName());
					info.put("total", redisClub.getPersonQuota());
					//限免时间
					info.put("fs", redisClub.getFreeStart());
					info.put("fe", redisClub.getFreeEnd());
				}
				info.put("num", clubUserService.allUsers(redisClub.getClubId()));
				listInfo.add(info);
			}
		}
		JSONObject result = getJSONObj(interfaceId, 1, listInfo);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
		System.out.println("查询我的俱乐部成功**********************");
	}

	/**
	 * 申请加入俱乐部
	 */
	public static void interface_500000(IoSession session, ProtocolData readData)
			throws Exception {
		logger.I("准备,interfaceId -> 500000");
		JSONObject obj = JSONObject.parseObject(readData.getJsonString());
		Integer interfaceId = obj.getInteger("interfaceId");
		Object userId = obj.get("userId");
		Object clubId = obj.get("clubId");

		isParameterError(interfaceId, session,
				StringUtils.isNotEmpty(userId.toString()));
		isParameterError(interfaceId, session,
				StringUtils.isNumeric(userId.toString()));
		isParameterError(interfaceId, session,
				StringUtils.isNotEmpty(clubId.toString()));
		isParameterError(interfaceId, session,
				StringUtils.isNumeric(clubId.toString()));
		Long sessionUserId = (Long) session
				.getAttribute(Cnst.USER_SESSION_USER_ID);
		if (!sessionUserId.equals(StringUtils.parseLong(userId))) {// 不相同
			illegalRequest(interfaceId, session);
			return;
		}

		Map<String, Object> info = new HashMap<>();
		ClubUser user = clubUserService.selectUserByUserIdAndClubId(
				StringUtils.parseLong(userId), StringUtils.parseInt(clubId));
		if (null != user) {//
			info.put("reqState", 3);
		} else {

			Integer count = clubUserService.countByUserId(StringUtils
					.parseLong(userId));// 查询我加入的俱乐部
			if (null != count && count >= 3) {// 如果加入的大于3个
				info.put("reqState", 2);
			} else {
				ClubUser clubUser = new ClubUser();
				clubUser.setUserId(StringUtils.parseLong(userId));
				clubUser.setClubId(StringUtils.parseInt(clubId));
				clubUser.setStatus(0);// 默认申请中
				clubUser.setCreateTime(new Date().getTime());// 申请时间
				clubUserService.insert(clubUser);// 保存
				info.put("reqState", 1);
			}
		}

		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
		System.out.println("申请加入俱乐部成功**********************");
	}

	/**
	 * 申请离开俱乐部
	 */
	public static void interface_500007(IoSession session, ProtocolData readData)
			throws Exception {
		logger.I("准备,interfaceId -> 500007");
		JSONObject obj = JSONObject.parseObject(readData.getJsonString());
		Integer interfaceId = obj.getInteger("interfaceId");
		Object userId = obj.get("userId");
		Object clubId = obj.get("clubId");

		isParameterError(interfaceId, session,
				StringUtils.isNotEmpty(userId.toString()));
		isParameterError(interfaceId, session,
				StringUtils.isNumeric(userId.toString()));
		isParameterError(interfaceId, session,
				StringUtils.isNotEmpty(clubId.toString()));
		isParameterError(interfaceId, session,
				StringUtils.isNumeric(clubId.toString()));
		Long sessionUserId = (Long) session
				.getAttribute(Cnst.USER_SESSION_USER_ID);
		if (!sessionUserId.equals(StringUtils.parseLong(userId))) {// 不相同
			illegalRequest(interfaceId, session);
			return;
		}

		Map<String, Object> info = new HashMap<>();
		// 根据用户id 和通过状态 查询
		ClubUser clubUser = clubUserService.selectUserByUserIdAndClubId(
				StringUtils.parseLong(userId), StringUtils.parseInt(clubId));
		if (clubUser.getUserId() == userId) {
			return;
		}
		if (null != clubUser) {
			if (clubUser.getStatus() == 1) {
				info.put("reqState", 1);
				info.put("exState",1);
				clubUser.setStatus(2);// 状态 状态 0申请加入 1已通过 2申请退出
				clubUserService.updateById(clubUser);// 修改保存记录
			} else if (clubUser.getStatus() == 2) {
				info.put("reqState", 2);
				info.put("exState",1);
			}
		}
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
		System.out.println("申请离开俱乐部成功**********************");
	}

	/**
	 * 查询俱乐部详情
	 */
	public static void interface_500003(IoSession session, ProtocolData readData)
			throws Exception {
		logger.I("准备,interfaceId -> 500003");
		JSONObject obj = JSONObject.parseObject(readData.getJsonString());
		Integer interfaceId = obj.getInteger("interfaceId");
		Object userId = obj.get("userId");
		Object clubId = obj.get("clubId");

		isParameterError(interfaceId, session,
				StringUtils.isNotEmpty(userId.toString()));
		isParameterError(interfaceId, session,
				StringUtils.isNumeric(userId.toString()));
		isParameterError(interfaceId, session,
				StringUtils.isNotEmpty(clubId.toString()));
		isParameterError(interfaceId, session,
				StringUtils.isNumeric(clubId.toString()));
		Long sessionUserId = (Long) session
				.getAttribute(Cnst.USER_SESSION_USER_ID);
		if (!sessionUserId.equals(StringUtils.parseLong(userId))) {// 不相同
			illegalRequest(interfaceId, session);
			return;
		}
		//
		Map<String, Object> info = new HashMap<>();
		// 根据用户id 和通过状态 查询
		ClubInfo clubInfo = getClubInfoByClubId(clubId.toString());
		if (null == clubInfo) {// 如果为空 从数据库查询
			clubInfo = clubInfoService.selectByClubId(StringUtils
					.parseInt(clubId));// 根据俱乐部id查询
			// 保存到redis
			setClubInfoByClubId(clubId.toString(), clubInfo);
		}
		if (null != clubInfo) {
			info.put("clubId", clubInfo.getClubId());
			info.put(
					"user",
					clubInfoService.selectCreateName(Integer.valueOf(clubInfo
							.getCreateId() + "")));
			info.put("mlast", clubInfo.getRoomCardNum());
			info.put("need", clubInfo.getRoomCardQuota());
			//限免时间
			info.put("fs", clubInfo.getFreeStart());
			info.put("fe", clubInfo.getFreeEnd());
		}

		// 根据俱乐部id和时间 查询消费房卡数
		Integer used = clubUserUseService.sumMoneyByClubIdAndDate(
				DateUtil.formatDate(new Date(), "yyyy-MM-dd"),
				StringUtils.parseInt(clubId));
		info.put("used", used == null ? 0 : used);
		// 根据俱乐部id和时间查询开局数
//		Integer juNum = clubUserUseService.countJuNumByClubIdAndDate(
//				DateUtil.formatDate(new Date(), "yyyy-MM-dd"),
//				StringUtils.parseInt(clubId));
//		// 根据俱乐部id和时间查询 活跃牌友数
//		Integer actNum = clubGamePlayRecordService.countActNumByClubIdAndDate(
//				DateUtil.formatDate(new Date(), "yyyy-MM-dd"),
//				StringUtils.parseInt(clubId));
		Integer actNum=clubUserService.todayPerson(Integer.valueOf(clubId.toString())).size();
		Integer juNum =clubUserService.todayGames(Integer.valueOf(clubId.toString()));
		info.put("juNum", juNum == null ? 0 : juNum);
		info.put("actNum", actNum == null ? 0 : actNum);
		// 根据俱乐部id和userid查询当前状态
		Integer exState = clubUserService.selectUserState(
				StringUtils.parseInt(clubId), StringUtils.parseLong(userId));

		// 俱乐部页面刷新 此时管理员已同意退出
		if (exState == null) {
			info.put("reqState", 0);
		} else {
			info.put("exState", exState == 2 ? 1 : 0);
		}
		/************************** 未开局的房间数 **********************************/

		JSONArray jsonArrayInfo = new JSONArray();

		Map<String, String> roomMap = TCPGameFunctions.hgetAll(String
				.valueOf(clubId));
		if (roomMap.isEmpty()) {
			// 社么也不用做处理 似乎
		} else {

			for (String roomId : roomMap.keySet()) {
				RoomResp room = TCPGameFunctions.getRoomRespByRoomId(roomId);
				if (room == null) {
					// 房间已解散
					TCPGameFunctions.hdel(String.valueOf(clubId), roomId);
				} else {
					JSONObject jsobj = new JSONObject();
					JSONObject roomobj = new JSONObject();
					jsobj.put("rId", room.getRoomId());
					Player play = userService.isExistUserId(room.getCreateId());// 查询用户信息
					jsobj.put("cname", play.getUserName());
					jsobj.put("cimg", play.getUserImg());
					int num = 0;
					for (int i = 0; i < room.getPlayerIds().length; i++) {
						if (room.getPlayerIds()[i] != null) {
							num++;
						}
					}
					jsobj.put("num", num);// 当前人数
					roomobj.put("ms", room.getMaxScoreInRoom());
					jsobj.put("rule", roomobj);

					jsonArrayInfo.add(jsobj);
				}

			}
		}
		/************************** 未开局的房间数结束 **********************************/

		info.put("rooms", jsonArrayInfo);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);

	}

	/**
	 * 查询我的战绩
	 */
	public static void interface_500006(IoSession session, ProtocolData readData)
			throws Exception {
		logger.I("准备,interfaceId -> 500006");
		JSONObject obj = JSONObject.parseObject(readData.getJsonString());
		Integer interfaceId = obj.getInteger("interfaceId");
		Object userId = obj.get("userId");
		Object clubId = obj.get("clubId");
		Object page = obj.get("page");
		Object st = obj.get("st");
		Object et = obj.get("et");

		isParameterError(interfaceId, session,
				StringUtils.isNotEmpty(userId.toString()));
		isParameterError(interfaceId, session,
				StringUtils.isNumeric(userId.toString()));
		isParameterError(interfaceId, session,
				StringUtils.isNotEmpty(clubId.toString()));
		isParameterError(interfaceId, session,
				StringUtils.isNumeric(clubId.toString()));

		isParameterError(interfaceId, session,
				StringUtils.isNotEmpty(page.toString()));
		isParameterError(interfaceId, session,
				StringUtils.isNumeric(page.toString()));
		isParameterError(interfaceId, session,
				StringUtils.isNumeric(st.toString()));
		isParameterError(interfaceId, session,
				StringUtils.isNumeric(et.toString()));

		if (st.toString().length() != 13) {
			illegalRequest(interfaceId, session);
			return;
		}
		if (et.toString().length() != 13) {
			illegalRequest(interfaceId, session);
			return;
		}

		Long sessionUserId = (Long) session
				.getAttribute(Cnst.USER_SESSION_USER_ID);
		if (!sessionUserId.equals(StringUtils.parseLong(userId))) {// 不相同
			illegalRequest(interfaceId, session);
			return;
		}

		Integer juNum =clubUserService.todayGames(Integer.valueOf(clubId.toString()));
		// 根据俱乐部id，人员id和时间查询 其参与的 战绩 分页
		List<PlayerRecord> list = clubGamePlayRecordService
				.findPlayerRecordByUserId(StringUtils.parseLong(userId),
						(StringUtils.parseInt(page) - 1) * Cnst.PAGE_SIZE,
						Cnst.PAGE_SIZE, StringUtils.parseInt(clubId),
						StringUtils.parseLong(st), StringUtils.parseLong(et));
		// 根据俱乐部id，人员id和时间查询 总分数
		Integer score = clubGamePlayRecordService.findScoreByUserIdAndClubId(
				StringUtils.parseLong(userId), StringUtils.parseInt(clubId),
				DateUtil.formatDate(new Date(), "yyyy-MM-dd"));

		Map<String, Object> info = new HashMap<>();
		info.put("pages", "");
		info.put("juNum", juNum == null ? 0 : juNum);
		info.put("score", score == null ? 0 : score);
		JSONArray jsonArrayInfo = new JSONArray();
		if (list != null && list.size() > 0) {
			for (PlayerRecord play : list) {
				JSONObject recordobj = new JSONObject();
				JSONObject jsobj = new JSONObject();
				recordobj.put("rId", play.getRoomId());
				recordobj.put("ct", play.getStartTime());
				if(play.getEastUserName()!=null){
					jsobj.put(play.getEastUserName(),
							String.valueOf(play.getEastUserMoneyRecord()));
				}
				if(play.getSouthUserName()!=null){
					jsobj.put(play.getSouthUserName(),
							String.valueOf(play.getSouthUserMoneyRecord()));
				}
				if(play.getWestUserName()!=null){
					jsobj.put(play.getWestUserName(),
							String.valueOf(play.getWestUserMoneyRecord()));
				}
				if(play.getNorthUserName()!=null){
					jsobj.put(play.getNorthUserName(),
							String.valueOf(play.getNorthUserMoneyRecord()));
				}

				recordobj.put("users", jsobj);
				jsonArrayInfo.add(recordobj);
			}
		}
		info.put("record", jsonArrayInfo);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
		System.out.println("查询我的战绩成功**********************");
	}

	/**
	 * 俱乐部创建房间
	 */
	public static void interface_500004(IoSession session, ProtocolData readData) {
		logger.I("创建房间,interfaceId -> 500004");
		JSONObject obj = JSONObject.parseObject(readData.getJsonString());
		Integer interfaceId = obj.getInteger("interfaceId");
		Long userId = obj.getLong("userId");
		Integer circleNum = obj.getInteger("circleNum");
		Integer maxScore = obj.getInteger("maxScore");
		Integer roomType = obj.getInteger("roomType");
		Object clubId = obj.get("clubId");
		Integer maxNum = obj.getInteger("maxNum");
		
		  //根据人数判断房卡
        Integer needMoney=0;
        RoomResp room = new RoomResp();
        if(maxNum==2){
        	needMoney=Cnst.twoMoneyMap.get(circleNum);
        }else if(maxNum==3){
        	needMoney=Cnst.threeMoneyMap.get(circleNum);
        }else if(maxNum==4){
        	needMoney=Cnst.fourMoneyMap.get(circleNum);
        }
        room.setMaxNum(maxNum);
        room.setNeedMoney(needMoney);
		Map<String, String> roomMap = TCPGameFunctions.hgetAll(String
				.valueOf(clubId));
		if (roomMap.isEmpty()) {
			// 似乎也不用做处理
		} else {
			if (roomMap.keySet().size() >= 5) {
				JSONObject result = getJSONObj(interfaceId, 0, null);
				ProtocolData pd = new ProtocolData(interfaceId,
						result.toJSONString());
				session.write(pd);
				return;
			}
		}

		Player p = getPlayerByUserId(String.valueOf(session
				.getAttribute(Cnst.USER_SESSION_USER_ID)));
		// 通过clubId从redis中获取俱乐部信息
		ClubInfo redisClub = getClubInfoByClubId(clubId.toString());
		if(redisClub==null){
			
		}
		// 超过每日限额
		int max = redisClub.getRoomCardQuota();
		// 获取当前玩家一天的房卡数
		Integer todayUse = clubUserUseService.todayUse(
				StringUtils.parseInt(clubId), StringUtils.parseInt(userId));
//		int willUse = Cnst.clubMoneyMap.get(StringUtils.parseInt(circleNum));// 获取本局房卡数
		int willUse = room.getNeedMoney();// 获取本局房卡数
		System.out.println(todayUse + "=====" + willUse + "=====" + max);
		// 房卡不能超过限制(此处需要根据人数进行修改)
		if ((todayUse  + willUse / room.getMaxNum()) > max) {
			JSONObject error = getJSONObj(interfaceId, 0, null);
			error.put("message", "已达到每日消耗限额，明日再来吧");
			ProtocolData pd = new ProtocolData(interfaceId,
					error.toJSONString());
			session.write(pd);
			return;
		}

		Long freeStart = redisClub.getFreeStart();
		Long freeEnd = redisClub.getFreeEnd();
		long currentTimeMillis = System.currentTimeMillis();
		Boolean isFree=false;
		if(currentTimeMillis >=freeStart && currentTimeMillis<=freeEnd){//限免时间满足
			//不用做判断
			isFree=true;
		}
		if(!isFree){//如果不是限免时间。
			if (redisClub.getRoomCardNum() < willUse) {// 俱乐部房卡不足
				playerMoneyNotEnough(interfaceId, session,
						StringUtils.parseInt(roomType));
				return;
			}
		}

		if (p.getRoomId() != null) {// 已存在其他房间
			playerExistOtherRoom(interfaceId, session);
			return;
		}

		String createTime = String.valueOf(new Date().getTime());
		room.setNeedMoney(needMoney);
		room.setClubId(StringUtils.parseInt(clubId));// 俱乐部id
		room.setCreateId(userId);// 创建人
		room.setStatus(Cnst.ROOM_STATE_CREATED);// 房间状态为等待玩家入坐
		room.setCircleNum(circleNum);// 房间的总圈数
		room.setLastNum(circleNum);// 剩余圈数
		room.setCircleWind(Cnst.WIND_EAST);// 圈风为东风
		room.setRoomType(roomType);// 房间类型：房主模式或者自由模式
		room.setMaxScoreInRoom(maxScore);// 封顶分
		room.setCreateTime(createTime);// 创建时间，long型数据
		room.setLastHuang(false);
		room.setOpenName(p.getUserName());
		room.setMaxScore(maxScore);

		// 初始化大接口的id
		room.setWsw_sole_action_id(1);
		room.setWsw_sole_main_id(1);

		// toEdit 需要去数据库匹配，查看房间号是否存在，如果存在，则重新生成
		while (true) {
			room.setRoomId(CommonUtil.getGivenRamdonNum(7));// 设置随机房间密码
			if (getRoomRespByRoomId(String.valueOf(room.getRoomId())) == null) {
				break;
			}
		}

		Long[] userIds = new Long[maxNum];// 现在按照最大人数区分

		Map<String, Object> info = new HashMap<>();
		// 处理开房模式
		if (roomType == null) {
			illegalRequest(interfaceId, session);
		} else if (roomType.equals(Cnst.ROOM_TYPE_1)) {// 房主模式
			// 设置用户信息
			// p.setPosition(getWind(null));// 设置庄家位置为东
			p.setPosition(getWind(null, maxNum));// 设置庄家位置为东

			if (p.getPosition().equals(Cnst.WIND_EAST)) {
				p.setZhuang(true);
				room.setZhuangId(userId);
			} else {
				p.setZhuang(false);
			}
			p.setPlayStatus(Cnst.PLAYER_STATE_IN);// 进入房间状态
			p.setRoomId(room.getRoomId());
			p.setJoinIndex(1);
			p.initPlayer(p.getRoomId(), p.getPosition(), p.getZhuang(),
					Cnst.PLAYER_STATE_IN, p.getScore(), p.getHuNum(),
					p.getLoseNum());
			// userIds[p.getPosition() - 1] = p.getUserId();
			if (maxNum == 2 && p.getPosition() > 1) {// //2,3人麻将的判断
				userIds[p.getPosition() - 2] = p.getUserId();
			} else {
				userIds[p.getPosition() - 1] = p.getUserId();
			}
			info.put("reqState", Cnst.REQ_STATE_1);
			info.put("playerNum", 1);
			p.setMoney(p.getMoney() - room.getNeedMoney());
		}
		else {
			illegalRequest(interfaceId, session);
			return;
		}
		room.setPlayerIds(userIds);
		room.setIp(Cnst.SERVER_IP);

		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);

		// 更新redis数据
		updateRedisData(room, p);
		// 更新俱乐部redis数据
		setClubInfoByClubId(clubId.toString(), redisClub);
		// 添加到 存放俱乐部未开局房间信息的redis中
		TCPGameFunctions.hset(String.valueOf(clubId),
				String.valueOf(room.getRoomId()), "1", null);
		// 解散房间命令
		startDisRoomTask(room.getRoomId(), Cnst.DIS_ROOM_TYPE_1);

	}

	/**
	 * 加入房间
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_500005(IoSession session, ProtocolData readData)
			throws Exception {
		logger.I("加入房间,interfaceId -> 100008");
		JSONObject obj = JSONObject.parseObject(readData.getJsonString());
		Integer interfaceId = obj.getInteger("interfaceId");
		Long userId = obj.getLong("userId");
		Integer roomId = obj.getInteger("roomSn");

		Player p = getPlayerByUserId(String.valueOf(session
				.getAttribute(Cnst.USER_SESSION_USER_ID)));
		// 已经在其他房间里
		if (p.getRoomId() != null) {// 玩家已经在非当前请求进入的其他房间里
			playerExistOtherRoom(interfaceId, session);
			return;
		}
		// 房间不存在
		RoomResp room = getRoomRespByRoomId(String.valueOf(roomId));
		if (room == null || room.getStatus().equals(Cnst.ROOM_STATE_YJS)) {
			roomDoesNotExist(interfaceId, session);
			return;
		}
		// 加入俱乐部房间 达到每人每日限额
		// if(String.valueOf(room.getRoomId()).length() > 6){
		// 通过clubId从redis中获取俱乐部信息
		Integer clubId = room.getClubId();
		ClubInfo redisClub = getClubInfoByClubId(clubId.toString());
		// 超过每日限额
		int max = redisClub.getRoomCardQuota();
		//因为有2,3人麻将，以后肯定要修改
		Integer todayUse = clubUserUseService.todayUse(
				StringUtils.parseInt(clubId), StringUtils.parseInt(userId));
//		int willUse = Cnst.clubMoneyMap.get(StringUtils.parseInt(room.getCircleNum()));// 获取本局房卡数
		int willUse = room.getNeedMoney();// 获取本局房卡数
		if ((todayUse  + willUse / room.getMaxNum()) > max) {
			JSONObject error = getJSONObj(interfaceId, 0, null);
			error.put("message", "已达到每日消耗限额，明日再来吧");
			ProtocolData pd = new ProtocolData(interfaceId,
					error.toJSONString());
			session.write(pd);
			return;
		}
		// }

		// 房间人满
		Long[] userIds = room.getPlayerIds();
		boolean hasNull = false;
		int jionIndex = 0;
		for (Long uId : userIds) {
			if (uId == null) {
				hasNull = true;
			} else {
				jionIndex++;
			}
		}
		if (!hasNull) {
			roomFully(interfaceId, session);
			return;
		}
		// 验证ip是否一致
		if (!Cnst.SERVER_IP.equals(room.getIp())) {
			Map<String, Object> info = new HashMap<>();
			info.put("reqState", Cnst.REQ_STATE_14);
			info.put("roomSn", roomId);
			info.put("roomIp", room.getIp().concat(":").concat(Cnst.MINA_PORT));
			JSONObject result = getJSONObj(interfaceId, 1, info);
			ProtocolData pd = new ProtocolData(interfaceId,
					result.toJSONString());
			session.write(pd);
			return;
		}
		// 设置用户信息
		p.setPlayStatus(Cnst.PLAYER_STATE_PREPARED);// 准备状态
		p.setRoomId(roomId);
		p.setPosition(getWind(userIds, room.getMaxNum()));
		if (p.getPosition().equals(Cnst.WIND_EAST)) {
			p.setZhuang(true);
			room.setZhuangId(userId);
		} else {
			p.setZhuang(false);
		}
		if (room.getMaxNum() == 2 && p.getPosition() > 1) {// 2,3人麻将的判断
			userIds[p.getPosition() - 2] = p.getUserId();
		} else {
			userIds[p.getPosition() - 1] = p.getUserId();
		}
		// userIds[p.getPosition() - 1] = p.getUserId();
		p.initPlayer(p.getRoomId(), p.getPosition(), p.getZhuang(),
				Cnst.PLAYER_STATE_IN, p.getScore(), p.getHuNum(),
				p.getLoseNum());

		p.setJoinIndex(jionIndex + 1);

		Map<String, Object> info = new HashMap<>();
		info.put("reqState", Cnst.REQ_STATE_1);
		info.put("playerNum", jionIndex + 1);
		info.put("roomSn", roomId);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		// 更新redis数据
		updateRedisData(room, p);

		session.write(pd);

	}

	/**
	 * 产生随机的风
	 * 
	 * @param maxNum
	 * @param players
	 * @return
	 */
	private static Integer getWind(Long[] userIds, Integer maxNum) {
		List<Integer> ps = new ArrayList<>();
		if (maxNum == 4) {// 4人玩家
			ps.add(Cnst.WIND_EAST);
			ps.add(Cnst.WIND_SOUTH);
			ps.add(Cnst.WIND_WEST);
			ps.add(Cnst.WIND_NORTH);
		} else if (maxNum == 3) {// 3人玩家
			ps.add(Cnst.WIND_EAST);
			ps.add(Cnst.WIND_SOUTH);
			ps.add(Cnst.WIND_WEST);
		} else if (maxNum == 2) {// 2人玩家，东西
			ps.add(Cnst.WIND_EAST);// 1
			ps.add(Cnst.WIND_WEST);// 3
		}
		if (userIds != null) {
			for (int i = userIds.length - 1; i >= 0; i--) {
				if (userIds[i] != null) {
					ps.remove(i);
				}
			}
		}
		return ps.get(CommonUtil.getRamdonInNum(ps.size()));
	}

}
