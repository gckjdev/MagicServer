package com.orange.game.api.service.bbs;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.bbs.BBSPost;
import com.orange.game.model.manager.bbs.BBSManager;
import com.orange.game.model.manager.group.GroupManager;
import com.orange.game.model.manager.group.GroupUserManager;
import org.bson.types.ObjectId;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-10-23
 * Time: 下午3:31
 * To change this template use File | Settings | File Templates.
 */
public class FindBBSPostByKeyWords extends CommonGameService {

    String boardId;
    String keyWord;
    int deviceType;
    int limit;
    int offset;
    private int mode;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        if (!setAndCheckUserIdAndAppId(request)) {
            return false;
        }
        mode = getIntValueFromRequest(request, ServiceConstant.PARA_MODE, BBSManager.MODE_BBS);

        deviceType = getIntValueFromRequest(request,
                ServiceConstant.PARA_DEVICETYPE, 0);

        // get user post
        boardId = request.getParameter(ServiceConstant.PARA_BOARDID);

        keyWord = request.getParameter(ServiceConstant.PARA_KEYWORD);

        limit = getIntValueFromRequest(request, ServiceConstant.PARA_LIMIT,
                ServiceConstant.CONST_DEFAULT_PAGE_COUNT);

        offset = getIntValueFromRequest(request, ServiceConstant.PARA_OFFSET, 0);
        if (!check(keyWord, ErrorCode.ERROR_PARAMETER_KEYWORD_EMPTY,
                ErrorCode.ERROR_PARAMETER_KEYWORD_NULL)) {
            return false;
        }

        return true;
    }

    @Override
    public void handleData() {
        String userGroupId = null;
        if (mode == BBSManager.MODE_GROUP){
            userGroupId = GroupManager.getStringGroupIdByUserId(mongoClient, userId);
        }

        List<BBSPost> postList = BBSManager.searchPostFromES(mongoClient, keyWord, boardId, offset, limit, mode);
        byteData = CommonServiceUtils.bbsPostListToProto(postList, userId, userGroupId);
    }
}
