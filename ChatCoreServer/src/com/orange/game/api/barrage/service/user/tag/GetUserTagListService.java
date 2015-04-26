package com.orange.game.api.barrage.service.user.tag;

import com.orange.barrage.service.user.UserTagService;
import com.orange.game.api.barrage.common.CommonBarrageService;
import com.orange.protocol.message.MessageProtos;
import com.orange.protocol.message.UserProtos;

/**
 * Created by pipi on 15/1/20.
 */
public class GetUserTagListService extends CommonBarrageService {
    private static GetUserTagListService ourInstance = new GetUserTagListService();

    public static GetUserTagListService getInstance() {
        return ourInstance;
    }

    private GetUserTagListService() {
    }

    @Override
    public boolean validateRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {
        return true;
    }

    @Override
    public void handleRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {
        MessageProtos.PBGetUserTagListRequest req = dataRequest.getGetUserTagListRequest();

        String userId = dataRequest.getUserId();

        MessageProtos.PBGetUserTagListResponse.Builder builder = MessageProtos.PBGetUserTagListResponse.newBuilder();

        int resultCode = UserTagService.getInstance().getUserTagList(userId, builder);

        MessageProtos.PBGetUserTagListResponse rsp = builder.build();
        responseBuilder.setResultCode(resultCode);
        responseBuilder.setGetUserTagListResponse(rsp);
    }
}
