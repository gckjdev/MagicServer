package com.orange.game.model.manager.opus.contest;

import java.util.List;
import java.util.concurrent.Callable;

import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonZSetIndexManager;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.opus.OpusUtils;

public class ContestLatestOpusManager extends CommonZSetIndexManager<UserAction> {

	private static final String REDIS_PREFIX = "contest_latest_opus_";
	private static final String MONGO_FIELD_PREFIX = "";
	private static final String MONGO_TABLE_NAME = DBConstants.T_OPUS;
	
	public ContestLatestOpusManager(String contestId) {
		super(REDIS_PREFIX+contestId.toLowerCase(), MONGO_TABLE_NAME, UserAction.class);
	}

	public void updateContestLatestIndex(final String id){		
		this.updateTopScore(id, System.currentTimeMillis(), null, false, true);		
	}
	
	public void  updateContestLatestIndex(final String id,int createDate) {
		this.updateTopScore(id, createDate, null, false, true);
	}
	
	public List<UserAction> getList(int offset,int limit){
		return getTopList(offset, limit, DBConstants.F_OPUS_STATUS, UserAction.STATUS_DELETE, OpusUtils.NORMAL_RETURN_FIELDS);
	}
}
