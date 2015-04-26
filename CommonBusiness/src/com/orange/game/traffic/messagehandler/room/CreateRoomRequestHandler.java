package com.orange.game.traffic.messagehandler.room;


import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;

import com.orange.game.traffic.messagehandler.AbstractMessageHandler;
import com.orange.game.traffic.model.dao.GameSession;
import com.orange.game.traffic.model.dao.GameUser;
import com.orange.game.traffic.model.manager.GameSessionAllocationManager;
import com.orange.game.traffic.server.HandlerUtils;
import com.orange.game.traffic.service.SessionUserService;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameResultCode;
import com.orange.network.game.protocol.message.GameMessageProtos.CreateRoomResponse;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;
import com.orange.network.game.protocol.model.GameBasicProtos.PBGameSession;
import com.orange.network.game.protocol.model.GameBasicProtos.PBGameUser;

public class CreateRoomRequestHandler extends AbstractMessageHandler {

	public CreateRoomRequestHandler(MessageEvent messageEvent) {
		super(messageEvent);
	}

	@Override
	public void handleRequest(GameMessage message, Channel channel, GameSession requestSession) {

		PBGameUser pbUser = message.getCreateRoomRequest().getUser();

		int sessionId = sessionManager.getUserCreatedSessionIndex();
		
		String roomName = message.getCreateRoomRequest().getRoomName();
		String password = message.getCreateRoomRequest().getPassword();
		
		GameResultCode resultCode = GameResultCode.SUCCESS;
		
//		// Avoid duplicated session name 
//		Map<Integer, GameSession> sessionList = sessionManager.getAllSessionList();
//		for ( Map.Entry<Integer, GameSession> entry: sessionList.entrySet()) {
//			String name = entry.getValue().getName();
//			if ( name != null && name.equals(roomName)) {
//					resultCode = GameResultCode.ERROR_SESSION_NAME_DUPLICATED;
//			}
//		}
		
		// Create session
		int ruleType = sessionManager.getRuleType();
		int testEnable = sessionManager.getTestEnable();
		int maxPlayerCount = sessionManager.getMaxPlayerCount();
		GameSession session = sessionManager.addSession(sessionId, roomName, password, true, pbUser.getUserId(), ruleType, maxPlayerCount,testEnable);
		
		// Add session into allocation queue for later allocation on demand. 
		GameSessionAllocationManager.getInstance().addSession(session);
		
		// Add user into session directly
		GameSessionAllocationManager.getInstance().allocSession(pbUser.getUserId(), sessionId);  

		// Add user into session
		GameUser user = SessionUserService.getInstance().addUserIntoSession(session, pbUser, channel, message);
		user.setInterfaceVersion(message.getCreateRoomRequest().getVersion());
		
		// Make response and then send it.
		GameMessage responseMessage = makeCreatRoomResponse(resultCode, session,message.getMessageId());
		HandlerUtils.sendMessage(responseMessage, channel);
		
		// update user game status
		sessionManager.updateUserEnterGameStatus(session, user);
		
	}

	private GameMessage makeCreatRoomResponse(GameResultCode resultCode,
			GameSession session, int messageId) {
		
		PBGameSession pbGameSession  = session.toPBGameSession(sessionManager);
		
		CreateRoomResponse createRoomResponse = CreateRoomResponse.newBuilder()
				.setGameSession(pbGameSession)
				.build();
		
		GameMessage responseMessage = GameMessage.newBuilder()
				.setCommand(GameCommandType.CREATE_ROOM_RESPONSE)
				.setMessageId(messageId)
				.setCreateRoomResponse(createRoomResponse)
				.setResultCode(resultCode)
				.build();
		
		return responseMessage;
	} 

	
	@Override
	public boolean isProcessForSessionAllocation() {
		return true;
	}

	@Override
	public boolean isProcessIgnoreSession() {
		return true;
	}

	@Override
	public boolean isProcessInStateMachine() {
		return false;
	}

}
