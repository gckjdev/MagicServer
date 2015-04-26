package com.orange.game.model.manager.opus;

import java.util.List;
import java.util.concurrent.Callable;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonZSetIndexManager;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.CommonManager;
import com.orange.game.model.service.DBService;

public class AllTimeTopOpusManager extends CommonZSetIndexManager<UserAction> {

	
	private static final String REDIS_PREFIX = "alltime_top_opus_";
	private static final String MONGO_FIELD_PREFIX = "";
	private static final String MONGO_TABLE_NAME = DBConstants.T_OPUS;
    private static final int ALLTIME_TOP_COUNT = 200000;

	public AllTimeTopOpusManager(String category) {
		super(REDIS_PREFIX+category.toLowerCase(), MONGO_TABLE_NAME, ALLTIME_TOP_COUNT, UserAction.class);
	}

	public void updateOpusHistoryTopScore(final String id, final double score){
		
		this.updateTopScore(id, score, new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				// TODO update score in mongo db
				return null;
			}
		}, false, true);
		
	}
	
	public List<UserAction> getTopList(int offset,int limit){
		return getTopList(offset, limit, DBConstants.F_OPUS_STATUS, UserAction.STATUS_DELETE, OpusUtils.NORMAL_RETURN_FIELDS);
	}

    public static List<UserAction> getTopListFromMongoDB(int offset,int limit){

        BasicDBObject query = new BasicDBObject();
        BasicDBObject sort = new BasicDBObject(DBConstants.F_HISTORY_SCORE, -1);
        DBCursor cursor = DBService.getInstance().getMongoDBClient().find(DBConstants.T_OPUS, query,
                OpusUtils.NORMAL_RETURN_FIELDS, sort, offset, limit);

        List<UserAction> list = CommonManager.getDataListFromCursor(cursor, UserAction.class);
        for (int i=0; i<list.size(); i++){
            UserAction opus = list.get(i);
            if (!opus.isXiaojiDraw()){
                list.remove(opus);
            }
        }

        return list;
    }
}
