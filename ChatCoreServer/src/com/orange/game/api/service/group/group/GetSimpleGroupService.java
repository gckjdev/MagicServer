package com.orange.game.api.service.group.group;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.group.Group;
import com.orange.game.model.manager.group.GroupManager;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 14-1-7
 * Time: 上午11:27
 * To change this template use File | Settings | File Templates.
 */
public class GetSimpleGroupService extends CommonGameService {
    String groupId;


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
        Group group = GroupManager.getSimpleGroup(mongoClient, groupId);
        byteData = CommonServiceUtils.groupToPB(group);
    }
}
