package com.orange.game.api.service.group.user;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.group.GroupUserManager;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-25
 * Time: 下午12:31
 * To change this template use File | Settings | File Templates.
 */
public class QuitGroupService extends CommonGameService {
    private String groupId;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        if(!setAndCheckUserIdAndAppId(request)){
            return false;
        }

        groupId = request.getParameter(ServiceConstant.PARA_GROUPID);

        if (!check(groupId, ErrorCode.ERROR_PARAMETER_GROUPID_EMPTY,
                ErrorCode.ERROR_PARAMETER_GROUPID_NULL)) {
            return false;
        }
        return true;
    }

    @Override
    public void handleData() {
        GroupUserManager.quitGroup(mongoClient, userId, groupId, gameId);
        byteData = protocolBufferWithErrorCode(0);
    }
}
