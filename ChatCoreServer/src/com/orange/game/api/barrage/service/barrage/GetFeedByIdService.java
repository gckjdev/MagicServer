package com.orange.game.api.barrage.service.barrage;

import com.orange.barrage.model.feed.Feed;
import com.orange.barrage.model.feed.UserTimelineFeedManager;
import com.orange.barrage.service.feed.FeedService;
import com.orange.game.api.barrage.common.CommonBarrageService;
import com.orange.game.model.dao.CommonData;
import com.orange.protocol.message.BarrageProtos;
import com.orange.protocol.message.MessageProtos;

import java.util.List;

/**
 * Created by pipi on 15/3/24.
 */
public class GetFeedByIdService  extends CommonBarrageService {
    private static GetFeedByIdService ourInstance = new GetFeedByIdService();

    public static GetFeedByIdService getInstance() {
        return ourInstance;
    }

    private GetFeedByIdService() {
    }

    @Override
    public boolean validateRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {
        return true;
    }

    @Override
    public void handleRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {
        MessageProtos.PBGetFeedByIdRequest req = dataRequest.getGetFeedByIdRequest();

        String feedId = req.getFeedId();

        // set response
        MessageProtos.PBGetFeedByIdResponse.Builder rspBuilder = MessageProtos.PBGetFeedByIdResponse.newBuilder();

        int resultCode = FeedService.getInstance().getFeedById(feedId, rspBuilder);

        MessageProtos.PBGetFeedByIdResponse rsp = rspBuilder.build();
        responseBuilder.setResultCode(resultCode);
        responseBuilder.setGetFeedByIdResponse(rsp);
    }
}
