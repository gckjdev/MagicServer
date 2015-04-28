package com.orange.barrage.service.kafka;

import com.orange.barrage.model.chat.Agent;
import com.orange.protocol.message.UserProtos;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.apache.log4j.Logger;

import java.util.Properties;

/**
 * Created by pipi on 15/4/28.
 */
public class KafkaChatProducer {

    protected static Logger log = Logger.getLogger(KafkaChatProducer.class.getName());

    private static KafkaChatProducer ourInstance = new KafkaChatProducer();

    public static KafkaChatProducer getInstance() {
        return ourInstance;
    }

    private final Producer<String, UserProtos.PBChat> producer;

    private KafkaChatProducer() {

        Properties props = new Properties();

        //此处配置的是kafka的端口
        props.put("metadata.broker.list", "localhost:9092");

        //配置value的序列化类
        props.put("serializer.class", "kafka.serializer.KafkaChatEncoder");

        //配置key的序列化类
        props.put("key.serializer.class", "kafka.serializer.StringEncoder");

        //request.required.acks
        //0, which means that the producer never waits for an acknowledgement from the broker (the same behavior as 0.7). This option provides the lowest latency but the weakest durability guarantees (some data will be lost when a server fails).
        //1, which means that the producer gets an acknowledgement after the leader replica has received the data. This option provides better durability as the client waits until the server acknowledges the request as successful (only messages that were written to the now-dead leader but not yet replicated will be lost).
        //-1, which means that the producer gets an acknowledgement after all in-sync replicas have received the data. This option provides the best durability, we guarantee that no messages will be lost as long as at least one in sync replica remains.
        props.put("request.required.acks","1");

        producer = new Producer<String, UserProtos.PBChat>(new ProducerConfig(props));
    }

    public void sendChat(UserProtos.PBChat pbChat, Agent agent){

        // auto create topic configuration, check here http://kafka.apache.org/08/configuration.html

        String topic = createChatTopic(agent.getAgentId());
        String key = pbChat.getChatId();
        producer.send(new KeyedMessage<String, UserProtos.PBChat>(topic, key ,pbChat));
    }

    public final static String KEY_CHAT_TO_AGENT = "ChatToAgent";

    public String createChatTopic(String agentId) {
        return String.format("%s-%s", KEY_CHAT_TO_AGENT, agentId);
    }
}
