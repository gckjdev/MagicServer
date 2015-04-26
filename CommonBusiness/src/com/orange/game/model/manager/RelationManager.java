package com.orange.game.model.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.orange.game.model.dao.UserFriend;
import org.bson.types.ObjectId;


import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.Relation;
import com.orange.game.model.dao.User;
import com.orange.game.model.manager.friend.FriendManager;

public class RelationManager extends CommonManager {

	private static DBObject getFriendReturnObject() {
		DBObject object = new BasicDBObject();
		object.put(DBConstants.F_ITEMS, 0);
		object.put("fans", 0);
		object.put("follows", 0);
		return object;
	}

	private static List<User> getUserListByListOrder(MongoDBClient mongoClient,
			List<Object> uidList, DBObject returnFields) {
		// get the user list
		int limit = uidList.size();
		DBCursor cursor = mongoClient.findByFieldInValues(DBConstants.T_USER,
				DBConstants.F_USERID, uidList, returnFields, 0, limit);
		if (cursor != null) {
			HashMap<String, User> map = new HashMap<String, User>(limit);
			while (cursor.hasNext()) {
				DBObject obj = cursor.next();
				if (obj != null) {
					User friend = new User(obj);
					if (friend.getUserId() != null) {
						map.put(friend.getUserId(), friend);
					}
				}
			}
			cursor.close();

			// sort the user
			List<User> userList = new ArrayList<User>(limit);
			for (Object oId : uidList) {
				User friend = map.get(oId.toString());
				if (friend != null) {
					userList.add(friend);
				}
			}
			return userList;
		}
		return Collections.emptyList();
	}

	private static List<User> getFriendList(MongoDBClient mongoClient,
			boolean getFollowList, String userId, int type, int offset,
			int limit) {

		if (StringUtil.isEmpty(userId)) {
			return Collections.emptyList();
		}

		DBObject query = new BasicDBObject();
		String key = getFollowList ? DBConstants.F_UID : DBConstants.F_FRIENDID;
		query.put(key, new ObjectId(userId));
		query.put(DBConstants.F_TYPE, type);

		DBObject orderBy = new BasicDBObject(DBConstants.F_CREATE_DATE, -1);
		orderBy.put(DBConstants.F_FRIEND_GAME_STATUS, -1);

		DBCursor cursor = mongoClient.find(DBConstants.T_RELATION, query,
				orderBy, offset, limit);

		// get the list
		List<Object> list = Collections.emptyList();
		if (cursor != null) {
			list = new ArrayList<Object>(limit);
			while (cursor.hasNext()) {
				DBObject object = cursor.next();
				Relation relation = new Relation(object);
				ObjectId oId = getFollowList ? relation.getFid() : relation
						.getUid();
				if (oId != null) {
					list.add(oId);
				}
			}
			cursor.close();

			DBObject returnFields = getFriendReturnObject();

			log.info("list count = " + list.size());
			return getUserListByListOrder(mongoClient, list, returnFields);

		}

		return Collections.emptyList();
	}

	public static List<User> getFanList(MongoDBClient mongoClient,
			String userId, int offset, int limit) {
		List<User> list = getFriendList(mongoClient, false, userId,
				Relation.RELATION_TYPE_FOLLOW, offset, limit);
		if (offset == 0 && list != null && list.size() != 0) {
			UserManager.resetCount(mongoClient, userId,
					DBConstants.F_NEW_FAN_COUNT);
		}
		return list;
	}

	public static List<User> getFollowList(MongoDBClient mongoClient,
			String userId, int offset, int limit) {
		List<User> list = getFriendList(mongoClient, true, userId,
				Relation.RELATION_TYPE_FOLLOW, offset, limit);
		if (list == null) {
			return Collections.emptyList();
		}
		for (User user : list) {
			user.setRelation(Relation.RELATION_TYPE_FOLLOW);
		}
		return list;

	}
	
	public static List<User> getBlackFriendList(MongoDBClient mongoClient,
			String userId, int offset, int limit) {
		List<User> list = getFriendList(mongoClient, true, userId,
				Relation.RELATION_TYPE_BLACK, offset, limit);
		if (list == null) {
			return Collections.emptyList();
		}
		for (User user : list) {
			user.setRelation(Relation.RELATION_TYPE_BLACK);
		}
		return list;

	}

//    private static boolean  isUserFollowFriend(MongoDBClient mongoClient, String userId,
//			String fid) {
//		int type = getUserRelationType(mongoClient, userId, fid);
//		return type == Relation.RELATION_TYPE_FOLLOW;
//	}
//
//	static private boolean isUserBlackFriend(MongoDBClient mongoClient, String userId,
//			String fid) {
//		int type = getUserRelationType(mongoClient, userId, fid);
//		return type == Relation.RELATION_TYPE_BLACK;
//	}

//	static boolean isFriend(MongoDBClient mongoClient, String userId, String fid) {
//		boolean flag = isUserFollowFriend(mongoClient, userId, fid);
//		if (flag) {
//			return isUserFollowFriend(mongoClient, fid, userId);
//		}
//		return false;
//	}

