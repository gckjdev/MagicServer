package com.orange.game.traffic.robot.client;

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

public class RobotClientPipelineFactory implements ChannelPipelineFactory {

	private static final int READ_IDLE_TIME_SECONDS = 60;
	private static final int WRITE_IDLE_TIME_SECONDS = 300;
	private static final int ALL_IDLE_TIME_SECONDS = 0;
	final AbstractRobotClient client;
	private final HashedWheelTimer timer = new HashedWheelTimer();
	
	public RobotClientPipelineFactory(AbstractRobotClient client){
		this.client = client;
	}
	
	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline p = Channels.pipeline();
		p.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4));
		p.addLast("protobufDecoder", new ProtobufDecoder(GameMessageProtos.GameMessage.getDefaultInstance()));
		 
		p.addLast("frameEncoder", new LengthFieldPrepender(4));
		p.addLast("protobufEncoder", new ProtobufEncoder());
		 
//		p.addLast("idleStatleHandler", new IdleStateHandler(timer, READ_IDLE_TIME_SECONDS,
//				WRITE_IDLE_TIME_SECONDS,ALL_IDLE_TIME_SECONDS));
		p.addLast("handler", new RobotClientHandler(client));
		return p;	
	}

}
