package com.orange.game.api.barrage.service.user.invitecode;

import com.orange.barrage.service.user.InviteCodeService;
import com.orange.game.api.barrage.common.CommonBarrageService;
import com.orange.protocol.message.MessageProtos;

/**
 * Created by pipi on 15/2/2.
 */
public class ApplyInviteCodeService extends CommonBarrageService {

    private static ApplyInviteCodeService ourInstance = new ApplyInviteCodeService();

    public static ApplyInviteCodeService getInstance() {
        return ourInstance;
    }

    private ApplyInviteCodeService() {
    }

    @Override
    public boolean validateRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {
        return true;
    }

    @Override
    public void handleRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {

        MessageProtos.PBApplyInviteCodeRequest req = dataRequest.getApplyInviteCodeRequest();
        int count = req.getCount();
        String userId = dataRequest.getUserId();

        MessageProtos.PBApplyInviteCodeResponse.Builder builder = MessageProtos.PBApplyInviteCodeResponse.newBuilder();
        int resultCode = InviteCodeService.getInstance().applyInviteCode(userId, count, builder);

        MessageProtos.PBApplyInviteCodeResponse rsp = builder.build();
        responseBuilder.setResultCode(resultCode);
        responseBuilder.setApplyInviteCodeResponse(rsp);
    }
}
