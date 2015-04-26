package com.orange.game.model.manager.guessopus;

import com.mongodb.BasicDBObject;
import com.orange.common.redis.RedisCallable;
import com.orange.common.redis.RedisClient;
import com.orange.common.scheduler.ScheduleService;
import com.orange.common.utils.PropertyUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.Bulletin;
import com.orange.game.model.dao.User;
import com.orange.game.model.dao.opus.UserGuessAchievement;
import com.orange.game.model.manager.BulletinManager;
import com.orange.game.model.manager.MessageManager;
import com.orange.game.model.manager.UserManager;
import com.orange.game.model.service.DBService;
import com.orange.game.model.xiaoji.XiaojiFactory;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: Linruin
 * Date: 13-8-6
 * Time: 下午4:10
 * To change this template use File | Settings | File Templates.
 */
public class AwardManager {

    private static final int CONTEST_AWARD_HOUR = UserGuessConstants.CONTEST_END_HOUR;
    private static final int CONTEST_AWARD_MINUTE = 0;
    private static final int CONTEST_AWARD_SECOND = 60;

    protected static Logger log = Logger.getLogger(AwardManager.class.getName());
//    final private ScheduledExecutorService scheduleService = Executors.newScheduledThreadPool(1);

    private static AwardManager ourInstance = new AwardManager();

    public static AwardManager getInstance() {
        return ourInstance;
    }

    private AwardManager() {

        if (isEnableAwardTimer()){
            ScheduleService.getInstance().scheduleEveryday(CONTEST_AWARD_HOUR, CONTEST_AWARD_MINUTE, CONTEST_AWARD_SECOND,
                    new Runnable() {

                        @Override
                        public void run() {
                            String contestId = UserGuessUtil.getTodayContestId(XiaojiFactory.getInstance().getDraw().getCategoryName());
                            calculateAndInsertContestAward(contestId);
                        }
                    });
        }
    }

    private boolean isEnableAwardTimer() {
        int value = PropertyUtil.getIntProperty("guess.award_timer", 1);
        log.info("guess award timer set to "+value);
        return (value != 0);
    }

    public void calculateAndInsertContestAward(final String contestId){


        final String redisContestKey = TopUserGuessManager.getContestRedisTableName(contestId);

        final String mongoDbContestKey = UserGuessOpusPlayManager.getContestTableName(contestId);

        log.info("<calculateAndInsertContestAward> contestId = " + contestId
                + ", redisContestKey = " + redisContestKey
                + ", mongoDbContestKey = " + mongoDbContestKey);


        RedisClient.getInstance().execute(new RedisCallable<Void>() {
            @Override
            public Void call(Jedis jedis) {

                Long totalCount = jedis.zcard(redisContestKey);

                long awardUserCount = (long) (totalCount.longValue() * UserGuessConstants.CONTEST_AWARD_RATE);

                if (awardUserCount < UserGuessConstants.MIN_AWARD_USER_COUNT){
                    awardUserCount = Math.min(UserGuessConstants.MIN_AWARD_USER_COUNT, totalCount.longValue());
                }

//                log.info("<calculateAndInsertContestAward> totalCount = " + totalCount.longValue()
//                        + ", awardUserCount = " + awardUserCount);

//                Set<String> userIdSet = jedis.zrange(redisContestKey, 0, awardUserCount);
                Set<String> userIdSet = jedis.zrevrange(redisContestKey, 0, awardUserCount);

                if (userIdSet ==null){
                    log.info("<GetContestRankUserList> get contest user rank is null");
                    return null;
                }

                int i = 1;
                int award = 0;
                int MESSAGE_SENT_TO_TOP_COUNT = 20; // 消息发给前20名
                int BULLETIN_SENT_TO_TOP_COUNT = 0; // 公告包含前3名

                String bulletinMessage = "今日小吉猜画比赛，获得前"+BULLETIN_SENT_TO_TOP_COUNT+"名的依次是用户";

                if (BULLETIN_SENT_TO_TOP_COUNT == 1){
                    bulletinMessage = "今日小吉猜画比赛，第一名获得者是";
                }

                String firstUserId = "";
                for (String userId : userIdSet){

                    if (i == 1){
                        firstUserId = userId;
                    }

                    award = getAwardWithRank(i, totalCount.longValue());

//                    log.info("<calculateAndInsertContestAward> No."+ i + " is user<" + userId + ">, award is " + award);

                    if (award > 0){
                        UserManager.chargeAccount(DBService.getInstance().getMongoDBClient(),
                                userId,
                                award,
                                DBConstants.C_CHARGE_SOURCE_GUESS_CONTEST_AWARD,
                                UserManager.BALANCE_TYPE_COINS);

                        if (i <= MESSAGE_SENT_TO_TOP_COUNT){
                            String message = "恭喜你参加了今天的猜画比赛获得第"+i+"名"+",获得了"+ award+"金币奖励"+",欢迎明天再来参加哟!";
                            MessageManager.sendSystemMessage(DBService.getInstance().getMongoDBClient(), userId, message, DBConstants.APPID_DRAW, false);
                        }

                    }

                    if (i<=BULLETIN_SENT_TO_TOP_COUNT){
                        User user = UserManager.findPublicUserInfoByUserId(userId);
                        if (user != null){
                            bulletinMessage = bulletinMessage + user.getNickName();
                        }
                        if (i == BULLETIN_SENT_TO_TOP_COUNT){
                            bulletinMessage = bulletinMessage + "，真是超级厉害的疯狂猜画玩家啊，赶快关注她/他啦";
                            BulletinManager.createBulletin(bulletinMessage, DBConstants.GAME_ID_DRAW, Bulletin.JumpTypeGame, Bulletin.FUNC_USER_DETAIL, firstUserId);
                        }
                        else{
                            bulletinMessage = bulletinMessage + "，";
                        }
                    }

//                    log.info("<calculateAndInsertContestAward> update mongoDb "+ mongoDbContestKey + " userId<" + userId + ">, award " + award);
                    updateContestAward(mongoDbContestKey, userId, award);

                    TopUserGuessManager manager = XiaojiFactory.getInstance().getDraw().getContestTopGuessManager(contestId);

//                    UserGuessAchievement achievement = model.getAchievement(userId);
//                    log.info("<calculateAndInsertContestAward> get achievement "+ achievement.toString());

                    manager.updateArchievement(userId, DBConstants.F_EARN, award);

//                    log.info("<calculateAndInsertContestAward> update achievement "+ achievement.toString());


                    i ++;
                }

                return null;
            }
        });

        return;
    }

