package com.orange.game.model.manager.user;

import com.orange.common.redis.RedisClient;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonZSetIndexManager;
import com.orange.game.model.dao.User;
import com.orange.game.model.manager.UserManager;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 14-1-23
 * Time: 下午5:40
 * To change this template use File | Settings | File Templates.
 */
public class VipTopUserManager  extends CommonZSetIndexManager<User> {


    private static final String REDIS_KEY = "vip_top_user";

    private static VipTopUserManager ourInstance = new VipTopUserManager(REDIS_KEY, DBConstants.T_USER, 100000, User.class);

    private VipTopUserManager(String redisKey, String mongoTableName, int topCount, Class<User> returnDataObjectClass) {
        super(redisKey, mongoTableName, topCount, returnDataObjectClass);
    }

    public static VipTopUserManager getInstance() {
        return ourInstance;
    }

    public List<User> getTopList(int offset, int limit){
        return getTopList(offset, limit, null, 0, UserManager.getUserPublicReturnFields());
    }

    public void updateVipUser(String userId, int payDate, int expireDate){
        double score = getScore(payDate, expireDate);
        log.info("<updateVipUser> userId = "+userId+", incScore = "+score);
        updateTopScore(userId, score, null, false, true);
    }

    private double getScore(int payDate, int expireDate) {
        long high = (long)(payDate << 32);
        long low = expireDate;
        return high + low;
    }

}
