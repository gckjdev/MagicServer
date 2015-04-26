package com.orange.game.api.barrage.service.barrage;

import com.orange.barrage.model.feed.FeedManager;
import com.orange.barrage.service.feed.FeedService;
import com.orange.game.api.barrage.common.CommonBarrageService;
import com.orange.protocol.message.BarrageProtos;
import com.orange.protocol.message.MessageProtos;

/**
 * Created by pipi on 15/3/20.
 */
public class GetMyNewFeedService extends CommonBarrageService {

    private static GetMyNewFeedService ourInstance = new GetMyNewFeedService();

    public static GetMyNewFeedService getInstance() {
        return ourInstance;
    }

    private GetMyNewFeedService() {
    }

    @Override
    public boolean validateRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {
        return true;
    }

    @Override
    public void handleRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {

        MessageProtos.PBGetMyNewFeedListRequest req = dataRequest.getGetMyNewFeedListRequest();

        String userId = dataRequest.getUserId();

        MessageProtos.PBGetMyNewFeedListResponse.Builder rspBuilder = MessageProtos.PBGetMyNewFeedListResponse.newBuilder();

        int resultCode = FeedService.getInstance().getUserNewFeed(userId, rspBuilder);

        MessageProtos.PBGetMyNewFeedListResponse rsp = rspBuilder.build();

        responseBuilder.setResultCode(resultCode);
        responseBuilder.setGetMyNewFeedListResponse(rsp);
    }
}
