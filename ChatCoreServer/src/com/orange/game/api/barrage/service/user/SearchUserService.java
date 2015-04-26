package com.orange.game.api.barrage.service.user;

import com.orange.barrage.service.user.FriendService;
import com.orange.barrage.service.user.SearchService;
import com.orange.game.api.barrage.common.CommonBarrageService;
import com.orange.protocol.message.MessageProtos;

/**
 * Created by pipi on 14/12/24.
 */
public class SearchUserService extends CommonBarrageService {

    private static SearchUserService ourInstance = new SearchUserService();

    public static SearchUserService getInstance() {
        return ourInstance;
    }

    private SearchUserService() {
    }

    @Override
    public boolean validateRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {
        return true;
    }

    @Override
    public void handleRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {

        MessageProtos.PBSearchUserRequest req = dataRequest.getSearchUserRequest();
        String searchKey = req.getKeyword();
        String userId = dataRequest.getUserId();
        int offset = req.getOffset();
        int limit = req.getLimit();

        MessageProtos.PBSearchUserResponse.Builder builder = MessageProtos.PBSearchUserResponse.newBuilder();

        int resultCode = SearchService.getInstance().searchUserByKey(userId, searchKey, offset, limit, builder);

        MessageProtos.PBSearchUserResponse rsp = builder.build();
        responseBuilder.setResultCode(resultCode);
        responseBuilder.setSearchUserResponse(rsp);

    }
}
