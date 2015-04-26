package com.orange.game.model.manager.opus;

import com.orange.common.redis.RedisClient;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonZSetIndexManager;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.xiaoji.AbstractXiaoji;

import java.util.ArrayList;
import java.util.List;

public class OpusForMatchManager extends CommonZSetIndexManager<UserAction> {

    private static final String REDIS_PREFIX = "opus_for_match_";
    private static final String MONGO_FIELD_PREFIX = "";
    private static final String MONGO_TABLE_NAME = DBConstants.T_OPUS;
    private static final int HOT_TOP_COUNT = 100000;
    private static final String REDIS_KEY_USER_GUESS_OPUS = "opus_for_match_userguessedopus";

    public OpusForMatchManager(String category) {
        super(REDIS_PREFIX+category.toLowerCase(), MONGO_TABLE_NAME, HOT_TOP_COUNT, UserAction.class);
    }

    private double buildOpusMatchScore(int time){
        int reverseScore = 1999999999;
        return ((long)reverseScore << 32) + ((long)time);
    }

    private double getMinusScore(int minus){
        return ((long)minus << 32);
    }


    static final int MATCH_SCORE = 1;
    static final int GUESS_SCORE = 3;

    public void updateOpusForMatchScore(final String opusId, final boolean firstInsert, final boolean isMatched, final boolean isGuessed){

        double score = 0;

        if (firstInsert){
            score = buildOpusMatchScore((int)(System.currentTimeMillis()/1000));
            RedisClient.getInstance().zadd(getRedisKey(), score, opusId);
        }
        else if (isMatched){
            double minus = getMinusScore(MATCH_SCORE);
            RedisClient.getInstance().zinc(getRedisKey(), -minus, opusId);
        }
        else if (isGuessed){
            double minus = getMinusScore(GUESS_SCORE);
            RedisClient.getInstance().zinc(getRedisKey(), -minus, opusId);
        }
    }


    public List<UserAction> getTopList(int offset,int limit){
        return getTopList(offset, limit, null, 0, OpusUtils.NORMAL_RETURN_FIELDS);
    }

    public UserAction matchOne(String userId, AbstractXiaoji xiaoji) {

        int offset = 0;
        int LIMIT = 10;
        List<UserAction> list = null;

        List<UserAction> deletedList = new ArrayList<UserAction>();

        do{
            list = getTopList(offset, LIMIT);
            if (list == null || list.size() == 0){
                return null;
            }

            boolean matched = false;
            for (UserAction userAction : list){

                if (userAction.isDeleted()){
                    deletedList.add(userAction);
                    continue;
                }

                if (userId.equalsIgnoreCase(userAction.getCreateUserId())){
                    // dont't guess user's own opus
                    continue;
                }

                if (isUserGuessOpus(userId, userAction.getActionId())){
                    // don't guess the one user already guessed
                    continue;
                }

                // clean deleted list
                for (UserAction action : deletedList) {
                    deleteIndex(action.getActionId(), true);
                }

                return userAction;
            }

            offset += LIMIT;

        }while(true);


    }

    public void addUserGuessOpus(String uid, String opusId) {

        if (StringUtil.isEmpty(uid) || StringUtil.isEmpty(opusId)){
            log.warn("<addUserGuessOpus> but uid "+uid+" or opusId "+opusId +" is null");
            return;
        }

        RedisClient.getInstance().sadd(REDIS_KEY_USER_GUESS_OPUS, uid+opusId);
    }

    public boolean isUserGuessOpus(String uid, String opusId) {
        return RedisClient.getInstance().sismember(REDIS_KEY_USER_GUESS_OPUS, uid+opusId);
    }

}