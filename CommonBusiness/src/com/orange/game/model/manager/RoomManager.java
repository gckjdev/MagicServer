package com.orange.game.model.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.Room;
import com.orange.game.model.dao.RoomUser;

public class RoomManager extends CommonManager {
	public static final Logger log = Logger.getLogger(UserManager.class
			.getName());

	private static final String SERVER_ADDRESS = System
			.getProperty("friend_server.address");
	private static final String SERVER_PORT = "9100";

	private static void createUser2RoomIndex(MongoDBClient mongoClient,
			String userId, String roomId) {
		BasicDBObject userRoom = new BasicDBObject();
		userRoom.put(DBConstants.F_ROOM_USERID, userId);
		userRoom.put(DBConstants.F_ROOM_TYPE, Room.ROOM_TYPE_DRAW);

		DBObject push = new BasicDBObject();
		push.put(DBConstants.F_ROOM_LIST, roomId);
		DBObject update = new BasicDBObject();
		update.put("$push", push);
		mongoClient.updateOrInsert(DBConstants.T_USERROOM, userRoom, update);
	}

	private static void removeUser2RoomIndex(MongoDBClient mongoClient,
			String userId, String roomId) {
		BasicDBObject userRoom = new BasicDBObject();
		userRoom.put(DBConstants.F_ROOM_USERID, userId);
		userRoom.put(DBConstants.F_ROOM_TYPE, Room.ROOM_TYPE_DRAW);

		DBObject pull = new BasicDBObject();
		pull.put(DBConstants.F_ROOM_LIST, roomId);
		DBObject update = new BasicDBObject();
		update.put("$pull", pull);
		mongoClient.updateAll(DBConstants.T_USERROOM, userRoom, update);
	}

	private static List<String> getRoomIdsByUserId(MongoDBClient mongoClient,
			String userId, int offset, int limit) {
		DBObject query = new BasicDBObject();
		query.put(DBConstants.F_ROOM_USERID, userId);
		query.put(DBConstants.F_ROOM_TYPE, Room.ROOM_TYPE_DRAW);

		List<String> list = new ArrayList<String>();
		DBObject userRoom = mongoClient.findOneWithArrayLimit(
				DBConstants.T_USERROOM, query, DBConstants.F_ROOM_LIST, offset,
				limit,null);

		if (userRoom != null) {
			List<String> roomIdList = (List<String>) userRoom
					.get(DBConstants.F_ROOM_LIST);
			if (roomIdList != null && roomIdList.size() != 0) {
				for (int i = 0; i < roomIdList.size(); i++) {
					String roomId = roomIdList.get(i);
					if (!StringUtil.isEmpty(roomId)) {
						list.add(roomId);
					}
				}
			}
		}
		return list;
	}

	public static Room createRoom(MongoDBClient mongoClient, String userId,
			String userAvatar, String userNickName, String roomPassword,
			String roomName, String gender) {

		// TODO get the address and port from the configure file, and set the
		// address and port.

		BasicDBObject object = new BasicDBObject();
		Room room = new Room(object);
		room.setCreatorUserId(userId);
		room.setCreatorNickName(userNickName);

		room.setRoomName(roomName);
		room.setPassword(roomPassword);
		room.setStatus(Room.ROOM_WAITTING);

		room.setServerAddress(SERVER_ADDRESS);
		room.setServerPort(SERVER_PORT);

		room.setCreateDate(new Date());
		RoomUser user = new RoomUser(userId, userNickName, gender, userAvatar,
				RoomUser.STATUS_ACCEPTED);
		BasicDBList userList = new BasicDBList();
		userList.add(user.getDbObject());
		object.put(DBConstants.F_ROOM_USERS, userList);

		boolean flag = mongoClient.insert(DBConstants.T_ROOM, object);

		if (flag) {
			createUser2RoomIndex(mongoClient, userId, room.getRoomId());
			return room;// getRoomByRoomId(mongoClient, room.getRoomId());
		} else {
			return null;
		}

	}

	private static Room getRoomByRoomId(MongoDBClient mongoDBClient,
			String roomId) {
		DBObject obj = mongoDBClient.findOneByObjectId(DBConstants.T_ROOM,
				roomId);
		if (obj == null) {
			return null;
		}
		return new Room(obj);
	}

