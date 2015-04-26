package com.orange.game.model.dao.bbs;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.CommonData;

public class BBSBoard extends CommonData {

	public static final int BBSBoardTypeParent = 1;
	public static final int BBSBoardTypeSub = 2;

	public BBSBoard() {
		super();
		// TODO Auto-generated constructor stub
	}

	public BBSBoard(DBObject dbObject) {
		super(dbObject);
		// TODO Auto-generated constructor stub
	}

	public BBSBoard(int type, String name, String icon, String parentBoardId,
			int index, String desc) {
		super();
		getDbObject().put("_id", new ObjectId());
		setType(type);
		setName(name);
		setIcon(icon);
		setIndex(index);
		setDesc(desc);
		if (type == BBSBoardTypeSub && parentBoardId != null) {
			setParentBoardId(new ObjectId(parentBoardId));
		}
	}

	public String getBoardId() {
		return getObjectId().toString();
	}

	public boolean isSubBoard() {
		return !StringUtil.isEmpty(getParentBoardId());
	}

	public String getName() {
		return getString(DBConstants.F_NAME);
	}

	public String getIcon() {
		return getString(DBConstants.F_ICON);
	}

	public String getParentBoardId() {
		ObjectId oId = (ObjectId) getObject(DBConstants.F_PARENT_BOARDID);
		if (oId != null) {
			return oId.toString();
		}
		return null;
	}

	public ObjectId getLastPostId() {
		return (ObjectId) getObject(DBConstants.F_LAST_POSTID);
	}

	public BBSPost getLastPost() {
		DBObject object = (DBObject) getObject(DBConstants.F_LAST_POST);
		if (object != null) {
			return new BBSPost(object);
		}
		return null;
	}

	// public int getTopicCount() {
	// return getInt(DBConstants.F_TOPIC_COUNT);
	// }

	public int getPostCount() {
		return getInt(DBConstants.F_POST_COUNT);
	}

	public int geType() {
		return getInt(DBConstants.F_TYPE);
	}

	public int getIndex() {
		return getInt(DBConstants.F_INDEX);
	}

	public String getDesc() {
		return getString(DBConstants.F_DESC);
	}

	// setter methods.

	public void setType(int type) {
		put(DBConstants.F_TYPE, type);
	}

	public void setName(String name) {
		put(DBConstants.F_NAME, name);
	}

	public void setIcon(String icon) {
		put(DBConstants.F_ICON, icon);
	}

	public void setParentBoardId(ObjectId parentBoardID) {
		put(DBConstants.F_PARENT_BOARDID, parentBoardID);
	}

	public void setLastPost(BBSPost lastPost) {
		put(DBConstants.F_LAST_POST, lastPost);
	}

	public void setLastPostId(String lastPostId) {
		if (lastPostId != null) {
			put(DBConstants.F_LAST_POSTID, new ObjectId(lastPostId));
		}
	}

	public void setLastPostId(ObjectId lastPostId) {
		put(DBConstants.F_LAST_POSTID, lastPostId);
	}

	public void setActionCount(int actionCount) {
		put(DBConstants.F_ACTION_COUNT, actionCount);
	}

	public void setPostCount(int postCount) {
		put(DBConstants.F_POST_COUNT, postCount);
	}

	public void setIndex(int index) {
		put(DBConstants.F_INDEX, index);
	}

	public void setDesc(String desc) {
		put(DBConstants.F_DESC, desc);
	}

	public int getActionCount() {
		return getInt(DBConstants.F_ACTION_COUNT);
	}

	public Set<ObjectId> getAdminUidSet() {
		BasicDBList list =  (BasicDBList) getObject(DBConstants.F_ADMINUID_LIST);
		if (list == null || list.isEmpty()) {
			return null;
		}
		HashSet<ObjectId> set = new HashSet<ObjectId>();
		for (Object object : list) {
			set.add((ObjectId) object);
		}
		return set;
	}

	public void setAdminUserList(Collection<BBSUser> userSet) {
		put(DBConstants.F_ADMINUSER_LIST, userSet);
	}
	
	public Collection<BBSUser> getAdminUserList() {
		return (Collection<BBSUser>) getObject(DBConstants.F_ADMINUSER_LIST);
	}
}
