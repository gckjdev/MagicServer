package com.orange.game.traffic.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;

import com.orange.common.log.ServerLog;
import com.orange.common.statemachine.StateMachine;
import com.orange.common.utils.RandomUtil;
import com.orange.game.traffic.messagehandler.AbstractMessageHandler;
import com.orange.game.traffic.model.dao.GameSession;
import com.orange.game.traffic.model.manager.GameSessionAllocationManager;
import com.orange.game.traffic.model.manager.GameSessionManager;
import com.orange.game.traffic.robot.client.RobotService;
import com.orange.game.traffic.statemachine.GameEvent;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCompleteReason;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;

public class GameEventExecutor {

	protected static final Logger logger = Logger.getLogger(GameEventExecutor.class.getName());

	final private ScheduledExecutorService scheduleService = Executors.newScheduledThreadPool(5);	
	final private List<ExecutorService> executorList = new ArrayList<ExecutorService>();	
	final private ExecutorService executorForSessionAllocation = Executors.newSingleThreadExecutor();
	final int numberOfExecutor = 20;	
	private AtomicInteger messageIdIndex = new AtomicInteger(0);
	private StateMachine stateMachine;
	private GameSessionManager sessionManager;
	
	// thread-safe singleton implementation
	private static GameEventExecutor defaultService = new GameEventExecutor();
	private GameEventExecutor() {
		initExecutors();
		scheduleInitRobotTimer();
	}

	private int getInitRobotCount() {
		String robot = System.getProperty("config.init_robot");
		if (robot != null && !robot.isEmpty()){
			return Integer.parseInt(robot);
		}
		return 0; // default
	}
	
	
	private void scheduleInitRobotTimer() {
		
		int count = getInitRobotCount();
		if (count == 0)
			return;
		
		Runnable callable = new Runnable() {
			@Override
			public void run() {
				try {										
					int start = 0;
					int end = getInitRobotCount()-1;
					if (end < 0)
						return;
					
					// third parameter: "", not search by key word, fourth parameter: room type is 0, all rooms, 
					// fifth parameter: null, userId(for room type use only) 
					List<Integer> list = GameSessionAllocationManager.getInstance().getSessionList(start, end, "", 0, null);
					if (list == null)
						return;
					
					GameSessionManager manager = GameEventExecutor.getInstance().getSessionManager();
					for (Integer sessionId : list){						
						GameSession session = manager.findSessionById(sessionId.intValue());
						if (session != null && session.getUserCount() == 0 && !session.hasPassword()){
							ServerLog.info(sessionId, "Session has no user, fire robot in the session");
							RobotService.getInstance().startOneRobot(sessionId); 
						}
					}
				} catch (Exception e) {
				}
			}
		};
		
		int INIT_DELAY = 3;
		int OTHER_DELAY = 15;
		scheduleService.scheduleWithFixedDelay(callable, INIT_DELAY, OTHER_DELAY, TimeUnit.SECONDS);		
	}

	public static GameEventExecutor getInstance() {
		return defaultService;
	}

	public void initExecutors() {
		for (int i = 0; i < numberOfExecutor; i++) {
			ExecutorService executorService = Executors.newSingleThreadExecutor();
			executorList.add(executorService);
			logger.info("Create & Start Executor " + i);
		}
	}

	public void dispatchEvent(GameEvent event) {
		int index = hash(event.getTargetSession());
		if (index >= 0 && index < numberOfExecutor){
			Runnable runnable = createRunnable(event);
			executorList.get(index).execute(runnable);
		}				
	}

	public void executeForSessionAllocation(final AbstractMessageHandler handler, final GameMessage message, final Channel channel){
		executorForSessionAllocation.execute(new Runnable(){
			@Override
			public void run() {
				try{
					handler.handleRequest(message, channel, null);
				}
				catch (Exception e){
					ServerLog.error(0, e, "<executeForSessionAllocation> catch exception=");
				}
			}			
		});
	}
	
	
	public void executeForSessionRealease(final int sessionId) {
		executorForSessionAllocation.execute(new Runnable() {
			@Override
			public void run() {
				GameSessionAllocationManager.getInstance().releaseSession(sessionId);
			}
		});
	}
	
	
	private int hash(int targetSession) {
		return (targetSession + 31) % numberOfExecutor;
	}

