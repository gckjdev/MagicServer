package com.orange.game.api.service.bbs;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.bbs.BBSPost;
import com.orange.game.model.manager.bbs.BBSManager;
import com.orange.game.model.manager.bbs.BBSMarkPostManager;
import com.orange.game.model.manager.group.GroupManager;
import com.orange.game.model.manager.group.index.GroupTopicIndexManager;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-10-22
 * Time: 上午11:08
 * To change this template use File | Settings | File Templates.
 */
public class GetMarkedPostsService extends CommonGameService {

    String boardId;
    int deviceType;
    int offset;
    int limit;
    private int mode;

    @Override
    public void handleData() {
        List<BBSPost> postList;
        if (mode == BBSManager.MODE_GROUP){
            postList = GroupTopicIndexManager.getInstanceForMarked().getList(boardId, offset, limit);
        }else{
            postList = BBSMarkPostManager.getInstance().getList(boardId, offset, limit);
        }

        String userGroupId = null;
        if (mode == BBSManager.MODE_GROUP){
            userGroupId = GroupManager.getStringGroupIdByUserId(mongoClient, userId);
        }

        byteData = CommonServiceUtils.bbsPostListToProto(postList, userId, userGroupId);
    }

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {

        if (!setAndCheckUserIdAndAppId(request)) {
            return false;
        }
        mode = getIntValueFromRequest(request, ServiceConstant.PARA_MODE, BBSManager.MODE_BBS);

        deviceType = getIntValueFromRequest(request,
                ServiceConstant.PARA_DEVICETYPE, 0);


        // get board post
        boardId = request.getParameter(ServiceConstant.PARA_BOARDID);


        offset = getIntValueFromRequest(request, ServiceConstant.PARA_OFFSET,
                0);
        limit = getIntValueFromRequest(request, ServiceConstant.PARA_LIMIT,
                ServiceConstant.DEFAULT_MAX_COUNT);


        if (!check(boardId, ErrorCode.ERROR_PARAMETER_BOARDID_EMPTY,
                ErrorCode.ERROR_PARAMETER_BOARDID_NULL)) {
            return false;
        }

        return true;
    }

}
