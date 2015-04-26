package com.orange.game.model.manager.group;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ErrorCode;
import com.orange.game.model.dao.group.*;
import com.orange.game.model.manager.CommonManager;
import com.orange.game.model.manager.MessageManager;
import com.orange.game.model.manager.group.index.GroupUserIndexManager;
import com.orange.game.model.manager.group.index.UserNoticeIndexManager;
import com.orange.game.model.service.DBService;
import org.bson.types.ObjectId;

import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-6
 * Time: 下午4:22
 * To change this template use File | Settings | File Templates.
 */
public class GroupUserManager extends CommonManager {

    public static final int HANDLE_TYPE_NONE = 0;
    public static final int HANDLE_TYPE_ACCEPT = 1;
    public static final int HANDLE_TYPE_REJECT = 2;

    public static int updateUserRoleInGroup(MongoDBClient mongoDBClient, String groupId, String targetUid, GroupRole role) {
        //get the user original role, then change it.

        GroupRelation relation = getUserRelationWithGroup(mongoDBClient, targetUid, groupId);

        DBObject query = new BasicDBObject("_id", new ObjectId(groupId));
        DBObject update = null;


        //pull the uid from the old role list && push the uid into the new role list
        if (relation == null || (relation.getRole() != role)) {
            DBObject addToSet = new BasicDBObject();
            if (role == GroupRole.GUEST) {
                //push it into guest list
                addToSet.put(DBConstants.F_GUESTUID_LIST, new ObjectId(targetUid));
            } else if (role == GroupRole.ADMIN || role == GroupRole.CREATOR) {
                //push it into admin list
                addToSet.put(DBConstants.F_ADMINUID_LIST, new ObjectId(targetUid));
            } else {
                //pass
                addToSet = null;
            }
            if (addToSet != null) {
                update = new BasicDBObject();
                update.put("$addToSet", addToSet);
            }

            if (relation != null) {
                //pull it out
                DBObject pull = new BasicDBObject();
                if (relation.getRole() == GroupRole.ADMIN) {
                    pull.put(DBConstants.F_ADMINUID_LIST, new ObjectId(targetUid));
                } else if (relation.getRole() == GroupRole.GUEST) {
                    pull.put(DBConstants.F_GUESTUID_LIST, new ObjectId(targetUid));
                } else {
                    pull = null;
                }
                if (pull != null) {
                    update = new BasicDBObject();
                    update.put("$pull", pull);
                }
            }
        }

        if (update != null) {
            mongoDBClient.updateOne(DBConstants.T_GROUP, query, update);
        }

        updateRelationRole(mongoDBClient, groupId, targetUid, role);
        return 0;
    }

    public static GroupRelation getUserRelationWithGroup(MongoDBClient mongoDBClient, String userId, String groupId) {
        DBObject query = GroupRelation.queryBy(userId, groupId);
        DBObject obj = mongoDBClient.findOne(DBConstants.T_GROUP_RELATION, query);
        if (obj != null) {
            return new GroupRelation(obj);
        }
        return GroupRelation.getDefaultRelation();
    }

    public static int acceptUser(MongoDBClient mongoDBClient, String userId, String groupId, String targetId, String gameId) {
        int resultCode = GroupRoleManager.userCanAcceptJoinRequest(mongoDBClient, userId, groupId, targetId);
        if (resultCode == 0) {
            log.info("<acceptUser> success, userId = " + userId + ", groupId = " + groupId + ", targetId = " + targetId);

            //update role.
            updateUserRoleInGroup(mongoDBClient, groupId, targetId, GroupRole.MEMBER);

            //add user into user list of group title.
            GroupTitleManager.addUserToTitle(mongoDBClient, userId, groupId, GroupTitle.MEMBER, targetId);

            //update group size
            GroupManager.increaseGroupSize(mongoDBClient, userId, groupId, 1);
            toBeMemberOfGroup(mongoDBClient, targetId, groupId, gameId);

            GroupNoticeManager.postAcceptNotice(mongoDBClient, userId, groupId, targetId);

        } else {
            log.info("<acceptUser> failed, userId = " + userId + ", ErrorCode = " + resultCode);
        }
        return resultCode;
    }

    public static int rejectUser(MongoDBClient mongoDBClient, String userId, String groupId, String targetId, String reason, String gameId) {

        int resultCode = GroupRoleManager.userCanRejectJoinRequest(mongoDBClient, userId, groupId, targetId);
        if (resultCode == 0) {
            GroupNoticeManager.postRejectNotice(mongoDBClient, userId, groupId, targetId, reason);
        }
        return resultCode;

    }

