package com.orange.game.traffic.model.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.orange.common.log.ServerLog;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.Relation;
import com.orange.game.model.manager.RelationManager;
import com.orange.game.traffic.model.dao.GameSession;
import com.orange.game.traffic.server.GameServer;

public class GameSessionAllocationManager {
	private final static Logger logger = Logger.getLogger(GameSessionAllocationManager.class.getName());
	private final static MongoDBClient mongoClient = new MongoDBClient(DBConstants.D_GAME);

	private class GamePrioritySession implements Comparable<GamePrioritySession>{
		final int sessionId;

		volatile int priority = 0;
		
		public GamePrioritySession(int sessionId){
			this.sessionId = sessionId;
		}

		public int getSessionId() {
			return sessionId;
		}
		
		public void setPriority(int allocationPriority) {
			priority = allocationPriority;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + sessionId;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			GamePrioritySession other = (GamePrioritySession) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (sessionId != other.sessionId)
				return false;
			return true;
		}

		private GameSessionAllocationManager getOuterType() {
			return GameSessionAllocationManager.this;
		}

		@Override
		public String toString() {
			return "[priority=" + priority + ", sessionId="
					+ sessionId + "]";
		}

		@Override
		public int compareTo(GamePrioritySession ps) {
			return (ps.priority - this.priority); // arrange in descending order by priority
		}				
	}

	public static int NO_SESSION_AVAILABLE = -1;
	
	private static final int MAX_FULL_RETRY = 10;
	private PriorityQueue<GamePrioritySession> prioritySessionQueue = new PriorityQueue<GamePrioritySession>();
	private GameSessionManager sessionManager;
	
	// thread-safe singleton implementation
   private static GameSessionAllocationManager sessionAllocManager = new GameSessionAllocationManager();     
   private GameSessionAllocationManager() {} 	    
   public static GameSessionAllocationManager getInstance() { 
    	return sessionAllocManager; 
    } 
    

   
    // request to alloc a session from existing session pool
    public synchronized GameSession allocSession(String userId, Set<Integer> excludeSessionSet){

//      ServerLog.info(0, "queue="+queue.toString());    	
    	
    	List<GamePrioritySession> sessionForPutBackList = new ArrayList<GamePrioritySession>();
    	GamePrioritySession prioritySession = prioritySessionQueue.poll();	
    	int fullRetryCount = 0;
    	boolean result = false;
    	GameSession retSession = null;
    	while (true){
    		
	    	if (prioritySession == null){
	    		// failure
	    		break;
	    	}
	    	
	    	GameSession session = sessionManager.findSessionById(prioritySession.sessionId);
	    	if (session == null){
	    		// failure
	    		break;
	    	}
	    	
	    	// 1) Not allowed to enter a room that has password
	    	// 2) Session ID should not in excluded session ID set
	    	if (session.hasPassword() || (excludeSessionSet != null && excludeSessionSet.contains(session.getSessionId()))) {
	    		// save as old
        		GamePrioritySession oldPrioritySession = prioritySession;
    			sessionForPutBackList.add(oldPrioritySession);
    			
	    		// find a new session in queue 
	    		prioritySession = prioritySessionQueue.poll();    			
	    		continue;
	    	}
	    	
	    	boolean isSessionFull = session.isFull();	    	
	    	if (isSessionFull){
	    		
	    		// find next session in queue 
	    		prioritySession = prioritySessionQueue.poll();	    		
	    		
	    		fullRetryCount ++;
	    		if (fullRetryCount > MAX_FULL_RETRY){
		    		// failure, reach max full retry
	    			break;
	    		}
	    		
	    		continue;
	    	}	    		
	    	
//	    	boolean isUserTakenOver = session.isUserTakenOver(userId);    		
//    		if (isUserTakenOver){

	    	boolean hasUser = session.hasUser(userId);
	    	if (hasUser){
    			// save as old
        		GamePrioritySession oldPrioritySession = prioritySession;
    			sessionForPutBackList.add(oldPrioritySession);
    			
	    		// find a new session in queue 
	    		prioritySession = prioritySessionQueue.poll();    			
	    		continue;
    		}	    		
	    	
	    	boolean canAlloc = session.canAllocate();
	    	if (!canAlloc){
    			// save as old
        		GamePrioritySession oldPrioritySession = prioritySession;
    			sessionForPutBackList.add(oldPrioritySession);
    			
	    		// find a new session in queue 
	    		prioritySession = prioritySessionQueue.poll();    			
	    		continue;
    		}	  	    	
	    	
    		// update session priority and add into queue again
    		prioritySession.setPriority(session.getAllocationPriority(true));
    		prioritySessionQueue.add(prioritySession);
    		
        	ServerLog.info(session.getSessionId(), "<GameSessionAllocationManager.AllocSession> " +
        			"priority="+prioritySession.priority);				    	    			    	
        	result = true;
        	retSession = session;
    		break;
    	}    	
    	
    	prioritySessionQueue.addAll(sessionForPutBackList);
    	return retSession;
    }
    
