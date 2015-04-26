package com.orange.game.api.barrage.service.user.invitecode;

import com.orange.barrage.service.user.InviteCodeService;
import com.orange.game.api.barrage.common.CommonBarrageService;
import com.orange.protocol.message.MessageProtos;
import com.orange.protocol.message.UserProtos;

/**
 * Created by pipi on 15/2/2.
 */
public class UpdateInviteCodeService  extends CommonBarrageService {

    private static UpdateInviteCodeService ourInstance = new UpdateInviteCodeService();

    public static UpdateInviteCodeService getInstance() {
        return ourInstance;
    }

    private UpdateInviteCodeService() {
    }

    @Override
    public boolean validateRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {
        return true;
    }

    @Override
    public void handleRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {

        MessageProtos.PBUpdateInviteCodeRequest req = dataRequest.getUpdateInviteCodeRequest();
        String userId = dataRequest.getUserId();
        UserProtos.PBUserInviteCodeList updateList = req.getCodeList();

        MessageProtos.PBUpdateInviteCodeResponse.Builder builder = MessageProtos.PBUpdateInviteCodeResponse.newBuilder();
        int resultCode = InviteCodeService.getInstance().updateUserInviteCodeList(userId, updateList, builder);

        MessageProtos.PBUpdateInviteCodeResponse rsp = builder.build();
        responseBuilder.setResultCode(resultCode);
        responseBuilder.setUpdateInviteCodeResponse(rsp);
    }

}
