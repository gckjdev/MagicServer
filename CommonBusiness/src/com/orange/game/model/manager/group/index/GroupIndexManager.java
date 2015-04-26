package com.orange.game.model.manager.group.index;

import com.orange.common.utils.StringUtil;

import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonZSetIndexManager;
import com.orange.game.model.dao.group.Group;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-11
 * Time: 下午12:07
 * To change this template use File | Settings | File Templates.
 */
public class GroupIndexManager extends CommonZSetIndexManager<Group> {

    private static final String REDIS_PREFIX = "group_";
    private static final String MONGO_TABLE_NAME = DBConstants.T_GROUP;
    private static final int HOT_TOP_COUNT = 2000;
    private static final String TYPE_NAME_FAME = "fame";
    private static final String TYPE_NAME_BALANCE = "balance";
    private static final String TYPE_NAME_ACTIVE = "active";
    private static final String TYPE_NAME_NEW = "new";
    private static Map<String, GroupIndexManager> map = new HashMap<String, GroupIndexManager>();
    private String typeName;



    private GroupIndexManager(String gameId, String typeName) {
        super(constructKey(gameId, typeName), MONGO_TABLE_NAME, HOT_TOP_COUNT, Group.class);
        this.typeName = typeName;
    }

    public static GroupIndexManager fameManager() {
        return getIndexManager("", TYPE_NAME_FAME);
    }

    public static GroupIndexManager activeManager() {
        return getIndexManager("", TYPE_NAME_ACTIVE);
    }

    public static GroupIndexManager balanceManager() {
        return getIndexManager("", TYPE_NAME_BALANCE);
    }

    public static GroupIndexManager newManager() {
        return getIndexManager("", TYPE_NAME_NEW);
    }

    private static GroupIndexManager getIndexManager(String gameId, String typeName) {
        String key = constructKey(gameId, typeName);
        GroupIndexManager manager = map.get(key);
        if (manager == null) {
            manager = new GroupIndexManager(gameId, typeName);
            map.put(key, manager);
        }
        return manager;
    }

    private static String constructKey(String gameId, String typeName) {
        if (StringUtil.isEmpty(gameId)) {
            return REDIS_PREFIX + typeName;
        }
        return REDIS_PREFIX + typeName + "_" + gameId;
    }

    public static void initNewGroup(Group group) {
        activeManager().updateScore(group.getGroupId(), 0);
        newManager().updateScore(group.getGroupId(), group.getCreateDate().getTime());
        fameManager().updateScore(group.getGroupId(), group.getFame());
        balanceManager().updateScore(group.getGroupId(), group.getBalance());
    }

    public void updateScore(final String groupId, final double score) {
        this.updateTopScore(groupId, score, null, false, true);
    }

    public void increaseScore(final String groupId, final double inc) {
        this.incTopScore(groupId, inc, null, true);
    }

    public List<Group> getTopList(int offset, int limit) {
        return getTopList(offset, limit, DBConstants.F_STATUS, Group.StatusDelete, null);
    }

    public void removeGroupId(String boardId) {
        removeMember(boardId);

    }

    public static void removeAllIndex(String groupId) {
        activeManager().removeGroupId(groupId);
        newManager().removeGroupId(groupId);
        fameManager().removeGroupId(groupId);
        balanceManager().removeGroupId(groupId);
    }
}
