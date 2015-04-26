package com.orange.game.model.manager.group;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.utils.MapUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ErrorCode;
import com.orange.game.model.dao.CommonData;
import com.orange.game.model.dao.group.Group;
import com.orange.game.model.dao.group.GroupPermission;
import com.orange.game.model.dao.group.GroupRelation;
import com.orange.game.model.dao.group.GroupRole;
import com.orange.game.model.manager.CommonManager;
import com.orange.network.game.protocol.model.GroupProtos;
import org.bson.types.ObjectId;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-23
 * Time: 上午10:18
 * To change this template use File | Settings | File Templates.
 */
public class GroupRoleManager extends CommonManager {

    protected static int userCanJoinGroup(MongoDBClient mongoClient, String userId, String groupId) {
        //if use had joined a group
        if (GroupUserManager.userHasJoinedAGroup(mongoClient, userId, groupId)){
            return ErrorCode.ERROR_GROUP_MULTIJOINED;
        }
/*
        GroupRelation relation = GroupUserManager.getUserRelationWithGroup(mongoClient, userId, groupId);
        if (relation != null && relation.isRequester()) {
                log.info("<userCanJoinGroup> userId = " + userId + ", multiple request to join groupId = " + groupId);
                return ErrorCode.ERROR_GROUP_MULTIREQUESTED;
        }
*/
        return ErrorCode.ERROR_SUCCESS;
    }

    protected static int userCanAcceptJoinRequest(MongoDBClient mongoDBClient, String userId, String groupId, String targetId) {

        if (GroupUserManager.userHasJoinedAGroup(mongoDBClient, targetId, groupId)){
            return ErrorCode.ERROR_GROUP_MULTIJOINED;
        }
        if (!checkHasPermission(mongoDBClient, userId, groupId, GroupPermission.HANDLE_REQUEST)) {
            return ErrorCode.ERROR_GROUP_PERMISSION;
        }
        Group group = GroupManager.getSimpleGroup(mongoDBClient, groupId);
        if (group.isFull()) {
            return ErrorCode.ERROR_GROUP_FULL;
        }

        return 0;
    }

    protected static int userCanRejectJoinRequest(MongoDBClient mongoDBClient, String userId, String groupId, String targetId) {
        if (!checkHasPermission(mongoDBClient, userId, groupId, GroupPermission.HANDLE_REQUEST)) {
            return ErrorCode.ERROR_GROUP_PERMISSION;
        }

        return 0;
    }

    protected static int userCanExpelUser(MongoDBClient mongoDBClient, String userId, String groupId, String targetId) {
        if (!checkHasPermission(mongoDBClient, userId, groupId, GroupPermission.EXPEL_USER)) {
            return ErrorCode.ERROR_GROUP_PERMISSION;
        }
        return 0;
    }

    protected static int canInviteUser(MongoDBClient mongoDBClient, String userId, String groupId) {
        if (!checkHasPermission(mongoDBClient, userId, groupId, GroupPermission.INVITE_USER)) {
            return ErrorCode.ERROR_GROUP_PERMISSION;
        }
        return 0;
    }

    public static boolean checkHasPermission(MongoDBClient mongoDBClient, String userId, String groupId, GroupPermission permission) {
        GroupRelation relation = GroupUserManager.getUserRelationWithGroup(mongoDBClient, userId, groupId);
        return (relation != null && ((relation.getPermission() & permission.getPermission()) != 0));
    }

    private static boolean checkUserRole(MongoDBClient mongoDBClient, String userId, String groupId, GroupRole role) {
        GroupRelation relation = GroupUserManager.getUserRelationWithGroup(mongoDBClient, userId, groupId);
        return (relation != null && (relation.getRole() == role));
    }

    public static int canUpgradeGroup(MongoDBClient mongoClient, String userId, String groupId) {
        if (!checkHasPermission(mongoClient, userId, groupId, GroupPermission.UPGRADE_GROUP)) {
            return ErrorCode.ERROR_GROUP_PERMISSION;
        }
        return 0;
    }

    public static int setUserAsAdmin(MongoDBClient mongoClient, String userId, String groupId, String targetUid) {
        if (!checkHasPermission(mongoClient, userId, groupId, GroupPermission.ARRANGE_ADMIN)) {
            return ErrorCode.ERROR_GROUP_PERMISSION;
        }
        GroupRelation relation = GroupUserManager.getUserRelationWithGroup(mongoClient, targetUid, groupId);
        if (relation.getRole() == GroupRole.MEMBER) {
            updateUserRelation(mongoClient, targetUid, groupId, GroupRole.ADMIN);
            log.info("<setUserAsAdmin> add user = " + userId + ", into admin list");
            mongoClient.addToSet(DBConstants.T_GROUP, groupId, DBConstants.F_ADMINUID_LIST, new ObjectId(targetUid));
            return 0;
        }
        return ErrorCode.ERROR_GROUP_NOT_MEMBER;
    }

    public static int removeUserFromAdmin(MongoDBClient mongoClient, String userId, String groupId, String targetUid) {
        if (!checkHasPermission(mongoClient, userId, groupId, GroupPermission.ARRANGE_ADMIN)) {
            return ErrorCode.ERROR_GROUP_PERMISSION;
        }
        GroupRelation relation = GroupUserManager.getUserRelationWithGroup(mongoClient, targetUid, groupId);
        if (relation.getRole() == GroupRole.ADMIN) {
            updateUserRelation(mongoClient, targetUid, groupId, GroupRole.MEMBER);
            log.info("<removeUserFromAdmin> pull user = " + userId + ", from admin list");
            mongoClient.pullValueFromSet(DBConstants.T_GROUP, groupId, DBConstants.F_ADMINUID_LIST, new ObjectId(targetUid));
            return 0;
        }
        return ErrorCode.ERROR_GROUP_NOT_ADMIN;
    }

    private static void updateUserRelation(MongoDBClient mongoClient, String userId, String groupId, GroupRole role) {
        GroupRelation query = new GroupRelation();
        query.setGroupId(groupId);
        query.setUserId(userId);

        GroupRelation set = new GroupRelation();
        set.setRole(role);

        mongoClient.updateOne(DBConstants.T_GROUP_RELATION, query.getDbObject(), new BasicDBObject("$set", set.getDbObject()));
    }


    public static Collection<GroupRelation> getAllRoles(MongoDBClient mongoClient, String userId) {
        GroupRelation query = new GroupRelation();
        query.setUserId(userId);
        DBCursor cursor = mongoClient.findAll(DBConstants.T_GROUP_RELATION, query.getDbObject(),null);
        Set<GroupRelation> set = getDataSetFromCursor(cursor, GroupRelation.class);
        if (set == null || set.isEmpty()){
            return Collections.emptySet();
        }
        Set<ObjectId> oidSet = new HashSet<ObjectId>();

        Map<ObjectId, GroupRelation> map = new HashMap<ObjectId, GroupRelation>();

        for(GroupRelation relation : set){
            ObjectId oid = new ObjectId(relation.getGroupId());
            oidSet.add(oid);
            map.put(oid, relation);

        }

        Set<Group> groups = GroupManager.getSimpleGroupsByIds(mongoClient, oidSet, new BasicDBObject(DBConstants.F_NAME, 1));
        log.info("<getAllRoles> groups size = "+ groups.size());
        for (Group group : groups){
            GroupRelation relation = map.get(group.getObjectId());
            if (relation != null){
                log.info("<getAllRoles> match group name = " + group.getName()); 
                relation.setGroupName(group.getName());
            }
        }
        return set;
    }
}
