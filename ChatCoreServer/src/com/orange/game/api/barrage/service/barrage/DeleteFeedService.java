package com.orange.game.api.barrage.service.barrage;

import com.orange.barrage.model.feed.FeedManager;
import com.orange.barrage.service.feed.FeedService;
import com.orange.game.api.barrage.common.CommonBarrageService;
import com.orange.protocol.message.BarrageProtos;
import com.orange.protocol.message.MessageProtos;

/**
 * Created by pipi on 15/3/6.
 */
public class DeleteFeedService extends CommonBarrageService {
    private static DeleteFeedService ourInstance = new DeleteFeedService();

    public static DeleteFeedService getInstance() {
        return ourInstance;
    }

    private DeleteFeedService() {
    }

    @Override
    public boolean validateRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {
        return true;
    }

    @Override
    public void handleRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {

        MessageProtos.PBDeleteFeedRequest req = dataRequest.getDeleteFeedRequest();
        String feedId = req.getFeedId();
        String userId = dataRequest.getUserId();

        MessageProtos.PBDeleteFeedResponse.Builder rspBuilder = MessageProtos.PBDeleteFeedResponse.newBuilder();

        int resultCode = FeedService.getInstance().deleteFeed(feedId, rspBuilder, userId);

        MessageProtos.PBDeleteFeedResponse rsp = rspBuilder.build();

        responseBuilder.setResultCode(resultCode);
        responseBuilder.setDeleteFeedResponse(rsp);
    }

}
