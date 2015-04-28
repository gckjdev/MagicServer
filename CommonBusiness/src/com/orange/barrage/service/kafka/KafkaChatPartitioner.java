package com.orange.barrage.service.kafka;

import kafka.producer.Partitioner;

/**
 * Created by pipi on 15/4/28.
 */
public class KafkaChatPartitioner implements Partitioner {

    @Override
    public int partition(Object o, int numPartitions) {
        return o.hashCode() % numPartitions;
    }

}
