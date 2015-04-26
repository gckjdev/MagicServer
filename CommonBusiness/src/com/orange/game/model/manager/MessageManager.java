package com.orange.game.model.manager;

import java.util.*;

import com.mongodb.BasicDBList;
import com.orange.game.model.common.CommonMongoIdListManager;
import com.orange.game.model.dao.app.AbstractApp;
import com.orange.game.model.dao.app.AppFactory;
import com.orange.game.model.dao.group.Group;
import com.orange.game.model.manager.group.GroupManager;
import com.orange.game.model.manager.group.index.GroupUserIndexManager;
import com.orange.game.model.manager.message.UserMessageManager;
import com.orange.game.model.service.DBService;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.Message;
import com.orange.game.model.dao.MessageStat;
import com.orange.game.model.dao.User;

public class MessageManager extends CommonManager {

	public static final Logger log = Logger.getLogger(MessageManager.class
			.getName());

    public static Message creatMessage(MongoDBClient mongoClient,
                                       int messageType, String fromId, String toId, byte[] drawData,
                                       String text, double longitude, double latitude,
                                       String reqMessageId, int replyResult,
                                       String imageURL, String thumbImageURL, String appId,
                                       boolean showInSender){

        return creatMessage(mongoClient, messageType, fromId, toId,
                drawData, text, longitude, latitude, reqMessageId,
                replyResult, imageURL, thumbImageURL, false, appId, showInSender);

    }

	// return messageId
	public static Message creatMessage(final MongoDBClient mongoClient,
                                       final int messageType,
                                       final String fromId,
                                       final String toId,
                                       final byte[] drawData,
                                       final String text,
                                       final double longitude,
                                       final double latitude,
                                       final String reqMessageId,
                                       final int replyResult,
                                       final String imageURL,
                                       final String thumbImageURL,
                                       final boolean isGroup,
                                       final String appId,
                                       final boolean showInSender) {
		
		BasicDBObject messageDBObject = new BasicDBObject();
		messageDBObject.put("_id", new ObjectId());

		
		final Message message = new Message(messageDBObject);
		Date createDate = new Date();
		
		message.setType(messageType);
		message.setFrom(fromId);
		message.setTo(toId);
		message.setDrawData(drawData);
		message.setCreateDate(createDate);
		message.setText(text);
		message.setStatus(Message.MessageStatusUnread);
		message.setSenderDelFlag(Message.FLAG_NORMAL);
		message.setReceiverDelFlag(Message.FLAG_NORMAL);
		message.setImageURL(imageURL);
		message.setThumbImageURL(thumbImageURL);
        message.setIsGroup(isGroup);
		
		if (messageType == Message.MessageTypeLocationRequest) {
			message.setLatitude(latitude);
			message.setLongitude(longitude);
		} else if (messageType == Message.MessageTypeLocationResponse) {
			message.setLatitude(latitude);
			message.setLongitude(longitude);
			message.setReplyResult(replyResult);
			message.setReqMessageId(reqMessageId);
		}

		mongoClient.insert(DBConstants.T_MESSAGE, message.getDbObject());
		final String newMessageId = message.getMessageId();

        if (isGroup){
            // group message
            String groupId = toId;
            UserMessageManager.getInstance().insertGroupMessage(groupId, newMessageId, DBConstants.C_MESSAGE_GROUP);
        }
        else{
            // normal user P2P message

            // new cassandra index
            if (showInSender){
                UserMessageManager.getInstance().insertUserMessage(fromId, toId, newMessageId, DBConstants.C_MESSAGE_SENDER);
            }
            UserMessageManager.getInstance().insertUserMessage(toId, fromId, newMessageId, DBConstants.C_MESSAGE_RECEIVER);

            // insert two documents in table user_message
            if (showInSender){
                createUserMessageDoc(mongoClient, fromId, toId, createDate, newMessageId, DBConstants.C_MESSAGE_SENDER);
            }
            createUserMessageDoc(mongoClient, toId, fromId, createDate, newMessageId, DBConstants.C_MESSAGE_RECEIVER);
        }

		if (messageType == Message.MessageTypeLocationResponse) {
			updateLocationRequestMessage(mongoClient, reqMessageId,
					replyResult, newMessageId);
		}

        DBService.getInstance().executeDBRequest(2, new Runnable(){

            @Override
            public void run() {
                addMessageToMessageStatistic(mongoClient, fromId, toId, newMessageId,
                        text, messageType, isGroup, appId, message, showInSender);
            }
        });

		return message;
	}

