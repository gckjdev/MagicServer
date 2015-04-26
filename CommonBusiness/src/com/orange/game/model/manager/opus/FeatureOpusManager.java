package com.orange.game.model.manager.opus;

import java.util.List;
import java.util.concurrent.Callable;

import com.mongodb.BasicDBObject;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonZSetIndexManager;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.OpusManager;
import com.orange.game.model.manager.opusclass.OpusClassService;

public class FeatureOpusManager extends CommonZSetIndexManager<UserAction> {

	private static final String REDIS_PREFIX = "feature_opus_";
	private static final String MONGO_FIELD_PREFIX = "";
	private static final String MONGO_TABLE_NAME = DBConstants.T_OPUS;
	
	public FeatureOpusManager(String category) {
		super(REDIS_PREFIX+category.toLowerCase(), MONGO_TABLE_NAME, UserAction.class);
	}

	public void featureOpus(final String userId, final String opusId){
		
		this.updateTopScore(opusId, System.currentTimeMillis(), new Callable<Object>() {
			@Override
			public Object call() throws Exception {
            return null;
			}
		}, false, true);
        BasicDBObject obj = new BasicDBObject(DBConstants.F_FEATURE, true);
        OpusManager.updateOpus(opusId, obj);
    }
	
	public void unfeatureOpus(final String userId, final String opusId){
		this.deleteIndex(opusId, false);

        BasicDBObject obj = new BasicDBObject(DBConstants.F_FEATURE, false);
        OpusManager.updateOpus(opusId, obj);
    }
	
	public List<UserAction> getList(int offset,int limit){
		return getTopList(offset, limit, DBConstants.F_OPUS_STATUS, UserAction.STATUS_DELETE, OpusUtils.NORMAL_RETURN_FIELDS);
	}
}
