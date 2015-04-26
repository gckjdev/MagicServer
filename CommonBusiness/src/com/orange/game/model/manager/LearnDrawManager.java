package com.orange.game.model.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import java.util.List;
import java.util.logging.Logger;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.LearnDraw;
import com.orange.game.model.dao.LearnDrawIndex;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.feed.FeedManager;
import com.orange.game.model.service.DataService;

public class LearnDrawManager extends CommonManager {

	public static final int SortTypeTime = 1;
	public static final int SortTypeBuyCount = 2;
	public static final int SortTypePrice = 3;

	public static void addDraw(MongoDBClient mongoClient, String drawId,
			int price, int type, int sellContentType) {
		
		
		
		LearnDraw query = new LearnDraw();
		query.setDrawId(drawId);
		query.setSellContentType(sellContentType);

		LearnDraw update = new LearnDraw();
		update.setDrawId(drawId);
		update.setPrice(price);
		update.setType(type);
		update.setSellContentType(sellContentType);

        DBObject up = new BasicDBObject("$set", update.getDbObject());

		mongoClient.updateOrInsert(DBConstants.T_LEARN_DRAW, query
				.getDbObject(), up);
	}

	public static boolean removeDraw(MongoDBClient mongoDBClient, String drawId, int sellContentType) {
		BasicDBObject query = new BasicDBObject(DBConstants.F_OPUS_ID, new ObjectId(drawId));
		query.put(DBConstants.F_SELL_CONTENT_TYPE, sellContentType);
		return mongoDBClient.remove(DBConstants.T_LEARN_DRAW, query);
	}

	public static void buyDraw(MongoDBClient mongoClient, String drawId,
			String userId, int sellContentType) {
		LearnDrawIndex learnDrawIndex = new LearnDrawIndex();
		learnDrawIndex.setCreateDate(new Date());
		learnDrawIndex.setDrawId(drawId);
		learnDrawIndex.setUserId(userId);
		learnDrawIndex.setSellContentType(sellContentType);
		mongoClient.insert(DBConstants.T_LEARN_DRAW_INDEX, learnDrawIndex
				.getDbObject());

		String field = DBConstants.F_BOUGHT_TIMES + "_" + sellContentType;
		BasicDBObject times = new BasicDBObject(field, 1);
		times.put(DBConstants.F_BOUGHT_TIMES, 1);
		DBObject update = new BasicDBObject("$inc", times);

		BasicDBObject query = new BasicDBObject (DBConstants.F_OPUS_ID, new ObjectId(drawId));
		query.put(DBConstants.F_SELL_CONTENT_TYPE, sellContentType);
		
		mongoClient.updateOne(DBConstants.T_LEARN_DRAW, query, update);
	}

	public static List<ObjectId> getDrawIdListByUserId(
			MongoDBClient mongoClient, String userId, int sellContentType) {

		return getDrawIdListByUserId(mongoClient, userId, sellContentType, 0, 2000);

	}

	public static List<UserAction> getBoughtOpusListByUserId(
			MongoDBClient mongoClient, String userId, int sellContentType, int offset, int limit) {
		List<ObjectId> learnDrawIds = getDrawIdListByUserId(mongoClient,
				userId, sellContentType, offset, limit);
		if (learnDrawIds == null || learnDrawIds.isEmpty()) {
			return Collections.emptyList();
		}

		List<UserAction> actionList = getLearnDrawDetailByIds(mongoClient,
				learnDrawIds);

		if (actionList == null || actionList.isEmpty()) {
			return Collections.emptyList();
		}

		sortActionsWithIds(actionList, learnDrawIds);
		List<LearnDraw> learnDraws = getLearnDrawByIds(mongoClient,
				learnDrawIds);
		updateActionsWithLearnDraw(actionList, learnDraws);
		return actionList;
	}

	public static List<UserAction> getOpusListByType(MongoDBClient mongoClient,
			int sellContentType, int type, int offset, int limit, int sortType) {
		// mongoClient.find(tableName, query, orderBy, offset, limit)
		List<LearnDraw> list = getLearnDrawListByType(mongoClient, sellContentType,  type,
				offset, limit, sortType);
		if (list == null || list.isEmpty()) {
			return Collections.emptyList();
		}

		List<ObjectId> ids = new ArrayList<ObjectId>(list.size());
		for (LearnDraw learnDraw : list) {
			ids.add(new ObjectId(learnDraw.getDrawId()));
		}
		List<UserAction> actions = getLearnDrawDetailByIds(mongoClient, ids);

		actions = sortActionsWithIds(actions, ids);
		updateActionsWithLearnDraw(actions, list);

		return actions;
	}

	private static List<ObjectId> getDrawIdListByUserId(
			MongoDBClient mongoClient, String userId, int sellContentType, int offset, int limit) {
		DBObject query = new BasicDBObject(DBConstants.F_UID, new ObjectId(
				userId));
		query.put(DBConstants.F_SELL_CONTENT_TYPE, sellContentType);
		
		DBObject orderBy = new BasicDBObject("_id", -1);
		DBCursor cursor = mongoClient.find(DBConstants.T_LEARN_DRAW_INDEX,
				query, orderBy, offset, limit);
		if (cursor != null) {
			List<ObjectId> list = new ArrayList<ObjectId>();
			while (cursor.hasNext()) {
				LearnDrawIndex lIndex = new LearnDrawIndex(cursor.next());
				list.add(lIndex.getDrawObjectId());
			}
			cursor.close();
			log.info("<getDrawIdListByUserId> list count = " + list.size());
			return list;
		}
		return Collections.emptyList();
	}