	private static void createUserMessageDoc(MongoDBClient mongoDBClient, String userId, String relatedUserId, Date createDate,
			String newMessageId, int type) {
		
		BasicDBObject dbObject = new BasicDBObject();
		dbObject.put(DBConstants.F_FOREIGN_USER_ID, new ObjectId(userId));
		dbObject.put(DBConstants.F_RELATED_USER_ID, new ObjectId(relatedUserId));
		dbObject.put(DBConstants.F_USER_MESSAGE_ID, new ObjectId(newMessageId));
		dbObject.put(DBConstants.F_CREATE_DATE, createDate);
		dbObject.put(DBConstants.F_TYPE, type);
        dbObject.put(DBConstants.F_INDEX, true);
		
		mongoDBClient.insert(DBConstants.T_USER_MESSAGE, dbObject);
	}

	private static void updateLocationRequestMessage(MongoDBClient mongoClient,
			String reqMessageId, int replyResult, String newMessageId) {
		// update ask location message status
		BasicDBObject query = new BasicDBObject();
		BasicDBObject update = new BasicDBObject();
		BasicDBObject updateValue = new BasicDBObject();

		query.put("_id", new ObjectId(reqMessageId));
		updateValue.put(DBConstants.F_REPLY_RESULT, replyResult);
		updateValue.put(DBConstants.F_REPLY_MESSAGE_ID, newMessageId);
		update.put("$set", updateValue);
		mongoClient.updateOne(DBConstants.T_MESSAGE, query, update);

	}

	private static void addMessageToMessageStatistic(MongoDBClient mongoClient,
                                                     String fromId,
                                                     String toId,
                                                     String messageId,
                                                     String text,
                                                     int messageType,
                                                     boolean group,
                                                     String appId,
                                                     Message message,
                                                     boolean showInSender) {

        if (showInSender){
            addMessageStatToUser(mongoClient, fromId, toId, messageId,
                    text, ServiceConstant.CONST_MESSAGE_DIRECTION_SEND, messageType, group);
        }

        if (group){
            // add to group memeber user's stat table and PUSH message to group users
            String groupId = toId;
//            List<ObjectId> userIdList = GroupManager.getAllGroupMemberAndGuestIdList(mongoClient, groupId);

            Group userGroup = GroupManager.getSimpleGroup(mongoClient, groupId);
            if (userGroup == null){
                return;
            }

            List<ObjectId> userIdToNoticeList = GroupManager.getAllGroupMemberAndGuestIdList(mongoClient, userGroup);
            Set<String> offUserIds = new HashSet<String>();
            offUserIds.addAll(userGroup.getOffUsers());

            log.info("<addMessageToMessageStatistic> userIdList="+userIdToNoticeList.toString());
            Set<String> userForPushSet = MessageManager.getNoNewMessageUser(mongoClient, userIdToNoticeList);
            log.info("<addMessageToMessageStatistic> userForPush="+userForPushSet.toString());
            log.info("<addMessageToMessageStatistic> offUsers="+offUserIds.toString());

            userIdToNoticeList.remove(new ObjectId(fromId));

            Set<String> userForNoticeSet = new HashSet<String>();
            for (ObjectId id : userIdToNoticeList){
                String recvUserId = id.toString();
                boolean isIncNewCount = (offUserIds.contains(recvUserId) == false);
                if (isIncNewCount){
                    userForNoticeSet.add(id.toString());
                }
            }

            // batch handling
            HashSet<String> batchHandleUserIdSet = new HashSet<String>();
            Set<String> hasMessageStatUserIdList = getMessageStatList(mongoClient, groupId);
            for (String id : userForNoticeSet){
                if (hasMessageStatUserIdList.contains(id)){
                    batchHandleUserIdSet.add(id);
                }
            }
            addMessageStatToUserList(mongoClient, batchHandleUserIdSet, groupId, messageId, text,
                    ServiceConstant.CONST_MESSAGE_DIRECTION_RECIEVE, messageType, group, true);

            // handle one by one
            for (ObjectId id : userIdToNoticeList){
                String recvUserId = id.toString();
                boolean isIncNewCount = (offUserIds.contains(recvUserId) == false);

                if (batchHandleUserIdSet.contains(recvUserId)){
                    // already processed in batch operation, skip
                    continue;
                }
                else{
                    // update receiver message user stat
                    addMessageStatToUser(mongoClient, recvUserId, groupId, messageId, text,
                            ServiceConstant.CONST_MESSAGE_DIRECTION_RECIEVE, messageType, group, isIncNewCount);
                }

                // push message notification, only for those user has no new message
                if (userForPushSet.contains(recvUserId) && !offUserIds.contains(recvUserId)){
                    // send push
                    DrawGamePushManager.sendMessage(message, appId);
                }
            }


        }
        else{
            // update receiver message user stat
            addMessageStatToUser(mongoClient, toId, fromId, messageId, text,
                    ServiceConstant.CONST_MESSAGE_DIRECTION_RECIEVE, messageType, group);

            DrawGamePushManager.sendMessage(message, appId);
        }

	}

