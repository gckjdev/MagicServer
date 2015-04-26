package com.orange.game.api.barrage.service.barrage;

import com.orange.barrage.model.feed.FeedManager;
import com.orange.barrage.service.feed.FeedService;
import com.orange.game.api.barrage.common.CommonBarrageService;
import com.orange.protocol.message.BarrageProtos;
import com.orange.protocol.message.MessageProtos;

/**
 * Created by pipi on 15/3/6.
 */
public class DeleteFeedActionService extends CommonBarrageService {
    private static DeleteFeedActionService ourInstance = new DeleteFeedActionService();

    public static DeleteFeedActionService getInstance() {
        return ourInstance;
    }

    private DeleteFeedActionService() {
    }

    @Override
    public boolean validateRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {
        return true;
    }

    @Override
    public void handleRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {

        MessageProtos.PBDeleteFeedActionRequest req = dataRequest.getDeleteFeedActionRequest();

        String feedId = req.getFeedId();
        String actionId = req.getActionId();

        MessageProtos.PBDeleteFeedActionResponse.Builder rspBuilder = MessageProtos.PBDeleteFeedActionResponse.newBuilder();

        int resultCode = FeedService.getInstance().deleteFeedAction(feedId, actionId, rspBuilder);

        MessageProtos.PBDeleteFeedActionResponse rsp = rspBuilder.build();

        responseBuilder.setResultCode(resultCode);
        responseBuilder.setDeleteFeedActionResponse(rsp);    }
}
