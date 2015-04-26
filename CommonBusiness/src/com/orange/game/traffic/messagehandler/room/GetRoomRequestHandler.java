package com.orange.game.traffic.messagehandler.room;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;

import com.orange.game.traffic.messagehandler.AbstractMessageHandler;
import com.orange.game.traffic.model.dao.GameSession;
import com.orange.game.traffic.model.manager.GameSessionAllocationManager;
import com.orange.game.traffic.model.manager.GameSessionManager;
import com.orange.game.traffic.model.manager.GameUserManager;
import com.orange.game.traffic.server.GameEventExecutor;
import com.orange.game.traffic.server.HandlerUtils;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;
import com.orange.network.game.protocol.message.GameMessageProtos.GetRoomsRequest;
import com.orange.network.game.protocol.message.GameMessageProtos.GetRoomsResponse;
import com.orange.network.game.protocol.model.GameBasicProtos.PBGameSession;

public class GetRoomRequestHandler extends AbstractMessageHandler {

	Logger logger = Logger.getLogger(GetRoomRequestHandler.class);
	
	public GetRoomRequestHandler(MessageEvent messageEvent) {
		super(messageEvent);
	}
	
	@Override
	public void handleRequest(GameMessage message, Channel channel, GameSession session) {
		
		GetRoomsRequest request = message.getGetRoomsRequest();
		List<PBGameSession>  pbGameSessionsList = null;
		
		// Return session list.
		int start = 0; 	// Default value if client does not set this.
		int end = 29;   	// Default value if client does not set this.
		
		if (message.hasStartOffset() ) 
			start = message.getStartOffset();
		
		if (message.hasMaxCount() && message.getMaxCount() > 0)
			end = start + message.getMaxCount() - 1; 
		else 
			end = start + 30;
		
		String keyWord = "";
		if (request.hasKeyword()) {
			keyWord = request.getKeyword();
		} 

		int roomType = 0; // 0: all rooms[default], 1: friends' rooms
		if (request.hasRoomType()) {
			roomType = request.getRoomType();
		}
		
		String userId = message.getUserId();
		
		GameSessionManager sessionManager = GameEventExecutor.getInstance().getSessionManager();
		GameUserManager userManager = GameUserManager.getInstance();
		
		List<GameSession> gameSessionList = new ArrayList<GameSession>();
		
		// Get sessionId list from priority session queue, ranging from start to end.
		List<Integer> sessionIdList = GameSessionAllocationManager.getInstance().getSessionList(start, end, keyWord, roomType,userId);

		// If search by friends, and you haven't follow any friends, the sessionIdList is empty.
		if ( sessionIdList != null ) {
			// Do real job ,get the session from gameSession model.
			for ( Integer sessionId: sessionIdList) {
				gameSessionList.add(sessionManager.findSessionById(sessionId));
			}
		
			// Only PBGameSession should be encapsulated in response, so wrap it.
			pbGameSessionsList = new ArrayList<PBGameSession>(); 
			for( GameSession gameSession : gameSessionList) {
				if (gameSession.isUserTakenOver(userId) == false){
					pbGameSessionsList.add(gameSession.toPBGameSession(sessionManager));
				}
			}
		} else {
			pbGameSessionsList = Collections.emptyList();
		}
		
		// Make response and then send it.
		int onLineUserCount = userManager.getOnlineUserCount();
		GameMessage responseMessage = makeGetRoomsResponse(onLineUserCount,pbGameSessionsList,message.getMessageId());
		HandlerUtils.sendMessage(responseMessage, channel);
	}

	private GameMessage makeGetRoomsResponse(int onLineUserCount, List<PBGameSession> pbGameSessionsList
					,int messageId) {
		
		GetRoomsResponse getRoomsResponse = GetRoomsResponse.newBuilder()
			.addAllSessions(pbGameSessionsList)
			.build();
		
		GameMessage responseMessage = GameMessage.newBuilder()
				.setCommand(GameCommandType.GET_ROOMS_RESPONSE)
				.setMessageId(messageId)
				.setOnlineUserCount(onLineUserCount)
				.setGetRoomsResponse(getRoomsResponse)
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
