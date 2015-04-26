package com.orange.game.model.manager.opus.contest;

import com.orange.common.redis.RedisClient;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.Contest;
import com.orange.game.model.manager.ContestManager;
import com.orange.game.model.service.DBService;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-8-5
 * Time: 下午10:46
 * To change this template use File | Settings | File Templates.
 */
public class ContestFlowerManager {

    private static final int MAX_FLOWER_PER_CONTEST = 20;
    private static final String REDIS_KEY_PREFIX = "contest_user_flower_";
    private static ContestFlowerManager ourInstance = new ContestFlowerManager();

    public static ContestFlowerManager getInstance() {
        return ourInstance;
    }

    private ContestFlowerManager() {
    }

    public int incFlowerTimes(String contestId, String userId) {
        String redisKey = REDIS_KEY_PREFIX + contestId;
        int flowerTimes = RedisClient.getInstance().hinc(redisKey, userId, 1);
        return flowerTimes;
    }

    public boolean isReachMaxFlower(Contest contest, int flowerTimes){

        if (contest == null)
            return false;

        int maxFlowerTimes = contest.getMaxFlowerPerContest();
        if (flowerTimes > maxFlowerTimes){
            return true;
        }
        else{
            return false;
        }
    }

    public int getMaxFlowerTimes(String contestId){
        return MAX_FLOWER_PER_CONTEST;
    }


    public boolean canThrowFlower(String contestId) {
        Contest contest = ContestManager.getContestById(DBService.getInstance().getMongoDBClient(), contestId);
        if (contest == null)
            return false;

        return contest.canThrowFlower();
    }
}
