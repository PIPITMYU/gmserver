package com.up72.server.mina.main;

import com.up72.server.mina.function.TCPGameFunctions;
import com.up72.server.mina.tcp.MinaTCPServer;
import com.up72.server.mina.utils.BackFileUtil;
import com.up72.server.mina.utils.DataLoader;
import com.up72.server.mina.utils.TaskUtil;
import com.up72.server.mina.utils.redis.MyRedis;

public class MinaServerManager {

    public static MinaTCPServer tcpServer;
//    protected MinaHttpServer httpServer;

    public MinaServerManager() {
        tcpServer = new MinaTCPServer();
//        httpServer = new MinaHttpServer();
    }

    public void startMinaTCPServer() {
        tcpServer.startServer();
    }

    public void startMinaHTTPServer() {
        //httpServer.startServer();
    }

    public void stopMinaTCPServer() {
        tcpServer.stopServer();
    }

    public void stopMinaHTTPServer() {
        //httpServer.stopServer();
    }

    public void broadcastMessage2TCPClient(Object message) {
        tcpServer.broadcast(message);
    }

    public void startMinaServer() {
        DataLoader.initMybatis();
        //DataLoader.loadInitData();

        //初始化redis
      	MyRedis.initRedis();
      	
      	//初始化俱乐部数据
        TCPGameFunctions.initClubList();
        
        
      	//清理回放文件，避免服务器停服之后，json文件不完整
      	BackFileUtil.deleteAllRecord();
        
        tcpServer.startServer();
        //启动定时任务
        TaskUtil.initTaskSchdual();
//        httpServer.startServer();
    }

    public void stopMinaServer() {
        tcpServer.stopServer();
        //httpServer.startServer();
    }

    public static void main(String[] args) {
        MinaServerManager minaManager = new MinaServerManager();
        minaManager.startMinaServer();
    	
//    	byte[] dd = new byte[]{123, 34, 105, 110, 116, 101, 114, 102, 97, 99, 101, 73,
//    		100, 34, 58, 34, 49, 48, 48, 49, 48, 48, 34, 44, 34, 111, 112, 101, 110, 
//    		73, 100, 34, 58, 34, 122, 99, 95, 52, 34, 44, 34, 117, 115, 101, 114, 73,
//    		100, 34, 58, 110, 117, 108, 108, 44, 34, 99, 73, 100, 34, 58, 49, 125, 
//    		107, -19, -6, -80, 114, 109, -127, -110, 27, 3, -114, -43, 0, 11, -101,
//    		-45, 23, 36, -98, -110, 72, 79, -53, -128, 66, 93, -54, -128, 80, 16
//    	};
//    	
//    	for(byte b:dd){
//    		char c = (char) b;
//    		System.out.print(c);
//    	}
    	
    	
//    	String ss = "{afdsfasfas{}fdsafsfs}{";
//    	System.out.println(ss.substring(0, CommonUtil.getLastIndex(ss)+1));
    	
//    	System.out.println(" ".getBytes()[0]);
    	
    }
    
    
}
