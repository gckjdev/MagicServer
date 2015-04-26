package com.orange.game.model.manager.useropus;

import java.util.Collections;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonMongoIdListManager;
import com.orange.game.model.dao.User;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.OpusManager;
import com.orange.game.model.manager.opus.OpusUtils;
import com.orange.game.model.service.DBService;
import com.orange.game.model.xiaoji.XiaojiFactory;

public class UserOpusManager extends CommonMongoIdListManager<UserAction> {

	public static final String USER_OPUS_PREFIX = "user_opus_";
	public static final String OPUS_TABLE_NAME = DBConstants.T_OPUS;
    final String category;
	
	public UserOpusManager(String category) {
		super(USER_OPUS_PREFIX+category.toLowerCase(), OPUS_TABLE_NAME, UserAction.class);
        this.isListIdAllowDuplicate = NOT_ALLOW_DUPLICATE;
        this.category = category;
        this.autoFixTotalSize = true;
    }
	
	public void insertIndex(String userId, String opusId){
		this.insertAndConstructIndex(userId, opusId, false);
	}

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

    @Override
    protected String indexBeforeDate() {
        return DBConstants.C_USER_INDEX_BEFORE_DATE;
    }


    @Override
	protected List<UserAction> invokeOldGetList(String userId,int offset,int limit){

        if (!XiaojiFactory.getInstance().isDraw(category)){
            return Collections.emptyList();
        }

        if (userId.equalsIgnoreCase(DBConstants.ANOUNYMOUS_USER_ID)){
            return Collections.emptyList();
        }

		return OpusManager.getUserOpusList(DBService.getInstance().getMongoDBClient(), category, userId, offset, limit,true);
	};
	
	@Override
	protected List<UserAction> invokeOldGetListForConstruct(String key){
        if (!XiaojiFactory.getInstance().isDraw(category)){
            return Collections.emptyList();
        }

		return OpusManager.getUserOpusListDescending(DBService.getInstance().getMongoDBClient(), category, key, 0, 1000, true);
	};
	
}

