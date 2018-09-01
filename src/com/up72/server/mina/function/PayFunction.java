package com.up72.server.mina.function;

import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.session.IoSession;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.leo.rms.utils.StringUtils;
import com.up72.game.constant.Cnst;
import com.up72.game.service.IPayService;
import com.up72.game.service.impl.PayServiceImpl;
import com.up72.server.mina.bean.ProtocolData;

public class PayFunction extends TCPGameFunctions{
	static IPayService payService = new PayServiceImpl();
	/**
     * 点击支付按钮
     */
    public static void interface_600000(IoSession session, ProtocolData readData) throws Exception{
        logger.I("准备,interfaceId -> 600000");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Object userId = obj.get("userId");
        
        isParameterError(interfaceId, session, StringUtils.isNotEmpty(userId.toString()));
		isParameterError(interfaceId, session, StringUtils.isNumeric(userId.toString()));
		Long sessionUserId = (Long)session.getAttribute(Cnst.USER_SESSION_USER_ID);
		if (!sessionUserId.equals(StringUtils.parseLong(userId))){//不相同
            illegalRequest(interfaceId,session);
            return;
        }
        
        Map<String,Object> info = new HashMap<>();
        Integer daiL = payService.findDaiLbyUserId(StringUtils.parseLong(userId));
        if(daiL==null){
        	info.put("reqState", 0);
        }else{
        	info.put("reqState", 1);
        }
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
        System.out.println("返回绑定代理信息成功**********************");
    }
	/**
     * 用户绑定代理
     */
    public static void interface_600001(IoSession session, ProtocolData readData) throws Exception{
        logger.I("准备,interfaceId -> 600001");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Object userId = obj.get("userId");
        Object inviteCode = obj.get("inviteCode");
        isParameterError(interfaceId, session, StringUtils.isNotEmpty(userId.toString()));
        isParameterError(interfaceId, session, StringUtils.isNotEmpty(inviteCode.toString()));
		isParameterError(interfaceId, session, StringUtils.isNumeric(userId.toString()));
		isParameterError(interfaceId, session, StringUtils.isNumeric(inviteCode.toString()));
		Long sessionUserId = (Long)session.getAttribute(Cnst.USER_SESSION_USER_ID);
		if (!sessionUserId.equals(StringUtils.parseLong(userId))){//不相同
            illegalRequest(interfaceId,session);
            return;
        }
		//查找代理id
        Integer daiL = payService.findDaiLbyCode(StringUtils.parseLong(inviteCode));
        if(daiL==null){
        	//无效邀请码
        	JSONObject error = getJSONObj(interfaceId,0,null);
            error.put("message","邀请码不存在");
            ProtocolData pd = new ProtocolData(interfaceId, error.toJSONString());
            session.write(pd);
            return;
        }
        //绑定代理
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("dail_id", daiL);
        map.put("user_id", StringUtils.parseInt(userId));
        map.put("inviteCode",StringUtils.parseLong(inviteCode) );
        map.put("time", System.currentTimeMillis());
        map.put("white", 0);
        payService.bindDaiL(map);
        
        Map<String,Object> info = new HashMap<>();
        
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
        System.out.println("绑定代理成功**********************");
    }
    /**
     * 用户支付页面
     */
    public static void interface_600002(IoSession session, ProtocolData readData) throws Exception{
        logger.I("准备,interfaceId -> 600002");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Object userId = obj.get("userId");
     
        isParameterError(interfaceId, session, StringUtils.isNotEmpty(userId.toString()));
		isParameterError(interfaceId, session, StringUtils.isNumeric(userId.toString()));	
		Long sessionUserId = (Long)session.getAttribute(Cnst.USER_SESSION_USER_ID);
		if (!sessionUserId.equals(StringUtils.parseLong(userId))){//不相同
            illegalRequest(interfaceId,session);
            return;
        }
		Integer daiL = payService.findDaiLbyUserId(StringUtils.parseLong(userId));
	    if(daiL==null){
	    	JSONObject error = getJSONObj(interfaceId,0,null);
            error.put("message","未绑定代理");
            ProtocolData pd = new ProtocolData(interfaceId, error.toJSONString());
            session.write(pd);
            return;
	     }
        Map<String,Object> info = new HashMap<String,Object>();
        info.put("4", 4);
        info.put("8", 8);
        info.put("12", 12);
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
        System.out.println("跳转支付页面成功**********************");
    }
    /**
     * 用户支付
     */
    public synchronized static void interface_600003(IoSession session, ProtocolData readData) throws Exception{
        logger.I("准备,interfaceId -> 600003");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Object userId = obj.get("userId");
        Object money = obj.get("money");
        Object card = obj.get("card");
        isParameterError(interfaceId, session, StringUtils.isNotEmpty(userId.toString()));
		isParameterError(interfaceId, session, StringUtils.isNumeric(userId.toString()));	
		   isParameterError(interfaceId, session, StringUtils.isNotEmpty(money.toString()));
			isParameterError(interfaceId, session, StringUtils.isNumeric(money.toString()));
			   isParameterError(interfaceId, session, StringUtils.isNotEmpty(card.toString()));
				isParameterError(interfaceId, session, StringUtils.isNumeric(card.toString()));
		Long sessionUserId = (Long)session.getAttribute(Cnst.USER_SESSION_USER_ID);
		if (!sessionUserId.equals(StringUtils.parseLong(userId))){//不相同
            illegalRequest(interfaceId,session);
            return;
        }
		Map<String,Object> info = new HashMap<String, Object>();
		//生成订单号 userId+时间戳
		String orderNum = String.valueOf(userId).concat(String.valueOf(System.currentTimeMillis()));
		//redis中获取订单号
		JSONArray array = TCPGameFunctions.getPayList();
		if(array.contains(orderNum)){
			//重复订单号 
			return;
		}
		array.add(orderNum);
		//更新redis
		TCPGameFunctions.setPayList(array);
		info.put("reqHtml", "www.baidu.com");
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
        System.out.println("跳转支付页面成功**********************");
    }
}
