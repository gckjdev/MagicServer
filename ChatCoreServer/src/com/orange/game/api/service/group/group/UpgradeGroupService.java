package com.orange.game.api.service.group.group;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.model.manager.group.GroupManager;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-8
 * Time: 上午11:07
 * To change this template use File | Settings | File Templates.
 */
public class UpgradeGroupService extends CommonGameService {

    String groupId;
    int level;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        if (!setAndCheckUserIdAndAppId(request)) {
            return false;
        }
        groupId = request.getParameter(ServiceConstant.PARA_GROUPID);

        level = getIntValueFromRequest(request, ServiceConstant.PARA_LEVEL, 0);

        if (!check(groupId, ErrorCode.ERROR_PARAMETER_GROUPID_EMPTY,
                ErrorCode.ERROR_PARAMETER_GROUPID_NULL)) {
            return false;
        }

        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void handleData() {
        //To change body of implemented methods use File | Settings | File Templates.
        resultCode = GroupManager.upgradeGroup(mongoClient, userId, groupId, level);
        byteData = protocolBufferWithErrorCode(resultCode);
    }

}
