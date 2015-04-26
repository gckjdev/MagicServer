package com.orange.game.model.manager.group;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.utils.MapUtil;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ErrorCode;
import com.orange.game.model.dao.User;
import com.orange.game.model.dao.group.Group;
import com.orange.game.model.dao.group.GroupNotice;
import com.orange.game.model.dao.group.GroupRelation;
import com.orange.game.model.dao.group.GroupRole;
import com.orange.game.model.manager.CommonManager;
import com.orange.game.model.manager.MessageManager;
import com.orange.game.model.manager.UserManager;
import com.orange.game.model.manager.bbs.BBSManager;
import com.orange.game.model.manager.bbs.BBSUserActionManager;
import com.orange.game.model.manager.group.index.GroupNoticeIndexManager;
import com.orange.game.model.manager.group.index.GroupUserIndexManager;
import com.orange.game.model.manager.group.index.UserNoticeIndexManager;
import org.bson.types.ObjectId;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-11
 * Time: 上午10:07
 * To change this template use File | Settings | File Templates.
 */
public class GroupNoticeManager extends CommonManager {

    public static final int NOTICE_TYPE_NOTICE = 1;
    public static final int NOTICE_TYPE_REQUEST = 2;
    private static int BADGE_COMMENT = 1;
    private static int BADGE_REQUEST = 2;
    private static int BADGE_NOTICE = 3;
    private static int CHAT_NOTICE = 4;
    private static int CONTEST_NOTICE = 5;

    private static void fillNoticeAttributesFromDB(MongoDBClient mongoDBClient, GroupNotice notice) {
        if (notice == null){
            return;
        }

        Group group = GroupManager.getSimpleGroup(mongoDBClient, notice.getGroupId());
        if (group == null){
            return;
        }

        notice.setGroupName(group.getName());
    }

    private static void postNotice(MongoDBClient mongoDBClient, GroupNotice notice) {

        if (notice == null){
            return;
        }

        fillNoticeAttributesFromDB(mongoDBClient, notice);
        mongoDBClient.insert(DBConstants.T_GROUP_NOTICE, notice.getDbObject());
    }

    private static void pushNoticeToAdmins(MongoDBClient mongoDBClient, GroupNotice notice) {
        log.info("<pushNoticeToAdmins> group id = " + notice.getGroupId());
        Group group = GroupManager.getSimpleGroup(mongoDBClient, notice.getGroupId());
        if (group != null) {
            UserNoticeIndexManager.getNoticeManager().insertIndexWithOids(notice.getNoticeId(), group.getAdminUids(), notice.getUserId());
        }
    }

    private static void pushRequestToAdmins(MongoDBClient mongoDBClient, GroupNotice notice) {
        Group group = GroupManager.getSimpleGroup(mongoDBClient, notice.getGroupId());
        if (group != null) {
            UserNoticeIndexManager.getRequestManager().insertIndexWithOids(notice.getNoticeId(), group.getAdminUids(), notice.getUserId());
        }
    }

    private static void pushNoticeToMembers(MongoDBClient mongoDBClient, GroupNotice notice) {
        List<ObjectId> uidList = GroupUserIndexManager.getMemberInstance().getAllMemberIds(notice.getGroupId());
        UserNoticeIndexManager.getNoticeManager().insertIndexWithOids(notice.getNoticeId(), uidList, notice.getUserId());
    }

    private static void pushNoticeToTarget(MongoDBClient mongoDBClient, GroupNotice notice) {
        UserNoticeIndexManager.getNoticeManager().insertIndex(notice.getTargetUid(), notice.getNoticeId(), notice.getUserId());
    }

    public static void postAcceptNotice(MongoDBClient mongoDBClient, String userId, String groupId, String targetId) {
        GroupNotice notice = GroupNotice.getAcceptNotice(userId, groupId, targetId);
        postNotice(mongoDBClient, notice);
        pushNoticeToTarget(mongoDBClient, notice);
        pushNoticeToAdmins(mongoDBClient, notice);
    }

    public static void postRejectNotice(MongoDBClient mongoDBClient, String userId, String groupId, String targetId, String message) {
        GroupNotice notice = GroupNotice.getRejectNotice(userId, groupId, targetId, message);
        postNotice(mongoDBClient, notice);
        pushNoticeToTarget(mongoDBClient, notice);
        pushNoticeToAdmins(mongoDBClient, notice);
    }

    public static void postExpelNotice(MongoDBClient mongoDBClient, String userId, String groupId, String targetId, String message) {
        GroupNotice notice = GroupNotice.getExpelNotice(userId, groupId, targetId, message);
        postNotice(mongoDBClient, notice);
        pushNoticeToTarget(mongoDBClient, notice);
        pushNoticeToAdmins(mongoDBClient, notice);
    }