    //user and member
    public static int expelUser(MongoDBClient mongoDBClient, String userId, String groupId, String targetId, int titleId, String gameId, String message) {
        int resultCode = GroupRoleManager.userCanExpelUser(mongoDBClient, userId, groupId, targetId);
        if (resultCode == 0) {
            removeUserFromGroup(mongoDBClient, targetId, groupId, gameId);
            GroupNoticeManager.postExpelNotice(mongoDBClient, userId, groupId, targetId, message);
        }
        return resultCode;
    }

    public static int joinGroup(MongoDBClient mongoDBClient, String userId, String groupId, String message) {
        int resultCode = GroupRoleManager.userCanJoinGroup(mongoDBClient, userId, groupId);
        if (resultCode == 0) {
            Group group = GroupManager.getSimpleGroup(mongoDBClient, groupId);
            if (group == null) {
                return ErrorCode.ERROR_GROUP_NOTEXIST;
            }
            GroupNoticeManager.postRequestNotice(mongoDBClient, userId, group, message);

        }
        return resultCode;
    }

    public static int inviteMembers(MongoDBClient mongoClient, String userId, String gameId, String groupId, Set<String> userIds, int titleId) {
        if (GroupRoleManager.canInviteUser(mongoClient, userId, groupId) != 0) {
            return ErrorCode.ERROR_GROUP_PERMISSION;
        }
        Set<ObjectId> uids = getFreeUserIds(mongoClient, groupId, userIds);
        if (!uids.isEmpty()) {
            GroupNoticeManager.postInviteMemberNotice(mongoClient, userId, groupId, uids, titleId);
        }else{
             return ErrorCode.ERROR_GROUP_INVITATION;     //200026
        }
        return 0;
    }

    private static void updateRelationRole(MongoDBClient mongoClient, String groupId, String userId, GroupRole role) {
        GroupRelation query = new GroupRelation();
        query.setGroupId(groupId);
        query.setUserId(userId);

        GroupRelation update = new GroupRelation();
        update.setRole(role);
        DBObject info = new BasicDBObject("$set", update.getDbObject());

        mongoClient.updateOrInsert(DBConstants.T_GROUP_RELATION, query.getDbObject(), info);
    }

    public static int inviteGuests(MongoDBClient mongoClient, String userId, String gameId, String groupId, Set<String> userIds) {

        if (userIds == null || userIds.size() == 0) return 0;
        if (GroupRoleManager.canInviteUser(mongoClient, userId, groupId) != 0) {
            return ErrorCode.ERROR_GROUP_PERMISSION;
        }
        //filter out the member of the group.

        Set<ObjectId> newMembers = new HashSet<ObjectId>();
        for (String uid : userIds) {
            newMembers.add(new ObjectId(uid));
        }

        DBObject in = new BasicDBObject();
        in.put("$in", newMembers);
        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_UID, in);
        query.put(DBConstants.F_GROUPID, new ObjectId(groupId));

        DBCursor cursor = mongoClient.findAll(DBConstants.T_GROUP, query, null);
        Set<GroupRelation> relations = getDataSetFromCursor(cursor, GroupRelation.class);

        for (GroupRelation relation : relations){
            if (relation.getRole().isMember() || relation.getRole() == GroupRole.GUEST){
                newMembers.remove(relation.getUserIdObjectId());
            }
        }

        Set<ObjectId> uids = newMembers;

