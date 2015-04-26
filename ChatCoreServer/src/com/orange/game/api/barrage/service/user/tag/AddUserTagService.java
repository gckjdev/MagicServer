package com.orange.game.api.barrage.service.user.tag;

import com.orange.barrage.service.user.UserTagService;
import com.orange.game.api.barrage.common.CommonBarrageService;
import com.orange.game.model.service.user.UserService;
import com.orange.protocol.message.MessageProtos;
import com.orange.protocol.message.UserProtos;

/**
 * Created by pipi on 15/1/20.
 */
public class AddUserTagService extends CommonBarrageService {
    private static AddUserTagService ourInstance = new AddUserTagService();

    public static AddUserTagService getInstance() {
        return ourInstance;
    }

    private AddUserTagService() {
    }

    @Override
    public boolean validateRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {
        return true;
    }

    @Override
    public void handleRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {

        MessageProtos.PBAddUserTagRequest req = dataRequest.getAddUserTagRequest();
        UserProtos.PBUserTag userTag = req.getTag();
        String userId = dataRequest.getUserId();

        MessageProtos.PBAddUserTagResponse.Builder builder = MessageProtos.PBAddUserTagResponse.newBuilder();

        int resultCode = UserTagService.getInstance().addUserTag(userId, userTag, builder);

        MessageProtos.PBAddUserTagResponse rsp = builder.build();
        responseBuilder.setResultCode(resultCode);
        responseBuilder.setAddUserTagResponse(rsp);
    }
}
