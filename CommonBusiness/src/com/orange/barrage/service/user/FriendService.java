package com.orange.barrage.service.user;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.barrage.common.CommonModelService;
import com.orange.barrage.constant.BarrageConstants;
import com.orange.barrage.model.user.FriendManager;
import com.orange.barrage.model.user.FriendRequestManager;
import com.orange.barrage.model.user.User;
import com.orange.game.constants.DBConstants;
import com.orange.protocol.message.ErrorProtos;
import com.orange.protocol.message.MessageProtos;
import com.orange.protocol.message.UserProtos;

/**
 * Created by pipi on 14/12/25.
 */
public class FriendService  extends CommonModelService {
    private static FriendService ourInstance = new FriendService();

    public static FriendService getInstance() {
        return ourInstance;
    }

    private FriendService() {
    }

    public int addUserFriend(User user, User friend, int addSource, MessageProtos.PBAddUserFriendResponse.Builder rspBuilder) {
        UserProtos.PBUser pbFriend = friend.toProtoBufModel();
        UserProtos.PBUser pbUser = user.toProtoBufModel();
        switch (pbFriend.getAddConfig()){
            case UserProtos.FriendAddConfigType.ACCEPT_ALL_VALUE:
                // add directly
                // insert friend into user-friend-id-list-table (both side)
                BasicDBObject friendInfo = new BasicDBObject();
                friendInfo.put(BarrageConstants.F_ADD_DATE, System.currentTimeMillis()/1000);
                friendInfo.put(BarrageConstants.F_ADD_SOURCE, addSource);

                FriendManager.getInstance().addUserFriend(user, friend, friendInfo);
                break;

            case UserProtos.FriendAddConfigType.DISALLOW_ADD_ME_VALUE:
                return ErrorProtos.PBError.ERROR_FRIEND_NOT_ALLOW_ADD_ME_VALUE;

            case UserProtos.FriendAddConfigType.REQUIRE_ACCEPT_VALUE:
            default:
                // need user accept the request
                // insert request to USER/FRIEND both side
                BasicDBObject reqInfo = new BasicDBObject();
                reqInfo.put(BarrageConstants.F_ADD_DATE, System.currentTimeMillis()/1000);
                reqInfo.put(BarrageConstants.F_ADD_SOURCE, addSource);

                FriendRequestManager.getInstance().addUserFriendRequest(user, friend, reqInfo);
                break;

        }

        return 0;
    }

    public int rejectFriendRequest(String userId, String friendId, String memo) {
        // update both request status
        FriendRequestManager.getInstance().rejectFriendRequest(userId, friendId, memo);
        return 0;
    }

    public int replyFriendRequest(String userId, String friendId, String memo) {
        // update both request status
        FriendRequestManager.getInstance().updateFriendRequestMemo(userId, friendId, memo);
        return 0;
    }

    public int acceptFriendRequest(String userId, String friendId) {

        // accept request
        FriendRequestManager.getInstance().acceptFriendRequest(userId, friendId);

        // add user friend now!
        DBObject requestInfo = FriendRequestManager.getInstance().getFriendRequest(userId, friendId);
        FriendManager.getInstance().addUserFriend(userId, friendId, requestInfo);
        return 0;
    }

    public int deleteFriend(String userId, String friendId, int addStatus) {

        if (addStatus != UserProtos.FriendAddStatusType.REQ_STATUS_NONE_VALUE){
            FriendRequestManager.getInstance().deleteFriendRequest(userId, friendId);
        }

        FriendManager.getInstance().deleteUserFriend(userId, friendId);
        return 0;
    }
}
