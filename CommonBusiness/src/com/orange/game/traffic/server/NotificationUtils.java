package com.orange.game.traffic.server;

import java.util.ArrayList;
import java.util.List;

import javax.xml.crypto.Data;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;

import com.orange.common.log.ServerLog;
import com.orange.common.utils.CompressColorUtil;
import com.orange.game.model.service.DataService;
import com.orange.game.traffic.model.dao.GameSession;
import com.orange.game.traffic.model.dao.GameUser;
import com.orange.game.traffic.model.manager.GameUserManager;

import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.message.GameMessageProtos.BetDiceRequest;
import com.orange.network.game.protocol.message.GameMessageProtos.CallDiceRequest;
import com.orange.network.game.protocol.message.GameMessageProtos.GameChatRequest;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;
import com.orange.network.game.protocol.message.GameMessageProtos.SendDrawDataRequest;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage.Builder;
import com.orange.network.game.protocol.message.GameMessageProtos.RoomNotificationRequest;
import com.orange.network.game.protocol.message.GameMessageProtos.UserDiceNotification;
import com.orange.network.game.protocol.model.GameBasicProtos.PBDrawAction;
import com.orange.network.game.protocol.model.GameBasicProtos.PBGameSessionChanged;
import com.orange.network.game.protocol.model.GameBasicProtos.PBGameUser;
import com.orange.network.game.protocol.model.GameBasicProtos.PBSNSUser;
import com.orange.network.game.protocol.model.GameBasicProtos.PBSize;

public class NotificationUtils {

	private static final Logger logger = Logger.getLogger(NotificationUtils.class.getName());
//	private static final GameSessionUserManager sessionUserManager = GameSessionUserManager.getInstance();
//	private static final GameSessionManager sessionManager = GameSessionManager.getInstance();
//	private static final GameService gameService = GameService.getInstance();
	
	

	
	public static void broadcastNotification(GameSession session, String excludeUserId, GameCommandType command) {
		
		List<GameUser> list = session.getUserList().getAllUsers();	
//		int onlineUserCount = UserManager.getInstance().getOnlineUserCount();
		
		for (GameUser user : list){		
			if (excludeUserId != null && user.getUserId().equalsIgnoreCase(excludeUserId))
				continue;
			
			GameMessageProtos.GeneralNotification notification;
			
//			if (command == GameCommandType.GAME_TURN_COMPLETE_NOTIFICATION_REQUEST){
//				notification = GameMessageProtos.GeneralNotification.newBuilder()		
//					.setCurrentPlayUserId(session.getCurrentPlayUserId())
////					.setTurnGainCoins(session.getCurrentUserGainCoins(user.getUserId()))
//					.build();				
//			}
//			else{
//				notification = GameMessageProtos.GeneralNotification.newBuilder()		
//					.setCurrentPlayUserId(session.getCurrentPlayUserId())
//					.build();
//			}
			
			// send notification for the user			
			GameMessageProtos.GameMessage.Builder builder = GameMessageProtos.GameMessage.newBuilder()
				.setCommand(command)
				.setMessageId(GameEventExecutor.getInstance().generateMessageId())
				.setSessionId(session.getSessionId())
				.setToUserId(user.getUserId());
//				.setNotification(notification);
//			.setCompleteReason(session.getCompleteReason())							
//			.setRound(session.getCurrentRound())
//			.setOnlineUserCount(onlineUserCount)
			
				if (excludeUserId != null){
					builder.setUserId(excludeUserId);
				}
			
				if (session.getCurrentPlayUserId() != null){
					builder.setCurrentPlayUserId(session.getCurrentPlayUserId());
				}
			
			GameMessage message = builder.build();	
			
			
			HandlerUtils.sendMessage(message, user.getChannel());
		}
	}
//	
//	public static void broadcastNotification(GameSession gameSession,
//			GameEvent gameEvent, String excludeUserId, GameCommandType command) {
//		
//		List<User> list = sessionUserManager.getUserListBySession(gameSession.getSessionId());	
//		int onlineUserCount = UserManager.getInstance().getOnlineUserCount();
//		
//		for (User user : list){		
//			if (excludeUserId != null && user.getUserId().equalsIgnoreCase(excludeUserId))
//				continue;
//			
//			GameMessageProtos.GeneralNotification notification;
//			
//			if (command == GameCommandType.GAME_TURN_COMPLETE_NOTIFICATION_REQUEST){
//				notification = GameMessageProtos.GeneralNotification.newBuilder()		
//					.setCurrentPlayUserId(gameSession.getCurrentPlayUserId())
//					.setTurnGainCoins(gameSession.getCurrentUserGainCoins(user.getUserId()))
//					.build();				
//			}
//			else{
//				notification = GameMessageProtos.GeneralNotification.newBuilder()		
//					.setCurrentPlayUserId(gameSession.getCurrentPlayUserId())
//					.build();
//			}
//			
//			// send notification for the user			
//			GameMessageProtos.GameMessage message = GameMessageProtos.GameMessage.newBuilder()
//				.setCommand(command)
//				.setMessageId(GameService.getInstance().generateMessageId())
//				.setSessionId(gameSession.getSessionId())
//				.setUserId(user.getUserId())
//				.setToUserId(user.getUserId())				
//				.setCompleteReason(gameSession.getCompleteReason())
//				.setNotification(notification)			
//				.setRound(gameSession.getCurrentRound())
//				.setOnlineUserCount(onlineUserCount)
//				.build();
//			
//			HandlerUtils.sendMessage(gameEvent, message, user.getChannel());
//		}
//	}
//
//	public static void broadcastDrawUserChangeNotification(GameSession gameSession) {
//		
//		int onlineUserCount = UserManager.getInstance().getOnlineUserCount();
//		
//		List<User> list = sessionUserManager.getUserListBySession(gameSession.getSessionId());
//		for (User user : list){
//			
//			// send notification for the user
//			GameMessageProtos.GeneralNotification notification = GameMessageProtos.GeneralNotification.newBuilder()		
//				.setCurrentPlayUserId(gameSession.getCurrentPlayUserId())
//				.setNextPlayUserId("")
//				.build();
//			
//			GameMessageProtos.GameMessage response = GameMessageProtos.GameMessage.newBuilder()
//				.setCommand(GameCommandType.USER_JOIN_NOTIFICATION_REQUEST)
//				.setMessageId(GameService.getInstance().generateMessageId())
//				.setNotification(notification)
//				.setSessionId(gameSession.getSessionId())
//				.setUserId(user.getUserId())
//				.setToUserId(user.getUserId())				
//				.setOnlineUserCount(onlineUserCount)
//				.build();
//			
//			HandlerUtils.sendMessage(response, user.getChannel());
//		}
//	}
//	
	public static void broadcastDrawUserJoinNotification(GameSession gameSession,
			GameUser newUser) {
		
		String newUserId = newUser.getUserId();
		PBGameUser pbUser = newUser.getPBUser();
		
		String newUserNickName = pbUser.getNickName();
		String newUserAvatar = pbUser.getAvatar();
		String newUserLocation = pbUser.getLocation();
		int newUserLevel = pbUser.getUserLevel();
		boolean newUserGender = pbUser.getGender();		
		List<PBSNSUser> newUserSNSList = pbUser.getSnsUsersList();		
		
		int onlineUserCount = GameUserManager.getInstance().getOnlineUserCount();
		
		List<GameUser> list = gameSession.getUserList().getAllUsers();
		for (GameUser user : list){
			if (user.getUserId().equalsIgnoreCase(newUserId)){
				continue;
			}
			
			// send notification for the user
			GameMessageProtos.GeneralNotification notification = GameMessageProtos.GeneralNotification.newBuilder()		
				.setNewUserId(newUserId)
				.setNickName(newUserNickName)
				.setUserAvatar(newUserAvatar)
				.setUserGender(newUserGender)
				.setLocation(newUserLocation)
				.setUserLevel(newUserLevel)
				.addAllSnsUsers(newUserSNSList)
				.setCurrentPlayUserId(gameSession.safeGetCurrentPlayUserId())
				.setNextPlayUserId("")
				.build();
			
			GameMessageProtos.GameMessage response = GameMessageProtos.GameMessage.newBuilder()
				.setCommand(GameCommandType.USER_JOIN_NOTIFICATION_REQUEST)
				.setMessageId(GameEventExecutor.getInstance().generateMessageId())
				.setNotification(notification)
				.setSessionId(gameSession.getSessionId())
				.setUserId(user.getUserId())
				.setToUserId(user.getUserId())				
				.setOnlineUserCount(onlineUserCount)
				.build();
			
			HandlerUtils.sendMessage(response, user.getChannel());
		}
	}
	