    public static void postRequestNotice(MongoDBClient mongoDBClient, String userId, Group group, String message) {
        GroupNotice notice = GroupNotice.getRequestNotice(userId, group.getGroupId(), message);
        postNotice(mongoDBClient, notice);
        pushRequestToAdmins(mongoDBClient, notice);
    }

    public static void postUserQuitNotice(MongoDBClient mongoDBClient, String userId, String groupId, String gameId) {
        GroupNotice notice = GroupNotice.getQuitNotice(userId, groupId);
        postNotice(mongoDBClient, notice);
        pushNoticeToAdmins(mongoDBClient, notice);
    }

    public static void postInviteMemberNotice(MongoDBClient mongoClient, String userId, String groupId, Set<ObjectId> uids, int titleId) {
        for (ObjectId uid : uids) {
            GroupNotice notice = GroupNotice.getInviteMemberNotice(userId, groupId, uid.toString(), titleId);
            postNotice(mongoClient, notice);
            UserNoticeIndexManager.getRequestManager().insertIndexWithOids(notice.getNoticeId(), uids, userId);
            pushNoticeToAdmins(mongoClient, notice);
        }
    }

    public static void postInviteGuestNotice(MongoDBClient mongoClient, String userId, String groupId, Set<ObjectId> uids) {
        for (ObjectId uid : uids) {
            GroupNotice notice = GroupNotice.getInviteGuestNotice(userId, groupId, uid.toString());
            postNotice(mongoClient, notice);
            UserNoticeIndexManager.getRequestManager().insertIndexWithOids(notice.getNoticeId(), uids, userId);
            pushNoticeToAdmins(mongoClient, notice);
        }
    }

    public static List<GroupNotice> getRequestNoticeList(MongoDBClient mongoDBClient, String userId, int offset, int limit) {
        List<GroupNotice> list = UserNoticeIndexManager.getRequestManager().getList(userId, offset, limit);
        return fillNoticeListResult(mongoDBClient, list);
    }

    public static List<GroupNotice> getNoticeList(MongoDBClient mongoDBClient, String userId, int offset, int limit) {
        List<GroupNotice> list = UserNoticeIndexManager.getNoticeManager().getList(userId, offset, limit);
        return fillNoticeListResult(mongoDBClient, list);
    }

    public static List<GroupNotice> fillNoticeListResult(MongoDBClient mongoDBClient, List<GroupNotice> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }

        Set<ObjectId> uidSet = new HashSet<ObjectId>();
        Set<ObjectId> gidSet = new HashSet<ObjectId>();
        for (GroupNotice notice : list) {
            if (notice.getUserId() != null) {
                uidSet.add(new ObjectId(notice.getUserId()));
            }
            if (notice.getTargetUid() != null) {
                uidSet.add(new ObjectId(notice.getTargetUid()));
            }
        }
        Set<User> userSet = UserManager.getUserSet(mongoDBClient, uidSet);
        Map<ObjectId, User> userMap = MapUtil.makeMap(userSet);

        if (MapUtil.isEmpty(userMap)) return list;

