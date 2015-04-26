package com.orange.game.model.manager.timeline;

import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonMongoIdListManager;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.opus.OpusUtils;

import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 14-7-21
 * Time: 下午9:27
 * To change this template use File | Settings | File Templates.
 */
public class TutorialTimelineManager extends CommonMongoIdListManager<UserAction> {
    private static TutorialTimelineManager ourInstance = new TutorialTimelineManager();

    public static TutorialTimelineManager getInstance() {
        return ourInstance;
    }

    public static final String USER_OPUS_PREFIX = "timeline_tutorial";
    public static final String OPUS_ACTION_TABLE_NAME = DBConstants.T_OPUS;

    private TutorialTimelineManager() {
        super(USER_OPUS_PREFIX, OPUS_ACTION_TABLE_NAME, UserAction.class);
    }


    public void insertTimeline(String userId, String actionId){
        this.insertId(userId, actionId, true, false);
    }

    public void insertOwnerUserTimeline(String userId, String actionId){
        this.insertId(userId, actionId, false, false);
    }

    public List<UserAction> getList(String userId,int offset,int limit) {
        List<UserAction> userActions = getList( userId, offset, limit,
                OpusUtils.NORMAL_RETURN_FIELDS,
                DBConstants.F_OPUS_STATUS,
                UserAction.STATUS_DELETE);

//        userActions = OpusUtils.handlerUserAction(userActions, true);
        return userActions;
    }

    @Override
    protected List<UserAction> invokeOldGetList(String userId,int offset,int limit){
        return Collections.emptyList();

    };

    @Override
    protected List<UserAction> invokeOldGetListForConstruct(String key){
        return Collections.emptyList();
    };

    @Override
    protected String indexBeforeDate() {
        return null;
    }
}
