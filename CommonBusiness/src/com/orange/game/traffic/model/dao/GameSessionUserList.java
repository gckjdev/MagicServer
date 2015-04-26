package com.orange.game.traffic.model.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.http.annotation.ThreadSafe;
import org.apache.log4j.Logger;

import com.orange.common.log.ServerLog;
import com.orange.network.game.protocol.model.GameBasicProtos.PBGameUser;

@ThreadSafe
public class GameSessionUserList {

	protected static final Logger logger = Logger.getLogger("GameSessionUserManager");
	private final int MAX_USER_PER_SESSION ;
	
		
	final int sessionId;
	
	GameUser[] userList ;	
	volatile int currentPlayUserIndex = -1;
		
    
    public GameSessionUserList(int sessionId, int maxUserPerSession){
    	this.sessionId = sessionId;
    	this.MAX_USER_PER_SESSION = maxUserPerSession;
    	this.userList = new GameUser[MAX_USER_PER_SESSION];
    }

    private synchronized int getUserCount(){
    	int count = 0;
		for (GameUser user : userList){
			if (user != null){
				count ++;
			}
		}
		
		return count;
    }
    
    private synchronized int addUser(GameUser user){
    	int seatId = -1;
    	for (int i=0; i<userList.length; i++){
    		if (userList[i] == null){
    			userList[i] = user;
    			seatId = i;
    			user.setSeatId(seatId);    			
    			return seatId;
    		}
    	}
    	
    	return seatId;
    }
    
    private synchronized boolean removeUser(GameUser user){
    	if (user == null)
    		return false;
    	
    	String userId = user.getUserId();
    	if (userId == null)
    		return false;
    	
    	for (int i=0; i<userList.length; i++){
    		if (userList[i] != null && userList[i].getUserId().equals(userId)){
    			userList[i] = null;
    			return true;
    		}
    	}
    	
    	return false;
    }    
    
    public synchronized void setAllUserPlaying(){
    	ServerLog.info(sessionId, "<setAllUserPlaying>");
    	for (GameUser user : userList){
    		if (user == null)
    			continue;
    		
    		user.setPlaying(true);
    		user.setLoseGame(false); // 一开始肯定未输
    		ServerLog.debug(sessionId, "set user "+user.getNickName()+ " PLAYING");
    	}
    }

    public synchronized void clearAllUserPlaying(){
    	for (GameUser user : userList){
    		if (user == null)
    			continue;
    		
    		user.setPlaying(false);
    		ServerLog.debug(sessionId, "set user "+user.getNickName()+ " NOT PLAYING");
    	}
    }
    
    public synchronized void setAllPlayerLoseGameToFalse() {
    	for (GameUser user : userList){
    		if (user == null)
    			continue;
    		
    		user.setLoseGame(false);
    		ServerLog.debug(sessionId, "set user "+user.getNickName()+ " loseGame to false");
    	}
    }
    
    public synchronized GameUser findGameUserById(String userId){
    	for (GameUser user : userList){
    		if (user == null)
    			continue;    		
    		
    		if (user.getUserId().equals(userId)){
    			return user;
    		}
    	}
    	
    	return null;
    }
    
    public synchronized int addUserIntoSession(GameUser user){
    	
    	GameUser userFound = findGameUserById(user.getUserId());
    	if (userFound != null){
    		userFound.setTakenOver(false);    		
        	ServerLog.info(sessionId, "<GameSessionUserList.addUserIntoSession> user="+user.getNickName()+" found, set take over false");
    		return getUserCount();
    	}
    	
    	int seatId = addUser(user);
    	
    	user.setCurrentSessionId(sessionId);    	    	
    	user.setSeatId(seatId);
//    	ServerLog.info(sessionId, "<addUserIntoSession> user="+user.getNickName()+", sessionId="+sessionId);
//    	ServerLog.info(sessionId,"<addUserIntoSession> userList now is： " + this);
    	int userCount = getUserCount();
    	if (userCount == 1){
    		currentPlayUserIndex = seatId;
    	}
    	
    	return userCount;
    }
    
