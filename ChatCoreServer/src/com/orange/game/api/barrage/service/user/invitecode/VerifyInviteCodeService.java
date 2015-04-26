package com.orange.game.api.barrage.service.user.invitecode;

import com.orange.barrage.service.user.InviteCodeService;
import com.orange.game.api.barrage.common.CommonBarrageService;
import com.orange.protocol.message.MessageProtos;

/**
 * Created by pipi on 14/12/25.
 */
public class VerifyInviteCodeService extends CommonBarrageService {

    private static VerifyInviteCodeService ourInstance = new VerifyInviteCodeService();

    public static VerifyInviteCodeService getInstance() {
        return ourInstance;
    }

    private VerifyInviteCodeService() {
    }

    @Override
    public boolean validateRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {
        return true;
    }

    @Override
    public void handleRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {

        MessageProtos.PBVerifyInviteCodeRequest req = dataRequest.getVerifyInviteCodeRequest();
        String code = req.getInviteCode();

        MessageProtos.PBVerifyInviteCodeResponse.Builder builder = MessageProtos.PBVerifyInviteCodeResponse.newBuilder();
        int resultCode = InviteCodeService.getInstance().checkInviteCode(code, builder);

        MessageProtos.PBVerifyInviteCodeResponse rsp = builder.build();
        responseBuilder.setResultCode(resultCode);
        responseBuilder.setVerifyInviteCodeResponse(rsp);
    }
}
