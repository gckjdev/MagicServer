package com.orange.game.api.service.bbs;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.bbs.BBSManager;
import com.orange.game.api.service.CommonServiceUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-29
 * Time: 上午10:24
 * To change this template use File | Settings | File Templates.
 */

//只看楼主

public class GetPostActionByUserService extends CommonGameService {
    private String targetUid;
    private String postId;
    private int mode;
    private int offset;
    private int limit;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        if (!setAndCheckUserIdAndAppId(request)){
            return false;
        }

        targetUid = request.getParameter(ServiceConstant.PARA_TARGETUSERID);

        // get board post
        postId = request.getParameter(ServiceConstant.PARA_POSTID);

        if (!check(postId, ErrorCode.ERROR_PARAMETER_POSTID_EMPTY,
                ErrorCode.ERROR_PARAMETER_POSTID_NULL)) {
            return false;
        }

        if (!check(targetUid, ErrorCode.ERROR_PARAMETER_TARGET_USERID_EMPTY,
                ErrorCode.ERROR_PARAMETER_TARGET_USERID_NULL)) {
            return false;
        }
        mode = getIntValueFromRequest(request, ServiceConstant.PARA_MODE, BBSManager.MODE_BBS);
        offset = getIntValueFromRequest(request, ServiceConstant.PARA_OFFSET,
                0);
        limit = getIntValueFromRequest(request, ServiceConstant.PARA_LIMIT,
                ServiceConstant.DEFAULT_MAX_COUNT);
        return true;
    }

    @Override
    public void handleData() {
        List actionList = BBSManager.getPostCommentListByUser(mongoClient, postId, targetUid, offset, limit, mode);
        byteData = CommonServiceUtils.bbsActionListToProto(actionList);

    }
}
