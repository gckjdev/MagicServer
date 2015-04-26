package com.orange.game.model.manager.group.index;

import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonMongoIdListManager;
import com.orange.game.model.dao.bbs.BBSAction;
import com.orange.game.model.dao.bbs.BBSPost;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-20
 * Time: 上午11:51
 * To change this template use File | Settings | File Templates.
 */
public class GroupTopicIndexManager extends CommonMongoIdListManager<BBSPost> {

    private GroupTopicIndexManager(String idListTableName, String idTableName, Class<BBSPost> returnDataObjectClass) {
        super(idListTableName, idTableName, returnDataObjectClass);
    }

    private static GroupTopicIndexManager instanceForLatest = new GroupTopicIndexManager(DBConstants.T_GROUP_TOPIC_LATEST, DBConstants.T_GROUP_TOPIC, BBSPost.class);
    private static GroupTopicIndexManager instanceForMarked = new GroupTopicIndexManager(DBConstants.T_GROUP_TOPIC_MARK, DBConstants.T_GROUP_TOPIC, BBSPost.class);
    private static GroupTopicIndexManager instanceForTop = new GroupTopicIndexManager(DBConstants.T_GROUP_TOPIC_TOP, DBConstants.T_GROUP_TOPIC, BBSPost.class);

    public static GroupTopicIndexManager getInstanceForLatest(){
        return instanceForLatest;
    }

    public static GroupTopicIndexManager getInstanceForMarked(){
        return instanceForMarked;
    }

    public static GroupTopicIndexManager getInstanceForTop(){
        return instanceForTop;
    }

    @Override
    protected String indexBeforeDate() {
        return null;
    }

    @Override
    protected List<BBSPost> invokeOldGetList(String userId, int offset, int limit) {
        return null;
    }

    @Override
    protected List<BBSPost> invokeOldGetListForConstruct(String key) {
        return null;
    }

    public void insertIndex(String groupId, String topicId) {
        log.info("insert group topic, groupId=" + groupId + ", topicId=" + topicId);
        insertId(groupId, topicId, false, true);
    }

    public List<BBSPost> getList(String groupId, int offset, int limit) {
        return  getList(groupId, offset, limit, null, DBConstants.F_STATUS, BBSPost.StatusDelete);
    }

    @Override
    protected String deleteStatusFieldName(){
        return DBConstants.F_STATUS;
    }

    @Override
    protected int deleteStatusValue(){
        return BBSPost.StatusDelete;
    }

    public static void changeTopicStatus(String boardId, String postId, int oldStatus, int status) {
        //original status
        if (oldStatus == BBSPost.StatusNormal){
            GroupTopicIndexManager.getInstanceForLatest().removeId(boardId, postId, true);
        }else if(oldStatus == BBSPost.StatusTop){
            GroupTopicIndexManager.getInstanceForTop().removeId(boardId, postId, true);
        }else{
            //other status
        }
        //new status
        if (status == BBSPost.StatusNormal){
            GroupTopicIndexManager.getInstanceForLatest().insertIndex(boardId, postId);
        }else if(status == BBSPost.StatusTop){
            GroupTopicIndexManager.getInstanceForTop().insertIndex(boardId, postId);
        }else{
            //Other status
        }
    }
}