	public static void broadcastDrawUserQuitNotification(GameSession gameSession, List<String> removeUsers) {
		
		if (removeUsers == null)
			return;
		
		String currentPlayUserId = gameSession.getCurrentPlayUserId();
		if (currentPlayUserId == null){
			ServerLog.warn(gameSession.getSessionId(), "<broadcastDrawUserQuitNotification> but current play user Id is null");
		}
		
		for (String quitUserId : removeUsers){
			List<GameUser> list = gameSession.getUserList().getAllUsers();
			int onlineUserCount = GameUserManager.getInstance().getOnlineUserCount();

			for (GameUser user : list){
				if (user.getUserId().equalsIgnoreCase(quitUserId)){
					continue;
				}
				
				// send notification for the user
				GameMessageProtos.GeneralNotification.Builder builder = GameMessageProtos.GeneralNotification.newBuilder();		
				builder.setQuitUserId(quitUserId);
				builder.setNextPlayUserId("");
				if (currentPlayUserId != null){
					builder.setCurrentPlayUserId(currentPlayUserId);
				}
					
				GameMessageProtos.GeneralNotification notification = builder.build();				
				GameMessageProtos.GameMessage response = GameMessageProtos.GameMessage.newBuilder()
					.setCommand(GameCommandType.USER_QUIT_NOTIFICATION_REQUEST)
					.setMessageId(GameEventExecutor.getInstance().generateMessageId())
					.setNotification(notification)
					.setSessionId(gameSession.getSessionId())
					.setUserId(user.getUserId())
					.setToUserId(user.getUserId())				
					.setOnlineUserCount(onlineUserCount)
					.build();
				
				HandlerUtils.sendMessage(response, user.getChannel());
			}
			
		}
		
	}
	
//	public static void broadcastUserQuitNotification(GameSession gameSession, String quitUserId) {
//		
//		List<User> list = sessionUserManager.getUserListBySession(gameSession.getSessionId());
//		int onlineUserCount = UserManager.getInstance().getOnlineUserCount();
//
//		for (User user : list){
//			if (user.getUserId().equalsIgnoreCase(quitUserId)){
//				continue;
//			}
//			
//			// send notification for the user
//			GameMessageProtos.GeneralNotification notification = GameMessageProtos.GeneralNotification.newBuilder()		
//				.setQuitUserId(quitUserId)
//				.setNextPlayUserId("")
//				.setCurrentPlayUserId(gameSession.getCurrentPlayUserId())
//				.setSessionHost(gameSession.getHost())
//				.build();
//			
//			GameMessageProtos.GameMessage response = GameMessageProtos.GameMessage.newBuilder()
//				.setCommand(GameCommandType.USER_QUIT_NOTIFICATION_REQUEST)
//				.setMessageId(GameService.getInstance().generateMessageId())
//				.setNotification(notification)
//				.setSessionId(gameSession.getSessionId())
//				.setUserId(user.getUserId())
//				.setOnlineUserCount(onlineUserCount)
//				.build();
//			
//			HandlerUtils.sendMessage(response, user.getChannel());
//		}
//	}
//
//
//	public static void broadcastGameStartNotification(GameSession gameSession, GameEvent gameEvent) {
//		
//		List<User> list = sessionUserManager.getUserListBySession(gameSession.getSessionId());
//		for (User user : list){			
//			
//			if (!user.isPlaying()){
//				GameLog.info(gameSession.getSessionId(), "send START game notificaiton but user "+
//						user.getNickName()+" not in play state");
//				continue;
//			}
//			
//			// send notification for the user
//			GameMessageProtos.GeneralNotification notification = GameMessageProtos.GeneralNotification.newBuilder()		
//				.setCurrentPlayUserId(gameSession.getCurrentPlayUserId())
//				.setNextPlayUserId("")
//				.build();
//			
//			GameMessageProtos.GameMessage message = GameMessageProtos.GameMessage.newBuilder()
//				.setCommand(GameCommandType.GAME_START_NOTIFICATION_REQUEST)
//				.setMessageId(GameService.getInstance().generateMessageId())
//				.setNotification(notification)
//				.setSessionId(gameSession.getSessionId())
//				.setUserId(user.getUserId())
//				.setToUserId(user.getUserId())				
//				.build();
//			
//			HandlerUtils.sendMessage(gameEvent, message, user.getChannel());
//		}
//	}
//
	public static void broadcastDrawDataNotification(GameSession gameSession,
			String userId, boolean guessCorrect, GameMessageProtos.GeneralNotification notification, PBSize canvasSize) {
						
		List<GameUser> list = gameSession.getUserList().getAllUsers();
		for (GameUser user : list){
			
			if (!user.isPlaying()){
				ServerLog.info(gameSession.getSessionId(), "send DRAW REQUEST but user "+
						user.getNickName()+" not in play state");
				continue;
			}			
			
			if (!guessCorrect && user.getUserId().equalsIgnoreCase(userId))
				continue;
			
			if (notification.getPointsCount() > 0 && user.getInterfaceVersion() > 0){
								
				// draw message && user used new version, need conversion
//				if (notification.getWidth() >= 2999.0f){
//					// change background
//					
//				}
//				else{
					float oldWidth = notification.getWidth();
					int oldColor = notification.getColor();
					List<Integer> pointList = notification.getPointsList();
					int penType = notification.getPenType();
					
					// normal draw
					PBDrawAction.Builder newDrawActionBuilder = PBDrawAction.newBuilder();

					newDrawActionBuilder.setType(0);					// TODO change to macro
					newDrawActionBuilder.setPenType(penType);
					
					// convert data width
					float newWidth = oldWidth * 2.0f;
					newDrawActionBuilder.setWidth(newWidth);
					
					// covert data points X & Y and add data
					int pointCount = pointList.size();
					for (int pointIndex=0; pointIndex < pointCount; pointIndex++){
						int point = pointList.get(pointIndex);
					    int div = 1<< 15;
					    float y = ((float)(point % div)) * DataService.DRAW_VERSION_1_IPAD_HEIGHT_SCALE ;
					    float x = ((float)point / (float)div) * DataService.DRAW_VERSION_1_IPAD_WIDTH_SCALE;
					    
					    newDrawActionBuilder.addPointsX(x);
					    newDrawActionBuilder.addPointsY(y);
					}
										
					// convert old color to new color
					long color = getUnsignedInt(oldColor);
					float red = CompressColorUtil.getRedFromColor6(color);
					float green = CompressColorUtil.getGreenFromColor6(color);
					float blue = CompressColorUtil.getBlueFromColor6(color);
					float alpha = CompressColorUtil.getAlphaFromColor6(color);
					long newColor = CompressColorUtil.compressColor8WithRed(red, green, blue, alpha);
					
					newDrawActionBuilder.setBetterColor((int)newColor);
					
					// construct data
					PBDrawAction drawAction = newDrawActionBuilder.build();
					PBSize size =PBSize.newBuilder()
							.setWidth(DataService.DRAW_VERSION_1_WIDTH)
							.setHeight(DataService.DRAW_VERSION_1_HEIGHT)
							.build();
					
					SendDrawDataRequest drawRequest = SendDrawDataRequest.newBuilder()
							.setDrawAction(drawAction)
							.setCanvasSize(size)
							.build();
					
					GameMessageProtos.GameMessage forwardMessage = GameMessageProtos.GameMessage.newBuilder()
							.setCommand(GameCommandType.SEND_DRAW_DATA_REQUEST)
							.setMessageId(GameEventExecutor.getInstance().generateMessageId())
							.setSendDrawDataRequest(drawRequest)
							.setSessionId(gameSession.getSessionId())
							.setUserId(userId)
							.setToUserId(user.getUserId())
							.build();
					
//					logger.info("<broadcastDrawDataNotification> to new user, message="+forwardMessage.toString());
					logger.info("<broadcastDrawDataNotification> to new user "+user.getNickName());
					HandlerUtils.sendMessage(forwardMessage, user.getChannel());		
//				}
			}
			else{
				// old version or other message, forward message directly
				GameMessageProtos.GameMessage message = GameMessageProtos.GameMessage.newBuilder()
					.setCommand(GameCommandType.NEW_DRAW_DATA_NOTIFICATION_REQUEST)
					.setMessageId(GameEventExecutor.getInstance().generateMessageId())
					.setNotification(notification)
					.setSessionId(gameSession.getSessionId())
					.setUserId(userId)
					.setToUserId(user.getUserId())
					.build();
				
				HandlerUtils.sendMessage(message, user.getChannel());				
			}
		}
	}