	private static BasicDBObject getQueryDbObjectByRoomId(String roomId) {
		BasicDBObject query = new BasicDBObject();
		query.put(DBConstants.F_OBJECT_ID, new ObjectId(roomId));
		return query;
	}

	private static void safePutArrayField(BasicDBObject object, String field,
			String subField, String value) {
		if (!StringUtil.isEmpty(value)) {
			String key = field + ".$." + subField;
			object.put(key, value);
		}

	}

	private static void safePutArrayField(BasicDBObject object, String field,
			String subField, Date value) {
		if (value != null) {
			String key = field + ".$." + subField;
			object.put(key, value);
		}
	}

	private static void safePutArrayField(BasicDBObject object, String field,
			String subField, int value) {
		if (value != RoomUser.STATUS_UNCHANGE) {
			String key = field + ".$." + subField;
			object.put(key, value);
		}
	}

	private static boolean isRoomCanUpdate(Room room, String userId,
			String roomPassword) {
		if (room != null) {
			if (room.getCreatorUserId().equalsIgnoreCase(userId)
					&& room.getPassword().equalsIgnoreCase(roomPassword)) {
				return true;
			}
		}
		return false;
	}

	public static boolean updateRoomUser(MongoDBClient mongoDBClient,
			String roomId, String userId, String gender, String nickName,
			String avatar, int status, Date lastPlayDate, boolean incPlayTimes) {

		if (StringUtil.isEmpty(roomId) || StringUtil.isEmpty(userId)) {
			return false;
		}
		BasicDBObject user = new BasicDBObject();

		String field = DBConstants.F_ROOM_USERS;
		safePutArrayField(user, field, DBConstants.F_GENDER, gender);
		safePutArrayField(user, field, DBConstants.F_AVATAR, avatar);
		safePutArrayField(user, field, DBConstants.F_NICKNAME, nickName);
		safePutArrayField(user, field, DBConstants.F_STATUS, status);
		safePutArrayField(user, field, DBConstants.F_LAST_PALY_DATE,
				lastPlayDate);

		BasicDBObject query = getQueryDbObjectByRoomId(roomId);
		query
				.put(DBConstants.F_ROOM_USERS + "." + DBConstants.F_USERID,
						userId);

		BasicDBObject update = new BasicDBObject();
		update.put("$set", user);
		if (incPlayTimes) {
			BasicDBObject obj = new BasicDBObject();
			obj.put(field + ".$." + DBConstants.F_PLAY_TIMES, 1);
			update.put("$inc", obj);
		}

		log.info("update room user, query=" + query.toString() + ", update="
				+ update.toString());
		mongoDBClient.upsertAll(DBConstants.T_ROOM, query, update);

		return true;
	}

	public static void resetRoomUser(MongoDBClient mongoDBClient, String roomId) {
		if (StringUtil.isEmpty(roomId)) {
			return;
		}

		BasicDBObject query = getQueryDbObjectByRoomId(roomId);
		query.put(DBConstants.F_ROOM_USERS.concat(".").concat(
				DBConstants.F_STATUS), RoomUser.STATUS_PLAYING);

		BasicDBObject value = new BasicDBObject();
		value.put(DBConstants.F_ROOM_USERS.concat("").concat(
				DBConstants.F_STATUS), RoomUser.STATUS_ACCEPTED);

		BasicDBObject update = new BasicDBObject();
		update.put("$set", value);

		log.info("reset room user, query=" + query.toString() + ", update="
				+ update.toString());
		mongoDBClient.updateAll(DBConstants.T_ROOM, query, update);
	}

