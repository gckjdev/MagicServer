package com.orange.game.api.service.statistics;

import javax.servlet.http.HttpServletRequest;

import com.orange.game.model.manager.bbs.BBSManager;
import com.orange.game.model.manager.group.GroupNoticeManager;
import com.orange.game.model.manager.timeline.TutorialTimelineManager;
import com.orange.game.model.xiaoji.XiaojiFactory;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.User;
import com.orange.game.model.manager.MessageManager;
import com.orange.game.model.manager.friend.FriendManager;

public class GetStatisticsService extends CommonGameService {

	String userId;
	String appId;

    @Override
	public void handleData() {
		long feedCount = 0;
		long messageCount = 0;
		long fanCount = 0;
		long roomCount = 0;
		long commentCount = 0;
		long drawToMeCount = 0;
		long bbsCommentCount = 0;
		long timelineOpusCount = 0;
		long timelineGuessCount = 0;
        long groupNoticeCount = 0;
        long timelineConquerCount = 0;
		if (userId == null || userId.length() <= 0){
			log.warn("<GetStatisticsService> userId null");			
			return;
		}
		
		DBObject query = new BasicDBObject();
		query.put(DBConstants.F_USERID, new ObjectId(userId));
		
		DBObject fields = new BasicDBObject();
		fields.put(DBConstants.F_NEW_FAN_COUNT, 1);
		fields.put(DBConstants.F_DRAWTOME_COUNT, 1);		
		fields.put(DBConstants.F_NEW_BBSACTION_COUNT, 1);
		
		DBObject obj = mongoClient.findOne(DBConstants.T_USER, query, fields);
		if (obj == null){
			log.warn("<GetStatisticsService> userId "+userId+" not found");			
			return;
		}

		User user = new User(obj);
        if (xiaoji != null){
		    drawToMeCount = xiaoji.drawToUserOpusManager().getCountData(userId).getNewCount();
//            timelineGuessCount = xiaoji.guessOpusTimelineManager().getCountData(userId).getNewCount();
//            timelineOpusCount = xiaoji.opusTimelineManager().getCountData(userId).getNewCount();
            timelineGuessCount = xiaoji.guessOpusTimelineByCategoryManager().getCountData(userId).getNewCount();
            timelineConquerCount = TutorialTimelineManager.getInstance().getCountData(userId).getNewCount();

            if (XiaojiFactory.getInstance().isDraw(xiaoji.getCategoryName())){
                timelineOpusCount = xiaoji.opusTimelineManager().getCountData(userId).getNewCount();
            }
            else{
                timelineOpusCount = xiaoji.opusTimelineByCategoryManager().getCountData(userId).getNewCount();
            }
            commentCount = xiaoji.commentTimelineManager().getCountData(userId).getNewCount();
        }

		bbsCommentCount = BBSManager.getNewBBSActionCount(mongoClient, userId);

        groupNoticeCount = GroupNoticeManager.getGroupNewNoticeCount(mongoClient, groupId, userId, gameId);

		messageCount = MessageManager.getNewMessageCount(mongoClient, userId);

		log.info("message count = " + messageCount + ", fancount = " + fanCount
				+ ", roomcount = " + roomCount);

        feedCount = 0;
		fanCount = FriendManager.getFriendfansmanager().getCountData(userId).getNewCount();

		resultData = CommonServiceUtils.statisticsToJSON(messageCount,
				fanCount, feedCount, roomCount, commentCount, drawToMeCount,
				bbsCommentCount,timelineGuessCount,timelineOpusCount,timelineConquerCount, groupNoticeCount);

	}

	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
		userId = request.getParameter(ServiceConstant.PARA_USERID);
		if (!check(userId, ErrorCode.ERROR_PARAMETER_USERID_EMPTY,
				ErrorCode.ERROR_PARAMETER_USERID_NULL))
			return false;


		return true;
	}

}