	public static void broadcastCleanDrawNotification(GameSession gameSession, String userId) {
		
		List<GameUser> list = gameSession.getUserList().getAllUsers();
//		int onlineUserCount = GameUserManager.getInstance().getOnlineUserCount();
		for (GameUser user : list){	
			
			if (!user.isPlaying()){
				ServerLog.info(gameSession.getSessionId(), "send CLEANDRAW but user "+
						user.getNickName()+" not in play state");
				continue;
			}	
			
			String toUserId = user.getUserId();
			if (toUserId.equalsIgnoreCase(userId))
				continue;
			
			// send notification for the user			
			GameMessageProtos.GameMessage message = GameMessageProtos.GameMessage.newBuilder()
				.setCommand(GameCommandType.CLEAN_DRAW_NOTIFICATION_REQUEST)
				.setMessageId(GameEventExecutor.getInstance().generateMessageId())
				.setSessionId(gameSession.getSessionId())
				.setUserId(userId)
				.setToUserId(user.getUserId())				
				.build();
			
			HandlerUtils.sendMessage(message, user.getChannel());
		}
	}
	
	public static void broadcastSendDrawActionRequest(GameSession gameSession, String userId, GameMessage message2) {
		
		
		
		List<GameUser> list = gameSession.getUserList().getAllUsers();
//		int onlineUserCount = GameUserManager.getInstance().getOnlineUserCount();
		for (GameUser user : list){	
			
			if (!user.isPlaying()){
				ServerLog.info(gameSession.getSessionId(), "send CLEANDRAW but user "+
						user.getNickName()+" not in play state");
				continue;
			}	
			
			String toUserId = user.getUserId();
			if (toUserId.equalsIgnoreCase(userId))
				continue;
			
			// send notification for the user			
			GameMessageProtos.GameMessage message = GameMessageProtos.GameMessage.newBuilder()
				.setCommand(GameCommandType.CLEAN_DRAW_NOTIFICATION_REQUEST)
				.setMessageId(GameEventExecutor.getInstance().generateMessageId())
				.setSessionId(gameSession.getSessionId())
				.setUserId(userId)
				.setToUserId(user.getUserId())				
				.build();
			
			HandlerUtils.sendMessage(message, user.getChannel());
		}
	}
	
//	
	
//	
//	public static void broadcastCleanDrawNotification(GameSession gameSession, String userId) {
//		
//		List<User> list = sessionUserManager.getUserListBySession(gameSession.getSessionId());
//		for (User user : list){	
//			
//			if (!user.isPlaying()){
//				GameLog.info(gameSession.getSessionId(), "send CLEANDRAW but user "+
//						user.getNickName()+" not in play state");
//				continue;
//			}	
//			
//			String toUserId = user.getUserId();
//			if (toUserId.equalsIgnoreCase(userId))
//				continue;
//			
//			// send notification for the user			
//			GameMessageProtos.GameMessage message = GameMessageProtos.GameMessage.newBuilder()
//				.setCommand(GameCommandType.CLEAN_DRAW_NOTIFICATION_REQUEST)
//				.setMessageId(GameService.getInstance().generateMessageId())
//				.setSessionId(gameSession.getSessionId())
//				.setUserId(userId)
//				.setToUserId(user.getUserId())				
//				.build();
//			
//			HandlerUtils.sendMessage(message, user.getChannel());
//		}
//	}
//
//	public static void broadcastChatNotification(GameSession session,
//			GameEvent gameEvent, String userId) {		
//		
//		GameMessage message = gameEvent.getMessage();
//		if (message.getChatRequest() == null)
//			return;
//		
//		GameChatRequest chatRequest = message.getChatRequest();
//		int onlineUserCount = UserManager.getInstance().getOnlineUserCount();
//
//		List<User> list = sessionUserManager.getUserListBySession(session.getSessionId());	
//		List<String> toUserIdList = chatRequest.getToUserIdList();
//		boolean hasTargetUser = (toUserIdList != null && toUserIdList.size() > 0);
//		for (User user : list){		
//			String toUserId = user.getUserId();
//			
//			// don't send to request user, he knows it!
//			if (toUserId.equalsIgnoreCase(userId))
//				continue;
//			
//			// if user is not in target user list, skip
//			if (hasTargetUser && !toUserIdList.contains(toUserId)){				
//				continue;
//			}
//			
//			// send notification for the user
//			GameMessageProtos.GeneralNotification notification = GameMessageProtos.GeneralNotification.newBuilder()		
//				.addAllChatToUserId(toUserIdList)
//				.setChatContent(chatRequest.getContent())
//				.setChatType(chatRequest.getChatType())				
//				.build();
//
//			// send notification for the user			
//			GameMessageProtos.GameMessage m = GameMessageProtos.GameMessage.newBuilder()
//				.setCommand(GameCommandType.CHAT_NOTIFICATION_REQUEST)
//				.setMessageId(GameService.getInstance().generateMessageId())
//				.setSessionId(session.getSessionId())
//				.setUserId(userId)
//				.setToUserId(user.getUserId())				
//				.setNotification(notification)
//				.setRound(message.getRound())
//				.setOnlineUserCount(onlineUserCount)
//				.build();
//			
//			HandlerUtils.sendMessage(gameEvent, m, user.getChannel());
//		}
//	}

