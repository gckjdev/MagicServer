package com.orange.game.traffic.server;

import org.jboss.netty.channel.Channel;

import com.orange.common.log.ServerLog;
import com.orange.game.traffic.model.dao.GameUser;
import com.orange.game.traffic.statemachine.GameEvent;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameResultCode;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;

public class HandlerUtils {

//	protected static final Logger logger = Logger.getLogger(HandlerUtils.class.getName());

	private static void safeWrite(Channel channel, GameMessage message){
		if (channel.isConnected() && channel.isWritable()){
			channel.write(message);
		}
		else{
			ServerLog.warn((int)message.getSessionId(), "send message by channel "+channel.toString()+" but not connected or writable");
		}
	}
	
	private static void logSendMessage(GameMessage message){
		ServerLog.info((int)message.getSessionId(), message.getCommand().toString().concat(" ==> ")
				.concat(message.getToUserId())); 
	}
	
	
	private static void logSendMessage(GameMessage message, String userId) {
		ServerLog.info((int)message.getSessionId(), message.getCommand().toString().concat(" ==> ")
				.concat(userId)); 
	}


	
	public static void sendMessage(GameMessage message, Channel channel) {
		if (message == null || channel == null)
			return;
		
		ServerLog.debug((int)message.getSessionId(), message.toString());
		logSendMessage(message);
		safeWrite(channel, message);
	}
	
	public static void sendMessage(GameMessage message, GameUser gameUser) {
		if (message == null || gameUser.getChannel() == null)
			return;
		
		String id;
		ServerLog.debug((int)message.getSessionId(), message.toString());
		if ( (id = gameUser.getNickName()) == null ) 
			id = gameUser.getUserId();
		logSendMessage(message, id);
		safeWrite(gameUser.getChannel(), message);
	}

	
	public static void sendMessageWithResultCode(GameMessage requestMessage, GameResultCode resultCode, Channel channel){
		if (requestMessage == null || channel == null)
			return;
		
		GameMessageProtos.GameMessage response = GameMessageProtos.GameMessage.newBuilder()
			.setCommand(HandlerUtils.getResponseCommandByRequest(requestMessage.getCommand()))
			.setMessageId(requestMessage.getMessageId())
			.setResultCode(resultCode)
			.build();

		ServerLog.debug((int)response.getSessionId(), response.toString());
		logSendMessage(response);
		safeWrite(channel, response);
	}
	
	public static void sendMessage(GameEvent gameEvent, GameMessage message, Channel channel) {
		if (gameEvent == null || message == null || channel == null)
			return;
		
		ServerLog.debug((int)message.getSessionId(), message.toString());
		logSendMessage(message);
		safeWrite(channel, message);
	}
	
	// A diffrent name to avoid ambiguity?
	public static void sendResponse(GameEvent gameEvent, GameMessage response) {
		if (gameEvent == null || response == null || gameEvent.getChannel() == null)
			return;
		
		ServerLog.debug((int)response.getSessionId(), response.toString());
		logSendMessage(response);
		safeWrite(gameEvent.getChannel(), response);

	}
	
	public static GameCommandType getResponseCommandByRequest(GameCommandType requestCommand){
		return GameCommandType.valueOf(requestCommand.getNumber() + 1);
	}
	
	public static void sendErrorResponse(GameEvent gameEvent, GameResultCode resultCode){
		
		GameMessage request = gameEvent.getMessage();
		
		GameCommandType command = HandlerUtils.getResponseCommandByRequest(request.getCommand());
		if (command == null)
			return;
		
		GameMessageProtos.GameMessage response = GameMessageProtos.GameMessage.newBuilder()
			.setCommand(command)
			.setMessageId(request.getMessageId())
			.setResultCode(resultCode)
			.build();

		sendResponse(gameEvent, response);		
	}
	
	public static void sendErrorResponse(GameMessage erroneousRequest, GameResultCode resultCode, Channel channel){
		
		if (erroneousRequest == null || channel == null)
			return;
		
		GameMessageProtos.GameMessage response = GameMessageProtos.GameMessage.newBuilder()
			.setCommand(HandlerUtils.getResponseCommandByRequest(erroneousRequest.getCommand()))
			.setMessageId(erroneousRequest.getMessageId())
			.setResultCode(resultCode)
			.build();

		ServerLog.debug((int)response.getSessionId(), response.toString());
		logSendMessage(response);
		safeWrite(channel, response);
	}
}

