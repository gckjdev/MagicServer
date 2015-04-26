package com.orange.game.model.manager.bbs;

import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonMongoIdListManager;
import com.orange.game.model.dao.bbs.BBSPost;

import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-10-26
 * Time: 下午6:00
 * To change this template use File | Settings | File Templates.
 */
public class BBSUserPostManager extends CommonMongoIdListManager<BBSPost> {

//    final private static String TABLE_OPUS_ACTION_PREFIX = "bbs_user_post";
//    final private static String TABLE_OPUS_ACTION_DATA = DBConstants.T_BBS_POST;

    final private static BBSUserPostManager instanceForBBS = new BBSUserPostManager("bbs_user_post", DBConstants.T_BBS_POST);
    final private static BBSUserPostManager instanceForGroup = new BBSUserPostManager("group_user_topic", DBConstants.T_GROUP_TOPIC);


    private BBSUserPostManager(String prefix, String table) {
        super(prefix, table, BBSPost.class);
        autoFixTotalSize = true;
    }

    public static BBSUserPostManager getInstance(int mode) {
        if (mode == BBSManager.MODE_GROUP)
            return instanceForGroup;
        return instanceForBBS;
    }

    @Override
    protected String deleteStatusFieldName() {
        return DBConstants.F_STATUS;
    }

    @Override
    protected int deleteStatusValue() {
        return BBSPost.StatusDelete;
    }

    @Override
    protected String indexBeforeDate() {
        return DBConstants.C_BBS_INDEX_BEFORE_DATE;
    }

    @Override
    protected List<BBSPost> invokeOldGetList(String userId, int offset, int limit) {
        return BBSManager.getBBSPostListByTargetUid(mongoDBClient, userId, null, userId, offset, limit, BBSManager.MODE_BBS);
    }

    ;

    @Override
    protected List<BBSPost> invokeOldGetListForConstruct(String userId) {
        List<BBSPost> list = BBSManager.getBBSPostListByTargetUid(mongoDBClient, null, null, userId, 0, Integer.MAX_VALUE, BBSManager.MODE_BBS);
        if (list != null && !list.isEmpty()) {
            Collections.reverse(list);
        }
        return list;
    }

    public void insertIndex(String userId, String postId) {
        log.info("insert post action, postId=" + postId + ", userId=" + userId);
        this.insertAndConstructIndex(userId, postId, false);
    }

    public List<BBSPost> getList(String userId, int offset, int limit) {
        return getListAndConstructIndex(userId, offset, limit);
    }

}
