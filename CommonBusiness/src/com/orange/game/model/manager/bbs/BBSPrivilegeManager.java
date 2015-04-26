package com.orange.game.model.manager.bbs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.orange.game.model.manager.CommonManager;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.bbs.BBSPrivilege;

public class BBSPrivilegeManager extends CommonManager {

	public static void updateUserPermission(MongoDBClient mongoClient,
			String targetUid, String boardId, int permission, Date expireDate) {
		DBObject query = new BasicDBObject(DBConstants.F_BOARD_ID, boardId);
		query.put(DBConstants.F_UID, new ObjectId(targetUid));
		DBObject set = new BasicDBObject(DBConstants.F_PERMISSION, permission);
		set.put(DBConstants.F_EXPIRE_DATE, expireDate);
		DBObject update = new BasicDBObject("$set", set);
		mongoClient.upsertAll(DBConstants.T_BBS_PERMISSION, query, update);
	}

	public static void updateUserRole(MongoDBClient mongoClient, String userId,
			int roleType, String boardId, Date expireDate) {
		int permission = BBSPrivilege.getPermissionByRole(roleType);
		if (roleType == BBSPrivilege.BBS_USER_ROLE_BBS_ADMIN) {
			boardId = BBSPrivilege.PERMISSION_SITE;
		} else if (roleType == BBSPrivilege.BBS_USER_ROLE_BOARD_ADMIN) {
			BBSManager.addUserToBoardAdminList(mongoClient, userId, boardId);
		} else {
			BBSManager
					.pullUserOutOfBoardAdminList(mongoClient, userId, boardId);

		}
		updateUserPermission(mongoClient, userId, boardId, permission,
				expireDate);
	}

	public static List<BBSPrivilege> getPrivilegeList(
			MongoDBClient mongoClient, String userId) {
		DBCursor cursor = mongoClient.find(DBConstants.T_BBS_PERMISSION,
				DBConstants.F_UID, new ObjectId(userId));
		if (cursor != null) {
			List<BBSPrivilege> list = new ArrayList<BBSPrivilege>();
			while (cursor.hasNext()) {
				BBSPrivilege privilege = new BBSPrivilege(cursor.next());
				Date date = privilege.getExpireDate();
				if (date != null) {
					if (date.after(new Date())) {
						list.add(privilege);
					}
				} else {
					list.add(privilege);
				}
			}
			cursor.close();
			return list;
		}
		return Collections.emptyList();
	}
	
	public static boolean isSuperAdmin(MongoDBClient mongoClient, String userId){
		List<BBSPrivilege> privileges = getPrivilegeList(mongoClient, userId);
		for (BBSPrivilege privilege : privileges){
			if (privilege.isSuperAdmin()){
				return true;
			}
		}
		
		return false;
	}
	
}
