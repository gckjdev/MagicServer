package com.orange.game.api.service.group.group;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.group.Group;
import com.orange.game.model.manager.group.GroupManager;
import com.orange.game.model.manager.group.exception.GroupException;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-2
 * Time: 上午10:24
 * To change this template use File | Settings | File Templates.
 */
public class CreateGroupService extends CommonGameService {

    String name;
    int level;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        setUserIdAndAppId(request);
        if (!checkUserIdAndAppId()){
            return false;
        }
        name = request.getParameter(ServiceConstant.PARA_NAME);
        level = getIntValueFromRequest(request, ServiceConstant.PARA_LEVEL, 1);


        if (!check(name, ErrorCode.ERROR_PARAMETER_NAME_EMPTY,
                ErrorCode.ERROR_PARAMETER_NAME_NULL)) {
            return false;
        }

        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void handleData() {

        try {
            Group group = GroupManager.createGroup(mongoClient, userId, gameId, name, level);
            byteData = CommonServiceUtils.groupToPB(group);
        } catch (GroupException e) {
            resultCode = e.getErrorCode();
            byteData = protocolBufferWithErrorCode(resultCode);
        }
    }
}
