package com.orange.game.traffic.server;

import java.util.List;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.orange.common.log.ServerLog;
import com.orange.common.network.ChannelUserManager;
import com.orange.game.traffic.messagehandler.AbstractMessageHandler;
import com.orange.game.traffic.model.dao.GameSession;
import com.orange.game.traffic.model.manager.GameSessionManager;
import com.orange.game.traffic.model.manager.GameUserManager;
import com.orange.game.traffic.statemachine.GameEvent;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameResultCode;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;

public abstract class GameServerHandler extends SimpleChannelUpstreamHandler {
	private static final Logger logger = Logger.getLogger(GameServerHandler.class
			.getName()); 
	
	
	
	public enum DisconnectReason {
		EXCEPTION,
		EXCEPTION_HANDLE_UP_STREAM,
		CHANNEL_DISCONNECTED,
		CHANNEL_CLOSED
	};
	
//	public abstract void userQuitSession(String userId,
//			GameSession session, boolean needFireEvent);
	
	private void processDisconnectChannel(Channel channel, DisconnectReason reason) {

		GameSessionManager sessionManager = GameEventExecutor.getInstance().getSessionManager();
		
		// remove users in channel
		List<String> userIdList = ChannelUserManager.getInstance().findUsersInChannel(channel);
		for (String userId : userIdList){
			int sessionId = GameUserManager.getInstance().findGameSessionIdByUserId(userId);
			if (sessionId != -1){
				GameSession session = sessionManager.findSessionById(sessionId);				
				sessionManager.userQuitSession(session, userId, true, false);
			}

			GameUserManager.getInstance().removeUser(userId);
		}
		
		// remove channel
		ChannelUserManager.getInstance().removeChannel(channel);				
	} 
 
	
	abstract public AbstractMessageHandler getMessageHandler(MessageEvent event);
	
	public GameEvent toGameEvent(GameMessage gameMessage, MessageEvent messageEvent, AbstractMessageHandler handler){
		return new GameEvent(
				gameMessage.getCommand(), 
				(int)gameMessage.getSessionId(), 
				gameMessage, 
				messageEvent.getChannel(),
				handler);
	}

	private void handleMessage(ChannelHandlerContext ctx, MessageEvent e){
		GameMessage message = (GameMessageProtos.GameMessage)e.getMessage();
		ServerLog.info((int)message.getSessionId(), "<==".concat(message.getCommand().toString()));
		ServerLog.debug((int)message.getSessionId(), "<messageReceived> ".concat(message.toString()));
						
		AbstractMessageHandler handler = getMessageHandler(e);
		if (handler == null)
			return;

		if (!handler.verfiySecurity(message)){
			sendErrorResponse(e, message, GameResultCode.ERROR_SYSTEM_SECURITY_CHECK);
			return;
		}
		
		if (handler.isProcessIgnoreSession() || message.hasSessionId() == false){
			if (handler.isProcessForSessionAllocation()){
				// do this to make a single thread for session allocation & de-allocation
				GameEventExecutor.getInstance().executeForSessionAllocation(handler, message, e.getChannel());
			}
			else{
				
				handler.handleRequest(message, e.getChannel(), null);
			}
		}		
		else {
			GameEventExecutor.getInstance().dispatchEvent(toGameEvent(message, e, handler));
		}		
	}
	
	public static long MIN_PROCESS_LATENCY = 800;
	public static long SLOW_PROCESS_LATENCY = 2000;
	public volatile static boolean processMessageSlow = false;
	public volatile static long processMessageSlowCount = 0;
	public volatile static long MIN_TOO_SLOW_COUNT = 50;
	public volatile static long lastTooSlowMessageTime= System.currentTimeMillis();
	public volatile static long SLOW_MESSAGE_INTERVAL = 60*1000; // in 60 seconds
	
	public static boolean isProcessMessageTooSlow(){
		return processMessageSlow;
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		
		long startTime = System.currentTimeMillis();		
		
		// process message here
		handleMessage(ctx, e);
		
		// check process time and print log & update counter
		long endTime = System.currentTimeMillis();
		long latency = endTime - startTime;
		if (latency >= MIN_PROCESS_LATENCY){
			ServerLog.warn(0, "<messageReceived> process message too long ("+latency+" ms), message="+e.getMessage().toString());
		}
		
		if (latency >= SLOW_PROCESS_LATENCY){
			
			ServerLog.warn(0, "<messageReceived> process message reach too slow ("+latency+" ms)!!!!!!!!! need to restart server!!!");
			if (endTime - lastTooSlowMessageTime > SLOW_MESSAGE_INTERVAL){
				processMessageSlowCount = 1;  // reset counter if last slow count is too long ago
			}
			else{
				processMessageSlowCount ++;				
			}
			
			lastTooSlowMessageTime = endTime;			
			if (processMessageSlowCount > MIN_TOO_SLOW_COUNT){
				processMessageSlow = true;
			}
		}

	}
	
	public void sendErrorResponse(MessageEvent messageEvent, GameMessage request, GameResultCode resultCode){
		
		GameMessageProtos.GameMessage response = GameMessageProtos.GameMessage.newBuilder()
			.setCommand(HandlerUtils.getResponseCommandByRequest(request.getCommand()))
			.setMessageId(request.getMessageId())
			.setResultCode(resultCode)
			.build();

		ServerLog.info((int)response.getSessionId(), resultCode.toString());
		messageEvent.getChannel().write(response);
	}
	
	@Override
	public void exceptionCaught( ChannelHandlerContext ctx, ExceptionEvent e) {
		logger.error("<exceptionCaught> catch unexpected exception at " + e.getChannel().toString() + ", cause=" + e.getCause().getMessage(), e.getCause());
		processDisconnectChannel(e.getChannel(), DisconnectReason.EXCEPTION);
	}				
	
	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
            ChannelStateEvent e){
		processDisconnectChannel(e.getChannel(), DisconnectReason.CHANNEL_DISCONNECTED);
	}
	
	@Override
	public void channelClosed(ChannelHandlerContext ctx,
            ChannelStateEvent e){
		processDisconnectChannel(e.getChannel(), DisconnectReason.CHANNEL_CLOSED);
	}
	
	@Override
	public void channelConnected(ChannelHandlerContext ctx,
            ChannelStateEvent e){
		ChannelUserManager.getInstance().addChannel(e.getChannel());		
	}

	

}
