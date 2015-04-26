package com.orange.game.model.manager.group;


import com.orange.common.utils.StringUtil;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.scheduler.ScheduleService;
import com.orange.common.utils.DateUtil;
import com.orange.common.utils.MapUtil;
import com.orange.common.utils.PropertyUtil;
import com.orange.game.api.service.ElasticsearchService;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ErrorCode;
import com.orange.game.model.common.MongoGetIdListUtils;
import com.orange.game.model.dao.User;
import com.orange.game.model.dao.group.Group;
import com.orange.game.model.dao.group.GroupPermission;
import com.orange.game.model.dao.group.GroupRole;
import com.orange.game.model.dao.group.GroupTitle;
import com.orange.game.model.manager.CommonManager;
import com.orange.game.model.manager.UserManager;
import com.orange.game.model.manager.group.exception.GroupException;
import com.orange.game.model.manager.group.index.GroupIndexManager;
import com.orange.game.model.manager.group.index.GroupUserIndexManager;
import com.orange.game.model.manager.group.index.UserTopicIndexManager;
import com.orange.game.model.service.DBService;
import org.bson.types.ObjectId;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-2
 * Time: 上午10:42
 * To change this template use File | Settings | File Templates.
 */


public class GroupManager extends CommonManager {

    static {
        ScheduleService.getInstance().scheduleEveryMonth(1, new Runnable() {
            @Override
            public void run() {
                if (PropertyUtil.getIntProperty("group.enable_deduct_monthly_fee", 0) == 1){
                    log.info("Start to deductAllGroupsFeeMonthly");
                    GroupManager.deductAllGroupsFeeMonthly();
                    log.info("finish deductAllGroupsFeeMonthly");
                }
                else{
                    log.info("<GroupManager> group.enable_deduct_monthly_fee not enabled, skip monthly charge group feed");
                }
            }
        });
    }

    public static Group createGroup(MongoDBClient mongoClient, String userId, String gameId, String name, int level) throws GroupException {


        log.info("<createGroup> userID = " + userId + ", group name = " + name + ", level = " + level);

        if (isGroupNameExisted(mongoClient, name)) {
            log.info("<createGroup> failed, name = " + name + " duplicated group name!");
            throw new GroupException.DuplicateGroupNameException();
        }

        if (GroupUserManager.userHasJoinedAGroup(mongoClient, userId, gameId)) {
            throw new GroupException.MultipleJoinException();
        }


        Group group = new Group(new BasicDBObject());
        group.setObjectId(new ObjectId());
        group.setCreatorUid(userId);
        group.setName(name);
        group.setLevel(level);
        group.setCreateDate(new Date());
        group.setGameId(gameId);
        group.setSize(1);
        group.setStatus(Group.StatusNormal);
        group.addAdminId(userId);

        if (mongoClient.insert(DBConstants.T_GROUP, group.getDbObject())) {

            //make user creator
            GroupUserManager.updateUserRoleInGroup(mongoClient, group.getGroupId(), userId, GroupRole.CREATOR);

            //make user as a member of the group.
            GroupUserManager.toBeMemberOfGroup(mongoClient, userId, group.getGroupId(), gameId);

            //init active/fame/balance/new index
            GroupIndexManager.initNewGroup(group);

            //follow my group;
            FollowGroupManager.followGroup(mongoClient, userId, group.getGroupId());

            //add default title(Member)
            GroupTitle memberTitle = GroupRole.MEMBER.groupTitle();
            memberTitle.setGroupId(group.getGroupId());
            memberTitle.addUid(new ObjectId(userId));
            GroupTitleManager.createTitle(mongoClient, memberTitle);
/*    
            //add creator title.
            GroupTitle creatorTitle = GroupRole.CREATOR.groupTitle();
            creatorTitle.setGroupId(group.getGroupId());
            creatorTitle.addUid(new ObjectId(userId));
            GroupTitleManager.createTitle(mongoClient, creatorTitle);

            //add admin title.
            GroupTitle adminTitle = GroupRole.ADMIN.groupTitle();
            adminTitle.setGroupId(group.getGroupId());
            GroupTitleManager.createTitle(mongoClient, adminTitle);
*/

            log.info("<createGroup> successfully! name = " + name);

            //make ES index
            ElasticsearchService.addOrUpdateIndex(group, mongoClient);

            //load member for return
            group.loadAdminsAndGuests(mongoClient);

            return group;
        }
        return null;
    }

