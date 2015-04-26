package com.orange.game.api.service.group.user;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.group.GroupTitleManager;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-12-13
 * Time: 下午3:47
 * To change this template use File | Settings | File Templates.
 */
public class UpdateTitleNameService extends CommonGameService {


    private int titleId;
    private String groupId;
    private String title;

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

        titleId = getIntValueFromRequest(request, ServiceConstant.PARA_TITLE_ID, -1);
        title = request.getParameter(ServiceConstant.PARA_TITLE);

        if (titleId == -1){
            resultCode = ErrorCode.ERROR_GROUP_TITLEID_NOTEXISTED;
            return false;
        }
        return true;
    }

    @Override
    public void handleData() {
        resultCode = GroupTitleManager.updateGroupTitle(mongoClient, userId, groupId, titleId, title);
        byteData = protocolBufferWithErrorCode(resultCode);
    }

    @Override
    public String toString() {
        return "UpdateTitleNameService{" +
                "titleId=" + titleId +
                ", groupId='" + groupId + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