    public synchronized GameSession allocSession(String userId){
    	return allocSession(userId, null);
    }
    
    // request to alloc a session directly
    public synchronized GameSession allocSession(String userId, int sessionId){

    	GamePrioritySession prioritySession = new GamePrioritySession(sessionId);    	
    	prioritySessionQueue.remove(prioritySession);
    	
    	GameSession session = sessionManager.findSessionById(prioritySession.sessionId);
    	if (session == null)
    		return null;
    	
    	if (session.isFull()){
        	return null;
    	}	    		
    	
    	if (!session.canAllocate()){
    		return null;
    	}
    	
		// update session priority and add into queue again
		prioritySession.setPriority(session.getAllocationPriority(true));
		prioritySessionQueue.add(prioritySession);
		
    	ServerLog.info(session.getSessionId(), "<AllocSession.withSessionId> priority="+prioritySession.priority);				    	
    	return session;
    }
    
    
    public synchronized void releaseSession(int sessionId) {
    	
    	GameSession session = sessionManager.findSessionById(sessionId);
    	
    	if ( session == null ) {
    		ServerLog.info(sessionId, "<GameSessionAllocationManager> Try to release sesison "+sessionId
    				+ ", but session doesn't exit");
    		return;
    	}
    	
    	if ( session.getUserCount() > 0 ) {
    		ServerLog.info(sessionId, "<GameSessionAllocationManager> Try to release sesison "+sessionId
    				+ ", but session is not empty ?!");
    		return;
    	}
    	
    	prioritySessionQueue.remove(session);
    	sessionManager.removeSession(session);
    	ServerLog.info(sessionId, "<GameSessionAllocationManager> Release user-created session "+ sessionId);
    }
    
    
    // call this method after user quit session
    public synchronized void updateSessionPriority(GameSession session){
    	if (session == null)
    		return;

    	GamePrioritySession prioritySession = new GamePrioritySession(session.getSessionId());  
    	prioritySessionQueue.remove(prioritySession);
    	
    	if (session.isFull()){
    		return;
    	}
    	
		// update session priority and add into queue again
    	int newPriority = session.getAllocationPriority(false);
		prioritySession.setPriority(newPriority);
		prioritySessionQueue.add(prioritySession);

    	ServerLog.info(session.getSessionId(), "<updateSessionPriority> priority="+prioritySession.priority);		    
    }
    
    // call this method after user quit session
    public synchronized void addSession(GameSession session){
    	
    	if (session == null)
    		return;

    	GamePrioritySession prioritySession = new GamePrioritySession(session.getSessionId());
    	int newPriority = session.getAllocationPriority(false);
		prioritySession.setPriority(newPriority);
		prioritySessionQueue.add(prioritySession);

    	ServerLog.info(session.getSessionId(), "Session Added");		
    }
    
	public void setSessionManager(GameSessionManager sessionManager) {
		this.sessionManager = sessionManager;		
	}
	
	public synchronized List<Integer>  getSessionList(int start, int end, String keyWord, int roomType, String userId) {
		
		int cursor = 0;
	
		// TODO: should use strategy pattern to refactor these dirty codes...?
		if ( !keyWord.isEmpty() ) {
			return searchByKeyWord(start, end, keyWord, cursor);
		} 
		else if ( roomType == 1 ) {
			return searchByFriendsRooms(start, end, userId, cursor);
		}
		else {
			return searchByRoomPriority(start, end, cursor);	
		} 
		
	}
	
	
	
