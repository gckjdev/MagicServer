package com.orange.game.api.service.group.user;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.group.GroupTitle;
import com.orange.game.model.manager.group.GroupTitleManager;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-12-3
 * Time: 下午5:53
 * To change this template use File | Settings | File Templates.
 */
public class ChangeUserTitleService extends CommonGameService {

    private String groupId;
    private String targetUid;
    private int sourceTitleId;
    private int targetTitleId;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        if (!setAndCheckUserIdAndAppId(request)){
            return false;
        }
        groupId = request.getParameter(ServiceConstant.PARA_GROUPID);
        targetUid = request.getParameter(ServiceConstant.PARA_TARGETUSERID);
        sourceTitleId = getIntValueFromRequest(request, ServiceConstant.PARA_SOURCE_TITLEID, GroupTitle.MEMBER);
        targetTitleId = getIntValueFromRequest(request, ServiceConstant.PARA_TITLE_ID, GroupTitle.MEMBER);


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
        resultCode = GroupTitleManager.updateUserGroupTitle(mongoClient, userId, groupId, targetUid, sourceTitleId, targetTitleId);
        byteData = getPBDataByErrorCode(resultCode);
    }

    @Override
    public String toString() {
        return "ChangeUserTitleService{" +
                "groupId='" + groupId + '\'' +
                ", targetUid='" + targetUid + '\'' +
                ", sourceTitleId=" + sourceTitleId +
                ", targetTitleId=" + targetTitleId +
                '}';
    }
}
