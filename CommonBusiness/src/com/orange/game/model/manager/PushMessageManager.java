package com.orange.game.model.manager;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.utils.DateUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.PushMessage;
import com.orange.game.model.dao.User;

/**
 * The Class PushMessageManager.
 */
public class PushMessageManager {

    public static final Logger log = Logger.getLogger(PushMessageManager.class.getName());
    private static final int MAX_IPHONE_LEN = 60;


    /**
     * Reset all push messages whose status of running or null to not running
     *
     * @param mongoClient the mongo client
     */
    public static void resetAllRunningMessage(final MongoDBClient mongoClient) {
        DBObject query = new BasicDBObject();
        BasicDBObject update = new BasicDBObject();

        BasicDBList values = new BasicDBList();

//        BasicDBObject query_trycount = new BasicDBObject();
//        query_trycount.put(DBConstants.F_PUSH_MESSAGE_TRYCOUNT, new BasicDBObject("$lt", DBConstants.C_PUSH_MESSAGE_TRY_COUNT_LIMIT));
//        query_trycount.put(DBConstants.F_PUSH_MESSAGE_STATUS, DBConstants.C_PUSH_MESSAGE_STATUS_FAILURE);
//        values.add(query_trycount);

        values.add(new BasicDBObject(DBConstants.F_PUSH_MESSAGE_STATUS, null));
        values.add(new BasicDBObject(DBConstants.F_PUSH_MESSAGE_STATUS, DBConstants.C_PUSH_MESSAGE_STATUS_RUNNING));

        query.put("$or", values);

        BasicDBObject updateValue = new BasicDBObject();
        updateValue.put(DBConstants.F_PUSH_MESSAGE_STATUS, DBConstants.C_PUSH_MESSAGE_STATUS_NOT_RUNNING);
        update.put("$set", updateValue);

        mongoClient.updateAll(DBConstants.T_PUSH_MESSAGE, query, update);
    }


    
    public static PushMessage findMessageForPush(final MongoDBClient mongoClient) {
        
        //find p_status:null OR failure but try_cnt < limit OR not_running

        DBObject query = new BasicDBObject();
        BasicDBObject update = new BasicDBObject();
        
        query.put(DBConstants.F_PUSH_MESSAGE_SCHEDULE_DATE, new BasicDBObject("$lte", new Date()));
        query.put(DBConstants.F_PUSH_MESSAGE_STATUS, DBConstants.C_PUSH_MESSAGE_STATUS_NOT_RUNNING);

        BasicDBObject updateValue = new BasicDBObject();
        updateValue.put(DBConstants.F_PUSH_MESSAGE_STATUS, DBConstants.C_PUSH_MESSAGE_STATUS_RUNNING);
        update.put("$set", updateValue);
        
        log.debug("<findMessageForPush> query="+query.toString()+", update="+update.toString());
        DBObject obj =  mongoClient.findAndModify(DBConstants.T_PUSH_MESSAGE, query, update);

        if (obj != null) {
            PushMessage message =  new PushMessage(obj);
            /*
            User user = UserManager.findPushableUser(mongoClient, message.getUserId());
            if (user != null) {
                return message;
            } else {
                pushMessageFailure(mongoClient, message, DBConstants.C_PUSH_MESSAGE_FAIL_REACH_USER_LIMIT);
                log.warn("<findMessageForPush> push message exceed daily limit of user=" + message.getUserId());
                return null;
            }
            */
            
            return message;
        }
        return null;
    }
    

    private static void failPushMessage(MongoDBClient mongoClient, PushMessage message, int reason) {
        message.setStatus(DBConstants.C_PUSH_MESSAGE_STATUS_FAILED);
        message.setReason(reason);
        mongoClient.save(DBConstants.T_PUSH_MESSAGE, message.getDbObject());
    }


    public static User findAndModifyUserByMessage(MongoDBClient mongoClient, PushMessage message) {
        String userId = message.getUserId();

        BasicDBObject query = new BasicDBObject();
        query.put(DBConstants.F_USERID, new ObjectId(userId));

        BasicDBObject update = new BasicDBObject();
        
        BasicDBObject incValue = new BasicDBObject();
        incValue.put(DBConstants.F_PUSH_COUNT, 1);
        update.put("$inc", incValue);

        BasicDBObject updateValue = new BasicDBObject();
        updateValue.put(DBConstants.F_PUSH_DATE, new Date());
        update.put("$set", updateValue);

        DBObject obj = mongoClient.findAndModifyUpsert(DBConstants.T_USER_PUSH_INFO, query, update);

        if (obj != null) {
            return new User(obj);
        }

        return null;
    }

