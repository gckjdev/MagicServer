package com.orange.game.model.manager.feed;

import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.xiaoji.AbstractXiaoji;
import com.orange.game.model.xiaoji.XiaojiFactory;


/*
 * comment table
 * owner 
 * comment_ids
 * comment_count
 * comment_read_count
 * 
 */
public class CommentManager extends FeedManager {
	static CommentManager commentManager = null;

	CommentManager() {
		super();
	}

	public static CommentManager getInstance() {
		if (commentManager == null) {
			commentManager = new CommentManager();
		}
		return commentManager;
	}

	private void updateCommentReadCount (String uid, int value) {
		
//		MongoDBClient mongoClient = mongoDBClientHolder.get(); 
		
		DBObject query = new BasicDBObject();
		query.put(DBConstants.F_OWNER, new ObjectId(uid));
		
		DBObject update = new BasicDBObject();
		DBObject setObject = new BasicDBObject();
		setObject.put(DBConstants.F_COMMENT_COUNT, value);
		setObject.put(DBConstants.F_COMMENT_READ_COUNT, value);
		update.put("$set", setObject);
		
		mongoClient.updateOne(DBConstants.T_COMMENT, query, update);
	}
	
	public List<ObjectId> getCommentList(String uid, int offset, int limit) {
		if (StringUtil.isEmpty(uid)) {
			return null;
		}
		
//		MongoDBClient mongoClient = mongoDBClientHolder.get(); 
		
		String arrayField = DBConstants.F_COMMENT_IDS;
		DBObject query = new BasicDBObject();
		query.put(DBConstants.F_OWNER, new ObjectId(uid));
		int nOffset = -(offset + limit);
		DBObject object = mongoClient.findOneWithArrayLimit(
				DBConstants.T_COMMENT, query, arrayField, nOffset, limit, null);
		
		if (object != null) {
			int commentCount = (Integer) object
					.get(DBConstants.F_COMMENT_COUNT);
			if (offset == 0) {
				if (commentCount < 0) {
					commentCount = 0;
				}
				updateCommentReadCount(uid, commentCount);
			}
			
			List<ObjectId> objectIds = (List<ObjectId>) object
					.get(DBConstants.F_COMMENT_IDS);			
			
			if (commentCount < -nOffset) {
				int size = limit + nOffset + commentCount;
				if (size > 0 && size <= objectIds.size()) {
					return objectIds.subList(0, size);
				} else {
					log.info("<AllCommentManager>:uid = " + uid + " offset = "
							+ offset + ", limit = " + limit
							+ " timelineCount = " + commentCount);
					return null;
				}
			} else {
				return objectIds;
			}
		}
		return null;
	}

	public void addCommentToUsers(String commentId, List<ObjectId>userIds, String creator,AbstractXiaoji xiaoji)
	{
		
//		MongoDBClient mongoClient = mongoDBClientHolder.get(); 
		
		if (userIds == null || userIds.isEmpty()) {
			return;
		}
		for (ObjectId oid : userIds) {
			DBObject query = new BasicDBObject();
			query.put(DBConstants.F_OWNER, oid);
			DBObject update = new BasicDBObject();

			DBObject pushUpdate = new BasicDBObject();
			pushUpdate.put(DBConstants.F_COMMENT_IDS, new ObjectId(commentId));
			update.put("$push", pushUpdate);
			DBObject incUpdate = new BasicDBObject();
			incUpdate.put(DBConstants.F_COMMENT_COUNT, 1);
			if (oid.toString().equalsIgnoreCase(creator)) {
				incUpdate.put(DBConstants.F_COMMENT_READ_COUNT, 1);
			}
			update.put("$inc", incUpdate);
			mongoClient.upsertAll(DBConstants.T_COMMENT, query, update);
			
			xiaoji.commentTimelineManager().insertIndex(oid.toString(), commentId);
		}
	}
	

	public void removeCommentFromUsers(String commentId, List<ObjectId>userIds)
	{
//		MongoDBClient mongoClient = mongoDBClientHolder.get(); 
		
		if (userIds == null || userIds.isEmpty()) {
			return;
		}
		for (ObjectId oid : userIds) {
			DBObject query = new BasicDBObject();
			query.put(DBConstants.F_OWNER, oid);
			DBObject update = new BasicDBObject();
			DBObject pullUpdate = new BasicDBObject();
			pullUpdate.put(DBConstants.F_COMMENT_IDS, new ObjectId(commentId));
			update.put("$pull", pullUpdate);
			DBObject incUpdate = new BasicDBObject();
			incUpdate.put(DBConstants.F_COMMENT_COUNT, -1);
			update.put("$inc", incUpdate);
			log.info("remove comment, query = "+query+", update = "+update);
			mongoClient.updateOne(DBConstants.T_COMMENT, query, update);
		}
	}
	
	public long getNewCommentCount(MongoDBClient mongoClient, String userId) {
		
		DBObject query = new BasicDBObject();
		query.put(DBConstants.F_OWNER, new ObjectId(userId));
		
		DBObject returnFields = new BasicDBObject();
		returnFields.put(DBConstants.F_COMMENT_COUNT, 1);
		returnFields.put(DBConstants.F_COMMENT_READ_COUNT, 1);
		DBObject object = null;
		try {
			object = mongoClient.findOne(DBConstants.T_COMMENT, query, returnFields);	
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		if (object == null) {
			return 0;
		}
		Integer totalCount = (Integer) object.get(DBConstants.F_COMMENT_COUNT);
		Integer readCount = (Integer) object.get(DBConstants.F_COMMENT_READ_COUNT);
		if (totalCount == null) {
			totalCount = 0;
		}
		if (readCount == null) {
			readCount = 0;
		}
		int count = totalCount - readCount;
		if (count < 0) {
			return 0;
		}
		return count;
	}
	
	
}
