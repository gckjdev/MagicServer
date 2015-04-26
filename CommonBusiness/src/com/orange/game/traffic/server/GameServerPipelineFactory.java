package com.orange.game.traffic.server;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.HashedWheelTimer;

import com.orange.network.game.protocol.message.GameMessageProtos;

public class GameServerPipelineFactory implements ChannelPipelineFactory {

	final ChannelHandler handler;
	
	private final int READ_IDLE_TIME_SECONDS = 180;
	private static final int WRITE_IDLE_TIME_SECONDS = 0;
	private static final int ALL_IDLE_TIME_SECONDS = 0;
	protected final HashedWheelTimer timer = new HashedWheelTimer();	
	
	public GameServerPipelineFactory(ChannelHandler handler){
		this.handler = handler;
	}
	
	protected int getReadIdleTimeSeconds() { 
			return READ_IDLE_TIME_SECONDS;
	} 
	
	protected int getWriteIdleTimeSeconds() {
		    return WRITE_IDLE_TIME_SECONDS;
	}
	
	protected int getAllIdleTimeSeconds() {
			return ALL_IDLE_TIME_SECONDS;
	}
	
	public ChannelPipeline getPipeline() throws Exception {
	
		ChannelPipeline p = Channels.pipeline();
		p.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4));
		p.addLast("protobufDecoder", new ProtobufDecoder(GameMessageProtos.GameMessage.getDefaultInstance()));
		 
		p.addLast("frameEncoder", new LengthFieldPrepender(4));
		p.addLast("protobufEncoder", new ProtobufEncoder());		 

		/** For idle state handler
		 * 
		 * idleStateHandler handles an idle event(read idle, write idle, or both),
		 * and triggers an idleStateEvent,
		 * which is then handled by idleStateChannelHandler, we can do any 
		 * cleanup there .
		 */
		p.addLast("idleStateHandler", new IdleStateHandler(timer,
				READ_IDLE_TIME_SECONDS, WRITE_IDLE_TIME_SECONDS,
				ALL_IDLE_TIME_SECONDS));
		p.addLast("idleStateChannelHandler",new IdleStateChannelHandler());
		
		p.addLast("handler", handler);
		return p;	
	}

}