    private static Set<String> getMessageStatList(MongoDBClient mongoClient, String groupId) {

        BasicDBObject query = new BasicDBObject();
        query.put(DBConstants.F_FRIENDID, groupId);
        query.put(DBConstants.F_IS_GROUP, true);

        DBCursor cursor = mongoClient.findAll(DBConstants.T_MESSAGE_STATISTIC, query, null);
        if (cursor == null){
            return Collections.emptySet();
        }

        Set<String> retSet = new HashSet<String>();
        while (cursor.hasNext()){
            BasicDBObject obj = (BasicDBObject)cursor.next();
            String userId = obj.getString(DBConstants.F_MESSAGE_USER_ID);
            if (!StringUtil.isEmpty(userId)){
                retSet.add(userId);
            }
        }
        cursor.close();
        return retSet;  //To change body of created methods use File | Settings | File Templates.
    }

    private static void addMessageStatToUser(MongoDBClient mongoClient,
                                             String userId, String friendUserId, String messageId, String text,
                                             int direction, int messageType, boolean group) {

        addMessageStatToUser(mongoClient,
                userId, friendUserId, messageId, text,
                direction, messageType, group, true);
    }

    private static void addMessageStatToUserList(MongoDBClient mongoClient,
                                             Set<String> userIdSet, String friendUserId, String messageId, String text,
                                             int direction, int messageType, boolean group, boolean incNewCount) {

        if (userIdSet.size() == 0){
            log.info("<addMessageStatToUserList> but userIdSet is empty");
            return;
        }

        BasicDBList inList = new BasicDBList();
        inList.addAll(userIdSet);

        DBObject query = new BasicDBObject(DBConstants.F_MESSAGE_USER_ID, new BasicDBObject("$in", inList));
        query.put(DBConstants.F_FRIENDID, friendUserId);

        int newMessageCount = (direction == ServiceConstant.CONST_MESSAGE_DIRECTION_RECIEVE) ? 1
                : 0;

        BasicDBObject dataToSet = new BasicDBObject();
        dataToSet.put(DBConstants.F_MESSAGE_CONTENT, text);
        dataToSet.put(DBConstants.F_LATEST_MSG, messageId);
        dataToSet.put(DBConstants.F_MODIFY_DATE, new Date());
        dataToSet.put(DBConstants.F_MESSAGE_DIRECTION, direction);
        dataToSet.put(DBConstants.F_TYPE, messageType);
        dataToSet.put(DBConstants.F_IS_GROUP, group);

        BasicDBObject dataToIncBasicDBObject = new BasicDBObject();
        dataToIncBasicDBObject.put(DBConstants.F_NEW_MSG_COUNT, incNewCount ? newMessageCount : 0);

        if (group){
            dataToIncBasicDBObject.put(DBConstants.F_NEW_GROUP_MSG_COUNT, newMessageCount);
        }

        dataToIncBasicDBObject.put(DBConstants.F_TOTAL_MSG_COUNT, 1);

        BasicDBObject updater = new BasicDBObject();
        updater.put("$inc", dataToIncBasicDBObject);
        updater.put("$set", dataToSet);

        log.info("<MessageManager> update statistic begin, query=" + query + ", update=" + updater);
        mongoClient.upsertAll(DBConstants.T_MESSAGE_STATISTIC, query, updater);
        log.info("<MessageManager> update statistic finish, query=" + query + ", update=" + updater);

    }

