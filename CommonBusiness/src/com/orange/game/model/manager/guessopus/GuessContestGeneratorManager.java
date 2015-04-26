package com.orange.game.model.manager.guessopus;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.orange.common.redis.RedisCallable;
import com.orange.common.redis.RedisClient;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.dao.opus.UserGuess;
import com.orange.game.model.manager.word.WordManager;
import com.orange.game.model.service.DBService;
import com.orange.game.model.xiaoji.XiaojiFactory;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Linruin
 * Date: 13-8-5
 * Time: 下午5:15
 * To change this template use File | Settings | File Templates.
 */
public class GuessContestGeneratorManager {

    protected static Logger log = Logger.getLogger(GuessContestGeneratorManager.class.getName());
    private static GuessContestGeneratorManager ourInstance = new GuessContestGeneratorManager();
//    private final String sourceDrawPoolKey;

    public static GuessContestGeneratorManager getInstance() {
        return ourInstance;
    }

    private GuessContestGeneratorManager() {
//        sourceDrawPoolKey = UserGuessOpusPlayManager.getContestOpusForGuessTableName(XiaojiFactory.getInstance().getDraw().getCategoryName());
    }

    private String getContestTableName(String contestId, String category){
        String poolKey = UserGuessOpusPlayManager.getContestOpusForGuessTableName(category);
         return poolKey + "_" + contestId;
    }

    public void insertContestPool(String opusId, String category){
        String poolKey = UserGuessOpusPlayManager.getContestOpusForGuessTableName(category);
        RedisClient.getInstance().sadd(poolKey, opusId);
    }

    public void clearDrawContestPool(String category){
        String poolKey = UserGuessOpusPlayManager.getContestOpusForGuessTableName(category);
        RedisClient.getInstance().del(poolKey);
    }

    public List<String> getRandomOpusList(String contestId, int randomCount, String category){

        log.info("<getRandomOpusList> contestId = " + contestId + ", randomCount = " + randomCount);

        Set<String> opusIdSet = getOpusList(contestId, category);
        if (opusIdSet == null){
            log.warn("<getRandomOpusList> no opus for random");
            return Collections.emptyList();
        }

        int count = opusIdSet.size();
        List<String> opusIdList = new ArrayList<String>();
        opusIdList.addAll(opusIdSet);

        if (randomCount >= opusIdSet.size()){
            log.warn("<getRandomOpusList> not enough opus for random, size = " + opusIdSet.size());
            return opusIdList;
        }

        List<String> retOpusIdList = new ArrayList<String>();

        int temp = count;

        for(int i = 0; i < randomCount; i ++){
            int index = (int)(Math.random() * temp) % temp;
            String value = opusIdList.get(index);
            String lastValue = opusIdList.get(temp - 1);
            opusIdList.set(index, lastValue);
            opusIdList.set(temp-1,value);
            temp --;
            // add value to return
            retOpusIdList.add(value);
        }

        log.info("<getRandomOpusList> return opus list="+retOpusIdList.toString());

        return retOpusIdList;
    }

    private synchronized Set<String> getOpusList(final String contestId, final String category){

        final String contestKey = getContestTableName(contestId, category);
        Set<String> retSet = (Set<String>)RedisClient.getInstance().execute(new RedisCallable<Set<String>>() {
            @Override
            public Set<String> call(Jedis jedis) {
                Set<String> opusIdSet = jedis.smembers(contestKey);


                 if (opusIdSet !=null && opusIdSet.size() >= UserGuessConstants.TOTAL_OPUS_NUMBER_IN_CONTEST){

                     log.info("<GuessContestGenerator> contest opus list exist, return directly, " +
                             "contestId = " + contestId + ", list size = " + opusIdSet.size());
                     return opusIdSet;
                 }

                // load data from source pool and remove the data from source pool
                opusIdSet = new HashSet<String>();
                int count = UserGuessConstants.TOTAL_OPUS_NUMBER_IN_CONTEST -  opusIdSet.size();
                String poolKey = UserGuessOpusPlayManager.getContestOpusForGuessTableName(category);
                for (int i=0; i<count; i++){
                    String opusId = jedis.spop(poolKey);
                    if (!StringUtil.isEmpty(opusId)){
                        opusIdSet.add(opusId);
                    }
                    else{
                        break;
                    }
                }

                log.info("<GuessContestGenerator> create contest opus list = "+opusIdSet.toString());

                // add into contest table
                if (opusIdSet.size() > 0){
                    String[] opusIds = new String[opusIdSet.size()];
                    opusIdSet.toArray(opusIds);
                    jedis.sadd(contestKey, opusIds);
                }

                return opusIdSet;
            }
        });

        return retSet;
    }

