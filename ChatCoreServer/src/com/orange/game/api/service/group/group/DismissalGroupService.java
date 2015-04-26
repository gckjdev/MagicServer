package com.orange.game.api.service.group.group;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.group.GroupManager;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-12-27
 * Time: 下午4:07
 * To change this template use File | Settings | File Templates.
 */
public class DismissalGroupService extends CommonGameService {

    private String groupId;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        if (!setAndCheckUserIdAndAppId(request)){
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
        resultCode = GroupManager.dismissalGroup(mongoClient, userId, groupId);
        byteData = protocolBufferWithErrorCode(resultCode);
    }
}
