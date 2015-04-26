package com.orange.game.model.manager.opus.contest;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonMongoIdListManager;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.opus.OpusUtils;
import com.orange.network.game.protocol.model.DrawProtos.PBFeed;

public class MyContestOpusManager extends CommonMongoIdListManager<UserAction>  {

	public static final String USER_OPUS_PREFIX = "contest_user_opus_";
	public static final String OPUS_TABLE_NAME = DBConstants.T_OPUS;
	
	public MyContestOpusManager(String contestId) {
		super(USER_OPUS_PREFIX+contestId.toLowerCase(), OPUS_TABLE_NAME, UserAction.class);
	}

	public void insertIndex(String userId, String opusId){
			this.insertId(userId, opusId, false, false);
		}	
	
	
	public List<UserAction> getList(String userId,int offset,int limit) {
		List<UserAction> userActions = getList(userId, offset, limit, null, null, 0);
		return userActions;
	}

    @Override
    protected String indexBeforeDate() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	protected List<UserAction> invokeOldGetList(String userId,int offset,int limit){
		return Collections.emptyList();
		
	};
	
	@Override
	protected List<UserAction> invokeOldGetListForConstruct(String key){
		return Collections.emptyList();
	};
}
