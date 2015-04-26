package com.orange.game.traffic.model.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import com.orange.common.log.ServerLog;
import com.orange.common.statemachine.State;
import com.orange.common.utils.StringUtil;
import com.orange.network.game.protocol.model.GameBasicProtos.PBGameSession;
import com.orange.network.game.protocol.model.GameBasicProtos.PBSize;
import com.orange.network.game.protocol.model.GameBasicProtos.PBUserResult;
import com.orange.game.traffic.model.manager.GameSessionManager;
import com.orange.game.traffic.service.GameDBService;

public class GameSession implements Comparable<GameSession>{


	public enum SessionStatus{
		INIT,		// 从加入房间的第一个玩家收到joinGameResponse到GameStartNotification这段时间
		WAIT,    // 从GameOver到下一个GameStartNotification这段时间
		PLAYING, // 从下发GameStartNotifiction到nextPlayerStart这段时间
		ACTUAL_PLAYING; //从第一个nextPlayerStart到GameOver这段时间
		
	};
	

	protected final int   		sessionId;
	final String name;
	final boolean createByUser;
	final String password;
	
	final String 	createBy;
	final Date   	createDate;
	String 	host;
	
	
	// DB service
	protected GameDBService dbService = GameDBService.getInstance();
	
	// ruleType: Normal, High, SuperHigh
	protected int ruleType;
	// test on?	
	protected int testEnable;

	// for decTime item, it stores new decremented interval
	protected int newInterval = 0;	

	// all users in the session
	protected int maxPlayerCount;
	protected GameSessionUserList gameSessionUserList;

	protected volatile State  currentState;
	
	protected volatile int displayPriority = 0;
	
	// play direction: clockwise or counterclockwise
	protected int playDirection = GameSessionConstant.COUNTER_CLOCK_WISE;
	protected boolean directionChanged = false;
	
	protected ConcurrentHashMap<String, PBUserResult> userResults = new ConcurrentHashMap<String, PBUserResult>();
	
	protected SessionStatus status = SessionStatus.INIT;	
	ScheduledFuture<Object> shareTimerFuture = null;
	ScheduledFuture<Object> inviteRobotTimer = null;
	private Object shareTimerType = null;

	public GameSession(int sessionId, String name, String password, boolean createByUser, String createBy, int ruleType, int maxPlayerCount, int testEnable){
		this.sessionId = sessionId;
		this.ruleType = ruleType;
		this.maxPlayerCount = maxPlayerCount;
		this.gameSessionUserList = new GameSessionUserList(sessionId, maxPlayerCount);		
		this.name = name;
		this.createByUser = createByUser;
		this.password = password;
		this.createBy = createBy;
		this.createDate = new Date();
		this.testEnable = testEnable;
	}
	
	public GameSession(int sessionId, String name, String createUserId, String hostUserId, State initState){
		this.sessionId = sessionId;
		this.gameSessionUserList = new GameSessionUserList(sessionId, maxPlayerCount);		
		this.name = name;
		this.createBy = createUserId;
		this.host = hostUserId;
		this.currentState = initState;
		this.createDate = new Date();
		this.createByUser = false;
		this.password = null;
	}

	
	public int getSessionId() {
		return sessionId;
	}

	public synchronized boolean isGamePlaying() {
		return (status == SessionStatus.PLAYING || 
				status == SessionStatus.ACTUAL_PLAYING);
	}

	public synchronized void clearTimer() {
		if (shareTimerFuture != null){
			shareTimerFuture.cancel(false);
			shareTimerFuture = null;
			shareTimerType = null;
		}
	}

	public synchronized  void setTimer(ScheduledFuture<Object> future, Object timerType) {
		this.shareTimerFuture = future;
		this.shareTimerType  = timerType;
	}

	public synchronized State getCurrentState() {
		return currentState;
	}

	public synchronized void setCurrentState(State nextState) {
		currentState = nextState;		
	}
	
	public GameSessionUserList getUserList(){
		return gameSessionUserList;
	}

	synchronized public boolean isFullAfterAddingUser() {
		int userCount = gameSessionUserList.getSessionUserCount();
		if (userCount + 1 >= maxPlayerCount){
			return true;
		}
		return false;
	}

	synchronized public boolean isFull() {
		int userCount = gameSessionUserList.getSessionUserCount();
		if (userCount >= maxPlayerCount){
			return true;
		}
		else {
			return false;
		}
	}

	public boolean isDrawGameSession(){
		// this is just for compatibility for online draw app
		return false;
	}
	
