package com.orange.game.api.service.group.group;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.group.GroupManager;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-6
 * Time: 上午10:54
 * To change this template use File | Settings | File Templates.
 */
public class EditGroupService extends CommonGameService {


	private String name;
	private String description;
	private String signature;

    private int fee;
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

        name = request.getParameter(ServiceConstant.PARA_NAME);
        description = request.getParameter(ServiceConstant.PARA_DESC);
        signature = request.getParameter(ServiceConstant.PARA_SIGNATURE);
        fee = getIntValueFromRequest(request, ServiceConstant.PARA_FEE, -1);

        return true;
    }

    @Override
    public void handleData() {
        resultCode = GroupManager.editGroup(mongoClient, userId, groupId, name, description, signature, fee);
        byteData = protocolBufferWithErrorCode(resultCode);
    }
}
