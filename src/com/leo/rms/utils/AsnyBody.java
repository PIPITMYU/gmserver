package com.leo.rms.utils;

//{"success":true,"succMsg":"获取成功","errMsg":null,"succData":{"jsapi_ticket":"sM4AOVdWfPE4DxkXGEs8VJ147211CMoEd-j9ALcWHxDUnDHlvlI5xJP8X3PBkrTvFt8-Rz17ITFztnA476GsvA","nonceStr":"649490d895094ec4","signature":"xUt4XUkyCtct3VIo_WvvyMiqXpR4fCxwK_3CW1TA-86d4rYeEzgOThtK51LX7x3OjiX5PJ426nfzI4_RJVObPr0qYZRex3Wh99rVJ2cTOeriHd-wDAM_ZNXl3beN57LUHGAgAEAKJD","appId":"wxa20917135e6112e5","timestamp":null},"errData":null}
public class AsnyBody {
	// 接口id
	private String interfaceId;
	// 状态
	private Boolean state = Boolean.FALSE;
	// 消息
	private String message = "失败";
	// 内容
	private Object info;
	//其他
	private Object others;
	
	// getter and setter
	public String getInterfaceId() {
		return interfaceId;
	}
	public void setInterfaceId(String interfaceId) {
		this.interfaceId = interfaceId;
	}
	public Boolean getState() {
		return state;
	}
	public void setState(Boolean state) {
		this.state = state;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Object getInfo() {
		return info;
	}
	public void setInfo(Object info) {
		this.info = info;
	}
	public Object getOthers() {
		return others;
	}
	public void setOthers(Object others) {
		this.others = others;
	}
	
}
