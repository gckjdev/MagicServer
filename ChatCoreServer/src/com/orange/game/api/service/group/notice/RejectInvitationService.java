package com.orange.game.api.service.group.notice;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.group.GroupNoticeManager;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-12-11
 * Time: 上午10:39
 * To change this template use File | Settings | File Templates.
 */
public class RejectInvitationService extends CommonGameService {


    private String noticeId;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        if (!setAndCheckUserIdAndAppId(request)){
            return false;
        }

        noticeId = request.getParameter(ServiceConstant.PARA_NOTICEID);
        if (!check(noticeId, ErrorCode.ERROR_PARAMETER_NOTICEID_EMPTY, ErrorCode.ERROR_PARAMETER_NOTICEID_NULL)) {
            return false;
        }


        return true;
    }

    @Override
    public void handleData() {
        resultCode = GroupNoticeManager.handleInvitationNotice(mongoClient, userId, noticeId, false);
        byteData = protocolBufferWithErrorCode(resultCode);

    }
}
