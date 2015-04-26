
package com.orange.game.traffic.messagehandler.room;

import java.util.List;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;

import com.orange.game.traffic.messagehandler.AbstractMessageHandler;
import com.orange.game.traffic.model.dao.GameSession;
import com.orange.game.traffic.model.manager.RoomsNotificationManager;
import com.orange.game.traffic.server.HandlerUtils;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameResultCode;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;


public class UnRegisterRoomsRequestHandler extends AbstractMessageHandler {
	// Must-implement public construtor to call super constructor.
	public UnRegisterRoomsRequestHandler(MessageEvent messageEvent) {
		super(messageEvent);
	}
	
	private RoomsNotificationManager regRoomsNotifyManager 
	= RoomsNotificationManager.getInstance();
	
	@Override
	public void handleRequest(GameMessage message, Channel channel,
			GameSession session) {

		GameResultCode gameResultCode = null;
		String userId = null;
		List<Integer> toBeUnRegisteredSessionIdList = null;
		
		if (message.hasUserId() == false ) {
			gameResultCode = GameResultCode.ERROR_USERID_NULL;
			HandlerUtils.sendErrorResponse(message, gameResultCode,channel);
			return;
		} else {
			userId = message.getUserId();
		}
		
		toBeUnRegisteredSessionIdList = message.getUnRegisterRoomsNotificationRequest().getSessionIdsList();
	
		gameResultCode = regRoomsNotifyManager.unRegisterSessionIds(userId, toBeUnRegisteredSessionIdList);
		
		HandlerUtils.sendMessageWithResultCode(message, gameResultCode, channel);
	}


	@Override
	public boolean isProcessForSessionAllocation() {
		return false;
	}

	@Override
	public boolean isProcessIgnoreSession() {
		return true;
	}

	
	@Override
	public boolean isProcessInStateMachine() {
		return true;
	}
	

}
