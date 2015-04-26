package com.orange.game.model.manager.opus.contest;

import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonZSetIndexManager;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.opus.OpusUtils;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-8-21
 * Time: 下午1:27
 * To change this template use File | Settings | File Templates.
 */

@Deprecated
public class ContestSpecialTopManager extends CommonZSetIndexManager<UserAction> {

    private static final String REDIS_PREFIX = "contest_top_opus_";
    private static final String MONGO_FIELD_PREFIX = "";
    private static final String MONGO_TABLE_NAME = DBConstants.T_OPUS;

    public ContestSpecialTopManager(String contestId, int topType) {
        super(REDIS_PREFIX+contestId.toLowerCase()+String.valueOf(topType), MONGO_TABLE_NAME, UserAction.class);
    }

    public void updateOpusTopScore(final String id, final double score){

        this.updateTopScore(id, score, new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                // TODO update score in mongo db
                return null;
            }
        }, false, true);

    }


    public List<UserAction> getTopList(int offset,int limit){
        return getTopList(offset, limit, DBConstants.F_OPUS_STATUS, UserAction.STATUS_DELETE, OpusUtils.NORMAL_RETURN_FIELDS);
    }

}