	public static boolean updateRoom(MongoDBClient mongoDBClient,
			String userId, String userNickName, String roomId,
			String roomPassword, String roomName, String userAvatar,
			String gender) {
		Room room1 = getRoomByRoomId(mongoDBClient, roomId);
		if (!isRoomCanUpdate(room1, userId, roomPassword)) {
			return false;
		}

		BasicDBObject query = getQueryDbObjectByRoomId(roomId);
		BasicDBObject update = new BasicDBObject();
		Room room = new Room(update);

		if (!StringUtil.isEmpty(userNickName)) {
			room.setCreatorNickName(userNickName);
		}
		if (!StringUtil.isEmpty(roomPassword)) {
			room.setPassword(roomPassword);
		}
		if (!StringUtil.isEmpty(roomName)) {
			room.setRoomName(roomName);
		}
		// if (!StringUtil.isEmpty(userAvatar)) {
		// room.setAvatar(userAvatar);
		// }
		// if (!StringUtil.isEmpty(gender)) {
		// room.setGender(gender);
		// }

		BasicDBObject set = new BasicDBObject();

		set.put("$set", update);
		mongoDBClient.updateAll(DBConstants.T_ROOM, query, set);

		updateRoomUser(mongoDBClient, roomId, userId, gender, userNickName,
				userAvatar, RoomUser.STATUS_UNCHANGE, null, false);

		return true;
	}

	private static void increaseNewRoomCount(MongoDBClient mongoClient, String userId)
	{
		DBObject query = new BasicDBObject();
		query.put(DBConstants.F_ROOM_USERID, userId);
		
		DBObject update = new BasicDBObject();
		DBObject inc = new BasicDBObject();
		inc.put(DBConstants.F_NEW_ROOM_COUNT, 1);
		update.put("$inc", inc);
		mongoClient.updateAll(DBConstants.T_USERROOM, query, update);
	}
	
	private static void reSetNewRoomCount(MongoDBClient mongoClient, String userId)
	{
		DBObject query = new BasicDBObject();
		query.put(DBConstants.F_ROOM_USERID, userId);
		DBObject update = new BasicDBObject();
		DBObject set = new BasicDBObject();
		set.put(DBConstants.F_NEW_ROOM_COUNT, 0);
		update.put("$set", set);
		mongoClient.updateAll(DBConstants.T_USERROOM, query, update);
	}
	
	
	public static boolean inviteUsers(MongoDBClient mongoClient, String roomId,
			String userId, String roomPassword, Set<String> userIdList) {
		Room room = getRoomByRoomId(mongoClient, roomId);
		if (room == null || room.getRoomId() == null) {
			return false;
		}
		if (isRoomCanUpdate(room, userId, roomPassword)) {
			if (userIdList != null && userIdList.size() != 0) {
				BasicDBList users = new BasicDBList();
				for (String id : userIdList) {
					RoomUser user = new RoomUser(new BasicDBObject());
					String[] str = id.split(",");
					if (str.length >= 2) {
						user.setNickName(str[1]);
					}
					if (str.length >= 1) {
						user.setUserId(str[0]);
						user.setStatus(RoomUser.STATUS_INVITED);
						users.add(user.getDbObject());
						createUser2RoomIndex(mongoClient, str[0], roomId);
						increaseNewRoomCount(mongoClient, str[0]);
						
						// TODO, pass null for appId here, need to use correct appId for each user...
						DrawGamePushManager.sendMessage(room, str[0], null);
					}
				}
				BasicDBObject roomUser = new BasicDBObject();
				roomUser.put(DBConstants.F_ROOM_USERS, users);
				BasicDBObject query = getQueryDbObjectByRoomId(roomId);
				BasicDBObject update = new BasicDBObject();

				update.put("$pushAll", roomUser);
				mongoClient.updateAll(DBConstants.T_ROOM, query, update);
			}
		}
		return true;
	}

	private static List<Room> cursorToRooms(DBCursor cursor) {
		if (cursor == null || cursor.size() == 0) {
			return null;
		}
		ArrayList<Room> list = new ArrayList<Room>();
		while (cursor.hasNext()) {
			DBObject object = cursor.next();
			Room room = new Room(object);
			list.add(room);
		}
		cursor.close();
		return list;
	}

	public static List<Room> findRoomByUser(MongoDBClient mongoClient,
			String userId, int offset, int limit) {
		
		reSetNewRoomCount(mongoClient, userId);
		List<String> roomIds = getRoomIdsByUserId(mongoClient, userId, offset,
				limit);
		if (roomIds == null || roomIds.size() == 0) {
			return null;
		}
		List<Object> valueList = new ArrayList<Object>();
		for (String roomId : roomIds) {
			valueList.add(new ObjectId(roomId));
		}
		DBCursor cursor = mongoClient.findByFieldInValues(DBConstants.T_ROOM,
				DBConstants.F_OBJECT_ID, valueList, 0, limit);
		return cursorToRooms(cursor);
	}

