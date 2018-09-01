package com.up72.game.constant;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import com.up72.game.utils.ProjectInfoPropertyUtil;

/**
 * 常量
 */
public class Cnst {
	
	// 获取项目版本信息
    public static final String version = ProjectInfoPropertyUtil.getProperty("project_version", "1.5");
    public static final String cid = ProjectInfoPropertyUtil.getProperty("cid", "1");
    public static String SERVER_IP = getLocalAddress();
    public static String HTTP_URL = "http://".concat(Cnst.SERVER_IP).concat(":").concat(ProjectInfoPropertyUtil.getProperty("httpUrlPort", "8086")).concat("/");

    public static String getLocalAddress(){
		String ip = "";
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return ip;
	}
    
    public static Boolean isTest = true;//是否是测试环境
    
    //回放文件目录
    public static final String BACK_FILE_PATH = ProjectInfoPropertyUtil.getProperty("backFilePath", "1.5");
    public static final String FILE_ROOT_PATH = ProjectInfoPropertyUtil.getProperty("fileRootPath", "1.5");

    public static final String p_name = ProjectInfoPropertyUtil.getProperty("p_name", "wsw_X1");
    public static final String o_name = ProjectInfoPropertyUtil.getProperty("o_name", "u_consume");
    public static final String gm_url = ProjectInfoPropertyUtil.getProperty("gm_url", "");
    
    //redis配置
    public static final String REDIS_HOST = ProjectInfoPropertyUtil.getProperty("redis.host", "localhost");
    public static final String REDIS_PORT = ProjectInfoPropertyUtil.getProperty("redis.port", "8998");
    public static final String REDIS_PASSWORD = ProjectInfoPropertyUtil.getProperty("redis.password", "");

    //mina的端口
    public static final String MINA_PORT = ProjectInfoPropertyUtil.getProperty("mina.port", "");
    
    
    public static final String rootPath = ProjectInfoPropertyUtil.getProperty("rootPath", "");

    
    
    public static final long HEART_TIME = 17000;
    
    public static final long ROOM_OVER_TIME = 5*60*60*1000;//房间定时24小时解散
    public static final long ROOM_CREATE_DIS_TIME = 40*60*1000;//创建房间之后，40分钟解散
    public static final long ROOM_DIS_TIME = 5*60*1000;//玩家发起解散房间之后，5分钟自动解散
    public static final String CLEAN_3 = "0 0 3 * * ?";
    public static final String CLEAN_EVERY_HOUR = "0 0 0/1 * * ?";
    public static final String COUNT_EVERY_TEN_MINUTE = "0 0/1 * * * ?";
    public static final long BACKFILE_STORE_TIME = 3*24*60*60*1000;//回放文件保存时间
    public static final int MONEY_INIT = 12;//初始赠送给用户的房卡数

    
    //测试时间
//    public static final long ROOM_OVER_TIME = 60*1000;//
//    public static final long ROOM_CREATE_DIS_TIME = 20*1000;
//    public static final long ROOM_DIS_TIME = 10*1000;
//	public static final String CLEAN_3 = "0/1 * * * * ?";
//	public static final String CLEAN_EVERY_HOUR = "0/1 * * * * ?";
//    public static final String COUNT_EVERY_TEN_MINUTE = "0/5 * * * * ?";
//    public static final long BACKFILE_STORE_TIME = 60*1000;//回放文件保存时间
    
    public static final int DIS_ROOM_RESULT = 1;

    public static final int DIS_ROOM_TYPE_1 = 1;//创建房间40分钟解散类型
    public static final int DIS_ROOM_TYPE_2 = 2;//玩家点击解散房间类型

    public static final int PAGE_SIZE = 10;

    //风向表示
    public static final int WIND_EAST = 1;//东
    public static final int WIND_SOUTH = 2;//南
    public static final int WIND_WEST = 3;//西
    public static final int WIND_NORTH = 4;//北

    public static final String USER_SESSION_USER_ID = "user_id";
    public static final String USER_SESSION_IP = "ip";

    //房间状态
    // 1等待玩家入坐；2游戏中；3小结算
    public static final int ROOM_STATE_CREATED = 1;
    public static final int ROOM_STATE_GAMIING = 2;
    public static final int ROOM_STATE_XJS = 3;
    public static final int ROOM_STATE_YJS = 4;

    //房间类型
    public static final int ROOM_TYPE_1 = 1;//房主模式
    public static final int ROOM_TYPE_2 = 2;//自由模式


    //小局结算时的
    public static final int OVER_TYPE_1 = 1;//胜利
    public static final int OVER_TYPE_2 = 2;//失败
    public static final int OVER_TYPE_3 = 3;//荒庄


