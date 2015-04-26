package com.orange.game.api.service.bbs;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.bbs.BBSManager;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 14-1-8
 * Time: 下午4:00
 * To change this template use File | Settings | File Templates.
 */
public class EditBBSPostTextService extends CommonGameService {
    private int mode;
    private String postId;
    private String text;
    String boardId;                 // TODO client add boardId as parameters

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        if (!setAndCheckUserIdAndAppId(request)) {
            return false;
        }
        mode = getIntValueFromRequest(request, ServiceConstant.PARA_MODE, BBSManager.MODE_BBS);
        postId = request.getParameter(ServiceConstant.PARA_POSTID);
        text = request.getParameter(ServiceConstant.PARA_TEXT_CONTENT);
        if (!check(postId, ErrorCode.ERROR_PARAMETER_POSTID_EMPTY,
                ErrorCode.ERROR_PARAMETER_POSTID_NULL)) {
            return false;
        }

        boardId = request.getParameter(ServiceConstant.PARA_BOARDID);

        if (BBSManager.isUserBlackInBoard(userId, boardId)){
            resultCode = ErrorCode.ERROR_USER_IS_BLACK_BOARD;
            return false;
        }

        return true;
    }

    @Override
    public void handleData() {
        BBSManager.updatePostText(mongoClient, userId, postId, text, mode);
    }
}
