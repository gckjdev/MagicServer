package com.orange.game.traffic.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.orange.game.traffic.model.dao.GameSession;
import com.orange.game.traffic.model.dao.GameUser;
import com.orange.game.traffic.model.dao.SimpleChannelUser;
import com.orange.game.traffic.model.manager.GameSessionManager;
import com.orange.game.traffic.model.manager.RoomsNotificationManager;
import com.orange.game.traffic.server.GameEventExecutor;
import com.orange.game.traffic.server.HandlerUtils;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;
import com.orange.network.game.protocol.message.GameMessageProtos.RoomNotificationRequest;
import com.orange.network.game.protocol.model.GameBasicProtos.PBGameSession;
import com.orange.network.game.protocol.model.GameBasicProtos.PBGameSessionChanged;
import com.orange.network.game.protocol.model.GameBasicProtos.PBGameUser;

public class RoomNotificationService {
	
	private RoomsNotificationManager roomsNotificationManager 
			= RoomsNotificationManager.getInstance();
	private GameSessionManager gameSessionManager = GameEventExecutor.getInstance().getSessionManager();
	
	// thread-safe singleton implementation
	private static RoomNotificationService defaultService = new RoomNotificationService();
	private RoomNotificationService() {
	}

	public static RoomNotificationService getInstance() {
		return defaultService;
	}
	
	private boolean skipSelf(boolean sendToSelf, String fromUserId, String checkUserId){
		return (sendToSelf == false && fromUserId != null && fromUserId.equals(checkUserId));
	}

	private void broadcastRoomNotification(GameSession session, 
			RoomNotificationRequest notification, 
			String fromUserId, 
			boolean sendToSelf)
	{
		broadcastRoomNotification(session, notification, GameCommandType.ROOM_NOTIFICATION_REQUEST,
				fromUserId, sendToSelf);
	}
	
	private void broadcastRoomNotification(GameSession session, 
			RoomNotificationRequest notification, 
			GameCommandType command,
			String fromUserId, 
			boolean sendToSelf) {
		
		// notify all users in the session
		Set<String> sentUserIdSet = new HashSet<String>();
		List<GameUser> list = session.getUserList().getAllUsers();	
		for (GameUser user : list){		
			if (skipSelf(sendToSelf, fromUserId, user.getUserId()))
				continue;			
			
			// send notification for the user			
			GameMessage.Builder builder = GameMessage.newBuilder()
				.setCommand(command)
				.setMessageId(GameEventExecutor.getInstance().generateMessageId())
				.setSessionId(session.getSessionId())
				.setToUserId(user.getUserId())
				.setRoomNotificationRequest(notification);
			
				if (fromUserId != null){
					builder.setUserId(fromUserId);
				}
				
			GameMessage message = builder.build();			
			HandlerUtils.sendMessage(message, user.getChannel());
			
			// add into set for later usage
			sentUserIdSet.add(user.getUserId());
		}		
		
		// notify all user who register the notification
		List<SimpleChannelUser> gameUserList = roomsNotificationManager
				.getGameUser(session.getSessionId());						
		for (SimpleChannelUser simpleUser: gameUserList) {
			
			// Send to self?
			if ( simpleUser.getUserId().equals(fromUserId) && sendToSelf == false )
				continue;
			
			// check if the user already notified
			if (sentUserIdSet.contains(simpleUser.getUserId()))
				continue;
			
			GameMessage.Builder builder = GameMessage.newBuilder()
				.setCommand(command)
				.setMessageId(GameEventExecutor.getInstance().generateMessageId())
				.setSessionId(session.getSessionId())
				.setToUserId(simpleUser.getUserId())
				.setRoomNotificationRequest(notification);
		
			if (fromUserId != null){
				builder.setUserId(fromUserId);
			}
			
			GameMessage message = builder.build();
			HandlerUtils.sendMessage(message, simpleUser.getChannel());
		}
	}

	
	public void notifyUserJoin(GameSession session, PBGameUser pbUser, 
			String fromUserId, boolean sendToSelf){
		String newUserId = pbUser.getUserId();
		if (newUserId == null)
			return;		
		
		List<PBGameUser> userAdded = new ArrayList<PBGameUser>();
		userAdded.add(pbUser);
		
		PBGameSessionChanged sessionChanged = PBGameSessionChanged.newBuilder()
			.setSessionId(session.getSessionId())
			.addAllUsersAdded(userAdded)		
			.build();
		
		List<PBGameSessionChanged> sessionChangedList = new ArrayList<PBGameSessionChanged>();
		sessionChangedList.add(sessionChanged);
		
		RoomNotificationRequest notification = RoomNotificationRequest.newBuilder()
			.addAllSessionsChanged(sessionChangedList)
			.build();
		
		broadcastRoomNotification(session, notification, 
				GameCommandType.ROOM_NOTIFICATION_REQUEST, fromUserId, sendToSelf);		
	}
	

	public void notifyUserQuit(GameSession session, List<String> quitUserIdList){
		
		if (quitUserIdList == null || quitUserIdList.size() == 0)
			return;
		
		PBGameSessionChanged sessionChanged = PBGameSessionChanged.newBuilder()
			.setSessionId(session.getSessionId())
			.addAllUserIdsDeleted(quitUserIdList)
			.build();		
		
		List<PBGameSessionChanged> sessionChangedList = new ArrayList<PBGameSessionChanged>();
		sessionChangedList.add(sessionChanged);
		
		RoomNotificationRequest notification = RoomNotificationRequest.newBuilder()
			.addAllSessionsChanged(sessionChangedList)
			.build();

		broadcastRoomNotification(session, notification, null, false);
	}
	
	public void notifyRoomCreate(GameSession session, String fromUserId, boolean sendToSelf){
		
		List<PBGameSession>  sessionAddedList = new ArrayList<PBGameSession>();		
		sessionAddedList.add( session.toPBGameSession(gameSessionManager) );
		
		RoomNotificationRequest notification = RoomNotificationRequest.newBuilder()
			.addAllSessionsAdded(sessionAddedList)
			.build();
		
		broadcastRoomNotification(session, notification, fromUserId,sendToSelf);		
		
	}
	
	public void notifyRoomDeleted(int sessionId, String fromUserId, boolean sendToSelf){
		
		GameSession session = gameSessionManager.findSessionById(sessionId);
		
		List<Integer> sessionIdDeletedList = new ArrayList<Integer>();
		sessionIdDeletedList.add(sessionId);
		
		RoomNotificationRequest notification = RoomNotificationRequest.newBuilder()
			.addAllSessionIdsDeleted(sessionIdDeletedList)
			.build();

		broadcastRoomNotification(session, notification, fromUserId, sendToSelf);		
		
	}
	
	public void notifyRoomChange(GameSession session){
		
	}
	
	
}
