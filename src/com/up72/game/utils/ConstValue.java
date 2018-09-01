package com.up72.game.utils;

public class ConstValue {

    public static String ip = "127.0.0.1";
    public static final int Session_Read_BufferSize = 65536;
    public static final int Session_life = 60;
    public static final String mappersClass="com.up72.game.mappers";
    /***/
    public static final boolean isDevoloping = true;
    public static final int WriteTimeOut = 500;

    public static final String session_用户 = "user";



    public static final int protocol_200110 = 200110;
    public static final int protocol_100000 = 100000;//=========0315

    public static final int result_正确 = 0;
    public static final int result_错误 = 1;
    public static final int result_异常 = 1000;
    public static final int result_已存 = 1001;
    public static final int result_密码 = 1002;
    public static final int result_参误 = 1003;
    public static final int result_求重 = 1004;
    public static final int result_未登 = 1005;
    public static final int result_引败 = 1006;
    public static final int result_fid错 = 1008;
    public static final int result_余额 = 1009;
    public static final int result_占用 = 1016;
    public static final int result_未加 = 1017;
    public static final int result_已加 = 1018;
    public static final int result_无aid = 1019;
    public static final int result_无mov = 1020;
    public static final int result_未养 = 1021;
    public static final int result_已养 = 1022;
    public static final int result_进度 = 1023;
    public static final int result_out = 1;//离线
    public static final int result_play = 3;//游戏中
    public static final int result_be_invited = 4;//被邀请
    public static final int result_wait = 5;//等待.
    public static final int result_已满 = 6;//房间已满
    public static final int result_不存 = 7;//不存在
    public static final int result_重新进房 = 8;//玩家重新登录回到游戏房间
    public static final int result_一般链接 = 9;//玩家通过他人分享的roomId链接进来

}
