package com.orange.game.traffic.statemachine;

import org.jboss.netty.channel.Channel;

import com.orange.common.statemachine.Event;
import com.orange.game.traffic.messagehandler.AbstractMessageHandler;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;

public class GameEvent extends Event implements Comparable<GameEvent> {

	public static final int HIGH = 1;
	public static final int MEDIUM = 2;
	public static final int LOW = 3;
	
	int priority = MEDIUM;
	
	public GameEvent(Object key, int sessionId,
			GameMessage message, Channel c) {
		
		super(key);		
		this.targetSessionId = sessionId;
		this.message = message;
		this.channel = c;
		this.handler = null;
	}

	public GameEvent(GameCommandType command, int sessionId,
			GameMessage gameMessage, Channel channel,
			AbstractMessageHandler handler) {
		super(command);		
		this.targetSessionId = sessionId;
		this.message = gameMessage;
		this.channel = channel;
		this.handler = handler;
	}

	final int  targetSessionId;
	final GameMessage message;
	final Channel channel;
	final AbstractMessageHandler handler;
	
	public Channel getChannel() {
		return channel;
	}

//	public void setChannel(Channel channel) {
//		this.channel = channel;
//	}

	public int getTargetSession() {
		return targetSessionId;
	}
//	public void setTargetSession(int id) {
//		this.targetSessionId = id;
//	}
	public GameMessage getMessage() {
		return message;
	}
//	public void setMessage(GameMessage message) {
//		this.message = message;
//	}

	public void setPriority(int p){
		this.priority = p;
	}
	
	@Override
	public int compareTo(GameEvent e) {
		return (this.priority - e.priority);
	}

	public AbstractMessageHandler getHandler() {
		return handler;
	}	
	
}
