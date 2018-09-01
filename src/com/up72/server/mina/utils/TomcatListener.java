package com.up72.server.mina.utils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.up72.server.mina.main.MinaServerManager;

public class TomcatListener implements ServletContextListener{
	
	private static MinaServerManager minaManager = new MinaServerManager();

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		minaManager.stopMinaTCPServer();
		System.out.println("tomcat关闭了..........");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
        minaManager.startMinaServer();
		System.out.println("tomcate启动了..............");
	}

}