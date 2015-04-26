package com.orange.game.api.barrage.service.barrage;

import com.orange.barrage.service.feed.FeedService;
import com.orange.game.api.barrage.common.CommonBarrageService;
import com.orange.protocol.message.MessageProtos;

/**
 * Created by pipi on 15/3/31.
 */
public class GetUserFeedService extends CommonBarrageService {
    private static GetUserFeedService ourInstance = new GetUserFeedService();

    public static GetUserFeedService getInstance() {
        return ourInstance;
    }

    private GetUserFeedService() {
    }

    @Override
    public boolean validateRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {
        return true;
    }

    @Override
    public void handleRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {
        MessageProtos.PBGetUserFeedRequest req = dataRequest.getGetUserFeedRequest();

        String userId = dataRequest.getUserId();
        int offset = req.getOffset();
        int limit = req.getLimit();

        MessageProtos.PBGetUserFeedResponse.Builder rspBuilder = MessageProtos.PBGetUserFeedResponse.newBuilder();

        int resultCode = FeedService.getInstance().getUserFeed(userId, offset, limit, rspBuilder);

        MessageProtos.PBGetUserFeedResponse rsp = rspBuilder.build();

        responseBuilder.setResultCode(resultCode);
        responseBuilder.setGetUserFeedResponse(rsp);
    }
}