	private synchronized List<Integer> searchByRoomPriority(int start, int end, int cursor) {
		
		List<Integer> resultlist = new ArrayList<Integer>();
		
//		Object[] array = queue.toArray();
		
		// TODO, performance is extremely so so here, can be improved a lot
		Map<Integer, GameSession> sessionList = sessionManager.getAllSessionList();
		Object[] array = sessionList.values().toArray();
		for (Object obj : array){
			((GameSession)obj).calculateDisplayPriority();
		}
		
		Arrays.sort(array);

		for(Object session : array) {
			if (cursor < start) {
				cursor++;
				continue;
			}
			else if (cursor > end) {
				break;
			}
			else {
				// Chances are that we get to the very rear of 
				// the queue, so we just stop and return the 
				// the resultlist.
				if ( session == null )
					break;
			
				resultlist.add(((GameSession)session).getSessionId());
				cursor++;
			}	
		}
		
		return resultlist;
	}
	
	private List<Integer> searchByFriendsRooms(int start, int end, String userId, int cursor) {
						
		String serverId = GameServer.getServerId();
		int offset = start;
		int limit = (end - start);
		List<Relation> friendList = RelationManager.getAllOnlineFollowUsers(mongoClient, userId, serverId, offset, limit);
		if ( friendList == null || friendList.size() == 0) {
			return Collections.emptyList();
		}
		
		List<Integer> resultlist = new ArrayList<Integer>();
		for (Relation friend : friendList){
			int sessionId = friend.getGameSessionId();
			if (sessionId != -1 && !resultlist.contains(sessionId)){
				resultlist.add(sessionId);
			}
		}
		return resultlist;

		/*
		boolean finishSearching = false;
		Map<Integer, GameSession> sessionList = sessionManager.getAllSessionList();
		for ( Map.Entry<Integer, GameSession> entry : sessionList.entrySet() ) {
			GameSession session = entry.getValue();
			// Chances are that we get to the very rear of 
			// the queue, so we just stop and return the 
			// the resultlist.
			if ( session == null ) {
				finishSearching = true;
				break;
			}
			String createdBy = session.getCreateBy();
			for( ObjectId uid: friendList) {
				if ( uid != null && uid.toString().equals(createdBy) ) {
					
					if (cursor < start) {
						cursor++;
						break;
					} else if (cursor > end){
						finishSearching = true;
						break;
					} else {
						resultlist.add(session.getSessionId());
						cursor++;
						break;
					}
				}
			} // inner for
			if ( finishSearching ) 
				break;
		} // outer for
		return resultlist;
		*/
	}
	
	private List<Integer> searchByKeyWord(int start, int end, String keyWord, int cursor) {
		
		List<Integer> resultlist = new ArrayList<Integer>();
		
		String regExpr = "[\u4E00-\u9FA5]+|[A-Za-z0-9]+";//"[\u4E00-\u9FA5]+": 中文
		Pattern pattern = Pattern.compile(regExpr,Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(keyWord);
		StringBuilder string = new StringBuilder("");
		while ( matcher.find())  {
			string.append(matcher.group());
			string.append(","); // use comma as the delimiter.
		}
		String[] toBeSearchedStringArray = string.toString().split(","); 
		
		boolean finishSearching = false;
		int exactMatch = 0; 
		Map<Integer, GameSession> sessionList = sessionManager.getAllSessionList();
		for ( Map.Entry<Integer, GameSession> entry : sessionList.entrySet() ) {
			GameSession session = entry.getValue();
			// Chances are that we get to the very rear of 
			// the queue, so we just stop and return the 
			// the resultlist.
			if ( session == null ) {
				finishSearching = true;
				break;
			}
			String sessionName = session.getName();
			String sessionId = Integer.toString(session.getSessionId());
			int index=0;
			for( String s : toBeSearchedStringArray) {
				if ( sessionId.contains(s) || sessionName.toLowerCase().contains(s.toLowerCase())) {
					if (cursor < start) {
						cursor++;
						break;
					} else if (cursor > end){
						finishSearching = true;
						break;
					} else {
						// exact matchup should put in the corresponding place
						if ( exactMatch < toBeSearchedStringArray.length && sessionId.equals(s) || sessionName.toLowerCase().equals(s.toLowerCase())) {
							resultlist.add(index,session.getSessionId());
							exactMatch++;
						} else {
							resultlist.add(session.getSessionId());
						}
						String matchedString =(sessionName.contains(s) ? sessionName: sessionId);
						ServerLog.info(0, "<GameSessionAllocationManager.getSess ionList> Match["+cursor+"]: " + matchedString);
						cursor++;
						break;
					}
				}
				index++; // the index in toBeSearchedStringArray
			} // inner for
			if ( finishSearching ) 
				break;
		} // outer for
		
		return resultlist;
	}


}
