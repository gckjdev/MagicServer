package com.orange.barrage.service.kafka;

import org.apache.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by pipi on 15/4/28.
 */
public class KafkaChatConsumerManager {

    protected static Logger log = Logger.getLogger(KafkaChatConsumerManager.class.getName());

    private static KafkaChatConsumerManager ourInstance = new KafkaChatConsumerManager();

    public static KafkaChatConsumerManager getInstance() {
        return ourInstance;
    }

    private KafkaChatConsumerManager() {
    }

    volatile int groupIndex = 0;

    final ConcurrentHashMap<String, KafkaChatConsumer> consumerConnectionMap = new ConcurrentHashMap<String, KafkaChatConsumer>();
    final Object lock = new Object();

    public void addAgent(String agentId, KafkaChatConsumerCallbackInterface callback){
        synchronized (lock){
            if (consumerConnectionMap.containsKey(agentId)){
                return;
            }

            // create new consumer and start it
            int index = groupIndex++;
            KafkaChatConsumer consumer = new KafkaChatConsumer(agentId, index, callback);
            consumerConnectionMap.put(agentId, consumer);
            consumer.start();
        }
    }

    public void removeAgent(String agentId){
        synchronized (lock) {
            if (!consumerConnectionMap.containsKey(agentId)){
                return;
            }

            // shutdown and remove
            KafkaChatConsumer consumer = consumerConnectionMap.get(agentId);
            consumer.shutdown();
            consumerConnectionMap.remove(agentId);
        }
    }



}
