package com.orange.game.api.barrage.service.user.invitecode;

import com.orange.barrage.service.user.InviteCodeService;
import com.orange.game.api.barrage.common.CommonBarrageService;
import com.orange.protocol.message.MessageProtos;

/**
 * Created by pipi on 15/2/2.
 */
public class GetUserInviteCodeListService extends CommonBarrageService {
    private static GetUserInviteCodeListService ourInstance = new GetUserInviteCodeListService();

    public static GetUserInviteCodeListService getInstance() {
        return ourInstance;
    }

    private GetUserInviteCodeListService() {
    }

    @Override
    public boolean validateRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {
        return true;
    }

    @Override
    public void handleRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {

        MessageProtos.PBGetUserInviteCodeListRequest req = dataRequest.getGetUserInviteCodeListRequest();
        String userId = dataRequest.getUserId();

        MessageProtos.PBGetUserInviteCodeListResponse.Builder builder = MessageProtos.PBGetUserInviteCodeListResponse.newBuilder();
        int resultCode = InviteCodeService.getInstance().getUserInviteCodeList(userId, builder);

        MessageProtos.PBGetUserInviteCodeListResponse rsp = builder.build();
        responseBuilder.setResultCode(resultCode);
        responseBuilder.setGetUserInviteCodeListResponse(rsp);
    }

}
