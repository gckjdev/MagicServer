package com.orange.game.model.manager.opus;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.common.redis.RedisClient;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonZSetIndexManager;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.CommonManager;
import com.orange.game.model.manager.useropus.UserStageOpusManager;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 14-7-21
 * Time: 下午9:24
 * To change this template use File | Settings | File Templates.
 */
public class StageTopOpusManager extends CommonZSetIndexManager<UserAction> {

    private static final String REDIS_PREFIX = "stage_top_opus";
    private static final String MONGO_TABLE_NAME = DBConstants.T_OPUS;

    private final String tutorialId;
    private final String stageId;

    public StageTopOpusManager(String tutorialId, String stageId) {
        super(REDIS_PREFIX+"_"+tutorialId.toLowerCase()+"_"+stageId.toLowerCase(),
                MONGO_TABLE_NAME, 0, UserAction.class);

        this.tutorialId = tutorialId;
        this.stageId = stageId;
    }

    public void insertUserScore(final String userId, final double score){

        this.updateTopScore(userId, score, new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return null;
            }
        }, false, true);
    }

    public List<UserAction> getTopList(int offset,int limit){

        List<ObjectId> list = getTopIdList(offset, limit);
        if (list.size() == 0){
            return Collections.emptyList();
        }

        return UserStageOpusManager.getInstance().getOpusByUserIdList(list, tutorialId, stageId);

//        return getTopList(offset, limit, DBConstants.F_OPUS_STATUS, UserAction.STATUS_DELETE, OpusUtils.NORMAL_RETURN_FIELDS);
    }

    public UserAction getUserOpus(String tutorialId, String stageId, String uid) {

        List<ObjectId> list = new ArrayList<ObjectId>();
        list.add(new ObjectId(uid));
        List<UserAction> opusList = UserStageOpusManager.getInstance().getOpusByUserIdList(list, tutorialId, stageId);;

        if (opusList.size() == 0){
            return null;
        }

        // set rank
        int rank = RedisClient.getInstance().zrevrank(getRedisKey(), uid);
        UserAction userAction = opusList.get(0);
        userAction.setStageRank(rank);
        return userAction;
    }
}
