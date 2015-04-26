package com.orange.barrage.model.user;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.barrage.constant.BarrageConstants;
import com.orange.common.utils.DateUtil;
import com.orange.game.model.common.CommonMongoIdComplexListManager;
import com.orange.protocol.message.UserProtos;

import java.util.List;

/**
 * Created by pipi on 14/12/25.
 */
public class FriendRequestManager extends CommonMongoIdComplexListManager<User> {
    private static FriendRequestManager ourInstance = new FriendRequestManager();

    public static FriendRequestManager getInstance() {
        return ourInstance;
    }

    private FriendRequestManager() {
        super(BarrageConstants.T_FRIEND_REQUEST, BarrageConstants.T_USER, BarrageConstants.F_FRIEND_ID, User.class);
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

    public void addUserFriendRequest(User user, User friend, BasicDBObject friendRequestInfo) {

        friendRequestInfo.put(BarrageConstants.F_ADD_STATUS, UserProtos.FriendAddStatusType.REQ_WAIT_ACCEPT_VALUE);
        friendRequestInfo.put(BarrageConstants.F_ADD_DATE, System.currentTimeMillis()/1000);

        insertObject(user.getUserId(),
                friend.toFriendRequestDBObject(friendRequestInfo, UserProtos.FriendRequestDirection.REQ_DIRECTION_SENDER_VALUE),
                false, false, false);

        insertObject(friend.getUserId(),
                user.toFriendRequestDBObject(friendRequestInfo, UserProtos.FriendRequestDirection.REQ_DIRECTION_RECEIVER_VALUE),
                false, false, false);
    }

    public void rejectFriendRequest(String userId, String friendId, String memo) {
        BasicDBObject obj = new BasicDBObject();
        obj.put(BarrageConstants.F_ADD_STATUS, UserProtos.FriendAddStatusType.REQ_REJECTED_VALUE);
        obj.put(BarrageConstants.F_ADD_DATE, DateUtil.getCurrentSeconds());

        updateIndexObject(userId, friendId, obj);
        updateIndexObject(friendId, userId, obj);
    }

    public void updateFriendRequestReplyMemo(String userId, String friendId, String memo) {
        BasicDBObject obj = new BasicDBObject();
        obj.put(BarrageConstants.F_REPLY_MEMO, memo);
        obj.put(BarrageConstants.F_ADD_DATE, DateUtil.getCurrentSeconds());

        updateIndexObject(userId, friendId, obj);
        updateIndexObject(friendId, userId, obj);
    }

    public void updateFriendRequestMemo(String userId, String friendId, String memo) {
        BasicDBObject obj = new BasicDBObject();
        obj.put(BarrageConstants.F_MEMO, memo);
        obj.put(BarrageConstants.F_ADD_DATE, DateUtil.getCurrentSeconds());

        updateIndexObject(userId, friendId, obj);
        updateIndexObject(friendId, userId, obj);
    }

    public void acceptFriendRequest(String userId, String friendId) {
        BasicDBObject obj = new BasicDBObject();
        obj.put(BarrageConstants.F_ADD_STATUS, UserProtos.FriendAddStatusType.REQ_ACCEPTED_VALUE);
        obj.put(BarrageConstants.F_ADD_DATE, DateUtil.getCurrentSeconds());

        updateIndexObject(userId, friendId, obj);
        updateIndexObject(friendId, userId, obj);
    }

    public List<User> getRequestList(String userId) {
        return getList(userId, 0, Integer.MAX_VALUE, User.getPublicReturnFields(), null, 0);
    }

    public DBObject getFriendRequest(String userId, String friendId) {
        return getObjectInfo(userId, friendId);
    }

    public void deleteFriendRequest(String userId, String friendId) {
        removeId(userId, friendId, false);
    }
}
