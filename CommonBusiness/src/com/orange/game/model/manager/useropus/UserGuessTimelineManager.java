package com.orange.game.model.manager.useropus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonMongoIdListManager;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.opus.OpusUtils;
import com.orange.game.model.service.DBService;

public class UserGuessTimelineManager extends CommonMongoIdListManager<UserAction> {

	public static final String USER_OPUS_PREFIX = "user_timeline_guess";
	public static final String OPUS_ACTION_TABLE_NAME = DBConstants.T_OPUS_ACTION;
	
	public UserGuessTimelineManager() {
		super(USER_OPUS_PREFIX, OPUS_ACTION_TABLE_NAME, UserAction.class);
	}	

	@Override
	protected List<UserAction> invokeOldGetList(String userId, int offset,
			int limit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<UserAction> invokeOldGetListForConstruct(String key) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void insertIndex(String userId,String actionId) {
		this.insertId(userId, actionId, false, false);
	}
	
	
	public List<UserAction> getList(String key,int offset ,int limit) {
		List<UserAction> userActions = this.getList(key, offset, limit,OpusUtils.NORMAL_RETURN_FIELDS, 
			    DBConstants.F_OPUS_STATUS, 
			    UserAction.STATUS_DELETE);
		
		userActions = OpusUtils.handlerUserAction(userActions, true);
	
		return userActions;
	}

    @Override
    protected String indexBeforeDate() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


}
