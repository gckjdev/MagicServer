package com.orange.game.traffic.config;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

public class ConfigServer {
	private static final Logger logger = Logger.getLogger(ConfigServer.class
			.getName());

	private static final String DEFAULT_PASSWORD = "gckj";
	
	boolean isAuthenticate = false;
	boolean isAskingAuthenticate = false;
	
	public static int getPort() {
		String port = System.getProperty("config_server.port");
		if (port != null && !port.isEmpty()){
			return Integer.parseInt(port);
		}
		return 7080; // default
	}
	
	private static ConfigServer server = new ConfigServer();

	private ConfigServer() {
	}

	public static ConfigServer getInstance() {
		return server;
	}
	
	public void start(){
				
		ServerBootstrap bootstrap = new ServerBootstrap(
				new NioServerSocketChannelFactory(
					Executors.newCachedThreadPool(),
					Executors.newCachedThreadPool()				
				));
		
		bootstrap.setPipelineFactory(new ConfigServerPipelineFactory(new ConfigServerHandler()));
		
		bootstrap.bind(new InetSocketAddress(getPort()));
		logger.info("Start Config Server At Port "+getPort());		
			
	}	
	
	public boolean authenticate(String password){	 
		if (password == null)
			return false;
		if (password.equalsIgnoreCase(DEFAULT_PASSWORD)){
			isAskingAuthenticate = false;
			return true;
		}
		else{
			return false;
		}
	}
	
	public boolean isAuthenticate(){
		return isAuthenticate;
	}
	
	public boolean isAskAuthenticate(){
		return isAskingAuthenticate;
	}
}
