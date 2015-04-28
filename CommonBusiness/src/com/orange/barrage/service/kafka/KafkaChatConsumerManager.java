package com.orange.barrage.service.kafka;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by pipi on 15/4/28.
 */
public class KafkaChatConsumerManager {
    private static KafkaChatConsumerManager ourInstance = new KafkaChatConsumerManager();

    public static KafkaChatConsumerManager getInstance() {
        return ourInstance;
    }

    private KafkaChatConsumerManager() {
    }

    final ConcurrentHashMap<String, KafkaChatConsumer> consumerConnectionMap = new ConcurrentHashMap<String, KafkaChatConsumer>();
    final Object lock = new Object();

    public void addAgent(String agentId, KafkaChatConsumerCallbackInterface callback){
        synchronized (lock){
            if (consumerConnectionMap.containsKey(agentId)){
                return;
            }

            // create new consumer and start it
            int index = consumerConnectionMap.size();
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
