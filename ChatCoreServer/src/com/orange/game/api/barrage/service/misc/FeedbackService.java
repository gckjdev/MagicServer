package com.orange.game.api.barrage.service.misc;

import com.mongodb.DBObject;
import com.orange.barrage.constant.BarrageConstants;
import com.orange.barrage.model.feed.FeedManager;
import com.orange.barrage.model.user.User;
import com.orange.game.api.barrage.common.CommonBarrageService;
import com.orange.game.model.dao.CommonData;
import com.orange.game.model.service.DBService;
import com.orange.protocol.message.BarrageProtos;
import com.orange.protocol.message.MessageProtos;
import com.orange.protocol.message.UserProtos;

import java.util.Date;

/**
 * Created by pipi on 15/3/6.
 */
public class FeedbackService extends CommonBarrageService {
    private static FeedbackService ourInstance = new FeedbackService();

    public static FeedbackService getInstance() {
        return ourInstance;
    }

    private FeedbackService() {
    }

    @Override
    public boolean validateRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {
        return true;
    }

    @Override
    public void handleRequest(MessageProtos.PBDataRequest dataRequest, MessageProtos.PBDataResponse.Builder responseBuilder) {

        MessageProtos.PBSendUserFeedbackRequest req = dataRequest.getSendUserFeedbackRequest();

        DBObject obj = CommonData.pbToDBObject(req, true, BarrageConstants.F_FEEDBACK_ID);
        obj.put(BarrageConstants.F_CREATE_DATE, new Date());

        log.info("create feedback request = "+obj.toString());
        DBService.getInstance().getMongoDBClient().insert(BarrageConstants.T_FEEDBACK, obj);

        MessageProtos.PBSendUserFeedbackResponse rsp = MessageProtos.PBSendUserFeedbackResponse.newBuilder().build();
        responseBuilder.setResultCode(0);
        responseBuilder.setSendUserFeedbackResponse(rsp);
    }

}
