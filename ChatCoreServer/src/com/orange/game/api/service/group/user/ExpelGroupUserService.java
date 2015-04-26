package com.orange.game.api.service.group.user;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.group.GroupRole;
import com.orange.game.model.dao.group.GroupTitle;
import com.orange.game.model.manager.group.GroupUserManager;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-6
 * Time: 上午10:53
 * To change this template use File | Settings | File Templates.
 */
public class ExpelGroupUserService extends CommonGameService {
    private String groupId;
    private String targetUId;
    private String message;
    private int titleId;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        setUserIdAndAppId(request);
        if (!checkUserIdAndAppId()){
            return false;
        }
        groupId = request.getParameter(ServiceConstant.PARA_GROUPID);
        targetUId = request.getParameter(ServiceConstant.PARA_TARGETUSERID);
        message = request.getParameter(ServiceConstant.PARA_MESSAGETEXT);
        titleId = getIntValueFromRequest(request, ServiceConstant.PARA_TITLE_ID, GroupTitle.MEMBER);

        if (!check(groupId, ErrorCode.ERROR_PARAMETER_GROUPID_EMPTY,
                ErrorCode.ERROR_PARAMETER_GROUPID_NULL)) {
            return false;
        }

        if (!check(targetUId, ErrorCode.ERROR_PARAMETER_TARGET_USERID_EMPTY,
                ErrorCode.ERROR_PARAMETER_TARGET_USERID_NULL)) {
            return false;
        }
        return true;
    }

    @Override
    public void handleData() {
        resultCode = GroupUserManager.expelUser(mongoClient, userId, groupId, targetUId, titleId, gameId, message);
        byteData = protocolBufferWithErrorCode(resultCode);
    }

    @Override
    public String toString() {
        return "ExpelGroupUserService{" +
                "groupId='" + groupId + '\'' +
                ", targetUId='" + targetUId + '\'' +
                ", titleId='" + titleId + '\'' +
                '}';
    }

}
