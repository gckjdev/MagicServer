package com.orange.game.model.dao.bbs;

import org.bson.types.ObjectId;

import com.mongodb.DBObject;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.CommonData;

public class BBSActionSource extends CommonData {

	public BBSActionSource(String postId, String postUid, String actionId,
			String actionUid, String actionNickName, int actionType,
			String briefText) {
		super();
		setPostId(postId);
		setPostUid(postUid);

		setActionId(actionId);
		setActionUid(actionUid);
		setActionType(actionType);
		setActionNickName(actionNickName);
		setBriefText(briefText);
	}

	public void setActionNickName(String actionNickName) {
		put(DBConstants.F_ACTION_NICKNAME, actionNickName);

	}

	public String getActionNickName() {
		return getString(DBConstants.F_ACTION_NICKNAME);
	}

	String getStringId(String key) {
		ObjectId oId = (ObjectId) getObject(key);
		if (oId != null) {
			return oId.toString();
		}
		return null;
	}

	void setStringId(String key, String stringId) {
		if (!StringUtil.isEmpty(stringId)) {
			put(key, new ObjectId(stringId));
		}
	}

	public BBSActionSource() {
		super();
	}

	public BBSActionSource(DBObject dbObject) {
		super(dbObject);
	}

	public String getPostId() {
		return getStringId(DBConstants.F_POSTID);
	}

	public String getPostUid() {
		return getStringId(DBConstants.F_POST_UID);
	}

	public String getActionId() {
		return getStringId(DBConstants.F_ACTION_ID);
	}

	public String getActionUid() {
		return getStringId(DBConstants.F_ACTION_UID);
	}

	public int getActionType() {
		return getInt(DBConstants.F_ACTION_TYPE);
	}

	public String getBriefText() {
		return getString(DBConstants.F_BRIEF_TEXT);
	}

	public void setPostId(String postId) {
		setStringId(DBConstants.F_POSTID, postId);
	}

	public void setPostUid(String postUid) {
		setStringId(DBConstants.F_POST_UID, postUid);
	}

	public void setActionId(String actionId) {
		setStringId(DBConstants.F_ACTION_ID, actionId);
	}

	public void setActionUid(String actionUid) {
		setStringId(DBConstants.F_ACTION_UID, actionUid);
	}

	public void setActionType(int actionType) {
		put(DBConstants.F_ACTION_TYPE, actionType);
	}

	public void setBriefText(String briefText) {
		put(DBConstants.F_BRIEF_TEXT, briefText);
	}

}
