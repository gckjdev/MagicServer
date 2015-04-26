package com.orange.game.model.dao;


import java.util.Date;

import com.mongodb.DBObject;
import com.orange.game.constants.DBConstants;

public class RoomUser extends CommonData{
	
	
	public static final int STATUS_NOT_INVITE = -1;

	public static final int STATUS_INVITED = 1;
//	public static final int STATUS_CREATOR = 2;
	public static final int STATUS_ACCEPTED = 3;
	public static final int STATUS_PLAYING = 4;

	
	
	public static final int STATUS_UNCHANGE = 0;
	public static final int PLAYTIMES_UNCHANGE = 0;
	
	
    public RoomUser(DBObject dbObject) {
		super(dbObject);
	}
    
	public RoomUser(String userId, String nickName, String gender,
			String avatar, int status, Date lastPlayDate, int playTimes) {
		super();
		this.setUserId(userId);
		this.setNickName(nickName);
		this.setGender(gender);
		this.setAvatar(avatar);
		this.setStatus(status);
		this.setLastPlayDate(lastPlayDate);
		this.setPlayTimes(playTimes);
	}
	
	public RoomUser(String userId, String nickName, String gender,
			String avatar, int status) {
		super();
		this.setUserId(userId);
		this.setNickName(nickName);
		this.setGender(gender);
		this.setAvatar(avatar);
		this.setStatus(status);
		this.setLastPlayDate(new Date());
		this.setPlayTimes(0);
	}

	public String getUserId() {
		return getString(DBConstants.F_USERID);
	}
	public void setUserId(String userId) {
		put(DBConstants.F_USERID, userId);
	}
	public String getnickName() {
		return getString(DBConstants.F_NICKNAME);
	}
	public void setNickName(String nickName) {
		put(DBConstants.F_NICKNAME, nickName);
	}
	public String getGender() {
		return getString(DBConstants.F_GENDER);
	}
	public void setGender(String gender) {
		put(DBConstants.F_GENDER, gender);
	}
	public String getAvatar() {
		return getString(DBConstants.F_AVATAR);
	}
	public void setAvatar(String avatar) {
		put(DBConstants.F_AVATAR, avatar);
	}
	

	public int getStatus() {
		return getInt(DBConstants.F_STATUS);
	}
	public void setStatus(int status) {
		put(DBConstants.F_STATUS, status);
	}
	public Date getLastPlayDate() {
		return getDate(DBConstants.F_LAST_PALY_DATE);
	}
	public void setLastPlayDate(Date lastPlayDate) {
		put(DBConstants.F_LAST_PALY_DATE, lastPlayDate);
	}
	public int getPlayTimes() {
		return getInt(DBConstants.F_PLAY_TIMES);
	}
	public void setPlayTimes(int playTimes) {
		put(DBConstants.F_PLAY_TIMES, playTimes);
	}

	public static String toGenderString(boolean gender) {
		if (gender)
			return "m";
		else
			return "f";
	}    
}