	private static void addMessageStatToUser(MongoDBClient mongoClient,
			String userId, String friendUserId, String messageId, String text,
			int direction, int messageType, boolean group, boolean incNewCount) {
		DBObject query = new BasicDBObject(DBConstants.F_MESSAGE_USER_ID,
				userId);
		query.put(DBConstants.F_FRIENDID, friendUserId);

		int newMessageCount = (direction == ServiceConstant.CONST_MESSAGE_DIRECTION_RECIEVE) ? 1
				: 0;

		BasicDBObject dataToSet = new BasicDBObject();
		dataToSet.put(DBConstants.F_MESSAGE_CONTENT, text);
		dataToSet.put(DBConstants.F_LATEST_MSG, messageId);
		dataToSet.put(DBConstants.F_MODIFY_DATE, new Date());
		dataToSet.put(DBConstants.F_MESSAGE_DIRECTION, direction);
		dataToSet.put(DBConstants.F_TYPE, messageType);
        dataToSet.put(DBConstants.F_IS_GROUP, group);

		BasicDBObject dataToIncBasicDBObject = new BasicDBObject();
		dataToIncBasicDBObject.put(DBConstants.F_NEW_MSG_COUNT, incNewCount ? newMessageCount : 0);

        if (group){
            dataToIncBasicDBObject.put(DBConstants.F_NEW_GROUP_MSG_COUNT, newMessageCount);
        }

		dataToIncBasicDBObject.put(DBConstants.F_TOTAL_MSG_COUNT, 1);

		BasicDBObject updater = new BasicDBObject();
		updater.put("$inc", dataToIncBasicDBObject);
		updater.put("$set", dataToSet);

		mongoClient.upsertAll(DBConstants.T_MESSAGE_STATISTIC, query, updater);
		log.info("<MessageManager> update single user statistic " + query + " for " + updater);

	}

	@Deprecated
	/*
	 * used in the old version(below 5.0) api
	 */
	public static List<Message> getUserMessage(MongoDBClient mongoClient,
			String userId, String friendUserId, int startOffset, int maxCount) {
		List<Message> list = new ArrayList<Message>();
		List<DBObject> queryList = new ArrayList<DBObject>();

		BasicDBObject reverseQuery = new BasicDBObject();
		reverseQuery.put(DBConstants.F_FROM_USERID, friendUserId);
		reverseQuery.put(DBConstants.F_TO_USERID, userId);
		reverseQuery.put(DBConstants.F_STATUS, Message.MessageStatusUnread);

		queryList.add(reverseQuery);

		BasicDBObject order = new BasicDBObject(DBConstants.F_CREATE_DATE, -1);

		DBCursor cursor = mongoClient.find(DBConstants.T_MESSAGE, reverseQuery,
				order, startOffset, maxCount);

		if (cursor != null) {
			while (cursor.hasNext()) {
				DBObject obj = cursor.next();
				list.add(new Message(obj));
			}
			cursor.close();
		}

		return list;
	}

