package com.orange.game.model.manager.group.index;

import com.orange.game.model.common.CommonMongoIdListManager;
import com.orange.game.model.dao.User;
import org.bson.types.ObjectId;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-22
 * Time: 下午5:45
 * To change this template use File | Settings | File Templates.
 */
public class GroupUserIndexManager extends CommonMongoIdListManager<User> {


    static GroupUserIndexManager memberInstance = new GroupUserIndexManager("group_member", "user");

    private GroupUserIndexManager(String idListTableName, String idTableName) {
        super(idListTableName, idTableName, User.class);
    }

    public static GroupUserIndexManager getMemberInstance(){
        return memberInstance;
    }

    public void insertIndex(String groupId, String userId)
    {
        insertId(groupId, userId, true, false);
    }

    public List<User> getList(String groupId, int offset, int limit)
    {
        return getList(groupId, offset, limit, null, null, 1);
    }


    public List<ObjectId>getAllMemberIds(String groupId)
    {
        return super.getAllIdList(groupId);
    }

    @Override
    protected String indexBeforeDate() {
        return null;
    }

    @Override
    protected List<User> invokeOldGetList(String userId, int offset, int limit) {
        return null;
    }

    @Override
    protected List<User> invokeOldGetListForConstruct(String key) {
        return null;
    }
}
