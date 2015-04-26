package com.orange.game.model.manager.user;
import com.orange.common.redis.RedisCallable;
import com.orange.common.redis.RedisClient;
import com.orange.common.utils.DateUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonZSetIndexManager;
import com.orange.game.model.dao.User;
import com.orange.game.model.manager.UserManager;
import org.apache.log4j.Logger;
import com.orange.common.utils.StringUtil;
import com.orange.common.scheduler.ScheduleService;
import java.util.List;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.Tuple;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-10-15
 * Time: 上午10:52
 * To change this template use File | Settings | File Templates.
 */
public class NewStarUserManager extends CommonZSetIndexManager<User> {


    private static final int FLOWER_WEIGHT  = 1;
    private static final int FAN_WEIGHT     = 5;

    static Logger log = Logger.getLogger(NewStarUserManager.class.getName());


    public final static int MAX_TOP_COUNT = Integer.MAX_VALUE;			// only cache 3000 top users
    final static String NEW_STAR_DATE_POOL = "new_star_date_";
    final static String NEW_STAR_SCORE_POOL= "new_star_score_";
    final String newStarDateRedisKey;
    private final static int STAY_SECONDS = 3600 * 24 * 30;
    private RedisClient client =  RedisClient.getInstance();


    private void scheduleRemoveInvalidateUsers()
    {
        ScheduleService.getInstance().scheduleEveryday(3, 0, 0, new Runnable() {
            @Override
            public void run() {
                double timestamp = getMinTimestamp();
                removeInvalidateUsersFromNewStarScorePool(timestamp);
                removeInvalidateUsersFromNewStarPool(timestamp);
            }
        });
    }

    public NewStarUserManager(String category) {
        super(NEW_STAR_SCORE_POOL + category, DBConstants.T_USER, MAX_TOP_COUNT, User.class);
        newStarDateRedisKey = NEW_STAR_DATE_POOL + category;
        scheduleRemoveInvalidateUsers();
    }


    private boolean isNewUserInPool(String userId){
        if(StringUtil.isEmpty(userId))
           return false; 
       return client.zismember(newStarDateRedisKey, userId);
    }

    public void updateNewStarScore(String userId, int addFlowerCount, int addFanCount){
        if(userId != null && isNewUserInPool(userId)){
            double score = addFlowerCount * FLOWER_WEIGHT + addFanCount * FAN_WEIGHT;
            incTopScore(userId, score, null, false);
        }
    }

    public List<User> getTopList(int offset, int limit){
        return getTopList(offset, limit, null, 0, UserManager.getUserPublicReturnFields());
    }

    //call when user register
    public void addUserIntoPools(String userId){
        long timestamp = DateUtil.getCurrentTime()/1000;
        client.zadd(newStarDateRedisKey, timestamp, userId);
        client.zadd(getRedisKey(), 0, userId);
    }


    private double getMinTimestamp(){
        return (DateUtil.getCurrentTime() / 1000) - STAY_SECONDS;
    }

    //call by a timer, should called before removeInvalidateUsersFromNewStarPool
    public void removeInvalidateUsersFromNewStarScorePool(double timestamp){

       log.info("<removeInvalidateUsersFromNewStarScorePool> timestamp = " + timestamp);

       final Set<String> set = client.zbelowScore(newStarDateRedisKey, timestamp);
       if(set.size() != 0){

           client.execute(new RedisCallable() {
               @Override
               public Object call(Jedis jedis) {
                   Pipeline p = jedis.pipelined();
//                   p.multi();
                   for(String userId:set){
                       p.zrem(getRedisKey(),userId);
                   }
//                   p.exec();
                   p.syncAndReturnAll();
                   return  null;
               }
           });

       }
    }

    //call by a timer
    public void removeInvalidateUsersFromNewStarPool(double timestamp){
        log.info("<removeInvalidateUsersFromNewStarPool> timestamp = " + timestamp);
        client.zremRangeByScore(newStarDateRedisKey, 0, timestamp);
    }

}