	public static void readFriendMessage(MongoDBClient mongoClient,
			String userId, String friendUserId) {

        /* rem by Benson since READ STATUS is NOT used at all and the following code is NOT efficient to DB
		DBObject query = new BasicDBObject();
		query.put(DBConstants.F_TO_USERID, userId);
		query.put(DBConstants.F_FROM_USERID, friendUserId);
		query.put(DBConstants.F_STATUS, Message.MessageStatusUnread);

		DBObject obj = new BasicDBObject(DBConstants.F_STATUS,
				Message.MessageStatusRread);

		BasicDBObject update = new BasicDBObject("$set", obj);
		log.info("<readFriendMessage> message query = " + query + "update = "
				+ update);

		mongoClient.updateAll(DBConstants.T_MESSAGE, query, update);
		*/

		BasicDBObject queryForStat = new BasicDBObject();
		queryForStat.put(DBConstants.F_MESSAGE_USER_ID, userId);
		queryForStat.put(DBConstants.F_FRIENDID, friendUserId);

		BasicDBObject updateForStat = new BasicDBObject(DBConstants.F_NEW_MSG_COUNT, 0);
        updateForStat.put(DBConstants.F_NEW_GROUP_MSG_COUNT, 0);

		log.info("<readFriendMessage> message_stat query = " + queryForStat);
		mongoClient.updateAll(DBConstants.T_MESSAGE_STATISTIC, queryForStat, new BasicDBObject("$set", updateForStat));
	}


	public static long getNewMessageCount(MongoDBClient mongoClient,
			String userId) {
		DBObject query = new BasicDBObject();
		query.put(DBConstants.F_MESSAGE_USER_ID, userId);
		query.put(DBConstants.F_NEW_MSG_COUNT, new BasicDBObject("$gt", 0));
		return mongoClient.count(DBConstants.T_MESSAGE_STATISTIC, query);
	}

    public static Set<String> getNoNewMessageUser(MongoDBClient mongoClient, List<ObjectId> userIdList) {

        if (userIdList == null || userIdList.size() == 0){
            return Collections.emptySet();
        }

        DBObject query = new BasicDBObject();

        BasicDBList list = new BasicDBList();
        for (ObjectId id : userIdList){
            list.add(id.toString());
        }
        BasicDBObject inQuery = new BasicDBObject("$in", list);

        query.put(DBConstants.F_MESSAGE_USER_ID, inQuery);
        query.put(DBConstants.F_NEW_MSG_COUNT, new BasicDBObject("$gt", 0));

        DBCursor cursor = mongoClient.findAll(DBConstants.T_MESSAGE_STATISTIC, query, null);
        if (cursor == null){
            return Collections.emptySet();
        }

        HashSet<String> set = new HashSet<String>();
        while (cursor.hasNext()){
            BasicDBObject obj = (BasicDBObject)cursor.next();
            String userId = obj.getString(DBConstants.F_MESSAGE_USER_ID);
            if (userId != null){
                set.add(userId);
            }
        }

        cursor.close();
        return set;
    }

	public static void deleteMessageStat(MongoDBClient mongoClient,
			String userId, String targetUserId) {

		DBObject queryForMessageStat = new BasicDBObject();
		queryForMessageStat.put(DBConstants.F_MESSAGE_USER_ID, userId);
		queryForMessageStat.put(DBConstants.F_FRIENDID, targetUserId);
		mongoClient.removeOne(DBConstants.T_MESSAGE_STATISTIC,
				queryForMessageStat);
	}

