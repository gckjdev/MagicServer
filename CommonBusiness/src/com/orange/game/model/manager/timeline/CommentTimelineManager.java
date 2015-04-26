package com.orange.game.model.manager.timeline;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonMongoIdListManager;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.OpusManager;
import com.orange.game.model.manager.opus.OpusUtils;
import com.orange.game.model.service.DBService;

public class CommentTimelineManager extends CommonMongoIdListManager<UserAction> {

	public static final String USER_OPUS_PREFIX = "timeline_comment";
	public static final String OPUS_ACTION_TABLE_NAME = DBConstants.T_OPUS_ACTION;
	
	public CommentTimelineManager() {
		super(USER_OPUS_PREFIX, OPUS_ACTION_TABLE_NAME, UserAction.class);
	}	
	
	
	public void insertIndex(String userId, String actionId){
		this.insertAndConstructIndex(userId, actionId, true);
	}	


	public List<UserAction> getList(String userId,int offset,int limit) {
		/*List<UserAction> userActions = getList( userId, offset, limit, 
												OpusUtils.NORMAL_RETURN_FIELDS, 
											    DBConstants.F_OPUS_STATUS, 
											    UserAction.STATUS_DELETE);*/
		List<UserAction> userActions = getListAndConstructIndex(userId, offset, limit);
		return userActions;
	}

    @Override
    protected String indexBeforeDate() {
        return DBConstants.C_OPUS_INDEX_BEFORE_DATE;
    }

    @Override
	protected List<UserAction> invokeOldGetList(String userId,int offset,int limit){
		return OpusManager.getMyCommentList(DBService.getInstance().getMongoDBClient(), userId, offset, limit);
		
	};
	
	@Override
	protected List<UserAction> invokeOldGetListForConstruct(String key){
		List<UserAction> list = OpusManager.getMyCommentListDisorder(DBService.getInstance().getMongoDBClient(), key, 0, 1000);
		
		Collections.sort(list, new Comparator<UserAction>() {

			@Override
			public int compare(UserAction o1, UserAction o2) {
				return o1.getCreateDate().compareTo(o2.getCreateDate());
			}
		});
		return list;
	};
	
	
	
	@Override
    protected BasicDBObject returnMongoDBFields(){
        return OpusUtils.createReturnFields();
    }

    @Override
    protected String deleteStatusFieldName(){
        return  DBConstants.F_OPUS_STATUS;
    }

    @Override
    protected int deleteStatusValue(){
        return UserAction.STATUS_DELETE;
    }

}
