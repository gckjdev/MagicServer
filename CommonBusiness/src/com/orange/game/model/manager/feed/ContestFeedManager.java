package com.orange.game.model.manager.feed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.UserAction;

public class ContestFeedManager {

	private static ContestFeedManager feedManager = new ContestFeedManager();
	public static final Logger log = Logger.getLogger(ContestFeedManager.class.getName());

	private ContestFeedManager() {
		super();
	}

	public static ContestFeedManager getInstance(){
		return feedManager;
	}

	public void insertContestFeed(MongoDBClient mongoClient,
			UserAction userAction) {
		
		if (userAction == null || userAction.isContestDraw() == false) {
			return;
		}

		try{
			// step1: insert contest index feed for my contest
			insertContestIndexForMyOpus(mongoClient, userAction);
			
			// step2: insert contest index for latest
			insertContestIndexForLatest(mongoClient, userAction);
		}
		catch (Exception e){
			log.error("<insertContestFeed> but catch exception="+e.toString(), e);
		}
	}

	private void insertContestIndexForLatest(MongoDBClient mongoClient,
			UserAction userAction) {
		
		String contestId = userAction.getContestId();
		
		BasicDBObject contestLatestIndexObject = new BasicDBObject();
		contestLatestIndexObject.put(DBConstants.F_CONTESTID, contestId);
		contestLatestIndexObject.put(DBConstants.F_OPUS_ID, userAction.getObjectId());
		
		log.info("<insertContestIndexForLatest> object="+contestLatestIndexObject.toString());
		mongoClient.insert(DBConstants.T_CONTEST_LATEST_OPUS_INDEX, contestLatestIndexObject);
		
		// ensureIndex({contest_id:1, opus_id:-1})
	}

	private void insertContestIndexForMyOpus(MongoDBClient mongoClient,
			UserAction userAction) {
				
		String userId = userAction.getCreateUserId();
		String contestId = userAction.getContestId();
		
		BasicDBObject query = new BasicDBObject();
		query.put(DBConstants.F_FOREIGN_USER_ID, userId);
		query.put(DBConstants.F_CONTESTID, contestId);
		
		BasicDBObject update = new BasicDBObject();

		BasicDBObject updateValue = new BasicDBObject();		
		updateValue.put(DBConstants.F_FOREIGN_USER_ID, userId);
		updateValue.put(DBConstants.F_CONTESTID, contestId);

		// {$push: "opus_id":"xxxx"}
		BasicDBObject pushValue = new BasicDBObject();
		pushValue.put(DBConstants.F_OPUS_ID_LIST, userAction.getObjectId());		
		
		update.put("$set", updateValue);
		update.put("$push", pushValue);
		
		log.info("<insertContestIndexForMyOpus> query="+query.toString()+", update="+update.toString());
		mongoClient.updateOrInsert(DBConstants.T_CONTEST_MY_OPUS_INDEX, query, update);
		
		// ensureIndex({user_id:1, contest_id:1})		
	}
	
	public List<ObjectId> getLatestContestOpusIds(MongoDBClient mongoClient, String contestId, int offset, int limit) {
		BasicDBObject query = new BasicDBObject();
		query.put(DBConstants.F_CONTESTID, contestId);
		
		BasicDBObject orderBy = new BasicDBObject();
		orderBy.put(DBConstants.F_OPUS_ID, -1);
		log.info("<getLatestContestOpusIds> query="+query.toString()+", orderBy="+orderBy+", offset="+offset+", limit="+limit);
		DBCursor cursor = mongoClient.find(DBConstants.T_CONTEST_LATEST_OPUS_INDEX, query, orderBy, offset, limit);
		if (cursor == null){
			return Collections.emptyList();
		}
		
		List<ObjectId> retList = new ArrayList<ObjectId>();
		while (cursor.hasNext()){
			DBObject obj = cursor.next();
			if (obj != null){
				ObjectId opusId = (ObjectId)obj.get(DBConstants.F_OPUS_ID);
				if (opusId != null){
					retList.add(opusId);
				}
			}
		}
		cursor.close();
		
		log.info("<getLatestContestOpusIds> return list="+retList.toString());				
		return retList;
	}
	
	public List<ObjectId> getContestUserOpusIds(MongoDBClient mongoClient, String contestId, String userId, int offset, int limit) {
		if (contestId == null || userId == null)
			return Collections.emptyList();
		
		BasicDBObject query = new BasicDBObject();
		query.put(DBConstants.F_FOREIGN_USER_ID, userId);
		query.put(DBConstants.F_CONTESTID, contestId);
		
//		BasicDBObject orderBy = new BasicDBObject();
//		orderBy.put(DBConstants.F_OPUS_ID, -1);
		log.info("<getContestUserOpusIds> query="+query.toString()+", offset="+offset+", limit="+limit);
		DBCursor cursor = mongoClient.find(DBConstants.T_CONTEST_MY_OPUS_INDEX, query, null, offset, limit);
		if (cursor == null){
			return Collections.emptyList();
		}
		
		List<ObjectId> retList = new ArrayList<ObjectId>();
		while (cursor.hasNext()){
			DBObject obj = cursor.next();
			if (obj != null){
				BasicDBList opusIdList = (BasicDBList)obj.get(DBConstants.F_OPUS_ID_LIST);
				if (opusIdList != null){
					for (Object opusId : opusIdList){
						retList.add((ObjectId)opusId);
					}
				}
			}
		}
		cursor.close();
		
		log.info("<getContestUserOpusIds> return list="+retList.toString());
		
		return retList;
	}	
}
