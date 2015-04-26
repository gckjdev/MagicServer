package com.orange.game.traffic.config;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.CharsetUtil;
import org.jboss.netty.util.HashedWheelTimer;

import com.orange.network.game.protocol.message.GameMessageProtos;

public class ConfigServerPipelineFactory implements ChannelPipelineFactory {

	final ChannelHandler handler;
	
	private static final int READ_IDLE_TIME_SECONDS = 120;
	private static final int WRITE_IDLE_TIME_SECONDS = 120;
	private static final int ALL_IDLE_TIME_SECONDS = 0;

	private static final int MAX_CONFIG_DATA_LEN = 64*1024;
	private final HashedWheelTimer timer = new HashedWheelTimer();	
	
	public ConfigServerPipelineFactory(ChannelHandler handler){
		this.handler = handler;
	}
	
	public ChannelPipeline getPipeline() throws Exception {
	
		ChannelPipeline p = Channels.pipeline();
		p.addLast("frameDecoder", new DelimiterBasedFrameDecoder(MAX_CONFIG_DATA_LEN, Delimiters.lineDelimiter()));
		p.addLast("stringDecoder", new StringDecoder(CharsetUtil.UTF_8));

		 // Encoder
		 p.addLast("stringEncoder", new StringEncoder(CharsetUtil.UTF_8));
		
//		
//		p.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4));
//		p.addLast("protobufDecoder", new ProtobufDecoder(GameMessageProtos.GameMessage.getDefaultInstance()));
//		 
//		p.addLast("frameEncoder", new LengthFieldPrepender(4));
//		p.addLast("protobufEncoder", new ProtobufEncoder());		 
		
		p.addLast("idleStatleHandler", new IdleStateHandler(timer, READ_IDLE_TIME_SECONDS,
				WRITE_IDLE_TIME_SECONDS, ALL_IDLE_TIME_SECONDS));				
		
		p.addLast("handler", handler);
		return p;	
	}

}
