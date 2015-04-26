package com.orange.game.api.barrage.service.barrage;

import com.orange.barrage.service.feed.FeedService;
import com.orange.game.api.barrage.common.CommonBarrageService;
import com.orange.game.api.barrage.common.CommonDataRequestService;
import com.orange.game.model.dao.CommonData;
import com.orange.protocol.message.BarrageProtos;
import com.orange.protocol.message.MessageProtos;

/**
 * Created by pipi on 14/12/8.
 */
public class CreateFeedService extends CommonBarrageService {

    private static CreateFeedService ourInstance = new CreateFeedService();

    public static CreateFeedService getInstance() {
        return ourInstance;
    }

    private CreateFeedService() {
    }

    @Override
    public boolean validateRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {
        return true;
    }

    @Override
    public void handleRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {

        BarrageProtos.PBFeed feed = dataRequest.getCreateFeedRequest().getFeed();
        MessageProtos.PBCreateFeedResponse.Builder rspBuilder = MessageProtos.PBCreateFeedResponse.newBuilder();

        int resultCode = FeedService.getInstance().createFeed(feed, rspBuilder);

        MessageProtos.PBCreateFeedResponse rsp = rspBuilder.build();

        responseBuilder.setResultCode(resultCode);
        responseBuilder.setCreateFeedResponse(rsp);
    }
}
