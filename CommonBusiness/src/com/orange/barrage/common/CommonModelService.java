package com.orange.barrage.common;

import com.orange.common.cassandra.CassandraClient;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.redis.RedisClient;
import com.orange.game.model.service.DBService;
import org.apache.log4j.Logger;

/**
 * Created by pipi on 14/12/2.
 */
public class CommonModelService {

    public static MongoDBClient mongoDBClient = DBService.getInstance().getMongoDBClient();
    public static RedisClient redisClient = RedisClient.getInstance();
    public static CassandraClient cassandraClient = CassandraClient.getInstance();

    public static final Logger log = Logger.getLogger(CommonModelService.class.getName());


}
