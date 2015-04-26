package com.orange.game.api.barrage.service.user;

import com.orange.barrage.model.user.UserManager;
import com.orange.game.api.barrage.common.CommonBarrageService;
import com.orange.protocol.message.MessageProtos;
import com.orange.protocol.message.UserProtos;

/**
 * Created by pipi on 14/12/24.
 */
public class UpdateUserInfoService extends CommonBarrageService {

    private static UpdateUserInfoService ourInstance = new UpdateUserInfoService();

    public static UpdateUserInfoService getInstance() {
        return ourInstance;
    }

    private UpdateUserInfoService() {
    }

    @Override
    public boolean validateRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {
        return true;
    }

    @Override
    public void handleRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {

        MessageProtos.PBUpdateUserInfoRequest req = dataRequest.getUpdateUserInfoRequest();

        UserProtos.PBUser updateUser = req.getUser();
        String userId = dataRequest.getUserId();

        MessageProtos.PBUpdateUserInfoResponse.Builder builder = MessageProtos.PBUpdateUserInfoResponse.newBuilder();

        int resultCode = UserManager.getInstance().updateUser(userId, updateUser, builder);

        responseBuilder.setResultCode(resultCode);
        responseBuilder.setUpdateUserInfoResponse(builder.build());
    }
}