	private static int getUserRelationType(MongoDBClient mongoClient,
			String userId, String fid) {

        int relation = Relation.RELATION_TYPE_NO;

        if (FriendManager.getFriendfollowmanager().hasRelation(userId, fid)){
            relation = relation | Relation.RELATION_TYPE_FOLLOW;
        }
        if (FriendManager.getFriendfansmanager().hasRelation(userId, fid)){
            relation = relation | Relation.RELATION_TYPE_FAN;
        }
        if (FriendManager.getFriendblackmanager().hasRelation(userId, fid)){
            relation = relation | Relation.RELATION_TYPE_BLACK;
        }

        return relation;

        /*
		
		log.info("userId= " + userId + ", fid = " + fid);

		DBObject query = new BasicDBObject();
		query.put(DBConstants.F_UID, new ObjectId(userId));
		query.put(DBConstants.F_FRIENDID, new ObjectId(fid));
		DBObject field = new BasicDBObject();
		field.put(DBConstants.F_TYPE, 1);
		DBObject object = mongoClient.findOne(DBConstants.T_RELATION, query,
				field);
		log.info("<getUserRelationType> query = " + query);
		if (object != null) {
			Relation relation = new Relation(object);
			log.info("<getUserRelationType> relation = " + object);
			return relation.getType();
		}
		return Relation.RELATION_TYPE_NO;
        */
	}

	static int userRelationWithFriend(MongoDBClient mongoClient, String userId,
			String fid) {
		
		if (userId == null || StringUtil.isEmpty(userId) 
				|| fid == null || StringUtil.isEmpty(fid)) {
			return Relation.RELATION_TYPE_NO;
		}



//		int type1 = getUserRelationType(mongoClient, userId, fid);
//		int type2 = getUserRelationType(mongoClient, fid, userId);
//		if (type2 == Relation.RELATION_TYPE_FOLLOW) {
//			type2 = Relation.RELATION_TYPE_FAN;
//		}
//		int type = type1 | type2;
		int type = getUserRelationType(mongoClient, userId, fid);
		log.info("<userRelationWithFriend> uid = " + userId + ", fid = " + fid
				+ ", type = "
				+ type);
		return type;
	}

	private static List<ObjectId> getAllFriendIDList(MongoDBClient mongoClient,
			String key, String value, String returnField, int type) {
		if (StringUtil.isEmpty(value)) {
			return Collections.emptyList();
		}

		DBObject query = new BasicDBObject();
		query.put(key, new ObjectId(value));
		query.put(DBConstants.F_TYPE, type);
		DBObject returnFields = new BasicDBObject(returnField, 1);

		DBCursor cursor = mongoClient.findAll(DBConstants.T_RELATION, query,
				returnFields);

		// get the list
		List<ObjectId> list = Collections.emptyList();
		if (cursor != null) {
			list = new ArrayList<ObjectId>();
			while (cursor.hasNext()) {
				DBObject object = cursor.next();
				ObjectId oId = null;
				if (object != null) {
					oId = (ObjectId) object.get(returnField);
					if (oId != null) {
						list.add(oId);
					}
				}
			}
			cursor.close();
		}
		return list;

	}

	public static List<ObjectId> getAllBlackFriendUids(MongoDBClient mongoClient,
			String userId) {
		return getAllFriendIDList(mongoClient, DBConstants.F_FRIENDID, userId,
				DBConstants.F_UID, Relation.RELATION_TYPE_BLACK);
	}	
	
	public static List<ObjectId> getAllFanUids(MongoDBClient mongoClient,
			String userId) {
		return getAllFriendIDList(mongoClient, DBConstants.F_FRIENDID, userId,
				DBConstants.F_UID, Relation.RELATION_TYPE_FOLLOW);
	}

	public static List<ObjectId> getAllFollowUids(MongoDBClient mongoClient,
			String userId) {
		return getAllFriendIDList(mongoClient, DBConstants.F_UID, userId,
				DBConstants.F_FRIENDID, Relation.RELATION_TYPE_FOLLOW);
	}

	public static List<String> getAllFollowStringUids(
			MongoDBClient mongoClient, String userId) {
		List<ObjectId> list = getAllFriendIDList(mongoClient,
				DBConstants.F_UID, userId, DBConstants.F_FRIENDID,
				Relation.RELATION_TYPE_FOLLOW);
		if (list != null) {
			List<String> retList = new ArrayList<String>(list.size());
			for (ObjectId oId : list) {
				retList.add(oId.toString());
			}
			return retList;
		}
		return Collections.emptyList();
	}
	
