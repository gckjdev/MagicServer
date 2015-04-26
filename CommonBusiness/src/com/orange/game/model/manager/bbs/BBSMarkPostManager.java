package com.orange.game.model.manager.bbs;

import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonMongoIdListManager;
import com.orange.game.model.common.CommonZSetIndexManager;
import com.orange.game.model.dao.bbs.BBSAction;
import com.orange.game.model.dao.bbs.BBSPost;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-10-28
 * Time: 上午10:05
 * To change this template use File | Settings | File Templates.
 */
public class BBSMarkPostManager  extends CommonMongoIdListManager<BBSPost> {

    private static final BBSMarkPostManager instance = new BBSMarkPostManager("bbs_mark_post", DBConstants.T_BBS_POST);

    public static final BBSMarkPostManager getInstance(){
        return instance;
    }

    public BBSMarkPostManager(String idListTableName, String idTableName) {
        super(idListTableName, idTableName, BBSPost.class);
    }

    public List<BBSPost> getList(String boardId, int offset, int limit) {
        return getListAndConstructIndex(boardId, offset, limit);
    }

    public void removePostId(String boardId, String postId) {
        removeId(boardId, postId, true);
    }

    public void insertIndex(String boardId, String postId){
        insertId(boardId, postId, false, true);
    }

    @Override
    protected String indexBeforeDate() {
        return null;
    }

    @Override
    protected List<BBSPost> invokeOldGetList(String boardId, int offset, int limit) {
        return BBSManager.getBBSMarkedPostList(mongoDBClient, boardId, offset, limit);
    }

    @Override
    protected List<BBSPost> invokeOldGetListForConstruct(String key) {
        return BBSManager.getBBSMarkedPostList(mongoDBClient, key, 0, 0);
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

