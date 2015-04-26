package com.orange.game.traffic.model.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;

import com.orange.game.traffic.model.dao.GameUser;
import com.orange.game.traffic.model.dao.SimpleChannelUser;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameResultCode;



public class RoomsNotificationManager {		
	
	protected final static Logger logger = 
		Logger.getLogger("RegisterRoomNotification");
	
	private static RoomsNotificationManager defaultManager = new RoomsNotificationManager();
		
	// suppress default constructor for noninstantialblity.
	private RoomsNotificationManager(){
	}
	
	public static RoomsNotificationManager getInstance() {
		return defaultManager;
	}
	
	// A userId-sessionIds register map
	private   ConcurrentHashMap<String, CopyOnWriteArraySet<Integer>> userSessionMap 
		= new ConcurrentHashMap<String, CopyOnWriteArraySet<Integer>>();
	
	// A sessionId-users register map, for performance reason, map to GameUser.
	private  ConcurrentHashMap<Integer,CopyOnWriteArraySet<SimpleChannelUser>> sessionUserMap
		= new ConcurrentHashMap<Integer, CopyOnWriteArraySet<SimpleChannelUser>>();
	

	public GameResultCode registerSessionIds(String userId, Channel channel, List<Integer> toBeRegisteredSessionIdList) {
		
		SimpleChannelUser user = new SimpleChannelUser(userId, channel);
		
		GameResultCode gameResultCode = GameResultCode.SUCCESS;
		
		print("<RoomsNotificationManager>Before registering");
		
		if ( userSessionMap.contains(userId) == false ) {
			// We new a new copy of the sessionIdList, in case of 
			// the operation on toBeRegisteredSessionIdList 
			// throws UnsupportedOperationException.
			userSessionMap.put(userId, new CopyOnWriteArraySet<Integer>(toBeRegisteredSessionIdList));
		} 
		else {
			 CopyOnWriteArraySet<Integer> resultSessionIdList = userSessionMap.get(userId);
			 resultSessionIdList.addAll(toBeRegisteredSessionIdList);
 		}
				
		// Update the reverse sessionId-userId map.
		registerGameUser(user, toBeRegisteredSessionIdList);
		
		print("<RoomsNotificationManager>After registering");		
		return gameResultCode;
	}
	
	@Deprecated
	 private synchronized void doNoduplicationRegisteration(String userId, List<Integer> toBeRegisteredSessionIdList) {
		 
		 // Checkout the userId's registered sessionid list for process.
		 CopyOnWriteArraySet<Integer> resultSessionIdList = userSessionMap.get(userId);
		 resultSessionIdList.addAll(toBeRegisteredSessionIdList);

//		 List<Integer> duplicateSessionIdList = new ArrayList<Integer>(toBeRegisteredSessionIdList);
//		 // Remove duplication from to-be-registered sessionId list, then add it. 
//		 duplicateSessionIdList.retainAll(resultSessionIdList);
//		 toBeRegisteredSessionIdList.removeAll(duplicateSessionIdList);
//		 resultSessionIdList.addAll(toBeRegisteredSessionIdList);
//		 
//		 userSessionMap.put(userId,(ArrayList<Integer>)resultSessionIdList);

	}
	 

	 
	 public GameResultCode unRegisterSessionIds(String userId, List<Integer> toBeUnRegisteredSessionIdList) {
			
		 	GameResultCode gameResultCode = GameResultCode.SUCCESS;
		 	
		 	defaultManager.print("<RoomsNotificationManager>Before unregistering");
			SimpleChannelUser user = new SimpleChannelUser(userId, null);

			if ( toBeUnRegisteredSessionIdList == null ) {
				 // if session list is null, then remove all sessions in the user
				 CopyOnWriteArraySet<Integer> sessionIdList = userSessionMap.get(userId);
				 if (sessionIdList == null){
					 return gameResultCode;
				 }
				 
				 // remove session ID from sessionUserMap			 
				 for ( Integer sessionId: sessionIdList ) {
					 CopyOnWriteArraySet<SimpleChannelUser> userList = sessionUserMap.get(sessionId);
					 if (userList != null){
						 userList.remove(user);
					 }
				 }
				 
				 // remove  
				 userSessionMap.remove(userId);			 
				 return gameResultCode;
			 } 
			
			CopyOnWriteArraySet<Integer> sessionIdList = userSessionMap.get(userId);
			if (sessionIdList != null){
				
				// remove user ID from sessionUserMap
				for ( Integer sessionId: sessionIdList ) {
					 CopyOnWriteArraySet<SimpleChannelUser> userList = sessionUserMap.get(sessionId);
					 if (userList != null){
						 userList.remove(user);
					 }				
				}
				
				// remove session ID from userSessionMap
				sessionIdList.removeAll(toBeUnRegisteredSessionIdList);
			}

			defaultManager.print("<RoomsNotificationManager>After unregistering");						
			return gameResultCode;
	 }
	 
//	 private synchronized void doUnRegisteration(String userId, List<Integer> toBeUnRegisteredSessionIdList) {
//		 
//		 	List<Integer> resultSessionIdList = null;
//		 	
//		 	// Exact userId's value(a sessionId list) to process.
//			resultSessionIdList = userSessionMap.get(userId);
//			resultSessionIdList.removeAll(toBeUnRegisteredSessionIdList);
//			// Put back the change value(the changed sessionId list).
//			userSessionMap.put(userId,resultSessionIdList);
//	 }
			 
	 public synchronized void registerGameUser(SimpleChannelUser user, List<Integer> toBeRegisteredSessionIdList) {

		 for (Integer sessionId: toBeRegisteredSessionIdList) {
			 
			 CopyOnWriteArraySet<SimpleChannelUser> userList = sessionUserMap.get(sessionId);
			 
			 if (userList == null){
				 // no user in the session, create a new set
				 CopyOnWriteArraySet<SimpleChannelUser> newUserList = new CopyOnWriteArraySet<SimpleChannelUser>();
				 newUserList.add(user);
				 sessionUserMap.put(sessionId, newUserList);
			 }
			 else{
				 // user list found, add user into list
				 userList.add(user);				 
			 }			 
		} 
	}
	
	public synchronized void unRegisterGameUser(String userId, List<Integer> toBeUnRegisteredSessionIdList) {
		
		
	 }
	 
	public void print(String locationString) {
		logger.debug(locationString+": <RoomNotificationManager> userSessionMap is :\n"
				+ userSessionMap.toString()+"sessionUserMap is : \n"+sessionUserMap.toString());
	}

	
	public List<SimpleChannelUser> getGameUser(int sessionId) {
		CopyOnWriteArraySet<SimpleChannelUser> list = sessionUserMap.get(sessionId);
		if (list == null){
			return Collections.emptyList();
		}
		else {
			return new ArrayList<SimpleChannelUser>(list);
		}
	}
	
}