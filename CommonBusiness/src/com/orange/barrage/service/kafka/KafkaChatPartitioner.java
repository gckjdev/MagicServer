package com.orange.barrage.service.kafka;

import com.orange.protocol.message.UserProtos;
import kafka.producer.Partitioner;

/**
 * Created by pipi on 15/4/28.
 */
public class KafkaChatPartitioner implements Partitioner {

    @Override
    public int partition(Object o, int numPartitions) {
        if (o instanceof UserProtos.PBChat){
            UserProtos.PBChat pbChat = (UserProtos.PBChat)o;
            return pbChat.getFromUserId().hashCode() % numPartitions;
        }
        else {
            return o.hashCode() % numPartitions;
        }
    }

}