	public static void deleteMessage(MongoDBClient mongoClient, String userId, String targetUserId,
			List<String> messageIdList) {
		
		List<ObjectId> messageObjectIdList = new ArrayList<ObjectId>(
				messageIdList.size());
		for (String messageIdString : messageIdList) {
			messageObjectIdList.add(new ObjectId(messageIdString));

            // remove new index in cassandra
            UserMessageManager.getInstance().deleteUserMessage(userId, targetUserId, messageIdString);
		}

		DBObject query = new BasicDBObject(DBConstants.F_FOREIGN_USER_ID, new ObjectId(userId));
		query.put(DBConstants.F_USER_MESSAGE_ID, new BasicDBObject("$in",messageObjectIdList));
		mongoClient.remove(DBConstants.T_USER_MESSAGE, query);
	}

	public static long countMessage(MongoDBClient mongoClient, String appId) {
		return mongoClient.count(DBConstants.T_MESSAGE, new BasicDBObject());
	}

	public static List<MessageStat> getMessageStatList(
			MongoDBClient mongoClient, String userId, String appId, int offset,
			int limit) {
		BasicDBObject query = new BasicDBObject(DBConstants.F_MESSAGE_USER_ID,
				userId);
		BasicDBObject order = new BasicDBObject(DBConstants.F_NEW_MSG_COUNT, -1);
        order.put(DBConstants.F_MODIFY_DATE, -1);

		DBCursor cursor = mongoClient.find(DBConstants.T_MESSAGE_STATISTIC,
				query, order, offset, limit);

		List<MessageStat> list = null;
		if (cursor != null) {
			list = new ArrayList<MessageStat>();
			while (cursor.hasNext()) {
				DBObject obj = cursor.next();
				if (obj != null) {
					list.add(new MessageStat(obj));
				}
			}
			cursor.close();
		}
		// update the friend avatar and nick name
		updateMessageStatListFriendInfo(mongoClient, list);

        // update the group avatar and nick name
        updateMessageStatListGroupInfo(mongoClient, list);

        // update the last message
		updateMessageStatListMessageInfo(mongoClient, list);
		return list;
	}

	private static void updateMessageStatListMessageInfo(
			MongoDBClient mongoClient, List<MessageStat> list) {
		// TODO set the last message info.
	}

    public static void updateMessageStatListGroupInfo(
            MongoDBClient mongoClient, List<MessageStat> list) {

        List<ObjectId> fidList = new ArrayList<ObjectId>(list.size());
        HashMap<String, MessageStat> map = new HashMap<String, MessageStat>(
                list.size());
        for (MessageStat stat : list) {
            String fid = stat.getFriendUserId();
            if (!StringUtil.isEmpty(fid) && stat.isGroup()) {
                fidList.add(new ObjectId(fid));
                map.put(fid, stat);
            }
        }

        if (fidList.size() == 0){
            return;
        }

        DBObject returnFields = new BasicDBObject();
        returnFields.put(DBConstants.F_NAME, 1);
        returnFields.put(DBConstants.F_IMAGE, 1);
        Set<Group> groups = GroupManager.getSimpleGroupsByIds(mongoClient, fidList, returnFields);

        for (Group group : groups){
            MessageStat stat = map.get(group.getGroupId());
            if (stat != null) {
                stat.setFriendAvatar(group.getMedalImage());
                stat.setFriendNickName(group.getName());
            }
        }
    }