	public int generateMessageId() {
		return messageIdIndex.incrementAndGet();
	}

	private Runnable createRunnable(final GameEvent event) {
		
		Runnable runnable = new Runnable(){
				
				@Override
				public void run()  {
				
					try {				
						GameSession session = sessionManager.findSessionById(event.getTargetSession());								
						if (session == null){
							ServerLog.warn(event.getTargetSession(), "process event "+event.getKey() +" but session id not found ");
							return;
						}
						
						boolean skipEvent = preHandleEvent(event, session);
						if (skipEvent){
							return;
						}
						
						com.orange.common.statemachine.State nextState = stateMachine.nextState(session.getCurrentState(), event, session);				 
						 if (nextState == null){
							 ServerLog.warn(event.getTargetSession(), "process event "+event.getKey() +" but next state not found");
							 return;
						 }
						 
						 session.setCurrentState(nextState);									
			
					} catch (Exception e) {
						logger.error("catch exception while handle event, exception = "+e.toString(), e);
					}
		
				}
		};				
		
		return runnable;
	}
	
	private boolean preHandleEvent(GameEvent event, GameSession session) {
		AbstractMessageHandler handler = event.getHandler();		
		if (handler == null)
			return false;

		if (handler.isProcessInStateMachine())
			return false;		
		
		try{
			if (session != null){
				// reset user zoombie
				session.resetUserZombieTimeOut(event.getMessage().getUserId());
			}
			
			handler.handleRequest(event.getMessage(), event.getChannel(), session);
		}
		catch (Exception e){
			ServerLog.error(event.getTargetSession(), e);
		}
		return true;
	}	
	
	public void fireAndDispatchEvent(GameCommandType command,
			int sessionId, String userId) {

		fireAndDispatchEvent(command, sessionId, userId, GameEvent.MEDIUM);				
	}
	
	public void fireAndDispatchEventHead(GameCommandType command,
			int sessionId, String userId) {

		fireAndDispatchEvent(command, sessionId, userId, GameEvent.HIGH);	
	}

	public void fireAndDispatchEvent(GameCommandType command,
			int sessionId, String userId, int priority) {
		
		ServerLog.info(sessionId, "fire event " + command + ", userId = " + userId);
		
		String userIdForMessage = userId;
		if (userId == null){
			userIdForMessage = "";
		}
		
		GameMessageProtos.GameMessage message = GameMessageProtos.GameMessage.newBuilder()
			.setCommand(command)
			.setSessionId(sessionId)
			.setUserId(userIdForMessage)
			.setMessageId(0)
			.build();
		
		GameEvent event = new GameEvent(command, sessionId, message, null);		
		event.setPriority(priority);
		dispatchEvent(event);
	}

//	private static int EXPIRE_TIME_SECONDS = 60;
//	
//	public void scheduleGameSessionExpireTimer(final GameSession session) {
//		if (session == null)
//			return;
//		
//		Timer timer = new Timer();
//		timer.schedule(new TimerTask(){
//
//			@Override
//			public void run() {
//				ServerLog.info(session.getSessionId(), "expired timer is triggered");
//				fireTurnFinishEvent(session, GameCompleteReason.REASON_EXPIRED);
//			}
//			
//		}, EXPIRE_TIME_SECONDS*1000);
//		
//		ServerLog.info(session.getSessionId(), "schedule expired timer after "+ EXPIRE_TIME_SECONDS);
//		session.setExpireTimer(timer);
//	}

	public void fireTurnFinishEvent(GameSession session, GameCompleteReason reason) {
		
		if (session.isGamePlaying() == false){
			ServerLog.warn(session.getSessionId(), "<fireTurnFinishEvent> but game turn is not in PLAY");
			return;
		}
		
		GameMessageProtos.GameMessage message = GameMessageProtos.GameMessage.newBuilder()
			.setCommand(GameCommandType.LOCAL_GAME_TURN_COMPLETE)
			.setSessionId(session.getSessionId())
			.setMessageId(0)
			.setCompleteReason(reason)
			.build();

		ServerLog.info(session.getSessionId(), "fire LOCAL_GAME_TURN_COMPLETE event due to "+reason);
		GameEvent event = new GameEvent(GameCommandType.LOCAL_GAME_TURN_COMPLETE, 
			session.getSessionId(), message, null);

		dispatchEvent(event);
	}
	

