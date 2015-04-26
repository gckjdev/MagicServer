package com.orange.game.traffic.config;

import java.util.List;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.orange.common.log.ServerLog;
import com.orange.common.network.ChannelUserManager;
import com.orange.game.traffic.messagehandler.AbstractMessageHandler;
import com.orange.game.traffic.model.dao.GameSession;
import com.orange.game.traffic.model.dao.GameSessionUserList;
import com.orange.game.traffic.model.dao.GameUser;
import com.orange.game.traffic.model.manager.GameSessionManager;
import com.orange.game.traffic.model.manager.GameUserManager;
import com.orange.game.traffic.server.GameEventExecutor;
import com.orange.game.traffic.server.GameServerHandler;
import com.orange.game.traffic.server.HandlerUtils;
import com.orange.game.traffic.server.GameServerHandler.DisconnectReason;
import com.orange.game.traffic.statemachine.GameEvent;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameResultCode;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;

public class ConfigServerHandler extends SimpleChannelUpstreamHandler {
	private static final Logger logger = Logger.getLogger(ConfigServerHandler.class
			.getName());
	private static final String AUTHENTICATE_SUCCESS = "Authenticate Pass!\n";
	private static final String AUTHENTICATE_FAILURE = "Authenticate Failure!\n";
	private static final String ASK_AUTHENTICATION = "Hey! Welcome, Enter Something Like Alibaba!\n";
	private static final String EXIT_COMMAND1 = "quit";
	private static final String EXIT_COMMAND2 = "exit";
	private static final String GOODBYE = "Bye Bye! See you next time!\n";
	private static final String KICK_USER_COMMAND = "kick";
	private static final String KICK_USER_ONGOING = "Kick User Out of Session Ongoing...\n";
	private static final String KICK_USER_BUT_SESSION_NOT_FOUND = "Cannot kick user due to session not found\n";
	private static final String INCORRECT_PARAMETER_FOR_KICK_USER = "Parameter not enough for kick user command, e.g. KICK <SessionID> <UserID>\n";
	private static final String  ERROR_EXCEPTION = "Catch exception while execute command, disconnect\n"; 
	
	
	
	public enum DisconnectReason {
		EXCEPTION,
		EXCEPTION_HANDLE_UP_STREAM,
		CHANNEL_DISCONNECTED,
		CHANNEL_CLOSED
	};
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		String message = (String)e.getMessage();
		logger.info("RECV CONFIG COMMAND = "+message);
	
		ConfigServer server = ConfigServer.getInstance();
		Channel channel = e.getChannel();
		
		if (!server.isAuthenticate() && server.isAskAuthenticate()){
			if (server.authenticate(message)){
				channel.write(AUTHENTICATE_SUCCESS);
			}
			else{
				channel.write(AUTHENTICATE_FAILURE);
				e.getChannel().disconnect();
				e.getChannel().close();
			}

			return;
		}
		
		if (message.equalsIgnoreCase(EXIT_COMMAND1) || message.equalsIgnoreCase(EXIT_COMMAND2)){
			channel.write(GOODBYE);
			e.getChannel().disconnect();
			e.getChannel().close();	
			return;
		}
		
		if (message.contains(KICK_USER_COMMAND)){
			String[] paraList = message.split(" ");
			if (paraList != null){
				
				GameSessionManager sessionManager = GameEventExecutor.getInstance().getSessionManager();
				GameSession session;
				int cursor;
				if ( paraList[1].equals("-s") ) {
					if ( paraList.length == 2 ) {
						logger.info("Config Server : No session name specified!");
						return;
					} else {
						String sessionName = paraList[2];
						session = sessionManager.findSessionByName(sessionName);
						cursor = 3;
					}
				} else {
					int sessionId = Integer.parseInt(paraList[1]);
					session = sessionManager.findSessionById(sessionId);
					cursor = 2;
				}
				
				if (session != null){
					
					if (paraList.length > cursor){
						String userId = paraList[cursor];
						logger.info("Config Server: Try to kick user "+userId+" out of session "+session.getSessionId());
						sessionManager.userQuitSession(session, userId, true, true);
						channel.write(KICK_USER_ONGOING);
					}
					else if (paraList.length == cursor){
						// kick all users in the session
						List<GameUser> userList = session.getUserList().getAllUsers();
						if (userList == null)
							return;
						
						for (GameUser user : userList){
							String userId = user.getUserId();
							logger.info("Config Server: Try to kick user "+userId+" out of session "+session.getSessionId());
							sessionManager.userQuitSession(session, userId, true, true);
							channel.write(KICK_USER_ONGOING);
						}
					}
				}
				else{
					channel.write(KICK_USER_BUT_SESSION_NOT_FOUND);
				}
			}
			else{
				channel.write(INCORRECT_PARAMETER_FOR_KICK_USER);
			}
		}
		
//		e.getChannel().write("Did you say "+message+" ?\n");		 
	}
	
	@Override
	public void exceptionCaught( ChannelHandlerContext ctx, ExceptionEvent e) {
		logger.error("<exceptionCaught> catch unexpected exception at " + e.getChannel().toString() + ", cause=" + e.getCause().getMessage(), e.getCause());
		e.getChannel().write(ERROR_EXCEPTION);
		e.getChannel().disconnect();
		e.getChannel().close();	
	}				
	
	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
            ChannelStateEvent e){
		e.getChannel().close();
	}
	
	@Override
	public void channelClosed(ChannelHandlerContext ctx,
            ChannelStateEvent e){
		logger.info("Config Server, Close Client at "+ctx.getChannel().toString());
	}
	
	@Override
	public void channelConnected(ChannelHandlerContext ctx,
            ChannelStateEvent e){
		logger.info("Config Server, Connected Client at "+ctx.getChannel().toString());
		
		ConfigServer.getInstance().isAskingAuthenticate = true;
		e.getChannel().write(ASK_AUTHENTICATION);
	}

}