    public static void updateMessageStatListFriendInfo(
			MongoDBClient mongoClient, List<MessageStat> list) {
		List<Object> fidList = new ArrayList<Object>(list.size());
		HashMap<String, MessageStat> map = new HashMap<String, MessageStat>(
				list.size());
		for (MessageStat stat : list) {
			String fid = stat.getFriendUserId();
			if (!StringUtil.isEmpty(fid)) {
				fidList.add(new ObjectId(fid));
				map.put(fid, stat);
			}
		}

        if (fidList.size() == 0)
            return;

		DBObject returnFields = new BasicDBObject();
		returnFields.put(DBConstants.F_AVATAR, 1);
		returnFields.put(DBConstants.F_NICKNAME, 1);
		returnFields.put(DBConstants.F_GENDER, 1);
        returnFields.put(DBConstants.F_VIP, 1);
        returnFields.put(DBConstants.F_VIP_EXPIRE_DATE, 1);
		DBCursor cursor = mongoClient.findByFieldInValues(DBConstants.T_USER,
				DBConstants.F_USERID, fidList, returnFields);
		if (cursor != null) {
			while (cursor.hasNext()) {
				DBObject object = cursor.next();
				if (object != null) {
					User user = new User(object);
					MessageStat stat = map.get(user.getUserId());
					if (stat != null) {
						stat.setFriendAvatar(user.getAvatar());
						stat.setFriendGender(user.getGender());
						stat.setFriendNickName(user.getNickName());
                        stat.setFriendVip(user.getFinalVip());
					}
				}
			}
			map = null;
			fidList = null;
			cursor.close();
		}
	}

	private static Date getMessageCreateDate(MongoDBClient mongoClient,
			String messageId) {
		if (!StringUtil.isEmpty(messageId)) {
			DBObject fields = new BasicDBObject(DBConstants.F_CREATE_DATE, 1);
			DBObject result = mongoClient.findOneByObjectId(
					DBConstants.T_MESSAGE, messageId, fields);
			if (result != null) {
				return new Message(result).getCreateDate();
			}
		}
		return null;
	}

	public static List<Message> getMessageList(MongoDBClient mongoClient,
			String userId, String friendUserId, String offsetMessageId,
			int limit, boolean forward) {

		Date date = getMessageCreateDate(mongoClient, offsetMessageId);
		log.info("date = "+date);
		if (date == null && !forward) {
			return Collections.emptyList();
		}

		BasicDBObject query = new BasicDBObject();
		query.put(DBConstants.F_FOREIGN_USER_ID, new ObjectId(userId));
		query.put(DBConstants.F_RELATED_USER_ID, new ObjectId(friendUserId));

		// date range
		if (forward) {
			if (date != null) {
				query.put(DBConstants.F_CREATE_DATE,new BasicDBObject("$gt",date));
			}
		} else {
			query.put(DBConstants.F_CREATE_DATE, new BasicDBObject("$lt",
					date));
		}

		
//		// send
//		BasicDBObject query = new BasicDBObject();
//		query.put(DBConstants.F_FROM_USERID, userId);
//		query.put(DBConstants.F_TO_USERID, friendUserId);
//		query.put(DBConstants.F_SENDER_DEL_FLAG, Message.FLAG_NORMAL);
//		// receive
//		BasicDBObject reverseQuery = new BasicDBObject();
//		reverseQuery.put(DBConstants.F_FROM_USERID, friendUserId);
//		reverseQuery.put(DBConstants.F_TO_USERID, userId);
//		reverseQuery.put(DBConstants.F_RECEIVER_DEL_FLAG, Message.FLAG_NORMAL);

//		List<DBObject> queryList = new ArrayList<DBObject>();
//
//		queryList.add(query);
////		queryList.add(reverseQuery);
//
//		BasicDBObject queryBoth = new BasicDBObject("$or", queryList);


		log.info("<getMessageList> query = " + query);

		DBObject orderBy = new BasicDBObject(DBConstants.F_CREATE_DATE, -1);

		DBCursor cursor = mongoClient.find(DBConstants.T_USER_MESSAGE, query,
				orderBy, 0, limit);

		if (cursor != null) {
			List<ObjectId> messageIdList = new ArrayList<ObjectId>();
			while (cursor.hasNext()) {
				DBObject object = cursor.next();
				if (object != null) {
					messageIdList.add((ObjectId) object.get(DBConstants.F_USER_MESSAGE_ID));
				}
			}
            cursor.close();

			if ( messageIdList.size() > 0 ) {
				DBObject messageQuery = new BasicDBObject();
				messageQuery.put(DBConstants.F_MESSAGE_ID, new BasicDBObject("$in",messageIdList));
				
				DBCursor messageCursor = mongoClient.find(DBConstants.T_MESSAGE,messageQuery, orderBy, 0, limit);
				if (messageCursor != null) {
					List<Message> messageList = new ArrayList<Message>();
					while (messageCursor.hasNext()) {
						DBObject object = messageCursor.next();
						if (object != null) {
							messageList.add(new Message(object));
						}
					}
                    messageCursor.close();
					return messageList;
			    }
		     }
	    }
		
		return Collections.emptyList();
  }
	
	
	public static String DRAW_CUSTOMER_SERVICER_ID = "888888888888888888888888";
	public static String DICE_CUSTOMER_SERVICER_ID = "888888888888888888888889";
	public static String ZJH_CUSTOMER_SERVICER_ID = "888888888888888888888889";

