package com.orange.game.model.dao;

import java.util.Date;

import org.bson.types.ObjectId;

import com.mongodb.DBObject;
import com.orange.game.constants.DBConstants;

public class Relation extends CommonData {

	//Don't edit the type. By Gamy
	public static final int RELATION_TYPE_NO = 0;
	public static final int RELATION_TYPE_FOLLOW = 1;
	public static final int RELATION_TYPE_FAN = 0x1 << 1;
	public static final int RELATION_TYPE_FRIEND = 1 | (0x1 << 1);
	public static final int RELATION_TYPE_BLACK = 0x1 << 2;

	
	public Relation() {
		super();
	}
	public Relation(DBObject dbObject) {
		super(dbObject);
	}
	public ObjectId getUid() {
		return (ObjectId) getObject(DBConstants.F_UID);
	}
	public ObjectId getFid() {
		return (ObjectId) getObject(DBConstants.F_FRIENDID);
	}
	public int getType() {
		return getInt(DBConstants.F_TYPE);
	}
	public Date getCreateDate() {
		return getDate(DBConstants.F_CREATE_DATE);
	}
	public String getSrouce() {
		return getString(DBConstants.F_CREATE_SOURCE_ID);
	}
	public void setUid(String uid) {
		put(DBConstants.F_UID, new ObjectId(uid));
	}
	public void setFid(String fid) {
		put(DBConstants.F_FRIENDID, new ObjectId(fid));
	}
	public void setType(int type) {
		put(DBConstants.F_TYPE, type);
	}
	public void setCreateDate(Date createDate) {
		put(DBConstants.F_CREATE_DATE, createDate);
	}
	public void setSrouce(String srouce) {
		put(DBConstants.F_CREATE_DATE, srouce);
	}
	

	public int getGameSessionId() {
		DBObject obj = (DBObject)this.dbObject.get(DBConstants.F_FRIEND_GAME_STATUS);
		if (obj == null)
			return -1;
		Integer id = (Integer)obj.get(DBConstants.F_SESSION_ID);
		if (id == null)
			return -1;
		return id.intValue();
	}
}
