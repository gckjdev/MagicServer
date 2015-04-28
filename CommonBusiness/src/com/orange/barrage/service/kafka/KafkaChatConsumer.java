package com.orange.barrage.service.kafka;

import com.orange.common.kafka.test.KafkaProducer;
import com.orange.common.utils.PropertyUtil;
import com.orange.protocol.message.UserProtos;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.serializer.StringDecoder;
import kafka.utils.VerifiableProperties;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by pipi on 15/4/28.
 */
public class KafkaChatConsumer {

    protected static Logger log = Logger.getLogger(KafkaChatConsumer.class.getName());

    private final ConsumerConnector consumer;
    private static final String ZOOKEEPER_URL = PropertyUtil.getStringProperty("kafka.zookeeper", "localhost:2181");
    private static final String KAFKA_GROUP = PropertyUtil.getStringProperty("kafka.group", "chat-agent-group");

    ExecutorService executor = Executors.newSingleThreadExecutor();
    KafkaChatConsumerCallbackInterface callback = null;

    String agentId = null;
    volatile boolean isShutDown = false;

    public KafkaChatConsumer(String agentId, int groupIndex, KafkaChatConsumerCallbackInterface callback) {
        Properties props = new Properties();

        //zookeeper 配置
        props.put("zookeeper.connect", ZOOKEEPER_URL);

        //group 代表一个消费组
        props.put("group.id", String.format("%s-%s", KAFKA_GROUP, agentId));

        //zk连接超时
        props.put("zookeeper.session.timeout.ms", "4000");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");
        props.put("auto.offset.reset", "smallest");

        //序列化类
        props.put("serializer.class", "com.orange.barrage.service.kafka.KafkaChatDecoder");

        ConsumerConfig config = new ConsumerConfig(props);

        this.agentId = agentId;
        this.callback = callback;

        consumer = kafka.consumer.Consumer.createJavaConsumerConnector(config);
    }

    public void start() {

        executor.execute(new Runnable() {
            @Override
            public void run() {

                String topic = KafkaChatProducer.getInstance().createChatTopic(agentId);

                Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
                topicCountMap.put(topic, new Integer(1));

                StringDecoder keyDecoder = new StringDecoder(new VerifiableProperties());
                KafkaChatDecoder valueDecoder = new KafkaChatDecoder(new VerifiableProperties());

                Map<String, List<KafkaStream<String, UserProtos.PBChat>>> consumerMap = consumer.createMessageStreams(topicCountMap,keyDecoder,valueDecoder);
                KafkaStream<String, UserProtos.PBChat> stream = consumerMap.get(topic).get(0);

                ConsumerIterator<String, UserProtos.PBChat> it = stream.iterator();
                while (!isShutDown && it.hasNext()) {

                    final UserProtos.PBChat pbChat = it.next().message();

                    if (callback != null){
                        callback.handleChatMessage(agentId, pbChat);
                    }

                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        break;
                    }
                }

                consumer.commitOffsets();
                isShutDown = true;
            }
        });
    }

    public void shutdown(){
        isShutDown = true;
        consumer.commitOffsets();
        executor.shutdown();
        consumer.shutdown();
    }
}