        for (GroupNotice notice : list) {
            if (notice.getUserId() != null) {
                User user = userMap.get(new ObjectId(notice.getUserId()));
                notice.setPublisher(user);
            }
            if (notice.getTargetUid() != null) {
                User user = userMap.get(new ObjectId(notice.getTargetUid()));
                notice.setTarget(user);
            }
        }
        return list;
    }

    public static UserNoticeIndexManager getIndexManager(GroupNotice notice) {
        if (notice.isRequest() || notice.isInvitation()) {
            return UserNoticeIndexManager.getRequestManager();
        }
        return UserNoticeIndexManager.getNoticeManager();
    }

    public static GroupNotice getNoticeById(MongoDBClient mongoClient, String noticeId) {
        DBObject obj = mongoClient.findOneByObjectId(DBConstants.T_GROUP_NOTICE, noticeId);
        if (obj == null) {
            return null;
        }
        return new GroupNotice(obj);
    }

    public static Map<Integer, Integer> getGroupBadges(MongoDBClient mongoClient, String groupId, String userId, String gameId) {
        //get new notice
        int noticeCount = (int) UserNoticeIndexManager.getNoticeManager().getUnreadCount(userId);
        //get new request
        int requestCount = (int) UserNoticeIndexManager.getRequestManager().getUnreadCount(userId);
        //get new reply

        int commentCount = (int) BBSUserActionManager.getInstance(BBSManager.MODE_GROUP).getUnreadCount(userId);
        log.info("<getGroupNewNoticeCount> commentCount=" + commentCount + ", noticeCount=" + noticeCount + ", requestCount=" + requestCount);

        int contestCount = 0;
        int chatCount = 0;

        if (!StringUtil.isEmpty(groupId)) {

            /* rem by Benson
            AbstractXiaoji xiaoji = XiaojiFactory.getInstance().getXiaojiByGameId(gameId);
            if (xiaoji != null){
                int category = xiaoji.getCategoryType();
                contestCount = ContestManager.getGroupOngoingContestCount(mongoClient, groupId, category);
                log.info("<getGroupNewNoticeCount> contestCount="+contestCount);
            }
            */

            chatCount = MessageManager.getNewMessageCount(userId, groupId);
            log.info("<getGroupNewNoticeCount> chatCount=" + chatCount);
        }

        Map<Integer, Integer> map = new HashMap<Integer, Integer>(3);
        map.put(BADGE_COMMENT, commentCount);
        map.put(BADGE_NOTICE, noticeCount);
        map.put(BADGE_REQUEST, requestCount);
        map.put(CHAT_NOTICE, chatCount);
        map.put(CONTEST_NOTICE, contestCount);
        return map;

    }

    public static long getGroupNewNoticeCount(MongoDBClient mongoClient, String groupId, String userId, String gameId) {

        Map<Integer, Integer> map = getGroupBadges(mongoClient, groupId, userId, gameId);
        Collection<Integer> values = map.values();
        int total = 0;
        for (Integer value : values) {
            total += value;
        }

        log.info("<getGroupNewNoticeCount> total=" + total);
        return total;
    }

    public static int handleInvitationNotice(MongoDBClient mongoClient, String userId, String noticeId, boolean accept) {
        GroupNotice notice = getNoticeById(mongoClient, noticeId);
        if (notice != null && notice.isInvitation()) {


            int result = 0;
            if (accept) {
                if (notice.getType() == GroupNotice.TypeInviteMember) {
                    result = acceptMemberInvitation(mongoClient, userId, notice);
                } else {
                    result = acceptGuestInvitation(mongoClient, userId, notice);
                }
            } else {
                if (notice.getType() == GroupNotice.TypeInviteMember) {
                    result = rejectMemberInvitation(mongoClient, userId, notice);
                } else {
                    result = rejectGuestInvitation(mongoClient, userId, notice);
                }

            }
            return result;
        }
        return 0;
    }

    private static int rejectGuestInvitation(MongoDBClient mongoClient, String userId, GroupNotice notice) {
        ignoreNotice(userId, notice);
        postRejectGuestInvitationNotice(mongoClient, userId, notice.getGroupId());
        return 0;
    }

    private static int rejectMemberInvitation(MongoDBClient mongoClient, String userId, GroupNotice notice) {
        ignoreNotice(userId, notice);
        postRejectMemberInvitationNotice(mongoClient, userId, notice.getGroupId());
        return 0;
    }

    private static void postRejectGuestInvitationNotice(MongoDBClient mongoClient, String userId, String groupId) {
        GroupNotice notice = GroupNotice.getRejectGuestInvitationNotice(userId, groupId);
        postNotice(mongoClient, notice);
        pushNoticeToAdmins(mongoClient, notice);
    }

    private static void postRejectMemberInvitationNotice(MongoDBClient mongoClient, String userId, String groupId) {
        GroupNotice notice = GroupNotice.getRejectMemberInvitationNotice(userId, groupId);
        postNotice(mongoClient, notice);
        pushNoticeToAdmins(mongoClient, notice);
    }

    private static void postAcceptMemberInvitationNotice(MongoDBClient mongoClient, String userId, String groupId) {
        GroupNotice notice = GroupNotice.getAcceptMemberInvitationNotice(userId, groupId);
        postNotice(mongoClient, notice);
        pushNoticeToAdmins(mongoClient, notice);
    }

    private static void postAcceptGuestInvitationNotice(MongoDBClient mongoClient, String userId, String groupId) {
        GroupNotice notice = GroupNotice.getAcceptGuestInvitationNotice(userId, groupId);
        postNotice(mongoClient, notice);
        pushNoticeToAdmins(mongoClient, notice);
    }

    private static int acceptGuestInvitation(MongoDBClient mongoClient, String userId, GroupNotice notice) {

        GroupRelation relation = GroupUserManager.getUserRelationWithGroup(mongoClient, userId, notice.getGroupId());
        if (!relation.getRole().isMember() && !relation.getRole().isGuest()) {
            Group group = GroupManager.getSimpleGroup(mongoClient, notice.getGroupId());
            if (group == null){
                return ErrorCode.ERROR_GROUP_NOTEXIST;
            }
            if (group.isGuestFull()) {
                return ErrorCode.ERROR_GROUP_FULL;
            }
            log.info("<acceptGuestInvitation>user = " + userId + " is invitee, groupId = " + notice.getGroupId());
            GroupUserManager.updateUserRoleInGroup(mongoClient, notice.getGroupId(), notice.getTargetUid(), GroupRole.GUEST);
            FollowGroupManager.followGroup(mongoClient, userId, notice.getGroupId());
            GroupManager.increaseGroupGuestSize(mongoClient, userId, notice.getGroupId(), 1);
            postAcceptGuestInvitationNotice(mongoClient, userId, notice.getGroupId());
        } else {
            log.info("<acceptGuestInvitation>user = " + userId + " is already member or guest, groupId = " + notice.getGroupId());
        }
        ignoreNotice(userId, notice);
        return 0;

    }

    private static int acceptMemberInvitation(MongoDBClient mongoClient, String userId, GroupNotice notice) {
        if (GroupUserManager.userHasJoinedAGroup(mongoClient, userId, notice.getGroupId())) {
            return ErrorCode.ERROR_GROUP_MULTIJOINED;
        }
        GroupRelation relation = GroupUserManager.getUserRelationWithGroup(mongoClient, userId, notice.getGroupId());
        if (!relation.getRole().isMember()) {
            Group group = GroupManager.getSimpleGroup(mongoClient, notice.getGroupId());
            if (group == null){
                return ErrorCode.ERROR_GROUP_NOTEXIST;
            }

            if (group.isFull()) {
                return ErrorCode.ERROR_GROUP_FULL;
            }
            log.info("<acceptGuestInvitation>user = " + userId + " is invitee, groupId = " + notice.getGroupId());
            GroupUserManager.updateUserRoleInGroup(mongoClient, notice.getGroupId(), notice.getTargetUid(), GroupRole.MEMBER);
            GroupUserManager.toBeMemberOfGroup(mongoClient, notice.getTargetUid(), notice.getGroupId(), null);
            GroupTitleManager.addUserToTitle(mongoClient, notice.getUserId(), notice.getGroupId(), notice.getTitleId(), notice.getTargetUid());
            GroupManager.increaseGroupSize(mongoClient, notice.getUserId(), notice.getGroupId(), 1);
            postAcceptMemberInvitationNotice(mongoClient, userId, notice.getGroupId());
        } else {
            log.info("<acceptGuestInvitation>user = " + userId + " is already member, groupId = " + notice.getGroupId());
        }
        ignoreNotice(userId, notice);
        return 0;
    }

    public static void ignoreNotice(MongoDBClient mongoDBClient, String userId, String noticeId) {
        GroupNotice notice = getNoticeById(mongoDBClient, noticeId);
        ignoreNotice(userId, notice);
    }

    private static void ignoreNotice(String userId, GroupNotice notice) {
        UserNoticeIndexManager manager = getIndexManager(notice);
        manager.removeId(userId, notice.getNoticeId());
    }

    public static void postChargeGroupNotice(MongoDBClient mongoClient, String userId, String groupId, int amount) {
        GroupNotice notice = GroupNotice.getChargeGroupNotice(userId, groupId, amount);
        postNotice(mongoClient, notice);
        pushNoticeToAdmins(mongoClient, notice);
        GroupNoticeIndexManager.getChargeIndexManager().insertAndConstructIndex(groupId, notice.getNoticeId(),false);
    }

    public static void postTransferBalanceNotice(MongoDBClient mongoClient, String userId, String groupId, int amount, String targetUid) {
        GroupNotice notice = GroupNotice.getTransferBalanceNotice(userId, groupId, targetUid, amount);
        postNotice(mongoClient, notice);
        pushNoticeToAdmins(mongoClient, notice);
        pushNoticeToTarget(mongoClient, notice);
    }

    public static void postSystemDeductGroupFeeNotice(MongoDBClient client, Group group) {
        GroupNotice notice = GroupNotice.getSystemDeductGroupFeeNotice(group.getGroupId(), group.getMonthlyFee());
        postNotice(client, notice);
        pushNoticeToAdmins(client, notice);
    }

    public static void postSystemDeductGroupFeeFailNotice(MongoDBClient client, Group group) {
        GroupNotice notice = GroupNotice.getSystemDeductGroupFeeFailNotice(group.getGroupId(), group.getMonthlyFee());
        postNotice(client, notice);
        pushNoticeToAdmins(client, notice);
    }

    public static List<GroupNotice> getNotices(MongoDBClient mongoDBClient, String groupId, int type, int offset, int limit) {
        GroupNotice notice = new GroupNotice();
        notice.setGroupId(groupId);
        notice.setType(type);
        DBObject orderBy = new BasicDBObject("_id", -1);
        log.info("get notices, query = "+notice.getDbObject());
        DBCursor cursor = mongoDBClient.find(DBConstants.T_GROUP_NOTICE, notice.getDbObject(), orderBy, offset, limit);
        return getDataListFromCursor(cursor, GroupNotice.class);
    }
}
