package com.orange.game.model.manager.useropus;

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
import com.orange.game.model.xiaoji.XiaojiFactory;

public class UserFavoriteOpusManager extends CommonMongoIdListManager<UserAction>  {

	public static final String USER_OPUS_PREFIX = "user_favorite_opus_";
	public static final String OPUS_TABLE_NAME = DBConstants.T_OPUS;
    final String category;
	
	public UserFavoriteOpusManager(String category) {
		super(USER_OPUS_PREFIX+category.toLowerCase(), OPUS_TABLE_NAME, UserAction.class);
        this.autoFixTotalSize = true;
        this.category = category;
	}	

	
	public void insertIndex(String userId, String opusId){
		this.insertId(userId, opusId, CommonMongoIdListManager.NOT_ALLOW_DUPLICATE, false, false);
	}

    public List<UserAction> getList(String userId, int offset,int limit){
        return getList(userId, offset, limit, OpusUtils.NORMAL_RETURN_FIELDS, DBConstants.F_OPUS_STATUS, UserAction.STATUS_DELETE);
    }

    @Override
    protected String indexBeforeDate() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	protected List<UserAction> invokeOldGetList(String userId,int offset,int limit){

        if (userId.equalsIgnoreCase(DBConstants.ANOUNYMOUS_USER_ID)){
            return Collections.emptyList();
        }

        if (category.equalsIgnoreCase(XiaojiFactory.getInstance().getDraw().getCategoryName())){
            return OpusManager.getSavedOpusList(DBService.getInstance().getMongoDBClient(), userId, offset, limit);
        }
        else{
            return Collections.emptyList();
        }


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
	
	@Override
	protected List<UserAction> invokeOldGetListForConstruct(String key){

        if (!category.equalsIgnoreCase(XiaojiFactory.getInstance().getDraw().getCategoryName())){
            return Collections.emptyList();
        }

        List<UserAction> insertList = OpusManager.getSavedOpusList(DBService.getInstance().getMongoDBClient(), key, 0, 1000);
		Collections.reverse(insertList);
		return insertList;
	};
	
	
	
	public void removeIndex(String key,String opusId) {
		this.removeId(key, opusId, false);
	}
}
