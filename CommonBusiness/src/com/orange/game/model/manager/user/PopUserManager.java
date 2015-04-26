package com.orange.game.model.manager.user;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.redis.RedisCallable;
import com.orange.common.redis.RedisClient;
import com.orange.common.scheduler.ScheduleService;
import com.orange.common.utils.DateUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonZSetIndexManager;
import com.orange.game.model.dao.User;
import com.orange.game.model.manager.UserManager;
import org.apache.log4j.Logger;
import com.orange.common.utils.StringUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.Tuple;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-10-15
 * Time: 上午10:52
 * To change this template use File | Settings | File Templates.
 */
public class PopUserManager extends CommonZSetIndexManager<User> {


    static Logger log = Logger.getLogger(PopUserManager.class.getName());
    RedisClient client = RedisClient.getInstance();

    public final static int MAX_TOP_COUNT = Integer.MAX_VALUE;			// only cache 3000 top users
    final static String FIELD_PREFIX_STRING = "pop_";
    final  private static int TOP_N = 1000;
    final  private static double SCORE_DIV = 5;


    final private static double DRAW_MAX_SCORES = 20;
    final private static double TEXT_MAX_SCORES = 10;
    final private static double TEXT_BASE_SCORES = 1;

    final private static double DRAW_WEIGHT = 0.0001;  // 1 score 10000 byte.
    final private static double TEXT_WEIGHT = 0.05;  // 1 score 20 words.

    final private static double OPUS_FLOWER_WEIGHT = 0.1; // 1 score 10 flowers.
    final private static double OPUS_GUESS_WEIGHT = 0.2;  // 1 score 5 guesses

    final private static double BBS_POST_IMAGE_WEIGHT = 1;     // 1 score an image
    final private static double BBS_COMMENT_IMAGE_WEIGHT = BBS_POST_IMAGE_WEIGHT;


    public PopUserManager(String category) {
        super(FIELD_PREFIX_STRING + category, DBConstants.T_USER, MAX_TOP_COUNT, User.class);
        //schedule to switch redis data.

        ScheduleService.getInstance().scheduleEveryday(0,0,1, new Runnable() {
            @Override
            public void run() {
                if (DateUtil.isTodayWeekDay(Calendar.MONDAY)){
                    switchRedisData();
                }
            }
        });

    }


    public void createOpus(String userId, double dataLength){
        double score = calDrawScore(dataLength);
        incPopScore(userId, score);
    }

    public  void commentOpus(String userId, String comment){
        double score = calTextScore(comment);
        incPopScore(userId, score);
    }

    public  void sendFlower(String userId){
        double score = 1 * OPUS_FLOWER_WEIGHT;
        incPopScore(userId, score);
    }

    public  void guessOpus(String userId){
        double score = 1 * OPUS_GUESS_WEIGHT;
        incPopScore(userId, score);
    }

    public  void createPost(String userId, String text, boolean hasImage, double dataLength){
        double score = calTextScore(text);
        score += calDrawScore(dataLength);
        if (hasImage){
            score += BBS_POST_IMAGE_WEIGHT;
        }
        incPopScore(userId, score);
    }

    public void commentPost(String userId, String text, boolean hasImage, double dataLength){
        double score = calTextScore(text);
        score += calDrawScore(dataLength);
        if (hasImage){
            score += BBS_COMMENT_IMAGE_WEIGHT;
        }
        incPopScore(userId, score);
    }

    private double calTextScore(String text){
        if (text != null){
            double score = TEXT_BASE_SCORES + text.length() * TEXT_WEIGHT;
            score = Math.min(score, TEXT_MAX_SCORES);
            return score;
        }
        return 0;
    }

    private double calDrawScore(double dataLength){
        double score = dataLength * DRAW_WEIGHT;
        score = Math.min(score, DRAW_MAX_SCORES);
        return score;
    }


    private void switchRedisData(){
        log.info("<switchRedisData> start to switch pop user rank in redis");
        client.zdeletebelowtop(getRedisKey(), TOP_N);

        client.execute(new RedisCallable() {
            @Override
            public Object call(Jedis jedis) {

                final Set<Tuple> tuples = jedis.zrangeByScoreWithScores(getRedisKey(), 0, Double.MAX_VALUE);

                if (tuples == null){
                    return null;
                }

                Pipeline p = jedis.pipelined();
//                p.multi();
                for (Tuple tuple : tuples){
                    String member = tuple.getElement();
                    double score = tuple.getScore()/SCORE_DIV;
                    p.zadd(getRedisKey(),score,member);
                }
//                p.exec();
                p.syncAndReturnAll();
                return null;
            }
        });
        log.info("<switchRedisData> start to switch pop user rank in redis");
    }

    private void incPopScore(String userId, double incScore){
        log.info("<incPopScore> userId = "+userId+", incScore = "+incScore);
        client.zinc(getRedisKey(), incScore, userId);
    }

    public List<User> getTopList(int offset, int limit){
        return getTopList(offset, limit, null, 0, UserManager.getUserPublicReturnFields());
    }
}
