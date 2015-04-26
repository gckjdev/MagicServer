package com.orange.game.api.barrage.service.barrage;

import com.googlecode.protobuf.format.JsonFormat;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import com.orange.barrage.model.feed.Feed;
import com.orange.barrage.model.feed.FeedManager;
import com.orange.barrage.model.feed.UserTimelineFeedManager;
import com.orange.barrage.service.feed.FeedService;
import com.orange.game.api.barrage.common.CommonBarrageService;
import com.orange.protocol.message.BarrageProtos;
import com.orange.protocol.message.MessageProtos;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pipi on 14/12/8.
 */
public class ReplyFeedService extends CommonBarrageService {

    private static ReplyFeedService ourInstance = new ReplyFeedService();

    public static ReplyFeedService getInstance() {
        return ourInstance;
    }

    private ReplyFeedService() {
    }

    @Override
    public boolean validateRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {
        return true;
    }

    @Override
    public void handleRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {

        MessageProtos.PBReplyFeedRequest req = dataRequest.getReplyFeedRequest();
        BarrageProtos.PBFeedAction action = req.getAction();

        MessageProtos.PBReplyFeedResponse.Builder rspBuilder = MessageProtos.PBReplyFeedResponse.newBuilder();

        int resultCode = FeedService.getInstance().replyFeed(action, rspBuilder);

        MessageProtos.PBReplyFeedResponse rsp = rspBuilder.build();

        responseBuilder.setResultCode(resultCode);
        responseBuilder.setReplyFeedResponse(rsp);
    }
}