	public static boolean isPassWordCorrect(MongoDBClient mongoClient,
			String roomId, String roomPassword) {
		Room room = getRoomByRoomId(mongoClient, roomId);
		if (room == null || room.getPassword() == null
				|| room.getRoomId() == null) {
			return false;
		}
		if (room.getRoomId().equalsIgnoreCase(roomId)
				&& room.getPassword().equalsIgnoreCase(roomPassword)) {
			return true;
		}
		return false;
	}

	public static List<Room> searchRoomByKeyWord(MongoDBClient mongoClient,
			String key, int offset, int limit) {
		BasicDBObject nickObject = new BasicDBObject();
		BasicDBObject roomName = new BasicDBObject();

		Pattern pattern = Pattern.compile(key, Pattern.CASE_INSENSITIVE);

//		nickObject.put(DBConstants.F_NICKNAME, pattern);
//		roomName.put(DBConstants.F_ROOM_NAME, pattern);

		BasicDBList list = new BasicDBList();
		list.add(nickObject);
		list.add(roomName);
		BasicDBObject query = new BasicDBObject();
//		query.put("$or", list);
		query.put(DBConstants.F_NICKNAME, key);

		
		DBObject order = new BasicDBObject();
		order.put("_id", -1);

		DBCursor cursor = mongoClient.find(DBConstants.T_ROOM, query, order,
				offset, limit);

		return cursorToRooms(cursor);
	}

	public static boolean removeRoom(MongoDBClient mongoClient, String roomId,
			String userId, String roomPassword) {
		Room room = getRoomByRoomId(mongoClient, roomId);
		if (room == null || room.getPassword() == null) {
			return false;
		}
		if (room.getPassword().equalsIgnoreCase(roomPassword)) {

			if (room.getCreatorUserId().equalsIgnoreCase(userId)) {
				// if the user is the creator, remove the room
				List<RoomUser> users = room.getRoomUsers();
				for (RoomUser user : users) {
					removeUser2RoomIndex(mongoClient, user.getUserId(), roomId);
				}
				return mongoClient.removeByObjectId(DBConstants.T_ROOM, roomId);
			} else {
				BasicDBObject query = new BasicDBObject();
				query.put(DBConstants.F_OBJECT_ID, new ObjectId(roomId));
				mongoClient.pullArrayKey(DBConstants.T_ROOM, query,
						DBConstants.F_ROOM_USERS, DBConstants.F_USERID, userId);
				removeUser2RoomIndex(mongoClient, userId, roomId);
			}
		}
		return true;
	}

	public static void addRoomUser(MongoDBClient mongoClient, String roomId,
			String userId, String gender, String userNickName, String userAvatar) {
		RoomUser user = new RoomUser(userId, userNickName, gender, userAvatar,
				RoomUser.STATUS_ACCEPTED);
		DBObject push = new BasicDBObject();
		push.put(DBConstants.F_ROOM_USERS, user.getDbObject());

		DBObject query = new BasicDBObject();
		DBObject update = new BasicDBObject();
		query.put(DBConstants.F_OBJECT_ID, new ObjectId(roomId));
		update.put("$push", push);
		mongoClient.updateAll(DBConstants.T_ROOM, query, update);
		createUser2RoomIndex(mongoClient, userId, roomId);
	}

	public static long getNewInviteRoomCount(MongoDBClient mongoClient,
			String userId) {
		
		DBObject query = new BasicDBObject();
		query.put(DBConstants.F_ROOM_USERID, userId);
		DBObject fields = new BasicDBObject();
		fields.put(DBConstants.F_NEW_ROOM_COUNT, 1);
		
		DBObject object = mongoClient.findOne(DBConstants.T_USERROOM, query, fields);
		if (object != null) {
			Integer value = (Integer) object.get(DBConstants.F_NEW_ROOM_COUNT);
			if (value != null) {
				return value.intValue();	
			}		
		}
		return 0;
	}
}
