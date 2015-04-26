package com.orange.game.api.service.group.user;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-13
 * Time: 下午12:18
 * To change this template use File | Settings | File Templates.
 */

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.group.GroupUserManager;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-2
 * Time: 上午10:28
 * To change this template use File | Settings | File Templates.
 */
public class JoinGroupService extends CommonGameService {


    private String groupId;
    private String message;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        setUserIdAndAppId(request);
        if(!checkUserIdAndAppId()){
            return false;
        }

        groupId = request.getParameter(ServiceConstant.PARA_GROUPID);
        message = request.getParameter(ServiceConstant.PARA_MESSAGETEXT);


        if (!check(groupId, ErrorCode.ERROR_PARAMETER_GROUPID_EMPTY,
                ErrorCode.ERROR_PARAMETER_GROUPID_NULL)) {
            return false;
        }
        return true;
    }


    @Override
    public void handleData() {
        resultCode = GroupUserManager.joinGroup(mongoClient, userId, groupId, message);
        byteData = protocolBufferWithErrorCode(resultCode);
    }

}
