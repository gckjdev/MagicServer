package com.orange.game.traffic.messagehandler;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;

import com.orange.game.traffic.model.dao.GameSession;
import com.orange.game.traffic.server.NotificationUtils;
import com.orange.game.traffic.statemachine.GameEvent;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
import com.orange.network.game.protocol.message.GameMessageProtos.GameChatRequest;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;

public class ChatRequestHandler extends AbstractMessageHandler {

	public ChatRequestHandler(MessageEvent messageEvent) {
		super(messageEvent);
	}

	@Override
	public void handleRequest(GameMessage message, Channel channel, GameSession reqSession) {
		GameChatRequest chatRequest = message.getChatRequest();
		if (chatRequest == null)
			return;				
		
		GameSession session = sessionManager.findSessionById((int)message.getSessionId());
		if (session == null)
			return;

		String fromUserId = message.getUserId();
		
		// broast draw data to all other users in the session
		NotificationUtils.broadcastNotification(session, fromUserId, message);
	}

	@Override
	public boolean isProcessIgnoreSession() {
		return false;
	}

	@Override
	public boolean isProcessInStateMachine() {
		return false;
	}

	@Override
	public boolean isProcessForSessionAllocation() {
		return false;
	}


}
