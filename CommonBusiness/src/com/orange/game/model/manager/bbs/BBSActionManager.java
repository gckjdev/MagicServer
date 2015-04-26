package com.orange.game.model.manager.bbs;

import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonMongoIdListManager;
import com.orange.game.model.dao.bbs.BBSAction;

import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-10-26
 * Time: 下午4:38
 * To change this template use File | Settings | File Templates.
 */
public class BBSActionManager extends CommonMongoIdListManager<BBSAction> {


    final private static BBSActionManager commentManagerForBBS = new BBSActionManager(BBSAction.ActionTypeComment, "bbs_action_comment", DBConstants.T_BBS_ACTION);
    final private static BBSActionManager supportManagerForBBS = new BBSActionManager(BBSAction.ActionTypeSupport, "bbs_action_support", DBConstants.T_BBS_ACTION);
    final private static BBSActionManager commentManagerForGroup = new BBSActionManager(BBSAction.ActionTypeComment, "group_action_comment", DBConstants.T_GROUP_ACTION);
    final private static BBSActionManager supportManagerForGroup = new BBSActionManager(BBSAction.ActionTypeSupport, "group_action_support", DBConstants.T_GROUP_ACTION);

    final private int actionType;


    private BBSActionManager(int actionType, String idListTableName, String tableName) {
        super(idListTableName, tableName, BBSAction.class);
        this.actionType = actionType;
        this.autoFixTotalSize = true;
    }

    public static BBSActionManager commentManagerInstance(int mode) {
        if (mode == BBSManager.MODE_GROUP)
            return commentManagerForGroup;
        return commentManagerForBBS;
    }

    public static BBSActionManager supportManagerInstance(int mode) {
        if (mode == BBSManager.MODE_GROUP)
            return supportManagerForGroup;
        return supportManagerForBBS;
    }

    @Override
    protected String deleteStatusFieldName() {
        return DBConstants.F_STATUS;
    }

    @Override
    protected int deleteStatusValue() {
        return BBSAction.StatusDelete;
    }

    @Override
    protected String indexBeforeDate() {
        return DBConstants.C_BBS_INDEX_BEFORE_DATE;
    }

    @Override
    protected List<BBSAction> invokeOldGetList(String postId, int offset, int limit) {
        return BBSManager.getBBSActionList(mongoDBClient, postId, null, actionType, offset, limit);

    }

    ;

    @Override
    protected List<BBSAction> invokeOldGetListForConstruct(String postId) {
        List<BBSAction> list = BBSManager.getBBSActionList(mongoDBClient, postId, null, actionType, 0, Integer.MAX_VALUE);
        if (list != null && !list.isEmpty()) {
            Collections.reverse(list);
        }
        return list;
    }

    public void insertIndex(String postId, String actionId) {
        log.info("insert post action, postId=" + postId + ", actionId=" + actionId);
        this.insertAndConstructIndex(postId, actionId, false);
    }

    public List<BBSAction> getList(String key, int offset, int limit) {
        return getListAndConstructIndex(key, offset, limit);
    }

}