	public static void blackUser(MongoDBClient mongoClient,
			String userId, String targetBlackUserId, String appId) {

		if (targetBlackUserId == null) {
			return;
		}

		ObjectId oId = new ObjectId(targetBlackUserId);

		DBObject set = new BasicDBObject();
		DBObject query = new BasicDBObject();
		query.put(DBConstants.F_UID, new ObjectId(userId));
		query.put(DBConstants.F_FRIENDID, oId);

		set.put(DBConstants.F_CREATE_DATE, new Date());
		set.put(DBConstants.F_CREATE_SOURCE_ID, appId);
		set.put(DBConstants.F_TYPE, Relation.RELATION_TYPE_BLACK);

		mongoClient.upsertAll(DBConstants.T_RELATION, query,
				new BasicDBObject("$set", set));

		//insert black user index
		FriendManager.getFriendblackmanager().addFriend(userId, targetBlackUserId, appId);
		FriendManager.getFriendfollowmanager().removeFriend(userId, targetBlackUserId);
		log.info("<blackUser> query="+query.toString()+", update="+set.toString());
		return;
	}
	
	public static void unblackUser(MongoDBClient mongoClient,
			String userId, String targetBlackUserId, String appId) {

		if (targetBlackUserId == null) {
			return;
		}

		ObjectId oId = new ObjectId(targetBlackUserId);
		DBObject query = new BasicDBObject();
		query.put(DBConstants.F_UID, new ObjectId(userId));
		query.put(DBConstants.F_FRIENDID, oId);

		mongoClient.remove(DBConstants.T_RELATION, query);
		log.info("<unblackUser> query="+query.toString());
		//remove black user index
		FriendManager.getFriendblackmanager().removeFriend(userId, targetBlackUserId);
		return;
	}

	
	public static List<User> followUsers(MongoDBClient mongoClient,
			String userId, List<String> targetUserIdList, String appId) {

		if (targetUserIdList == null || targetUserIdList.isEmpty()) {
			return Collections.emptyList();
		}

		List<Object> uidList = new ArrayList<Object>(targetUserIdList.size());
		for (String uid : targetUserIdList) {
			ObjectId oId = new ObjectId(uid);
			uidList.add(oId);

			DBObject set = new BasicDBObject();
			DBObject query = new BasicDBObject();
			query.put(DBConstants.F_UID, new ObjectId(userId));
			query.put(DBConstants.F_FRIENDID, oId);

			set.put(DBConstants.F_CREATE_DATE, new Date());
			set.put(DBConstants.F_CREATE_SOURCE_ID, appId);
			set.put(DBConstants.F_TYPE, Relation.RELATION_TYPE_FOLLOW);
			set.put(DBConstants.F_FRIEND_GAME_STATUS,
					createDBObjectForUserGameStatus(
							DBConstants.C_GAME_STATUS_OFFLINE, "", -1));

			mongoClient.upsertAll(DBConstants.T_RELATION, query,
					new BasicDBObject("$set", set));
			
			//insert friend follow ,fans index
			FriendManager.getFriendfollowmanager().addFriend(userId, uid, appId);
			FriendManager.getFriendfansmanager().addFriend(uid, userId, appId);
			
		}

		if (uidList.isEmpty()) {
			return Collections.emptyList();
		}

		if (uidList.size() == 1) {
			String tid = uidList.get(0).toString();
			UserManager.incNewFanCount(mongoClient, tid);
		} else {
			UserManager.incNewFanCount(mongoClient, uidList);
		}

		DBObject returnFields = getFriendReturnObject();

		return getUserListByListOrder(mongoClient, uidList, returnFields);

	}

	public static int unFollowUsers(MongoDBClient mongoClient, String userId,
			List<String> targetUserIdList) {
		if (targetUserIdList == null || targetUserIdList.isEmpty()) {
			return 0;
		}
		int result = 0;
		for (String uid : targetUserIdList) {
			ObjectId oId = new ObjectId(uid);
			DBObject query = new BasicDBObject();
			query.put(DBConstants.F_UID, new ObjectId(userId));
			query.put(DBConstants.F_FRIENDID, oId);
			boolean flag = mongoClient.remove(DBConstants.T_RELATION, query);
			if (flag) {
				++result;
			}
			
			//remove friend follow,fans index
			FriendManager.getFriendfollowmanager().removeFriend(userId, uid);
			FriendManager.getFriendfansmanager().removeFriend(uid, userId);
		}
		return result;
	}

	public static BasicDBObject createDBObjectForUserGameStatus(int gameStatus,
			String serverId, int sessionId) {
		BasicDBObject values = new BasicDBObject();
		values.put(DBConstants.F_SERVER_ID, serverId);
		values.put(DBConstants.F_STATUS, gameStatus);
		values.put(DBConstants.F_SESSION_ID, sessionId);
		return values;
	}

