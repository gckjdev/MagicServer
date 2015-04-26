package com.orange.game.model.manager.bbs;

import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonMongoIdListManager;
import com.orange.game.model.dao.bbs.BBSAction;

import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-20
 * Time: 下午2:26
 * To change this template use File | Settings | File Templates.
 */
public class BBSUserActionManager extends CommonMongoIdListManager<BBSAction> {


    private static final BBSUserActionManager instanceForBBS = new BBSUserActionManager("bbs_user_action", DBConstants.T_BBS_ACTION, BBSManager.MODE_BBS);
    private static final BBSUserActionManager instanceForGroup = new BBSUserActionManager("group_user_action", DBConstants.T_GROUP_ACTION, BBSManager.MODE_GROUP);
    private int mode;

    private BBSUserActionManager(String idListTableName, String idTableName, int mode) {
        super(idListTableName, idTableName, BBSAction.class);
        this.mode = mode;
    }

    public static BBSUserActionManager getInstance(int mode) {
        if (mode == BBSManager.MODE_GROUP) {
            return instanceForGroup;
        }
        return instanceForBBS;
    }

    public void insertIndex(String userId, String actionId) {
        log.info("insert user bbs action, userId=" + userId + ", actionId=" + actionId);
        if (mode == BBSManager.MODE_BBS) {
            insertAndConstructIndex(userId, actionId, true);
        } else {
            insertId(userId, actionId, true, true);
        }

    }

    public List<BBSAction> getList(String userId, int offset, int limit) {
        if (mode == BBSManager.MODE_BBS) {
            log.info("<getList> mode = BBS, start to call <getListAndConstructIndex> userId = " + userId);
            return getListAndConstructIndex(userId, offset, limit);
        } else {
            return getList(userId, offset, limit, null, DBConstants.F_STATUS, BBSAction.StatusDelete);
        }
    }

    @Override
    protected String indexBeforeDate() {
        return null;
    }

    @Override
    protected List<BBSAction> invokeOldGetList(String userId, int offset, int limit) {
        if (this.mode == BBSManager.MODE_BBS) {
            return BBSManager.getBBSActionList(mongoDBClient, null, userId, 0, offset, limit);
        }
        return null;
    }

    @Override
    protected List<BBSAction> invokeOldGetListForConstruct(String key) {
        if (this.mode == BBSManager.MODE_BBS) {
            List<BBSAction> list = BBSManager.getBBSActionList(mongoDBClient, null, key, 0, 0, 0);
            if (list != null){
                Collections.reverse(list);
            }
            return list;
        }
        return null;
    }

    @Override
    protected String deleteStatusFieldName(){
        return DBConstants.F_STATUS;
    }

    @Override
    protected int deleteStatusValue(){
        return BBSAction.StatusDelete;
    }
}