	public static void broadcastChatNotification(GameSession session,
			GameMessage message, String userId) {		
		
		if (message.getChatRequest() == null)
			return;
		
		GameChatRequest chatRequest = message.getChatRequest();
		int onlineUserCount = GameUserManager.getInstance().getOnlineUserCount();

		List<GameUser> list = session.getUserList().getAllUsers();
		List<String> toUserIdList = chatRequest.getToUserIdList();
		boolean hasTargetUser = (toUserIdList != null && toUserIdList.size() > 0);
		for (GameUser user : list){		
			String toUserId = user.getUserId();
			
			// don't send to request user, he knows it!
			if (toUserId.equalsIgnoreCase(userId))
				continue;
			
			// if user is not in target user list, skip
			if (hasTargetUser && !toUserIdList.contains(toUserId)){				
				continue;
			}
			
			// send notification for the user
			GameMessageProtos.GeneralNotification notification = GameMessageProtos.GeneralNotification.newBuilder()		
				.addAllChatToUserId(toUserIdList)
				.setChatContent(chatRequest.getContent())
				.setChatType(chatRequest.getChatType())				
				.build();

			// send notification for the user			
			GameMessageProtos.GameMessage m = GameMessageProtos.GameMessage.newBuilder()
				.setCommand(GameCommandType.CHAT_NOTIFICATION_REQUEST)
				.setMessageId(GameEventExecutor.getInstance().generateMessageId())
				.setSessionId(session.getSessionId())
				.setUserId(userId)
				.setToUserId(toUserId)				
				.setNotification(notification)
				.setRound(message.getRound())
				.setOnlineUserCount(onlineUserCount)
				.build();
			
			HandlerUtils.sendMessage(m, user.getChannel());
		}
	}

