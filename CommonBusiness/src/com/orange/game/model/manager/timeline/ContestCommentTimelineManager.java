package com.orange.game.model.manager.timeline;

import com.mongodb.BasicDBObject;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonMongoIdListManager;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.OpusManager;
import com.orange.game.model.manager.opus.OpusUtils;
import com.orange.game.model.service.DBService;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-8-5
 * Time: 下午2:43
 * To change this template use File | Settings | File Templates.
 */
public class ContestCommentTimelineManager extends CommonMongoIdListManager<UserAction> {

    public static final String MONGO_TABLE_NAME = "timeline_all_contest_comment";
    public static final String OPUS_ACTION_TABLE_NAME = DBConstants.T_OPUS_ACTION;

    private static ContestCommentTimelineManager defaultManager = new ContestCommentTimelineManager();

    public static ContestCommentTimelineManager getInstance(){
        return defaultManager;
    }

    private ContestCommentTimelineManager() {
        super(MONGO_TABLE_NAME, OPUS_ACTION_TABLE_NAME, UserAction.class);
    }


    public void insertIndex(String contestId, String actionId){
        insertId(contestId, actionId, ALLOW_DUPLICATE, false, false, true);
    }


    public List<UserAction> getList(String contestId,int offset,int limit) {
        List<UserAction> userActions = getList(contestId, offset, limit, returnMongoDBFields(), deleteStatusFieldName(), deleteStatusValue());
        userActions = OpusUtils.handlerUserAction(userActions, true);
        return userActions;
    }

    @Override
    protected String indexBeforeDate() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected List<UserAction> invokeOldGetList(String userId,int offset,int limit){
        return null;
    };

    @Override
    protected List<UserAction> invokeOldGetListForConstruct(String key){
        return null;
    };

    @Override
    protected BasicDBObject returnMongoDBFields(){
        return OpusUtils.createReturnFields();
    }

    @Override
    protected String deleteStatusFieldName(){
        return  DBConstants.F_OPUS_STATUS;
    }

    @Override
    protected int deleteStatusValue(){
        return UserAction.STATUS_DELETE;
    }
}
