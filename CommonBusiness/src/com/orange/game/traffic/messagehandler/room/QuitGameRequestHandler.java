package com.orange.game.traffic.messagehandler.room;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;

import com.orange.game.traffic.messagehandler.AbstractMessageHandler;
import com.orange.game.traffic.model.dao.GameSession;
import com.orange.game.traffic.server.GameEventExecutor;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;

public class QuitGameRequestHandler extends AbstractMessageHandler {

	public QuitGameRequestHandler(MessageEvent messageEvent) {
		super(messageEvent);
	}

	@Override
	public void handleRequest(GameMessage message, Channel channel,
			GameSession session) {

		String userId = message.getUserId();
		GameEventExecutor.getInstance().getSessionManager().userQuitSession(session, userId, true, true);
	}

	@Override
	public boolean isProcessForSessionAllocation() {
		return false;
	}

	@Override
	public boolean isProcessIgnoreSession() {
		return false;
	}

	@Override
	public boolean isProcessInStateMachine() {
		// TODO Auto-generated method stub
		return false;
	}

}