    private static boolean isGroupNameExisted(MongoDBClient mongoClient, String name) {
        long count = mongoClient.count(DBConstants.T_GROUP, new BasicDBObject(DBConstants.F_NAME, name));
        return count > 0;
    }

    private static int guestCapacityForLevel(int level) {
        return level * 5;
    }

    private static int capacityForLevel(int level) {
        return level * 10;
    }

    public static void loadAdminsAndGuests(MongoDBClient mongoDBClient, Group group) {

        if (group == null){
            return;
        }

        ObjectId creatorOid = group.getCreatorUid();
        Set<ObjectId> adminOids = new HashSet<ObjectId>();
        Set<ObjectId> guestOids = new HashSet<ObjectId>();
        Set<ObjectId> totalUids = new HashSet<ObjectId>();

        adminOids.addAll(group.getAdminUids());
        guestOids.addAll(group.getGuestUids());

        if (creatorOid != null) {
            totalUids.add(creatorOid);
        }
        totalUids.addAll(adminOids);
        totalUids.addAll(guestOids);

        Set<User> users = UserManager.getUserSet(mongoDBClient, totalUids);

        List<User> admins = new ArrayList<User>();
        List<User> guests = new ArrayList<User>();


        for (User user : users) {
            if (guestOids.contains(user.getObjectId())) {
                guests.add(user);
            } else {
                if (user.getUserId().equalsIgnoreCase(creatorOid.toString())) {
                    group.setCreator(user);
                }
                if (adminOids.contains(user.getObjectId())) {
                    admins.add(user);
                }
            }
        }
        group.setAdmins(admins);
        group.setGuests(guests);
    }

    public static Group getGroup(MongoDBClient mongoClient, String userId, String groupId) {
        DBObject obj = mongoClient.findOneByObjectId(DBConstants.T_GROUP, groupId);
        if (obj != null) {
            Group group = new Group(obj);
            group.loadAdminsAndGuests(mongoClient);
            return group;
        }
        return null;
    }

    public static Group getSimpleGroup(MongoDBClient mongoClient, String groupId) {
        DBObject object = mongoClient.findOneByObjectId(DBConstants.T_GROUP, groupId);
        if (object != null) {
            return new Group(object);
        }
        return null;
    }

    public static void increaseGroupSize(MongoDBClient mongoDBClient, String userId, String groupId, int inc) {
        mongoDBClient.inc(DBConstants.T_GROUP, new ObjectId(groupId), DBConstants.F_SIZE, inc);
    }

    public static void increaseGroupGuestSize(MongoDBClient mongoDBClient, String userId, String groupId, int inc) {
        mongoDBClient.inc(DBConstants.T_GROUP, new ObjectId(groupId), DBConstants.F_GUEST_SIZE, inc);
    }

    public static int upgradeGroup(MongoDBClient mongoClient, String userId, String groupId, int level) {
        int code = GroupRoleManager.canUpgradeGroup(mongoClient, userId, groupId);
        if (code != 0) return code;
        Group group = getSimpleGroup(mongoClient, groupId);
        if (group == null) {
            return ErrorCode.ERROR_GROUP_NOTEXIST;
        }
        if (group.getLevel() >= level) {
            return ErrorCode.ERROR_GROUP_LEVEL_SMALL;
        }

        int fee = Group.upgradeFee(group.getLevel(), level);

        DBObject update = Group.getUpdateByLevel(level);
        DBObject query = new BasicDBObject("_id", new ObjectId(groupId));

        mongoClient.updateOne(DBConstants.T_GROUP, query, new BasicDBObject("$set", update));

        increaseGroupBalance(mongoClient, groupId, -fee);

        return 0;
    }

    public static void increaseGroupBalance(MongoDBClient mongoClient, String groupId, int amount) {
        mongoClient.inc(DBConstants.T_GROUP, new ObjectId(groupId), DBConstants.F_GROUP_BALANCE, amount);
        GroupIndexManager.balanceManager().increaseScore(groupId, amount);
    }

