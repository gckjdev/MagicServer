package com.orange.game.model.manager.bbs;

import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonZSetIndexManager;
import com.orange.game.model.dao.bbs.BBSPost;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-10-26
 * Time: 下午3:56
 * To change this template use File | Settings | File Templates.
 */
public class BBSTopPostManager extends CommonZSetIndexManager<BBSPost> {
    private static final String REDIS_PREFIX = "top_post_";
    private static final String MONGO_TABLE_NAME = DBConstants.T_BBS_POST;
    private static final int POST_TOP_COUNT = 20000;
    static final private ConcurrentHashMap<String, BBSTopPostManager> managerMap = new ConcurrentHashMap<String, BBSTopPostManager>();
    private String boardId;

    public BBSTopPostManager(String boardId) {
        super(REDIS_PREFIX + boardId, MONGO_TABLE_NAME, POST_TOP_COUNT, BBSPost.class);
        this.boardId = boardId;
    }

    public static BBSTopPostManager managerForBoard(String boardId) {
        if (boardId == null) {
            return null;
        }
        if (managerMap.containsKey(boardId)) {
            return managerMap.get(boardId);
        } else {
            BBSTopPostManager manager = new BBSTopPostManager(boardId);
            managerMap.put(boardId, manager);
            return manager;
        }
    }

    public void updatePostModefyDate(final String postId, final Date modifyDate) {

        this.updateTopScore(postId, modifyDate.getTime(), new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return null;
            }
        }, false, true);

    }

    public List<BBSPost> getTopList(int offset, int limit) {
        return getTopList(offset, limit, DBConstants.F_STATUS, BBSPost.StatusDelete, null);
    }

    public void removePostId(String postId) {
        removeMember(postId);
    }


}
