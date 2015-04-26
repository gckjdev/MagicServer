package com.orange.game.traffic.robot.client;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.orange.common.log.ServerLog;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.User;
import com.orange.game.model.manager.UserManager;
import com.orange.game.traffic.model.dao.GameSession;
import com.orange.game.traffic.model.dao.GameUser;
import com.orange.game.traffic.server.GameEventExecutor;
import com.orange.game.traffic.server.GameServer;
import com.orange.game.traffic.service.GameDBService;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;
import com.orange.network.game.protocol.message.GameMessageProtos.GeneralNotification;
import com.orange.network.game.protocol.message.GameMessageProtos.JoinGameRequest;
import com.orange.network.game.protocol.model.GameBasicProtos.PBGameUser;

public abstract class AbstractRobotClient implements Runnable {

	private final static Logger logger = Logger.getLogger(AbstractRobotClient.class.getName());
//	protected MongoDBClient dbclient;
	
	private static final int MIN_SESSION_USER_COUNT = 3;
	protected final int sessionId;
	protected final String userId;
	protected final String nickName;
	protected final boolean gender;
	protected String userAvatar;
	protected final String location;
	protected final int robotIndex;
	protected volatile boolean isStopping = false;
	protected volatile boolean isDisconnect = false;
	protected final User user ;
	
	// for level-experience reference table
	protected int MAX_LEVEL = 99;
	protected int FIRST_LEVEL_EXP = 60; // 
	protected double EXP_INC_RATE = 1.08;
	protected long[] levExpTable = new long[MAX_LEVEL+1];
	// level info
	protected long oldExp; 
	protected long experience;
	protected int level;
	
	// balance(coins)
	protected int balance;
	
	// parameters to control robot max play game count
	public static int MAX_PLAY_COUNT = 30;
	int playGameCount = 0;
	
	
	// parameters to control robot max time out in room
	int ROBOT_PLAY_TIMEOUT = 45*60;		// 45 minutes
	volatile boolean isTimeOut = false;
	ScheduledFuture<Object> robotTimeOutFuture = null;
	
	public enum ClientState{
		WAITING,
		PICK_WORD,
		PLAYING
	};
	

	
	
	final protected ScheduledExecutorService scheduleService = Executors.newScheduledThreadPool(1); 
	
	// game session running data
	protected ConcurrentHashMap<String, GameUser> userList = new ConcurrentHashMap<String, GameUser>();
	protected ClientState state = ClientState.WAITING;	
	protected String currentPlayUserId = null;
	int round = -1;
		
	// connection information
	ChannelFuture future;
	ClientBootstrap bootstrap;
	Channel channel;

	// message
	AtomicInteger messageId = new AtomicInteger(1);
	
   private PBGameUser.Builder toPBGameCommonPart() {
	   
	   PBGameUser.Builder builder = PBGameUser.newBuilder();
		builder.setUserId(userId);
		builder.setNickName(nickName);
		if (location != null)
			builder.setLocation(location);
		builder.setAvatar(userAvatar);
		builder.setGender(gender);
		
		return builder;
   }
	
	abstract public PBGameUser toPBGameUserSpecificPart(PBGameUser.Builder builder);
	
	// for draw game compatibility
	protected boolean isForDrawGame(){
		return false;
	}
		
	private PBGameUser toPBGameUser() {
		
		PBGameUser.Builder builder = toPBGameCommonPart();
		PBGameUser pbGameUser = toPBGameUserSpecificPart(builder);
		
		return pbGameUser;
		
	}
		
			
	public AbstractRobotClient(User user, int sessionId, int index) {
		this.user = user;
		this.sessionId = sessionId;
		this.robotIndex = index;
		this.location = user.getLocation();
		this.userId = user.getUserId();
		this.gender = user.isMale();
		this.nickName = user.getNickName();
		this.userAvatar = user.getAvatar();
		initLevelExpTable();
	}
	
	
	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public void run(){
        
        String host = "127.0.0.1";
        int port = GameServer.getPort();
        int THREAD_NUM = 1;

        bootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(
                        Executors.newFixedThreadPool(THREAD_NUM),
                        Executors.newFixedThreadPool(THREAD_NUM)));
        
        // Set up the event pipeline factory.
        bootstrap.setPipelineFactory(new RobotClientPipelineFactory(this));

        // Start the connection attempt.
        future = bootstrap.connect(new InetSocketAddress(host, port));
        
        // Wait until the connection is closed or the connection attempt fails.
        future.getChannel().getCloseFuture().awaitUninterruptibly();
        future = null;
        
