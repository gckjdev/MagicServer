package com.orange.game.model.dao.bbs;

import java.util.BitSet;
import java.util.Date;

import org.bson.types.ObjectId;

import com.mongodb.DBObject;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.CommonData;

public class BBSPrivilege extends CommonData {

	public final static int PERMISSION_READ = 0x1; // 读
	public final static int PERMISSION_WRITE = 0x1 << 1; // 写
	public final static int PERMISSION_DELETE = 0x1 << 2; // 删帖
	public final static int PERMISSION_TRANSFER = 0x1 << 3; // 转移
	public final static int PERMISSION_TOTOP = 0x1 << 4; // 置顶
	public final static int PERMISSION_FORBID_USER = 0x1 << 5; // 封禁用户
	public final static int PERMISSION_FORBIDED = PERMISSION_READ;

	public final static int PERMISSION_DEFAULT = PERMISSION_WRITE
			| PERMISSION_READ;
	public final static int PERMISSION_ADMIN = PERMISSION_DEFAULT
			| PERMISSION_DELETE | PERMISSION_TOTOP | PERMISSION_FORBID_USER;
	public final static int PERMISSION_SUPER = (0x1 << 31) - 1;

	public final static String PERMISSION_SITE = "PERMISSION_SITE";

	public final static int BBS_USER_ROLE_NORMAL = 0;
	public final static int BBS_USER_ROLE_FORBIDED = 0x1;
	public final static int BBS_USER_ROLE_BOARD_ADMIN = 0x1 << 1;
	public final static int BBS_USER_ROLE_BBS_ADMIN = 0x1 << 2;

	public BBSPrivilege() {
		super();
	}

	public BBSPrivilege(DBObject dbObject) {
		super(dbObject);
	}

	public BBSPrivilege(String userId, int type, int permission,
			String boardId, Date forbidEndDate) {
		super();
		this.setUserId(userId);
		this.setPermission(permission);
		this.setBoardId(boardId);
		this.setExpireDate(forbidEndDate);
	}

	public ObjectId getObjectUserId() {
		return getObjectId(DBConstants.F_USERID);
	}

	public String getUserId() {
		return ObjectIdToString(getObjectUserId());
	}

	public int getPermission() {
		return getInt(DBConstants.F_PERMISSION);
	}

	public Date getExpireDate() {
		return getDate(DBConstants.F_EXPIRE_DATE);
	}

	public void setUserId(String userId) {
		setUserId(new ObjectId(userId));
	}

	public void setUserId(ObjectId userId) {
		put(DBConstants.F_USERID, userId);
	}

	public void setBoardId(String boardId) {
		put(DBConstants.F_BOARD_ID, boardId);
	}

	public String getBoardId() {
		return getString(DBConstants.F_BOARD_ID);
	}

	public void setPermission(int permission) {
		put(DBConstants.F_PERMISSION, permission);
	}

	public void setExpireDate(Date forbidEndDate) {
		put(DBConstants.F_EXPIRE_DATE, forbidEndDate);
	}

	// private methods
	public String ObjectIdToString(ObjectId oId) {
		if (oId != null) {
			return oId.toString();
		}
		return null;
	}

	public static int getPermissionByRole(int roleType) {
		switch (roleType) {
		case BBS_USER_ROLE_FORBIDED:
			return PERMISSION_FORBIDED;
		case BBS_USER_ROLE_BOARD_ADMIN:
			return PERMISSION_ADMIN;
		case BBS_USER_ROLE_BBS_ADMIN:
			return PERMISSION_SUPER;
		case BBS_USER_ROLE_NORMAL:
		default:
			return PERMISSION_DEFAULT;
		}
	}

	public boolean isSuperAdmin() {
		if (getBoardId().equals(BBSPrivilege.PERMISSION_SITE)){
			if (getPermission() == BBSPrivilege.PERMISSION_SUPER)
				return true;				
		}
		
		return false;
	}
}