	public int calculateDisplayPriority() {
		if (!canAllocate()){
			displayPriority = 1;
			return 1;
		}
		
		int userCount = gameSessionUserList.getSessionUserCount();		
		if (userCount <= 0){
			displayPriority = 0;
			return 0;
		}
		else if (userCount > 0 && userCount <= 3){
			displayPriority = 3;
			return 3;	// highest
		}
		else if (userCount > 3 && userCount < maxPlayerCount){
			displayPriority = 2;
			return 2;	
		}
		else{
			displayPriority = 1;			
			return 1;	// full
		}
	}

	
	public int getAllocationPriority(boolean increase) {
		int userCount = gameSessionUserList.getSessionUserCount();
		if (increase)
			userCount++;
		
		if (userCount == 0){
			return 0;
		}
		else if (userCount > 0 && userCount <= 3){
			return 2;	// highest
		}
		else if (userCount > 3 && userCount < maxPlayerCount){
			return 1;
		}
		else{
			return -1;
		}
	}

	public synchronized String getCurrentPlayUserId() {
		return gameSessionUserList.getCurrentPlayUserIdSafe();
	}

	public synchronized String safeGetCurrentPlayUserId() {
		String id = gameSessionUserList.getCurrentPlayUserIdSafe();
		if (id == null)
			return "";
		else
			return id;
	}
	
	
//	private synchronized int getCurrentPlayUserIndex() {
//		return userList.getCurrentPlayUser;
//	}
	
//	public synchronized void setCurrentPlayUser(String userId, int index) {
//		this.currentPlayUserId = userId;
//		this.currentPlayUserIndex = index;
//		ServerLog.info(sessionId, "set current play user to "+userId);
//	}



	public String getName() {
		return this.name;
	}

	public synchronized void addUserIntoSession(GameUser user) {
		if (user == null) {			
			return;
		}
//		if (userList.getSessionUserCount() == 0){
//			setCurrentPlayUser(user.getUserId(), 0);
//		}
		gameSessionUserList.addUserIntoSession(user);
	}

	@Override
	public String toString() {
		return String.format("[%010d]", sessionId);
	}

	public void selectPlayerUser() {		
		gameSessionUserList.selectCurrentPlayUserByDirection(playDirection);	
	}

	public int getUserIndex(String userId) {
		GameUser user = gameSessionUserList.findGameUserById(userId);
		if (user == null)
			return -1;
		
		return user.seatId;
	}
	
//	private int getCurrentPlayUserIndex(List<GameUser> users,
//			String userId) {
//		
//		if (userId == null)
//			return -1;
//		
//		int index = 0;
//		for (GameUser user : users){
//			if (userId.equals(user.getUserId()))
//				return index;
//			index ++;
//		}
//		
//		return -1;
//	}

	public void startGame(){
		status = SessionStatus.PLAYING;
		gameSessionUserList.setAllUserPlaying();
		ServerLog.info(sessionId, "start game, set status to " + status);
	}
	
	
	public void restartGame(){
		directionChanged = false;
	}
	
	public void finishGame(){
		status = SessionStatus.WAIT;
		ServerLog.info(sessionId, "finish game, set status to " + status);
	}
	
	public void resetGame() {
		status = SessionStatus.INIT;
		gameSessionUserList.clearCurrentPlayUser();
		clearTimer();
		restartGame();
		ServerLog.info(sessionId, "reset game, set status to " + status);
	}

	public void completeTurn() {
		gameSessionUserList.clearAllUserPlaying();
	}

//	public synchronized boolean isCurrentPlayUser(String userId) {
//		if (currentPlayUserId == null)
//			return false;
//		return currentPlayUserId.equals(userId);
//	}

	public int getUserCount() {
		return gameSessionUserList.getSessionUserCount();
	}

	public void takeOverUser(String userId) {
		gameSessionUserList.takeOverUser(userId);
	}

	public GameUser getUser(String userId) {		
		return gameSessionUserList.getUser(userId);
	}
	
	public boolean hasUser(String userId){
		return (gameSessionUserList.getUser(userId) != null);
	}

	public void removeUser(String userId) {
		gameSessionUserList.removeUserFromSession(userId);
	}