    public void generateContestOpusPoolForDraw(int limit){

        float minScore = 500f;
//        int limit = 1000;
        int count = 0;
        int insertCount = 0;

        BasicDBObject query = new BasicDBObject();

        BasicDBList inList = new BasicDBList();
        inList.add(UserAction.TYPE_DRAW);
        inList.add(UserAction.TYPE_DRAW_TO_USER);

        query.put(DBConstants.F_TYPE, new BasicDBObject("$in", inList));
        query.put(DBConstants.F_HISTORY_SCORE, new BasicDBObject("$gte", minScore));

        log.info("<generateContestOpusPoolForDraw> qurey = "+ query);
        DBCursor cursor = DBService.getInstance().getMongoDBClient().find(DBConstants.T_OPUS, query, null, 0, limit);
        if (cursor != null) {
            while (cursor.hasNext()) {
                // only fetch one record
                BasicDBObject dbObject = (BasicDBObject) cursor.next();

                count ++;

                if (count % 100 == 0){
                    log.info("<generateContestOpusPoolForDraw> scan for " + count + " opus " + "insert " + insertCount + " opus");
                }

                UserAction action = new UserAction();
                action.setDbObject(dbObject);

                if (action.getOpusStatus() == UserAction.STATUS_DELETE){
                    continue;
                }

                if (action.getOpusImageUrl() == null
                        || action.getOpusImageUrl().length() == 0
                        || StringUtil.isEmpty(action.getOpusThumbImageUrl())){
                    continue;
                }

                String opusId = action.getActionId();
                String word = action.getWord();
                if (!WordManager.getInstance().isValidWord(word)){
                    log.info("<generateContestOpusPoolForDraw> opus word " + word +" is not in dictionary, skip");
                     continue;
                }

                insertContestPool(opusId, XiaojiFactory.getInstance().getDraw().getCategoryName());
                insertCount ++;
            }
            cursor.close();
        }

        log.info("<generateContestOpusPoolForDraw> scan for " + count + " opus " + "insert " + insertCount + " opus");
    }



    public void generateHappyOpusPoolForDraw(int limit){

//        int limit = 1000;
        int count = 0;
        int insertCount = 0;

        BasicDBObject query = new BasicDBObject();

        BasicDBList inList = new BasicDBList();
        inList.add(UserAction.TYPE_DRAW);
        inList.add(UserAction.TYPE_DRAW_TO_USER);

        query.put(DBConstants.F_TYPE, new BasicDBObject("$in", inList));

        log.info("<generateHappyOpusPoolForDraw> qurey = "+ query);
        DBCursor cursor = DBService.getInstance().getMongoDBClient().find(DBConstants.T_OPUS, query, null, 0, limit);
        if (cursor != null) {
            while (cursor.hasNext()) {
                // only fetch one record
                BasicDBObject dbObject = (BasicDBObject) cursor.next();

                count ++;

                if (count % 100 == 0){
                    log.info("<generateHappyOpusPoolForDraw> scan for " + count + " opus " + "insert " + insertCount + " opus");
                }

                UserAction action = new UserAction();
                action.setDbObject(dbObject);

                if (action.getOpusStatus() == UserAction.STATUS_DELETE){
                    continue;
                }

                if (action.getOpusImageUrl() == null || action.getOpusImageUrl().length() == 0){
                    continue;
                }

                String opusId = action.getActionId();
                String userId = action.getCreateUserId();
                XiaojiFactory.getInstance().getDraw().userGuessOpusHappyManager.createRedisIndex(opusId, userId, action);

                insertCount ++;
            }
            cursor.close();
        }

        log.info("<generateHappyOpusPoolForDraw> scan for " + count + " opus " + "insert " + insertCount + " opus");
    }


    public void generateGeniusOpusPoolForDraw(int limit){

        float minScore = 200f;
//        int limit = 1000;
        int count = 0;
        int insertCount = 0;

        BasicDBObject query = new BasicDBObject();

        BasicDBList inList = new BasicDBList();
        inList.add(UserAction.TYPE_DRAW);
        inList.add(UserAction.TYPE_DRAW_TO_USER);

        query.put(DBConstants.F_TYPE, new BasicDBObject("$in", inList));
        query.put(DBConstants.F_HISTORY_SCORE, new BasicDBObject("$gte", minScore));

        log.info("<generateGeniusOpusPoolForDraw> qurey = "+ query);
        DBCursor cursor = DBService.getInstance().getMongoDBClient().find(DBConstants.T_OPUS, query, null, 0, limit);
        if (cursor != null) {
            while (cursor.hasNext()) {
                // only fetch one record
                BasicDBObject dbObject = (BasicDBObject) cursor.next();

                count ++;

                if (count % 100 == 0){
                    log.info("<generateGeniusOpusPoolForDraw> scan for " + count + " opus " + "insert " + insertCount + " opus");
                }

                UserAction action = new UserAction();
                action.setDbObject(dbObject);

                if (action.getOpusStatus() == UserAction.STATUS_DELETE){
                    continue;
                }

                if (action.getOpusImageUrl() == null || action.getOpusImageUrl().length() == 0 || StringUtil.isEmpty(action.getOpusThumbImageUrl())){
                    continue;
                }

                String opusId = action.getActionId();
                String word = action.getWord();
                if (!WordManager.getInstance().isValidWord(word)){
                    log.info("<generateGeniusOpusPoolForDraw> opus word " + word +" is not in dictionary, skip");
                    continue;
                }

                String userId = action.getCreateUserId();
                XiaojiFactory.getInstance().getDraw().userGuessOpusGeniusManager.createRedisIndex(opusId, userId, action);

                insertCount ++;
            }
            cursor.close();
        }

        log.info("<generateGeniusOpusPoolForDraw> scan for " + count + " opus " + "insert " + insertCount + " opus");
    }

    public void clearContestOpusPoolForDraw() {
        clearDrawContestPool(XiaojiFactory.getInstance().getDraw().getCategoryName());
    }

    public void clearGeniusOpusPoolForDraw() {
        XiaojiFactory.getInstance().getDraw().userGuessOpusGeniusManager.clearOpusForGuess();
    }

    public void clearHappyOpusPoolForDraw() {
        XiaojiFactory.getInstance().getDraw().userGuessOpusHappyManager.clearOpusForGuess();
    }
}


