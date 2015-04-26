package com.orange.game.model.dao.push;

import com.mongodb.*;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.CommonData;
import com.orange.game.model.dao.tutorial.UserTutorial;
import com.orange.game.model.service.DBService;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.UUID;

/**
 * Created by chaoso on 14-8-18.
 * model of PushMessage
 */
public class PushMessageInfo extends CommonData{

    public PushMessageInfo(){
    }
    public PushMessageInfo(DBObject dbObject){
        super(dbObject);
    }



    public PushMessageInfo(String appId, String userId,
                           String xiaoji, String nickName, String token,
                           Date sendDate, int sendStatus) {

        put(DBConstants.F_APPID, appId);
        put(DBConstants.F_UID, userId);
        put(DBConstants.F_XIAOJI_NUMBER, xiaoji);
        put(DBConstants.F_NICKNAME, nickName);
        put(DBConstants.F_DEVICETOKEN, token);
        put(DBConstants.F_CREATE_DATE, new Date());
        put(DBConstants.F_DATE, sendDate);
        put(DBConstants.F_STATUS, sendStatus);


    }

    public static final int NOT_SEND = 0;
    public static final int SUCCESS = 1;
    public static final int FAIL = 2;

    private String _id;

    public String get_id() {
        return getObjectId().toString();
    }

    private String appId;
    private String userId;
    private String xiaoji;
    private String nickName;
    private String token;
    private Date sendDate;
    private int sendStatus;


    public String getAppId() {
        return (String)getDbObject().get(DBConstants.F_APPID);
    }

    public String getUserId() {
        return (String)getDbObject().get(DBConstants.F_UID);
    }

    public String getXiaoji() {
        return (String)getDbObject().get(DBConstants.F_XIAOJI_NUMBER);
    }

    public String getNickName() {
        return (String)getDbObject().get(DBConstants.F_NICKNAME);
    }

    public String getToken() {
        return (String)getDbObject().get(DBConstants.F_DEVICETOKEN);
    }

    public Date getSendDate() {
        return (Date)getDbObject().get(DBConstants.F_DATE);
    }



    public void setAppId(String appId) {
        put(DBConstants.F_APPID, appId);

    }

    public void setUserId(String userId) {

        put(DBConstants.F_UID, userId);

    }

    public void setXiaoji(String xiaoji) {

        put(DBConstants.F_XIAOJI_NUMBER, xiaoji);

    }

    public void setNickName(String nickName) {

        put(DBConstants.F_NICKNAME, nickName);

    }

    public void setToken(String token) {

        put(DBConstants.F_DEVICETOKEN, token);

    }

    public void setSendDate(Date sendDate) {

        put(DBConstants.F_DATE, sendDate);

    }



    public void setSendStatus(int sendStatus) {

        put(DBConstants.F_STATUS, sendStatus);
    }

    public int getSendStatus() {

        return (Integer)getDbObject().get(DBConstants.F_STATUS);
    }





}
