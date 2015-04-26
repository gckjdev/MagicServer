package com.orange.game.model.manager.group;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ErrorCode;
import com.orange.game.model.common.CommonMongoIdListManager;
import com.orange.game.model.dao.group.Group;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-11
 * Time: 下午3:03
 * To change this template use File | Settings | File Templates.
 */
public class FollowGroupManager extends CommonMongoIdListManager<Group> {

    private static FollowGroupManager instance = new FollowGroupManager();


    private FollowGroupManager() {
        super(DBConstants.T_FOLLOW_GROUP, DBConstants.T_GROUP, Group.class);
    }

    public FollowGroupManager(String idListTableName, String idTableName, Class<Group> returnDataObjectClass) {
        super(idListTableName, idTableName, returnDataObjectClass);
    }



    public static List<Group> getFollowGroups(MongoDBClient mongoClient, String userId, String gameId, int offset, int limit) {
        List<Group> groups = instance.getList(userId, offset, limit, (BasicDBObject) Group.getSimpleReturnFields(), DBConstants.F_STATUS, Group.StatusDelete);
        if (!groups.isEmpty()){
            if (offset == 0){
                Group group = GroupManager.getSimpleGroupByUserId(mongoClient, userId);
                if (group != null){
                    instance.log.info("<getFollowGroups> group is not null");
                    Group tp = getGroupInList(groups, group.getGroupId());
                    if (tp != null){
                        instance.log.info("<getFollowGroups> group is in list, remove it, and insert new group.");
                        groups.remove(tp);
                        groups.add(0, tp);
                    }else{
                        instance.log.info("<getFollowGroups> group is not in list, insert new group.");
                        groups.add(0, group);
                    }
                }else{
                    instance.log.info("<getFollowGroups> group is null, no group to insert.");
                }
            }else{
                ObjectId gid = GroupManager.getGroupIdByUserId(mongoClient, userId);
                if (gid != null){
                    Group gp = getGroupInList(groups, gid.toString());
                    if (gp != null){
                        groups.remove(gp);
                    }
                }
            }
        }
        return groups;
    }

    private static Group getGroupInList(List<Group> groups, String groupId) {
        for (Group g : groups){
            if (g.getGroupId().equalsIgnoreCase(groupId)){
                return g;
            }
        }
        return null;
    }

    public static FollowGroupManager getInstance() {
        return instance;
    }

    public static int followGroup(MongoDBClient mongoClient, String userId, String groupId) {
        if (userHasFollowedGroup(mongoClient, userId, groupId)) {
            return ErrorCode.ERROR_GROUP_REPEAT_FOLLOW;
        }
        mongoClient.inc(DBConstants.T_GROUP, new ObjectId(groupId), DBConstants.F_FAN_COUNT, 1);
        instance.insertId(userId, groupId, true, true);
        return 0;
    }


    public static List<ObjectId> getAllFollowGroupIds(MongoDBClient mongoClient, String userId) {

        // TODO use
        // return this.getAllIdList(userId);

        DBObject query = new BasicDBObject(DBConstants.F_OWNER, new ObjectId(userId));
        DBObject returnFields = new BasicDBObject(DBConstants.F_ID_LIST, 1);
        DBObject object = mongoClient.findOne(DBConstants.T_FOLLOW_GROUP, query, returnFields);
        instance.log.info("<getAllFollowGroupIds> query = "+ query + ", result = " + object);
        if (object != null){
            List<ObjectId> list = (List) object.get(DBConstants.F_ID_LIST);
            if (list != null){
                return list;
            }
        }
        return Collections.emptyList();
    }

    private static boolean userHasFollowedGroup(MongoDBClient mongoClient, String userId, String groupId) {
        List<ObjectId> list = getAllFollowGroupIds(mongoClient, userId);
        return list.contains(new ObjectId(groupId));
    }





    public static int unfollowGroup(MongoDBClient mongoClient, String userId, String groupId) {
        if (GroupUserManager.isUserGroupMember(mongoClient, userId, groupId)) {
            return ErrorCode.ERROR_GROUP_MEMBER_UNFOLLOW;
        }
        instance.removeId(userId, groupId, false);
        return 0;
    }

    @Override
    protected String indexBeforeDate() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected List<Group> invokeOldGetList(String userId, int offset, int limit) {
        return null;
    }

    @Override
    protected List<Group> invokeOldGetListForConstruct(String key) {
        return null;
    }
}
