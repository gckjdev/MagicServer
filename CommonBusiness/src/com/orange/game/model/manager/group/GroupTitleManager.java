package com.orange.game.model.manager.group;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ErrorCode;
import com.orange.game.model.dao.User;
import com.orange.game.model.dao.group.GroupPermission;
import com.orange.game.model.dao.group.GroupTitle;
import com.orange.game.model.dao.group.GroupUsersByTitle;
import com.orange.game.model.manager.CommonManager;
import com.orange.game.model.manager.UserManager;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-12-4
 * Time: 下午4:22
 * To change this template use File | Settings | File Templates.
 */
public class GroupTitleManager extends CommonManager {

    public static List<GroupUsersByTitle> getUserListByTitle(MongoDBClient mongoDBClient, String groupId) {
        GroupTitle query = new GroupTitle();
        query.setGroupId(groupId);
        DBCursor cursor = mongoDBClient.findAll(DBConstants.T_GROUP_TITLE, query.getDbObject(), null);
        if (cursor != null) {
            List<GroupUsersByTitle> titles = new ArrayList<GroupUsersByTitle>();
            while (cursor.hasNext()) {
                GroupTitle title = new GroupTitle(cursor.next());
                Set<User> users = UserManager.getUserSet(mongoDBClient, title.getUserIdList());
                GroupUsersByTitle usersByTitle = new GroupUsersByTitle(title, users);
                titles.add(usersByTitle);
            }
            cursor.close();
            return titles;
        }
        return Collections.emptyList();
    }

    public static GroupTitle getTitle(MongoDBClient mongoDBClient, String groupId, int titleId, boolean returnUids) {
        DBObject query = getTitleQuery(groupId, titleId);

        DBObject returnFields = new BasicDBObject();
        if (!returnUids) {
            returnFields.put(DBConstants.F_USERID_LIST, 0);
        }
        DBObject obj = mongoDBClient.findOne(DBConstants.T_GROUP_TITLE, query, returnFields);
        if (obj != null) {
            return new GroupTitle(obj);
        }
        return null;
    }

    public static int updateUserGroupTitle(MongoDBClient mongoDBClient, String userId, String groupId, String targetUid, int srcTitleId, int titleId) {

        if (!GroupRoleManager.checkHasPermission(mongoDBClient, userId, groupId, GroupPermission.CUSTOM_TITLE)) {
            return ErrorCode.ERROR_GROUP_PERMISSION;
        }

        DBObject query = getTitleQuery(groupId, srcTitleId);
        mongoDBClient.pullValueFromSet(DBConstants.T_GROUP_TITLE, query, DBConstants.F_USERID_LIST, new ObjectId(targetUid));

        query = getTitleQuery(groupId, titleId);
        mongoDBClient.addToSet(DBConstants.T_GROUP_TITLE, query, DBConstants.F_USERID_LIST, new ObjectId(targetUid));
        return 0;
    }

    private static DBObject getTitleQuery(String groupId, int titleId) {
        GroupTitle query = new GroupTitle();
        query.setGroupId(groupId);
        query.setTitleId(titleId);
        return query.getDbObject();
    }

    public static void createTitle(MongoDBClient mongoClient, GroupTitle title) {
        mongoClient.insert(DBConstants.T_GROUP_TITLE, title.getDbObject());
    }



    public static int createTitle(MongoDBClient mongoClient, String userId, String groupId, int titleId, String title) {
        if (!GroupRoleManager.checkHasPermission(mongoClient, userId, groupId, GroupPermission.CUSTOM_TITLE)) {
            return ErrorCode.ERROR_GROUP_PERMISSION;
        }
        if(titleId <= GroupTitle.NONE){
            titleId = getNextTitleId(mongoClient, groupId);
        }

        GroupTitle groupTitle = new GroupTitle();
        groupTitle.setGroupId(groupId);
        groupTitle.setTitle(title);
        groupTitle.setTitleId(titleId);
        createTitle(mongoClient, groupTitle);
        return 0;
    }

    private static int getNextTitleId(MongoDBClient mongoClient, String groupId) {
        DBObject returnFields = new BasicDBObject(DBConstants.F_TITLE_ID, 1);
        DBObject query = new BasicDBObject(DBConstants.F_GROUPID, new ObjectId(groupId));
        DBCursor cursor = mongoClient.findAll(DBConstants.T_GROUP_TITLE, query, returnFields);
        int titleId = GroupTitle.CUSTOM_START;

        if (cursor != null){
            while(cursor.hasNext()){
                GroupTitle title = new GroupTitle(cursor.next());
                titleId = Math.max(titleId, title.getTitleId());
            }
            cursor.close();
        }
        return titleId;

    }

    public static int deleteTitle(MongoDBClient mongoClient, String userId, String groupId, int titleId) {
        if (!GroupRoleManager.checkHasPermission(mongoClient, userId, groupId, GroupPermission.CUSTOM_TITLE)) {
            return ErrorCode.ERROR_GROUP_PERMISSION;
        }

        GroupTitle title = getTitle(mongoClient, groupId, titleId, true);
        List<ObjectId> list = title.getUserIdList();

        //add the user into member title list
        DBObject query = getTitleQuery(groupId, GroupTitle.MEMBER);
        if (!list.isEmpty()) {
            mongoClient.addEachToSet(DBConstants.T_GROUP_TITLE, query, DBConstants.F_USERID_LIST, list);
        }

        //delete the title;
        query = getTitleQuery(groupId, titleId);
        mongoClient.removeOne(DBConstants.T_GROUP_TITLE, query);
        return 0;
    }

    public static void addUserToTitle(MongoDBClient mongoDBClient, String userId, String groupId, int titleId, String targetId) {
        DBObject query = getTitleQuery(groupId, titleId);
        mongoDBClient.addToSet(DBConstants.T_GROUP_TITLE, query, DBConstants.F_USERID_LIST, new ObjectId(targetId));
    }

    public static int updateGroupTitle(MongoDBClient mongoClient, String userId, String groupId, int titleId, String title) {
        if (!GroupRoleManager.checkHasPermission(mongoClient, userId, groupId, GroupPermission.CUSTOM_TITLE)) {
            return ErrorCode.ERROR_GROUP_PERMISSION;
        }
        GroupTitle groupTitle = new GroupTitle();
        groupTitle.setGroupId(groupId);
        groupTitle.setTitleId(titleId);

        GroupTitle update = new GroupTitle();
        update.setTitle(title);

        mongoClient.updateOne(DBConstants.T_GROUP_TITLE, groupTitle.getDbObject(), new BasicDBObject("$set", update.getDbObject()));
        return 0;
    }

    public static void removeUserFromTitle(MongoDBClient mongoDBClient, String userId, String groupId) {

        ObjectId oid = new ObjectId(userId);
        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_GROUPID, new ObjectId(groupId));
        query.put(DBConstants.F_USERID_LIST, oid);

        mongoDBClient.pullValueFromSet(DBConstants.T_GROUP_TITLE, query, DBConstants.F_USERID_LIST, oid);
    }
}