    @Deprecated
    public static void savePushMessage(final MongoDBClient mongoClient, User user) {

        saveIphonePushMessage(mongoClient,user);
        // saveEmailPushMessage(mongoClient,product,user, item);
    }

    @Deprecated
    private static void saveEmailPushMessage(MongoDBClient mongoClient, User user) {
        
        int titlelen = 60;
        String userId = user.getUserId();
        BasicDBObject query = new BasicDBObject();
        query.put(DBConstants.F_FOREIGN_USER_ID, userId);
//        query.put(DBConstants.F_PRODUCTID, product.getId());
        
        String emailMessage = buildMessageForEmail(user);
        String emailTitle = emailMessage.substring(0, titlelen)+"...";
        BasicDBObject obj = new BasicDBObject();
//        obj.put(DBConstants.F_PRODUCTID, product.getId());
        obj.put(DBConstants.F_FOREIGN_USER_ID, userId);
        obj.put(DBConstants.F_PUSH_MESSAGE_SUBJECT, emailTitle);
        obj.put(DBConstants.F_PUSH_MESSAGE_BODY, emailMessage);
//        obj.put(DBConstants.F_PUSH_MESSAGE_IMAGE, product.getImage());
        
//        obj.put(DBConstants.F_ITEM_ID, item.getItemId());
        obj.put(DBConstants.F_PUSH_MESSAGE_TYPE, DBConstants.C_PUSH_TYPE_EMAIL);
        obj.put(DBConstants.F_PUSH_MESSAGE_SCHEDULE_DATE, new Date());
        obj.put(DBConstants.F_PUSH_MESSAGE_STATUS, DBConstants.C_PUSH_MESSAGE_STATUS_NOT_RUNNING);
        obj.put(DBConstants.F_START_DATE, new Date());

        BasicDBObject update = new BasicDBObject();
        update.put("$set", obj);

        log.debug("update push, query=" + query.toString() + ", value=" + update.toString());

        mongoClient.updateOrInsert(DBConstants.T_PUSH_MESSAGE, query, update);
    }

    
    public static void insertIphonePushMessage(final MongoDBClient mongoClient, final String userId, 
    		final String deviceToken, final String message, final String appId, final Date date) {
    	
    	if (userId == null || deviceToken == null || message == null){
    		return;
    	}

        BasicDBObject obj = new BasicDBObject();
        obj.put(DBConstants.F_DEVICETOKEN, deviceToken);
        obj.put(DBConstants.F_FOREIGN_USER_ID, userId);
        obj.put(DBConstants.F_PUSH_MESSAGE_IPHONE, message);
        
        obj.put(DBConstants.F_PUSH_MESSAGE_TYPE, DBConstants.C_PUSH_TYPE_IPHONE);
        obj.put(DBConstants.F_START_DATE, date);
        obj.put(DBConstants.F_PUSH_MESSAGE_SCHEDULE_DATE, date);
        obj.put(DBConstants.F_PUSH_MESSAGE_STATUS, DBConstants.C_PUSH_MESSAGE_STATUS_NOT_RUNNING);
        obj.put(DBConstants.F_APPID, appId);

        log.info("<insertIphonePushMessage> obj="+obj);

        mongoClient.insert(DBConstants.T_PUSH_MESSAGE, obj);
    }

    @Deprecated
    public static void saveIphonePushMessage(final MongoDBClient mongoClient, User user) {

        String userId = user.getUserId();
        BasicDBObject query = new BasicDBObject();
        query.put(DBConstants.F_FOREIGN_USER_ID, userId);
//        query.put(DBConstants.F_PRODUCTID, product.getId());

        String iPhoneMessage = buildMessageForIPhone(user);
        //String emailMessage = buildMessageForEmail(product,user);
        //String androidMessage = buildMessageForAndroid(product,user);
        //String weiboMessage = buildMessageForWeibo(product,user);

        BasicDBObject obj = new BasicDBObject();
        obj.put(DBConstants.F_DEVICETOKEN, user.getDeviceToken());
//        obj.put(DBConstants.F_PRODUCTID, product.getId());
        obj.put(DBConstants.F_FOREIGN_USER_ID, userId);
        obj.put(DBConstants.F_PUSH_MESSAGE_IPHONE, iPhoneMessage);
        //obj.put(DBConstants.F_PUSH_MESSAGE_ANDROID, androidMessage);
        //obj.put(DBConstants.F_PUSH_MESSAGE_EMAIL, emailMessage);
        //obj.put(DBConstants.F_PUSH_MESSAGE_WEIBO, weiboMessage);
        
        obj.put(DBConstants.F_PUSH_MESSAGE_TYPE, DBConstants.C_PUSH_TYPE_IPHONE);
        obj.put(DBConstants.F_START_DATE, new Date());
        obj.put(DBConstants.F_PUSH_MESSAGE_SCHEDULE_DATE, new Date());
        obj.put(DBConstants.F_PUSH_MESSAGE_STATUS, DBConstants.C_PUSH_MESSAGE_STATUS_NOT_RUNNING);
//        obj.put(DBConstants.F_ITEM_ID, item.getItemId());
//        obj.put(DBConstants.F_APPID, item.getAppId());

        BasicDBObject update = new BasicDBObject();
        update.put("$set", obj);

        log.debug("update push, query=" + query.toString() + ", value=" + update.toString());

        mongoClient.updateOrInsert(DBConstants.T_PUSH_MESSAGE, query, update);
    }
    
