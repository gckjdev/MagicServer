package com.orange.game.traffic.model.manager;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;

import com.orange.game.traffic.model.dao.GameUser;
import com.orange.network.game.protocol.model.GameBasicProtos.PBGameUser;

// Used to manage all users in the game
public class GameUserManager {

	ConcurrentHashMap<String, GameUser> onlineUserMap = new ConcurrentHashMap<String, GameUser>();
	protected static final Logger logger = Logger.getLogger(GameUserManager.class.getName());
	
	private static GameUserManager userManager = null;
	private GameUserManager() {
	}
	public static GameUserManager getInstance() {
		if (userManager == null) {
			userManager = new GameUserManager();
		}
		return userManager;
	}
	
	public void removeUser(String userId) {
		if (userId != null) {			
			logger.info("<GameUserManager.removeUser> Remove " + userId + " from onlineUserMap, now total online user count " + onlineUserMap.size());
			this.onlineUserMap.remove(userId);
		}	
	}
	
	public GameUser findUserById(String userId) {
		return onlineUserMap.get(userId);
	}
	
	public GameUser addUser(PBGameUser pbUser, Channel channel, int sessionId) {
				
		if (pbUser == null || channel == null)
			return null;
		
		String userId = pbUser.getUserId();
		if (userId == null)
			return null;
	
		
		GameUser user = new GameUser(pbUser, channel, sessionId);

		// add new user
		onlineUserMap.put(userId, user);		
//		logger.info("<GameUserManager.addOnlineuser> The onlineUsrMap now is:" + onlineUserMap.toString());
		return user;
	}
	
	public int findGameSessionIdByUserId(String userId){
		GameUser user = findUserById(userId);
		if (user == null)
			return -1;
		
		return user.getCurrentSessionId();
	}
	
	// TODO move to other place
	private static final int GAME_SERVER_NUM = 1;	
	public int getOnlineUserCount(){
		return onlineUserMap.size() * GAME_SERVER_NUM + 1;
	}	

	
	
}
