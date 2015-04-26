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

public class RecommendFeedManager {
	private static RecommendFeedManager feedManager = new RecommendFeedManager();
	public static final Logger log = Logger.getLogger(RecommendFeedManager.class.getName());

	private RecommendFeedManager() {
		super();
	}

	public static RecommendFeedManager getInstance(){
		return feedManager;
	}

	public List<ObjectId> getFeedIds(MongoDBClient mongoClient, int language, int offset, int limit) {
		BasicDBObject query = new BasicDBObject();
		query.put(DBConstants.F_LANGUAGE, language);
		
		BasicDBObject orderBy = new BasicDBObject();
		orderBy.put(DBConstants.F_MODIFY_DATE, -1);
		log.info("<getFeedIds> query="+query.toString()+", orderBy="+orderBy+", offset="+offset+", limit="+limit);
		DBCursor cursor = mongoClient.find(DBConstants.T_RECOMMEND_OPUS, query, orderBy, offset, limit);
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

	public void recommendOpus(MongoDBClient mongoClient, String opusId, int language, String byUserId) {
		
		BasicDBObject query = new BasicDBObject();
		query.put(DBConstants.F_LANGUAGE, language);
		query.put(DBConstants.F_OPUS_ID, opusId);
		
		BasicDBObject update = new BasicDBObject();
		update.put(DBConstants.F_BY_USER_ID, byUserId);
		update.put(DBConstants.F_MODIFY_DATE, new Date());
		update.put(DBConstants.F_OPUS_ID, opusId);
		update.put(DBConstants.F_LANGUAGE, language);
		
		log.info("<recommendOpus> query="+query.toString()+", update="+update.toString());
		mongoClient.updateOrInsert(DBConstants.T_RECOMMEND_OPUS, query, update);
		
		// db.recommend_opus.ensureIndex({language:1, opus_id:1, m_date:-1})
	}

	public void unrecommendOpus(MongoDBClient mongoClient, String opusId,
			int language, String uid) {
		BasicDBObject query = new BasicDBObject();
		query.put(DBConstants.F_LANGUAGE, language);
		query.put(DBConstants.F_OPUS_ID, opusId);
		
		log.info("<unrecommendOpus> query="+query.toString()+", update="+query.toString());
		mongoClient.remove(DBConstants.T_RECOMMEND_OPUS, query);
		
	}
}