	private static void broadcastRoomNotification(GameSession session, String excludeUserId, RoomNotificationRequest notificationRequest){
		
		List<GameUser> list = session.getUserList().getAllUsers();	
		for (GameUser user : list){		
			if (excludeUserId != null && excludeUserId.equals(user.getUserId()))
				continue;			
			
			// send notification for the user			
			GameMessage.Builder builder = GameMessage.newBuilder()
				.setCommand(GameCommandType.ROOM_NOTIFICATION_REQUEST)
				.setMessageId(GameEventExecutor.getInstance().generateMessageId())
				.setSessionId(session.getSessionId())
				.setToUserId(user.getUserId())
				.setRoomNotificationRequest(notificationRequest);
			
				if (excludeUserId != null){
					builder.setUserId(excludeUserId);
				}
				
				GameMessage message = builder.build();
			
			HandlerUtils.sendMessage(message, user.getChannel());
		}		
	}
	
	public static void broadcastUserJoinNotification(GameSession session,
			PBGameUser pbUser) {
		
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
		
		broadcastRoomNotification(session, newUserId, notification);
	}

	public static void broadcastUserStatusChangeNotification(
			GameSession session, String updateUserId) {
		
		GameUser updateUser = session.getUser(updateUserId);
		if (updateUser == null)
			return;
				
		List<PBGameUser> userUpdated = new ArrayList<PBGameUser>();
		userUpdated.add(updateUser.getPBUser());
		
		PBGameSessionChanged sessionChanged = PBGameSessionChanged.newBuilder()
			.setSessionId(session.getSessionId())
			.addAllUsersUpdated(userUpdated)		
			.build();
		
		List<PBGameSessionChanged> sessionChangedList = new ArrayList<PBGameSessionChanged>();
		sessionChangedList.add(sessionChanged);
		
		RoomNotificationRequest notification = RoomNotificationRequest.newBuilder()
			.addAllSessionsChanged(sessionChangedList)
			.build();
		logger.info("");
		broadcastRoomNotification(session, updateUserId, notification);
	}

