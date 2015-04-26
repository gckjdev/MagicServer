package com.orange.game.traffic.server;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.timeout.IdleState;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;

import com.orange.common.log.ServerLog;
import com.orange.common.network.ChannelUserManager;
import com.orange.game.traffic.model.dao.GameSession;
import com.orange.game.traffic.model.dao.GameUser;
import com.orange.game.traffic.model.manager.GameSessionManager;
import com.orange.game.traffic.model.manager.GameUserManager;


/**
 *  An idle state channel handler for users who idle for too long.
 */
public class IdleStateChannelHandler extends IdleStateAwareChannelHandler {

	 private final ChannelUserManager channelUserManager = ChannelUserManager.getInstance();
	 private final GameUserManager userManager = GameUserManager.getInstance();
	 private final GameSessionManager sessionManager = GameEventExecutor.getInstance().getSessionManager();
	 
	 @Override
	 public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) {
		 
		 if (e.getState() == IdleState.READER_IDLE) {
			 Channel channel = e.getChannel();
			 if (channel == null) {
				 return;
			 }
			 
			 String userId = channelUserManager.findUserInChannel(channel);
			 if ( userId == null) {
				 ServerLog.info(0, "<IdleStateChannelHandler> An idleStateEvent triggerd, but found no user on channel " + channel);
				 channel.close();
				 return;
			 }
			 
			 GameUser user = userManager.findUserById(userId);
			 if ( user == null ) {
				 ServerLog.info(0, "<IdleStateChannelHandler> An idleStateEvent triggerd, but found no user on channel " + channel);
				 channel.close();
				 return;
			 }
			 
			 int sessionId = user.getCurrentSessionId();
			 if ( sessionId == -1 ) {
				 ServerLog.info(0, "<IdleStateChannelHandler> An idleStateEvent triggerd, but found no session for user "+ userId
						 +" on channel " + channel);
				 channel.close();
				 return;
			 }
			 
			 GameSession session = sessionManager.findSessionById(sessionId);
			 if ( session == null ) {
				 ServerLog.info(0, "<IdleStateChannelHandler> An idleStateEvent triggerd, but found no session for user "+ userId
						 +" on channel " + channel);
				 channel.close();
				 return;
			 }
			 
			 ServerLog.info(0, "<IdleStateChannelHandler> An idleStateEvent triggerd, remove user " + user.toString() + " from "
					 + "channel " + channel);
			 sessionManager.userQuitSession(session, userId, true, true);
				 
	     }
	 }	
}
