package com.orange.game.model.manager.message;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonMongoIdComplexListManager;
import com.orange.game.model.dao.Message;
import com.orange.game.model.dao.MessageStat;
import com.orange.game.model.dao.User;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.MessageManager;
import com.orange.game.model.manager.UserManager;
import com.orange.game.model.manager.opus.OpusUtils;
import com.orange.game.model.service.DBService;

import org.bson.types.ObjectId;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-6-25
 * Time: 下午3:11
 * To change this template use File | Settings | File Templates.
 */

@Deprecated
public class UserMessageStatisticManager extends CommonMongoIdComplexListManager<MessageStat> {

	
	private static UserMessageStatisticManager userMessageStatisticManager = new UserMessageStatisticManager();
	
	public static UserMessageStatisticManager getInstance() {
		
		return userMessageStatisticManager;
	}
	
	
	
    public UserMessageStatisticManager() {
        super(DBConstants.T_USER_MESSAGE_STAT, DBConstants.T_MESSAGE, null, MessageStat.class);

    }

    @Override
    protected String indexBeforeDate() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected List invokeOldGetList(String userId, int offset, int limit) {
        return Collections.emptyList();
    }

    @Override
    protected List invokeOldGetListForConstruct(String key) {
        return Collections.emptyList();
    }

    /*// this method is only used for transferring old data
    public void insertUserMessageStat(String userId, String relatedUserId, String messageId, Date date, int type,
                                      String messageText, int messageContentType){

        BasicDBObject obj = new BasicDBObject();
        obj.put(DBConstants.F_MESSAGE_ID, new ObjectId(messageId));
        obj.put(DBConstants.F_CREATE_DATE, date);
        obj.put(DBConstants.F_RELATED_USER_ID, relatedUserId);
        obj.put(DBConstants.F_TYPE, type);
        obj.put(DBConstants.F_MESSAGE_TEXT, messageText);
        obj.put(DBConstants.F_MESSAGE_CONTENT_TYPE, messageContentType);

        insertObject(userId, obj, DBConstants.F_CREATE_DATE, false, false);
    }*/
    
    // this method is only used for transferring old data
    public void insertUserMessageStat(String userId,
                                      String friendId,
                                      String latestMsgId,
                                      String messageId,
                                      Date createDate,
    								  int msgDirectionint,
                                      int type,
                                      int newMsgCount,
                                      int totalMsgCount,
                                      String messageText){

        BasicDBObject obj = new BasicDBObject();
        obj.put(DBConstants.F_MESSAGE_ID, new ObjectId(messageId));
        obj.put(DBConstants.F_CREATE_DATE, createDate);
        obj.put(DBConstants.F_FRIENDID, friendId);
        obj.put(DBConstants.F_MESSAGE_DIRECTION,msgDirectionint);
        obj.put(DBConstants.F_TYPE,type);
        obj.put(DBConstants.F_MESSAGE_TEXT, messageText);
        obj.put(DBConstants.F_NEW_MSG_COUNT,newMsgCount);
        obj.put(DBConstants.F_TOTAL_MSG_COUNT, totalMsgCount);
        obj.put(DBConstants.F_LATEST_MSG, latestMsgId);

        insertObject(userId, obj, DBConstants.F_CREATE_DATE, false, false);
    }

    public void insertUserMessageStat(String userId,
                                      String friendUserId,
                                      String messageId,
                                      int messageDirection,
                                      String messageText,
                                      int messageContentType,
                                      int newMsgCount){

        BasicDBObject obj = new BasicDBObject();

        obj.put(DBConstants.F_CREATE_DATE, new Date());
        obj.put(DBConstants.F_MODIFY_DATE, new Date());
        obj.put(DBConstants.F_FRIENDID, friendUserId);
        obj.put(DBConstants.F_MESSAGE_DIRECTION, messageDirection);
        obj.put(DBConstants.F_TYPE, messageContentType);
        obj.put(DBConstants.F_MESSAGE_TEXT, messageText);
        obj.put(DBConstants.F_LATEST_MSG, messageId);
        obj.put(DBConstants.F_NEW_MSG_COUNT,newMsgCount);

        insertObject(userId, obj, false, true, false);
    }

    public void deleteUserMessageStat(String userId, String friendUserId){
        removeId(userId, friendUserId, false);
    }

    public void updateFriendUserInfo(List<MessageStat> list) {

        if (list == null || list.size() == 0)
            return;

        // construct friend user id list
        List<ObjectId> fidList = new ArrayList<ObjectId>();
        for (MessageStat stat : list) {
            String fid = stat.getFriendUserId();
            if (!StringUtil.isEmpty(fid) && ObjectId.isValid(fid)) {
                fidList.add(new ObjectId(fid));
            }
        }

        // get user details and put into map
        List<User> userList = UserManager.findPublicUserInfoByUserIdList(DBService.getInstance().getMongoDBClient(), fidList);
        HashMap<String, User> map = new HashMap<String, User>();
        for (User user : userList){
            map.put(user.getUserId(), user);
        }

        if (map.size() == 0)
            return;

        // put data into message stat
        for (MessageStat stat : list){
            String fid = stat.getFriendUserId();
            if (!StringUtil.isEmpty(fid) && map.containsKey(fid)){
                User user = map.get(fid);
                stat.setFriendAvatar(user.getAvatar());
                stat.setFriendGender(user.getGender());
                stat.setFriendNickName(user.getNickName());
            }
        }
    }
    
    public List<MessageStat> getList(String userId, int offset,int limit) {
    	List<MessageStat> messageStats = this.getList(userId, offset, limit, null, null, 0);
        updateFriendUserInfo(messageStats);
		return messageStats;
	}
    
}