    public static void sendSystemMessage(MongoDBClient dbClient, String toUserId, String message, String appId, boolean showInSender){
        if (toUserId == null || message == null)
            return;

        String fromUserId = null;
        fromUserId = DRAW_CUSTOMER_SERVICER_ID;

        MessageManager.creatMessage(dbClient, Message.MessageTypeText, fromUserId, toUserId,
                null, message, 0.0, 0.0, null, 0, null, null, false, appId, showInSender);
    }

	public static void sendSystemMessage(MongoDBClient dbClient, String toUserId, String message, String gameId, String appId, boolean showInSender){
		if (toUserId == null || message == null || gameId == null) 
			return;			
		
		String fromUserId = null;
		if (gameId.equalsIgnoreCase(DBConstants.GAME_ID_DRAW)){
			fromUserId = DRAW_CUSTOMER_SERVICER_ID;
		}
		else{
			fromUserId = DICE_CUSTOMER_SERVICER_ID; 
		}
		
		MessageManager.creatMessage(dbClient, Message.MessageTypeText, fromUserId, toUserId, 
				null, message, 0.0, 0.0, null, 0, null, null, false, appId, showInSender);
	}

    public static boolean isGroupMessage(String messageId) {

        Message message = MessageManager.getMessageById(messageId);
        if (message == null){
            return false;
        }

        return message.isGroup();
    }

    private static Message getMessageById(String messageId) {

        if (StringUtil.isEmpty(messageId)){
            log.warn("getMessageById but messageId "+messageId+" is null or empty");
            return null;
        }

        BasicDBObject query = new BasicDBObject(DBConstants.F_MESSAGE_ID, new ObjectId(messageId));
        DBObject obj = DBService.getInstance().getMongoDBClient().findOne(DBConstants.T_MESSAGE, query);
        if (obj == null){
            log.warn("getMessageById but messageId "+messageId+" not found");
            return null;
        }

        return new Message(obj);
    }

    public static int getNewMessageCount(String userId, String friendUserId) {

        DBObject query = new BasicDBObject(DBConstants.F_MESSAGE_USER_ID, userId);
        query.put(DBConstants.F_FRIENDID, friendUserId);

        BasicDBObject obj = (BasicDBObject)DBService.getInstance().getMongoDBClient().findOne(DBConstants.T_MESSAGE_STATISTIC, query);
        if (obj == null){
            return 0;
        }

        return obj.getInt(DBConstants.F_NEW_MSG_COUNT);
    }

    public static void sendWelcomeMessage(String userId, String appId) {

        if (userId == null)
            return;

        AbstractApp app = AppFactory.getInstance().getApp(appId);
        if (app == null)
            return;

        if (app.isSupportMessage() == false)
            return;

        String message = app.welcomeMessage();
        String CUSTOMER_SERVICER_ID = app.welcomeCustomerServiceId();
        if (message != null && CUSTOMER_SERVICER_ID != null){

            MessageManager.sendSystemMessage(mongoClient, userId, message, appId, false);

//            MessageManager.creatMessage(mongoClient, Message.MessageTypeText, CUSTOMER_SERVICER_ID, userId,
//                    null, message, 0.0, 0.0, null, 0, null, null, appId);
        }

    }
}