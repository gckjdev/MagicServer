package com.orange.game.model.dao;

import java.util.Date;

import org.bson.types.ObjectId;

import com.mongodb.DBObject;
import com.orange.game.constants.DBConstants;

public class LearnDrawIndex extends CommonData {

	
	
	public LearnDrawIndex() {
		super();
	}

	public LearnDrawIndex(DBObject dbObject) {
		super(dbObject);
	}

	public String getUserId() {

		ObjectId oid = getObjectId(DBConstants.F_UID);
		if (oid != null) {
			return oid.toString();
		}
		return null;
	}

	public void setUserId(String userId) {
		ObjectId oId = new ObjectId(userId);
		put(DBConstants.F_UID, oId);
	}

	public String getDrawId() {
		ObjectId oid = getDrawObjectId();
		if (oid != null) {
			return oid.toString();
		}
		return null;
	}

	public ObjectId getDrawObjectId() {
		ObjectId oid = getObjectId(DBConstants.F_OPUS_ID);
		return oid;
	}
	
	public void setDrawId(String drawId) {
		ObjectId oId = new ObjectId(drawId);
		put(DBConstants.F_OPUS_ID, oId);
	}

	public Date getCreateDate() {
		return getDate(DBConstants.F_CREATE_DATE);
	}

	public void setCreateDate(Date createDate) {
		put(DBConstants.F_CREATE_DATE, createDate);
	}

	public void setSellContentType(int sellContentType) {
		put(DBConstants.F_SELL_CONTENT_TYPE, sellContentType);
	}

}
