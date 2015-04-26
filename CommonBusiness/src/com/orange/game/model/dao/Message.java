package com.orange.game.model.dao;

import java.util.Date;
import java.util.List;


import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.utils.DateUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.manager.utils.ImageUploadManager;


public class Message extends CommonData {
	public static final int MessageTypeText = 0;
	public static final int MessageTypeLocationRequest = 1;
	public static final int MessageTypeLocationResponse = 2;
	public static final int MessageTypeDraw = 4;
	public static final int MessageTypeImage = 5;
	public static final int MessageTypeVoice = 6;

	public static final int MessageStatusUnread = 0;
	public static final int MessageStatusRread = 1;

	public static final int LocationResponseReject = 1;
	public static final int LocationResponseAccept = 0;
	public static final int FLAG_NORMAL = 0;
	public static final int FLAG_DELETE = 1;

    public Message() {
        super();
    }

	public Message(DBObject dbObject) {
		super(dbObject);
		// TODO Auto-generated constructor stub
	}

	public String getMessageId() {
		return this.getObjectId().toString();
	}

	public String getFrom() {
		return this.getString(DBConstants.F_FROM_USERID);
	}

	public String getTo() {
		return this.getString(DBConstants.F_TO_USERID);
	}

	public byte[] getDrawData() {
		return (byte[]) this.getObject(DBConstants.F_DRAW_DATA);
	}

	public String getText() {
		return this.getString(DBConstants.F_TEXT_CONTENT);
	}

	public int getStatus() {
		return this.getInt(DBConstants.F_STATUS);
	}

	public Date getCreateDate() {
		return getDate(DBConstants.F_CREATE_DATE);
	}

	public int getCreateDateIntValue() {
		Date createdate = getCreateDate();
		return DateUtil.dateToInt(createdate);
	}

	public int getType() {
		return getInt(DBConstants.F_TYPE);
	}

	public String getReqMessageId() {
		return getString(DBConstants.F_REQUEST_MESSAGE_ID);
	}

	public int getReplyResult() {
		return getInt(DBConstants.F_REPLY_RESULT);
	}

	public double getLatitude() {
		return getDouble(DBConstants.F_LATITUDE);
	}

	public double getLongitude() {
		return getDouble(DBConstants.F_LONGITUDE);
	}

	// setter

	public void setFrom(String from) {
		put(DBConstants.F_FROM_USERID, from);

	}

	public void setTo(String to) {
		put(DBConstants.F_TO_USERID, to);
	}

	public void setDrawData(byte[] data) {
		put(DBConstants.F_DRAW_DATA, data);
	}

	public void setText(String text) {
		put(DBConstants.F_TEXT_CONTENT, text);
	}

	public void setStatus(int status) {
		put(DBConstants.F_STATUS, status);
	}

	public void setCreateDate(Date createDate) {
		put(DBConstants.F_CREATE_DATE, createDate);
	}

	public void setType(int type) {
		put(DBConstants.F_TYPE, type);
	}

	public void setReqMessageId(String reqMessageId) {
		put(DBConstants.F_REQUEST_MESSAGE_ID, reqMessageId);
	}

	public void setReplyResult(int replyResult) {
		put(DBConstants.F_REPLY_RESULT, replyResult);
	}

	public void setLatitude(double latitude) {
		put(DBConstants.F_LATITUDE, latitude);
	}

	public void setLongitude(double longitude) {
		put(DBConstants.F_LONGITUDE, longitude);
	}

	public void setSenderDelFlag(int flag) {
		put(DBConstants.F_SENDER_DEL_FLAG, flag);
	}
	public void setReceiverDelFlag(int flag) {
		put(DBConstants.F_RECEIVER_DEL_FLAG, flag);
	}

	public void setRelatedUserId(BasicDBList relatedUserIdList) {
		put(DBConstants.F_RELATED_USER_ID, relatedUserIdList);
	}

	public void setImageURL(String imageURL) {
		if (imageURL != null){
			put(DBConstants.F_IMAGE_URL, imageURL);
		}
	}

	public void setThumbImageURL(String thumbImageURL) {
		if (thumbImageURL != null){
			put(DBConstants.F_THUMB_URL, thumbImageURL);
		}
	}

	public String getRemoteImageURL() {
		String url = getString(DBConstants.F_IMAGE_URL);
		if (url == null)
			return null;

		return ImageUploadManager.getMessageImageManager().getRemoteURL(url);
	}

	public String getRemoteThumbImageURL() {
		String url = getString(DBConstants.F_THUMB_URL);
		if (url == null)
			return null;
		
		return ImageUploadManager.getMessageImageManager().getRemoteURL(url);
	}

    public void setIsGroup(boolean group) {
        put(DBConstants.F_IS_GROUP, group);
    }

    public boolean isGroup(){
        return getBoolean(DBConstants.F_IS_GROUP);
    }
}
