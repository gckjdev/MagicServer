package com.orange.game.model.manager.useropus;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import com.mongodb.BasicDBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonMongoIdListManager;
import com.orange.game.model.dao.User;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.OpusManager;
import com.orange.game.model.manager.UserManager;
import com.orange.game.model.manager.opus.OpusUtils;
import com.orange.game.model.service.DBService;
import com.orange.game.model.xiaoji.XiaojiDraw;
import com.orange.game.model.xiaoji.XiaojiFactory;
import com.orange.game.model.xiaoji.XiaojiSing;

public class DrawToUserOpusManager extends CommonMongoIdListManager<UserAction> {

	
	public static final String USER_OPUS_PREFIX = "draw_to_user_opus_";
	public static final String OPUS_TABLE_NAME = DBConstants.T_OPUS;
    final String category;
	
	public DrawToUserOpusManager(String category) {
		super(USER_OPUS_PREFIX+category.toLowerCase(), OPUS_TABLE_NAME, UserAction.class);
        this.autoFixTotalSize = true;
        this.category = category;
	}	
	
	public void insertIndex(String userId, String opusId){
        insertAndConstructIndex(userId, opusId, true);
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

        if (userId.equalsIgnoreCase(DBConstants.ANOUNYMOUS_USER_ID)){
            return Collections.emptyList();
        }

        if (category.equalsIgnoreCase(XiaojiFactory.getInstance().getDraw().getCategoryName())){
            return OpusManager.getDrawToUserOpusList(DBService.getInstance().getMongoDBClient(), userId, offset, limit,true);
        }
        else{
            return Collections.emptyList();
        }
	};
	
	@Override
	protected List<UserAction> invokeOldGetListForConstruct(String key){

        if (category.equalsIgnoreCase(XiaojiFactory.getInstance().getDraw().getCategoryName())){
            return OpusManager.getDrawToUserOpusListDescending(DBService.getInstance().getMongoDBClient(), key, 0, 1000,true);
        }
        else{
            return Collections.emptyList();
        }

	};
	

}