	public static void broadcastUserQuitNotification(GameSession session,
			String deleteUserId) {
				
		List<String> userDeleted = new ArrayList<String>();
		userDeleted.add(deleteUserId);
		
		PBGameSessionChanged sessionChanged = PBGameSessionChanged.newBuilder()
			.setSessionId(session.getSessionId())
			.addAllUserIdsDeleted(userDeleted)		
			.build();
		
		List<PBGameSessionChanged> sessionChangedList = new ArrayList<PBGameSessionChanged>();
		sessionChangedList.add(sessionChanged);
		
		RoomNotificationRequest notification = RoomNotificationRequest.newBuilder()
			.addAllSessionsChanged(sessionChangedList)
			.build();
		
		broadcastRoomNotification(session, deleteUserId, notification);
	}

	public static void broadcastNotification(GameSession session,
			GameMessage message) {		
		broadcastNotification(session, null, message);
	}

	
	public static void broadcastNotification(GameSession session,
			String excludeUserId, GameMessage message) {
		
		List<GameUser> list = session.getUserList().getAllUsers();	
		for (GameUser user : list){		
			if (excludeUserId != null && excludeUserId.equals(user.getUserId()))
				continue;			
			
			// send notification for the user			
			HandlerUtils.sendMessage(message, user);
		}				
	}
	
	public static long getUnsignedInt (int data){     //将int数据转换为0~4294967295 (0xFFFFFFFF即DWORD)。
        return data & 0x0FFFFFFFFl;
     }		
	