	public void fireUserTimeOutEvent(int sessionId, String userId, final Channel channel) {
		ServerLog.info(sessionId, "fire LOCAL_USER_TIME_OUT event for user "+userId);
		GameMessageProtos.GameMessage message = GameMessageProtos.GameMessage.newBuilder()
			.setCommand(GameCommandType.LOCAL_USER_TIME_OUT)
			.setSessionId(sessionId)
			.setUserId(userId)
			.setMessageId(0)
			.build();

		GameEvent event = new GameEvent(GameCommandType.LOCAL_USER_TIME_OUT, 
			sessionId, message, channel);
	
		dispatchEvent(event);		
	}

 
	
	public void startTimer(final GameSession session, final long timeOutSeconds, final Object timerType) {
		
		session.clearTimer();		

		final int sessionId = session.getSessionId();
		
		Callable<Object> callable = new Callable<Object>(){
			@Override
			public Object call()  {
				ServerLog.info(sessionId, timerType.toString() + " TIMER after "+timeOutSeconds+" fired");
				fireAndDispatchEvent(GameCommandType.LOCAL_TIME_OUT, sessionId, "");
				return null;
			}			
		};
		
		ServerLog.info(sessionId, "set "+ timerType.toString() + " TIMER after "+timeOutSeconds+" seconds");
		session.setTimer(scheduleService.schedule(callable, timeOutSeconds, TimeUnit.SECONDS), timerType);  			
	}

	public void setStateMachine(StateMachine sm) {
		this.stateMachine = sm;
	}

	public void setSessionManager(GameSessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	public GameSessionManager getSessionManager() {
		return sessionManager;
	}


	public final static int ROBOT_TIMEROUT = 5;
	public final static int MIN_USER_COUNT = 1;
	public void prepareRobotTimer(final GameSession session, final RobotService robotService) {
		
		if (session.hasPassword()){
			ServerLog.info(session.getSessionId(), "<prepareRobotTimer> but session has password");
			return;
		}
		
		final int sessionId = session.getSessionId();
		int userCount = session.getUserCount();
		if (userCount != MIN_USER_COUNT){
			return;
		}				
			
		int robotUserCount = session.getRobotUserCount();
		if (robotUserCount >= 1){
			// robot exists
			return;
		}
			
		Callable<Object> callable = new Callable<Object>(){
			@Override
			public Object call()  {
				try{
					int userCount = session.getUserCount();
					if (userCount == MIN_USER_COUNT){
						ServerLog.info(sessionId, "Fire robot timer, start robot now");
						robotService.startNewRobot(sessionId);
					}
					else{
						ServerLog.info(sessionId, "Fire robot timer but user count <> 1");					
					}
				}
				catch(Exception e){
					ServerLog.error(sessionId, e);					
				}

				return null;
			}			
		};
		
		ScheduledFuture<Object> newFuture = scheduleService.schedule(callable, 
				RandomUtil.random(ROBOT_TIMEROUT)+2, TimeUnit.SECONDS);		
		
		ServerLog.info(sessionId, "Only one user, set robot timer");
		session.setRobotTimeOutFuture(newFuture);
	}

	
	public void fireUserOuitEvent(int sessionId, String userId, Channel channel) {
		
		ServerLog.info(sessionId, "fire QUIT GAME event for user "+userId);
		GameMessageProtos.GameMessage message = GameMessageProtos.GameMessage.newBuilder()
			.setCommand(GameCommandType.QUIT_GAME_REQUEST)
			.setSessionId(sessionId)
			.setUserId(userId)
			.setMessageId(0)
			.build();

		GameEvent event = new GameEvent(GameCommandType.QUIT_GAME_REQUEST, 
			sessionId, message, channel);
	
		dispatchEvent(event);		
	}
}