        // Shut down thread pools to exit.
        bootstrap.releaseExternalResources();		        
        bootstrap = null;        
	}
	
	protected void send(GameMessage msg){
		if (channel != null && channel.isWritable()){
			ServerLog.info(sessionId, "robot "+nickName+ " send "+msg.getCommand());
			channel.write(msg);
		}
	}
	
	public void sendJoinGameRequest() {
		
		JoinGameRequest request = null;
		GameMessage message = null;

		if ( userAvatar == null ) {
			userAvatar = "";
		}
		
		request =  JoinGameRequest.newBuilder()
			.setUserId(userId)
			.setGameId("")
			.setNickName(nickName)
			.setLocation(location)
			.setAvatar(userAvatar)
			.setGender(gender)			
			.setUser(toPBGameUser())
			.setTargetSessionId(sessionId)
			.build();
		
		message = GameMessage.newBuilder()
				.setMessageId(messageId.getAndIncrement())
				.setCommand(GameCommandType.JOIN_GAME_REQUEST)
				.setSessionId(sessionId)
				.setJoinGameRequest(request)
				.build();
		
		send(message);
		
		robotTimeOutFuture = RobotService.getInstance().schedule(new Callable<Object>(){

			@Override
			public Object call() throws Exception {
				isTimeOut = true;
				return null;
			}
			
		}, ROBOT_PLAY_TIMEOUT);
	}

	public synchronized void disconnect() {
		
		if (isDisconnect)
			return;
		
		isDisconnect = true;
		
		ServerLog.info(sessionId, "Robot " + nickName + " Disconnect");
		
		if (robotTimeOutFuture != null && robotTimeOutFuture.isDone() == false){
			robotTimeOutFuture.cancel(true);
			robotTimeOutFuture = null;
		}
		
		try{
			// we should write robot's cached experience and level info back here
			updateLevelAndExp();
			
			this.resetPlayData(false);
			
			if (channel != null){
				if (channel.isConnected()){
					channel.disconnect();
				}	
			}
		}
		catch (Exception e){
			ServerLog.error(sessionId, e);
		}
		finally{
			if (channel != null){
				channel.close();
				channel = null;				
			}
			
			RobotService.getInstance().finishRobot(this);					
		}
		
	}
		
	public void removeUserByUserId(String userIdForRemove) {
		ServerLog.info(sessionId, "Robot "+nickName+" remover user="+userIdForRemove);
		userList.remove(userIdForRemove);
	}

	public void sendQuitGameRequest() {
		disconnect();
	}

	public int getClientIndex() {
		return robotIndex;
	}

	public void stopClient() {
		
		if (isStopping)
			return;
		
		isStopping = true;
		disconnect();
		
		if (future != null){		
			future.cancel();
		}
		
        // Shut down thread pools to exit.
		if (bootstrap != null){
			bootstrap.releaseExternalResources();
		}
	}

	public ClientState getState() {
		return state;
	}

	public void setState(ClientState state) {
		this.state = state;
	}

	public void updateByNotification(GeneralNotification notification) {
		
		if (notification == null){
			return;
		}
		
		if (notification.hasCurrentPlayUserId()){
			this.setCurrentPlayUserId(notification.getCurrentPlayUserId());			
		}
		
		if (notification.hasNewUserId()){
			PBGameUser.Builder builder = PBGameUser.newBuilder();			
			builder.setUserId(notification.getNewUserId());
			builder.setNickName(notification.getNickName());
			builder.setAvatar(notification.getUserAvatar());
			builder.setGender(notification.getUserGender());			
			this.addUser(builder.build());
		}
		
		if (notification.hasQuitUserId()){
			removeUserByUserId(notification.getQuitUserId());
		}
		
	}

	private void setCurrentPlayUserId(String userId) {
		this.currentPlayUserId = userId;
	}

	
	private GameSession getSession() {
		GameSession session = GameEventExecutor.getInstance().getSessionManager().findSessionById(sessionId);
		return session;
	}
	
	public boolean canQuitNow(Boolean isGameCompleted) {
		
		if (isTimeOut)
              return true;

      if (playGameCount >= MAX_PLAY_COUNT)
              return true;

      int userCount = sessionRealUserCount();
      if (userCount == 0)
              return true;

      if (userCount >= MIN_SESSION_USER_COUNT){
             return true;
        }
      else {
              return false;
        }

	}

	public int sessionRealUserCount() {
		Collection<GameUser> list = userList.values();
		int userCount = 0;
		for (GameUser user : list){
			if (!AbstractRobotManager.isRobotUser(user.getUserId())){
				if (user.isTakenOver() == false)
					userCount ++;
			}
		}
		return userCount;
	}

	abstract public void resetPlayData(boolean robotWin);
	

	public void saveUserList(List<PBGameUser> pbUserList) {
		if (pbUserList == null)
			return;
		
		userList.clear();
		for (PBGameUser pbUser : pbUserList){
			GameUser user = new GameUser(pbUser,
					null, sessionId);
			user.setTakenOver(pbUser.getIsTakenOver());
			user.setPlaying(pbUser.getIsPlaying());
			user.setSeatId(pbUser.getSeatId());
			userList.put(pbUser.getUserId(), user);
		}
	}

	public void checkStart() {
		// TODO Auto-generated method stub
		
	}

	public abstract void handleMessage(GameMessage message);


	public void addUser(PBGameUser pbUser) {		
		GameUser user = new GameUser(pbUser, null, sessionId);
		user.setTakenOver(pbUser.getIsTakenOver());
		user.setPlaying(pbUser.getIsPlaying());
		user.setSeatId(pbUser.getSeatId());
		userList.put(user.getUserId(), user);
		ServerLog.info(sessionId, "Robot "+nickName+" add user="+pbUser.getNickName()+", id="+pbUser.getUserId());
	}

	public int getPlayerCount() {
		return userList.size();
	}

	public void incPlayGameCount() {
		playGameCount ++;
	}

	
	protected void initLevelExpTable() {
		long experience = 0;
		long lastLevelUpExp = 0;

		for (int i = 0; i <= MAX_LEVEL; i++) {
			if (i <= 5) {
				lastLevelUpExp = FIRST_LEVEL_EXP * i;
				experience += lastLevelUpExp;
			} else if (i > 90) {
				lastLevelUpExp = lastLevelUpExp * 2;
				experience += lastLevelUpExp;
			} else {
				lastLevelUpExp = (long) (lastLevelUpExp * EXP_INC_RATE);
				experience += lastLevelUpExp;
			}
			levExpTable[i] = experience;
		}
//		for ( int i = 0; i <= MAX_LEVEL; i++) {
//			logger.info("levExpTable["+i+"] = "+levExpTable[i] );
//		}
	}
	
	public int calNewLevel(long experience) {
		
		if ( levExpTable[level+1] <= experience )
			level++;
		
		return level;
	}
	
	
	public abstract String getAppId();
	public abstract String getGameId();
	
