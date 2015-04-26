package com.orange.game.model.manager.xiaojinumber;

import org.apache.log4j.Logger;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Transaction;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-7-22
 * Time: 下午4:45
 * To change this template use File | Settings | File Templates.
 */
public class UsedPoolManager {

    public static final Logger log = Logger.getLogger(UsedPoolManager.class
            .getName());

    private static UsedPoolManager ourInstance = new UsedPoolManager();

    public static UsedPoolManager getInstance() {
        return ourInstance;
    }

    private static final String REDIS_KEY = "number_used";

    private UsedPoolManager() {
    }

    public void addNumber(Pipeline p, String number) {
        log.info("add number "+number+" into number used pool "+REDIS_KEY);
        p.sadd(REDIS_KEY, number);
    }

    public void isNumberUsed(Transaction transaction, String number) {
        // TODO
    }

    public void removeNumber(Pipeline p, String number) {
        log.info("remove number "+number+" from number used pool "+REDIS_KEY);
        p.srem(REDIS_KEY, number);
    }
}
