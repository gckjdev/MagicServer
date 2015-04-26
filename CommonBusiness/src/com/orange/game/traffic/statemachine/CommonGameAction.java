package com.orange.game.traffic.statemachine;

import java.util.List;

import com.orange.common.log.ServerLog;
import com.orange.common.statemachine.Action;
import com.orange.game.model.dao.User;
import com.orange.game.model.manager.UserManager;
import com.orange.game.traffic.model.dao.GameSession;
import com.orange.game.traffic.model.dao.GameUser;
import com.orange.game.traffic.robot.client.RobotService;
import com.orange.game.traffic.server.GameEventExecutor;
import com.orange.game.traffic.server.NotificationUtils;
import com.orange.game.traffic.service.GameDBService;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;

public class CommonGameAction {


	public static class IncUserZombieTimeOut implements Action {

		@Override
		public void execute(Object context) {
			final GameSession session = (GameSession)context;
			session.incUserZombieTimeOut(session.getCurrentPlayUserId());
		}

	}

	public static class KickZombieUser implements Action {

		@Override
		public void execute(Object context) {
			final GameSession session = (GameSession)context;
			List<String> zombieUserList = session.getZombieUserIdList();
			for (String userId : zombieUserList){
				ServerLog.info(session.getSessionId(), "<KickZombieUser> userId="+userId);
				GameEventExecutor.getInstance().getSessionManager().userQuitSession(session, userId, true, true);
			}
		}

	}

	public static class QueryUserBalance implements Action {

		@Override
		public void execute(Object context) {
			final GameSession session = (GameSession)context;
			GameDBService.getInstance().executeDBRequest(session.getSessionId(), new Runnable(){
				@Override
				public void run() {
					List<String> userIdList = session.getUserList().getAllUserIds();
					List<User> users = UserManager.findUserAccountInfoByUserIdList(GameDBService.getInstance().getMongoDBClient(session.getSessionId()), userIdList);
					for (User user : users){
						session.getUserList().setUserBalance(user.getUserId(), user.getBalance());
					}
				}				
			});
		}

	}

	public static class FinishGame implements Action {

	
		@Override
		public void execute(Object context) {
			GameSession session = (GameSession)context;
			session.finishGame();
		}

	}

		public static class PrepareRobot implements Action {

				@Override
				public void execute(Object context) {
					GameSession session = (GameSession)context;
					GameEventExecutor.getInstance().prepareRobotTimer(session, RobotService.getInstance());
				}

		}	

		public static class ClearRobotTimer implements Action {

			@Override
			public void execute(Object context) {
				GameSession session = (GameSession)context;
				session.clearRobotTimer();
			}

		}

		


	public static class BroadcastPlayUserChange implements Action {

		@Override
		public void execute(Object context) {
			GameSession session = (GameSession)context;
//			GameNotification.broadcastDrawUserChangeNotification(session);
		}

	}

	public static class SelectPlayUserIfNone implements Action {

		@Override
		public void execute(Object context) {
			GameSession session = (GameSession)context;
			if (session.getCurrentPlayUserId() == null){
				session.selectPlayerUser();
			}
		}

	}
	
	enum TimerType{
		USER_WAIT;		
	};
	
	static final int PICK_WORD_TIMEOUT = 60;
	static final int START_GAME_TIMEOUT = 3;			// 36 seconds, 20 for start, 10 for result, 6 for reserved
	static final int USER_WAIT_TIMEOUT = 60*30;		// 30 minutes
	static final int DRAW_GUESS_TIMEOUT = 60;
	
	public static class StartGame implements Action {

		@Override
		public void execute(Object context) {
			GameSession session = (GameSession)context;
			session.startGame();
		}

	}

	public static class CompleteGame implements Action {

		@Override
		public void execute(Object context) {
			GameSession session = (GameSession)context;
			
			session.getUserList().setAllPlayerLoseGameToFalse();
			session.completeTurn();			

			NotificationUtils.broadcastNotification(session, null, GameCommandType.GAME_TURN_COMPLETE_NOTIFICATION_REQUEST);
		}

	}	

	public static class KickPlayUser implements Action {
		@Override
		public void execute(Object context) {
			GameSession session = (GameSession)context;
			GameUser user = session.getCurrentPlayUser();
			if (user != null){
				GameEventExecutor.getInstance().getSessionManager().userQuitSession(session, user.getUserId(), true, true);
			}
		}
	}
	
	public static class ClearTimer implements Action {

		@Override
		public void execute(Object context) {
			GameSession session = (GameSession)context;
			session.clearTimer();
		}

	}

	public static class SetOneUserWaitTimer implements Action {

		@Override
		public void execute(Object context) {
			GameSession session = (GameSession)context;
			GameEventExecutor.getInstance().startTimer(session, 
					USER_WAIT_TIMEOUT, TimerType.USER_WAIT);
		}

	}
	
	public static class CommonTimer implements Action {

		int interval;
		final Object timerType;
		
		public CommonTimer(int interval, Object timerType){
			this.interval = interval;
			this.timerType = timerType;
		}
		
		@Override
		public void execute(Object context) {
			GameSession session = (GameSession)context;
			GameEventExecutor.getInstance().startTimer(session, interval, timerType);
		}
	}
	
	public static class SelectPlayUser implements Action {

		@Override
		public void execute(Object context) {
			GameSession session = (GameSession)context;
			session.selectPlayerUser();
			session.setActualStartGame(); // 只有在开始选择玩家后，才真正开始游戏，之前这一段是个
													// 特殊窗口期，因此需要用这个标志来判断
			session.setDirectionChanged(false); // 
		}

	}

	public static class InitGame implements Action{

		@Override
		public void execute(Object context) {
			GameSession session = (GameSession)context;
			session.resetGame();
		}
		
	}
	
	public static class ClearAllUserPlaying implements Action {

		@Override
		public void execute(Object context) {
			// make all user not playing
			GameSession session = (GameSession)context;
			session.getUserList().clearAllUserPlaying();
		}

	}

	public static class SetAllUserPlaying implements Action {

		@Override
		public void execute(Object context) {
			// make all user not playing
			GameSession session = (GameSession)context;
			session.getUserList().setAllUserPlaying();
		}

	}
	
}
