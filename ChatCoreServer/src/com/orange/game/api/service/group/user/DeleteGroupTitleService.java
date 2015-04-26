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
 * Date: 13-12-10
 * Time: 下午3:04
 * To change this template use File | Settings | File Templates.
 */
public class DeleteGroupTitleService extends CommonGameService {
    private String groupId;
    private int titleId;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        if (!setAndCheckUserIdAndAppId(request)) {
            return false;
        }

        groupId = request.getParameter(ServiceConstant.PARA_GROUPID);

        if (!check(groupId, ErrorCode.ERROR_PARAMETER_GROUPID_EMPTY,
                ErrorCode.ERROR_PARAMETER_GROUPID_NULL)) {
            return false;
        }


        titleId = getIntValueFromRequest(request, ServiceConstant.PARA_TITLE_ID, GroupTitle.MEMBER);

        if (titleId == GroupTitle.MEMBER) {
            resultCode = ErrorCode.ERROR_GROUP_TITLEID_EXISTED;
            return false;
        }

        return true;
    }

    @Override
    public void handleData() {
        resultCode = GroupTitleManager.deleteTitle(mongoClient, userId, groupId, titleId);
        byteData = protocolBufferWithErrorCode(resultCode);
    }

    @Override
    public String toString() {
        return "DeleteGroupTitleService{" +
                "groupId='" + groupId + '\'' +
                ", titleId=" + titleId +
                '}';
    }
}