	public static void updateUserGameStatus(MongoDBClient mongoClient,
			String userId, int gameStatus, String serverId, int sessionId) {
		if (userId == null)
			return;

		BasicDBObject query = new BasicDBObject();
		query.put(DBConstants.F_FRIENDID, new ObjectId(userId));

		BasicDBObject updateValue = new BasicDBObject();
		updateValue
				.put(DBConstants.F_FRIEND_GAME_STATUS,
						createDBObjectForUserGameStatus(gameStatus, serverId,
								sessionId));

		BasicDBObject update = new BasicDBObject();
		update.put("$set", updateValue);

		log.info("<updateUserGameStatus> query=" + query.toString()
				+ ",update=" + update.toString());
		mongoClient.updateAll(DBConstants.T_RELATION, query, update);
	}

	public static String getGameStatusStatusKey() {
		return DBConstants.F_FRIEND_GAME_STATUS.concat(".").concat(
				DBConstants.F_STATUS);
	}

	public static String getGameStatusSessionIdKey() {
		return DBConstants.F_FRIEND_GAME_STATUS.concat(".").concat(
				DBConstants.F_SESSION_ID);
	}

	public static String getGameStatusServerIdKey() {
		return DBConstants.F_FRIEND_GAME_STATUS.concat(".").concat(
				DBConstants.F_SERVER_ID);
	}

	public static List<Relation> getAllOnlineFollowUsers(
			MongoDBClient mongoClient, String userId, String serverId,
			int offset, int limit) {

		BasicDBObject query = new BasicDBObject();
		query.put(DBConstants.F_UID, new ObjectId(userId));
		query.put(getGameStatusStatusKey(), DBConstants.C_GAME_STATUS_ONLINE);
		query.put(getGameStatusServerIdKey(), serverId);

		BasicDBObject returnFields = new BasicDBObject();
		returnFields.put(getGameStatusSessionIdKey(), 1);

		log.info("<getAllOnlineFollowUsers> query=" + query.toString()
				+ ", returnFields=" + returnFields.toString());
		DBCursor cursor = mongoClient.find(DBConstants.T_RELATION, query,
				returnFields, null, offset, limit);
		if (cursor == null)
			return Collections.emptyList();

		List<Relation> retList = new ArrayList<Relation>();
		while (cursor.hasNext()) {
			DBObject obj = cursor.next();
			if (obj != null) {
				Relation friend = new Relation(obj);
				retList.add(friend);
			}
		}
		cursor.close();
		return retList;
	}

	public static void clearUserGameStatusByServerId(MongoDBClient mongoClient,
			String serverId) {
		if (serverId == null)
			return;

		BasicDBObject query = new BasicDBObject();
		query.put(getGameStatusServerIdKey(), serverId);

		BasicDBObject updateValue = new BasicDBObject();
		updateValue.put(DBConstants.F_FRIEND_GAME_STATUS,
				createDBObjectForUserGameStatus(
						DBConstants.C_GAME_STATUS_OFFLINE, null, -1));

		BasicDBObject update = new BasicDBObject();
		update.put("$set", updateValue);

		log.info("<clearUserGameStatusByServerId> query=" + query.toString()
				+ ",update=" + update.toString());
		mongoClient.updateAll(DBConstants.T_RELATION, query, update);
	}

	public static long getFanCount(MongoDBClient mongoClient, String appId,
			String userId) {
		DBObject query = new BasicDBObject();
		query.put(DBConstants.F_FRIENDID, new ObjectId(userId));
		query.put(DBConstants.F_TYPE,Relation.RELATION_TYPE_FOLLOW);
		// TODO performance of count is soso
		return mongoClient.count(DBConstants.F_RELATION, query);
	}

	public static long getFollowCount(MongoDBClient mongoClient, String appId,
			String userId) {		
		DBObject query = new BasicDBObject();
		query.put(DBConstants.F_UID, new ObjectId(userId));
		query.put(DBConstants.F_TYPE,Relation.RELATION_TYPE_FOLLOW);
		// TODO performance of count is soso
		return mongoClient.count(DBConstants.F_RELATION, query);
	}
	
	public static long getBlackCount(MongoDBClient mongoClient, String appId,
			String userId) {		
		DBObject query = new BasicDBObject();
		query.put(DBConstants.F_UID, new ObjectId(userId));
		query.put(DBConstants.F_TYPE,Relation.RELATION_TYPE_BLACK);
		// TODO performance of count is soso
		return mongoClient.count(DBConstants.F_RELATION, query);
	}

    public static UserFriend userFriendInfoWithFriend(MongoDBClient mongoClient, String userId, String targetUserId) {

        return null;  //To change body of created methods use File | Settings | File Templates.
    }
}
