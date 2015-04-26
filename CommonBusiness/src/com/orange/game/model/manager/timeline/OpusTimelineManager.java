package com.orange.game.model.manager.timeline;

import java.util.Collections;
import java.util.List;

import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonMongoIdListManager;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.opus.OpusUtils;

public class OpusTimelineManager extends CommonMongoIdListManager<UserAction> {

	public static final String USER_OPUS_PREFIX = "timeline_opus";
	public static final String OPUS_TABLE_NAME = DBConstants.T_OPUS;
	
	public OpusTimelineManager() {
		super(USER_OPUS_PREFIX, OPUS_TABLE_NAME, UserAction.class);
	}

    public OpusTimelineManager(String category) {
        super(USER_OPUS_PREFIX + "_" + category, OPUS_TABLE_NAME, UserAction.class);
    }
	
	public void insertIndex(String userId, String actionId){
		this.insertId(userId, actionId, true, false);
	}	


	public List<UserAction> getList(String userId,int offset,int limit) {
		List<UserAction> userActions = getList( userId, offset, limit, 
												OpusUtils.NORMAL_RETURN_FIELDS, 
											    DBConstants.F_OPUS_STATUS, 
											    UserAction.STATUS_DELETE);
		return userActions;
	}
	
	@Override
	protected List<UserAction> invokeOldGetList(String userId,int offset,int limit){
		return Collections.emptyList();
		
	};
	
	@Override
	protected List<UserAction> invokeOldGetListForConstruct(String key){
		return Collections.emptyList();
	};

    @Override
    protected String indexBeforeDate() {
        return null;
    }

}
