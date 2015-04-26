package com.orange.game.model.dao;

import java.util.Date;

import com.google.protobuf.ByteString;
import com.mongodb.DBObject;
import com.orange.common.utils.DateUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ServiceConstant;

public class MessageStat extends CommonData {

	public MessageStat(DBObject dbObject) {
		super(dbObject);
	}

    public MessageStat() {
        super();
    }


    public byte[] getDrawData() {
		return (byte[])this.dbObject.get(DBConstants.F_DRAW_DATA);
	}

	public String getMessageId() {
		return this.getString(DBConstants.F_LATEST_MSG);
	}

	
	public Date getModifyDate()
	{
		return getDate(DBConstants.F_MODIFY_DATE);
	}
	
	public int getModifiedDateIntValue() {
		Date date = getModifyDate();
		return DateUtil.dateToInt(date);
	}

	public int getTotalMessageCount() {
		return this.getInt(DBConstants.F_TOTAL_MSG_COUNT);
	}

	public String getText() {
		return this.getString(DBConstants.F_TEXT_CONTENT);
	}

	public int getNewMessageCount() {
		return this.getInt(DBConstants.F_NEW_MSG_COUNT);
	}

    public int getNewGroupMessageCount(){
        return getInt(DBConstants.F_NEW_GROUP_MSG_COUNT);
    }

	public String getUserId() {
		return this.getString(DBConstants.F_MESSAGE_USER_ID);
	}

	public String getFriendUserId() {
		return this.getString(DBConstants.F_FRIENDID);
	}

	public void setFriendUserId(String fid) {
		put(DBConstants.F_FRIENDID, fid);
	}
	
	public String getFriendNickName() {
		return this.getString(DBConstants.F_FRIEND_NICKNAME);
	}

	public void setFriendNickName(String fid) {
		put(DBConstants.F_FRIEND_NICKNAME, fid);
	}
	public String getFriendAvatar() {
//		return this.getString(DBConstants.F_FRIEND_AVATAR);
		return User.getTranslatedAvatar(getString(DBConstants.F_FRIEND_AVATAR));
	}

	public void setFriendAvatar(String fid) {
		put(DBConstants.F_FRIEND_AVATAR, fid);
	}

	public String getFriendGender() {
		return this.getString(DBConstants.F_FRIEND_GENDER);
	}

	public boolean isFriendMale() {
		String gender = getFriendGender();
		if (gender != null && gender.equalsIgnoreCase("m")) {
			return true;
		}
		return false;
	}

    public boolean isGroup() {
        return getBoolean(DBConstants.F_IS_GROUP);
    }

    public void setGroup(boolean isGroup){
        put(DBConstants.F_IS_GROUP, isGroup);
    }

	public void setFriendGender(String fid) {
		put(DBConstants.F_FRIEND_GENDER, fid);
	}

	public String getFromId() {
		if (this.getDirection() == ServiceConstant.CONST_MESSAGE_DIRECTION_SEND) {
			return this.getUserId();
		} else {
			return this.getFriendUserId();
		}
	}

	public int getDirection() {
		return this.getInt(DBConstants.F_MESSAGE_DIRECTION);
	}

	public String getToId() {
		if (this.getDirection() == ServiceConstant.CONST_MESSAGE_DIRECTION_RECIEVE) {
			return this.getUserId();
		} else {
			return this.getFriendUserId();
		}
	}

	public Date getCreateDate()
	{
		return getDate(DBConstants.F_CREATE_DATE);
	}
	
	public int getCreateDateIntValue() {
		Date createdate = getCreateDate();
		return DateUtil.dateToInt(createdate);
	}

	public int getType() {
		return getInt(DBConstants.F_TYPE);
	}

	public String getLatestMsgId() {
		return this.getString(DBConstants.F_LATEST_MSG);
	}


    public void setFriendVip(int finalVip) {
        put(DBConstants.F_VIP, finalVip);
    }

    public int getFriendVip() {
        return getInt(DBConstants.F_VIP);
    }
}
