package com.orange.game.traffic.model.manager;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.eclipse.jetty.util.log.Log;

import com.orange.common.log.ServerLog;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.model.manager.UserGameStatusManager;
import com.orange.game.traffic.model.dao.GameSession;
import com.orange.game.traffic.model.dao.GameUser;
import com.orange.game.traffic.server.GameEventExecutor;
import com.orange.game.traffic.server.GameServer;
import com.orange.game.traffic.server.NotificationUtils;
import com.orange.game.traffic.service.GameDBService;
import com.orange.game.traffic.service.SessionUserService;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;


public abstract class GameSessionManager {

	protected static final Logger logger = Logger.getLogger("GameSessionManager");
	
	public static final int GAME_SESSION_COUNT = 100;

	public static final int USER_CREATED_SESSION_INDEX = 80000;
	public static final AtomicInteger sessionIdIndex = new AtomicInteger(USER_CREATED_SESSION_INDEX);
	
	// all active sessions
	ConcurrentMap<Integer, GameSession> sessionList = new ConcurrentHashMap<Integer, GameSession>();
	
   public GameSessionManager(){		
    	initAllGameSession(GAME_SESSION_COUNT);
	} 	    
    
   // abstract methods
   abstract public GameSession createSession(int sessionId, String name, String password, boolean createByUser, 
		   												String createBy,int ruleType,  int maxPlayerCount, int testEnable);
	abstract public String getGameId();
	abstract public int getRuleType();
	abstract public int getTestEnable();
	abstract public int getMaxPlayerCount();
	abstract public boolean takeOverWhenUserQuit(GameSession session, GameUser quitUser, int sessionUserCount);
	abstract public void updateQuitUserInfo(GameSession session, GameUser quitUser);
	abstract public void postActionForUserQuitSession(GameSession session, GameUser quitUser);	
	
	public GameCommandType  getCommandForUserQuitSession(GameSession session, GameUser quitUser, int sessionUserCount){
		
		/**
		 *  注意！ 
		 *  由于userQuitSession中调用此方法后才把这个离开的用户从房间中移除，
		 *  所以，此时的sessionUserCount是把离开的这个用户算在内的。      
		 *  所以，此时的userCount是把离开的这个用户算在内的。      
		 */
		GameCommandType command = null;		
		if (session.isCurrentPlayUser(quitUser.getUserId())){
			command = GameCommandType.LOCAL_PLAY_USER_QUIT;			
		}
		else if (sessionUserCount <= 2){ 
			command = GameCommandType.LOCAL_ALL_OTHER_USER_QUIT;			
		}
		else {
			command = GameCommandType.LOCAL_OTHER_USER_QUIT;						
		}	
		
		return command;
	}
	
	
	public void userQuitSession(GameSession session, String userId, boolean needFireEvent, boolean needRemoveUserChannel) {
		
		if ( session == null ) 
			return;
		
		
		int sessionId = session.getSessionId();
		ServerLog.info(sessionId, "<GameSessionManager.userQuitSession> User "+userId+" tries to quit from session " + sessionId);
		
		GameUser user = session.findUser(userId);
		if (user == null){
			ServerLog.info(sessionId, "<GameSessionManager.userQuitSession> User "+userId+"tries to quit from session " + sessionId 
					+ ", but user not found in session");
			return;
		}
				
		int sessionUserCount = session.getUserCount();
		boolean takeOverUser = takeOverWhenUserQuit(session, user, sessionUserCount);
		if (takeOverUser){
			session.takeOverUser(userId);
			NotificationUtils.broadcastUserStatusChangeNotification(session, userId);
		}
		
		updateQuitUserInfo(session, user);
		
		// update DB User Game Status for user quit
		updateUserQuitGameStatus(session, user);		
				
		// fire message
		GameCommandType command = getCommandForUserQuitSession(session, user, sessionUserCount);					
		if (command != null && needFireEvent){
			GameEventExecutor.getInstance().fireAndDispatchEvent(command, sessionId, userId);
		}
		
		if (!takeOverUser){
			SessionUserService.getInstance().removeUser(session, userId, needRemoveUserChannel);
		}
		
		postActionForUserQuitSession(session, user);
	}
	
