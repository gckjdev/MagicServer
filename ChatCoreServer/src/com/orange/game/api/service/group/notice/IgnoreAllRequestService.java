package com.orange.game.api.service.group.notice;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.model.manager.group.index.UserNoticeIndexManager;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 14-1-22
 * Time: 下午1:44
 * To change this template use File | Settings | File Templates.
 */
public class IgnoreAllRequestService extends CommonGameService {
    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        if (!setAndCheckUserIdAndAppId(request)){
            return false;
        }
        return true;
    }

    @Override
    public void handleData() {
        UserNoticeIndexManager.getRequestManager().removeIndex(userId, true);
        byteData = protocolBufferWithErrorCode(0);
    }
}