    private static String buildMessageForWeibo(User user) {
        
        // TODO Auto-generated method stub
        return null;
    }


    private static String buildMessageForAndroid(User user) {
        // TODO Auto-generated method stub
        return null;
    }


    private static String buildMessageForEmail(User user) {
        
        StringBuilder builder = new StringBuilder();
        builder.append("test");
        
        String contactUrl = "<br> 点击了解详细内容：<br><a href='"    
                + "" + "'>"+ "" + "</a><br>" ;
        
        
        String image = "<br><img src="+""+"width=\"60\" height=\"45\" border=\"0\">";
        String message = builder.toString();
        String   html   = 
                " <IMG   SRC="+""+"   width=80%   height=60%> <br> "+ 
                " <b>   end   of   jpg </b> ";

            return message+contactUrl+"";
// TODO Auto-generated method stub
    }


    private static String buildMessageForIPhone(User user) {
        StringBuilder builder = new StringBuilder();
        builder.append("test"); // TODO to be changed

        String message = builder.toString();
        int len = message.length();
        if (len > MAX_IPHONE_LEN) {
            len = MAX_IPHONE_LEN;
            return message.substring(0, len).concat("...");
        }
        else {
            return message;
        }

    }

    /**
     * Push close.
     *
     * @param mongoClient the mongo client
     * @param pushMessage the push message
     */
    public static void pushMessageClose(final MongoDBClient mongoClient, final PushMessage pushMessage, int reason) {        
        pushMessage.setReason(reason);
        pushMessage.setStatus(DBConstants.C_PUSH_MESSAGE_STATUS_CLOSED);
        mongoClient.save(DBConstants.T_PUSH_MESSAGE, pushMessage.getDbObject());
    }

    public static void pushMessageFailure(final MongoDBClient mongoClient, final PushMessage pushMessage, int reason) {
        pushMessage.incTryCount();
        pushMessage.setStatus(DBConstants.C_PUSH_MESSAGE_STATUS_FAILED);
        pushMessage.setReason(reason);
        mongoClient.save(DBConstants.T_PUSH_MESSAGE, pushMessage.getDbObject());

        String userId = pushMessage.getUserId();
        mongoClient.inc(DBConstants.T_USER_PUSH_INFO, DBConstants.F_USERID, new ObjectId(userId), DBConstants.F_PUSH_COUNT, -1);
    }

    public static User findUserByMessage(final MongoDBClient mongoClient, final PushMessage pushMessage) {
        String userId = pushMessage.getUserId();
        DBObject obj = mongoClient.findOne(DBConstants.T_USER, DBConstants.F_USERID, new ObjectId(userId));
        if (obj != null) {
            return new User(obj);
        }
        return null;
    }


    private static int RETRY_INTERVAL = 1000 * 60;  // 60 seconds
    
    public static void pushMessageRetry(MongoDBClient mongoClient, PushMessage pushMessage, int result) {
        
        if (pushMessage.incTryCount() > DBConstants.C_PUSH_MESSAGE_TRY_COUNT_LIMIT){
            pushMessageClose(mongoClient, pushMessage, result);
            return;
        }

        Date retryDate = new Date(System.currentTimeMillis() + RETRY_INTERVAL);
        
        pushMessage.setStatus(DBConstants.C_PUSH_MESSAGE_STATUS_NOT_RUNNING);
        pushMessage.setScheduleDate(retryDate);
        pushMessage.setReason(result);
        mongoClient.save(DBConstants.T_PUSH_MESSAGE, pushMessage.getDbObject());

        String userId = pushMessage.getUserId();
        mongoClient.inc(DBConstants.T_USER_PUSH_INFO, DBConstants.F_USERID, new ObjectId(userId), DBConstants.F_PUSH_COUNT, -1);
    }
}
