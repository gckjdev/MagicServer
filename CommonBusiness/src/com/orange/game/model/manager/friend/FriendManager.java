package com.orange.game.model.manager.friend;

import com.mongodb.BasicDBObject;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonMongoIdComplexListManager;
import com.orange.game.model.dao.Relation;
import com.orange.game.model.dao.User;
import com.orange.game.model.dao.UserFriend;
import com.orange.game.model.manager.UserManager;
import org.bson.types.ObjectId;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-6-25
 * Time: 下午3:11
 * To change this template use File | Settings | File Templates.
 */
public class FriendManager extends CommonMongoIdComplexListManager<User> {

   

	static final String T_USER_FRIEND_PREFIX = "user_friend_";
    
    final static String FRIEND_FOLLOW = "follow";
	final static String FRIEND_FANS = "fans";
	final static String FRIEND_BLACK_USER = "black";
    final int relationType;
    static final FriendManager friendFollowManager = new FriendManager(FRIEND_FOLLOW, Relation.RELATION_TYPE_FOLLOW);
	static final FriendManager friendFansManager = new FriendManager(FRIEND_FANS, Relation.RELATION_TYPE_FAN);
	static final FriendManager friendBlackManager = new FriendManager(FRIEND_BLACK_USER, Relation.RELATION_TYPE_BLACK);

    public FriendManager(String relationName, int relationType) {
        super(T_USER_FRIEND_PREFIX + relationName.toLowerCase(), DBConstants.T_USER, DBConstants.F_FRIENDID, User.class);
        this.relationType = relationType;
    }

    @Override
    protected String indexBeforeDate() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected List invokeOldGetList(String userId, int offset, int limit) {
        return Collections.emptyList();
    }

    @Override
    protected List invokeOldGetListForConstruct(String key) {
        return Collections.emptyList();
    }

    // this method is only used for transferring old data
    public void addFriend(String userId, String friendUserId, String sourceGameId, Date date){

        BasicDBObject obj = new BasicDBObject();
        obj.put(DBConstants.F_FRIENDID, new ObjectId(friendUserId));
        obj.put(DBConstants.F_CREATE_DATE, date);
        obj.put(DBConstants.F_TYPE, relationType);
        obj.put(DBConstants.F_MEMO, null);
        obj.put(DBConstants.F_SOURCE, sourceGameId);

        insertObject(userId, obj, DBConstants.F_CREATE_DATE, false, false);
    }

    public void addFriend(String userId, String friendUserId, String sourceGameId){

        BasicDBObject obj = new BasicDBObject();
        obj.put(DBConstants.F_FRIENDID, new ObjectId(friendUserId));
        obj.put(DBConstants.F_CREATE_DATE, new Date());
        obj.put(DBConstants.F_TYPE, relationType);
        obj.put(DBConstants.F_MEMO, null);
        obj.put(DBConstants.F_SOURCE, sourceGameId);

        insertObject(userId, obj, false, true, false);
    }

    public void removeFriend(String userId, String friendUserId){
        removeId(userId, friendUserId, false);
    }

    public void updateFriendMemo(String userId, String friendUserId, String memo){

        BasicDBObject objectData = new BasicDBObject();
        objectData.put(DBConstants.F_MEMO, memo);

        updateIndexObject(userId, friendUserId, objectData);
    }

    public List<User> getUserFriendList(String userId, int offset, int limit){
        return getList(userId, offset, limit, UserManager.getUserPublicReturnFields(), null, 0);
    }
    
    public static FriendManager getFriendfollowmanager() {
		return friendFollowManager;
	}

	public static FriendManager getFriendfansmanager() {
		return friendFansManager;
	}

	public static FriendManager getFriendblackmanager() {
		return friendBlackManager;
	}

	public List<ObjectId> getUserFriendIdList(String userId, int offset, int limit) {
		return getObjectIdList(userId, offset, limit);
	}


    public boolean hasRelation(String userId, String fid) {
        return isIdExistInList(userId, fid);
    }

    public UserFriend getFriendInfo(String userId, String fid){
        BasicDBObject obj = (BasicDBObject)getObjectInfo(userId, fid);
        if (obj == null){
            return UserFriend.relationNO();
        }

        UserFriend userFriend = new UserFriend(relationType, obj.getString(DBConstants.F_MEMO));
        return userFriend;
    }


}
