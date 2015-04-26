package com.orange.game.model.manager.group.index;

import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonMongoIdListManager;
import com.orange.game.model.dao.App;
import com.orange.game.model.dao.app.AppFactory;
import com.orange.game.model.dao.bbs.BBSPost;
import com.orange.game.model.manager.bbs.BBSManager;
import org.bson.types.ObjectId;

import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-22
 * Time: 下午5:24
 * To change this template use File | Settings | File Templates.
 */
public class UserTopicIndexManager extends CommonMongoIdListManager<BBSPost> {

    private static final UserTopicIndexManager managerForTimeline = new UserTopicIndexManager("user_topic_timeline", DBConstants.T_GROUP_TOPIC);
    private static final UserTopicIndexManager managerForFollow = new UserTopicIndexManager("user_topic_follow", DBConstants.T_GROUP_TOPIC);
    private static final UserTopicIndexManager managerForMine = new UserTopicIndexManager("user_topic_creation", DBConstants.T_GROUP_TOPIC);

    private UserTopicIndexManager(String idListTableName, String idTableName) {
        super(idListTableName, idTableName, BBSPost.class);
    }

    public static UserTopicIndexManager getTimelineInstance() {
        return managerForTimeline;
    }

    public static UserTopicIndexManager getManagerForFollow() {
        return managerForFollow;
    }

    public static UserTopicIndexManager getManagerForMine() {
        return managerForMine;
    }

    public void insertIndex(String userId, String topicId) {
        insertId(userId, topicId, true, true);
    }


    public void insertIndex(Collection<ObjectId> userIdList, String topicId) {
        if (userIdList != null) {
            for (ObjectId oid : userIdList) {
                insertId(oid.toString(), topicId, true, true);
            }
        }
    }

    public List<BBSPost> getList(String userId, int offset, int limit) {
        if (getManagerForMine() == this) {
            return getListAndConstructIndex(userId, offset, limit);
        }
        return getList(userId, offset, limit, null, DBConstants.F_STATUS, BBSPost.StatusDelete);
    }

    @Override
    protected String indexBeforeDate() {
        return null;
    }

    @Override
    protected List<BBSPost> invokeOldGetList(String userId, int offset, int limit) {
        if (this == getManagerForMine()) {
            return BBSManager.getBBSPostListByTargetUid(mongoDBClient, null, null, userId, offset, limit, BBSManager.MODE_GROUP);
        }
        return null;
    }

    @Override
    protected List<BBSPost> invokeOldGetListForConstruct(String key) {
        if (this == getManagerForMine()) {
            return BBSManager.getBBSPostListByTargetUid(mongoDBClient, null, null, key, 0, 0, BBSManager.MODE_GROUP);
        }
        return null;
    }
}
