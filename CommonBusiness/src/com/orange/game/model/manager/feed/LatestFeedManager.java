package com.orange.game.model.manager.feed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.UserAction;

@Deprecated
public class LatestFeedManager {
	
	private static LatestFeedManager feedManager = new LatestFeedManager();
	public static final Logger log = Logger.getLogger(LatestFeedManager.class.getName());

	private LatestFeedManager() {
		super();
	}

	public static LatestFeedManager getInstance(){
		return feedManager;
	}

	public List<ObjectId> getFeedIds(MongoDBClient mongoClient, int language, int offset, int limit) {
		BasicDBObject query = new BasicDBObject();
		query.put(DBConstants.F_LANGUAGE, language);
		
		BasicDBObject orderBy = new BasicDBObject();
		orderBy.put(DBConstants.F_MODIFY_DATE, -1);
		log.info("<getFeedIds> query="+query.toString()+", orderBy="+orderBy+", offset="+offset+", limit="+limit);
		DBCursor cursor = mongoClient.find(DBConstants.T_LATEST_FEED, query, orderBy, offset, limit);
		if (cursor == null){
			return Collections.emptyList();
		}
		
		List<ObjectId> retList = new ArrayList<ObjectId>();
		while (cursor.hasNext()){
			DBObject obj = cursor.next();
			if (obj != null){
				String opusId = (String)obj.get(DBConstants.F_OPUS_ID);
				if (opusId != null){
					retList.add(new ObjectId(opusId));
				}
			}
		}
		cursor.close();
		
		return retList;
	}

	public void insertLatestFeed(MongoDBClient mongoClient,
			UserAction userAction) {

		if (userAction == null || userAction.isContestDraw()) {
			return;
		}
		
		BasicDBObject query = new BasicDBObject();
		query.put(DBConstants.F_FOREIGN_USER_ID, userAction.getCreateUserId());
		query.put(DBConstants.F_LANGUAGE, userAction.getLanguage());
		
		BasicDBObject update = new BasicDBObject();
		update.put(DBConstants.F_FOREIGN_USER_ID, userAction.getCreateUserId());
		update.put(DBConstants.F_MODIFY_DATE, new Date());
		update.put(DBConstants.F_OPUS_ID, userAction.getObjectId().toString());
		update.put(DBConstants.F_LANGUAGE, userAction.getLanguage());
		
		mongoClient.updateOrInsert(DBConstants.T_LATEST_FEED, query, update);
		
		// db.latest_feed.ensureIndex({"modify_date":-1, "language":1});
	}
}
