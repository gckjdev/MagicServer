package com.orange.game.traffic.service;

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.channel.Channel;

import com.orange.common.log.ServerLog;
import com.orange.common.network.ChannelUserManager;
import com.orange.game.traffic.model.dao.GameSession;
import com.orange.game.traffic.model.dao.GameUser;
import com.orange.game.traffic.model.manager.GameSessionAllocationManager;
import com.orange.game.traffic.model.manager.GameSessionManager;
import com.orange.game.traffic.model.manager.GameUserManager;
import com.orange.game.traffic.server.GameEventExecutor;
import com.orange.game.traffic.server.NotificationUtils;
import com.orange.game.traffic.statemachine.GameEvent;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;
import com.orange.network.game.protocol.model.GameBasicProtos.PBGameUser;

public class SessionUserService {
	
	protected final GameUserManager userManager = GameUserManager.getInstance();
	protected final GameSessionManager sessionManager = GameEventExecutor.getInstance().getSessionManager();


	// thread-safe singleton implementation
	private static SessionUserService defaultService = new SessionUserService();
	private SessionUserService() {
	}

	public static SessionUserService getInstance() {
		return defaultService;
	}	
	

	
	private void driveStateMachineForUserJoin(int sessionId, GameMessage message, Channel channel){
		GameEvent event = new GameEvent(
				GameCommandType.LOCAL_NEW_USER_JOIN, 
				sessionId, 
				message, 
				channel);
		
		GameEventExecutor.getInstance().dispatchEvent(event);				
	}
	
	public GameUser addUserIntoSession(GameSession session, PBGameUser pbUser, Channel channel, GameMessage requestMessage){
		int sessionId = session.getSessionId();
		String userId = pbUser.getUserId();

		// add user and add user into session
		GameUser user = userManager.addUser(pbUser, channel, sessionId);
		session.addUserIntoSession(user);
		
		// add user into channel
		ChannelUserManager.getInstance().addUserIntoChannel(channel, userId);
				
		// invoke notification service
		PBGameUser pbUserUpdated = user.getPBUser();
		if (session.isDrawGameSession()){
			NotificationUtils.broadcastDrawUserJoinNotification(session, user);
		}
		else{
			RoomNotificationService.getInstance().notifyUserJoin(session, pbUserUpdated, userId, false);
		}
						
		// drive state machine running
		driveStateMachineForUserJoin(sessionId, requestMessage, channel);
		
		// just print log here for debug
		session.getUserList().printUsers();		
		
		return user;
	}
	
	public void kickPlayUser(GameSession session) {
		
		int sessionId = session.getSessionId();
		String userId = session.getCurrentPlayUserId();
		if (userId == null)
			return;
		
		ServerLog.info(sessionId, "<kickPlayUser> "+userId);		
		removeUser(session, userId, true);
	}		
	
	public void kickTakenOverUser(GameSession session) {
		
		// only remove user from session, don't need to remove user from 
		List<String> removerUsers = session.removeTakenOverUser();
		
		// update session priority
		if (removerUsers.size() > 0){
			GameSessionAllocationManager.getInstance().updateSessionPriority(session);			
		}
		
		// broadcast user exit message to all other users
		if (session.isDrawGameSession()){
			NotificationUtils.broadcastDrawUserQuitNotification(session, removerUsers);
		}
		else{
			RoomNotificationService.getInstance().notifyUserQuit(session, removerUsers);			
		}
	}
	
	public void removeUser(GameSession session, String userId, boolean needRemoveUserChannel) {
		
		GameUser user = session.findUser(userId);
		if (user == null){
			return;
		}
		
		if (user.isRemoving()){
			return;
		}
		user.setRemoving();
		
		// remove user frome session
		session.removeUser(userId);
		
		List<String> removerUsers = new ArrayList<String>();
		removerUsers.add(userId);
		
		// update session priority
		if (removerUsers.size() > 0){
			GameSessionAllocationManager.getInstance().updateSessionPriority(session);			
		}
		
		// remove channel if needed
		if (needRemoveUserChannel){
			GameUserManager.getInstance().removeUser(userId);
			ChannelUserManager.getInstance().removeChannel(user.getChannel());
		}
		
		// broadcast user exit message to all other users
		if (session.isDrawGameSession()){
			NotificationUtils.broadcastDrawUserQuitNotification(session, removerUsers);
		}
		else{
			RoomNotificationService.getInstance().notifyUserQuit(session, removerUsers);			
		}
		
	}
}