    public static void increaseGroupBalance(MongoDBClient mongoClient, String groupId, int amount, Date date) {

        String chargeMonth = DateUtil.dateToChineseStringByFormat(date, "yyyyMM");
        BasicDBObject incValue = new BasicDBObject(DBConstants.F_GROUP_BALANCE, amount);
        BasicDBObject update = new BasicDBObject("$inc", incValue);
        update.put("$set", new BasicDBObject(DBConstants.F_LAST_CHARGE_MONTH, chargeMonth));
        DBObject query = new BasicDBObject("_id", new ObjectId(groupId));
        log.info("<increaseGroupBalance> query="+query.toString()+", update="+update.toString());
        mongoClient.updateOne(DBConstants.T_GROUP, query, update);

        GroupIndexManager.balanceManager().increaseScore(groupId, amount);
    }

    public static void postNewTopicToRelatedUsers(MongoDBClient mongoClient, String groupId, String topicId) {
        Set<ObjectId> objectIdSet = new HashSet<ObjectId>();
        Group group = getSimpleGroup(mongoClient, groupId);
        if (group != null) {
            List<ObjectId> admins = group.getAdminUids();
            if (admins != null && !admins.isEmpty()) {
                objectIdSet.addAll(admins);
                log.info("<postNewTopicToRelatedUsers> topic id = " + topicId + ", admin list size = " + admins.size());
            }
            List<ObjectId> guests = group.getGuestUids();
            if (guests != null && !guests.isEmpty()) {
                objectIdSet.addAll(guests);
                log.info("<postNewTopicToRelatedUsers> topic id = " + topicId + ", guest list size = " + guests.size());
            }
            List<ObjectId> members = GroupUserIndexManager.getMemberInstance().getAllMemberIds(groupId);
            if (members != null && !members.isEmpty()) {
                objectIdSet.addAll(members);
                log.info("<postNewTopicToRelatedUsers> topic id = " + topicId + ", member list size = " + members.size());
            }
            log.info("<postNewTopicToRelatedUsers> topic id = " + topicId + ", all user list size = " + objectIdSet.size());
            UserTopicIndexManager.getTimelineInstance().insertIndex(objectIdSet, topicId);
        }

    }

    public static List<Group> searchGroup(MongoDBClient mongoClient, String keyword, int offset, int limit) {
        List<ObjectId> list = ElasticsearchService.search(keyword, Group.getSearchCandidateFields(), DBConstants.ES_GROUP_ID, offset, limit, DBConstants.ES_INDEX_TYPE_GROUP);

        if (list == null || list.isEmpty()) return Collections.emptyList();

        MongoGetIdListUtils<Group> util = new MongoGetIdListUtils<Group>();
        DBObject returnFields = Group.getSimpleReturnFields();
        return util.getList(mongoClient, DBConstants.T_GROUP, "_id", null, 1, list, returnFields, Group.class);
    }

    public static int editGroup(MongoDBClient mongoClient, String userId, String groupId, String name, String description, String signature, int fee) {
        if (!GroupRoleManager.checkHasPermission(mongoClient, userId, groupId, GroupPermission.EDIT_GROUP)) {
            return ErrorCode.ERROR_PARAMETER_PERMISSION_NOT_ENOUGH;
        }

        Group group = new Group();

        if (name != null && name.length() != 0) {
            if (isGroupNameExisted(mongoClient, name)) {
                return new GroupException.DuplicateGroupNameException().getErrorCode();
            }
            group.setName(name);
        }
        if (description != null) {
            group.setDesc(description);
        }

        if (signature != null) {
            group.setSignature(signature);
        }
        if (fee >= 0) {
            group.setMemberFee(fee);
        }

        DBObject update = group.getDbObject();

        log.info("<editGroup> groupId = " + groupId + ", update = " + update);

//        mongoClient.updateOneById(DBConstants.T_GROUP, groupId, new BasicDBObject("$set", update));

        DBObject query = new BasicDBObject("_id", new ObjectId(groupId));
        DBObject obj = mongoClient.findAndModify(DBConstants.T_GROUP, query, new BasicDBObject("$set", update));
        if (obj != null) {
            log.info("EditGroup done!! update group index in ES");
            group = new Group(obj);
            ElasticsearchService.addOrUpdateIndex(group, mongoClient);
        }

        return 0;
    }

