package com.orange.game.api.barrage.service.barrage;

import com.orange.barrage.service.feed.FeedService;
import com.orange.game.api.barrage.common.CommonBarrageService;
import com.orange.protocol.message.MessageProtos;

/**
 * Created by pipi on 15/3/24.
 */
public class ReadMyNewFeedService  extends CommonBarrageService {
    private static ReadMyNewFeedService ourInstance = new ReadMyNewFeedService();

    public static ReadMyNewFeedService getInstance() {
        return ourInstance;
    }

    private ReadMyNewFeedService() {
    }

    @Override
    public boolean validateRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {
        return true;
    }

    @Override
    public void handleRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {

        MessageProtos.PBReadMyNewFeedRequest req = dataRequest.getReadMyNewFeedRequest();

        String feedId = req.getFeedId();
        String userId = dataRequest.getUserId();

        int resultCode = FeedService.getInstance().readUserNewFeed(userId, feedId);

        responseBuilder.setResultCode(resultCode);
    }
}
