package com.orange.game.model.manager.message;

import com.orange.common.utils.StringUtil;

import com.mongodb.BasicDBObject;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonCassandraIdListManager;
import com.orange.game.model.common.CommonMongoIdComplexListManager;
import com.orange.game.model.dao.Message;
import com.orange.game.model.dao.User;
import org.bson.types.ObjectId;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-6-25
 * Time: 下午3:11
 * To change this template use File | Settings | File Templates.
 */

public class UserMessageManager extends CommonCassandraIdListManager<Message> {
	
	private static UserMessageManager userMessageManager = new UserMessageManager();
	
	public static UserMessageManager getInstance() {
		
		return userMessageManager;
	}

    public UserMessageManager() {
        super(DBConstants.T_USER_MESSAGE, DBConstants.T_MESSAGE, Message.class);
    }

    private String createKey(String userId, String relatedUserId){
        return userId+":"+relatedUserId;
    }

    public void insertUserMessage(String userId, String relatedUserId, String messageId, int type){
        String key = createKey(userId, relatedUserId);
        insertIndex(key, messageId, String.valueOf(type));
    }

    public void deleteUserMessage(String userId, String relatedUserId, String messageId){
        if (StringUtil.isEmpty(userId) || StringUtil.isEmpty(relatedUserId) || StringUtil.isEmpty(messageId)){
            log.info("<deleteUserMessageInCassandra> but userId or relatedUserId or messageId is EMPTY/NULL");
            return;
        }

        String key = createKey(userId, relatedUserId);
        log.info("<deleteUserMessageInCassandra> key="+key+", messageId="+messageId);
        deleteIndex(key, messageId);
    }

    public List<Message> getUserMessageList(String userId, String targetUserId, boolean isGroup, String offsetMessageId, int limit, boolean forward){
        String key = "";
        if (isGroup){
            key = targetUserId; // targetUserId should be group ID here
        }
        else{
            key = createKey(userId, targetUserId);
        }

        String startOffsetId = null;
        String endOffsetId = null;

        if (forward)
            endOffsetId = offsetMessageId;
        else
            startOffsetId = offsetMessageId;

        return getList(key, startOffsetId, endOffsetId, limit, null, 0, null);
    }

    public void insertGroupMessage(String groupId, String newMessageId, int type) {
        insertIndex(groupId, newMessageId, String.valueOf(type));
    }
}
