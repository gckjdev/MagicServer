package com.orange.game.model.manager.opus.contest;

import com.orange.common.redis.RedisClient;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-8-6
 * Time: 上午11:30
 * To change this template use File | Settings | File Templates.
 */
public class AllContestOpusManager {
    private static final String REDIS_KEY = "all_contest_opus";
    private static AllContestOpusManager ourInstance = new AllContestOpusManager();


    public static AllContestOpusManager getInstance() {
        return ourInstance;
    }

    private AllContestOpusManager() {
    }

    public boolean isContestOpus(String opusId){
        return RedisClient.getInstance().sismember(REDIS_KEY, opusId);
    }

    public void insert(String opusId, String contestId){
        RedisClient.getInstance().sadd(REDIS_KEY, opusId);
    }
}
