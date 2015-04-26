package com.orange.game.model.dao.bbs;

import java.util.Date;

import org.bson.types.ObjectId;

import com.mongodb.DBObject;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.CommonData;

public class BBSAction extends CommonData {

	public static final int ActionTypeComment = 1;
//	public static final int ActionTypeReply = 2;
	public static final int ActionTypeSupport = 2;
	public static final int ActionTypeNO = 0;

	public static final int StatusNormal = 0;
	public static final int StatusDelete = 1;
	
	public BBSAction() {
		super();
		// TODO Auto-generated constructor stub
	}

	public BBSAction(DBObject dbObject) {
		super(dbObject);
		// TODO Auto-generated constructor stub
	}

	public BBSAction(String appId, int deviceType, BBSUser createUser,
			int actionType, BBSContent content, BBSActionSource source) {
		super();
		setActionId(new ObjectId());
		setAppId(appId);
		setType(actionType);
		setDeviceType(deviceType);
		setCreateUser(createUser);
		setContent(content);
		setActionSource(source);
		setCreateDate(new Date());
		setActionId(new ObjectId());
		setStatus(StatusNormal);
	}

	public void setStatus(int status) {
		put(DBConstants.F_STATUS, status);
	}

	public int getStatus() {
		return getInt(DBConstants.F_STATUS);
	}

	
	public String getActionId() {
		return getStringObjectId();
	}

	void setActionId(ObjectId oId) {
		put("_id", oId);
	}

	public String getSourcePostId() {
		BBSActionSource source = getActionSource();
		if (source != null) {
			return source.getPostId();
		}
		return null;
	}

	public String getSourceActionId() {
		BBSActionSource source = getActionSource();
		if (source != null) {
			return source.getActionId();
		}
		return null;
	}

	public String getAppId() {
		String appId = getString(DBConstants.F_APPID);
		if (appId == null) {
			return "";
		}
		return appId;
	}

	public int getType() {
		return getInt(DBConstants.F_TYPE);
	}

	public int getDeviceType() {
		return getInt(DBConstants.F_DEVICE_TYPE);
	}

	public Date getCreateDate() {
		return getDate(DBConstants.F_CREATE_DATE);
	}

	public BBSUser getCreateUser() {
		DBObject object = (DBObject) getObject(DBConstants.F_CREATE_USER);
		if (object != null) {
			return new BBSUser(object);
		}
		return null;
	}

	public BBSContent getContent() {
		DBObject object = (DBObject) getObject(DBConstants.F_CONTENT);
		if (object != null) {
			BBSContent content = new BBSContent(object);
			if (BBSContent.ContentTypeImage == content.getType()) {
				return new BBSImageContent(object);
			}else if(BBSContent.ContentTypeDraw == content.getType()){
				return new BBSDrawContent(object);
			}
            else{
                return new BBSContent(object);
            }
		}
		return null;
	}

	public void setType(int type) {
		put(DBConstants.F_TYPE, type);
	}

	public void setAppId(String appId) {
		put(DBConstants.F_APPID, appId);
	}

	public void setDeviceType(int deviceType) {
		put(DBConstants.F_DEVICE_TYPE, deviceType);
	}

	public void setCreateDate(Date createDate) {
		put(DBConstants.F_CREATE_DATE, createDate);
	}

	public void setCreateUser(BBSUser createUser) {
		if (createUser != null) {
			put(DBConstants.F_CREATE_USER, createUser.getDbObject());
		}
	}

	public void setContent(BBSContent content) {
		if (content != null) {
			put(DBConstants.F_CONTENT, content.getDbObject());
		}
	}

	// source post && action

	BBSActionSource actionSource;

	public BBSActionSource getActionSource() {
		DBObject object = (DBObject) getObject(DBConstants.F_ACTION_SOURCE);
		if (object != null) {
			return new BBSActionSource(object);
		}
		return null;
	}

	public void setActionSource(BBSActionSource actionSource) {
		if (actionSource != null) {
			put(DBConstants.F_ACTION_SOURCE, actionSource.getDbObject());
		}
	}

	public int getReplyCount() {
		return getInt(DBConstants.F_REPLY_COUNT);
	}
}
