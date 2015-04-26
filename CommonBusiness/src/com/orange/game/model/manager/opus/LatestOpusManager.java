package com.orange.game.model.manager.opus;

import java.util.List;

import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.LatestUserDataManager;
import com.orange.game.model.dao.UserAction;

public class LatestOpusManager extends LatestUserDataManager<UserAction> {


	public static final String REDIS_LIST_KEY_PREFIX = "lastest_user_opus_list_";
	public static final String REDIS_MAP_KEY_PREFIX = "lastest_user_opus_map_";
	public static final String MONGO_DB_TABLE = DBConstants.T_OPUS;
	
	public LatestOpusManager(String category, String subCategory) {		
		super(REDIS_LIST_KEY_PREFIX + category + "_" + subCategory, 
				REDIS_MAP_KEY_PREFIX + category + "_" + subCategory, 
				MONGO_DB_TABLE, UserAction.class);
	}

	public LatestOpusManager(String category) {		
		super(REDIS_LIST_KEY_PREFIX + category, 
				REDIS_MAP_KEY_PREFIX + category, 
				MONGO_DB_TABLE, UserAction.class);
	}	
	
	public List<UserAction> getList(final int offset, final int limit){
		return getList(offset, limit, DBConstants.F_OPUS_STATUS, UserAction.STATUS_DELETE, OpusUtils.NORMAL_RETURN_FIELDS);
	}
	
}