    public synchronized void removeUserFromSession(String userId){
    	if (userId == null){
    		ServerLog.info(sessionId, "<GameSessionUserList.removeUserFromSession> session not found, user = "+userId+", sessionId = "+sessionId);    	
    		return;
    	}
    	
    	GameUser userFound = null;
    	for (GameUser user : userList){
    		if (user == null)
    			continue;    		
    		
    		if (user.getUserId().equals(userId)){
    			userFound = user;
    			break;
    		}
    	}
    	
    	if (userFound != null){
    		ServerLog.info(sessionId, "<GameSessionUserList.removeUserFromSession> user = "+userFound+", sessionId = "+sessionId);
    		removeUser(userFound);
    	} else{
    		ServerLog.info(sessionId, "<GameSessionUserList.removeUserFromSession> cannot find user, user = "+userId+", sessionId = "+sessionId);    		
    	}
    }
    
	public synchronized List<String> removeTakenOverUser() {
    	
		List<String> removeList = new ArrayList<String>();		
    	for (int i=0; i<userList.length; i++){
    		
    		if (userList[i] == null)
    			continue;    		    		    		
    		
    		if (userList[i].isTakenOver){
    			removeList.add(userList[i].getUserId());
    			userList[i] = null;
    		}
    	}
    	
    	if (removeList.size() > 0){
    		ServerLog.info(sessionId, "<removeTakenOverUser> user="+removeList.toString());
//    		userList.removeAll(removeUserList);
    		
//    		for (GameUser user : removeUserList){
//    			// release the seat here
//        		seatList.deallocSeat(user.getSeatId());
//    		}
    		
    		return removeList;
    	}
    	else{
    		return Collections.emptyList();
    	}	
    }
    
//    public List<GameUser> getUserList(){
//    	return userList;
//    }
    
    public synchronized int getSessionUserCount(){
    	return getUserCount();
    }
        
	public synchronized List<PBGameUser> usersToPBUsers() {		
		List<PBGameUser> list = new ArrayList<PBGameUser>();
		for (int i=0; i<userList.length; i++){
			GameUser user = userList[i];
    		if (user == null)
    			continue;    		
			
			PBGameUser pbUser = user.getPBUser();
			PBGameUser pbUserForAdd = PBGameUser.newBuilder(pbUser).setSeatId(user.getSeatId()).build();
			list.add(pbUserForAdd);
		}
		return list;
	}
	
	public synchronized boolean isSessionFull(int sessionId) {
		return getUserCount() >= MAX_USER_PER_SESSION ? true : false;
	}	
    
	public synchronized boolean isSessionEmpty(int sessionId) {		
		return (getUserCount() == 0);
	}

	public synchronized void takeOverUser(String userId) {
		for (GameUser user : userList){
    		if (user == null)
    			continue;    		
			
			if (userId.equals(user.getUserId())){
				ServerLog.info(sessionId, "Take over user "+user.toString());
				user.setTakenOver(true);				
			}
		}
	}
	


	public synchronized GameUser getUser(String userId) {
		if (userId == null)
			return null;
		
		for (GameUser user : userList){
    		if (user == null)
    			continue;    		
			
			if (userId.equals(user.getUserId())){
				return user;
			}
		}
		
		return null;
	}

	public synchronized int getPlayUserCount() {
		int count = 0;
		for (GameUser user : userList){
			if (user == null)
				continue;
			
			if (user.isPlaying)
				count ++;
		}
		return count;
	}

	public void printUsers(){
		ServerLog.info(sessionId, "users="+Arrays.toString(userList));
	}

	public synchronized List<GameUser> getPlayingUserList() {
		List<GameUser> retList = new ArrayList<GameUser>();
		for (GameUser user : userList){
    		if (user == null)
    			continue;    		
			
			if (user.isPlaying){
				retList.add(user);
			}
		}
		return retList;
	}

	public synchronized  boolean isUserTakenOver(String userId) {
		for (GameUser user : userList){
    		if (user == null)
    			continue;    		
			
			if (user.isTakenOver && user.getUserId().equals(userId)){
				return true;
			}
		}

		return false;
	}

	public synchronized boolean isAllUserTakenOver() {				
		for (GameUser user : userList){
    		if (user == null)
    			continue;    		
			
			if (user.isPlaying && !user.isTakenOver){
				ServerLog.info(sessionId, "userId "+user.getUserId()+ " not taken over");
				return false;
			}
		}

		return true;
	}
	
	public String toString() {
		StringBuilder resultString = new StringBuilder("");
		
		for (GameUser user: userList) {
			if (user == null)
				break;
			else {
				resultString.append(user.toString());
				resultString.append("\n");
			}
		}
		
		return resultString.toString();
	}