        if (!uids.isEmpty()) {
            GroupNoticeManager.postInviteGuestNotice(mongoClient, userId, groupId, uids);
        }
        return 0;
    }

    private static Set<ObjectId> getFreeUserIds(MongoDBClient mongoClient, String groupId, Set<String> userIds) {

        Set<ObjectId> newMembers = new HashSet<ObjectId>();
        for (String uid : userIds) {
            newMembers.add(new ObjectId(uid));
        }
        DBCursor cursor = mongoClient.findByFieldInValues(DBConstants.T_GROUP_RELATION, DBConstants.F_UID, newMembers, null);
        Set<GroupRelation> relations = getDataSetFromCursor(cursor, GroupRelation.class);

        log.info("<getFreeUserIds> uid count = " + relations.size());
        for (GroupRelation relation : relations) {
            if(!relation.getRole().isMember()) continue;
            ObjectId oid = relation.getUserIdObjectId();
            newMembers.remove(oid);
            log.info("<getFreeUserIds>" + "uid = " + oid + " has relation with group = " + groupId + ", role = " + relation.getRole() + ", to be removed");
        }
        return newMembers;
    }

    public static boolean isUserGroupMember(MongoDBClient mongoClient, String userId, String groupId) {
        GroupRelation relation = getUserRelationWithGroup(mongoClient, userId, groupId);
        if (relation == null) {
            return false;
        }
        return relation.getRole().isMember();
    }

    public static boolean userHasJoinedAGroup(MongoDBClient mongoClient, String userId, String gameId) {
        return !mongoClient.isListEmpty(DBConstants.T_USER, userId, DBConstants.F_GROUPS);
    }

    public static void toBeMemberOfGroup(MongoDBClient mongoDBClient, String userId, String groupId, String gameId) {
        mongoDBClient.addToSet(DBConstants.T_USER, userId, DBConstants.F_GROUPS, new ObjectId(groupId));
        FollowGroupManager.followGroup(mongoDBClient, userId, groupId);
        GroupUserIndexManager.getMemberInstance().insertIndex(groupId, userId);
        //pull the user out of the group guest list.
        mongoDBClient.pullValueFromSet(DBConstants.T_GROUP, groupId, DBConstants.F_GUESTUID_LIST, new ObjectId(userId));
    }

    private static void removeUserFromGroup(MongoDBClient mongoDBClient, String userId, String groupId, String gameId) {
        GroupRelation relation = getUserRelationWithGroup(mongoDBClient, userId, groupId);

        if (relation.getRole() == GroupRole.GUEST) {
            GroupManager.increaseGroupGuestSize(mongoDBClient, userId, groupId, -1);
            //pull it out of guest list
            mongoDBClient.pullValueFromSet(DBConstants.T_GROUP, groupId, DBConstants.F_GUESTUID_LIST, new ObjectId(userId));

        } else if (relation.getRole() == GroupRole.ADMIN) {
            //pull it out of admin list
            mongoDBClient.pullValueFromSet(DBConstants.T_GROUP, groupId, DBConstants.F_ADMINUID_LIST, new ObjectId(userId));
        }

        //if the user is member then remove it from member list, update the group size, remove relation and title.
        if (relation.getRole().isMember()) {

            //update the group size.
            GroupManager.increaseGroupSize(mongoDBClient, userId, groupId, -1);

            //remove user from group member
            GroupUserIndexManager.getMemberInstance().removeId(groupId, userId, true);

            //remove group from user table.
            mongoDBClient.pullValueFromSet(DBConstants.T_USER, userId, DBConstants.F_GROUPS, new ObjectId(groupId));

            //remove user title
            GroupTitleManager.removeUserFromTitle(mongoDBClient, userId, groupId);
       }
        //remove user relation
        DBObject query = new BasicDBObject("_id", relation.getObjectId());
        mongoDBClient.removeOne(DBConstants.T_GROUP_RELATION, query);
    }

    public static void quitGroup(MongoDBClient mongoDBClient, String userId, String groupId, String gameId) {
        removeUserFromGroup(mongoDBClient, userId, groupId, gameId);

        // add 188 coins to the group
        GroupManager.increaseGroupBalance(mongoDBClient, groupId, Group.CONST_QUIT_GROUP_FEE);
        GroupNoticeManager.postUserQuitNotice(mongoDBClient, userId, groupId, gameId);

        // delete message stat here
        MessageManager.deleteMessageStat(mongoDBClient, userId, groupId);
    }

    public static int handleJoinRequest(MongoDBClient mongoClient, String userId, String noticeId, int handleType, String reason, String gameId) {
        GroupNotice notice = GroupNoticeManager.getNoticeById(mongoClient, noticeId);
        if (notice == null) {
            return ErrorCode.ERROR_GROUP_NOTICE_NOTFOUND;
        }
        int code = 0;
        if (handleType == HANDLE_TYPE_ACCEPT) {
            code = acceptUser(mongoClient, userId, notice.getGroupId(), notice.getUserId(), gameId);

        } else if (handleType == HANDLE_TYPE_REJECT) {
            code = rejectUser(mongoClient, userId, notice.getGroupId(), notice.getUserId(), reason, gameId);
        } else {
            return ErrorCode.ERROR_GROUP_REQUEST_HANDLE_TYPE_INVALID;
        }
        if (code == 0) {
            UserNoticeIndexManager.getRequestManager().removeId(userId, noticeId);
        }
        return code;
    }

    public static Group setUserGroupMessageNotice(String groupId, String userId, boolean groupMessageNoticeOn) {

        if (StringUtil.isEmpty(userId) || StringUtil.isEmpty(groupId)){
            return null;
        }

        BasicDBObject query = new BasicDBObject("_id", new ObjectId(groupId));
        BasicDBObject update = new BasicDBObject();

        if (groupMessageNoticeOn){
            update.put("$pull", new BasicDBObject(DBConstants.F_OFF_USERS, userId));
        }
        else{
            update.put("$addToSet", new BasicDBObject(DBConstants.F_OFF_USERS, userId));
        }

        log.info("<setUserGroupMessageNotice> query="+query.toString()+", update="+update.toString());
        DBObject obj = DBService.getInstance().getMongoDBClient().findAndModify(DBConstants.T_GROUP, query, update);
        if (obj == null)
            return null;
        else
            return new Group(obj);
    }
}
