package com.orange.game.api.service.group.user;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.group.GroupRoleManager;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-12-17
 * Time: 上午10:15
 * To change this template use File | Settings | File Templates.
 */
public class SetUserAsAdminService extends CommonGameService {
    private String groupId;
    private String targetUid;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        groupId = request.getParameter(ServiceConstant.PARA_GROUPID);
        targetUid = request.getParameter(ServiceConstant.PARA_TARGETUSERID);


        if (!check(groupId, ErrorCode.ERROR_PARAMETER_GROUPID_EMPTY,
                ErrorCode.ERROR_PARAMETER_GROUPID_NULL)) {
            return false;
        }

        if (!check(targetUid, ErrorCode.ERROR_PARAMETER_TARGET_USERID_EMPTY,
                ErrorCode.ERROR_PARAMETER_TARGET_USERID_NULL)) {
            return false;
        }

        return true;
    }

    @Override
    public void handleData() {
        resultCode = GroupRoleManager.setUserAsAdmin(mongoClient, userId, groupId, targetUid);
        byteData = protocolBufferWithErrorCode(resultCode);
    }

    @Override
    public String toString() {
        return "SetUserAsAdminService{" +
                "groupId='" + groupId + '\'' +
                ", targetUid='" + targetUid + '\'' +
                '}';
    }
}
