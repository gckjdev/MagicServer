package com.orange.game.api.service.group.notice;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.group.GroupNoticeManager;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-23
 * Time: 下午2:37
 * To change this template use File | Settings | File Templates.
 */
public class IgnoreNoticeService extends CommonGameService {
    private String noticeId;
    private int type;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        if (!setAndCheckUserIdAndAppId(request)) {
            return false;
        }

        noticeId = request.getParameter(ServiceConstant.PARA_NOTICEID);
        if (!check(noticeId, ErrorCode.ERROR_PARAMETER_NOTICEID_EMPTY, ErrorCode.ERROR_PARAMETER_NOTICEID_NULL)) {
            return false;
        }
        type = getIntValueFromRequest(request, ServiceConstant.PARA_TYPE, GroupNoticeManager.NOTICE_TYPE_NOTICE);
        return true;
    }

    @Override
    public void handleData() {
        GroupNoticeManager.ignoreNotice(mongoClient, userId, noticeId);
        byteData = protocolBufferWithErrorCode(0);
    }
}
