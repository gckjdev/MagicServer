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
 * Time: 下午12:03
 * To change this template use File | Settings | File Templates.
 */

public class BBSBoardPostManager extends CommonZSetIndexManager<BBSPost> {


    private static final String REDIS_PREFIX = "board_post_";
    private static final String MONGO_TABLE_NAME = DBConstants.T_BBS_POST;
    private static final int HOT_TOP_COUNT = 20000;
    private String boardId;


    static final private ConcurrentHashMap<String, BBSBoardPostManager> managerMap = new ConcurrentHashMap<String, BBSBoardPostManager>();


    public static BBSBoardPostManager managerForBoard(String boardId){
        if (boardId == null){
            return null;
        }
        if (managerMap.containsKey(boardId)){
            return managerMap.get(boardId);
        }else{
            BBSBoardPostManager manager = new BBSBoardPostManager(boardId);
            managerMap.put(boardId, manager);
            return manager;
        }
    }

    public BBSBoardPostManager(String boardId) {
        super(REDIS_PREFIX+boardId, MONGO_TABLE_NAME, HOT_TOP_COUNT, BBSPost.class);
        this.boardId = boardId;
    }

    public void updatePostModefyDate(final String postId, final Date modifyDate){
        updateTopScore(postId, modifyDate.getTime(), null, false, true);
    }

    public void addPostId(String postId){
        updatePostModefyDate(postId, new Date());
    }

    public List<BBSPost> getTopList(int offset,int limit){
        return getTopList(offset, limit, DBConstants.F_STATUS, BBSPost.StatusDelete, null);
    }

    public void removePostId(String postId) {
        removeMember(postId);
    }


}