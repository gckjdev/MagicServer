package com.orange.game.api.service.group.group;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.bbs.BBSManager;
import com.orange.game.model.manager.bbs.BBSPrivilegeManager;
import com.orange.game.model.manager.group.GroupManager;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-12-31
 * Time: 下午4:32
 * To change this template use File | Settings | File Templates.
 */
public class ChargeGroupBalance extends CommonGameService {


    private String groupId;
    private String targetUid;
    private int amount;
    private boolean forceByAdmin;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {

        if (!setAndCheckUserIdAndAppId(request)) {
            return false;
        }

        amount = getIntValueFromRequest(request, ServiceConstant.PARA_AMOUNT, 0);

        groupId = request.getParameter(ServiceConstant.PARA_GROUPID);
        targetUid = request.getParameter(ServiceConstant.PARA_TARGETUSERID);

        forceByAdmin = getBoolValueFromRequest(request, ServiceConstant.PARA_FORCE_BY_ADMIN, false);

        if (!check(groupId, ErrorCode.ERROR_PARAMETER_GROUPID_EMPTY,
                ErrorCode.ERROR_PARAMETER_GROUPID_NULL)) {
            return false;
        }

        if (!forceByAdmin){
            if (amount < 0 && !check(targetUid, ErrorCode.ERROR_PARAMETER_TARGET_USERID_EMPTY,
                    ErrorCode.ERROR_PARAMETER_TARGET_USERID_NULL)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void handleData() {

        if (forceByAdmin && BBSPrivilegeManager.isSuperAdmin(mongoClient, userId)){
            resultCode = GroupManager.forceChargeBalance(mongoClient, groupId, amount);
        }
        else{
            if (amount > 0) {
                resultCode = GroupManager.chargeBalance(mongoClient, userId, groupId, amount);

            } else if (amount < 0) {
                resultCode = GroupManager.transferBalance(mongoClient, userId, groupId, amount, targetUid);
            }
        }

        byteData = protocolBufferWithErrorCode(resultCode);
    }
}
