package com.orange.game.api.service.bbs;

import com.orange.common.utils.StringUtil;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.bbs.BBSPost;
import com.orange.game.model.manager.bbs.BBSBoardPostManager;
import com.orange.game.model.manager.bbs.BBSManager;
import com.orange.game.model.manager.bbs.BBSTopPostManager;
import com.orange.game.model.manager.bbs.BBSUserPostManager;
import com.orange.game.model.manager.group.GroupManager;
import com.orange.game.model.manager.group.index.GroupTopicIndexManager;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

public class GetBBSPostListService extends CommonGameService {

    String boardId;
    String targetUid;
    int deviceType;
    int rangeType;
    int offset;
    int limit;
    private int mode;

    @Override
    public void handleData() {
        List<BBSPost> postList = null;
        if (!StringUtil.isEmpty(boardId)) {
            postList = getBoardPosts();
        } else if (!StringUtil.isEmpty(targetUid)) {
            postList = BBSUserPostManager.getInstance(mode).getList(targetUid, offset, limit);
        }

        String userGroupId = null;
        if (mode == BBSManager.MODE_GROUP){
            userGroupId = GroupManager.getStringGroupIdByUserId(mongoClient, userId);
        }

        byteData = CommonServiceUtils.bbsPostListToProto(postList, userId, userGroupId);
    }

    private List<BBSPost> combinePosts(List<BBSPost> topPosts, List<BBSPost> normalPost) {
        List<BBSPost> posts = new ArrayList<BBSPost>();
        if (topPosts != null && !topPosts.isEmpty()) {
            posts.addAll(topPosts);
        }
        if (normalPost != null && !normalPost.isEmpty()) {
            posts.addAll(normalPost);
        }
        return posts;
    }

    private List<BBSPost> getBoardPosts() {
        List<BBSPost> topPosts = null;
        List<BBSPost> nomalPosts;
        if (mode == BBSManager.MODE_GROUP) {
            nomalPosts = GroupTopicIndexManager.getInstanceForLatest().getList(boardId, offset, limit);
            log.info("<getBoardPosts> get group timeline posts. group = " + boardId + ", size = " + nomalPosts.size());
        } else {
            nomalPosts = BBSBoardPostManager.managerForBoard(boardId).getTopList(offset, limit);
            log.info("<getBoardPosts> get bbs timeline posts, board = " + boardId + ", size = " + nomalPosts.size());
        }
        if (offset == 0) {
            if (mode == BBSManager.MODE_GROUP) {
                topPosts = GroupTopicIndexManager.getInstanceForTop().getList(boardId, offset, limit);
                log.info("<getBoardPosts> get group top posts. group = " + boardId + ", size = " + topPosts.size());
            } else {
                topPosts = BBSTopPostManager.managerForBoard(boardId).getTopList(offset, limit);
                log.info("<getBoardPosts> get bbs top posts, board = " + boardId + ", size = " + topPosts.size());
            }
        }
        return combinePosts(topPosts, nomalPosts);
    }

    @Override
    public String toString() {
        return "GetBBSPostListService [appId=" + appId + ", boardId=" + boardId
                + ", deviceType=" + deviceType + ", limit=" + limit
                + ", offset=" + offset + ", rangeType=" + rangeType
                + ", targetUid=" + targetUid + ", userId=" + userId + "]";
    }

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {


        if (!setAndCheckUserIdAndAppId(request)) {
            return false;
        }
        mode = getIntValueFromRequest(request, ServiceConstant.PARA_MODE, BBSManager.MODE_BBS);


        deviceType = getIntValueFromRequest(request,
                ServiceConstant.PARA_DEVICETYPE, 0);


        // get target uid
        targetUid = request.getParameter(ServiceConstant.PARA_TARGETUSERID);

        // get board post
        boardId = request.getParameter(ServiceConstant.PARA_BOARDID);

        rangeType = getIntValueFromRequest(request,
                ServiceConstant.PARA_RANGE_TYPE, BBSManager.RangeTypeNew);

        offset = getIntValueFromRequest(request, ServiceConstant.PARA_OFFSET,
                0);
        limit = getIntValueFromRequest(request, ServiceConstant.PARA_LIMIT,
                ServiceConstant.DEFAULT_MAX_COUNT);


        if (StringUtil.isEmpty(targetUid) && StringUtil.isEmpty(boardId)) {
            resultCode = ErrorCode.ERROR_GET_BBSPOST_LIST;
            return false;
        }
        return true;
    }

}