    //开房的局数对应消耗的房卡数
    public static final Map<Integer,Integer> twoMoneyMap = new HashMap<>();// 二人麻将
    public static final Map<Integer,Integer> threeMoneyMap = new HashMap<>();//3人麻将
    public static final Map<Integer,Integer> fourMoneyMap = new HashMap<>();//四人麻将
    static {
    	//二人麻将
    	twoMoneyMap.put(6,4);
    	twoMoneyMap.put(12,4);
    	twoMoneyMap.put(26,8);
    	//三人麻将
    	threeMoneyMap.put(5,3);
    	threeMoneyMap.put(10,6);
    	threeMoneyMap.put(20,9);
    	//四人麻将
    	fourMoneyMap.put(2,4);
    	fourMoneyMap.put(4,8);
    	fourMoneyMap.put(8,16);
    }

    //玩家在线状态
    public static final String PLAYER_LINE_STATE_INLINE = "inline";
    public static final String PLAYER_LINE_STATE_OUT = "out";

    //玩家状态
    public static final String PLAYER_STATE_DATING = "dating";
    public static final String PLAYER_STATE_IN = "in";
    public static final String PLAYER_STATE_PREPARED = "prepared";
    public static final String PLAYER_STATE_CHU = "chu";
    public static final String PLAYER_STATE_WAIT = "wait";
    public static final String PLAYER_STATE_XJS = "xjs";

    //请求状态
    public static final int REQ_STATE_0 = 0;//非法请求
    public static final int REQ_STATE_1 = 1;//正常
    public static final int REQ_STATE_2 = 2;//余额不足
    public static final int REQ_STATE_3 = 3;//已经在其他房间中
    public static final int REQ_STATE_4 = 4;//房间不存在
    public static final int REQ_STATE_5 = 5;//房间人员已满
    public static final int REQ_STATE_6 = 6;//游戏中，不能退出房间
    public static final int REQ_STATE_7 = 7;//有玩家拒绝解散房间
    public static final int REQ_STATE_8 = 8;//玩家不存在（代开模式中，房主踢人用的）
    public static final int REQ_STATE_9 = 9;//接口id不符合，需请求大接口
    public static final int REQ_STATE_10 = 10;//代开房间创建成功
    public static final int REQ_STATE_11 = 11;//已经代开过10个了，不能再代开了
    public static final int REQ_STATE_12 = 12;//房间存在超过24小时解散的提示
    public static final int REQ_STATE_13 = 13;//房间40分钟未开局解散提示
    
    //俱乐部
    public static final int REQ_STATE_14 = 14;//ip不一致

    //动作列表
    public static final int ACTION_HU = 4;
    public static final int ACTION_GANG = 3;
    public static final int ACTION_PENG = 2;
    public static final int ACTION_CHI = 1;
    public static final int ACTION_GUO = 0;
	public static final int ACTION_TING = 5;

    //牌局底分
    public static final int SCORE_BASE = 1;


    //胡牌类型
    //1:基本分数
    public static final int HUTYPE_PINGHU = 1;//平胡
    public static final int HUTYPE_DANDIAO = 4;//单吊   只有单吊1张牌的时候才叫单吊   特殊4567吊4和7两张牌的时候不算   
    public static final int HUTYPE_JIAHU = 5;//夹胡  例子：13夹2 特殊：12胡3和89胡7也叫夹胡
    public static final int HUTYPE_DUIDAO = 6;//对倒   连个对子
    public static final int HUTYPE_QIXIAODUI = 7;//七小对
    public static final int HUTYPE_PIAO = 9;//飘胡   不能有吃，手中都是3个的
    public static final int  HUTYPE_HONGZHONG= 26;//红中 (分数需要翻倍)
//    public static final int  HUTYPE_DUIDAOHONGZHONG= 25;//对倒红中
//    public static final int HUTYPE_DANDIAOHONGZHONG = 17;//单吊红中  
    //番数=护法番数+碰杠的番数
    //胡番番数
    public static final int HUTYPE_QINGYISE = 8;//清一色  一种牌，包括吃和碰
    public static final int HUTYPE_KOU = 27;//扣牌，赢钱翻倍
    public static final int HUTYPE_SANJIAQING = 10;//三家清    
    public static final int HUTYPE_SIJIAQING = 11;//四家清    
    public static final int HUTYPE_MINGGANGSHANGKAIHUA = 12;//明杠杠上开花    
    public static final int HUTYPE_MINGGANGHOUDIANPAO = 13;//明杠杠后点炮    
    public static final int HUTYPE_QIANGGANGHU = 14;//抢杠胡    
    public static final int HUTYPE_SIGUIYI = 15;//四归一    手里加上开门的牌中一种牌为4张（如：4个一万   123   111），可以叠加
    public static final int HUTYPE_LIANZHUANG = 16;//荒庄
    public static final int HUTYPE_ANGANGSHANGKAIHUA = 18;//暗杠杠上开花    
    public static final int HUTYPE_ANGANGHOUDIANPAO = 19;//暗杠杠后点炮    
    //碰杠的番数
    public static final int  HUTYPE_GANG_TYPE_MING = 20;//明杠番数
    public static final int  HUTYPE_GANG_TYPE_AN = 21;//暗杠番数
    public static final int  HUTYPE_GANG_HONGZHONG_MING = 22;//红中的杠分必须乘以此值
    public static final int  HUTYPE_GANG_HONGZHONG_AN = 23;//红中的杠分必须乘以此值
    public static final int  HUTYPE_PENG_HONGZHONG_FAN = 24;//只有红中碰的时候才算番
    