    public int getContestRank(String contestId, final String userId){

        final String redisContestKey = TopUserGuessManager.getContestRedisTableName(contestId);

        Integer rank = (Integer) RedisClient.getInstance().execute(new RedisCallable<Integer>() {
            @Override
            public Integer call(Jedis jedis) {
                Long rank = jedis.zrank(redisContestKey, userId);
                return rank.intValue();
            }
        });

        return rank.intValue() + 1;
    }

    public int getTotalUserCount(String contestId){

        final String redisContestKey = TopUserGuessManager.getContestRedisTableName(contestId);

        Integer count = (Integer) RedisClient.getInstance().execute(new RedisCallable<Integer>() {
            @Override
            public Integer call(Jedis jedis) {
                Long count = jedis.scard(redisContestKey);
                return count.intValue();
            }
        });

        return count.intValue();
    }

    private void updateContestAward(String mongoDbTableName, String userId, int award){

        BasicDBObject query = new BasicDBObject();
        query.put(DBConstants.F_OWNER, new ObjectId(userId));

        BasicDBObject updateValue = new BasicDBObject();
        updateValue.put(DBConstants.F_AWARD_COIN, award);
        updateValue.put(DBConstants.F_IS_AWARD, true);
        updateValue.put(DBConstants.F_AWARD_DATE, new Date());

        BasicDBObject update = new BasicDBObject();
        update.put("$set", updateValue);

        DBService.getInstance().getMongoDBClient().updateAll(mongoDbTableName, query, update);

    }

    public int getAwardWithRank(int rank, long totalCount){

        long totalAward = totalCount * (UserGuessConstants.CONTEST_COST_COIN
                - UserGuessConstants.CONTEST_OPUS_AUTHOR_AWARD
                - UserGuessConstants.CONTEST_SYSTEM_COST);

        long awardUserCount = (long) (totalCount * UserGuessConstants.CONTEST_AWARD_RATE);

        int award = 0;
        if (rank == 1){
            award = (int)(totalAward * UserGuessConstants.CONTEST_NUMBER_ONE_AWARD_RATE);
            if (award <= 0){
                log.info("<GetContestAwardWithRank> WARRING: user (rank 1) get 0 award");
            }
            return award;
        }else if (rank >= 2 && rank <= 4){
            award = (int)(totalAward * UserGuessConstants.CONTEST_NUMBER_TWO_AWARD_RATE) / 3;
            if (award <= 0){
                log.info("<GetContestAwardWithRank> WARRING: user (rank 2~4) get 0 award");
            }
            return award;
        }else if (rank >= 5 && rank <= 20){
            award = (int)(totalAward * UserGuessConstants.CONTEST_NUMBER_THREE_AWARD_RATE) / 15;
            if (award <= 0){
                log.warn("<GetContestAwardWithRank> WARRING: user (rank 5~20) get "+award+" award");
                award = 0;
            }
            return award;
        }
//        }else if (rank >= 5 && rank <= 10){
//            award = (int)(totalAward * UserGuessConstants.CONTEST_NUMBER_THREE_AWARD_RATE) / 6;
//            if (award <= 0){
//                log.info("<GetContestAwardWithRank> WARRING: user (rank 5~10) get 0 award");
//            }
//            return award;
//        }else if (rank >= 11 && rank <= awardUserCount){
//            award = (int)((totalAward * UserGuessConstants.CONTEST_NUMBER_FOUR_AWARD_RATE) / (awardUserCount - 11 + 1));
//            if (award <= 0){
//                log.info("<GetContestAwardWithRank> WARRING: user (rank 15%) get 0 award");
//            }
//            return award;
//        }
        else{
            return 0;
        }
    }
}