//	public MongoDBClient getMongoDBClient() {
//			return dbclient;
//	}
	
	
	public long incExperience() {
			
			long EXP_GAINED_PER_GAME = 10;
			experience += EXP_GAINED_PER_GAME;
			
			return experience;
	}
	
	
	public void chargeBalance() {
		// if robot has less than 15,000 coins, charge it 50,0000 60,000  coins.
		GameDBService.getInstance().executeDBRequest(robotIndex, new Runnable(){
			@Override
			public void run() {
				int coinsToChargeMin = 50000;
				int coinsToChargeMax = 60000;
				int toCharge ;
				
				if ( balance < 15000) {
					toCharge = RandomUtils.nextInt(coinsToChargeMax-coinsToChargeMin) + coinsToChargeMin;
					UserManager.chargeAccount(GameDBService.getInstance().getMongoDBClient(robotIndex), userId, toCharge, DBConstants.C_CHARGE_SOURCE_ROBOT_AUTO, null, null);
					logger.info("<DiceRobotClient>Robot["+nickName+"] gets charged "+toCharge+" coins");
				}					
			}
		});
	}
	
	

   public boolean needUpdateLvlAndExp() {
	   
	   logger.info("<AbstractRobotClient>Robot["+nickName+"] need update level??? oldExp="+oldExp +
	    		", experience=" +experience+", level="+level);
	   boolean result = false;
	   
    	experience = incExperience();
    	int oldLevel = level;
    	int newLevel = calNewLevel(experience);
    	if ( newLevel > oldLevel ) {
    		result = true;
    		level = newLevel;
    		oldExp= experience;
    		logger.info("<AbstractRobotClient>Robot["+nickName+"] level updates!");
    	}
    	
    	return result;
    }

   public String getUserId() {
	   return userId;
    }

   public long getExperince() {
	   return experience;
    }

   public int getLevel() {
	   return level;
   	}
   
   
   
   public boolean updateLevelAndExp(){		   
		 GameDBService.getInstance().executeDBRequest(robotIndex, new Runnable(){
			@Override
			public void run() {
				MongoDBClient dbClient = GameDBService.getInstance().getMongoDBClient(sessionId);
				UserManager.updateLevelAndExp(dbClient, userId, getGameId(), experience, level, true, ServiceConstant.CONST_SYNC_TYPE_UPDATE, 0);  
			}
			 
		 });		 
		 return true;	   
   }
	
}
