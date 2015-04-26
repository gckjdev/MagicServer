package com.orange.game.traffic.robot.client;


import java.util.List;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.orange.common.log.ServerLog;
import com.orange.game.traffic.model.dao.GameSession;
import com.orange.game.traffic.robot.client.AbstractRobotClient.ClientState;
import com.orange.game.traffic.server.GameEventExecutor;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameResultCode;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;
import com.orange.network.game.protocol.message.GameMessageProtos.RoomNotificationRequest;
import com.orange.network.game.protocol.model.GameBasicProtos.PBGameSessionChanged;
import com.orange.network.game.protocol.model.GameBasicProtos.PBGameUser;
import com.orange.network.game.protocol.model.GameBasicProtos.PBUserResult;

public class RobotClientHandler extends SimpleChannelUpstreamHandler {

	private static final Logger logger = Logger.getLogger(RobotClientHandler.class.getName());
	final AbstractRobotClient robotClient;
	
	public RobotClientHandler(AbstractRobotClient client) {
		this.robotClient = client;
	}

	private void handleJoinGameResponse(GameMessage message){

		if (message.getResultCode() != GameResultCode.SUCCESS){
			ServerLog.warn(robotClient.sessionId, "robot JOIN GAME failure, error="+message.getResultCode());
			robotClient.disconnect();
			return;
		}		
		
		// save user data here
		if (message.getJoinGameResponse() == null)
			return;
		
		if (message.getJoinGameResponse().getGameSession() == null)
			return;
						
		robotClient.saveUserList(message.getJoinGameResponse().getGameSession().getUsersList());
		robotClient.currentPlayUserId = message.getJoinGameResponse().getGameSession().getCurrentPlayUserId();
		
		robotClient.checkStart();
	}
			
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {

		GameMessage message = (GameMessage) e.getMessage();
		
		switch (message.getCommand()){
		
		case JOIN_GAME_RESPONSE:
			handleJoinGameResponse(message);
			break;
									
		case ROOM_NOTIFICATION_REQUEST:			
			handleRoomNotification(message);
			break;
			
		case GAME_OVER_NOTIFICATION_REQUEST:
			handleGameTurnCompleteNotification(message);
			break;
			
		default:
			robotClient.handleMessage(message);
			break;
		}
	}

	private void handleRoomNotification(GameMessage message) {
		RoomNotificationRequest notificationRequest = message.getRoomNotificationRequest();
		if (notificationRequest == null)
			return;
		
		List<PBGameSessionChanged> sessionChangedList = notificationRequest.getSessionsChangedList();
		for (PBGameSessionChanged sessionChanged : sessionChangedList){
			if (sessionChanged.getSessionId() == robotClient.sessionId){
				// update users in the session
				for (PBGameUser user : sessionChanged.getUsersAddedList()){
					robotClient.addUser(user);
				}
				
				for (String userId : sessionChanged.getUserIdsDeletedList()){
					robotClient.removeUserByUserId(userId);
				}				
			}	
		}
		
		Boolean isGameCompleted ;
		GameSession session = GameEventExecutor.getInstance().getSessionManager().findSessionById(robotClient.sessionId);
		if ( session.isGamePlaying() ) 
			isGameCompleted = false;
		else 
			isGameCompleted = true;
		if (robotClient.canQuitNow(isGameCompleted)){
			// no other users, quit robot
			robotClient.disconnect();
		}		
	}

	private void handleGameTurnCompleteNotification(GameMessage message) {
		
		List<PBUserResult>  pbUserResultList = null; 
		boolean robotWinThisRound = false;
		String userId = robotClient.getUserId();
		
		robotClient.setState(ClientState.WAITING);
		
		if (robotClient.canQuitNow(true)){
			ServerLog.info(robotClient.sessionId, "reach min user for session, robot can escape now!");
			robotClient.disconnect();
			return;
		}
		
		robotClient.incPlayGameCount();
		
		if (message.hasGameOverNotificationRequest()) {
			pbUserResultList = message.getGameOverNotificationRequest().getGameResult().getUserResultList();
			for ( PBUserResult result : pbUserResultList) {
				if (result.getWin() && result.getUserId().equals(robotClient.userId)) {
					robotWinThisRound = true;
					break;
				}
			}
		}

		// cache exp and level when robot starts, only sync to db when level changes.
	   if (robotClient.needUpdateLvlAndExp()) {
		    boolean success = robotClient.updateLevelAndExp();
		    if ( success ) {  
		    	logger.info("<RobotClientHandler> Robot["+userId+"]'s level update!");
		    } else {
		    	logger.warn("<RobotClientHandler> Robot["+userId+"]'s level fails to update!");
		    }
	    }
	   
		
		robotClient.resetPlayData(robotWinThisRound);
		robotClient.checkStart();

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		logger.error("<exceptionCaught> on robot client "+robotClient.nickName, e.getCause());
		robotClient.disconnect();
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) {
		
		ServerLog.info(robotClient.sessionId, "<robotClient> channel disonnected "+robotClient.nickName);
		robotClient.disconnect();
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
		ServerLog.info(robotClient.sessionId, "<robotClient> "+robotClient.nickName +" connected to channel.");
		robotClient.setChannel(e.getChannel());		
		robotClient.sendJoinGameRequest();
	}
}
