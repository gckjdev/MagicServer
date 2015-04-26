package com.orange.game.api.service.group.user;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.group.GroupUserManager;
import com.orange.game.model.manager.group.index.UserNoticeIndexManager;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-2
 * Time: 上午10:28
 * To change this template use File | Settings | File Templates.
 */
public class HandleJoinGroupRequestService extends CommonGameService {

    public final int HANDLE_TYPE_ACCEPT = 1;
    public final int HANDLE_TYPE_REJECT = 2;



    private String reason;
    private int handleType;
    private String noticeId;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        setUserIdAndAppId(request);
        if (!checkUserIdAndAppId()){
            return false;
        }
        reason = request.getParameter(ServiceConstant.PARA_MESSAGETEXT);
        handleType = getIntValueFromRequest(request, ServiceConstant.PARA_TYPE, GroupUserManager.HANDLE_TYPE_NONE);
        noticeId = request.getParameter(ServiceConstant.PARA_NOTICEID);
        if (!check(noticeId, ErrorCode.ERROR_PARAMETER_NOTICEID_EMPTY, ErrorCode.ERROR_PARAMETER_NOTICEID_NULL)) {
            return false;
        }
        return true;
    }

    @Override
    public void handleData() {
        resultCode = GroupUserManager.handleJoinRequest(mongoClient, userId, noticeId, handleType, reason, gameId);
        byteData = protocolBufferWithErrorCode(resultCode);
    }
}
