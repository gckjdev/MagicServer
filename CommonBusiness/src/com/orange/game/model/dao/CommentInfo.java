package com.orange.game.model.dao;

import com.mongodb.DBObject;
import com.orange.game.constants.DBConstants;

public class CommentInfo extends CommonData {
		
	public CommentInfo() {
		super();
	}
	
	public CommentInfo(DBObject object) {
		super(object);
	}
	
	public int getType() {
		return getInt(DBConstants.F_TYPE);
	}
	public String getComment() {
		return getString(DBConstants.F_COMMENT_CONTENT);
	}
	public String getActionId() {
		return getString(DBConstants.F_ACTION_ID);
	}
	public String getActionUserId() {
		return getString(DBConstants.F_USERID);
	}
	public String getActionNickName() {
		return getString(DBConstants.F_NICKNAME);
	}
	public String getActionSummary() {
		return getString(DBConstants.F_SUMMARY);
	}
	
	//setter
	public void setType(int type) {
		put(DBConstants.F_TYPE, type);
	}
	public void setComment(String comment) {
		put(DBConstants.F_COMMENT_CONTENT, comment);
	}
	public void setActionId(String actionId) {
		put(DBConstants.F_ACTION_ID, actionId);
	}
	public void setActionUserId(String actionUserId) {
		put(DBConstants.F_USERID, actionUserId);
	}
	public void setActionNickName(String actionNickName) {
		put(DBConstants.F_NICKNAME, actionNickName);
	}
	public void setActionSummary(String actionSummary) {
		put(DBConstants.F_SUMMARY, actionSummary);
	}
    
	
}
