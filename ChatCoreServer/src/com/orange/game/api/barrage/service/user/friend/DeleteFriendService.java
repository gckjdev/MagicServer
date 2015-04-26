package com.orange.game.api.barrage.service.user.friend;

import com.orange.barrage.model.feed.FeedManager;
import com.orange.barrage.model.user.UserManager;
import com.orange.barrage.service.user.FriendService;
import com.orange.game.api.barrage.common.CommonBarrageService;
import com.orange.protocol.message.BarrageProtos;
import com.orange.protocol.message.MessageProtos;

/**
 * Created by pipi on 15/3/6.
 */
public class DeleteFriendService extends CommonBarrageService {
    private static DeleteFriendService ourInstance = new DeleteFriendService();

    public static DeleteFriendService getInstance() {
        return ourInstance;
    }

    private DeleteFriendService() {
    }

    @Override
    public boolean validateRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {
        return true;
    }

    @Override
    public void handleRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {

        MessageProtos.PBDeleteFriendRequest req = dataRequest.getDeleteFriendRequest();
        String userId = dataRequest.getUserId();
        String friendId = req.getUserId();
        int addStatus = req.getAddStatus();

        MessageProtos.PBDeleteFriendResponse.Builder rspBuilder = MessageProtos.PBDeleteFriendResponse.newBuilder();

        int resultCode = FriendService.getInstance().deleteFriend(userId, friendId, addStatus);

        MessageProtos.PBDeleteFriendResponse rsp = rspBuilder.build();

        responseBuilder.setResultCode(resultCode);
        responseBuilder.setDeleteFriendResponse(rsp);
    }

}
