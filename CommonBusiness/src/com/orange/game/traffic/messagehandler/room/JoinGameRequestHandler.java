package com.orange.game.traffic.messagehandler.room;

import java.util.List;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;

import com.orange.common.log.ServerLog;
import com.orange.game.traffic.messagehandler.AbstractMessageHandler;
import com.orange.game.traffic.model.dao.GameSession;
import com.orange.game.traffic.model.dao.GameUser;
import com.orange.game.traffic.model.manager.GameSessionAllocationManager;
import com.orange.game.traffic.model.manager.GameSessionManager;
import com.orange.game.traffic.model.manager.GameUserManager;
import com.orange.game.traffic.server.GameEventExecutor;
import com.orange.game.traffic.server.HandlerUtils;
import com.orange.game.traffic.service.SessionUserService;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameResultCode;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;
import com.orange.network.game.protocol.message.GameMessageProtos.JoinGameRequest;
import com.orange.network.game.protocol.message.GameMessageProtos.JoinGameResponse;
import com.orange.network.game.protocol.model.GameBasicProtos;
import com.orange.network.game.protocol.model.GameBasicProtos.PBGameSession;
import com.orange.network.game.protocol.model.GameBasicProtos.PBGameUser;

public abstract class JoinGameRequestHandler extends AbstractMessageHandler {

	public JoinGameRequestHandler(MessageEvent event) {
		super(event);
	}
	
	protected GameSession processRequest(GameMessage message, Channel channel, GameSession requestSession){
		// init data
		JoinGameRequest request = message.getJoinGameRequest();
		String userId = request.getUserId();
		PBGameUser pbUser = request.getUser();

		// kick user out of session if user alreay exists
		GameUser gameUser = GameUserManager.getInstance().findUserById(userId);
		if ( gameUser != null && gameUser.getCurrentSessionId() > 0 ) {
			ServerLog.info(gameUser.getCurrentSessionId(), "<JoinGameRequestHandler> User " + userId + " has already existed in session " + gameUser.getCurrentSessionId()
					+ ", so kick him/her out");
			GameEventExecutor.getInstance().fireUserOuitEvent(gameUser.getCurrentSessionId(), userId, gameUser.getChannel());
		}
		
		// alloc session if needed
		GameSession session = null;
		if (message.hasSessionId()){
			// has session id, alloc user into the session directly
			session = GameSessionAllocationManager.getInstance().allocSession(userId, (int)message.getSessionId());
		}
		else{
			// no session id, alloc a session from session queue
			session = GameSessionAllocationManager.getInstance().allocSession(userId);
		}
		if (session == null){
			HandlerUtils.sendErrorResponse(message, GameResultCode.ERROR_NO_SESSION_AVAILABLE, channel);
			return null;
		}

		// add user into session
		GameUser user = SessionUserService.getInstance().addUserIntoSession(session, pbUser, channel, message);		
		
		// send response
		sendResponseForUserJoin(session, user.getPBUser(), channel, message);

		// update user game status
		sessionManager.updateUserEnterGameStatus(session, user);
		
		return session;
	}

	@Override
	public void handleRequest(GameMessage message, Channel channel, GameSession requestSession) {
		processRequest(message, channel, requestSession);		
	}
	
	

	private JoinGameResponse.Builder responseCommonPart(GameSession session, PBGameUser pbUser, Channel channel, GameMessage requestMessage) {
		
		List<PBGameUser> pbGameUserList = session.getUserList().usersToPBUsers();
		PBGameSession.Builder builder = GameBasicProtos.PBGameSession.newBuilder()		
										.setGameId(sessionManager.getGameId())
										.setName(session.getName())
										.setSessionId(session.getSessionId())
										.setRuleType(session.getRuleType())
										.addAllUsers(pbGameUserList)
										.setStatus(session.getStatus());

		if (session.getCurrentPlayUserId() != null)
			builder.setCurrentPlayUserId(session.getCurrentPlayUserId());

		PBGameSession gameSessionData = builder.build();		
		
		JoinGameResponse.Builder joinGameResponseBuilder = GameMessageProtos.JoinGameResponse.newBuilder()
										.setGameSession(gameSessionData);
	
		return joinGameResponseBuilder;
	}
	
	
	public abstract JoinGameResponse responseSpecificPart(JoinGameResponse.Builder builder, GameSession session);
	
	protected void sendResponseForUserJoin(GameSession session, PBGameUser pbUser, Channel channel, GameMessage requestMessage)
	{
		GameMessage message = null;
		
		// make common response section
		JoinGameResponse.Builder builder = responseCommonPart(session, pbUser, channel, requestMessage);
		// now do the subclass-specific job to fullfill the response
		JoinGameResponse response = responseSpecificPart(builder,session);
		// make the message to be sent
		GameMessage.Builder messageBuilder = GameMessage.newBuilder()
						.setJoinGameResponse(response)
						.setCommand(GameCommandType.JOIN_GAME_RESPONSE)
						.setMessageId(requestMessage.getMessageId())
						.setResultCode(GameResultCode.SUCCESS);
		
		if ( response != null ) {
			message = messageBuilder.setJoinGameResponse(response)
									.build();
		} else {
			message = messageBuilder.build();
		}
		
		HandlerUtils.sendMessage(message, channel);
	}

	@Override
	public boolean isProcessIgnoreSession() {
		return true;
	}

	@Override
	public boolean isProcessInStateMachine() {
		return false;
	}

	@Override
	public boolean isProcessForSessionAllocation() {
		return true;
	}

}
