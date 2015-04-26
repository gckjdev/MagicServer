package com.orange.game.model.manager.opus;

import java.util.List;
import java.util.concurrent.Callable;



import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonZSetIndexManager;
import com.orange.game.model.dao.UserAction;

public class HotTopOpusManager extends CommonZSetIndexManager<UserAction> {

	private static final String REDIS_PREFIX = "hot_top_opus_";
	private static final String MONGO_FIELD_PREFIX = "";
	private static final String MONGO_TABLE_NAME = DBConstants.T_OPUS;
    private static final int HOT_TOP_COUNT = 20000;
	
	public HotTopOpusManager(String category) {
		super(REDIS_PREFIX+category.toLowerCase(), MONGO_TABLE_NAME, HOT_TOP_COUNT, UserAction.class);
	}

	public void updateOpusHotTopScore(final String id, final double score){
		
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
}
