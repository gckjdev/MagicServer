package com.orange.game.model.manager.opus;

import com.mongodb.BasicDBObject;
import com.orange.common.redis.RedisClient;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.CommonManager;
import com.orange.game.model.manager.useropus.UserStageOpusManager;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 14-7-22
 * Time: 下午2:28
 * To change this template use File | Settings | File Templates.
 */
public class StageOpusManager extends CommonManager {
    private static StageOpusManager ourInstance = new StageOpusManager();

    public static StageOpusManager getInstance() {
        return ourInstance;
    }

    private StageOpusManager() {
    }

    public BasicDBObject insertOpus(String userId, String tutorialId, String stageId, int stageIndex, String opusId, int score){

        if (StringUtil.isEmpty(userId) ||
            StringUtil.isEmpty(tutorialId) ||
            StringUtil.isEmpty(stageId) ||
            StringUtil.isEmpty(opusId)){
            return null;
        }

        BasicDBObject retObject = new BasicDBObject();

        // insert into stage top rank zset in redis
        StageTopOpusManager topOpusManager = new StageTopOpusManager(tutorialId, stageId);
        int bestScoreInRedis = (int)RedisClient.getInstance().zscore(topOpusManager.getRedisKey(), userId);

        // get rank
        int totalCount = RedisClient.getInstance().ztopcount(topOpusManager.getRedisKey());

        // get total defeat user count
        int totalDefeat = RedisClient.getInstance().zcountbelow(topOpusManager.getRedisKey(), score);

        retObject.put(DBConstants.F_TOTAL_COUNT, totalCount);
        retObject.put(DBConstants.F_TOTAL_DEFEAT, totalDefeat);

        boolean needToUpdateRedis = false;
        if (bestScoreInRedis < score){
            needToUpdateRedis = true;
        }

        boolean needToUpdateBestScoreInMongo = false;
        // check mongo DB
        BasicDBObject obj = UserStageOpusManager.getInstance().getObject(userId, null);
        BasicDBObject best = null;
        if (obj != null){

            BasicDBObject tutorial = (BasicDBObject)obj.get(tutorialId);
            if (tutorial != null ){
                best = (BasicDBObject)tutorial.get(stageId);
            }

            if (best != null){
                int currentBestScore = best.getInt(DBConstants.F_BEST_SCORE);
                String currentBestOpusId = best.getString(DBConstants.F_BEST_OPUS_ID);

                if (!StringUtil.isEmpty(currentBestOpusId) &&
                        currentBestScore < score &&
                        opusId.equalsIgnoreCase(currentBestOpusId) == false){

                    log.info("user stage opus, current best score less than new one, need to update mongo db");
                    needToUpdateBestScoreInMongo = true;
                }
            }
            else{
                // no best record found
                log.info("user stage opus, no best score found, need to update mongo db");
                needToUpdateBestScoreInMongo = true;
            }
        }

        // update latest opus info in stage and best score if needed
        BasicDBObject updateValue = new BasicDBObject();
        BasicDBObject scoreValue = new BasicDBObject();
        if (best != null){
            scoreValue.putAll(best.toMap());    // put old data firstly
        }
        scoreValue.put(DBConstants.F_LATEST_SCORE, score);
        scoreValue.put(DBConstants.F_LATEST_OPUS_ID, opusId);
        scoreValue.put(DBConstants.F_LATEST_CREATE_DATE, new Date());
        if (needToUpdateBestScoreInMongo){
            scoreValue.put(DBConstants.F_BEST_SCORE, score);
            scoreValue.put(DBConstants.F_BEST_OPUS_ID, opusId);
            scoreValue.put(DBConstants.F_BEST_CREATE_DATE, new Date());
        }
        updateValue.put(getBestKey(tutorialId, stageId), scoreValue);
        retObject.putAll(scoreValue.toMap()); // return best opus and last opus id here
        log.info("user stage opus, update mongo db, obj="+updateValue.toString());
        UserStageOpusManager.getInstance().updateObject(userId, updateValue);

        // update best score in redis if needed
        if (needToUpdateRedis){
            log.info("user stage opus, need to update redis, userId="+userId+", score="+score);
            topOpusManager.insertUserScore(userId, score);
        }

        log.info("stage opus return info="+retObject.toString());
        return retObject;
    }

    private String getBestKey(String tutorialId, String stageId) {
        return tutorialId + "." + stageId;
    }
}