    //房间的上线番数
    public static final int MAX_SCORE_4 = 4;
    public static final int MAX_SCORE_8 = 8;
    public static final int MAX_SCORE_16 = 16;

    public static final Map<Integer,Integer> huScore = new HashMap<>();
    static {
        huScore.put(1,1);
        huScore.put(2,2);
        huScore.put(3,2);
    }
    //胡牌分数（基本分之和 *番数分1*番数分2....
    //基本分（需要相加）
    public static final int HU_FAN_PINGHU = 2;//平胡
    public static final int HU_FAN_DANDIAO = 4;//单吊   只有单吊1张牌的时候才叫单吊   特殊4567吊4和7两张牌的时候不算   
    public static final int HU_FAN_JIAHU = 4;//夹胡  例子：13夹2 特殊：12胡3和89胡7也叫夹胡
    public static final int HU_FAN_DUIDAO = 2;//对倒   连个对子
    public static final int HU_FAN_QIXIAODUI = 16;//七小对
    public static final int HU_FAN_PIAO = 16;//飘胡   不能有吃，手中都是3个的
//    public static final int HU_FAN_DUIDAOHONGZHONG= 4;//对倒红中
//    public static final int HU_FAN_DANDIAOHONGZHONG = 8;//单吊红中   
    public static final int  HU_FAN__HONGZHONG= 2;//红中 (分数需要翻倍)

    
    
    //番数分，需要相乘
    public static final int HU_FAN_QINGYISE = 2;//清一色  一种牌，包括吃和碰
    public static final int HU_FAN_SANJIAQING = 2;//三家清    
    public static final int HU_FAN_SIJIAQING = 4;//四家清    
    public static final int HU_FAN_MINGGANGSHANGKAIHUA = 1;//杠上开花    
    public static final int HU_FAN_MINGGANGHOUDIANPAO = 1;//杠后点炮    
    public static final int HU_FAN_ANGANGSHANGKAIHUA = 2;//暗杠杠上开花    
    public static final int HU_FAN_ANGANGHOUDIANPAO = 2;//暗杠杠后点炮
    public static final int HU_FAN_QIANGGANGHU = 1;//抢杠胡    
    public static final int HU_FAN_SIGUIYI = 1;//四归一    手里加上开门的牌中一种牌为4张（如：4个一万   123   111），可以叠加
    public static final int HU_FAN_LIANZHUANG = 1;//连庄    
    public static final int HU_FAN_KOU = 1;//扣牌，赢钱翻倍


    
    
    
    
