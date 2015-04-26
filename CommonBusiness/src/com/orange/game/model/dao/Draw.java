package com.orange.game.model.dao;

import java.util.Date;

import com.mongodb.DBObject;
import com.orange.game.constants.DBConstants;

public class Draw extends CommonData {

	public Draw(DBObject dbObject) {
		super(dbObject);
	}
	
	public String getNickName() {
		return this.getString(DBConstants.F_DRAW_NICK_NAME);
	}

	public String getUserId() {
		return this.getString(DBConstants.F_DRAW_USER_ID);
	}

	public String getUserAvatar() {
		return this.getString(DBConstants.F_DRAW_AVATAR);
	}

	public String getWord() {
		return this.getString(DBConstants.F_DRAW_WORD);
	}

	public byte[] getDrawData() {
		return (byte[])this.dbObject.get(DBConstants.F_DRAW_DATA);
	}

	public Date getCreateDate() {
		return this.getDate(DBConstants.F_DRAW_CREATE_DATE);
	}
	
}