	public static void broadcastSendDrawDataRequest(GameSession session,
			String excludeUserId, GameMessage message, PBSize canvasSize) {
		
		List<GameUser> list = session.getUserList().getAllUsers();	
		for (GameUser user : list){		
			if (excludeUserId != null && excludeUserId.equals(user.getUserId()))
				continue;			
			
			// send notification for the user		
			if (user.getInterfaceVersion() == 0){
				// old version, send old message
				
				if (message.getSendDrawDataRequest() == null){
					logger.info("<broadcastSendDrawDataRequest> but draw data request is null");
					continue;
				}
				
				PBDrawAction drawAction = message.getSendDrawDataRequest().getDrawAction();
				if (drawAction == null){
					logger.info("<broadcastSendDrawDataRequest> but draw action is null");
					continue;
				}
												
				if (drawAction.getType() == 0 && drawAction.getPointsXCount() > 0){
					
					long color = getUnsignedInt(drawAction.getBetterColor());	// to fix red color lose issue
					float red = CompressColorUtil.getRedFromColor8(color);
					float green = CompressColorUtil.getGreenFromColor8(color);
					float blue = CompressColorUtil.getBlueFromColor8(color);
					float alpha = CompressColorUtil.getAlphaFromColor8(color);					
					long newColor = CompressColorUtil.compressColor6WithRed(red, green, blue, alpha);		
					
					// calculate scale
					PBSize drawSize = canvasSize;
					float drawWidth = DataService.DRAW_VERSION_1_WIDTH;
					float drawHeight = DataService.DRAW_VERSION_1_HEIGHT;
					if (drawSize != null){
						drawWidth = drawSize.getWidth();
						drawHeight = drawSize.getHeight();
					}
					float widthScale = drawWidth / DataService.DRAW_VERSION_1_IPHONE_WIDTH;
					float heightScale = drawHeight / DataService.DRAW_VERSION_1_IPHONE_HEIGHT;
					
					float penWidthScale = (widthScale+heightScale)/2.0f;
					float penWidth = drawAction.getWidth() / penWidthScale;
					
					// covert data points X & Y and add data
					List<Integer> pointList = new ArrayList<Integer>();
					int pointCount = drawAction.getPointsXCount();
					for (int pointIndex=0; pointIndex < pointCount; pointIndex++){
						
						float x = drawAction.getPointsX(pointIndex) / widthScale;
						float y = drawAction.getPointsY(pointIndex) / heightScale;
														
						int point = (DataService.roundFloatValue(x) * (1 << 15)) + DataService.roundFloatValue(y);
						pointList.add(point);
					}
					
					// send notification for the user
					GameMessageProtos.GeneralNotification notification = GameMessageProtos.GeneralNotification.newBuilder()		
						.setColor((int)newColor)
						.addAllPoints(pointList)
						.setWidth(penWidth)
						.setPenType(drawAction.getPenType())
						.build();		
					
					GameMessageProtos.GameMessage forwardMessage = GameMessageProtos.GameMessage.newBuilder()
							.setCommand(GameCommandType.NEW_DRAW_DATA_NOTIFICATION_REQUEST)
							.setMessageId(GameEventExecutor.getInstance().generateMessageId())
							.setNotification(notification)
							.setSessionId(session.getSessionId())
							.setUserId(message.getUserId())
							.setToUserId(user.getUserId())
							.build();
						
//					logger.info("<broadcastSendDrawDataRequest> to old user, message="+forwardMessage.toString());					
					logger.info("<broadcastSendDrawDataRequest> send to old user "+user.getNickName());					
					HandlerUtils.sendMessage(forwardMessage, user.getChannel());					
				}				
				else if (drawAction.getType() == 1){
					
					GameMessageProtos.GameMessage forwardMessage = GameMessageProtos.GameMessage.newBuilder()
						.setCommand(GameCommandType.CLEAN_DRAW_NOTIFICATION_REQUEST)
						.setMessageId(GameEventExecutor.getInstance().generateMessageId())
						.setSessionId(session.getSessionId())
						.setUserId(message.getUserId())
						.setToUserId(user.getUserId())				
						.build();
					
					HandlerUtils.sendMessage(forwardMessage, user.getChannel());					
				}
				else if (drawAction.getType() == 3){
					
					// TODO change backgroud color set width to 3000
					// (0,1500) -> (3000, 1500)
					
					float FIXED_WIDTH_FOR_CLEAN = 3000.0f;
					int PEN_TYPE_ERASER = 1100;

					long color = getUnsignedInt(drawAction.getBetterColor());	// to fix red color lose issue
					float red = CompressColorUtil.getRedFromColor8(color);
					float green = CompressColorUtil.getGreenFromColor8(color);
					float blue = CompressColorUtil.getBlueFromColor8(color);
					float alpha = CompressColorUtil.getAlphaFromColor8(color);					
					long newColor = CompressColorUtil.compressColor6WithRed(red, green, blue, alpha);		
					
					// calculate scale
//					PBSize drawSize = canvasSize;
//					float drawWidth = DataService.DRAW_VERSION_1_WIDTH;
//					float drawHeight = DataService.DRAW_VERSION_1_HEIGHT;
//					if (drawSize != null){
//						drawWidth = drawSize.getWidth();
//						drawHeight = drawSize.getHeight();
//					}
//					float widthScale = drawWidth / DataService.DRAW_VERSION_1_IPHONE_WIDTH;
//					float heightScale = drawHeight / DataService.DRAW_VERSION_1_IPHONE_HEIGHT;
					
//					float penWidthScale = (widthScale+heightScale)/2.0f;
					float penWidth = FIXED_WIDTH_FOR_CLEAN; // FIXED_WIDTH_FOR_CLEAN / penWidthScale;
					
					// add data points X & Y and add data
					List<Integer> pointList = new ArrayList<Integer>();

					float x1 = 0.0f;
					float y1 = 1500.0f;													
					int point1 = (DataService.roundFloatValue(x1) * (1 << 15)) + DataService.roundFloatValue(y1);
					pointList.add(point1);
					
					float x2 = 3000.0f;
					float y2 = 1500.0f;													
					int point2 = (DataService.roundFloatValue(x2) * (1 << 15)) + DataService.roundFloatValue(y2);
					pointList.add(point2);
										
					// send notification for the user
					GameMessageProtos.GeneralNotification notification = GameMessageProtos.GeneralNotification.newBuilder()		
						.setColor((int)newColor)
						.addAllPoints(pointList)
						.setWidth(penWidth)
						.setPenType(PEN_TYPE_ERASER)
						.build();		
					
					GameMessageProtos.GameMessage forwardMessage = GameMessageProtos.GameMessage.newBuilder()
							.setCommand(GameCommandType.NEW_DRAW_DATA_NOTIFICATION_REQUEST)
							.setMessageId(GameEventExecutor.getInstance().generateMessageId())
							.setNotification(notification)
							.setSessionId(session.getSessionId())
							.setUserId(message.getUserId())
							.setToUserId(user.getUserId())
							.build();
						
					logger.info("<broadcastSendDrawDataRequest> send clean pen to old user"); //, message="+forwardMessage.toString());					
					HandlerUtils.sendMessage(forwardMessage, user.getChannel());					
				}
				else{
					// not support yet
					logger.info("<broadcastSendDrawDataRequest> draw aciton type not supported yet, type="+drawAction.getType());
				}
				
							
			}
			else{
				// new version, forward message directly
				HandlerUtils.sendMessage(message, user);
			}
		}				
	}
	
		
	public static void broadcastCallDiceNotification(GameSession session, CallDiceRequest request, boolean includeCallUser) {
		
		String userId = session.getCurrentPlayUserId();
		
		GameMessageProtos.GameMessage.Builder builder = GameMessageProtos.GameMessage.newBuilder()
			.setCommand(GameCommandType.CALL_DICE_REQUEST)
			.setMessageId(GameEventExecutor.getInstance().generateMessageId())
			.setSessionId(session.getSessionId())
			.setUserId(userId)
			.setCallDiceRequest(request);
		
		if (session.getCurrentPlayUserId() != null){
			builder.setCurrentPlayUserId(session.getCurrentPlayUserId());
		}
	
		GameMessage message = builder.build();		
		if (includeCallUser){
			NotificationUtils.broadcastNotification(session, null, message);
		}
		else{
			NotificationUtils.broadcastNotification(session, userId, message);			
		}
	}