    public static Integer getFan(int type){
    	int fan = 0;
    	switch (type) {
    	case HUTYPE_PINGHU:						fan = HU_FAN_PINGHU;break;
    	case HUTYPE_DANDIAO:					fan = HU_FAN_DANDIAO;break;
//    	case HUTYPE_DANDIAOHONGZHONG:			fan = HU_FAN_DANDIAOHONGZHONG;break;
    	case HUTYPE_JIAHU:						fan = HU_FAN_JIAHU;break;
    	case HUTYPE_DUIDAO:						fan = HU_FAN_DUIDAO;break;
    	case HUTYPE_QIXIAODUI:					fan = HU_FAN_QIXIAODUI;break;
    	case HUTYPE_QINGYISE:					fan = HU_FAN_QINGYISE;break;
    	case HUTYPE_PIAO:						fan = HU_FAN_PIAO;break;
    	case HUTYPE_SANJIAQING:					fan = HU_FAN_SANJIAQING;break;
    	case HUTYPE_SIJIAQING:					fan = HU_FAN_SIJIAQING;break;
        case HUTYPE_MINGGANGSHANGKAIHUA:			fan = HU_FAN_MINGGANGSHANGKAIHUA;break;
	    case HUTYPE_MINGGANGHOUDIANPAO:				fan = HU_FAN_MINGGANGHOUDIANPAO;break;
	    case HUTYPE_ANGANGSHANGKAIHUA:			fan = HU_FAN_ANGANGSHANGKAIHUA;break;
	    case HUTYPE_ANGANGHOUDIANPAO:				fan = HU_FAN_ANGANGHOUDIANPAO;break;
	    case HUTYPE_QIANGGANGHU:				fan = HU_FAN_QIANGGANGHU;break;
    	case HUTYPE_SIGUIYI:					fan = HU_FAN_SIGUIYI;break;
    	case HUTYPE_LIANZHUANG:					fan = HU_FAN_LIANZHUANG;break;
    	//碰,杠
    	case HUTYPE_GANG_TYPE_MING:					fan = GANG_TYPE_MING;break;
    	case HUTYPE_GANG_TYPE_AN:					fan = GANG_TYPE_AN;break;
    	case HUTYPE_GANG_HONGZHONG_MING:			fan = GANG_HONGZHONG_MING;break;
    	case HUTYPE_GANG_HONGZHONG_AN:					fan = GANG_HONGZHONG_AN;break;
    	case HUTYPE_PENG_HONGZHONG_FAN:					fan = PENG_HONGZHONG_FAN;break;
//    	case HUTYPE_DUIDAOHONGZHONG:                     fan=HU_FAN_DUIDAOHONGZHONG;break;
		}
    	return fan;
    }
    
    
    
    
    //碰分计算
    public static final int PENG_HONGZHONG_FAN = 1;//只有红中碰的时候才算番

    //杠分数计算
    public static final int GANG_TYPE_MING = 1;//明杠番数
    public static final int GANG_TYPE_AN = 2;//暗杠番数
    public static final int GANG_HONGZHONG_MING = 2;//红中的杠分必须乘以此值
    public static final int GANG_HONGZHONG_AN = 4;//红中的杠分必须乘以此值
   
   
    //退出类型
    public static final String EXIST_TYPE_EXIST = "exist";
    public static final String EXIST_TYPE_DISSOLVE = "dissolve";
    
    
    
    //redis存储的key的不同类型的前缀
    public static final String REDIS_PREFIX_ROOMMAP = "HONGZHONG_ROOM_MAP_";//房间信息
//    public static final String REDIS_PREFIX_USERROOMNUMBERMAP = "USER_ROOMNUM_MAP_";//用户房间号码信息
//    public static final String REDIS_PREFIX_ROOMUSERMAP = "ROOM_USERS_MAP_";//房间人员信息
//    public static final String REDIS_PREFIX_IOSESSIONMAP = "IOSESSION_MAP_";//玩家——session数据
    public static final String REDIS_PREFIX_OPENIDUSERMAP = "HONGZHONG_OPENID_USERID_MAP_";//openId-user数据
    
//    public static final String REDIS_PREFIX_DISROOMIDMAP = "DIS_ROOMID_MAP_";//解散房间的任务
//    public static final String REDIS_PREFIX_DISROOMIDRESULTINFO = "DIS_ROOM_RESULT_MAP_";//房间解散状态集合
    
    public static final String REDIS_PREFIX_USER_ID_USER_MAP = "HONGZHONG_USER_ID_USER_MAP_";//通过userId获取用户
    
    //redis中通知的key
    public static final String NOTICE_KEY = "HONGZHONG_NOTICE_KEY";
    
    public static final String REDIS_ONLINE_NUM_COUNT = "HONGZHONG_ONLINE_NUM_";
    
    public static final String PROJECT_PREFIX = "HONGZHONG_*";
    
    //俱乐部
    public static final String REDIS_PREFIX_CLUBMAP = "HONGZHONG_CLUB_MAP_";//东风俱乐部信息
    public static final String REDIS_PREFIX_CLUBMAP_LIST = "HONGZHONG_CLUB_MAP_LIST_";//存放俱乐部未开局房间信息
    //俱乐部的局数对应消耗的房卡数
    public static final Map<Integer,Integer> clubMoneyMap = new HashMap<>();
    static {
        clubMoneyMap.put(2,4);
        clubMoneyMap.put(4,8);
        clubMoneyMap.put(8,16);
    }
    
    public static final String REDIS_PAY_ORDERNUM = "HONGZHONG_PAY_ORDERNUM";//充值订单号
    
    
}
