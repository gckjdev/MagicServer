package com.orange.game.traffic.messagehandler;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;

import com.orange.common.log.ServerLog;
import com.orange.common.utils.StringUtil;
import com.orange.game.traffic.model.dao.GameSession;
import com.orange.game.traffic.model.manager.GameSessionManager;
import com.orange.game.traffic.model.manager.GameUserManager;
import com.orange.game.traffic.server.GameEventExecutor;
import com.orange.game.traffic.statemachine.GameEvent;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;

public abstract class AbstractMessageHandler {
	
	protected static final Logger logger = Logger.getLogger(AbstractMessageHandler.class.getName());

	private static final Object SHARE_KEY = "pb_share_key";	

	final GameEventExecutor executor = GameEventExecutor.getInstance();
	protected final GameUserManager userManager = GameUserManager.getInstance();
	protected final GameSessionManager sessionManager = GameEventExecutor.getInstance().getSessionManager();
	
	final Channel channel;
	final GameMessage gameMessage;
	
	public AbstractMessageHandler(MessageEvent messageEvent) {
		this.channel = messageEvent.getChannel();
		this.gameMessage = (GameMessage)messageEvent.getMessage();
	}

	public abstract boolean isProcessIgnoreSession();
	public abstract boolean isProcessInStateMachine();
	public abstract boolean isProcessForSessionAllocation();
	
	public abstract void handleRequest(GameMessage message, Channel channel, GameSession session);
	
	public void sendResponse(GameMessage response){
		if (channel == null || response == null)
			return;

		ServerLog.info((int)response.getSessionId(), "[SEND] ", response.toString());
		ServerLog.info((int)response.getSessionId(), "[SEND] ", 
				response.getCommand().toString(), 
				response.getResultCode().toString());
		
		channel.write(response);
	}
	
//	public void sendErrorResponse(GameResultCode resultCode, GameMessage request){
//		GameCommandType responseCommand = HandlerUtils.getResponseCommandByRequest(request.getCommand());
//		GameMessageProtos.GameMessage response = GameMessageProtos.GameMessage.newBuilder()
//			.setCommand(responseCommand)
//			.setMessageId(request.getMessageId())
//			.setResultCode(resultCode)
//			.build();
//	
//		ServerLog.info((int)response.getSessionId(), "[SEND] ", 
//				response.getCommand().toString(), response.getResultCode().toString());
//		channel.write(response);
//	}
	
	public GameEvent toGameEvent(GameMessage gameMessage){
		return new GameEvent(
				gameMessage.getCommand(), 
				(int)gameMessage.getSessionId(), 
				gameMessage, 
				channel);
	}

	public boolean verfiySecurity(GameMessage message) {
		String clientMac = message.getMac();
		int timeStamp = message.getTimeStamp();
		int messageId = message.getMessageId();
		
		String input = String.format("%d%d%s", messageId, timeStamp, SHARE_KEY);
		String serverMac = StringUtil.md5base64encode(input);
		logger.debug("<verifySecurity> input"+input+", client mac = "+clientMac+", server mac="+serverMac);
		if (StringUtil.isEmpty(clientMac))
			return true;
		
		if (clientMac.equalsIgnoreCase(serverMac))
			return true;
		
		return false;
	}



}