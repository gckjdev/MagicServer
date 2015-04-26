package com.orange.game.model.manager.group.index;

import com.mongodb.BasicDBObject;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonMongoIdListManager;
import com.orange.game.model.dao.group.GroupNotice;
import com.orange.game.model.manager.group.GroupNoticeManager;

import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 14-1-22
 * Time: 下午1:50
 * To change this template use File | Settings | File Templates.
 */
public class GroupNoticeIndexManager extends CommonMongoIdListManager<GroupNotice> {
    private static final String TABLE_NAME = "group_charge_notice";
    private static GroupNoticeIndexManager chargeIndexManager = new GroupNoticeIndexManager(TABLE_NAME, GroupNotice.TypeChargeGroup);

    private int type;
    private GroupNoticeIndexManager(String tableName, int type) {
        super(tableName, DBConstants.T_GROUP_NOTICE, GroupNotice.class);
        this.type = type;
    }

    public static GroupNoticeIndexManager getChargeIndexManager(){
        return chargeIndexManager;
    }


    public List<GroupNotice> getList(String key, int offset, int limit) {
        List<GroupNotice> list = getListAndConstructIndex(key, offset, limit);
        return GroupNoticeManager.fillNoticeListResult(mongoDBClient, list);
    }

    @Override
    protected String indexBeforeDate() {
        return null;
    }

    @Override
    protected List<GroupNotice> invokeOldGetList(String groupId, int offset, int limit) {
        return GroupNoticeManager.getNotices(mongoDBClient, groupId, this.type, offset, limit);
    }

    @Override
    protected List<GroupNotice> invokeOldGetListForConstruct(String groupId) {
        List<GroupNotice> list = GroupNoticeManager.getNotices(mongoDBClient, groupId, this.type, 0, Integer.MAX_VALUE);
        if (list != null && !list.isEmpty()){
            Collections.reverse(list);
        }
        return list;
    }
}
