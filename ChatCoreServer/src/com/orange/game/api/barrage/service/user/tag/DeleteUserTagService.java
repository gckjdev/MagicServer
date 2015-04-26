package com.orange.game.api.barrage.service.user.tag;

import com.orange.barrage.service.user.UserTagService;
import com.orange.game.api.barrage.common.CommonBarrageService;
import com.orange.protocol.message.MessageProtos;
import com.orange.protocol.message.UserProtos;

/**
 * Created by pipi on 15/1/20.
 */
public class DeleteUserTagService extends CommonBarrageService {
    private static DeleteUserTagService ourInstance = new DeleteUserTagService();

    public static DeleteUserTagService getInstance() {
        return ourInstance;
    }

    private DeleteUserTagService() {
    }

    @Override
    public boolean validateRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {
        return true;
    }

    @Override
    public void handleRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {
        MessageProtos.PBDeleteUserTagRequest req = dataRequest.getDeleteUserTagRequest();
        UserProtos.PBUserTag userTag = req.getTag();
        String userId = dataRequest.getUserId();

        MessageProtos.PBDeleteUserTagResponse.Builder builder = MessageProtos.PBDeleteUserTagResponse.newBuilder();

        int resultCode = UserTagService.getInstance().deleteUserTag(userId, userTag, builder);

        MessageProtos.PBDeleteUserTagResponse rsp = builder.build();
        responseBuilder.setResultCode(resultCode);
        responseBuilder.setDeleteUserTagResponse(rsp);
    }
}
