package com.orange.game.model.manager.timeline;

import java.util.ArrayList;
import java.util.Collections;
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

public class GuessOpusTimelineManager extends CommonMongoIdListManager<UserAction> {

	public static final String USER_OPUS_PREFIX = "timeline_guess";
	public static final String OPUS_ACTION_TABLE_NAME = DBConstants.T_OPUS_ACTION;
	
	public GuessOpusTimelineManager() {
		super(USER_OPUS_PREFIX, OPUS_ACTION_TABLE_NAME, UserAction.class);
	}	

	
	public void insertIndex(String userId, String actionId){
		this.insertId(userId, actionId, true, false);
	}	


	public List<UserAction> getList(String userId,int offset,int limit) {
		List<UserAction> userActions = getList( userId, offset, limit, 
												OpusUtils.NORMAL_RETURN_FIELDS, 
											    DBConstants.F_OPUS_STATUS, 
											    UserAction.STATUS_DELETE);
		
		
		userActions = OpusUtils.handlerUserAction(userActions, true);
		
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