    public static int updateGroupBGImage(MongoDBClient mongoClient, String userId, String groupId, String imageURL) {
        if (!GroupRoleManager.checkHasPermission(mongoClient, userId, groupId, GroupPermission.EDIT_GROUP)) {
            return ErrorCode.ERROR_GROUP_PERMISSION;
        }
        Group setter = new Group();
        setter.setBGImage(imageURL);
        mongoClient.updateOneById(DBConstants.T_GROUP, groupId, new BasicDBObject("$set", setter.getDbObject()));
        return 0;
    }

    public static int updateGroupMedalImage(MongoDBClient mongoClient, String userId, String groupId, String imageURL) {
        if (!GroupRoleManager.checkHasPermission(mongoClient, userId, groupId, GroupPermission.EDIT_GROUP)) {
            return ErrorCode.ERROR_GROUP_PERMISSION;
        }
        Group setter = new Group();
        setter.setMedalImage(imageURL);
        mongoClient.updateOneById(DBConstants.T_GROUP, groupId, new BasicDBObject("$set", setter.getDbObject()));
        return 0;
    }

    public static List<User> getAllGroupGuests(MongoDBClient mongoClient, String groupId) {
        Group group = getSimpleGroup(mongoClient, groupId);
        if (group == null) {
            return Collections.emptyList();
        }
        return UserManager.getUserList(mongoClient, group.getGuestUids());
    }

    public static List<ObjectId> getAllGroupGuestIdList(MongoDBClient mongoClient, String groupId) {
        Group group = getSimpleGroup(mongoClient, groupId);
        if (group == null) {
            return Collections.emptyList();
        }
        return group.getGuestUids();
    }

    public static List<ObjectId> getAllGroupMemberAndGuestIdList(MongoDBClient mongoClient, String groupId) {
        List<ObjectId> guestIdList = Collections.emptyList();
        Group group = getSimpleGroup(mongoClient, groupId);
        return getAllGroupMemberAndGuestIdList(mongoClient, group);

//        if (group != null) {
//            guestIdList = group.getGuestUids();
//        }
//
//        List<ObjectId> memberIdList = GroupUserIndexManager.getMemberInstance().getAllMemberIds(groupId);
//
//        List<ObjectId> retList = new ArrayList<ObjectId>();
//        retList.addAll(guestIdList);
//        retList.addAll(memberIdList);
//
//        return retList;
    }

    public static List<ObjectId> getAllGroupMemberAndGuestIdList(MongoDBClient mongoClient, Group group) {
        List<ObjectId> guestIdList = Collections.emptyList();
        if (group != null) {
            guestIdList = group.getGuestUids();
        } else {
            return Collections.emptyList();
        }

        List<ObjectId> memberIdList = GroupUserIndexManager.getMemberInstance().getAllMemberIds(group.getGroupId());

        List<ObjectId> retList = new ArrayList<ObjectId>();
        retList.addAll(guestIdList);
        retList.addAll(memberIdList);

        return retList;
    }

    public static List<ObjectId> getAllGroupMemberAndGuestIdListForNotice(MongoDBClient mongoClient, Group group) {
        List<ObjectId> guestIdList = Collections.emptyList();
        if (group != null) {
            guestIdList = group.getGuestUids();
        } else {
            return Collections.emptyList();
        }

        List<String> offUserStringIds = group.getOffUsers();
        List<ObjectId> offUserIds = new ArrayList<ObjectId>();
        for (String id : offUserStringIds) {
            offUserIds.add(new ObjectId(id));
        }

        List<ObjectId> memberIdList = GroupUserIndexManager.getMemberInstance().getAllMemberIds(group.getGroupId());

        List<ObjectId> retList = new ArrayList<ObjectId>();
        retList.addAll(guestIdList);
        retList.addAll(memberIdList);

        // remove off user ids
        retList.removeAll(offUserIds);
        return retList;
    }

    public static boolean isGroupMember(String groupId, String userId) {
        if (StringUtil.isEmpty(groupId) || StringUtil.isEmpty(userId)) {
            return false;
        }

        return GroupUserIndexManager.getMemberInstance().isIdExistInList(groupId, userId);
    }