    private void updateUserQuitGameStatus(final GameSession session, final GameUser user) {
		GameDBService.getInstance().executeDBRequest(session.getSessionId(), new Runnable(){
			@Override
			public void run() {
				MongoDBClient dbClient = GameDBService.getInstance().getMongoDBClient(session.getSessionId());
				UserGameStatusManager.userQuitGame(dbClient, user.getUserId());
			}			
		});	
	}
    
    public void updateUserEnterGameStatus(final GameSession session, final GameUser user) {
    	
    	if (user != null && user.isRobotUser()){
    		ServerLog.info(session.getSessionId(), "user "+user.getUserId()+" is robot, skip update status");
    	}
    	
		GameDBService.getInstance().executeDBRequest(session.getSessionId(), new Runnable(){
			@Override
			public void run() {
				MongoDBClient dbClient = GameDBService.getInstance().getMongoDBClient(session.getSessionId());
				UserGameStatusManager.userEnterGame(dbClient, 
						user.getUserId(), 
						GameServer.getServerAddress(), 
						GameServer.getPort(), 
						GameServer.getServerId(), 
						session.getSessionId(), 
						getGameId());
			}			
		});	
	}

	public void initAllGameSession(int count){
    	int testEnable = getTestEnable();
		for (int i = 1; i <= count; i++) {
			int sessionId = i;	
			String roomName = "";	// set system room to empty string here
			int ruleType = getRuleType();
			int maxPlayerCount = getMaxPlayerCount();
			GameSession session = createSession(sessionId, roomName, null, false, null,ruleType, maxPlayerCount, testEnable);
			sessionList.put(Integer.valueOf(sessionId), session);						
		}			
	}
        
	public GameSession findSessionById(int id) {
		return sessionList.get(id);		
	}	
	
	public void addAllSessions(){
		Collection<GameSession> sessions = sessionList.values();
		for (GameSession session : sessions) {
			GameSessionAllocationManager.getInstance().addSession(session);
		}
	}
	
	public void addSession(GameSession session){
		sessionList.put(session.getSessionId(), session);
	}
	
	public void removeSession(GameSession session) { 
		
		int sessionId = session.getSessionId();
		if ( sessionList.containsKey(sessionId) ) 
			sessionList.remove(sessionId, session);
	}
	
	public int getUserCreatedSessionIndex(){
		return sessionIdIndex.getAndIncrement();
	}
	
	public GameSession addSession(int sessionId, String roomName, String password, boolean createByUser, String createBy,
			int ruleType, int maxPlayerCount, int testEnable) {
		GameSession session = createSession(sessionId, roomName, password, createByUser, createBy, ruleType, maxPlayerCount, testEnable);
		sessionList.put(session.getSessionId(), session);
		return session;
	}

	
	public Map<Integer, GameSession> getAllSessionList() {
		return sessionList;
	}
	
	static final int configSessionMaxPlayerCount = readMaxPlayerCountFromConfig();	
	private static int readMaxPlayerCountFromConfig() {
		
		int retValue;
		String sessionMaxPlayerCount = System.getProperty("game.maxsessionuser");
		
		if ( sessionMaxPlayerCount != null && ! sessionMaxPlayerCount.isEmpty()) {
			retValue = Integer.parseInt(sessionMaxPlayerCount);
		} else {
			retValue = 0; 
		}
		
		return retValue;
	}
		
	public int readMaxPlayerCount(int defaultValue) {		
		if (configSessionMaxPlayerCount == 0){
			ServerLog.info(0, "GameSession: MaxUserPerSession use default = "+ defaultValue);			
			return defaultValue;
		}
		else{
			ServerLog.info(0, "GameSession: MaxUserPerSession use config = "+ configSessionMaxPlayerCount);			
			return configSessionMaxPlayerCount;
		}
	}

	
	public GameSession findSessionByName(String sessionName) {
		
		for ( Entry<Integer, GameSession> entry : sessionList.entrySet() ) {
		    GameSession session = entry.getValue();
			 if ( session != null && session.getName().equals(sessionName)) {
				 	return session;
		     }
		}
		
		return null;
	}
	
}
