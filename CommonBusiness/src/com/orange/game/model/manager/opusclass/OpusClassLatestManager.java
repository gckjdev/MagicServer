package com.orange.game.model.manager.opusclass;

import com.mongodb.BasicDBObject;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonZSetIndexManager;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.opus.OpusUtils;
import com.orange.game.model.service.DBService;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 14-6-4
 * Time: 下午5:37
 * To change this template use File | Settings | File Templates.
 */
public class OpusClassLatestManager  extends CommonZSetIndexManager<UserAction> {

    private static final String REDIS_PREFIX = "opus_class_latest_";
    private static final String MONGO_TABLE_NAME = DBConstants.T_OPUS;
    private static final int HOT_TOP_COUNT = NO_LIMIT;
    private static final String MONGO_INDEX_PREFIX = "opus_class_";
    private final String mongodbIndex;

    public OpusClassLatestManager(String className) {
        super(REDIS_PREFIX+className.toLowerCase(), MONGO_TABLE_NAME, HOT_TOP_COUNT, UserAction.class);
        mongodbIndex = MONGO_INDEX_PREFIX + className.toLowerCase();
    }

    public void addOpus(final String opusId, final double time){

        this.updateTopScore(opusId, time, null, false, true);

        addOpusIntoMongoDB(opusId, time);
    }

    public void removeOpus(final String opusId){
        this.deleteIndex(opusId, false);

        // remove from mongo db, for backup
        removeOpusFromMongoDB(opusId);
    }

    private void addOpusIntoMongoDB(final String opusId, final double time){
        BasicDBObject query = new BasicDBObject("_id", new ObjectId(opusId));
        BasicDBObject obj = new BasicDBObject("_id", new ObjectId(opusId));
        obj.put(DBConstants.F_CREATE_DATE, time);
        DBService.getInstance().getMongoDBClient().upsertAll(mongodbIndex, query, obj);
    }

    private void removeOpusFromMongoDB(final String opusId){
        DBService.getInstance().getMongoDBClient().removeByObjectId(mongodbIndex, opusId);
    }

    public List<UserAction> getTopList(int offset,int limit){
        return getTopList(offset, limit, DBConstants.F_OPUS_STATUS, UserAction.STATUS_DELETE, OpusUtils.NORMAL_RETURN_FIELDS);
    }

}