    public static boolean isGroupGuest(String groupId, String userId) {
        if (StringUtil.isEmpty(groupId) || StringUtil.isEmpty(userId) || !ObjectId.isValid(userId)) {
            return false;
        }

        List<ObjectId> guestIdList = Collections.emptyList();
        Group group = getSimpleGroup(DBService.getInstance().getMongoDBClient(), groupId);
        if (group != null) {
            guestIdList = group.getGuestUids();
        }

        if (guestIdList.contains(new ObjectId(userId))) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isGroupMemberOrGuest(String groupId, String userId) {
        return (isGroupGuest(groupId, userId) || isGroupMember(groupId, userId));
    }

    public static List<ObjectId> getAllGroupMemberIdList(MongoDBClient mongoClient, String groupId) {
        List<ObjectId> memberIdList = GroupUserIndexManager.getMemberInstance().getAllMemberIds(groupId);
        return memberIdList;
    }

    public static List<User> getAllGroupAdmins(MongoDBClient mongoClient, String groupId) {
        Group group = getSimpleGroup(mongoClient, groupId);
        if (group == null) {
            return Collections.emptyList();
        }
        return UserManager.getUserList(mongoClient, group.getAdminUids());
    }

    public static Set<Group> getSimpleGroupsByIds(Collection<ObjectId> ids) {
        return getSimpleGroupsByIds(DBService.getInstance().getMongoDBClient(), ids, getDefaultGroupReturnFields());
    }

    public static Map<String, Group> getGroupMapByIds(Collection<ObjectId> ids) {
        Set<Group> groups = getGroupsAllDataByIds(DBService.getInstance().getMongoDBClient(), ids, getDefaultGroupReturnFields());
        if (groups == null || groups.size() == 0) {
            return Collections.emptyMap();
        }

        HashMap<String, Group> groupMap = new HashMap<String, Group>();
        for (Group group : groups) {
            groupMap.put(group.getGroupId(), group);
        }
        return groupMap;
    }

    private static DBObject getDefaultGroupReturnFields() {
        BasicDBObject fields = new BasicDBObject();
        fields.put(DBConstants.F_GUESTUID_LIST, 0);
        fields.put(DBConstants.F_ADMINUID_LIST, 0);
        return fields;
    }

    public static Set<Group> getSimpleGroupsByIds(MongoDBClient mongoClient, Collection<ObjectId> ids, DBObject returnFields) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptySet();
        }
        DBCursor cursor = mongoClient.findByFieldInValues(DBConstants.T_GROUP, "_id", ids, returnFields);
        return getDataSetFromCursor(cursor, Group.class);
    }

    public static Set<Group> getGroupsAllDataByIds(MongoDBClient mongoClient, Collection<ObjectId> ids, DBObject returnFields) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptySet();
        }
        DBCursor cursor = mongoClient.findByFieldInValues(DBConstants.T_GROUP, "_id", ids, null);
        return getDataSetFromCursor(cursor, Group.class);
    }

    public static int dismissalGroup(MongoDBClient mongoClient, String userId, String groupId) {
        //TODO

        /*
        1. post a notification to all members && all guests

        2. remove members

        3. pull the group out of user group list.

        4. pull the group out of user follow group list

        5. remove all the group roles from group relation table.

        6. remove all the group titles from group title table.

        7. pull the group out of fame/active/balance/level index.

        8. delete index from elastic search.

        9. delete the group

         */

        if (!GroupRoleManager.checkHasPermission(mongoClient, userId, groupId, GroupPermission.DISMISSAL_GROUP)) {
            return ErrorCode.ERROR_GROUP_PERMISSION;
        }

        Group group = getSimpleGroup(mongoClient, groupId);
        if (group != null) {
            List<ObjectId> admins = group.getAdminUids();
            List<ObjectId> guests = group.getGuestUids();

            if (guests != null && guests.size() > 0){
                return ErrorCode.ERROR_GROUP_DELETE_HAS_GUEST;
            }

            List<ObjectId> members = GroupUserIndexManager.getMemberInstance().getAllMemberIds(groupId);
            if (members != null && members.size() > 1){
                return ErrorCode.ERROR_GROUP_DELETE_HAS_MEMBER;
            }

            //remove members
            GroupUserIndexManager.getMemberInstance().removeIndex(groupId, true);

            //pull group out of user groups
            DBObject in = new BasicDBObject("$in", members);
            DBObject query = new BasicDBObject("_id", in);
            mongoClient.pullValueFromSet(DBConstants.T_USER, query, DBConstants.F_GROUPS, new ObjectId(groupId), true);

            //remove all relations
            query = new BasicDBObject(DBConstants.F_GROUPID, new ObjectId(groupId));
            mongoClient.remove(DBConstants.T_GROUP_RELATION, query);

            //remove all titles
            query = new BasicDBObject(DBConstants.F_GROUPID, new ObjectId(groupId));
            mongoClient.remove(DBConstants.T_GROUP_TITLE, query);

            //remove from active... index
            GroupIndexManager.removeAllIndex(groupId);

            // save to deleted group
            mongoClient.insert(DBConstants.T_DELETE_GROUP, group.getDbObject());

            // TODO delete group contest?

            // TODO delete group chat message?

            //at last remove the group.
            mongoClient.removeOne(DBConstants.T_GROUP, new BasicDBObject("_id", new ObjectId(groupId)));

        }

        return 0;
    }

    public static int chargeBalance(MongoDBClient mongoClient, String userId, String groupId, int amount) {
        User user = UserManager.findUserByUserId(mongoClient, userId, UserManager.getUserPublicReturnFields());
        if (user != null && user.getBalance() >= amount) {
//            Group group = getSimpleGroup(mongoClient, groupId);
            GroupManager.increaseGroupBalance(mongoClient, groupId, amount);
            GroupNoticeManager.postChargeGroupNotice(mongoClient, userId, groupId, amount);
            return 0;
        }
        return ErrorCode.ERROR_BALANCE_NOT_ENOUGH;
    }

    public static int forceChargeBalance(MongoDBClient mongoClient, String groupId, int amount) {
        Group group = getSimpleGroup(mongoClient, groupId);
        log.info("<forceChargeBalance> groupId=" + groupId + ", name=" + group.getName() + ", amount=" + amount);
        GroupManager.increaseGroupBalance(mongoClient, groupId, amount);
        return 0;
    }

    public static int systemDeductBalance(String groupId, int amount) {

        if (groupId == null || amount == 0) {
            log.warn("<systemDeductBalance> but groupId=" + groupId + ", amount=" + amount);
            return 0;
        }

        MongoDBClient mongoClient = DBService.getInstance().getMongoDBClient();
//        Group group = getSimpleGroup(mongoClient, groupId);
//        if (group == null) {
//            log.warn("<systemDeductBalance> groupId not found, id=" + groupId);
//            return ErrorCode.ERROR_GROUP_NOTEXIST;
//        }
//
//        if (group.getBalance() < amount) {
//            log.warn("<systemDeductBalance> balance +" + group.getBalance() + " not enough, groupId=" + groupId + " amount=" + amount);
//            return ErrorCode.ERROR_GROUP_BALANCE_NOT_ENOUGH;
//        }

//        mongoClient.inc(DBConstants.T_GROUP, new ObjectId(groupId), DBConstants.F_GROUP_BALANCE, -amount);

        GroupManager.increaseGroupBalance(mongoClient, groupId, -amount);
        GroupNoticeManager.postTransferBalanceNotice(mongoClient, DBConstants.SYSTEM_USERID, groupId, amount, DBConstants.SYSTEM_USERID);
//        GroupNoticeManager.postTransferBalanceNotice(mongoClient, userId, groupId, amount, targetUid);

//        log.info("<systemDeductBalance> groupId=" + groupId + " deduct amount=" + amount + ", before balance=" + group.getBalance());
        return 0;
    }

    public static int transferBalance(MongoDBClient mongoClient, String userId, String groupId, int amount, String targetUid) {
        if (!GroupRoleManager.checkHasPermission(mongoClient, userId, groupId, GroupPermission.EDIT_GROUP)) {
            return ErrorCode.ERROR_GROUP_PERMISSION;
        }
        Group group = getSimpleGroup(mongoClient, groupId);
        if (group != null && group.getBalance() + amount >= 0) {
            GroupManager.increaseGroupBalance(mongoClient, groupId, amount);
            UserManager.chargeAccount(mongoClient, targetUid, -amount, DBConstants.C_CHARGE_SOURCE_GROUP, UserManager.BALANCE_TYPE_COINS);
            // post a note to receiver && admins
            GroupNoticeManager.postTransferBalanceNotice(mongoClient, userId, groupId, amount, targetUid);
            return 0;
        }
        return ErrorCode.ERROR_GROUP_BALANCE_NOT_ENOUGH;
    }

    public static void fillUsersWithGroups(List<User> friendsList) {
        Set<ObjectId> set = new HashSet<ObjectId>();
        for (User user : friendsList) {
            ObjectId gid = user.getFirstGroupId();
            if (gid != null) {
                set.add(gid);
            }
        }
        if (!set.isEmpty()) {
            Set<Group> groupSet = GroupManager.getSimpleGroupsByIds(set);
            Map<ObjectId, Group> map = MapUtil.makeMap(groupSet);

            for (User user : friendsList) {
                ObjectId gid = user.getFirstGroupId();
                if (gid != null) {
                    Group group = map.get(gid);
                    user.setGroup(group);
                }
            }
        }
    }

    private static void deductGroupsFeeMonthly(MongoDBClient mongoDBClient, Group group) {
        MongoDBClient client = mongoDBClient;

        Date now = new Date();
        if (group.hasChargeCurrentMonth(now)){
            log.info("<deductGroupsFeeMonthly> but group "+group.getGroupId()+" last charge month is "+group.getLastChargeMonth()+", skip");
            return;
        }

        if (group.getBalance() >= group.getMonthlyFee()) {
            GroupNoticeManager.postSystemDeductGroupFeeNotice(client, group);
        } else {

            // get coins from user
            int resultCode = GroupManager.chargeBalance(mongoDBClient,
                    group.getCreatorUid().toString(),
                    group.getGroupId(),
                    group.getMonthlyFee());

            if (resultCode != 0){
                // user balance not enough, post notification
                GroupNoticeManager.postSystemDeductGroupFeeFailNotice(client, group);
            }
        }
        increaseGroupBalance(client, group.getGroupId(), -group.getMonthlyFee(), now);
    }

    public static void deductAllGroupsFeeMonthly() {
        DBObject returnFields = new BasicDBObject();
        returnFields.put(DBConstants.F_LEVEL, 1);
        returnFields.put(DBConstants.F_GROUP_BALANCE, 1);
        returnFields.put(DBConstants.F_LAST_CHARGE_MONTH, 1);
        MongoDBClient client = DBService.getInstance().getMongoDBClient();
        DBCursor cursor = client.findAll(DBConstants.T_GROUP, null, null);
        Set<Group> groups = getDataSetFromCursor(cursor, Group.class);
        for (Group group : groups) {
            deductGroupsFeeMonthly(client, group);
        }
    }

    public static void testAllGroupsFeeMonthly() {
        DBObject returnFields = new BasicDBObject();
        returnFields.put(DBConstants.F_LEVEL, 1);
        returnFields.put(DBConstants.F_GROUP_BALANCE, 1);
        returnFields.put(DBConstants.F_LAST_CHARGE_MONTH, 1);
        MongoDBClient client = DBService.getInstance().getMongoDBClient();
        DBCursor cursor = client.findAll(DBConstants.T_GROUP, new BasicDBObject("create_uid", new ObjectId("4fc3089a26099b2ca8c7a4ab")), null);
        Set<Group> groups = getDataSetFromCursor(cursor, Group.class);
        for (Group group : groups) {
            deductGroupsFeeMonthly(client, group);
        }
    }


    public static Group getSimpleGroupByUserId(MongoDBClient mongoClient, String userId) {
        ObjectId gid = getGroupIdByUserId(mongoClient, userId);
        if (gid != null) {
            log.info("<getSimpleGroupByUserId> userId = " + userId + ", groupId = " + gid.toString());
            return getSimpleGroup(mongoClient, gid.toString());
        }
        return null;
    }

    public static ObjectId getGroupIdByUserId(MongoDBClient mongoClient, String userId) {
        User user = UserManager.findPublicUserInfoByUserId(mongoClient, userId);
        if (user != null) {
            return user.getFirstGroupId();
        }
        return null;
    }

    public static String getStringGroupIdByUserId(MongoDBClient mongoClient, String userId) {
        User user = UserManager.findPublicUserInfoByUserId(mongoClient, userId);
        if (user != null) {
            ObjectId id = user.getFirstGroupId();
            if (id == null){
                return null;
            }
            else{
                return id.toString();
            }
        }
        return null;
    }

}
