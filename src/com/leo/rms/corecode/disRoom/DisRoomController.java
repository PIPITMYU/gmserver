package com.leo.rms.corecode.disRoom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.mina.core.session.IoSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.server.mina.bean.ProtocolData;
import com.up72.server.mina.function.MessageFunctions;
import com.up72.server.mina.function.TCPGameFunctions;
import com.up72.server.mina.main.MinaServerManager;

@Controller
@RequestMapping("/disRoom")
public class DisRoomController {

	/**
	 * 请求在线人数
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/open/index",method = RequestMethod.POST,produces = "application/json")
	public String onlineNum(Model model,HttpServletRequest request,HttpServletResponse response){
		Map<String, Object> infos = new HashMap<String, Object>();
		try {
			String roomSn = request.getParameter("roomSn");
			System.out.println("*******强制解散房间"+roomSn);
			if (roomSn!=null) {
				Integer roomId = Integer.valueOf(roomSn);
				RoomResp room = TCPGameFunctions.getRoomRespByRoomId(String.valueOf(roomId));
				if (room!=null) {
					room.setStatus(Cnst.ROOM_STATE_YJS);
					List<Player> players = TCPGameFunctions.getPlayerList(room);
					MessageFunctions.setOverInfo(room,players);
					MessageFunctions.updateDatabasePlayRecord(room);
					if (players!=null&&players.size()>0) {
						for(Player p:players){
					        p.initPlayer(null,null,null,Cnst.PLAYER_STATE_DATING,0,0,0);
					        IoSession se = MinaServerManager.tcpServer.getSessions().get(p.getSessionId());
                            if(se!=null&&se.isConnected()){
                                MessageFunctions.interface_100100(se, new ProtocolData(100100,"{\"interfaceId\":\"100100\",\"userId\":\""+p.getUserId()+"\"}"));
                            }
				        }
					}
				}else{
					System.out.println("*******强制解散房间"+roomSn+"，房间不存在");
				}
			}
			
			infos.put("success", 1);
		} catch (Exception e) {
			infos.put("success", 0);
		}
		return JSONObject.toJSONString(infos);
	}
	
}
