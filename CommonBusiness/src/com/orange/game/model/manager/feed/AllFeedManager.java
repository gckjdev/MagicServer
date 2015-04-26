package com.orange.game.model.manager.feed;

import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;

import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.UserAction;

public class AllFeedManager extends FeedManager {
	private static AllFeedManager allFeedManager = null;

	public static AllFeedManager getInstance() {
		if (allFeedManager == null) {
			allFeedManager = new AllFeedManager();
		}
		return allFeedManager;
	}

	private void updateTimeLineReadCount (String uid, int value) {
		
//		MongoDBClient mongoClient = mongoDBClientHolder.get(); 
		
		DBObject query = new BasicDBObject();
		query.put(DBConstants.F_OWNER, new ObjectId(uid));
		
		DBObject update = new BasicDBObject();
		DBObject setObject = new BasicDBObject();
		setObject.put(DBConstants.F_TIMELINE_READ_COUNT, value);
		update.put("$set", setObject);
		
		mongoClient.updateOne(DBConstants.T_TIMELINE, query, update);
	}
	
	public List<ObjectId> getAllFeedList(String uid, int offset, int limit) {
		
//		MongoDBClient mongoClient = mongoDBClientHolder.get(); 
		
		if (StringUtil.isEmpty(uid)) {
			return null;
		}
		String arrayField = DBConstants.F_ACTION_IDS;
		DBObject query = new BasicDBObject();
		query.put(DBConstants.F_OWNER, new ObjectId(uid));
		int nOffset = -(offset + limit);
		DBObject object = mongoClient.findOneWithArrayLimit(
				DBConstants.T_TIMELINE, query, arrayField, nOffset, limit, null);
		
		if (object != null) {
			int timeLineCount = (Integer) object
					.get(DBConstants.F_TIMELINE_COUNT);
			if (offset == 0) {
				updateTimeLineReadCount(uid, timeLineCount);
			}
			
			List<ObjectId> objectIds = (List<ObjectId>) object
					.get(DBConstants.F_ACTION_IDS);			
			
			if (timeLineCount < -nOffset) {
				int size = limit + nOffset + timeLineCount;
				if (size > 0 && size <= objectIds.size()) {
					return objectIds.subList(0, size);
				} else {
					log.info("<AllFeedManager>:uid = " + uid + " offset = "
							+ offset + ", limit = " + limit
							+ " timelineCount = " + timeLineCount);
					return null;
				}
			} else {
				return objectIds;
			}
		}
		return null;
	}

	public void addActionToFans(UserAction action,String appId) {
		FeedProcessor.getInstance().RegistAddProcess(action,appId);
	}

	public void deleteActionFromFans(UserAction action,String appId) {
		FeedProcessor.getInstance().RegistDeleteProcess(action,appId);
	}

	public long getNewFeedCount(MongoDBClient mongoClient, String userId) {
			
		
		DBObject query = new BasicDBObject();
		query.put(DBConstants.F_OWNER, new ObjectId(userId));
		
		DBObject returnFields = new BasicDBObject();
		returnFields.put(DBConstants.F_TIMELINE_COUNT, 1);
		returnFields.put(DBConstants.F_TIMELINE_READ_COUNT, 1);
		DBObject object = null;
		try {
			object = mongoClient.findOne(DBConstants.T_TIMELINE, query, returnFields);	
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		if (object == null) {
			return 0;
		}
		
		Object timelineCountObject = object.get(DBConstants.F_TIMELINE_COUNT);
		Object timelineReadCountObject = object.get(DBConstants.F_TIMELINE_READ_COUNT);
		
		int totalCount = 0;
		int readCount = 0;
		
		if (timelineCountObject == null){
			totalCount = 0;
		}
		else if (timelineCountObject instanceof Integer){
			totalCount = ((Integer) timelineCountObject).intValue();
		}
		else if (timelineCountObject instanceof Double){
			totalCount = ((Double) timelineCountObject).intValue();
		}
		
		if (timelineReadCountObject == null){
			readCount = 0;
		}
		else if (timelineReadCountObject instanceof Integer){
			readCount = ((Integer) timelineReadCountObject).intValue();
		}
		else if (timelineReadCountObject instanceof Double){
			readCount = ((Double) timelineReadCountObject).intValue();
		}
		
//		
//		Integer totalCount = (Integer) object.get(DBConstants.F_TIMELINE_COUNT);
//		Integer readCount = (Integer) object.get(DBConstants.F_TIMELINE_READ_COUNT);
//		if (totalCount == null) {
//			totalCount = 0;
//		}
//		if (readCount == null) {
//			readCount = 0;
//		}
		int count = totalCount - readCount;
		if (count < 0) {
			return 0;
		}
		return count;
	}

}
