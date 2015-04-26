package com.orange.game.traffic.server;

import java.net.InetSocketAddress;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.orange.common.log.ServerLog;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.statemachine.StateMachine;
import com.orange.game.model.manager.UserGameStatusManager;
import com.orange.game.traffic.config.ConfigServer;
import com.orange.game.traffic.model.manager.GameSessionAllocationManager;
import com.orange.game.traffic.model.manager.GameSessionManager;
import com.orange.game.traffic.robot.client.AbstractRobotManager;
import com.orange.game.traffic.service.GameDBService;

public class GameServer {

	private static final Logger logger = Logger.getLogger(GameServer.class
			.getName());
	
	private final ChannelHandler handler;
	private final StateMachine stateMachine;
	private final GameSessionManager sessionManager;
	private final AbstractRobotManager robotManager;
	
	private ScheduledExecutorService service = Executors.newScheduledThreadPool(2);
	
	public static int getPort() {
		String port = System.getProperty("server.port");
		if (port != null && !port.isEmpty()){
			return Integer.parseInt(port);
		}
		return 8080; // default
	}
	
	public static String getServerAddress() {
		String serverAddress = System.getProperty("server.address");
		return serverAddress; // default
	}
	
	public static String getServerId() {
		String serverId = System.getProperty("server.server_id");
		return serverId; // default
	}	
	
	public GameServer(ChannelHandler handler, StateMachine sm, GameSessionManager sessionManager, 
			   AbstractRobotManager robotManager){
		this.handler = handler;
		this.stateMachine = sm;
		this.sessionManager = sessionManager;
		this.robotManager = robotManager;
	}
	
	public void resetGameUserStatus(){
		MongoDBClient dbClient = GameDBService.getInstance().getMongoDBClient(0);
		UserGameStatusManager.clearAllUserGameStatus(dbClient);		
	}
	
	
	public void startRobotInfoUpdater() {
		
		// 半夜5:00更新
		int DUE_UPDATE_TIME_HOUR = 5; 
		int DUE_UPDATE_TIME_MINUTE = 0; 

		// 获取当前时间
		Calendar now = Calendar.getInstance();
		int currentHour = now.get(Calendar.HOUR_OF_DAY);
		int currentMinute = now.get(Calendar.MINUTE);
		
		// 计算初次更新机器人数据的时间
		long initialUpdateDelay = 0L; 
		long updatePeriod = 14 * 24 * 60L; // 14 days for update.
		if ( currentHour > DUE_UPDATE_TIME_HOUR ) {
			initialUpdateDelay = ((24 + DUE_UPDATE_TIME_HOUR - currentHour) * 60  + DUE_UPDATE_TIME_MINUTE - currentMinute) * 1L; // 现在距离下个凌晨4点还有多少分钟  
		} else {
			initialUpdateDelay = ((DUE_UPDATE_TIME_HOUR - currentHour) * 60  + DUE_UPDATE_TIME_MINUTE - currentMinute) * 1L; 
		}
		
		String needUpdate = System.getProperty("update_robot_info");
		if (needUpdate != null && needUpdate.equals("1")){
			ServerLog.info(0,"Schedule to periodically update robot info ! The recycle period is " + updatePeriod / 24 / 60 
					+ " days. Next update time is " + initialUpdateDelay / 60 + " hours and " + initialUpdateDelay % 60 + " minutes later.");
			service.scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					robotManager.updateRobotAvatarAndNickName();
					robotManager.reloadRobotUserList();
				}
			}, initialUpdateDelay, updatePeriod, TimeUnit.MINUTES);
			return;
		}

		
		// 所有其他机器需要定时重新加载机器人数据
		// 计算初次重载机器人数据的时间
		long initialReloadDelay = initialUpdateDelay + 5L; // 每次启动后都会读到最新的数据。然后把初次更新的时间定为比预定更新时间慢5分钟。
	   long reloadPeriod = 1 * 24 * 60L; // 1 days for reload.
				
		ServerLog.info(0,"Schedule to periodically load robot users list ! The recycle period is " + reloadPeriod / 24 / 60 
					+ " day. Next reload time is " + initialReloadDelay / 60 + " hours and " + initialReloadDelay % 60 + " minutes later.");
		service.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				robotManager.reloadRobotUserList();
			}
		}, initialReloadDelay, reloadPeriod, TimeUnit.MINUTES);
	}
	
	public void start(){
		
		// init game event executor
		GameEventExecutor.getInstance().setStateMachine(stateMachine);		
		GameEventExecutor.getInstance().setSessionManager(sessionManager);
		
		// init all sessions
		GameSessionAllocationManager.getInstance().setSessionManager(sessionManager);
		sessionManager.addAllSessions();

		// reset game user status for this server
		resetGameUserStatus();
		
		// add process exit hook
		Runtime.getRuntime().addShutdownHook(new Thread() { 
			public void run() { 
				logger.info("===================== SHUTDOWN HOOK CATCH =====================");
				resetGameUserStatus();
				logger.info("===================== SHUTDOWN HOOK COMPLETE =====================");
			} 
		}); 
		
		// start game TCP server
		ServerBootstrap bootstrap = new ServerBootstrap(
				new NioServerSocketChannelFactory(
					Executors.newCachedThreadPool(),
					Executors.newCachedThreadPool()				
				));		
		bootstrap.setPipelineFactory(new GameServerPipelineFactory(handler));		
		bootstrap.bind(new InetSocketAddress(getPort()));
		logger.info("Start TCP Server At Port "+getPort());		
		
		// start monitor server
		ServerMonitor.getInstance().start();

		// start config server
		ConfigServer.getInstance().start();
		
		// start the robots'infos periodical updater(optinal)
        // startRobotInfoUpdater();
	}	
}
