package com.orange.game.api.barrage.service.user.invitecode;

import com.orange.barrage.service.user.InviteCodeService;
import com.orange.game.api.barrage.common.CommonBarrageService;
import com.orange.protocol.message.MessageProtos;

/**
 * Created by pipi on 15/1/10.
 */
public class GetNewInviteCodeService  extends CommonBarrageService {

    private static GetNewInviteCodeService ourInstance = new GetNewInviteCodeService();

    public static GetNewInviteCodeService getInstance() {
        return ourInstance;
    }

    private GetNewInviteCodeService() {
    }

    @Override
    public boolean validateRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {
        return true;
    }

    @Override
    public void handleRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {

        MessageProtos.PBGetNewInviteCodeRequest req = dataRequest.getGetNewInviteCodeRequest();
        int count = req.getCount();

        MessageProtos.PBGetNewInviteCodeResponse.Builder builder = MessageProtos.PBGetNewInviteCodeResponse.newBuilder();
        int resultCode = InviteCodeService.getInstance().getNewInviteCode(count, builder);

        MessageProtos.PBGetNewInviteCodeResponse rsp = builder.build();
        responseBuilder.setResultCode(resultCode);
        responseBuilder.setGetNewInviteCodeResponse(rsp);
    }
}
