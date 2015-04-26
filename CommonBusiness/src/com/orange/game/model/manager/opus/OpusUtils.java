package com.orange.game.model.manager.opus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonMongoIdListManager;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.service.DBService;

public class OpusUtils {
	static Logger log = Logger.getLogger(CommonMongoIdListManager.class.getName());
	public static final BasicDBObject NORMAL_RETURN_FIELDS = createReturnFields();
	
	public static BasicDBObject createReturnFields(){
		BasicDBObject fields = new BasicDBObject();
		fields.put(DBConstants.F_GUESS_WORD_LIST, 0);
		fields.put(DBConstants.F_USERID_LIST, 0);
		fields.put(DBConstants.F_OPUS_RELATED_USER_ID, 0);
		fields.put(DBConstants.F_DRAW_DATA, 0);	
		return fields;
	}
	
	public static BasicDBObject createGuessReturnFields(){
		BasicDBObject fields = new BasicDBObject();
		fields.put(DBConstants.F_USERID_LIST, 0);
		fields.put(DBConstants.F_OPUS_RELATED_USER_ID, 0);
		fields.put(DBConstants.F_DRAW_DATA, 0);	
		return fields;
	}

	
	
	public static List<UserAction> handlerUserAction(List<UserAction> userActions, boolean requestOpusData) {
		List<Object> opusObjectIds = new ArrayList<Object>();
		for(UserAction userAction:userActions){
			if (userAction.isDrawType()
					&& userAction.getOpusStatus() == UserAction.STATUS_DELETE) {
				continue;
			}
			if (requestOpusData)  {
				String oid = userAction.getOpusId();
				if (!StringUtil.isEmpty(oid)) {
					opusObjectIds.add(new ObjectId(oid));
				}
			} else if (userAction.isDrawType()) {
				if (userAction.getOpusImageUrl() != null) {
					userAction.setDrawData(null);
				}
				userAction.setOpusWord(userAction.getWord());
			}
		}
		Map<String, UserAction> map = new HashMap<String, UserAction>();
		
		if (opusObjectIds.size() != 0) {
			// find the target opuses for once searching.

			DBCursor cursor = DBService.getInstance().getMongoDBClient().findByFieldInValues(DBConstants.T_OPUS,
					DBConstants.F_OBJECT_ID, opusObjectIds, OpusUtils.NORMAL_RETURN_FIELDS);
			// cursor = mongoClient.findByIds(DBConstants.T_ACTION,
			// DBConstants.F_OBJECT_ID, opusObjectIds);

			
		
			
			if (cursor != null) {
				while (cursor.hasNext()) {
					DBObject object = cursor.next();
					UserAction action = new UserAction(object);
					if (action != null) {
						String opid = action.getActionId();
						if (opid != null) {
							map.put(opid, action);
						}
					}
				}
				cursor.close();
			}
			log.info("<GetFeedList> iterate draw data query done");

			// find the target opus information, and set the information.
			for (UserAction uAction : userActions) {
				UserAction opus = map.get(uAction.getOpusId());
				if (opus != null) {

                    uAction.setCategory(opus.getCategory());
					uAction.setCorrectTimes(opus.getCorrectTimes());
					uAction.setMatchTimes(opus.getMatchTimes());
					uAction.setCommentTimes(opus.getCommentTimes());
					uAction.setGuessTimes(opus.getGuessTimes());
					uAction.setOpusStatus(opus.getOpusStatus());

					uAction.setTomatoTimes(opus.getTomatoTimes());
					uAction.setFlowerTimes(opus.getFlowerTimes());
					uAction.setSaveTimes(opus.getSaveTimes());

					uAction.setOpusWord(opus.getWord());
					uAction.setOpusCreatorNickName(opus.getNickName());
					uAction.setOpusCreatorAvatar(opus.getAvatar());
					uAction.setOpusCreatorGender(opus.getGender());

                    uAction.setDrawDataUrl(opus.getDrawDataUrl());

                    String imageUrl = opus.getOpusImageUrl();
                    String thumbUrl = opus.getOpusThumbImageUrl();
                    if (imageUrl == null || thumbUrl == null) {
                        uAction.setDrawData(opus.readDrawData(true));
                    } else {
                        uAction.setOpusImageUrl(imageUrl);
                        uAction.setOpusThumbImageUrl(thumbUrl);
                    }
				}
			}
			log.info("<GetFeedList> data generation ok");
		}
		return userActions;
	}
	
}