	private static List<LearnDraw> getLearnDrawByIds(MongoDBClient mongoClient,
			List<ObjectId> learnDrawIds) {

		DBObject fields = new BasicDBObject();
		fields.put("_id", 0);

		DBCursor cursor = mongoClient.findByFieldInValues(
				DBConstants.T_LEARN_DRAW, DBConstants.F_OPUS_ID, learnDrawIds,
				fields);

		if (cursor != null) {
			List<LearnDraw> list2 = new ArrayList<LearnDraw>(learnDrawIds
					.size());
			while (cursor.hasNext()) {
				LearnDraw draw = new LearnDraw(cursor.next());
				list2.add(draw);
			}
			cursor.close();
			return list2;
		}
		return Collections.emptyList();
	}

	private static void updateActionsWithLearnDraw(List<UserAction> actions,
			List<LearnDraw> list) {

		if (actions == null || list == null) {
			return;
		}

		Map<String, UserAction> map = new HashMap<String, UserAction>(actions
				.size());
		for (UserAction action : actions) {
			if (action == null) {
				continue;
			}
			String key = null;
			if (action.getObjectId() != null) {
				key = action.getObjectId().toString();
			}
			if (key != null) {
				map.put(key, action);
			}
		}
		for (LearnDraw learnDraw : list) {
			if (learnDraw == null) {
				continue;
			}
			String key = learnDraw.getDrawId();
			if (key != null) {
				UserAction action = map.get(key);
				if (action != null) {
					action.setLearnDraw(learnDraw);
				}
			}
		}
	}

	private static List<UserAction> sortActionsWithIds(
			List<UserAction> actions, List<ObjectId> ids) {
		Map<ObjectId, UserAction> map = new HashMap<ObjectId, UserAction>(
				actions.size());
		for (UserAction action : actions) {
			map.put(action.getObjectId(), action);
		}
		List<UserAction> actions2 = new ArrayList<UserAction>(actions.size());
		for (ObjectId oId : ids) {
			UserAction action = map.get(oId);
			actions2.add(action);
		}
		return actions2;
	}

	private static List<UserAction> getLearnDrawDetailByIds(
			MongoDBClient mongoClient, List<ObjectId> ids) {

		if (ids == null || ids.isEmpty()) {
			return Collections.emptyList();
		}
		log.info("<getLearnDrawDetailByIds> id list count = " + ids.size());

		DBObject fields = new BasicDBObject();
		fields.put(DBConstants.F_GUESS_WORD_LIST, 0);
		fields.put(DBConstants.F_USERID_LIST, 0);
		fields.put(DBConstants.F_OPUS_RELATED_USER_ID, 0);
		fields.put(DBConstants.F_DRAW_DATA, 0);

		// DBCursor cursor =
		// mongoClient.findByFieldInValues(DBConstants.T_ACTION,
		// "_id", ids, fields);

		List<Object> valueList = new ArrayList<Object>(ids.size());
		for (ObjectId oId : ids) {
			valueList.add(oId);
		}

		DBObject query = new BasicDBObject();
		query.put("_id", new BasicDBObject("$in", ids));
		log.info("Query = " + query);
		
		DBCursor cursor = mongoClient.findAll(DBConstants.T_OPUS, query, fields);

		if (cursor != null) {
			List<UserAction> list2 = new ArrayList<UserAction>(ids.size());
			while (cursor.hasNext()) {
				UserAction action = new UserAction(cursor.next());
				if (action != null) {
					list2.add(action);
				}
			}
			cursor.close();

			log.info("<getLearnDrawDetailByIds> user action list count = "
					+ list2.size());

			return list2;
		}
		return Collections.emptyList();
	}

	private static List<LearnDraw> getLearnDrawListByType(
			MongoDBClient mongoClient, int sellContentType, int type, int offset, int limit,
			int sortType) {
    		DBObject query = null;
       if(type != LearnDraw.LearnDrawTypeAll){
		    query = new BasicDBObject(DBConstants.F_TYPE, type);
        }else{
		    query = new BasicDBObject();
        }
       query.put(DBConstants.F_SELL_CONTENT_TYPE, sellContentType);
       
		DBObject returnFields = new BasicDBObject(DBConstants.F_CREATE_DATE, 0);
		DBObject orderBy = new BasicDBObject();
        if (sortType == SortTypePrice){
			orderBy.put(DBConstants.F_PRICE, 1);
        }else if (sortType == SortTypeBuyCount) {
			orderBy.put(DBConstants.F_BOUGHT_TIMES, -1);
		} else {
			orderBy.put("_id", -1);
		}
		DBCursor cursor = mongoClient.find(DBConstants.T_LEARN_DRAW, query,
				returnFields, orderBy, offset, limit);
		if (cursor != null) {
			List<LearnDraw> list = new ArrayList<LearnDraw>();
			while (cursor.hasNext()) {
				list.add(new LearnDraw(cursor.next()));
			}
			cursor.close();

			return list;
		}
		return Collections.emptyList();
	}
}
