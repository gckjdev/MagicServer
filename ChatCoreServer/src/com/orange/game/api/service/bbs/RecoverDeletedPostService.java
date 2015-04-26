package com.orange.game.api.service.bbs;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.bbs.BBSManager;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 14-8-21
 * Time: 下午1:26
 * To change this template use File | Settings | File Templates.
 */
public class RecoverDeletedPostService extends CommonGameService {

    String postId;
    String boardId;
    int mode;

    /*

    usage:

    http://place100.com:8699/api/i?&m=recoverDeletedPost&pi=53f1aa77e4b0400953ef0e04 <postId>

     */


    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {

        // post info
        postId = request.getParameter(ServiceConstant.PARA_POSTID);
        boardId = request.getParameter(ServiceConstant.PARA_BOARDID);

        if (!check(postId, ErrorCode.ERROR_PARAMETER_POSTID_EMPTY,
                ErrorCode.ERROR_PARAMETER_POSTID_NULL)) {
            return false;
        }

        mode = getIntValueFromRequest(request, ServiceConstant.PARA_MODE, BBSManager.MODE_BBS);
        return true;
    }

    @Override
    public void handleData() {
        BBSManager.recoverDeletePost(mongoClient, userId, postId, boardId, mode);
    }

    @Override
    public String toString() {
        return "RecoverDeletedPostService{" +
                "postId='" + postId + '\'' +
                ", boardId='" + boardId + '\'' +
                ", mode=" + mode +
                '}';
    }
}