	public static void broadcastBetNotification(GameSession session, BetDiceRequest request, String userId, boolean includeCallUser) {
		
		GameMessageProtos.GameMessage.Builder builder = GameMessageProtos.GameMessage.newBuilder()
			.setCommand(GameCommandType.BET_DICE_REQUEST)
			.setMessageId(GameEventExecutor.getInstance().generateMessageId())
			.setSessionId(session.getSessionId())
			.setUserId(userId)
			.setBetDiceRequest(request);
	
		GameMessage message = builder.build();		
		if (includeCallUser){
			NotificationUtils.broadcastNotification(session, null, message);
		}
		else{
			NotificationUtils.broadcastNotification(session, userId, message);			
		}
	}
	
	public static void broadcastUserQuitNotification(GameSession session,
			List<String> removerUsers) {
		
		if (removerUsers == null || removerUsers.size() == 0)
			return;
		
		List<String> userDeleted = new ArrayList<String>();
		userDeleted.addAll(removerUsers);
		
		PBGameSessionChanged sessionChanged = PBGameSessionChanged.newBuilder()
			.setSessionId(session.getSessionId())
			.addAllUserIdsDeleted(userDeleted)		
			.build();
		
		List<PBGameSessionChanged> sessionChangedList = new ArrayList<PBGameSessionChanged>();
		sessionChangedList.add(sessionChanged);
		
		RoomNotificationRequest notification = RoomNotificationRequest.newBuilder()
			.addAllSessionsChanged(sessionChangedList)
			.build();
		
		broadcastRoomNotification(session, null, notification);		
	}

	public static void broadcastUserDiceNotification(GameSession session,
			String excludeUserId, UserDiceNotification diceNoti) {
		
		GameMessageProtos.GameMessage.Builder builder = GameMessageProtos.GameMessage.newBuilder()
			.setCommand(GameCommandType.USER_DICE_NOTIFICATION)
			.setMessageId(GameEventExecutor.getInstance().generateMessageId())
			.setSessionId(session.getSessionId())
			.setUserId(excludeUserId) // the user who fire this notification
			.setUserDiceNotification(diceNoti);
		
		if (session.getCurrentPlayUserId() != null){
			builder.setCurrentPlayUserId(session.getCurrentPlayUserId());
		}
		
		GameMessage message = builder.build();		
		broadcastNotification(session, excludeUserId, message);		
	}

	public static void sendUserDiceNotification(GameSession session, String userId, Channel channel, UserDiceNotification diceNotification) {
		
		GameMessageProtos.GameMessage.Builder builder = GameMessageProtos.GameMessage.newBuilder()
			.setCommand(GameCommandType.USER_DICE_NOTIFICATION)
			.setMessageId(GameEventExecutor.getInstance().generateMessageId())
			.setSessionId(session.getSessionId())
			.setUserId(userId)
			.setUserDiceNotification(diceNotification);
		
		if (session.getCurrentPlayUserId() != null){
			builder.setCurrentPlayUserId(session.getCurrentPlayUserId());
		}
		
		GameMessage message = builder.build();		
		HandlerUtils.sendMessage(message, channel);
	}


	public static void broadcastNotification(GameSession session,
			Builder builder, boolean sendToSelf) {
		
		String userId = session.getCurrentPlayUserId();
		
		if (session.getCurrentPlayUserId() != null){
			builder.setCurrentPlayUserId(session.getCurrentPlayUserId());
		}
		GameMessage message = builder.build();		
	
		if (sendToSelf){
			NotificationUtils.broadcastNotification(session, null, message);
		}
		else{
			NotificationUtils.broadcastNotification(session, userId, message);			
		}

		
	}	
	
	public static void broadcastDrawUserChangeNotification(GameSession session) {
		
		String currentPlayUserId = session.getCurrentPlayUserId();
		if (currentPlayUserId == null)
			return;
		
		int onlineUserCount = GameUserManager.getInstance().getOnlineUserCount();		
		List<GameUser> list = session.getUserList().getAllUsers();		
		for (GameUser user : list){
			
			// send notification for the user
			GameMessageProtos.GeneralNotification notification = GameMessageProtos.GeneralNotification.newBuilder()		
				.setCurrentPlayUserId(currentPlayUserId)
				.setNextPlayUserId("")
				.build();
			
			GameMessageProtos.GameMessage response = GameMessageProtos.GameMessage.newBuilder()
				.setCommand(GameCommandType.USER_JOIN_NOTIFICATION_REQUEST)
				.setMessageId(GameEventExecutor.getInstance().generateMessageId())
				.setNotification(notification)
				.setSessionId(session.getSessionId())
				.setUserId(user.getUserId())
				.setToUserId(user.getUserId())				
				.setOnlineUserCount(onlineUserCount)
				.build();
			
			HandlerUtils.sendMessage(response, user.getChannel());
		}
	}

}
