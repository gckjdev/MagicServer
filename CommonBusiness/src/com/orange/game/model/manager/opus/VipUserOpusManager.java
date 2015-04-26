package com.orange.game.model.manager.opus;

import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonZSetIndexManager;
import com.orange.game.model.dao.User;
import com.orange.game.model.dao.UserAction;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 14-1-23
 * Time: 下午8:04
 * To change this template use File | Settings | File Templates.
 */
public class VipUserOpusManager extends CommonZSetIndexManager<UserAction> {

    private static final String REDIS_PREFIX = "vip_latest_opus_";
    private static final String MONGO_FIELD_PREFIX = "";
    private static final String MONGO_TABLE_NAME = DBConstants.T_OPUS;
    private static final int HOT_TOP_COUNT = 100000;

    public VipUserOpusManager(String category) {
        super(REDIS_PREFIX+category.toLowerCase(), MONGO_TABLE_NAME, HOT_TOP_COUNT, UserAction.class);
    }

    public void insertVipOpus(final String opusId){
        this.updateTopScore(opusId, System.currentTimeMillis(), null, false, true);
    }


    public List<UserAction> getTopList(int offset,int limit){
        return getTopList(offset, limit, DBConstants.F_OPUS_STATUS, UserAction.STATUS_DELETE, OpusUtils.NORMAL_RETURN_FIELDS);
    }

}
