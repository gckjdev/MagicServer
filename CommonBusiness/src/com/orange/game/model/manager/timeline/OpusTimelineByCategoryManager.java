
package com.orange.game.model.manager.timeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonMongoIdListManager;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.dao.bbs.BBSAction;
import com.orange.game.model.manager.bbs.BBSManager;
import com.orange.game.model.manager.opus.OpusUtils;
import com.orange.game.model.xiaoji.XiaojiFactory;

public class OpusTimelineByCategoryManager extends CommonMongoIdListManager<UserAction> {

    public static final String USER_OPUS_PREFIX = "timeline_opus";
    public static final String OPUS_TABLE_NAME = DBConstants.T_OPUS;

    public OpusTimelineByCategoryManager(String category) {
        super(USER_OPUS_PREFIX + "_" + category, OPUS_TABLE_NAME, UserAction.class);
    }

    public void insertIndex(String userId, String actionId){
        this.insertId(userId, actionId, true, false);
    }


    public List<UserAction> getList(String userId,int offset,int limit) {
//        List<UserAction> userActions = getList( userId, offset, limit,
//                OpusUtils.NORMAL_RETURN_FIELDS,
//                DBConstants.F_OPUS_STATUS,
//                UserAction.STATUS_DELETE);

        List<UserAction> userActions = getListAndConstructIndex(userId, offset, limit);
        return userActions;
    }

    @Override
    protected List<UserAction> invokeOldGetList(String userId,int offset,int limit){
        return XiaojiFactory.getInstance().getDraw().opusTimelineManager().getList(userId, offset, limit);

    };

    @Override
    protected List<UserAction> invokeOldGetListForConstruct(String userId){
        List<UserAction> list = XiaojiFactory.getInstance().getDraw().opusTimelineManager().getList(userId, 0, 1000);
        if (list != null && !list.isEmpty()) {
            Collections.reverse(list);
        }
        return list;
    };

    @Override
    protected String indexBeforeDate() {
        return "20131209000000";
    }

}