	public String getCreateBy() {
		return createBy;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public String getHost() {
		return this.host;
	}

	public int getStatus() {
		return status.ordinal();
	}
	
	public PBGameSession toPBGameSession(GameSessionManager sessionManager) {
		
		PBGameSession.Builder pbGameSessionBuilder = PBGameSession.newBuilder()
			.setSessionId(sessionId)
			.setName(name)
			.setRuleType(ruleType);
		
		pbGameSessionBuilder.setStatus(this.getStatus());
		if ( getUserList() != null ) 
			pbGameSessionBuilder.addAllUsers(this.getUserList().usersToPBUsers());
		if (pbGameSessionBuilder.hasGameId())
			pbGameSessionBuilder.setGameId(sessionManager.getGameId());
		if (createBy != null)
			pbGameSessionBuilder.setCreateBy(this.getCreateBy());
		if (host != null)
			pbGameSessionBuilder.setHost(this.getHost());		
		if (getCurrentPlayUserId() != null)
			pbGameSessionBuilder.setCurrentPlayUserId(this.getCurrentPlayUserId());	
		if (password != null)
			pbGameSessionBuilder.setPassword(password);
		if (createBy != null)
			pbGameSessionBuilder.setCreateBy(createBy);		
		
		PBGameSession pbGameSession = pbGameSessionBuilder.build();		
		return pbGameSession;
	}
	
	public int getPlayUserCount() {
		return gameSessionUserList.getPlayUserCount();
	}

	public List<String> removeTakenOverUser() {
		return gameSessionUserList.removeTakenOverUser();
	}
	
	public void setRobotTimeOutFuture(ScheduledFuture<Object> future) {
		clearRobotTimer();		
		inviteRobotTimer = future;
	}

	public void clearRobotTimer() {
		if (inviteRobotTimer != null){
			inviteRobotTimer.cancel(false);
			inviteRobotTimer = null;
		}		
	}

	public boolean isUserTakenOver(String userId) {
		if (userId == null)
			return false;
			
		return gameSessionUserList.isUserTakenOver(userId);
	}

	public GameUser findUser(String userId) {
		return gameSessionUserList.findGameUserById(userId);
	}
	
	public boolean isAllUserTakenOver() {
		return gameSessionUserList.isAllUserTakenOver();
	}

	public GameUser getCurrentPlayUser() {
		return gameSessionUserList.GetCurrentPlayUserSafe();
	}

	public boolean isCurrentPlayUser(String userId) {
		return gameSessionUserList.isCurrentPlayUser(userId);
	}

	public int getRobotUserCount() {
		return gameSessionUserList.getRobotUserCount();
	}

	public boolean hasPassword() {
		return (password != null && !StringUtil.isEmpty(password));
	}


	public int getPlayDirection() {
		return playDirection;
	}

	public void alternateSetPlayDirection() {
		
		if ( playDirection == GameSessionConstant.CLOCK_WISE ) 
			playDirection = GameSessionConstant.COUNTER_CLOCK_WISE;
		else if ( playDirection == GameSessionConstant.COUNTER_CLOCK_WISE ) 
			playDirection = GameSessionConstant.CLOCK_WISE;
	}


	public void setDirectionChanged(boolean b) {
		directionChanged = b;
	}
	
	
	// reset the WAIT_CLAIM_TIMEOUT with newInternal(user use decTime item) 
	public int getNewInterval() {
		
		return newInterval;
	}

	// counter-operation of getNewInterval(),
	// if @para value is 0, it means clear it,
	// because the item only influence the next user.
	public void setNewInternal(int value) {
		
		newInterval = value;
	}
	
	public int getRemainTime(Object timerType) {
		if ( shareTimerFuture != null ) {
			return (int)shareTimerFuture.getDelay(TimeUnit.SECONDS);
		} else {
			return -1;
		}
	}

	
	public Collection<PBUserResult> getUserResults(){
		return userResults.values();
	}
	
	public void addUserResult(String userId, int gainCoins, boolean isWon){
		
		if (userId == null){
			ServerLog.warn(sessionId, "<addUserResult> but userId is null");
			return;
		}
		
		PBUserResult result = PBUserResult.newBuilder()
				.setWin(isWon)
				.setUserId(userId)
				.setGainCoins(gainCoins)
				.build();
	
		userResults.put(userId, result);		
	}
	

	public void setRuleType(int ruleType) {
		this.ruleType = ruleType;
	}

	public int getRuleType() {
		return ruleType;
	}

	public String peekNextPlayerId() {
		return gameSessionUserList.peekNextPlayerId(playDirection);
	}

	@Override
	public int compareTo(GameSession ps) {
		return (ps.displayPriority - this.displayPriority); // arrange in descending order by priority
	}

	public void setDisplayPriority(int newPriority) {
		this.displayPriority = newPriority;		
	}

	
	public void setActualStartGame() {
		status = SessionStatus.ACTUAL_PLAYING;
	}

	public void resetUserZombieTimeOut(String userId) {
		GameUser user = findUser(userId);		
		if (user != null){
			ServerLog.info(sessionId, "<resetUserZombieTimeOut> user="+user.toString());
			user.resetZombieTimeOut();
		}		
	}
	
	public void incUserZombieTimeOut(String userId) {
		GameUser user = findUser(userId);		
		if (user != null){
			user.incZombieTimeOut();
			ServerLog.info(sessionId, "<incUserZombieTimeOut> times="+user.zombieTimeOutTimes + ", user="+user.toString());
		}		
	}
	
	public List<String> getZombieUserIdList() {
		return getUserList().getZombieUserIdList();

	}
	
	public boolean isCreatedByUser() {
		return  sessionId >= GameSessionManager.USER_CREATED_SESSION_INDEX;
	}

	public boolean canAllocate() {
		// default it's true, if you want to detect some condition , you can override it
		return true;
	}
}
