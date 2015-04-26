package com.orange.barrage.model.user;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.barrage.common.CommonModelManager;
import com.orange.barrage.constant.BarrageConstants;
import com.orange.barrage.model.feed.Feed;
import com.orange.game.model.common.CommonMongoIdComplexListManager;
import org.bson.types.ObjectId;

import java.util.List;

/**
 * Created by pipi on 14/12/25.
 */
public class FriendManager extends CommonMongoIdComplexListManager<User> {
    private static FriendManager ourInstance = new FriendManager();

    public static FriendManager getInstance() {
        return ourInstance;
    }

    private FriendManager() {
        super(BarrageConstants.T_FRIEND, BarrageConstants.T_USER, BarrageConstants.F_FRIEND_ID, User.class);
        this.useObjectIdForListKey = false;
    }


    @Override
    protected String indexBeforeDate() {
        return null;
    }

    @Override
    protected List invokeOldGetList(String userId, int offset, int limit) {
        return null;
    }

    @Override
    protected List invokeOldGetListForConstruct(String key) {
        return null;
    }

    public void addUserFriend(User user, User friend, BasicDBObject friendInfo) {
        insertObject(user.getUserId(), friend.toFriendDBObject(friendInfo), false, false, false);
        insertObject(friend.getUserId(), user.toFriendDBObject(friendInfo), false, false, false);
    }

    public void addUserFriend(String userId, String friendId, DBObject friendRequestInfo) {

        BasicDBObject requestInfoForUser = new BasicDBObject();
        requestInfoForUser.putAll(friendRequestInfo);
        requestInfoForUser.put(BarrageConstants.F_FRIEND_ID, friendId);     // replace to string for PB conversion
        insertObject(userId, requestInfoForUser, false, false, false);

        BasicDBObject requestInfoForFriend = new BasicDBObject();
        requestInfoForFriend.putAll(friendRequestInfo);
        requestInfoForFriend.put(BarrageConstants.F_FRIEND_ID, userId);     // replace to string for PB conversion
        insertObject(friendId, requestInfoForFriend, false, false, false);
    }

    public List<User> getFriendList(String userId) {
        return getList(userId, 0, Integer.MAX_VALUE, User.getPublicReturnFields(), null, 0);
    }

    public void deleteUserFriend(String userId, String friendId) {
        removeId(userId, friendId, false);
    }
}
