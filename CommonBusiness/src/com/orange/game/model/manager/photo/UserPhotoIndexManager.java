package com.orange.game.model.manager.photo;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonMongoIdListManager;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.dao.photo.UserPhoto;
import com.orange.game.model.dao.photo.UserPhotoData;

public class UserPhotoIndexManager extends CommonMongoIdListManager<UserPhotoData> {

	static final String TABLE_NAME_PREFIX = DBConstants.T_USER_PHOTO_INDEX + "_";
	
	public UserPhotoIndexManager(int usage) {
		super(TABLE_NAME_PREFIX+ usage, DBConstants.T_USER_PHOTO_DATA, UserPhotoData.class);
	}
	
	public static ConcurrentHashMap<Integer, UserPhotoIndexManager> map = new ConcurrentHashMap<Integer, UserPhotoIndexManager>();
	
	public static UserPhotoIndexManager getIndexManager(int usage){
		map.putIfAbsent(usage, new UserPhotoIndexManager(usage));
		return map.get(usage);
	}

	public void insertIndex(String userId, String userPhotoId){
		this.insertId(userId, userPhotoId, false, false);
	}


    @Override
    protected String indexBeforeDate() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	protected List<UserPhotoData> invokeOldGetList(String userId,int offset,int limit){
		return Collections.emptyList();
		
	};
	
	@Override
	protected List<UserPhotoData> invokeOldGetListForConstruct(String key){
		return Collections.emptyList();
	};
	
	
}
