package com.orange.game.api.service.group.group;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.group.FollowGroupManager;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-11
 * Time: 上午9:59
 * To change this template use File | Settings | File Templates.
 */
public class FollowGroupService extends CommonGameService {

    String groupId;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        setUserIdAndAppId(request);
        if (!checkUserIdAndAppId()){
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
        resultCode = FollowGroupManager.followGroup(mongoClient, userId, groupId);
        byteData = protocolBufferWithErrorCode(resultCode);
    }



}