	public GameUser GetCurrentPlayUserSafe(){
		if (currentPlayUserIndex == -1){
			return null;
		}
		
		if (currentPlayUserIndex >= 0 && currentPlayUserIndex < MAX_USER_PER_SESSION){
			if (userList[currentPlayUserIndex] == null)
				return null;
			
			return userList[currentPlayUserIndex];
		}
		
		return null;
	}
	
	public String getCurrentPlayUserIdSafe(){
		GameUser user = GetCurrentPlayUserSafe();
		if (user != null)
			return user.getUserId();
		else
			return null;
	}
	
	public void clearCurrentPlayUser(){
		currentPlayUserIndex = -1;
	}
	
	public GameUser selectCurrentPlayUserByDirection(int playDirection){

		int i = 0;
		int stepLength = (playDirection == GameSessionConstant.CLOCK_WISE ? 1 : -1);
		// if step counterclockwise, should wrap it.
		int wrap = ( playDirection == GameSessionConstant.CLOCK_WISE ? 0 : userList.length);
//		ServerLog.info(sessionId, "Now the currentPlayUserIndex is " + currentPlayUserIndex + ", userList.length is "+userList.length);
		if ( currentPlayUserIndex == -1 ) {
			i = 0; // Initial 
		} else {
			i = (currentPlayUserIndex + wrap + stepLength) % userList.length;
		}
		int count = 0 ;
		
		while ( i < userList.length && count < userList.length) {
			if ( userList[i] != null && userList[i].isPlaying && ! userList[i].loseGame) {
				currentPlayUserIndex = i;
				String direction = (playDirection == 0? "CLOCK WISE": "COUNTER CLOCKWISE");
				ServerLog.info(sessionId, "<selectCurrentPlayUserByDirection> " + "Direction is "+ direction +
						", select user: "+userList[i].getNickName());
				return userList[i];
			}
			i= (i + wrap + stepLength) % userList.length;
			count++;
		}
		
		ServerLog.info(sessionId, "<selectCurrentPlayUserByDirection> but no user selected");
		return null;	
	}

	// Just peek, not actually select, use selectCurrentPlayUserByDirection for selection
	public String peekNextPlayerId(int playDirection) {
		
		int i = 0;
		int stepLength = (playDirection == GameSessionConstant.CLOCK_WISE ? 1 : -1);
		// if step counterclockwise, should wrap it.
		int wrap = ( playDirection == GameSessionConstant.CLOCK_WISE ? 0 : userList.length);
		
		if ( currentPlayUserIndex == -1 ) {
			i = 0; // Initial 
		} else {
			i = (currentPlayUserIndex + wrap + stepLength) % userList.length;
		}
		int count = 0 ;
		while ( i < userList.length && count < userList.length) {
			if ( userList[i] != null && userList[i].isPlaying ) {
				ServerLog.info(sessionId, "<peekNextPlayerId> " + "Direction is "+ playDirection +
						", select user: "+userList[i].toString());
				return userList[i].getUserId();
			}
			i= (i + wrap + stepLength) % userList.length;
			count++;
		}
		
		return null;	
	}
	
	
	public List<GameUser> getAllUsers() {
		ArrayList<GameUser> users = new ArrayList<GameUser>();
		for (int i=0; i<userList.length; i++){
			if (userList[i] != null){
				users.add(userList[i]);
			}
		}
		return users;
	}
	
	public List<String> getAllUserIds() {
		List<String> users = new ArrayList<String>();
		for (int i=0; i<userList.length; i++){
			if (userList[i] != null){
				users.add(userList[i].getUserId());
			}
		}
		return users;
	}	

	public void selectCurrentPlayUser(int index) {		
		if (index < 0 || index >= userList.length)
			return;
		
		currentPlayUserIndex = index;
	}

	public boolean isCurrentPlayUser(String userId) {
		if (userId == null)
			return false;
				
		return userId.equals(getCurrentPlayUserIdSafe());
	}

	public int getRobotUserCount() {
		int count = 0;
		for (int i=0; i<userList.length; i++){
			if (userList[i] != null && userList[i].isRobotUser())
				count ++;
		}
		return count;
	}

	public void setUserBalance(String userId, int balance) {
		GameUser user = getUser(userId);
		if (user != null){
			if (balance <= 0){
				user.setBalance(0);
			}
			else{
				user.setBalance(balance);
			}
		}
	}

	public List<String> getZombieUserIdList() {
		List<String> retList = new ArrayList<String>();
		for (int i=0; i<userList.length; i++){
			if (userList[i] != null && userList[i].isZombie())
				retList.add(userList[i].getUserId());
		}
		return retList;
	}
	
}
