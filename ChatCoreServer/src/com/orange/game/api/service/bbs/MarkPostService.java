package com.orange.game.api.service.bbs;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.bbs.BBSManager;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-10-22
 * Time: 上午10:20
 * To change this template use File | Settings | File Templates.
 */
public class MarkPostService extends CommonGameService {

    String boardId;
    String postId;
    private int mode;


    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        if (!setAndCheckUserIdAndAppId(request)) {
            return false;
        }
        mode = getIntValueFromRequest(request, ServiceConstant.PARA_MODE, BBSManager.MODE_BBS);

        boardId = request.getParameter(ServiceConstant.PARA_BOARDID);
        postId = request.getParameter(ServiceConstant.PARA_POSTID);

        if (!check(boardId, ErrorCode.ERROR_PARAMETER_BOARDID_EMPTY,
                ErrorCode.ERROR_PARAMETER_BOARDID_NULL)) {
            return false;
        }

        if (!check(postId, ErrorCode.ERROR_PARAMETER_POSTID_EMPTY,
                ErrorCode.ERROR_PARAMETER_POSTID_NULL)) {
            return false;
        }


        return true;
    }

    @Override
    public void handleData() {
        BBSManager.markPost(mongoClient, userId, boardId, postId, mode);
        resultCode = ErrorCode.ERROR_SUCCESS;
    }
}
