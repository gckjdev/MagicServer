package com.orange.game.api.barrage.service.user.friend;

import com.orange.barrage.service.user.FriendService;
import com.orange.game.api.barrage.common.CommonBarrageService;
import com.orange.protocol.message.MessageProtos;

/**
 * Created by pipi on 14/12/24.
 */
public class ProcessUserFriendService extends CommonBarrageService {

    private static ProcessUserFriendService ourInstance = new ProcessUserFriendService();

    public static ProcessUserFriendService getInstance() {
        return ourInstance;
    }

    private ProcessUserFriendService() {
    }

    @Override
    public boolean validateRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {
        return true;
    }

    @Override
    public void handleRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {

        MessageProtos.PBProcessUserFriendRequest req = dataRequest.getProcessUserFriendRequest();

        String userId = dataRequest.getUserId();
        String friendId = req.getFriendId();
        String memo = req.getMemo();
        int processResult = req.getProcessResult();

        int resultCode;
        switch (processResult){

            case MessageProtos.PBProcessFriendResultType.REJECT_FRIEND_VALUE:
                resultCode = FriendService.getInstance().rejectFriendRequest(userId, friendId, memo);
                break;

            case MessageProtos.PBProcessFriendResultType.REPLY_MEMO_VALUE:
                resultCode = FriendService.getInstance().replyFriendRequest(userId, friendId, memo);
                break;


            default:
            case MessageProtos.PBProcessFriendResultType.ACCEPT_FRIEND_VALUE:
                resultCode = FriendService.getInstance().acceptFriendRequest(userId, friendId);
                break;
        }

        responseBuilder.setResultCode(resultCode);

    }
}
