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
 * Time: 下午6:23
 * To change this template use File | Settings | File Templates.
 */
public class CreateGroupTitleService extends CommonGameService {

    private String title;
    private int titleId;
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


        titleId = getIntValueFromRequest(request, ServiceConstant.PARA_TITLE_ID, GroupTitle.NONE);
        title = request.getParameter(ServiceConstant.PARA_TITLE);

        if (titleId == GroupTitle.MEMBER){
            resultCode = ErrorCode.ERROR_GROUP_TITLEID_EXISTED;
            return false;
        }
        return true;
    }

    @Override
    public void handleData() {
        resultCode = GroupTitleManager.createTitle(mongoClient, userId, groupId, titleId, title);
        byteData = protocolBufferWithErrorCode(resultCode);
    }

    @Override
    public String toString() {
        return "CreateGroupTitleService{" +
                "title='" + title + '\'' +
                ", titleId=" + titleId +
                ", groupId='" + groupId + '\'' +
                '}';
    }
}
